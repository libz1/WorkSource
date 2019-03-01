package util;

import java.util.ArrayDeque;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// ������  �۲���ģʽ
public class Publisher extends Observable {

	// ����ģʽ
	private volatile static Publisher uniqueInstance;
	Queue<Object[]> msgData = new ArrayDeque<Object[]>();
    Lock Queuelock = new ReentrantLock();

	public static Publisher getInstance() {
		if (uniqueInstance == null) {
			synchronized (Publisher.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new Publisher();
				}
			}
		}
		return uniqueInstance;
	}

	private Publisher() {
	}

	public synchronized void publish(final Object[] data) {
		// xuky 2018.07.20 �����

		// ��������
//		2018-07-20 12:11:57:747 [3recv �˿�:1 user data��1��:nullFEFE686180090000006895004F16] socket.DealData
//		2018-07-20 12:11:57:750 [4very �˿�:1 ���:�ɹ�  recv:4F16  expect:68**8*09000000689500**16] socket.DealSendData
		// �쳣����  3recv ����  4very̫��
//		2018-07-20 12:46:01:613 [2send �˿�:1 Data:FE FE FE FE 68AAAAAAAAAAAA68150694B33C3333330316] socket.DealSendData
//		2018-07-20 12:46:01:966 [3push �˿�:1 serial recv:4F16] mina.SerialServerHandlerByte
//		2018-07-20 12:46:02:021 [3recv �˿�:1 user data��1��:FEFE686180090000006895004F16] socket.DealData
//		2018-07-20 12:46:02:258 [1run  �˿�:1 ִ�з���������-begin taskID:000000098061.30 ������汾] socket.DealSendData
//		2018-07-20 12:46:02:363 [4very �˿�:1 ���:ʧ��  recv:4F16  expect:686180090000006891243433B337837F76806469607C7C60818A5BA96561635C60646B6369636953535353535353DD16] socket.DealSendData

        Queuelock.lock();
        try {
//        	Util698.log(Publisher.class.getName(), "deal "+data, Debug.LOG_INFO);
    		deal(data);
        } finally {
            Queuelock.unlock();
        }

		// xuky 2016.11.04 ���߳���ִ�п������쳣
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
