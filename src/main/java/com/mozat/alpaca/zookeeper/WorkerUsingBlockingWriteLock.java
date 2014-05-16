package com.mozat.alpaca.zookeeper;

import java.util.Properties;
import java.util.Random;

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import com.mozat.alpaca.AppBase;
import com.mozat.alpaca.util.ZooKeeperConnectionHelper;

/**
 * This Worker uses the {@link BlockingWriteLock}
 * 
 * @author Yu Yi
 */
public class WorkerUsingBlockingWriteLock {

	private static final Properties PROPERTIES = AppBase.properties;

	private static void doSomeWork(String name) {
		int seconds = randomTime();
		long workTimeMillis = seconds * 1000;
		System.out.printf("%s is doing some work for %d seconds\n", name,
				seconds);
		try {
			Thread.sleep(workTimeMillis);
		} catch (InterruptedException ex) {
			System.out.printf("Oops. Interrupted.\n");
			Thread.currentThread().interrupt();
		}
	}

	private static int randomTime() {
		Random random = new Random(System.currentTimeMillis());
		return 5 + random.nextInt(5);
	}

	public static void main(String[] args) throws Exception {
		String hosts = PROPERTIES.getProperty("zookeeper.hosts");
		String path = "/test"; //args[0];
		String myName = "app1"; // args[1];

		ZooKeeperConnectionHelper connectionHelper = new ZooKeeperConnectionHelper();
		ZooKeeper zooKeeper = connectionHelper.connect(hosts);
		BlockingWriteLock lock = new BlockingWriteLock(myName, zooKeeper, path,
				ZooDefs.Ids.OPEN_ACL_UNSAFE);
		
		System.out.printf("%s is attempting to obtain lock on %s...\n", myName, path);
		
		lock.lock();
		
		System.out.printf("%s has obtained lock on %s\n", myName, path);
		
		doSomeWork(myName);
		
		System.out.printf("%s is done doing work. releasing lock on %s\n", myName, path);
		
		lock.unlock();
	}
}
