package com.eci.secureproductchallenge.service;

import com.eci.secureproductchallenge.dto.DeviceResponse;
import com.eci.secureproductchallenge.model.DeviceType;

import java.util.List;
import java.util.UUID;

public interface DeviceService {
    List<DeviceResponse> findAll(DeviceType type);
    DeviceResponse findById(UUID id);
}
