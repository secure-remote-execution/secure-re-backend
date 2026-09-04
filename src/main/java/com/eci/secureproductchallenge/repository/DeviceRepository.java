package com.eci.secureproductchallenge.repository;

import com.eci.secureproductchallenge.model.Device;
import com.eci.secureproductchallenge.model.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByType(DeviceType type);
}
