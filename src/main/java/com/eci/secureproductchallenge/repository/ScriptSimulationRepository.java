package com.eci.secureproductchallenge.repository;

import com.eci.secureproductchallenge.model.ScriptSimulation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScriptSimulationRepository extends JpaRepository<ScriptSimulation, UUID> {
    List<ScriptSimulation> findAllByOrderByRequestedAtDesc();
}
