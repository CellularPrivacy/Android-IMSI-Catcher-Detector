"""Offline device-token minter (operator CLI).

Usage:
    JWT_SECRET=... DEVICE_JWT_TTL_SECONDS=2592000 \
        python -m app.provision --device-id def-sensor-01 --faction DEF
"""

import argparse
import sys

from .auth import AuthError, mint_device_token
from .masking import FACTIONS


def main(argv=None):
    parser = argparse.ArgumentParser(description="Mint a Prometheus device JWT.")
    parser.add_argument("--device-id", required=True)
    parser.add_argument("--faction", required=True, choices=sorted(FACTIONS))
    args = parser.parse_args(argv)
    try:
        token, expires = mint_device_token(args.device_id, args.faction)
    except AuthError as exc:
        print("error: %s" % exc, file=sys.stderr)
        return 1
    print("token: %s" % token)
    print("expires_at: %s" % expires.isoformat())
    return 0


if __name__ == "__main__":
    sys.exit(main())
