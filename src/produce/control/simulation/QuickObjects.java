package produce.control.simulation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.future.ConnectFuture;

import produce.control.comm.CommWithRecv;

public class QuickObjects {

	// ����ģʽ
	private volatile static QuickObjects uniqueInstance;
	private Map<String, CommWithRecv> Serials = new ConcurrentHashMap<String, CommWithRecv>(); // ����ͨ�Ŷ���
	private Map<String, Integer> SerialsUsetimes = new ConcurrentHashMap<String, Integer>(); // ����ͨ�Ŷ���ʹ�ô���
	public static QuickObjects getInstance() {
		if (uniqueInstance == null) {
			synchronized (QuickObjects.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
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
