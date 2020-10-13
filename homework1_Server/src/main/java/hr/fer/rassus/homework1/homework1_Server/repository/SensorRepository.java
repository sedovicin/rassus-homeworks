/**
 *
 */
package hr.fer.rassus.homework1.homework1_Server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hr.fer.rassus.homework1.homework1_Server.entity.Sensor;

/**
 * @author Sebastian
 *
 */
public interface SensorRepository extends JpaRepository<Sensor, Long> {

	List<Sensor> findByUsername(String username);
}
