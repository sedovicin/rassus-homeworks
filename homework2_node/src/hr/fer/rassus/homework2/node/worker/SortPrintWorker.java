package hr.fer.rassus.homework2.node.worker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import hr.fer.rassus.homework2.node.Node;

/**
 * Thread used for sorting and printing readings in the last five seconds.
 * 
 * @author Sebastian
 *
 */
public class SortPrintWorker implements Runnable {

	private final Node node;
	private final List<SocketMessageData> fiveSecondsPackets;

	public SortPrintWorker(final Node node, final List<SocketMessageData> fiveSecondsPackets) {
		super();
		this.node = node;
		this.fiveSecondsPackets = fiveSecondsPackets;
	}

	@Override
	public void run() {
		double sum = 0d;
		Map<SocketMessageData, Long> packetScalarMap = new HashMap<>();
		Map<SocketMessageData, Integer> packetVectorMap = new HashMap<>();

		for (SocketMessageData data : fiveSecondsPackets) {
			sum += Double.valueOf(data.getSensorData());
			packetScalarMap.put(data, data.getScalarTimestamp());
			packetVectorMap.put(data, data.getVectorTimestamp().get(node.getPort()));
		}

		System.out.println("Count: " + fiveSecondsPackets.size());
		System.out.println("Sorted by scalar time:");
		Set<Integer> nodes = new TreeSet<>(node.getVectorTimestamp().keySet());
		StringBuilder sb = new StringBuilder();
		sb.append("Node;CO value;scalarTime;vt");
		for (Integer node : nodes) {
			sb.append(node).append(",");
		}
		sb.delete(sb.length() - 1, sb.length());
		System.out.println(sb.toString());
		packetScalarMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.forEach(entry -> System.out.println(entry.getKey().toString().substring(2)));
		System.out.println();

		System.out.println("Sorted by vector time:");
		System.out.println(sb.toString());
		packetVectorMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.forEach(entry -> System.out.println(entry.getKey().toString().substring(2)));
		System.out.println("Average reading: " + (sum / fiveSecondsPackets.size()));
		System.out.println();
		System.out.println();
	}

}
