package com.eci.secureproductchallenge.service.impl;

import com.eci.secureproductchallenge.dto.DeviceResponse;
import com.eci.secureproductchallenge.exception.ResourceNotFoundException;
import com.eci.secureproductchallenge.mapper.DeviceMapper;
import com.eci.secureproductchallenge.model.Device;
import com.eci.secureproductchallenge.model.DeviceType;
import com.eci.secureproductchallenge.repository.DeviceRepository;
import com.eci.secureproductchallenge.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Override
    public List<DeviceResponse> findAll(DeviceType type) {
        List<Device> devices = (type == null)
                ? deviceRepository.findAll()
                : deviceRepository.findByType(type);

        return devices.stream()
                .map(deviceMapper::toResponse)
                .toList();
    }

    @Override
    public DeviceResponse findById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + id));
        return deviceMapper.toResponse(device);
    }
}
