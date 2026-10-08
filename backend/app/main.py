"""Prometheus Project: Infinity — FastAPI application factory."""

from contextlib import asynccontextmanager

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from starlette.middleware.base import BaseHTTPMiddleware

from . import db
from .auth import AuthError, decode_token
from .config import settings, validate
from .models import HealthResponse
from .routers import agents, devices, errors, map as map_router, telemetry
from .stream import hub


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        response = await call_next(request)
        response.headers["Strict-Transport-Security"] = (
            "max-age=63072000; includeSubDomains"
        )
        response.headers["X-Content-Type-Options"] = "nosniff"
        return response


def create_app():
    validate()

    @asynccontextmanager
    async def lifespan(app):
        db.init_pool()
        yield
        db.close_pool()

    app = FastAPI(
        title="Prometheus Project: Infinity API",
        version="2.0.0",
        docs_url="/docs",
        redoc_url=None,
        lifespan=lifespan,
    )
    app.add_middleware(SecurityHeadersMiddleware)
    if settings.CORS_ORIGINS:
        app.add_middleware(
            CORSMiddleware,
            allow_origins=settings.CORS_ORIGINS,
            allow_methods=["GET", "POST"],
            allow_headers=["Authorization", "Content-Type", "X-Provision-Key"],
            allow_credentials=False,
            max_age=3600,
        )

    app.include_router(telemetry.router)
    app.include_router(errors.router)
    app.include_router(agents.router)
    app.include_router(map_router.router)
    app.include_router(devices.router)

    @app.get("/", include_in_schema=False)
    async def root():
        return {"service": "prometheus-infinity", "api": "v1"}

    @app.get("/healthz", response_model=HealthResponse)
    async def healthz():
        database = "up" if db.check() else "down"
        return HealthResponse(
            status="ok" if database == "up" else "degraded", database=database
        )

    @app.websocket("/v1/stream")
    async def stream(websocket: WebSocket, token: str = ""):
        # Accept first: closing before accept can only send a plain HTTP
        # denial, while accept-then-close delivers the 4401 close frame.
        await websocket.accept()
        try:
            decode_token(token)
        except AuthError:
            await websocket.close(code=4401)
            return
        queue = await hub.subscribe()
        try:
            while True:
                payload = await queue.get()
                await websocket.send_json(payload)
        except WebSocketDisconnect:
            pass
        finally:
            await hub.unsubscribe(queue)

    return app


app = create_app()
