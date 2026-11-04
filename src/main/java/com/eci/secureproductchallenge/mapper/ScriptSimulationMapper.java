package com.eci.secureproductchallenge.mapper;

import com.eci.secureproductchallenge.dto.ScriptSimulationResponse;
import com.eci.secureproductchallenge.model.ScriptSimulation;
import org.springframework.stereotype.Component;

@Component
public class ScriptSimulationMapper {

    public ScriptSimulationResponse toResponse(ScriptSimulation simulation) {
        return ScriptSimulationResponse.builder()
                .id(simulation.getId())
                .deviceId(simulation.getDeviceId())
                .scriptName(simulation.getScriptName())
                .parameters(simulation.getParameters())
                .success(simulation.isSuccess())
                .output(simulation.getOutput())
                .requestedAt(simulation.getRequestedAt())
                .performedBy(simulation.getPerformedBy())
                .build();
    }
}
