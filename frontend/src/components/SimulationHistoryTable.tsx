import type { SimulationResult } from "../types/simulation";

interface SimulationHistoryTableProps {
  history: SimulationResult[];
}

export function SimulationHistoryTable({ history }: SimulationHistoryTableProps) {
  if (history.length === 0) {
    return <p className="empty-state">Aún no se ha registrado ninguna simulación.</p>;
  }

  return (
    <table className="table">
      <thead>
        <tr>
          <th>Fecha</th>
          <th>Dispositivo</th>
          <th>Script</th>
          <th>Parámetros</th>
          <th>Resultado</th>
          <th>Output</th>
        </tr>
      </thead>
      <tbody>
        {history.map((entry) => (
          <tr key={entry.id}>
            <td>{new Date(entry.executedAt).toLocaleString()}</td>
            <td>{entry.deviceName}</td>
            <td>{entry.scriptName}</td>
            <td>
              <code>{JSON.stringify(entry.parameters)}</code>
            </td>
            <td>{entry.success ? "✅ éxito (simulado)" : "❌ falló (simulado)"}</td>
            <td>{entry.output}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
