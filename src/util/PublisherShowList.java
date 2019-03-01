package util;

import java.util.ArrayDeque;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// ������  �۲���ģʽ
public class PublisherShowList extends Observable {

	// ����ģʽ
	private volatile static PublisherShowList uniqueInstance;
	Queue<Object[]> msgData = new ArrayDeque<Object[]>();
    Lock Queuelock = new ReentrantLock();

	public static PublisherShowList getInstance() {
		if (uniqueInstance == null) {
			synchronized (PublisherShowList.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new PublisherShowList();
				}
			}
		}
		return uniqueInstance;
	}

	private PublisherShowList() {
	}

	public synchronized void publish(final Object[] data) {
        Queuelock.lock();
        try {
    		deal(data);
        } finally {
            Queuelock.unlock();
        }


	}

	private synchronized void deal(Object[] data) {
//	private void deal(Object[] data) {
		setChanged();
		notifyObservers(data);
	}

}
