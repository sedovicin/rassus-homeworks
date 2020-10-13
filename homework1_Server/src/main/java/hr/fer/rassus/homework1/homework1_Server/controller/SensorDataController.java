/**
 *
 */
package hr.fer.rassus.homework1.homework1_Server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.rassus.homework1.homework1_Server.entity.Sensor;
import hr.fer.rassus.homework1.homework1_Server.entity.SensorData;
import hr.fer.rassus.homework1.homework1_Server.repository.SensorDataRepository;
import hr.fer.rassus.homework1.homework1_Server.repository.SensorRepository;

/**
 * @author Sebastian
 *
 */
@RestController
public class SensorDataController {

	private final SensorRepository sensorRepo;
	private final SensorDataRepository sensorDataRepo;

	public SensorDataController(final SensorRepository sensorRepo, final SensorDataRepository sensorDataRepo) {
		this.sensorRepo = sensorRepo;
		this.sensorDataRepo = sensorDataRepo;
	}

	@GetMapping("/storeMeasurement")
	public ResponseEntity<Boolean> storeMeasurement(@RequestParam(value = "username") final String username,
			@RequestParam(value = "parameter") final String parameter,
			@RequestParam(value = "averageValue") final String averageValue) {

		Sensor sensor = sensorRepo.findByUsername(username).get(0);

//		SensorData data = new SensorData(sensor.getId(), temperature, pressure, humidity, CO, NO2, SO2);
		SensorData data = parseValues(averageValue, sensor.getId());

		System.out.println("Sensor " + sensor.getUsername() + " sent data: " + data.toString());

		data = sensorDataRepo.save(data);

		if (data != null) {
			return new ResponseEntity<>(true, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private SensorData parseValues(final String entry, final Long id) {
		Double temperature, pressure, humidity, CO, NO2, SO2;
		temperature = pressure = humidity = CO = NO2 = SO2 = null;
		if (entry != null) {
			String[] splittedEntry = entry.split(",", -1);
			if (!((splittedEntry[0] == null) || splittedEntry[0].isEmpty())) {
				temperature = Double.valueOf(splittedEntry[0]);
			}
			if (!((splittedEntry[1] == null) || splittedEntry[1].isEmpty())) {
				pressure = Double.valueOf(splittedEntry[1]);
			}
			if (!((splittedEntry[2] == null) || splittedEntry[2].isEmpty())) {
				humidity = Double.valueOf(splittedEntry[2]);
			}
			if (!((splittedEntry[3] == null) || splittedEntry[3].isEmpty())) {
				CO = Double.valueOf(splittedEntry[3]);
			}
			if (!((splittedEntry[4] == null) || splittedEntry[4].isEmpty())) {
				NO2 = Double.valueOf(splittedEntry[4]);
			}
			if (!((splittedEntry[5] == null) || splittedEntry[5].isEmpty())) {
				SO2 = Double.valueOf(splittedEntry[5]);
			}
		}

		return new SensorData(id, temperature, pressure, humidity, CO, NO2, SO2);
	}
}
