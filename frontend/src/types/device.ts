export type DeviceType = "FIREWALL" | "ROUTER" | "SWITCH";

export type DeviceStatus = "ONLINE" | "OFFLINE" | "UNKNOWN";

export interface Device {
  id: string;
  name: string;
  type: DeviceType;
  ipAddress: string;
  vendor: string;
  status: DeviceStatus;
  lastConfigChange: string;
  managedBy: string;
}
