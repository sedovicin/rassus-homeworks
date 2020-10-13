package hr.fer.rassus.homework1.homework1_Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that represents sensor's main job that needs to do
 *
 * @author Sebastian
 *
 */
public class SensorJobWorker implements Runnable {

	/**
	 * Enum for different types of job this class has
	 *
	 * @author Sebastian
	 *
	 */
	public enum JobType {
		SensorNotifier, ServerNotifier
	}

	private final Sensor sensor;
	private final JobType jobType;
	private final AtomicBoolean sensorOnFlag;
	private final AtomicBoolean sensorRunningFlag;

	/**
	 * Creates new instance of job class. Every sensor should have one
	 * ServerNotifier-type instance. SensorNotifier-type instances can be as much as
	 * needed.
	 *
	 * @param sensor  sensor to which it is bound to
	 * @param jobType type of job, as represented by enum
	 */
	public SensorJobWorker(final Sensor sensor, final JobType jobType) {
		this.sensor = sensor;
		this.jobType = jobType;
		this.sensorOnFlag = sensor.getSensorOnFlag();
		this.sensorRunningFlag = sensor.getSensorRunningFlag();
	}

	/**
	 * Main thread method for running ServerNotifier-type job
	 */
	@Override
	public void run() {
		Socket clientSocket = null;
		BufferedReader fromClient = null;
		PrintWriter toClient = null;

		if (jobType.equals(JobType.ServerNotifier)) {
			sensorRunningFlag.set(true);
			String neighbour = null;
			try {
				neighbour = sensor.getNeighbour();
				if (neighbour != null) {
					String[] neighbourSplitted = neighbour.split(":");
					if (neighbourSplitted.length != 2) {
						throw new Exception("Error while fetching neighbour. Some info is missing.");
					}
					clientSocket = new Socket(neighbourSplitted[0], Integer.parseInt(neighbourSplitted[1]));

					fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					toClient = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
					clientSocket.setSoTimeout(500);
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
			while (sensorOnFlag.get() && sensorRunningFlag.get()) {
				try {

					SensorData data = readDataAndParse();
					System.out.println("Data read:" + data.toString());

					if (neighbour != null) {
						try {
							SensorData neighbourData = requestMeasurements(fromClient, toClient);
							data.calibrate(neighbourData);

							System.out.println("Calibrated my data to " + data.toString() + " with data from neighbour "
									+ String.valueOf(clientSocket.getPort()));
						} catch (IOException e) {
							System.out.println("Error when contacting neighbour. Sending uncalibrated data.");
						}

					} else {
						System.out.println("Warning: Neighbour not found. Sending uncalibrated data.");
					}
					sensor.storeMeasurements(data);
					Thread.sleep(1000);
				} catch (SocketTimeoutException e) {

				} catch (IOException e) {
					System.out.println("Error when contacting data server.");
					sensorRunningFlag.set(false);
					sensorOnFlag.set(false);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}

			}

			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Main method for running SensorNotifier-type job
	 *
	 * @param fromClient stream for reading other sensor's messages
	 * @param toClient   stream for sending response to other sensor
	 * @throws IOException if error occured
	 */
	public void run(final String identifier, final BufferedReader fromClient, final PrintWriter toClient)
			throws IOException {
		if (jobType.equals(JobType.SensorNotifier)) {
			String clientRead = null;
			while (sensorOnFlag.get()) {
				do {
					try {
						String data = readData().toString();
						System.out.println("Me (" + sensor.getIdentificator() + ") sending data to client ("
								+ identifier + "). Data: " + data);
						toClient.println(data);
						clientRead = fromClient.readLine();
					} catch (SocketTimeoutException e) {

					} catch (IOException e) {
						throw e;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} while (!(clientRead.equals("data stop")));

				do {
					try {
						clientRead = fromClient.readLine();
					} catch (SocketTimeoutException e) {

					} catch (IOException e) {
						throw e;
					} catch (Exception e) {
						e.printStackTrace();
					}

				} while (!(clientRead.equals("data gimme")));

			}
		}
	}

	/**
	 * Does the data scan from the sensor.
	 *
	 * @return SensorData representation of the data
	 * @throws IOException if error occured
	 */
	private SensorData readDataAndParse() throws IOException {
		return new SensorData(readData());
	}

	/**
	 * Does the data scan from the sensor.
	 *
	 * @return raw String data from the sensor
	 * @throws IOException if error occured
	 */
	private String readData() throws IOException {
		Integer orderNumber = Integer.valueOf(
				((Long.valueOf(((System.currentTimeMillis() / 1000) - sensor.getStartTimestamp())).intValue() % 100)
						+ 2) - 1);
		System.out.println("Fetching line " + orderNumber);

		return Files.readAllLines(sensor.getFile().toPath()).get(orderNumber);
	}

	/**
	 * Sends a request to the neighbour sensor in order to compare its data with own
	 *
	 * @param fromClient stream for reading other sensor's messages
	 * @param toClient   stream for sending response to other sensor
	 * @return SensorData representation of other sensor's data
	 * @throws IOException if error occured
	 */
	private SensorData requestMeasurements(final BufferedReader fromClient, final PrintWriter toClient)
			throws IOException {
		System.out.println("Fetching data from neighbour...");
		toClient.println("data gimme");

		String neighbourData;
		neighbourData = fromClient.readLine();

		toClient.println("data stop");

		System.out.println("Data fetched: " + neighbourData);
		return new SensorData(neighbourData);
	}

}
