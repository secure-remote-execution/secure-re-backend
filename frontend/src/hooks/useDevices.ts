import { useCallback, useEffect, useState } from "react";
import { deviceService } from "../services/deviceService";
import type { Device, DeviceType } from "../types/device";

export function useDevices(typeFilter?: DeviceType) {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(() => {
    setLoading(true);
    setError(null);
    deviceService
      .getDevices(typeFilter)
      .then(setDevices)
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
  }, [typeFilter]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { devices, loading, error, reload };
}
