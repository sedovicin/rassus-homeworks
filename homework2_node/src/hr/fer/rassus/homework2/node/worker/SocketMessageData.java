package hr.fer.rassus.homework2.node.worker;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class that represents content of a node's message from/to other node
 *
 * @author Sebastian
 *
 */
public class SocketMessageData {

	private String type;
	private final Integer node;
	private String sensorData;
	private Long scalarTimestamp;
	private Map<Integer, Integer> vectorTimestamp;

	public SocketMessageData(final String type, final Integer node, final String sensorData, final Long scalarTimestamp,
			final Map<Integer, Integer> vectorTimestamp) {
		super();
		this.type = type;
		this.node = node;
		this.sensorData = sensorData;
		this.scalarTimestamp = scalarTimestamp;
		this.vectorTimestamp = new TreeMap<>(vectorTimestamp);
	}

	public SocketMessageData(final String message, final Set<Integer> nodes) {
		String[] splitted = message.split(";");
		type = splitted[0];
		node = Integer.valueOf(splitted[1]);
		sensorData = splitted[2];
		scalarTimestamp = Long.valueOf(splitted[3]);
		vectorTimestamp = new TreeMap<>();
		String[] splittedVector = splitted[4].split(",");
		Iterator<Integer> nodeIterator = nodes.iterator();
		for (int i = 0; i < splittedVector.length; ++i) {
			vectorTimestamp.put(nodeIterator.next(), Integer.valueOf(splittedVector[i]));
		}

	}

	public SocketMessageData(final SocketMessageData data) {
		type = new String(data.type);
		node = Integer.valueOf(data.node);
		sensorData = new String(data.sensorData);
		scalarTimestamp = Long.valueOf(data.scalarTimestamp);
		vectorTimestamp = new TreeMap<>(data.vectorTimestamp);
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getSensorData() {
		return sensorData;
	}

	public void setSensorData(final String sensorData) {
		this.sensorData = sensorData;
	}

	public Long getScalarTimestamp() {
		return scalarTimestamp;
	}

	public void setScalarTimestamp(final Long scalarTimestamp) {
		this.scalarTimestamp = scalarTimestamp;
	}

	public Map<Integer, Integer> getVectorTimestamp() {
		return vectorTimestamp;
	}

	public void setVectorTimestamp(final Map<Integer, Integer> vectorTimestamp) {
		this.vectorTimestamp = new TreeMap<>(vectorTimestamp);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append(";").append(node).append(";").append(sensorData).append(";").append(scalarTimestamp)
				.append(";");
		for (Integer value : vectorTimestamp.values()) {
			sb.append(value).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}
}
