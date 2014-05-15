package com.mozat.alpaca.zookeeper;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import com.mozat.alpaca.AppBase;

public class CreateGroup implements Watcher {

	private static final int SESSION_TIMEOUT = 5000;
	private ZooKeeper _zk;
	private CountDownLatch _connectedSignal = new CountDownLatch(1);
	private static final Properties properties = AppBase.properties;

	public void connect(String hosts) throws IOException, InterruptedException {
		_zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
		_connectedSignal.await();
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getState() == Event.KeeperState.SyncConnected) {
			System.out.println("Connected...");
			_connectedSignal.countDown();
		}
	}

	public void close() throws InterruptedException {
		_zk.close();
	}

	public void create(String groupName) throws KeeperException,
			InterruptedException {
		String path = "/" + groupName;
		String createdPath = _zk.create(path, null,
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println("Created " + createdPath);
	}

	public static void main(String[] args) throws Exception {
		CreateGroup createGroup = new CreateGroup();
		createGroup.connect(properties.getProperty("zookeeper.hosts"));
		createGroup.create(properties.getProperty("zookeeper.groupname", "haha"));
		createGroup.close();
	}
}
