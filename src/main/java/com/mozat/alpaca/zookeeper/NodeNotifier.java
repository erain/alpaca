package com.mozat.alpaca.zookeeper;

import com.mozat.alpaca.App;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.apache.zookeeper.AsyncCallback.ChildrenCallback;

/**
 * This class will demo whether the watch is still there when network partition happens
 * @author Yi Yu<YiYu@Ymail.com>
 */
public class NodeNotifier implements Watcher{

    private static final Logger logger = LoggerFactory.getLogger(NodeNotifier.class);

    private ZooKeeper zk;
    private final String zkHostPort = App.config.getString("zookeeper.hosts");

    private volatile boolean connected = false;
    private volatile boolean expired = false;

    @Override
    public void process(WatchedEvent event) {
        logger.info("Processing event: {}", event.toString());
        if ( event.getType() == Event.EventType.None ) {
            switch (event.getState()) {
                case SyncConnected:
                    getNodes();
                    connected = true;
                    break;
                case Disconnected:
                    connected = false;
                    break;
                case Expired:
                    expired = true;
                    connected = false;
                    logger.error("Session expired.");
                default:
                    break;
            }
        }
    }

    Watcher nodesChangeWatcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                getNodes();
            }
        }
    };

    ChildrenCallback nodesGetChildrenCallback = new ChildrenCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    getNodes();
                    break;
                case OK:
                    if (children != null) {
                        for (String child: children) {
                            System.out.println(child);
                        }
                        System.out.println("===========================");
                    }
                    break;
                default:
                    logger.error("getChildren failed.", KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };

    void getNodes() {
        zk.getChildren("/nodes", nodesChangeWatcher, nodesGetChildrenCallback, null);
    }

    void startZK() throws IOException {
        zk = new ZooKeeper(zkHostPort, 15000, this);
    }

    void stopZK() throws IOException, InterruptedException{
        zk.close();
    }

    boolean isConnected() {
        return connected;
    }

    boolean isExpired() {
        return expired;
    }

    public static void main(String args[]) throws Exception {
        NodeNotifier nodeNotifier = new NodeNotifier();
        nodeNotifier.startZK();

        while (!nodeNotifier.isConnected()) {
            Thread.sleep(100);
        }

        while (!nodeNotifier.isExpired()) {
            Thread.sleep(1000);
        }

        nodeNotifier.stopZK();
    }

}
