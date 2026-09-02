export interface SimulationRequest {
  deviceId: string;
  scriptName: string;
  parameters: Record<string, string>;
}

export interface SimulationResult {
  id: string;
  deviceId: string;
  deviceName: string;
  scriptName: string;
  parameters: Record<string, string>;
  success: boolean;
  output: string;
  executedAt: string;
}
