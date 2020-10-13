package hr.fer.rassus.homework2.node.worker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import hr.fer.rassus.homework2.node.Node;

/**
 * Thread that is in charge of sending datagrams to another socket (node)
 *
 * @author Sebastian
 *
 */
public class SocketSendWorker implements Runnable {

	private final Node node;
	private final DatagramSocket socket;

	private final Map<DatagramPacket, Long> unackPackets;

	public SocketSendWorker(final Node node) {
		super();
		this.node = node;
		socket = node.getSocket();
		unackPackets = new HashMap<>();
	}

	@Override
	public void run() {
		while (node.getNodeOnFlag().get()) {
			try {
				/**
				 * Receive a packet
				 */
				DatagramPacket sendPacket = node.getSendQueue().poll(10, TimeUnit.MILLISECONDS);
				if (sendPacket != null) {

					String data = new String(sendPacket.getData()).trim();
					if (data.startsWith("a")) {
						socket.send(sendPacket);
					} else if (data.startsWith("m")) {
						unackPackets.put(sendPacket, System.currentTimeMillis());
						socket.send(sendPacket);
					}
				}
				/**
				 * Manage received ACK packets
				 */
				DatagramPacket ackPacket = node.getAckReceiveQueue().poll();
				if (ackPacket != null) {

					String message = new String(ackPacket.getData());
					message = message.substring(1, message.length()).trim();

					for (DatagramPacket unackPacket : unackPackets.keySet()) {
						String unackMessage = new String(unackPacket.getData());
						unackMessage = unackMessage.substring(1, unackMessage.length()).trim();

						if (unackMessage.equals(message)) {
							unackPackets.remove(unackPacket);
							break;
						}
					}
				}
				/**
				 * retransmit packets that did not receive ACK for over 1000 ms
				 */
				for (Entry<DatagramPacket, Long> unackPacketEntry : unackPackets.entrySet()) {
					Long time = System.currentTimeMillis();
					Long sendTime = unackPacketEntry.getValue();

					if ((time - sendTime) > 1000) {
						socket.send(unackPacketEntry.getKey());
						unackPacketEntry.setValue(time);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}