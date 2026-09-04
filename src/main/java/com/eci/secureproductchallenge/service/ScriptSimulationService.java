package com.eci.secureproductchallenge.service;

import com.eci.secureproductchallenge.dto.ScriptSimulationRequest;
import com.eci.secureproductchallenge.dto.ScriptSimulationResponse;

import java.util.List;

public interface ScriptSimulationService {

    /**
     * Simula la ejecución de un script sobre un dispositivo.
     * NO realiza ningún cambio real: solo valida que el dispositivo exista
     * y registra la solicitud como evidencia para el Blue Team.
     */
    ScriptSimulationResponse simulate(ScriptSimulationRequest request);

    List<ScriptSimulationResponse> history();
}
