package com.mozat.alpaca.zookeeper;

import java.util.Properties;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import com.mozat.alpaca.AppBase;

public class CreateGroup extends ZooKeeperConnectionWatch {
	
	private static final Properties properties = AppBase.properties;

	public void create(String groupName) throws KeeperException,
			InterruptedException {
		String path = "/" + groupName;
		String createdPath = zk.create(path, null,
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
