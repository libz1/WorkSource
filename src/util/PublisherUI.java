package util;

import java.util.ArrayDeque;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.eastsoft.util.Debug;

import SocketAssident.Server;

// 出版者  观察者模式
public class PublisherUI extends Observable {

	// 单例模式
	private volatile static PublisherUI uniqueInstance;
	Queue<Object[]> msgData = new ArrayDeque<Object[]>();
    Lock Queuelock = new ReentrantLock();

	public static PublisherUI getInstance() {
		if (uniqueInstance == null) {
			synchronized (PublisherUI.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new PublisherUI();
				}
			}
		}
		return uniqueInstance;
	}


	public synchronized void publish(final Object[] data) {
        Queuelock.lock();
        try {
//			Util698.log(PublisherUI.class.getName(), "publish："+data, Debug.LOG_INFO);
    		deal(data);
        } finally {
            Queuelock.unlock();
        }


	}

	private synchronized void deal(Object[] data) {
		setChanged();
		notifyObservers(data);
	}

}
