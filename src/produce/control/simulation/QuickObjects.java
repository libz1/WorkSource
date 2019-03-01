package produce.control.simulation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.future.ConnectFuture;

import produce.control.comm.CommWithRecv;

public class QuickObjects {

	// 单例模式
	private volatile static QuickObjects uniqueInstance;
	private Map<String, CommWithRecv> Serials = new ConcurrentHashMap<String, CommWithRecv>(); // 串口通信对象
	private Map<String, Integer> SerialsUsetimes = new ConcurrentHashMap<String, Integer>(); // 串口通信对象使用次数
	public static QuickObjects getInstance() {
		if (uniqueInstance == null) {
			synchronized (QuickObjects.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new QuickObjects();
				}
			}
		}
		return uniqueInstance;
	}

	private QuickObjects() {
	}

	public Map<String, CommWithRecv> getSerials() {
		return Serials;
	}

	public void setSerials(Map<String, CommWithRecv> serials) {
		Serials = serials;
	}

	public Map<String, Integer> getSerialsUsetimes() {
		return SerialsUsetimes;
	}

	public void setSerialsUsetimes(Map<String, Integer> serialsUsetimes) {
		SerialsUsetimes = serialsUsetimes;
	}


}
