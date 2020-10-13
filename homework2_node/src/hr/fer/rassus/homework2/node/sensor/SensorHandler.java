package hr.fer.rassus.homework2.node.sensor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import hr.fer.rassus.homework2.node.Node;

/**
 * Class whose purpose is to communicate with the sensor (reads its data).
 * 
 * @author Sebastian
 *
 */
public class SensorHandler {

	private final Node node;
	private final File file;

	public SensorHandler(final Node node, final File file) {
		super();
		this.node = node;
		this.file = file;
	}

	/**
	 * Does the data scan from the sensor.
	 *
	 * @return SensorData representation of the data
	 * @throws IOException if error occured
	 */
	public SensorData readDataAndParse() throws IOException {
		return new SensorData(readData());
	}

	/**
	 * Does the data scan from the sensor.
	 *
	 * @return raw String data from the sensor
	 * @throws IOException if error occured
	 */
	public String readData() throws IOException {
		Integer orderNumber = getOrderNumber();
//		System.out.println("Fetching line " + orderNumber);
		return Files.readAllLines(file.toPath()).get(orderNumber);
	}

	private Integer getOrderNumber() {
		return Math.toIntExact(
				((((System.currentTimeMillis() / 1000) - (node.getRealStartTimestamp() / 1000)) % 100) + 2) - 1);
	}
}
