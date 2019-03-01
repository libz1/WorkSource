package util;

import java.util.ArrayDeque;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 出版者  观察者模式
public class Publisher extends Observable {

	// 单例模式
	private volatile static Publisher uniqueInstance;
	Queue<Object[]> msgData = new ArrayDeque<Object[]>();
    Lock Queuelock = new ReentrantLock();

	public static Publisher getInstance() {
		if (uniqueInstance == null) {
			synchronized (Publisher.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new Publisher();
				}
			}
		}
		return uniqueInstance;
	}

	private Publisher() {
	}

	public synchronized void publish(final Object[] data) {
		// xuky 2018.07.20 添加锁

		// 正常数据
//		2018-07-20 12:11:57:747 [3recv 端口:1 user data【1】:nullFEFE686180090000006895004F16] socket.DealData
//		2018-07-20 12:11:57:750 [4very 端口:1 结果:成功  recv:4F16  expect:68**8*09000000689500**16] socket.DealSendData
		// 异常数据  3recv 距离  4very太久
//		2018-07-20 12:46:01:613 [2send 端口:1 Data:FE FE FE FE 68AAAAAAAAAAAA68150694B33C3333330316] socket.DealSendData
//		2018-07-20 12:46:01:966 [3push 端口:1 serial recv:4F16] mina.SerialServerHandlerByte
//		2018-07-20 12:46:02:021 [3recv 端口:1 user data【1】:FEFE686180090000006895004F16] socket.DealData
//		2018-07-20 12:46:02:258 [1run  端口:1 执行非阻塞任务-begin taskID:000000098061.30 红外读版本] socket.DealSendData
//		2018-07-20 12:46:02:363 [4very 端口:1 结果:失败  recv:4F16  expect:686180090000006891243433B337837F76806469607C7C60818A5BA96561635C60646B6369636953535353535353DD16] socket.DealSendData

        Queuelock.lock();
        try {
//        	Util698.log(Publisher.class.getName(), "deal "+data, Debug.LOG_INFO);
    		deal(data);
        } finally {
            Queuelock.unlock();
        }

		// xuky 2016.11.04 在线程中执行可能有异常
//		new Thread() {
//			public void run() {
//				deal(data);
//			}
//		}.start();
//		msgData.add(data);

	}

	private synchronized void deal(Object[] data) {
//	private void deal(Object[] data) {
		setChanged();
		notifyObservers(data);
	}

}
