package com.eci.secureproductchallenge.service.impl;

import com.eci.secureproductchallenge.dto.ScriptSimulationRequest;
import com.eci.secureproductchallenge.dto.ScriptSimulationResponse;
import com.eci.secureproductchallenge.exception.ResourceNotFoundException;
import com.eci.secureproductchallenge.mapper.ScriptSimulationMapper;
import com.eci.secureproductchallenge.model.ScriptSimulation;
import com.eci.secureproductchallenge.repository.DeviceRepository;
import com.eci.secureproductchallenge.repository.ScriptSimulationRepository;
import com.eci.secureproductchallenge.service.ScriptSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScriptSimulationServiceImpl implements ScriptSimulationService {

    private final ScriptSimulationRepository simulationRepository;
    private final DeviceRepository deviceRepository;
    private final ScriptSimulationMapper simulationMapper;

    @Override
    public ScriptSimulationResponse simulate(ScriptSimulationRequest request) {
        // Se valida que el dispositivo exista en el inventario, pero en ningún
        // momento se abre conexión ni se envía nada al dispositivo real.
        UUID deviceId = Objects.requireNonNull(request.deviceId(), "deviceId es obligatorio");
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Dispositivo no encontrado: " + deviceId);
        }

        ScriptSimulation simulation = ScriptSimulation.builder()
                .deviceId(deviceId)
                .scriptName(request.scriptName())
                .parameters(request.parameters())
                .success(true)
                .output("Simulación completada, sin cambios aplicados")
                .requestedAt(Instant.now())
                .build();

        ScriptSimulation saved = simulationRepository.save(Objects.requireNonNull(simulation));
        return simulationMapper.toResponse(saved);
    }

    @Override
    public List<ScriptSimulationResponse> history() {
        return simulationRepository.findAllByOrderByRequestedAtDesc().stream()
                .map(simulationMapper::toResponse)
                .toList();
    }
}
