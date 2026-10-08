"""Agent presence: faction-masked list + self heartbeat."""

from fastapi import APIRouter, Depends, HTTPException

from .. import db
from ..auth import get_current_claims, viewer_of
from ..masking import mask_agent_for_viewer
from ..models import AgentState, Heartbeat

router = APIRouter(prefix="/v1/agents", tags=["agents"])


@router.get("", response_model=list)
async def list_agents(claims: dict = Depends(get_current_claims)):
    faction, roles = viewer_of(claims)
    try:
        agents = db.list_agents()
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database unavailable: %s" % exc)
    return [mask_agent_for_viewer(agent, faction, roles) for agent in agents]


@router.post("/heartbeat", response_model=AgentState)
async def heartbeat(body: Heartbeat, claims: dict = Depends(get_current_claims)):
    # Identity comes from the token, never from the body (no spoofing).
    agent = {
        "agent_id": claims.get("sub"),
        "faction": claims.get("faction"),
        "role": body.role,
        "x": body.x,
        "y": body.y,
        "sector": body.sector,
        "status": body.status,
    }
    if not agent["agent_id"] or not agent["faction"]:
        raise HTTPException(status_code=403, detail="token lacks sub/faction")
    try:
        db.upsert_agent(agent)
        stored = [
            row for row in db.list_agents() if row["agent_id"] == agent["agent_id"]
        ]
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database unavailable: %s" % exc)
    if not stored:
        raise HTTPException(status_code=503, detail="heartbeat not persisted")
    faction, roles = viewer_of(claims)
    return mask_agent_for_viewer(stored[0], faction, roles)
