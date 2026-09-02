package com.eci.secureproductchallenge.mapper;

import com.eci.secureproductchallenge.dto.DeviceResponse;
import com.eci.secureproductchallenge.model.Device;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public DeviceResponse toResponse(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getType(),
                device.getIpAddress(),
                device.getVendor(),
                device.getStatus(),
                device.getLastConfigChange(),
                device.getManagedBy()
        );
    }
}
