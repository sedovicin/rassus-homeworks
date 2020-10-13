package hr.fer.rassus.homework1.homework1_Server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hr.fer.rassus.homework1.homework1_Server.entity.SensorData;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

}
