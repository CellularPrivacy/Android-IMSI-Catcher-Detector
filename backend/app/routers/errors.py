"""Neural error-logger ingest + restricted history."""

from fastapi import APIRouter, Depends, HTTPException, Query

from .. import db
from ..auth import get_current_claims, viewer_of
from ..masking import can_read_error_history
from ..models import ErrorReport, TelemetryAck, dump

router = APIRouter(prefix="/v1/errors", tags=["errors"])


@router.post("", response_model=TelemetryAck, status_code=201)
async def ingest(report: ErrorReport, claims: dict = Depends(get_current_claims)):
    payload = dump(report)
    if "device" in (claims.get("roles") or []) and not payload.get("device_id"):
        payload["device_id"] = claims.get("sub")
    try:
        row_id = db.insert_error(payload)
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database unavailable: %s" % exc)
    return TelemetryAck(id=row_id)


@router.get("/recent")
async def recent(
    limit: int = Query(50, ge=1, le=200),
    claims: dict = Depends(get_current_claims),
):
    faction, roles = viewer_of(claims)
    if not can_read_error_history(faction, roles):
        raise HTTPException(status_code=403, detail="restricted to INF/DEF command/watch")
    try:
        return db.list_recent_errors(limit)
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database unavailable: %s" % exc)
