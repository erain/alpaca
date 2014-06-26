package com.mozat.alpaca.zookeeper;

import com.mozat.alpaca.App;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CuratorNodeNotifier {

    private static final Logger logger = LoggerFactory.getLogger(CuratorNodeNotifier.class);

    private CuratorFramework zk;
    private PathChildrenCache cache;
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

        try {
            cache = new PathChildrenCache(zk, "/nodes", true);
            cache.start();
        } catch (Exception e) {
            logger.error("Error in starting path cache: {}", e.getMessage(), e);
        }
    }


    void stopZK() {
        CloseableUtils.closeQuietly(cache);
        CloseableUtils.closeQuietly(zk);
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

        curatorNodeNotifier.addListener(curatorNodeNotifier.cache);
        curatorNodeNotifier.outputPath();

        while (!curatorNodeNotifier.isExpired()) {
            Thread.sleep(1000);
        }

        curatorNodeNotifier.stopZK();
    }

    void outputPath() throws Exception {
        System.out.println("*****");
        List<String> nodes = ZKPaths.getSortedChildren(zk.getZookeeperClient().getZooKeeper(), "/nodes");
        if (nodes != null) {
            for (String node : nodes) {
                System.out.println(node);
            }
            System.out.println("---------------------");
        }
    }

    private void addListener(PathChildrenCache cache) {
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework zk, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        System.out.println("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        outputPath();
                        break;
                    case CHILD_UPDATED:
                        System.out.println("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        outputPath();
                        break;
                    case CHILD_REMOVED:
                        System.out.println("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        outputPath();
                        break;
                }
            }
        };
        cache.getListenable().addListener(listener);
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

}
