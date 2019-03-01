package socket;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//  �������ݻ����� (����ģʽ�������յ������ݶ���������)
public class RecvData {
	private volatile static RecvData uniqueInstance;

	Queue<String> recvData = new LinkedList<String>();
	Lock lock = new ReentrantLock();

	public static RecvData getInstance() {
		if (uniqueInstance == null) {
			synchronized (RecvData.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
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
