"""Faction RBAC/ABAC masking for agent presence.

Stdlib only and side-effect free so it is unit-testable without dependencies
(see ``tests/test_masking.py``) and reusable from any transport.

Rules:
  * Same faction as the viewer  -> full coordinates.
  * Viewer holds ``command`` or ``watch`` in INF or DEF -> full coordinates,
    except stealth agents of other factions (need INF ``command``).
  * Stealth agent, other faction, viewer without INF ``command`` -> masked.
  * Everyone else -> masked (sector stays visible, x/y nulled).
"""

FACTIONS = frozenset({"INF", "PRM", "DEF", "UNT", "EQB", "PKB"})

_PRIVILEGED_FACTIONS = frozenset({"INF", "DEF"})
_PRIVILEGED_ROLES = frozenset({"command", "watch"})


def mask_agent_for_viewer(agent, viewer_faction, viewer_roles):
    """Return a copy of ``agent`` with coordinates masked unless permitted.

    :param agent: dict with at least ``faction``, ``status``, ``x``, ``y``.
    :param viewer_faction: faction code from the viewer's JWT (may be None).
    :param viewer_roles: iterable of role strings from the viewer's JWT.
    """
    roles = set(viewer_roles or [])
    agent_faction = agent.get("faction")
    is_stealth = agent.get("status") == "stealth"
    same_faction = (
        viewer_faction is not None and viewer_faction == agent_faction
    )
    privileged = bool(roles & _PRIVILEGED_ROLES) and (
        viewer_faction in _PRIVILEGED_FACTIONS
    )
    inf_command = viewer_faction == "INF" and "command" in roles

    if same_faction or (privileged and not is_stealth) or inf_command:
        masked = dict(agent)
        masked["masked"] = False
        return masked

    masked = dict(agent)
    masked["x"] = None
    masked["y"] = None
    masked["masked"] = True
    return masked


def can_read_error_history(viewer_faction, viewer_roles):
    """Crash history is restricted to INF/DEF command & watch roles."""
    roles = set(viewer_roles or [])
    return bool(roles & _PRIVILEGED_ROLES) and (
        viewer_faction in _PRIVILEGED_FACTIONS
    )
