package com.mozat.alpaca.zookeeper;

import com.mozat.alpaca.App;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CuratorNodeNotifier {

    private static final Logger logger = LoggerFactory.getLogger(CuratorNodeNotifier.class);

    private CuratorFramework zk;
    private final String zkHostPort = App.config.getString("zookeeper.hosts");

    private volatile boolean connected = false;
    private volatile boolean expired = false;

    void startZK() {
        logger.info("Connecting to ZooKeeper: {}", zkHostPort);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        zk = CuratorFrameworkFactory
                .builder()
                .connectString(zkHostPort)
                .defaultData(null)
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(15000)
                .connectionTimeoutMs(5000)
                .build();
        zk.getConnectionStateListenable().addListener(new ZKConnectionStateListener());

        zk.start();
    }

    void getNodes() throws Exception {
        logger.info("Getting all the nodes under /nodes...");

        zk.getCuratorListenable().addListener(new ZKMasterListen());

        List<String> children =  zk.getChildren().watched().forPath("/nodes");
        if (children != null) {
            for (String child: children) {
                System.out.println(child);
            }
            System.out.println("===========================");
        }
    }

    void stopZK() {
        zk.close();
    }

    boolean isConnected() {
        return connected;
    }

    boolean isExpired() {
        return expired;
    }

    public static void main(String args[]) throws Exception {
        CuratorNodeNotifier curatorNodeNotifier = new CuratorNodeNotifier();
        curatorNodeNotifier.startZK();

        while (!curatorNodeNotifier.isConnected()) {
            Thread.sleep(100);
        }

        curatorNodeNotifier.getNodes();

        while (!curatorNodeNotifier.isExpired()) {
            Thread.sleep(1000);
        }

        curatorNodeNotifier.stopZK();

    }

    class ZKConnectionStateListener implements ConnectionStateListener {

        @Override
        public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
            logger.info("ZooKeeper connection changed: {}", connectionState);
            switch (connectionState) {
                case CONNECTED:
                    logger.info("Connecting to ZooKeeper!");
                    connected = true;
                    break;
                case SUSPENDED:
                    connected = false;
                    logger.info("ZooKeeper connection Suspended!");
                    break;
                case RECONNECTED:
                    connected = true;
                    logger.info("Re-Connecting to ZooKeeper!");
                    break;
                case LOST:
                    connected = false;
                    expired = true;
                    logger.info("ZooKeeper connection lost!");
                case READ_ONLY:
                    connected = true;
                    logger.info("ZooKeeper is read-only!");
            }
        }
    }

    class ZKMasterListen implements CuratorListener {

        @Override
        public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
            try {
                if (event.getWatchedEvent().getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    getNodes();
                }
            } catch (Exception e) {
                logger.error("shit happend: {}", e.getMessage(), e);
            }
        }
    }
}
