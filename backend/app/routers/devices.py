"""Field-node provisioning: mint device JWTs with the provision key."""

from fastapi import APIRouter, Header, HTTPException

from ..auth import AuthError, check_provision_key, mint_device_token
from ..models import DeviceTokenRequest, DeviceTokenResponse

router = APIRouter(prefix="/v1/devices", tags=["devices"])


@router.post("/token", response_model=DeviceTokenResponse, status_code=201)
async def mint_token(
    body: DeviceTokenRequest,
    x_provision_key: str = Header(None, alias="X-Provision-Key"),
):
    if not check_provision_key(x_provision_key):
        raise HTTPException(status_code=403, detail="bad provision key")
    try:
        token, expires = mint_device_token(body.device_id, body.faction)
    except AuthError as exc:
        raise HTTPException(status_code=500, detail=str(exc))
    return DeviceTokenResponse(token=token, expires_at=expires)
