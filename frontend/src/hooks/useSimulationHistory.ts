import { useCallback, useEffect, useState } from "react";
import { scriptService } from "../services/scriptService";
import type { SimulationResult } from "../types/simulation";

export function useSimulationHistory() {
  const [history, setHistory] = useState<SimulationResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(() => {
    setLoading(true);
    setError(null);
    scriptService
      .getHistory()
      .then(setHistory)
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  return { history, loading, error, reload };
}
