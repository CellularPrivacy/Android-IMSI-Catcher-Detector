"""JWT authentication: short-lived operator tokens + device tokens.

Two verification modes (decided by env, see ``config``):
  * HS256 with ``JWT_SECRET`` (simple deployments);
  * RS256 via Keycloak/Auth0 JWKS (``OIDC_JWKS_URL``), keys cached 10 minutes.

Device tokens are minted by ``POST /v1/devices/token`` (provision-key
protected) or offline via ``python -m app.provision``.
"""

import hmac
import json
import time
import urllib.request
from datetime import datetime, timedelta, timezone

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from .config import settings

_bearer = HTTPBearer(auto_error=False)

_jwks_cache = {"fetched_at": 0.0, "keys": {}}
_JWKS_TTL_SECONDS = 600


class AuthError(Exception):
    pass


def _fetch_jwks():
    now = time.time()
    if _jwks_cache["keys"] and now - _jwks_cache["fetched_at"] < _JWKS_TTL_SECONDS:
        return _jwks_cache["keys"]
    request = urllib.request.Request(
        settings.OIDC_JWKS_URL, headers={"Accept": "application/json"}
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        document = json.loads(response.read().decode("utf-8"))
    keys = {
        entry["kid"]: entry
        for entry in document.get("keys", [])
        if entry.get("kid")
    }
    _jwks_cache.update({"fetched_at": now, "keys": keys})
    return keys


def decode_token(token):
    """Verify signature + expiry (+ issuer) and return the claims dict."""
    try:
        if settings.OIDC_JWKS_URL:
            header = jwt.get_unverified_header(token)
            keys = _fetch_jwks()
            jwk = keys.get(header.get("kid"))
            if jwk is None:
                raise AuthError("unknown key id")
            key = jwt.algorithms.RSAAlgorithm.from_jwk(json.dumps(jwk))
            return jwt.decode(
                token,
                key,
                algorithms=["RS256"],
                issuer=settings.JWT_ISSUER,
                options={"require": ["exp", "iss", "sub"]},
            )
        return jwt.decode(
            token,
            settings.JWT_SECRET,
            algorithms=["HS256"],
            issuer=settings.JWT_ISSUER,
            options={"require": ["exp", "iss", "sub"]},
        )
    except AuthError:
        raise
    except Exception as exc:
        raise AuthError(str(exc))


def mint_device_token(device_id, faction):
    """Mint a device JWT (sub=device_id, roles=[device])."""
    if not settings.JWT_SECRET:
        raise AuthError("device tokens require JWT_SECRET (HS256 mode)")
    now = datetime.now(timezone.utc)
    expires = now + timedelta(seconds=settings.DEVICE_JWT_TTL_SECONDS)
    payload = {
        "iss": settings.JWT_ISSUER,
        "sub": device_id,
        "faction": faction,
        "roles": ["device"],
        "iat": int(now.timestamp()),
        "exp": int(expires.timestamp()),
    }
    return jwt.encode(payload, settings.JWT_SECRET, algorithm="HS256"), expires


def check_provision_key(provided):
    return bool(settings.PROVISION_KEY) and hmac.compare_digest(
        provided or "", settings.PROVISION_KEY
    )


def _unauthorized(detail="invalid or expired token"):
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=detail,
        headers={"WWW-Authenticate": "Bearer"},
    )


async def get_current_claims(
    credentials: HTTPAuthorizationCredentials = Depends(_bearer),
):
    if credentials is None or credentials.scheme.lower() != "bearer":
        raise _unauthorized("missing bearer token")
    try:
        return decode_token(credentials.credentials)
    except AuthError:
        raise _unauthorized()


def viewer_of(claims):
    """Extract (faction, roles) for the masking layer."""
    return claims.get("faction"), list(claims.get("roles") or [])
