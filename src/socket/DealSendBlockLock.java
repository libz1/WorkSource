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

// �ز�ͨ�ŵ���

// xuky 2018.04.24 ������͵������ȴ����еĺ��������ϣ��ٽ����ز�ͨ��
// ���������ɵı�־����������1������ز��౨���ˣ�2������ͨ�ų�ʱ��
//
// ��������ǰ�����������־��ÿ���豸��������ÿһ����ͨ���ˣ����������־

public class DealSendBlockLock implements Observer {
	private volatile static DealSendBlockLock uniqueInstance;
	private Map<String, Object> ADDR_FLAG = new HashMap<String, Object>();

	// �ο�https://www.cnblogs.com/dolphin0520/p/3923167.html
	private Lock lock = new ReentrantLock();    //ע������ط�

	public void addAddr(String addr) {
	    lock.lock();
	    try {
			Util698.log(DealSendBlockLock.class.getName(),
					"����ADDR:" + addr,
					Debug.LOG_INFO);
			ADDR_FLAG.put(addr, "1");
	    } finally {
	        lock.unlock();
	    }
	}

	public Boolean getISLOCK() {
	    lock.lock();
	    try {
			// ���ADDR_FLAG�������ݣ���������Ϊ1������Ϊ��������״̬
			Boolean haveData = false;
			for (Entry<String, Object> entry : ADDR_FLAG.entrySet()) {
				if (entry.getValue().equals("1")){
//					Util698.log(DealSendBlockLock.class.getName(),
//							"����ADDR:" + entry.getKey(),
//							Debug.LOG_INFO);
					return true;
				}
				haveData = true;
			}
			if (haveData){
				Util698.log(DealSendBlockLock.class.getName(),
						"ȫ���������ͷ�����",	Debug.LOG_INFO);
				ADDR_FLAG = null;
				ADDR_FLAG = new HashMap<String, Object>();

				Util698.log(DealSendBlockLock.class.getName(),
						"����DealSendBlockData.getInstance().setISBUSY(false)",	Debug.LOG_INFO);
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
					// ˫�ؼ�����
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
		// // ����DealSendData�Ĵ��룬���ڽ���ά��
		// DealSendData.getInstance().DealData(map,arg,"��������");
		// }
	}

	public void removeAddr(String addr, String type) {
	    lock.lock();
	    try {
			Util698.log(DealSendBlockLock.class.getName(),
					"����ADDR("+type+"):" + addr,
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
					"���DealSendBlockLock",
					Debug.LOG_INFO);
			ADDR_FLAG = null;
			ADDR_FLAG = new HashMap<String, Object>();
	    } finally {
	        lock.unlock();
	    }
	}

	// �޸�����־
	public void setAddr(String addr, String type) {
	    lock.lock();
	    try {
			Util698.log(DealSendBlockLock.class.getName(),
					"�޸���ADDR("+type+"):" + addr+"��־",
					Debug.LOG_INFO);
			ADDR_FLAG.put(addr, "0");
	    } finally {
	        lock.unlock();
	    }
	}

}
