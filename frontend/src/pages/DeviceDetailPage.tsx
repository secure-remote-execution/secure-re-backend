import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { deviceService } from "../services/deviceService";
import { DeviceCard } from "../components/DeviceCard";
import type { Device } from "../types/device";
import { ApiError } from "../services/httpClient";

export function DeviceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [device, setDevice] = useState<Device | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);
    deviceService
      .getDeviceById(id)
      .then(setDevice)
      .catch((err: ApiError) => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  return (
    <section>
      <div className="page-header">
        <h2>Detalle del dispositivo</h2>
        <Link to="/devices">← Volver al inventario</Link>
      </div>

      {loading && <p>Cargando dispositivo…</p>}
      {error && <p className="error-message">{error}</p>}
      {!loading && !error && device && (
        <>
          <DeviceCard device={device} />
          <p className="hint">
            Último cambio de configuración: {new Date(device.lastConfigChange).toLocaleString()}
          </p>
          <Link className="button-link" to={`/simulate?deviceId=${device.id}`}>
            Simular ejecución de script sobre este dispositivo
          </Link>
        </>
      )}
    </section>
  );
}
