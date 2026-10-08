"""Infinity City map sectors (the 6 faction territories)."""

from fastapi import APIRouter, Depends, HTTPException

from .. import db
from ..auth import get_current_claims
from ..models import Sector

router = APIRouter(prefix="/v1/map", tags=["map"])


@router.get("/sectors", response_model=list)
async def sectors(claims: dict = Depends(get_current_claims)):
    try:
        rows = db.list_sectors()
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database unavailable: %s" % exc)
    return [Sector(**row) for row in rows]
