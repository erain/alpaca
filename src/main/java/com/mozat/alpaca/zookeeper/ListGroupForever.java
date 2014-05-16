package com.mozat.alpaca.zookeeper;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.ZooKeeper;

import com.mozat.alpaca.AppBase;
import com.mozat.alpaca.util.ZooKeeperConnectionHelper;

public class ListGroupForever {

	private static final Properties _properties = AppBase.properties;
	private ZooKeeper _zooKeeper;
	private Semaphore _semaphore = new Semaphore(1);

	public ListGroupForever(ZooKeeper zooKeeper) {
		_zooKeeper = zooKeeper;
	}

	public void listForever(String groupName) throws KeeperException,
			InterruptedException {
		_semaphore.acquire();
		while (true) {
			list(groupName);
			_semaphore.acquire();
		}
	}

	public void list(String groupName) throws KeeperException,
			InterruptedException {
		String path = "/" + groupName;

		List<String> chidren = _zooKeeper.getChildren(path, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getType() == Event.EventType.NodeChildrenChanged) {
					_semaphore.release();
				}
			}
		});

		if (chidren.isEmpty()) {
			System.out.printf("No members in group %s\n", groupName);
			System.out.println("---------------------------");
			return;
		}

		Collections.sort(chidren);
		System.out.println(chidren);
		System.out.println("---------------------------");
	}
	
	public static void main(String[] args) throws Exception {
		String hosts = _properties.getProperty("zookeeper.hosts");
		ZooKeeper zk = new ZooKeeperConnectionHelper().connect(hosts);
		new ListGroupForever(zk).listForever("test");
	}
	
}
