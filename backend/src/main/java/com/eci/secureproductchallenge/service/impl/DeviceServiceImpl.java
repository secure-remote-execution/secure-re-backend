package com.eci.secureproductchallenge.service.impl;

import com.eci.secureproductchallenge.dto.DeviceResponse;
import com.eci.secureproductchallenge.exception.DeviceNotFoundException;
import com.eci.secureproductchallenge.mapper.DeviceMapper;
import com.eci.secureproductchallenge.model.Device;
import com.eci.secureproductchallenge.model.DeviceType;
import com.eci.secureproductchallenge.repository.DeviceRepository;
import com.eci.secureproductchallenge.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public DeviceServiceImpl(DeviceRepository deviceRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    @Override
    public List<DeviceResponse> getDevices(DeviceType typeFilter) {
        List<Device> devices = typeFilter != null
                ? deviceRepository.findByType(typeFilter)
                : deviceRepository.findAll();
        return devices.stream().map(deviceMapper::toResponse).toList();
    }

    @Override
    public DeviceResponse getDeviceById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
        return deviceMapper.toResponse(device);
    }
}
