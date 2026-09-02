package com.eci.secureproductchallenge.service;

import com.eci.secureproductchallenge.dto.SimulationRequest;
import com.eci.secureproductchallenge.dto.SimulationResponse;

import java.util.List;

public interface ScriptSimulationService {

    /**
     * Registra una SIMULACIÓN de ejecución de script. Nunca aplica cambios reales
     * sobre el dispositivo: solo queda evidencia en el historial de la aplicación.
     */
    SimulationResponse simulate(SimulationRequest request, String sourceIp);

    List<SimulationResponse> getHistory();
}
