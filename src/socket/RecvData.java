package socket;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//  接收数据缓冲区 (单例模式，所以收到的数据都放在这里)
public class RecvData {
	private volatile static RecvData uniqueInstance;

	Queue<String> recvData = new LinkedList<String>();
	Lock lock = new ReentrantLock();

	public static RecvData getInstance() {
		if (uniqueInstance == null) {
			synchronized (RecvData.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new RecvData();
				}
			}
		}
		return uniqueInstance;
	}

	public void push(String data) {
		lock.lock();
		try {
			recvData.offer(data);
		} finally {
			lock.unlock();
		}
	}

	public String pop() {
		lock.lock();
		try {
			String str = recvData.poll();
			if (str == null)
				str = "";
			return str;
		} finally {
			lock.unlock();
		}
	}

	public static void main(String[] args) {
		Queue<String> recvData1 = new LinkedList<String>();
		recvData1.offer("1");
		recvData1.offer("2");
		System.out.println(recvData1.poll());
		System.out.println(recvData1.poll());
	}

}
