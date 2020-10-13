package hr.fer.rassus.homework2.node;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import hr.fer.rassus.homework2.node.sensor.SensorData;
import hr.fer.rassus.homework2.node.sensor.SensorHandler;
import hr.fer.rassus.homework2.node.worker.SocketMessageData;
import hr.fer.rassus.homework2.node.worker.SocketReceiveWorker;
import hr.fer.rassus.homework2.node.worker.SocketSendWorker;
import hr.fer.rassus.homework2.node.worker.SortPrintWorker;
import hr.fer.rassus.homework2.utils.EmulatedSystemClock;
import hr.fer.rassus.homework2.utils.SimpleSimulatedDatagramSocket;

/**
 * Represents a single node that receives data from a sensor and exchanges
 * sensor data with other nodes.
 *
 * @author Sebastian
 *
 */
public class Node {

	private int PORT;

	private List<Integer> otherNodes;

	private DatagramSocket socket;
	private final AtomicBoolean nodeOnFlag;

	private SensorHandler sensor;

	private LinkedBlockingQueue<DatagramPacket> sendQueue;
	private Queue<DatagramPacket> ackReceiveQueue;
	private Queue<SocketMessageData> fiveSecondsPackets;
	private Queue<String> fiveSecondsMessages;

	private Long realStartTimestamp;
	private AtomicLong scalarTimestamp;
	private Long emulatedDeltaTime;
	private Map<Integer, Integer> vectorTimestamp;
	private final EmulatedSystemClock clock;

	private File file;

	public Node() {
		nodeOnFlag = new AtomicBoolean(false);
		clock = new EmulatedSystemClock();
	}

	private void startup() {
		realStartTimestamp = System.currentTimeMillis();
		emulatedDeltaTime = Long.valueOf(0);

		System.out.println("Started at " + new Date(realStartTimestamp).toString());
		try {
			file = new File("mjerenja.csv");
			sensor = new SensorHandler(this, file);

			NodeGrid grid = new NodeGrid();
			PORT = grid.getAvailablePort();
			otherNodes = grid.getOtherPorts();

			System.out.println("Got port " + PORT);

			socket = new SimpleSimulatedDatagramSocket(PORT, 0d, 1000);
			socket.setSoTimeout(10); // to avoid too long waiting to receive so the socket could send a message if
										// there is one

			scalarTimestamp = new AtomicLong(0);
			vectorTimestamp = new ConcurrentHashMap<>();
			for (Integer node : otherNodes) {
				vectorTimestamp.put(node, 0);
			}
			vectorTimestamp.put(PORT, 0);

			fiveSecondsPackets = new ConcurrentLinkedQueue<>();
			fiveSecondsMessages = new LinkedBlockingQueue<>();
			sendQueue = new LinkedBlockingQueue<>();
			ackReceiveQueue = new ConcurrentLinkedQueue<>();

			nodeOnFlag.set(true);

			new Thread(new SocketSendWorker(this)).start();
			new Thread(new SocketReceiveWorker(this)).start();

			System.out.println("Node is ready!");
		} catch (SocketException e1) {
			if (e1 instanceof ConnectException) {
				System.out.println("Unable to connect to server.");
			} else {
				System.err.println("Exception caught when setting server socket timeout: " + e1);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private void loop() {
		int countSeconds = 0;
		Long start = System.currentTimeMillis();
		while (nodeOnFlag.get()) {
			try {
				/**
				 * Read data
				 */
				SensorData data = sensor.readDataAndParse();
				Double COdata = data.getCO();

				updateCurrentScalarTime(null);
				updateVectorTimestamp(null);

				SocketMessageData packetMessage = createPacketMessage(COdata.toString());
				fiveSecondsPackets.add(packetMessage);

				/**
				 * Create packets and put them to send queue
				 */
				for (Integer node : otherNodes) {
					SocketMessageData packetMessageUnique = new SocketMessageData(packetMessage);
					packetMessageUnique.setScalarTimestamp(updateCurrentScalarTime(null));
					packetMessageUnique.setVectorTimestamp(updateVectorTimestamp(null));
					byte[] message = packetMessageUnique.toString().getBytes();
					DatagramPacket packet = new DatagramPacket(message, message.length, InetAddress.getLocalHost(),
							node);
					sendQueue.add(packet);
				}

				Thread.sleep(1000 - (System.currentTimeMillis() - start));

				/**
				 * Dump readings to sysout every 5 seconds
				 */
				start = System.currentTimeMillis();
				if (++countSeconds == 5) {
					new Thread(new SortPrintWorker(this, new ArrayList<>(fiveSecondsPackets))).start();
					clearFiveSecondsPackets();
					countSeconds = 0;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void shutdown() {
		socket.close();
		System.out.println("Shut down.");
	}

	public void run() {
		startup();
		loop();
		shutdown();
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return PORT;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public AtomicBoolean getNodeOnFlag() {
		return nodeOnFlag;
	}

	public Long getRealStartTimestamp() {
		return realStartTimestamp;
	}

	public synchronized Long updateCurrentScalarTime(final Long remoteTime) {
		Long current = clock.currentTimeMillis() + emulatedDeltaTime;
		if ((remoteTime != null) && (remoteTime > current)) {
			emulatedDeltaTime += (remoteTime - current);
		}
		scalarTimestamp.set(clock.currentTimeMillis() + emulatedDeltaTime);

		return scalarTimestamp.get();
	}

	public synchronized Map<Integer, Integer> updateVectorTimestamp(final Map<Integer, Integer> remoteVector) {
		if (remoteVector == null) {
			vectorTimestamp.put(PORT, vectorTimestamp.get(PORT) + 1);
		} else {
			for (Entry<Integer, Integer> vector : vectorTimestamp.entrySet()) {
				Integer remoteValue = remoteVector.get(vector.getKey());
				if (remoteValue > vector.getValue()) {
					vector.setValue(remoteValue);
				} else {
					remoteVector.put(vector.getKey(), vector.getValue());
				}
			}
		}
		return vectorTimestamp;
	}

	public Map<Integer, Integer> getVectorTimestamp() {
		return vectorTimestamp;
	}

	public SocketMessageData createPacketMessage(final String data) {
		return new SocketMessageData("m", PORT, data, updateCurrentScalarTime(null), vectorTimestamp);
	}

	public LinkedBlockingQueue<DatagramPacket> getSendQueue() {
		return sendQueue;
	}

	public Queue<DatagramPacket> getAckReceiveQueue() {
		return ackReceiveQueue;
	}

	public Queue<SocketMessageData> getFiveSecondsPackets() {
		return fiveSecondsPackets;
	}

	public synchronized void clearFiveSecondsPackets() {
		fiveSecondsPackets = new ConcurrentLinkedQueue<>();
		fiveSecondsMessages = new LinkedBlockingQueue<>();
	}

	public synchronized void addToFiveSecondsPackets(final SocketMessageData data) {
		if (!(fiveSecondsMessages.contains(data.toString()))) {
			fiveSecondsMessages.add(data.toString());
			fixTimestampsAndUpdate(data);
			getFiveSecondsPackets().add(data);
		}
	}

	public synchronized void fixTimestampsAndUpdate(final SocketMessageData receivedData) {
		receivedData.setScalarTimestamp(updateCurrentScalarTime(receivedData.getScalarTimestamp()));
		receivedData.setVectorTimestamp(updateVectorTimestamp(receivedData.getVectorTimestamp()));
	}

}
