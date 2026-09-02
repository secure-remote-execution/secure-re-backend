import { httpClient } from "./httpClient";
import type { Device, DeviceType } from "../types/device";

export const deviceService = {
  getDevices(type?: DeviceType): Promise<Device[]> {
    const query = type ? `?type=${type}` : "";
    return httpClient.get<Device[]>(`/api/devices${query}`);
  },

  getDeviceById(id: string): Promise<Device> {
    return httpClient.get<Device>(`/api/devices/${id}`);
  },
};
