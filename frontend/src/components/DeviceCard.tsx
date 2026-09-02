import type { Device } from "../types/device";
import { StatusBadge } from "./StatusBadge";

interface DeviceCardProps {
  device: Device;
}

export function DeviceCard({ device }: DeviceCardProps) {
  return (
    <div className="card">
      <div className="card__header">
        <h3>{device.name}</h3>
        <StatusBadge status={device.status} />
      </div>
      <dl className="card__details">
        <dt>Tipo</dt>
        <dd>{device.type}</dd>
        <dt>IP</dt>
        <dd>{device.ipAddress}</dd>
        <dt>Vendor</dt>
        <dd>{device.vendor}</dd>
        <dt>Responsable</dt>
        <dd>{device.managedBy}</dd>
      </dl>
    </div>
  );
}
