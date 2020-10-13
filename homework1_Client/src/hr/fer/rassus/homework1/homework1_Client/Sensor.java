package hr.fer.rassus.homework1.homework1_Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that represents a sensor that measures several air components in the
 * area where this sensor is located and sends the data to the predefined
 * server.
 *
 * @author Sebastian
 *
 */
public class Sensor {

	private static final int PORT = 0; // because it is run locally, so we won't have any collision in port numbers
//	private static final int NUMBER_OF_THREADS = 4;
	private static final int BACKLOG = 10;

	private final AtomicInteger activeConnections;
	private ServerSocket serverSocket;
	private final ExecutorService executor;
	private final AtomicBoolean sensorRunningFlag;
	private final AtomicBoolean sensorOnFlag;

	private Long startTimestamp;

	private File file;

	/**
	 * Creates new instance of this object
	 */
	public Sensor() {
		activeConnections = new AtomicInteger(0);
		sensorOnFlag = new AtomicBoolean(false);
		sensorRunningFlag = new AtomicBoolean(false);
		executor = Executors.newCachedThreadPool();
	}

	/**
	 * Does everything necessary to prepare sensor for its job.
	 */
	public void startup() {
		startTimestamp = System.currentTimeMillis() / 1000;

		System.out.println("Started at " + new Date(startTimestamp * 1000).toString());
		try {
			file = new File("mjerenja.csv");

			Random random = new Random();
			Double longitude = 15.87d + ((16d - 15.87d) * random.nextDouble());
			Double latitude = 45.85d + ((45.85d - 45.75d) * random.nextDouble());

			System.out.println("Chosen coordinates:" + longitude + " " + latitude);

			this.serverSocket = new ServerSocket(PORT, BACKLOG);

			String identificator = getIdentificator();
			System.out.println("Sensor identificator:" + identificator);

			register(identificator, latitude, longitude, "127.0.0.1", serverSocket.getLocalPort());

			serverSocket.setSoTimeout(500);
			sensorOnFlag.set(true);
			System.out.println("Server is ready!");

		} catch (SocketException e1) {
			if (e1 instanceof ConnectException) {
				System.out.println("Unable to connect to server.");
			} else {
				System.err.println("Exception caught when setting server socket timeout: " + e1);
			}
		} catch (IOException ex) {
			System.err.println("Exception caught when opening or setting the server socket: " + ex);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Does the basic looped work of the sensor (waiting for new connections to
	 * answer to).
	 */
	public void loop() {
		while (sensorOnFlag.get()) {
			try {
				Socket clientSocket = serverSocket.accept();

				clientSocket.setSoTimeout(500);

				System.out.println("Accepted connection from:" + clientSocket.getInetAddress().toString() + ":"
						+ clientSocket.getPort());

				Runnable connectionWorker = new SensorConnectionWorker(this, clientSocket);
				executor.execute(connectionWorker);

//				activeConnections.getAndIncrement();
			} catch (SocketTimeoutException ste) {
				// do nothing, check the runningFlag flag
			} catch (IOException e) {
				System.err.println("Exception caught when waiting for a connection: " + e);
			}
		}
	}

	/**
	 * Does everything necessary in order to completely shut down this sensor.
	 */
	public void shutdown() {
		while (activeConnections.get() > 0) {
			System.out.println("WARNING: There are still active connections"); // Need to wait!
			try {
				Thread.sleep(5000);
			} catch (java.lang.InterruptedException e) {
				// Do nothing, check again whether there are still active connections to the
				// server.
			}
		}
		if (activeConnections.get() == 0) {
			System.out.println("Starting server shutdown.");
			try {
				serverSocket.close(); /* CLOSE the main server socket */
			} catch (IOException e) {
				System.err.println("Exception caught when closing the server socket: " + e);
			} finally {
				executor.shutdown();
			}

			System.out.println("Server has been shutdown.");
		}
	}

	protected ServerSocket getServerSocket() {
		return serverSocket;
	}

	protected ExecutorService getExecutor() {
		return executor;
	}

	protected AtomicBoolean getSensorRunningFlag() {
		return sensorRunningFlag;
	}

	protected AtomicBoolean getSensorOnFlag() {
		return sensorOnFlag;
	}

	protected AtomicInteger getActiveConnections() {
		return activeConnections;
	}

	protected Long getStartTimestamp() {
		return startTimestamp;
	}

	protected File getFile() {
		return file;
	}

	/**
	 * Returns a new unique identificator for this instance of the sensor.
	 *
	 * @return
	 */
	protected String getIdentificator() {
		return String.valueOf(serverSocket.getLocalPort());
	}

	/**
	 * Registers this sensor to the server
	 *
	 * @param latitude      latitude coordinate of this sensor
	 * @param longitude     longitude coordinate of this sensor
	 * @param identificator this sensor's unique identificator (username)
	 */
	private void register(final String identificator, final Double latitude, final Double longitude,
			final String IPAddress, final int port) throws Exception {
		System.out.println("Registering sensor...");
		try {
			URL url = new URL("http://localhost:8080/register?username=" + identificator + "&latitude=" + latitude
					+ "&longitude=" + longitude + "&ipaddress=" + IPAddress + "&port=" + port);

			try {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setRequestMethod("GET");

				if (connection.getResponseCode() >= 300) {
					throw new IOException("Error when registering to server.");
				}

			} catch (IOException e) {
				throw e;
			}
			System.out.println("Registered successfully.");
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Fetches contact data for sensor geographically closest to this one
	 *
	 * @return String representation of IP address and port of the neighbouring
	 *         sensor
	 */
	protected String getNeighbour() {
		System.out.println("Fetching neighbour...");
		String neighbour = null;
		try {
			URL url = new URL("http://localhost:8080/searchNeighbour?username=" + getIdentificator());

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("GET");

			if (connection.getResponseCode() == 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				neighbour = reader.readLine();
				System.out.println("Neighbour " + neighbour + " fetched.");
			} else {
				System.out.println("Neighour not fetched");
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		return neighbour;

	}

	/**
	 * Sends measurement data to the server
	 *
	 * @param data data to be sent to the server
	 */
	protected void storeMeasurements(final SensorData data) throws Exception {
		System.out.println("Sending measurements to server...");
		boolean success = false;

		try {
			URL url = new URL("http://localhost:8080/storeMeasurement?username=" + getIdentificator()
					+ "&parameter=&averageValue=" + data.toString());

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("GET");

			if (connection.getResponseCode() == 200) {
				success = true;
			} else {
				success = false;
			}

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Measurements sent " + (success ? "" : "un") + "successfully.");
	}

	public void run() {
		startup();
		loop();
		shutdown();
	}
}
