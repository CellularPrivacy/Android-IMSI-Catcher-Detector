"""Pydantic request/response models. Mirror ../contracts/*.schema.json."""

from datetime import datetime
from typing import Dict, List, Literal, Optional

from pydantic import BaseModel, Field

Faction = Literal["INF", "PRM", "DEF", "UNT", "EQB", "PKB"]
ThreatLevel = Literal["IDLE", "OK", "MEDIUM", "HIGH", "DANGER"]
AgentStatus = Literal["online", "offline", "stealth"]


class CellInfo(BaseModel):
    mcc: Optional[int] = None
    mnc: Optional[int] = None
    lac: Optional[int] = None
    cid: Optional[int] = None
    rat: Optional[str] = None


class TelemetryEvent(BaseModel):
    event_id: int = Field(..., description="AIMSICD DF_id.")
    device_id: Optional[str] = None
    faction: Optional[Faction] = None
    threat_level: ThreatLevel
    description: Optional[str] = None
    cell: Optional[CellInfo] = None
    apn: Optional[str] = None
    sim_fingerprint_sha256: Optional[str] = Field(
        None, pattern="^[0-9a-f]{64}$"
    )
    app_version: Optional[str] = None
    occurred_at: datetime


class TelemetryAck(BaseModel):
    id: int


class ErrorReport(BaseModel):
    app: Literal["aimsicd-android", "prometheus-backend", "prometheus-web"]
    app_version: Optional[str] = None
    os: Optional[str] = None
    device_id: Optional[str] = None
    exception_type: str
    message: Optional[str] = None
    stack_trace: str
    breadcrumbs: List[str] = []
    occurred_at: datetime


class AgentState(BaseModel):
    agent_id: str
    faction: Faction
    role: Optional[str] = None
    x: Optional[float] = None
    y: Optional[float] = None
    sector: Optional[str] = None
    status: AgentStatus
    masked: bool = False
    last_seen: datetime


class Heartbeat(BaseModel):
    role: Optional[str] = None
    x: Optional[float] = None
    y: Optional[float] = None
    sector: Optional[str] = None
    status: AgentStatus = "online"


class Sector(BaseModel):
    code: Faction
    name: str
    mandate: str
    x: float
    y: float


class DeviceTokenRequest(BaseModel):
    device_id: str = Field(..., min_length=1, max_length=128)
    faction: Faction


class DeviceTokenResponse(BaseModel):
    token: str
    expires_at: datetime


class HealthResponse(BaseModel):
    status: str
    database: str
    claims: Dict[str, str] = {}


def dump(model):
    """Version-agnostic model → dict (pydantic v2 .model_dump / v1 .dict)."""
    if hasattr(model, "model_dump"):
        return model.model_dump()
    return model.dict()
