package socket;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.eastsoft.util.Debug;

import util.Util698;

import java.util.Observable;
import java.util.Observer;

// 载波通信的锁

// xuky 2018.04.24 添加新型的锁，等待所有的红外测试完毕，再进行载波通信
// 红外测试完成的标志，有两处：1、添加载波类报文了，2、红外通信超时了
//
// 批量测试前，添加锁定标志，每个设备进行锁控每一个都通过了，解除锁定标志

public class DealSendBlockLock implements Observer {
	private volatile static DealSendBlockLock uniqueInstance;
	private Map<String, Object> ADDR_FLAG = new HashMap<String, Object>();

	// 参考https://www.cnblogs.com/dolphin0520/p/3923167.html
	private Lock lock = new ReentrantLock();    //注意这个地方

	public void addAddr(String addr) {
	    lock.lock();
	    try {
			Util698.log(DealSendBlockLock.class.getName(),
					"加锁ADDR:" + addr,
					Debug.LOG_INFO);
			ADDR_FLAG.put(addr, "1");
	    } finally {
	        lock.unlock();
	    }
	}

	public Boolean getISLOCK() {
	    lock.lock();
	    try {
			// 如果ADDR_FLAG中有数据，且其数据为1，则认为还是锁定状态
			Boolean haveData = false;
			for (Entry<String, Object> entry : ADDR_FLAG.entrySet()) {
				if (entry.getValue().equals("1")){
//					Util698.log(DealSendBlockLock.class.getName(),
//							"有锁ADDR:" + entry.getKey(),
//							Debug.LOG_INFO);
					return true;
				}
				haveData = true;
			}
			if (haveData){
				Util698.log(DealSendBlockLock.class.getName(),
						"全部解锁！释放锁！",	Debug.LOG_INFO);
				ADDR_FLAG = null;
				ADDR_FLAG = new HashMap<String, Object>();

				Util698.log(DealSendBlockLock.class.getName(),
						"设置DealSendBlockData.getInstance().setISBUSY(false)",	Debug.LOG_INFO);
				DealSendBlockData.getInstance().setISBUSY(false);
			}
			return false;
	    } finally {
	        lock.unlock();
	    }

	}

	public static DealSendBlockLock getInstance() {
		if (uniqueInstance == null) {
			synchronized (DealSendBlockLock.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new DealSendBlockLock();
				}
			}
		}
		return uniqueInstance;
	}

	private DealSendBlockLock() {
		// ISLOCK = false;
	}

	@Override
	public void update(Observable o, Object arg) {
		// Object[] s = (Object[]) arg;
		// if (s[0].equals("recv frame") && s[1].equals("user data")) {
		// // 调用DealSendData的代码，便于进行维护
		// DealSendData.getInstance().DealData(map,arg,"阻塞处理");
		// }
	}

	public void removeAddr(String addr, String type) {
	    lock.lock();
	    try {
			Util698.log(DealSendBlockLock.class.getName(),
					"解锁ADDR("+type+"):" + addr,
					Debug.LOG_INFO);

			ADDR_FLAG.remove(addr);
	    } finally {
	        lock.unlock();
	    }
	}

	public void init() {
	    lock.lock();
	    try {
			Util698.log(DealSendBlockLock.class.getName(),
					"清空DealSendBlockLock",
					Debug.LOG_INFO);
			ADDR_FLAG = null;
			ADDR_FLAG = new HashMap<String, Object>();
	    } finally {
	        lock.unlock();
	    }
	}

	// 修改锁标志
	public void setAddr(String addr, String type) {
	    lock.lock();
	    try {
			Util698.log(DealSendBlockLock.class.getName(),
					"修改锁ADDR("+type+"):" + addr+"标志",
					Debug.LOG_INFO);
			ADDR_FLAG.put(addr, "0");
	    } finally {
	        lock.unlock();
	    }
	}

}
