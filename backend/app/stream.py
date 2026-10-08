"""WebSocket fan-out hub for live telemetry.

Single-instance in-memory hub (asyncio queues). A multi-replica deployment
should swap this for Redis pub/sub — the router code stays unchanged.
"""

import asyncio


class Hub:
    def __init__(self):
        self._queues = set()
        self._lock = asyncio.Lock()

    async def subscribe(self):
        queue = asyncio.Queue(maxsize=100)
        async with self._lock:
            self._queues.add(queue)
        return queue

    async def unsubscribe(self, queue):
        async with self._lock:
            self._queues.discard(queue)

    async def broadcast(self, payload):
        async with self._lock:
            queues = list(self._queues)
        for queue in queues:
            try:
                queue.put_nowait(payload)
            except asyncio.QueueFull:
                # Slow consumer: drop this update rather than blocking ingest.
                continue


hub = Hub()
