import { httpClient } from "./httpClient";
import type { SimulationRequest, SimulationResult } from "../types/simulation";

export const scriptService = {
  simulate(request: SimulationRequest): Promise<SimulationResult> {
    return httpClient.post<SimulationResult>("/api/scripts/simulate", request);
  },

  getHistory(): Promise<SimulationResult[]> {
    return httpClient.get<SimulationResult[]>("/api/scripts/simulate/history");
  },
};
