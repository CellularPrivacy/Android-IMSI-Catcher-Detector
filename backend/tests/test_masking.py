"""Unit tests for faction RBAC/ABAC masking. Run: python -m unittest."""

import os
import sys
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.masking import can_read_error_history, mask_agent_for_viewer


def _agent(faction="DEF", status="online"):
    return {
        "agent_id": "def-1",
        "faction": faction,
        "role": "patrol",
        "x": 10.0,
        "y": 20.0,
        "sector": "DEF",
        "status": status,
        "last_seen": "2026-09-21T00:00:00+00:00",
    }


class MaskingTest(unittest.TestCase):
    def test_same_faction_sees_full(self):
        out = mask_agent_for_viewer(_agent("DEF"), "DEF", ["operator"])
        self.assertFalse(out["masked"])
        self.assertEqual(out["x"], 10.0)

    def test_other_faction_masked(self):
        out = mask_agent_for_viewer(_agent("DEF"), "PRM", ["operator"])
        self.assertTrue(out["masked"])
        self.assertIsNone(out["x"])
        self.assertIsNone(out["y"])
        self.assertEqual(out["sector"], "DEF")  # sector stays visible

    def test_def_watch_sees_full(self):
        out = mask_agent_for_viewer(_agent("PRM"), "DEF", ["watch"])
        self.assertFalse(out["masked"])

    def test_privileged_role_wrong_faction_still_masked(self):
        out = mask_agent_for_viewer(_agent("PRM"), "UNT", ["command"])
        self.assertTrue(out["masked"])

    def test_stealth_hidden_from_privileged(self):
        out = mask_agent_for_viewer(_agent("PKB", "stealth"), "DEF", ["command"])
        self.assertTrue(out["masked"])

    def test_stealth_visible_to_same_faction(self):
        out = mask_agent_for_viewer(_agent("PKB", "stealth"), "PKB", ["operator"])
        self.assertFalse(out["masked"])

    def test_stealth_visible_to_inf_command(self):
        out = mask_agent_for_viewer(_agent("PKB", "stealth"), "INF", ["command"])
        self.assertFalse(out["masked"])

    def test_anonymous_masked(self):
        out = mask_agent_for_viewer(_agent("INF"), None, [])
        self.assertTrue(out["masked"])

    def test_input_not_mutated(self):
        agent = _agent("PRM")
        mask_agent_for_viewer(agent, "DEF", ["operator"])
        self.assertEqual(agent["x"], 10.0)
        self.assertNotIn("masked", agent)


class ErrorHistoryTest(unittest.TestCase):
    def test_allowed(self):
        self.assertTrue(can_read_error_history("DEF", ["watch"]))
        self.assertTrue(can_read_error_history("INF", ["command"]))

    def test_denied(self):
        self.assertFalse(can_read_error_history("PRM", ["command"]))
        self.assertFalse(can_read_error_history("DEF", ["operator"]))
        self.assertFalse(can_read_error_history(None, []))


if __name__ == "__main__":
    unittest.main()
