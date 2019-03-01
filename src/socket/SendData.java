package socket;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import produce.entity.ProduceCaseResult;


//  发送数据缓冲区 (单例模式，所有发送的数据都放在这里)
public class SendData {
	private volatile static SendData uniqueInstance;

	Queue<ProduceCaseResult> recvData = new ArrayDeque<ProduceCaseResult>();
    Lock lock = new ReentrantLock();

	public static SendData getInstance() {
		if (uniqueInstance == null) {
			synchronized (SendData.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new SendData();
				}
			}
		}
		return uniqueInstance;
	}

	// xuky 2018.02.07 返回第一个元素
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
			// 压栈
			recvData.offer(p);
	    }
	    finally {
	      lock.unlock();
	    }
	}

	public ProduceCaseResult pop(){
	    lock.lock();
	    try {
			// 出栈
			ProduceCaseResult p = recvData.poll() ;
			return p;
	    }
	    finally {
	      lock.unlock();
	    }
	}

}
