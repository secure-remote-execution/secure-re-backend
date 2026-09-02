import { useState, type FormEvent } from "react";
import { useSearchParams } from "react-router-dom";
import { useDevices } from "../hooks/useDevices";
import { useSimulationHistory } from "../hooks/useSimulationHistory";
import { scriptService } from "../services/scriptService";
import { SimulationHistoryTable } from "../components/SimulationHistoryTable";
import type { SimulationResult } from "../types/simulation";
import { ApiError } from "../services/httpClient";

interface ParamRow {
  key: string;
  value: string;
}

export function ScriptSimulatorPage() {
  const [searchParams] = useSearchParams();
  const { devices } = useDevices();
  const { history, reload: reloadHistory } = useSimulationHistory();

  const [deviceId, setDeviceId] = useState(searchParams.get("deviceId") ?? "");
  const [scriptName, setScriptName] = useState("");
  const [paramRows, setParamRows] = useState<ParamRow[]>([{ key: "", value: "" }]);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<SimulationResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  function updateRow(index: number, field: keyof ParamRow, value: string) {
    setParamRows((rows) => rows.map((row, i) => (i === index ? { ...row, [field]: value } : row)));
  }

  function addRow() {
    setParamRows((rows) => [...rows, { key: "", value: "" }]);
  }

  function removeRow(index: number) {
    setParamRows((rows) => rows.filter((_, i) => i !== index));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setResult(null);

    const parameters = paramRows
      .filter((row) => row.key.trim().length > 0)
      .reduce<Record<string, string>>((acc, row) => {
        acc[row.key.trim()] = row.value;
        return acc;
      }, {});

    try {
      const response = await scriptService.simulate({ deviceId, scriptName, parameters });
      setResult(response);
      reloadHistory();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Error inesperado al simular el script");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section>
      <div className="page-header">
        <h2>Simulador de scripts</h2>
      </div>
      <p className="hint">
        Esta acción NO ejecuta nada real sobre el dispositivo: solo queda registrada como
        evidencia en el historial de simulaciones.
      </p>

      <form className="form" onSubmit={handleSubmit}>
        <label htmlFor="device-select">Dispositivo</label>
        <select
          id="device-select"
          value={deviceId}
          onChange={(e) => setDeviceId(e.target.value)}
          required
        >
          <option value="" disabled>
            Selecciona un dispositivo
          </option>
          {devices.map((device) => (
            <option key={device.id} value={device.id}>
              {device.name} ({device.ipAddress})
            </option>
          ))}
        </select>

        <label htmlFor="script-name">Nombre del script</label>
        <input
          id="script-name"
          type="text"
          placeholder="ej: backup-config.sh"
          value={scriptName}
          onChange={(e) => setScriptName(e.target.value)}
          required
        />

        <fieldset>
          <legend>Parámetros</legend>
          {paramRows.map((row, index) => (
            <div className="param-row" key={index}>
              <input
                type="text"
                placeholder="clave"
                value={row.key}
                onChange={(e) => updateRow(index, "key", e.target.value)}
              />
              <input
                type="text"
                placeholder="valor"
                value={row.value}
                onChange={(e) => updateRow(index, "value", e.target.value)}
              />
              <button type="button" onClick={() => removeRow(index)} aria-label="Eliminar parámetro">
                ✕
              </button>
            </div>
          ))}
          <button type="button" onClick={addRow}>
            + Agregar parámetro
          </button>
        </fieldset>

        <button type="submit" disabled={submitting || !deviceId || !scriptName}>
          {submitting ? "Simulando…" : "Simular ejecución"}
        </button>
      </form>

      {error && <p className="error-message">{error}</p>}
      {result && (
        <div className="result-box">
          <strong>{result.success ? "✅ Simulación completada" : "❌ Simulación falló"}</strong>
          <p>{result.output}</p>
        </div>
      )}

      <h3>Historial de simulaciones</h3>
      <SimulationHistoryTable history={history} />
    </section>
  );
}
