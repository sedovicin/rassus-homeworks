package hr.fer.rassus.homework1.homework1_Server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.rassus.homework1.homework1_Server.entity.Sensor;
import hr.fer.rassus.homework1.homework1_Server.repository.SensorRepository;

@RestController
public class SensorController {

	private final SensorRepository sensorRepo;

	public SensorController(final SensorRepository sensorRepo) {
		this.sensorRepo = sensorRepo;
	}

	@GetMapping("/register")
	public ResponseEntity<Boolean> register(@RequestParam(value = "username") final String username,
			@RequestParam(value = "latitude") final Double latitude,
			@RequestParam(value = "longitude") final Double longitude,
			@RequestParam(value = "ipaddress") final String IPAddress,
			@RequestParam(value = "port") final Integer port) {

		Sensor sensor = new Sensor(username, latitude, longitude, IPAddress, port);

		System.out.println("Register requested. Data: " + sensor.toString());

		sensor = sensorRepo.saveAndFlush(sensor);

		if (sensor.getId() != null) {
			return new ResponseEntity<>(true, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/searchNeighbour")
	public ResponseEntity<String> searchNeighbour(@RequestParam(value = "username") final String username) {

		Sensor sensor = sensorRepo.findByUsername(username).get(0);

		System.out.println("User " + sensor.getUsername() + " requested data about its closest neighbour.");
		System.out.println("His coordinates:" + sensor.getLatitude() + " " + sensor.getLongitude());

		List<Sensor> sensors = sensorRepo.findAll();

		Double shortestDistance = Double.MAX_VALUE;
		Sensor closestSensor = null;
		for (Sensor candidateNeighbourSensor : sensors) {
			if (!(candidateNeighbourSensor.getUsername().equals(username))) {
				Double distance = calculateDistance(sensor, candidateNeighbourSensor);
				if (distance < shortestDistance) {
					shortestDistance = distance;
					closestSensor = candidateNeighbourSensor;
				}
			}
		}

		if (closestSensor != null) {
			System.out.println("Closest sensor:" + closestSensor.getUsername() + " " + closestSensor.getLatitude() + " "
					+ closestSensor.getLongitude());
			return new ResponseEntity<>(closestSensor.getIPAddress() + ":" + closestSensor.getPort(), HttpStatus.OK);
		} else {
			System.out.println("No neighbours found.");
			return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private Double calculateDistance(final Sensor sensor, final Sensor anotherSensor) {
		Double radius = 6371d;
		Double dLon = anotherSensor.getLongitude() - sensor.getLongitude();
		Double dLat = anotherSensor.getLatitude() - sensor.getLatitude();

		Double a = Math.pow(Math.sin(dLat / 2), 2) + (Math.cos(sensor.getLatitude())
				* Math.cos(anotherSensor.getLatitude()) * Math.pow(Math.sin(dLon / 2), 2));
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return radius * c;
	}
}
