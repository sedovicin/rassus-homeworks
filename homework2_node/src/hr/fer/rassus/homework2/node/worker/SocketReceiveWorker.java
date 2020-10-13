package hr.fer.rassus.homework2.node.worker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.TreeSet;

import hr.fer.rassus.homework2.node.Node;

/**
 * Thread that is in charge of receiving datagrams from a socket (node) and
 * filtering them.
 *
 * @author Sebastian
 *
 */
public class SocketReceiveWorker implements Runnable {

	public Node node;
	public DatagramSocket socket;

	public SocketReceiveWorker(final Node node) {
		super();
		this.node = node;
		socket = node.getSocket();
	}

	@Override
	public void run() {
		while (node.getNodeOnFlag().get()) {
			try {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				node.updateCurrentScalarTime(null);
				node.updateVectorTimestamp(null);

				String message = new String(buf, 0, packet.getLength()).trim();
				if (message.startsWith("m")) { // message
					SocketMessageData smData = new SocketMessageData(message,
							new TreeSet<>(node.getVectorTimestamp().keySet()));

					node.addToFiveSecondsPackets(smData);

					StringBuilder sb = new StringBuilder().append("a").append(message.substring(1, message.length()));
					DatagramPacket ack = new DatagramPacket(sb.toString().getBytes(), packet.getLength(),
							packet.getSocketAddress());
					node.getSendQueue().add(ack);

					node.updateCurrentScalarTime(null);
				} else if (message.startsWith("a")) { // ack
					node.getAckReceiveQueue().add(packet);
				} else { // everything else, go to stop
					node.getNodeOnFlag().set(false);
				}
			} catch (SocketTimeoutException e) {
				// moving on...
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
