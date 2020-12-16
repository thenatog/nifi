/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nifi.controller.state.server;

import org.apache.nifi.util.NiFiProperties;
import org.apache.zookeeper.client.ConnectStringParser;
import org.apache.zookeeper.server.DatadirCleanupManager;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ZooKeeperStateServer extends ZooKeeperServerMain {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperStateServer.class);

    static final int MIN_AVAILABLE_PORT = 2288;
    static final String SERVER_CNXN_FACTORY = "org.apache.zookeeper.server.NettyServerCnxnFactory";
    static final Map<String, String> ZOOKEEPER_TO_NIFI_PROPERTIES = new HashMap<String, String>() {{
        put("ssl.keyStore.location", "nifi.security.keystore");
        put("ssl.keyStore.password", "nifi.security.keystorePasswd");
        put("ssl.trustStore.location", "nifi.security.truststore");
        put("ssl.trustStore.password", "nifi.security.truststorePasswd");
    }};

    private final QuorumPeerConfig quorumPeerConfig;
    private volatile boolean started = false;

    private ServerCnxnFactory connectionFactory;
    private FileTxnSnapLog transactionLog;
    private ZooKeeperServer embeddedZkServer;
    private QuorumPeer quorumPeer;
    private DatadirCleanupManager datadirCleanupManager;

    private ZooKeeperStateServer(final QuorumPeerConfig config) {
        quorumPeerConfig = config;
    }

    // Expose the configuration for verification + testing:
    final QuorumPeerConfig getQuorumPeerConfig() {
        return quorumPeerConfig;
    }

    public synchronized void start() throws IOException {
        if (started) {
            return;
        }

        if (quorumPeerConfig.getPurgeInterval() > 0) {
            datadirCleanupManager = new DatadirCleanupManager(quorumPeerConfig
                    .getDataDir(), quorumPeerConfig.getDataLogDir(), quorumPeerConfig
                    .getSnapRetainCount(), quorumPeerConfig.getPurgeInterval());
            datadirCleanupManager.start();
        }

        if (quorumPeerConfig.isDistributed()) {
            startDistributed();
        } else {
            startStandalone();
        }

        started = true;
    }

    private void startStandalone() throws IOException {
        logger.info("Starting Embedded ZooKeeper Server");

        final ServerConfig config = new ServerConfig();
        config.readFrom(quorumPeerConfig);
        try {
            for (int i=0; i < 10; i++) {
                try {
                    transactionLog = new FileTxnSnapLog(config.getDataLogDir(), config.getDataDir());
                    break;
                } catch (final FileTxnSnapLog.DatadirException dde) {
                    // The constructor for FileTxnSnapLog sometimes throws a DatadirException indicating that it is unable to create data directory,
                    // but the data directory already exists. It appears to be a race condition with another ZooKeeper thread. Even if we create the
                    // directory before entering the constructor, we sometimes see the issue occur. So we just give it up to 10 tries
                    try {
                        Thread.sleep(50L);
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            embeddedZkServer = new ZooKeeperServer();
            embeddedZkServer.setTxnLogFactory(transactionLog);
            embeddedZkServer.setTickTime(config.getTickTime());
            embeddedZkServer.setMinSessionTimeout(config.getMinSessionTimeout());
            embeddedZkServer.setMaxSessionTimeout(config.getMaxSessionTimeout());

            connectionFactory = ServerCnxnFactory.createFactory();
            // TODO: This needs to set secure based on whether some properties are set (maybe there's already a isZooKeeperSecure() in NiFiProperties
            connectionFactory.configure(getAvailableSocketAddress(config), config.getMaxClientCnxns(), true);
            connectionFactory.startup(embeddedZkServer);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Embedded ZooKeeper Server interrupted", e);
        } catch (final IOException ioe) {
            throw new IOException("Failed to start embedded ZooKeeper Server", ioe);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to start embedded ZooKeeper Server", e);
        }
    }

    private void startDistributed() throws IOException {
        logger.info("Starting Embedded ZooKeeper Peer");

        try {
            transactionLog = new FileTxnSnapLog(quorumPeerConfig.getDataLogDir(), quorumPeerConfig.getDataDir());

            connectionFactory = ServerCnxnFactory.createFactory();
            connectionFactory.configure(quorumPeerConfig.getClientPortAddress(), quorumPeerConfig.getMaxClientCnxns());

            quorumPeer = new QuorumPeer();
            quorumPeer.setTxnFactory(new FileTxnSnapLog(quorumPeerConfig.getDataLogDir(), quorumPeerConfig.getDataDir()));
            quorumPeer.setElectionType(quorumPeerConfig.getElectionAlg());
            quorumPeer.setMyid(quorumPeerConfig.getServerId());
            quorumPeer.setTickTime(quorumPeerConfig.getTickTime());
            quorumPeer.setMinSessionTimeout(quorumPeerConfig.getMinSessionTimeout());
            quorumPeer.setMaxSessionTimeout(quorumPeerConfig.getMaxSessionTimeout());
            quorumPeer.setInitLimit(quorumPeerConfig.getInitLimit());
            quorumPeer.setSyncLimit(quorumPeerConfig.getSyncLimit());
            quorumPeer.setQuorumVerifier(quorumPeerConfig.getQuorumVerifier(), false);
            quorumPeer.setCnxnFactory(connectionFactory);
            quorumPeer.setZKDatabase(new ZKDatabase(quorumPeer.getTxnFactory()));
            quorumPeer.setLearnerType(quorumPeerConfig.getPeerType());
            quorumPeer.setSyncEnabled(quorumPeerConfig.getSyncEnabled());
            quorumPeer.setQuorumListenOnAllIPs(quorumPeerConfig.getQuorumListenOnAllIPs());

            quorumPeer.start();
        } catch (final IOException ioe) {
            throw new IOException("Failed to start embedded ZooKeeper Peer", ioe);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to start embedded ZooKeeper Peer", e);
        }
    }

    @Override
    public synchronized void shutdown() {
        if (started) {
            started = false;

            if (transactionLog != null) {
                try {
                    transactionLog.close();
                } catch (final IOException ioe) {
                    logger.warn("Failed to close Transaction Log", ioe);
                }
            }

            if (connectionFactory != null) {
                connectionFactory.shutdown();
            }

            if (quorumPeer != null && quorumPeer.isRunning()) {
                quorumPeer.shutdown();
            }

            if (embeddedZkServer != null && embeddedZkServer.isRunning()) {
                embeddedZkServer.shutdown();
            }

            if (datadirCleanupManager != null) {
                datadirCleanupManager.shutdown();
            }
        }
    }

    public static ZooKeeperStateServer create(final NiFiProperties properties) throws IOException, ConfigException {
        final File propsFile = properties.getEmbeddedZooKeeperPropertiesFile();
        if (propsFile == null) {
            return null;
        }

        if (!propsFile.exists() || !propsFile.canRead()) {
            throw new IOException("Cannot create Embedded ZooKeeper Server because the Properties File " + propsFile.getAbsolutePath()
                + " referenced in nifi.properties does not exist or cannot be read");
        }

        final Properties zkProperties = new Properties();
        try (final InputStream fis = new FileInputStream(propsFile);
            final InputStream bis = new BufferedInputStream(fis)) {
            zkProperties.load(bis);
        }

        return new ZooKeeperStateServer(reconcileProperties(properties, zkProperties));
    }

    /**
     * Reconcile properties between the nifi.properties and zookeeper.properties (zoo.cfg) files. Most of the ZooKeeper server properties are derived from
     * the zookeeper.properties file, while the TLS key/truststore properties are taken from nifi.properties.
     * @param niFiProperties
     * @param zkProperties
     * @return A reconciled QuorumPeerConfig which will include TLS properties set if they are available.
     * @throws IOException
     * @throws ConfigException
     */
    private static QuorumPeerConfig reconcileProperties(NiFiProperties niFiProperties, Properties zkProperties) throws IOException, ConfigException {
        QuorumPeerConfig peerConfig = new QuorumPeerConfig();
        peerConfig.parseProperties(zkProperties);

        // If this is an insecure NiFi no changes are needed:
        if (!niFiProperties.isHTTPSConfigured()) {
            logger.info("NiFi properties not mapped to ZooKeeper properties because NiFi is insecure.");
            return peerConfig;
        }

        // Remove HTTP client ports and addresses and warn if set, see NIFI-7203:
        InetSocketAddress clientPort = peerConfig.getClientPortAddress();
        if (clientPort != null) {
            zkProperties.remove("clientPort");
            zkProperties.remove("clientPortAddress");
            logger.warn("Invalid configuration detected: secure NiFi with embedded ZooKeeper configured for unsecured HTTP connections. " +
                    "Removed HTTP port from embedded ZooKeeper configuration to deactivate insecure HTTP connections.");
        } else {
            logger.info("Embedded ZooKeeper not configured for unsecured HTTP connections.");
        }

        // Disallow partial TLS configurations for ZK, it's either all or nothing to avoid inconsistent setups, see NIFI-7203:
        final Set<String> zkPropKeys = ZOOKEEPER_TO_NIFI_PROPERTIES.keySet();
        final int zkConfiguredPropCount = zkPropKeys.stream().mapToInt(key -> zkProperties.containsKey(key) ? 1 : 0).sum();
        if (zkConfiguredPropCount != 0 && zkConfiguredPropCount != zkPropKeys.size()) {
            throw new ConfigException("Embedded ZooKeeper configuration incomplete; either all TLS properties must be set or none must be set to avoid inconsistent or partial configurations.");
        }

        // Set HTTPS client port:
        final InetSocketAddress secureClientAddress = peerConfig.getSecureClientPortAddress();
        final String connectString = niFiProperties.getProperty(NiFiProperties.ZOOKEEPER_CONNECT_STRING);

        if (secureClientAddress == null && connectString == null) {
            final int secureClientPort = SocketUtils.findAvailableTcpPort(MIN_AVAILABLE_PORT);
            zkProperties.setProperty("secureClientPort", String.valueOf(secureClientPort));
            logger.info("Secure client port was not set, found and set available port {}", secureClientPort);

        } else if (secureClientAddress != null && connectString == null) {
            final int secureClientPort = secureClientAddress.getPort();
            zkProperties.setProperty("secureClientPort", String.valueOf(secureClientPort));
            logger.info("Secure client port set from ZooKeeper configuration, set port {}", secureClientPort);

        } else if (secureClientAddress == null) {
            final InetSocketAddress selectedServerAddress = getInetSocketAddress(connectString, "ZK not configured with secure port and NiFi ZK client connection string not usable.");
            final int port = selectedServerAddress.getPort();
            zkProperties.setProperty("secureClientPort", String.valueOf(port));
            logger.info("Secure client port set from NiFi ZK connection string, set port {}", port);

        } else {
            final InetSocketAddress selectedServerAddress = getInetSocketAddress(connectString, "ZK configured with secure port but NiFi ZK client connection string not usable.");
            if (secureClientAddress.getPort() != selectedServerAddress.getPort()) {

                //TODO: Nathan should this then set a zkProeprties property?
                logger.warn("Potential mismatch between NiFi ZK client connection string and embedded ZK server secure port.");
            } else {
                logger.info("Matched ZK client connection string {} with embedded ZK server secure port: {}", connectString, secureClientAddress);
            }
        }

        // Set the required connection factory for TLS
        // TODO: Nathan not sure if the key is ServerCnxnFactory.ZOOKEEPER_SERVER_CNXN_FACTORY or cnxnPropKey so check this with debugger
        final String cnxnPropKey = "serverCnxnFactory";
        zkProperties.setProperty(cnxnPropKey, SERVER_CNXN_FACTORY);

        // Copy the NiFi properties if needed:
        if (zkConfiguredPropCount == 0) {
            zkPropKeys.forEach(zkKey -> {
                final String nifiKey = ZOOKEEPER_TO_NIFI_PROPERTIES.get(zkKey);
                final String logValue = (zkKey.endsWith(".password") ? "********" : niFiProperties.getProperty(nifiKey));
                zkProperties.setProperty(zkKey, niFiProperties.getProperty(nifiKey));
                logger.info("Mapped NiFi property '{}' to ZooKeeper property '{}' with value '{}'", nifiKey, zkKey, logValue);
            });
        } else {
            logger.info("NiFi properties not mapped to ZooKeeper properties, all properties already set.");
        }

        // Recreate and reload the adjusted properties to ensure they're still valid for ZK:
        peerConfig = new QuorumPeerConfig();
        peerConfig.parseProperties(zkProperties);
        return peerConfig;
    }

    private static InetSocketAddress getInetSocketAddress(String connectString, String message) throws ConfigException {
        final ConnectStringParser parser = new ConnectStringParser(connectString);
        return parser.getServerAddresses().stream().findFirst().orElseThrow(() -> new ConfigException(message));
    }

    private static InetSocketAddress getAvailableSocketAddress(ServerConfig config) {
        return config.getSecureClientPortAddress() != null ? config.getSecureClientPortAddress() : config.getClientPortAddress();
    }
}
