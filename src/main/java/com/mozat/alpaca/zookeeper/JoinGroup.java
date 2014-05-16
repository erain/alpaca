package com.mozat.alpaca.zookeeper;

import java.util.Properties;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import com.mozat.alpaca.AppBase;
import com.mozat.alpaca.util.ZooKeeperConnectionWatch;

public class JoinGroup extends ZooKeeperConnectionWatch {

	private static final Properties _properties = AppBase.properties;

	public void join(String groupName, String memberName)
			throws KeeperException, InterruptedException {
		String path = "/" + groupName + "/" + memberName;
		String createdPath = zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL);
		System.out.println("Created " + createdPath);
	}

	public static void main(String[] args) throws Exception {
		JoinGroup joinGroup = new JoinGroup();
		joinGroup.connect(_properties.getProperty("zookeeper.hosts"));
		joinGroup.join("test", "node1");

		// stay alive until process is killed or thread is interrupted
		Thread.sleep(Long.MAX_VALUE);
	}

}
