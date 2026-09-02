package com.eci.secureproductchallenge.repository;

import com.eci.secureproductchallenge.model.SimulationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SimulationRecordRepository extends JpaRepository<SimulationRecord, UUID> {

    List<SimulationRecord> findAllByOrderByExecutedAtDesc();
}
