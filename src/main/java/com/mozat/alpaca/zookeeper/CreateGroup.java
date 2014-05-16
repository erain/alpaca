package com.mozat.alpaca.zookeeper;

import java.util.Properties;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import com.mozat.alpaca.AppBase;
import com.mozat.alpaca.util.ZooKeeperConnectionWatch;

public class CreateGroup extends ZooKeeperConnectionWatch {

	private static final Properties _properties = AppBase.properties;

	public void create(String groupName) throws KeeperException,
			InterruptedException {
		String path = "/" + groupName;
		String createdPath = zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);
		System.out.println("Created " + createdPath);
	}

	public static void main(String[] args) throws Exception {
		CreateGroup createGroup = new CreateGroup();
		createGroup.connect(_properties.getProperty("zookeeper.hosts"));
		createGroup.create("test");
		createGroup.close();
	}
}
