import type { DeviceStatus } from "../types/device";

const STATUS_STYLES: Record<DeviceStatus, string> = {
  ONLINE: "status-badge status-badge--online",
  OFFLINE: "status-badge status-badge--offline",
  UNKNOWN: "status-badge status-badge--unknown",
};

interface StatusBadgeProps {
  status: DeviceStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  return <span className={STATUS_STYLES[status]}>{status}</span>;
}
