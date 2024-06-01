package ru.nastya.memcache.discovery;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TcpNode implements StarTopologyNode {
    // fist - coordinator
    private final List<ClusterNode> topology = new ArrayList<>();
    private final Object topologyMonitor = new Object();
    private ClusterNode currentNode;
    private boolean isCoordinator;
    private final DiscoveryProperties discoveryProperties;
    private final ExecutorService connectionPool;
    private final BlockingQueue<DiscoveryMessage> inboundMessages = new LinkedBlockingQueue<>(); // todo set limit
    private final List<DiscoveryCommunicationNode> communicationNodes = new ArrayList<>();


    public TcpNode() throws IOException {
        discoveryProperties = loadProperties();
        connectionPool = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        // 1. Start listen thread.
        // 2. In current thread scan for nodes
        boolean connected = false;
        for (IpAddress nodeAddress : discoveryProperties.discoveryAddresses()) {
            // todo except myself
            try {
                final Socket socket = new Socket(nodeAddress.host(), nodeAddress.port()) ;
                connected = connect(socket);
                if (connected) {
                    // I'm a regular node, continue
                    break;
                }
            } catch (ConnectException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        // 3. If no nodes exist, declare myself as coordinator and add to topology
        if (!connected) {
            // I'm the coordinator
        }
    }

    private boolean connect(Socket socket) throws IOException, ClassNotFoundException {
        final InputStream in = socket.getInputStream();
        final OutputStream out = socket.getOutputStream();

        final ObjectInputStream objectInputStream = new ObjectInputStream(in);
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);

        objectOutputStream.writeObject(new DiscoveryJoinMessage(currentNode)); // todo it's null
        final DiscoveryMessage response = (DiscoveryMessage) objectInputStream.readObject();
        if (response instanceof DiscoveryReconnectMessage reconnectMessage) {
            closeQuietly(socket);
            final Socket coordinatorSocket = new Socket(
                    reconnectMessage.coordinator().host(),
                    reconnectMessage.coordinator().port()
            );

            return connect(coordinatorSocket);
        } else if (response instanceof DiscoveryJoinedMessage joined) {
            synchronized (topologyMonitor) {
                topology.clear();
                topology.addAll(joined.topology());
            }
        } else {
            // todo log error
            return false;
        }

        final DiscoveryReader reader = new DiscoveryReader(socket, objectInputStream);
        final DiscoveryWriter writer = new DiscoveryWriter(socket, objectOutputStream);
        final ClusterNode coordinator;
        synchronized (topologyMonitor) {
            coordinator = topology.getFirst();
        }
        final DiscoveryCommunicationNode communicationNode = new DiscoveryCommunicationNode(coordinator, reader, writer);

        connectionPool.submit(reader);
        connectionPool.submit(writer);

        communicationNodes.add(communicationNode);

        return true;
    }

    @Override
    public void stop() {

    }

    private DiscoveryProperties loadProperties() throws IOException {
        try (final InputStream propIn = getClass().getClassLoader().getResourceAsStream("memcache.properties")) {
            final Properties props = new Properties();
            props.load(propIn);

            final String addressProperty = props.getProperty("memcache.discovery.address");
            final String portProperty = props.getProperty("memcache.discovery.port-range");
            final String[] addresses = addressProperty.split(",");
            final String[] ports = portProperty.split("-");
            final Range portRange = new Range(Integer.parseInt(ports[0]), Integer.parseInt(ports[1]));
            final List<IpAddress> discoveryAddresses = Arrays.stream(addresses)
                    .map(p -> {
                        final String[] addr = p.split(":");
                        return new IpAddress(addr[0], Integer.parseInt(addr[1]));
                    })
                    .toList();

            return new DiscoveryProperties(discoveryAddresses, portRange);
        }
    }

    private class DiscoveryCommunicationNode {
        private final ClusterNode node;
        private final DiscoveryReader reader;
        private final DiscoveryWriter writer;

        public DiscoveryCommunicationNode(ClusterNode node, DiscoveryReader reader, DiscoveryWriter writer) {
            this.node = node;
            this.reader = reader;
            this.writer = writer;
        }

    }

    private class DiscoveryReader implements Runnable, Closeable {
        private final ObjectInputStream in;
        private final Socket socket;

        public DiscoveryReader(Socket socket, ObjectInputStream in) throws IOException {
            this.socket = socket;
            this.in = in;
        }

        @Override
        public void run() {

        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    private class DiscoveryWriter implements Runnable, Closeable {
        private final ObjectOutputStream out;
        private final Socket socket;

        public DiscoveryWriter(Socket socket, ObjectOutputStream out) throws IOException {
            this.socket = socket;
            this.out = out;
        }

        @Override
        public void run() {

        }

        @Override
        public void close() throws IOException {
            closeQuietly(out);
            socket.close();
        }
    }



    @Override
    public ClusterNode getCurrentNode() {
        return currentNode;
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
