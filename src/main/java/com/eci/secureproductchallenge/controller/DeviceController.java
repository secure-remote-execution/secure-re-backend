package com.eci.secureproductchallenge.controller;

import com.eci.secureproductchallenge.dto.DeviceResponse;
import com.eci.secureproductchallenge.model.DeviceType;
import com.eci.secureproductchallenge.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// TODO: Lab 4 - agregar Spring Security + JWT + roles aquí (@PreAuthorize, etc.)
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Inventario ficticio de dispositivos de red (solo lectura)")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @Operation(summary = "Lista el inventario de dispositivos, opcionalmente filtrado por tipo")
    public List<DeviceResponse> findAll(@RequestParam(required = false) DeviceType type) {
        return deviceService.findAll(type);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un dispositivo")
    public DeviceResponse findById(@PathVariable UUID id) {
        return deviceService.findById(id);
    }
}
