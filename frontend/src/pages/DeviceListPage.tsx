import { useState } from "react";
import { useDevices } from "../hooks/useDevices";
import { DeviceTable } from "../components/DeviceTable";
import type { DeviceType } from "../types/device";

const DEVICE_TYPES: DeviceType[] = ["FIREWALL", "ROUTER", "SWITCH"];

export function DeviceListPage() {
  const [typeFilter, setTypeFilter] = useState<DeviceType | undefined>(undefined);
  const { devices, loading, error, reload } = useDevices(typeFilter);

  return (
    <section>
      <div className="page-header">
        <h2>Inventario de dispositivos</h2>
        <div className="filters">
          <label htmlFor="type-filter">Filtrar por tipo</label>
          <select
            id="type-filter"
            value={typeFilter ?? ""}
            onChange={(e) =>
              setTypeFilter(e.target.value ? (e.target.value as DeviceType) : undefined)
            }
          >
            <option value="">Todos</option>
            {DEVICE_TYPES.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
          <button type="button" onClick={reload}>
            Refrescar
          </button>
        </div>
      </div>

      {loading && <p>Cargando dispositivos…</p>}
      {error && <p className="error-message">{error}</p>}
      {!loading && !error && <DeviceTable devices={devices} />}
    </section>
  );
}
