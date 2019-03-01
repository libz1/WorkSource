package socket;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import produce.entity.ProduceCaseResult;


//  �������ݻ����� (����ģʽ�����з��͵����ݶ���������)
public class SendData {
	private volatile static SendData uniqueInstance;

	Queue<ProduceCaseResult> recvData = new ArrayDeque<ProduceCaseResult>();
    Lock lock = new ReentrantLock();

	public static SendData getInstance() {
		if (uniqueInstance == null) {
			synchronized (SendData.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new SendData();
				}
			}
		}
		return uniqueInstance;
	}

	// xuky 2018.02.07 ���ص�һ��Ԫ��
	public ProduceCaseResult getFirst(){
		ProduceCaseResult ret = null;
		try{
			ret = recvData.element();
		}
		catch(Exception e){
			ret = null;
		}
		return ret;
	}


	public void push(ProduceCaseResult p){
	    lock.lock();
	    try {
			// ѹջ
			recvData.offer(p);
	    }
	    finally {
	      lock.unlock();
	    }
	}

	public ProduceCaseResult pop(){
	    lock.lock();
	    try {
			// ��ջ
			ProduceCaseResult p = recvData.poll() ;
			return p;
	    }
	    finally {
	      lock.unlock();
	    }
	}

}
