import { Link } from "react-router-dom";
import type { Device } from "../types/device";
import { StatusBadge } from "./StatusBadge";

interface DeviceTableProps {
  devices: Device[];
}

export function DeviceTable({ devices }: DeviceTableProps) {
  if (devices.length === 0) {
    return <p className="empty-state">No hay dispositivos que coincidan con el filtro.</p>;
  }

  return (
    <table className="table">
      <thead>
        <tr>
          <th>Nombre</th>
          <th>Tipo</th>
          <th>IP</th>
          <th>Vendor</th>
          <th>Estado</th>
          <th>Responsable</th>
        </tr>
      </thead>
      <tbody>
        {devices.map((device) => (
          <tr key={device.id}>
            <td>
              <Link to={`/devices/${device.id}`}>{device.name}</Link>
            </td>
            <td>{device.type}</td>
            <td>{device.ipAddress}</td>
            <td>{device.vendor}</td>
            <td>
              <StatusBadge status={device.status} />
            </td>
            <td>{device.managedBy}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
