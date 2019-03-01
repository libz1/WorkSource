package util;

import java.util.ArrayDeque;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.eastsoft.util.Debug;

// 出版者  观察者模式
public class PublisherFrame extends Observable {

	// 单例模式
	private volatile static PublisherFrame uniqueInstance;
	Queue<Object[]> msgData = new ArrayDeque<Object[]>();
    Lock Queuelock = new ReentrantLock();

	public static PublisherFrame getInstance() {
		if (uniqueInstance == null) {
			synchronized (PublisherFrame.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new PublisherFrame();
				}
			}
		}
		return uniqueInstance;
	}


	public synchronized void publish(final Object[] data) {
//    	Util698.log(PublisherFrame.class.getName(), "deal "+data, Debug.LOG_INFO);
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
