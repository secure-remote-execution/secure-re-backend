package com.eci.secureproductchallenge.mapper;

import com.eci.secureproductchallenge.dto.DeviceResponse;
import com.eci.secureproductchallenge.model.Device;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public DeviceResponse toResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .ipAddress(device.getIpAddress())
                .vendor(device.getVendor())
                .status(device.getStatus())
                .lastConfigChange(device.getLastConfigChange())
                .managedBy(device.getManagedBy())
                .build();
    }
}
