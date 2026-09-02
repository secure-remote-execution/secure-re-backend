package com.eci.secureproductchallenge.mapper;

import com.eci.secureproductchallenge.dto.SimulationResponse;
import com.eci.secureproductchallenge.model.SimulationRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class SimulationMapper {

    private static final TypeReference<Map<String, String>> PARAMETERS_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public SimulationMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String writeParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("No fue posible serializar los parámetros de la simulación", e);
        }
    }

    public Map<String, String> readParameters(String parametersJson) {
        if (parametersJson == null || parametersJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(parametersJson, PARAMETERS_TYPE);
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    public SimulationResponse toResponse(SimulationRecord record) {
        return new SimulationResponse(
                record.getId(),
                record.getDevice().getId(),
                record.getDeviceName(),
                record.getScriptName(),
                readParameters(record.getParametersJson()),
                record.isSuccess(),
                record.getOutput(),
                record.getExecutedAt()
        );
    }
}
