package javafx.assident;

import java.util.Observable;
import java.util.Observer;

import SocketAssident.ClientSimulator;
import util.PublisherUI;

// xuky 2018.06.04 消息中心
public class MessageCenter implements Observer {
	public String PLCSimulator_msg = "";
	public String LinkRemain_Server = "",LinkRemain_Client = "";
	public String WatchDog_msg = "";
	public String PLC2MES_msg = "";

	private volatile static MessageCenter uniqueInstance;
	public static MessageCenter getInstance() {
		if (uniqueInstance == null)
			synchronized (ClientSimulator.class) {
				if (uniqueInstance == null)
					// 双重检查加锁
					uniqueInstance = new MessageCenter();
			}
		return uniqueInstance;
	}
	private MessageCenter() {
		PublisherUI.getInstance().addObserver(this);
	}

	@Override
	public synchronized void update(Observable o, Object arg) {
//		System.out.println("MessageCenter update:"+arg);
		try {
			Object[] s = (Object[]) arg;
			if (s[0].equals("PLCSimulator")) {
				PLCSimulator_msg = "累计测试次数:" + (String) s[1];
			}
			if (s[0].equals("PLC2MES")) {
				PLC2MES_msg = (String) s[1];
			}
			if (s[0].equals("LinkRemain")) {
				if (s[1].equals("Server")){
					LinkRemain_Server = (String) s[2];
//					System.out.println("LinkRemain_Server:"+LinkRemain_Server);
				}
				else if (s[1].equals("Client"))
					LinkRemain_Client = (String) s[2];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
