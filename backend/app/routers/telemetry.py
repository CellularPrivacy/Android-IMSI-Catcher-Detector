"""Detection-telemetry ingest + history."""

from fastapi import APIRouter, Depends, HTTPException, Query
from fastapi.encoders import jsonable_encoder

from .. import db
from ..auth import get_current_claims
from ..models import TelemetryAck, TelemetryEvent, dump
from ..stream import hub

router = APIRouter(prefix="/v1/telemetry", tags=["telemetry"])


@router.post("", response_model=TelemetryAck, status_code=201)
async def ingest(event: TelemetryEvent, claims: dict = Depends(get_current_claims)):
    payload = dump(event)
    # Devices report for themselves: the token subject wins over the body.
    if "device" in (claims.get("roles") or []):
        payload["device_id"] = claims.get("sub")
        payload["faction"] = claims.get("faction")
    try:
        row_id = db.insert_telemetry(payload)
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database unavailable: %s" % exc)
    # jsonable_encoder: datetimes are not JSON-serializable for the WS fan-out.
    await hub.broadcast(jsonable_encoder({"type": "telemetry", "id": row_id, "event": payload}))
    return TelemetryAck(id=row_id)


@router.get("/recent")
async def recent(
    limit: int = Query(50, ge=1, le=200),
    claims: dict = Depends(get_current_claims),
):
    try:
        return db.list_recent_telemetry(limit)
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database unavailable: %s" % exc)
