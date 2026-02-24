"""
NetworkTables publisher for vision data.
Publishes detection results to roboRIO using pynetworktables.

Supports fused (best-across-all-cameras) publishing as well as
optional per-camera debug publishing.
"""

import time
from typing import Dict, List, Optional

from networktables import NetworkTables

from config import NT_SERVER_IP, NTKeys
from detectors.base_detector import Detection


class NTPublisher:
    """Publishes vision data to NetworkTables for roboRIO consumption."""

    def __init__(self, server_ip: str = NT_SERVER_IP):
        self.server_ip = server_ip
        self._connected = False
        self._robots_table = None
        self._fuel_table = None
        # Cache of per-camera subtables
        self._camera_robots_tables: Dict[str, object] = {}
        self._camera_fuel_tables: Dict[str, object] = {}

    def connect(self) -> bool:
        """Connect to the NetworkTables server."""
        try:
            NetworkTables.initialize(server=self.server_ip)

            # Get table references for fused data
            self._robots_table = NetworkTables.getTable(NTKeys.Robots.TABLE)
            self._fuel_table = NetworkTables.getTable(NTKeys.Fuel.TABLE)

            # Set up connection listener
            NetworkTables.addConnectionListener(
                self._connection_listener,
                immediateNotify=True,
            )

            print(f"NetworkTables connecting to {self.server_ip}...")
            return True

        except Exception as e:
            print(f"Failed to initialize NetworkTables: {e}")
            return False

    def _connection_listener(self, connected: bool, info):
        """Handle connection state changes."""
        self._connected = connected
        if connected:
            print(f"NetworkTables connected to {info.remote_ip}")
        else:
            print("NetworkTables disconnected")

    @property
    def is_connected(self) -> bool:
        """Check if connected to NetworkTables server."""
        return self._connected

    def _get_camera_tables(self, camera_name: str):
        """Get or create per-camera subtables for debug publishing."""
        if camera_name not in self._camera_robots_tables:
            self._camera_robots_tables[camera_name] = NetworkTables.getTable(
                f"{NTKeys.Robots.TABLE}/{camera_name}"
            )
        if camera_name not in self._camera_fuel_tables:
            self._camera_fuel_tables[camera_name] = NetworkTables.getTable(
                f"{NTKeys.Fuel.TABLE}/{camera_name}"
            )
        return (
            self._camera_robots_tables[camera_name],
            self._camera_fuel_tables[camera_name],
        )

    def publish_robots(
        self,
        detections: List[Detection],
        closest: Optional[Detection],
        timestamp: float,
        camera_name: Optional[str] = None,
    ):
        """
        Publish robot detection data to NetworkTables.

        Args:
            detections: List of all robot detections
            closest: Closest robot detection (or None)
            timestamp: Frame timestamp
            camera_name: If set, publishes to per-camera subtable for debugging
        """
        if camera_name is not None:
            robots_table, _ = self._get_camera_tables(camera_name)
            table = robots_table
        else:
            table = self._robots_table

        if table is None:
            return

        # Robot count
        table.putNumber(NTKeys.Robots.COUNT, len(detections))

        # Closest robot data
        if closest is not None:
            table.putNumber(NTKeys.Robots.CLOSEST_X, closest.x)
            table.putNumber(NTKeys.Robots.CLOSEST_Y, closest.y)
            table.putNumber(NTKeys.Robots.CLOSEST_DISTANCE, closest.distance)
            table.putNumber(NTKeys.Robots.CLOSEST_VEL_X, closest.vel_x)
            table.putNumber(NTKeys.Robots.CLOSEST_VEL_Y, closest.vel_y)
            table.putNumber(NTKeys.Robots.CLOSEST_CONFIDENCE, closest.confidence)
        else:
            table.putNumber(NTKeys.Robots.CLOSEST_X, 0.0)
            table.putNumber(NTKeys.Robots.CLOSEST_Y, 0.0)
            table.putNumber(NTKeys.Robots.CLOSEST_DISTANCE, 0.0)
            table.putNumber(NTKeys.Robots.CLOSEST_VEL_X, 0.0)
            table.putNumber(NTKeys.Robots.CLOSEST_VEL_Y, 0.0)
            table.putNumber(NTKeys.Robots.CLOSEST_CONFIDENCE, 0.0)

        table.putNumber(NTKeys.Robots.TIMESTAMP, timestamp)

    def publish_fuel(
        self,
        detections: List[Detection],
        best: Optional[Detection],
        intake_ready: bool,
        timestamp: float,
        camera_name: Optional[str] = None,
    ):
        """
        Publish FUEL detection data to NetworkTables.

        Args:
            detections: List of all FUEL detections
            best: Best FUEL target (or None)
            intake_ready: Whether best FUEL is ready for intake
            timestamp: Frame timestamp
            camera_name: If set, publishes to per-camera subtable for debugging
        """
        if camera_name is not None:
            _, fuel_table = self._get_camera_tables(camera_name)
            table = fuel_table
        else:
            table = self._fuel_table

        if table is None:
            return

        # FUEL count and presence
        table.putNumber(NTKeys.Fuel.COUNT, len(detections))
        table.putBoolean(NTKeys.Fuel.HAS_FUEL, len(detections) > 0)

        # Best FUEL target data
        if best is not None:
            table.putNumber(NTKeys.Fuel.BEST_X, best.x)
            table.putNumber(NTKeys.Fuel.BEST_Y, best.y)
            table.putNumber(NTKeys.Fuel.BEST_DISTANCE, best.distance)
            table.putNumber(NTKeys.Fuel.BEST_ANGLE, best.angle)
            table.putNumber(NTKeys.Fuel.BEST_CONFIDENCE, best.confidence)
            table.putBoolean(
                NTKeys.Fuel.IN_INTAKE_RANGE,
                best.distance <= 0.5,
            )
        else:
            table.putNumber(NTKeys.Fuel.BEST_X, 0.0)
            table.putNumber(NTKeys.Fuel.BEST_Y, 0.0)
            table.putNumber(NTKeys.Fuel.BEST_DISTANCE, 0.0)
            table.putNumber(NTKeys.Fuel.BEST_ANGLE, 0.0)
            table.putNumber(NTKeys.Fuel.BEST_CONFIDENCE, 0.0)
            table.putBoolean(NTKeys.Fuel.IN_INTAKE_RANGE, False)

        table.putBoolean(NTKeys.Fuel.INTAKE_READY, intake_ready)
        table.putNumber(NTKeys.Fuel.TIMESTAMP, timestamp)

    def disconnect(self):
        """Disconnect from NetworkTables server."""
        try:
            NetworkTables.shutdown()
            self._connected = False
            self._robots_table = None
            self._fuel_table = None
            self._camera_robots_tables.clear()
            self._camera_fuel_tables.clear()
            print("NetworkTables disconnected")
        except Exception as e:
            print(f"Error disconnecting from NetworkTables: {e}")

    def __enter__(self):
        self.connect()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.disconnect()
        return False
