package com.eci.secureproductchallenge.service.impl;

import com.eci.secureproductchallenge.dto.SimulationRequest;
import com.eci.secureproductchallenge.dto.SimulationResponse;
import com.eci.secureproductchallenge.exception.DeviceNotFoundException;
import com.eci.secureproductchallenge.mapper.SimulationMapper;
import com.eci.secureproductchallenge.model.Device;
import com.eci.secureproductchallenge.model.SimulationRecord;
import com.eci.secureproductchallenge.repository.DeviceRepository;
import com.eci.secureproductchallenge.repository.SimulationRecordRepository;
import com.eci.secureproductchallenge.service.ScriptSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Simulación pura: NUNCA ejecuta comandos reales ni se conecta a los
 * dispositivos. Solo dejamos evidencia (logs + historial) de lo que un
 * usuario intentó ejecutar, para que el Blue Team pueda auditarlo.
 */
@Service
@Transactional
public class ScriptSimulationServiceImpl implements ScriptSimulationService {

    private static final Logger log = LoggerFactory.getLogger(ScriptSimulationServiceImpl.class);
    private static final String SIMULATED_OUTPUT = "Simulación completada, sin cambios aplicados";

    private final DeviceRepository deviceRepository;
    private final SimulationRecordRepository simulationRecordRepository;
    private final SimulationMapper simulationMapper;

    public ScriptSimulationServiceImpl(DeviceRepository deviceRepository,
                                        SimulationRecordRepository simulationRecordRepository,
                                        SimulationMapper simulationMapper) {
        this.deviceRepository = deviceRepository;
        this.simulationRecordRepository = simulationRecordRepository;
        this.simulationMapper = simulationMapper;
    }

    @Override
    public SimulationResponse simulate(SimulationRequest request, String sourceIp) {
        Device device = deviceRepository.findById(request.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(request.deviceId()));

        String parametersJson = simulationMapper.writeParameters(request.parameters());

        log.info("SIMULACION script='{}' deviceId={} deviceName='{}' sourceIp={} parametros={} -> ningún cambio real fue aplicado",
                request.scriptName(), device.getId(), device.getName(), sourceIp, parametersJson);

        SimulationRecord record = new SimulationRecord(
                device,
                device.getName(),
                request.scriptName(),
                parametersJson,
                true,
                SIMULATED_OUTPUT,
                sourceIp,
                Instant.now()
        );

        SimulationRecord saved = simulationRecordRepository.save(record);
        return simulationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimulationResponse> getHistory() {
        return simulationRecordRepository.findAllByOrderByExecutedAtDesc().stream()
                .map(simulationMapper::toResponse)
                .toList();
    }
}
