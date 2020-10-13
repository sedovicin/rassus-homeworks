package hr.fer.rassus.homework2.node;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that nodes uses to get an available port and information about other
 * nodes.
 *
 * @author Sebastian
 *
 */
public class NodeGrid {

	private static final File file = new File("nodegrid.txt");

	private final int port;
	private final List<Integer> otherPorts;

	public NodeGrid() throws Exception {
		otherPorts = new ArrayList<>();

		Integer chosenPort = null;
		int bufSize = 7;
		if (System.lineSeparator().equals("\r\n")) {
			++bufSize;
		}
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel channel = raf.getChannel()) {
			FileLock lock;
			do {
				lock = channel.tryLock();
				if (lock == null) {
					Thread.sleep(500);
				}
			} while (lock == null);

			ByteBuffer[] buf = new ByteBuffer[Math.toIntExact(channel.size() / bufSize)];
			for (int i = 0; i < buf.length; ++i) {
				buf[i] = ByteBuffer.allocate(bufSize);
			}
			channel.read(buf);

			for (int i = 0; i < buf.length; ++i) {
				byte[] bytePort = new byte[5];
				buf[i].position(0);
				buf[i].get(bytePort, 0, 5);

				Integer port = Integer.valueOf(new String(bytePort));

				if ((chosenPort == null) && (buf[i].get(5) == 'n')) {
					chosenPort = port;

					buf[i].put((byte) 'y');

					channel.position(i * bufSize);
					buf[i].flip();
					channel.write(buf[i]);
				} else {
					otherPorts.add(port);
				}
			}
		} catch (Exception e) {
			throw e;
		}

		this.port = chosenPort != null ? chosenPort.intValue() : -1;
	}

	public int getAvailablePort() {
		return port;
	}

	public List<Integer> getOtherPorts() {
		return otherPorts;
	}
}
