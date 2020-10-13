package hr.fer.rassus.homework1.homework1_Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that represents a single incoming connection to the sensor.
 *
 * @author Sebastian
 *
 */
public class SensorConnectionWorker implements Runnable {

	private final Sensor sensor;
	private final Socket clientSocket;
	private final ExecutorService executor;
	private final AtomicBoolean sensorRunningFlag;
	private final AtomicBoolean sensorOnFlag;
	private final AtomicInteger activeConnections;

	/**
	 * Creates new instance (thread) specialized in handling a new connection
	 *
	 * @param sensor       sensor to which it is bound to
	 * @param clientSocket socket that was created for connection which this class
	 *                     handles
	 */
	public SensorConnectionWorker(final Sensor sensor, final Socket clientSocket) {
		this.sensor = sensor;
		this.clientSocket = clientSocket;
		this.executor = sensor.getExecutor();
		this.sensorOnFlag = sensor.getSensorOnFlag();
		this.sensorRunningFlag = sensor.getSensorRunningFlag();
		this.activeConnections = sensor.getActiveConnections();
	}

	/**
	 * Main thread method
	 */
	@Override
	public void run() {
		activeConnections.incrementAndGet();

		try (BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter toClient = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {
			String message = null;
			do {
				try {
					message = fromClient.readLine();
					System.out.println("Message: " + message);
					switch (message) {
					case "start":
						runSensor();
						toClient.println("ok");
						clientSocket.close();
						break;
					case "stop":
						sensorRunningFlag.set(false);
						toClient.println("ok");
						clientSocket.close();
						break;
					case "data gimme":
						sendMeasurements(String.valueOf(clientSocket.getPort()), fromClient, toClient);
						break;
					case "data stop":
						break;
					case "shutdown":
						sensorRunningFlag.set(false);
						sensorOnFlag.set(false);
					default:
						break;
					}
				} catch (SocketTimeoutException e) {
				} catch (IOException e) {
					throw e;
				}
			} while (sensorOnFlag.get());

		} catch (IOException e) {
			System.out.println("Connection error.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			activeConnections.decrementAndGet();
		}

	}

	/**
	 * Runs the sensor's independent scanning job.
	 */
	private void runSensor() {
		if (!(sensorRunningFlag.get())) {
			System.out.println("Running sensor...");
			Runnable jobWorker = new SensorJobWorker(sensor, SensorJobWorker.JobType.ServerNotifier);
			executor.execute(jobWorker);
			System.out.println("Sensor run.");

		}
	}

	/**
	 * Sends the requested data to the sensor that requested it
	 *
	 * @param fromClient stream for reading other sensor's messages
	 * @param toClient   stream for sending response to other sensor
	 * @throws IOException if error occured
	 */
	private void sendMeasurements(final String identifier, final BufferedReader fromClient, final PrintWriter toClient)
			throws IOException {
		System.out.println("Starting sending sensor reading to client " + clientSocket.getInetAddress().toString() + ":"
				+ clientSocket.getPort());
		SensorJobWorker jobWorker = new SensorJobWorker(sensor, SensorJobWorker.JobType.SensorNotifier);
		jobWorker.run(identifier, fromClient, toClient);
	}

}
