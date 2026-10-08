"""Environment-based configuration.

Stdlib only, and nothing is validated at import time, so unit tests can import
this module without a configured environment. :func:`validate` runs at app
startup and fails fast on missing secrets.
"""

import os


def _env(name, default=""):
    value = os.environ.get(name, default)
    return value.strip() if isinstance(value, str) else value


def _env_int(name, default):
    try:
        return int(os.environ.get(name, default))
    except (TypeError, ValueError):
        return default


class Settings:
    DATABASE_URL = _env("DATABASE_URL")
    JWT_SECRET = _env("JWT_SECRET")
    JWT_ISSUER = _env("JWT_ISSUER", "prometheus-infinity")
    JWT_TTL_SECONDS = _env_int("JWT_TTL_SECONDS", 900)
    DEVICE_JWT_TTL_SECONDS = _env_int("DEVICE_JWT_TTL_SECONDS", 30 * 24 * 3600)
    OIDC_JWKS_URL = _env("OIDC_JWKS_URL")
    PROVISION_KEY = _env("PROVISION_KEY")
    CORS_ORIGINS = [
        origin.strip()
        for origin in _env("CORS_ORIGINS", "").split(",")
        if origin.strip()
    ]


settings = Settings()


def validate():
    """Fail fast on insecure/missing configuration. Called at startup."""
    errors = []
    if not settings.DATABASE_URL:
        errors.append("DATABASE_URL is required (direct cloud Postgres, no local sim).")
    elif "sslmode=" not in settings.DATABASE_URL:
        errors.append("DATABASE_URL must enforce TLS (add ?sslmode=require).")
    if settings.OIDC_JWKS_URL:
        if not settings.OIDC_JWKS_URL.startswith("https://"):
            errors.append("OIDC_JWKS_URL must be https://.")
    elif not settings.JWT_SECRET:
        errors.append("JWT_SECRET is required unless OIDC_JWKS_URL is set.")
    if not settings.PROVISION_KEY:
        errors.append("PROVISION_KEY is required for /v1/devices/token.")
    for origin in settings.CORS_ORIGINS:
        if not origin.startswith("https://"):
            errors.append("CORS origin must be https:// (got %r)." % origin)
    if errors:
        raise RuntimeError("invalid backend configuration: " + "; ".join(errors))
