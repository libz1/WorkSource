package socket;

import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.comm.SerialPort;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;
import com.google.gson.Gson;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import mina.MinaSerialServer;
import mina.MinaTCPClient;
import util.Publisher;
import util.Util698;

// �ն�ͨ�Ŷ��󼯺�
public class ChannelList {

	private List<Channel> channelList = new ArrayList<Channel>();

	private volatile static ChannelList uniqueInstance;
    Lock Queuelock = new ReentrantLock();

	public static ChannelList getInstance() {
		if (uniqueInstance == null) {
			synchronized (ChannelList.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new ChannelList();
				}
			}
		}
		return uniqueInstance;
	}

	private ChannelList(){

		// xuky 2017.04.25 ��ʱ����ѭ�����������ն��б���
		// 1�����ʱ��Ϊjavafx.util.Duration.seconds(1) 1��
		// 2�����ִ�еĴ���ΪdealArray()
		// 3��ִ�еĴ���ΪAnimation.INDEFINITE ���޴�����
			Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
				try{
					dealArray();
				}
				catch (Exception e){
					Util698.log(ChannelList.class.getName(), "ChannelList Exception:"+e.getMessage(), Debug.LOG_INFO);
				}
			}));
			timeline.setCycleCount(Animation.INDEFINITE);
			timeline.play();

	}

	// ѭ�����������ն��б���
	private void dealArray() {
		// �ж��ն˵����ͨ��ʱ�䣬�ж��ն˵��������ڣ�������������������ϵ�ʱ�����±��ģ���Ϊ�ն˶��ߣ���ɾ����ͨ�Ŷ˿�
//		System.out.println("dealArray" +  LocalDateTime.now());
		for(Channel channel: channelList ){
			if (channel.getType().equals("2"))
				continue;
//			System.out.println("all-"+channel);
			if (channel.getRecvTime().equals(""))
				continue;
			// ��ǰʱ���
			LocalDateTime now = LocalDateTime.now();
			// ���һ��ͨ��ʱ��
			LocalDateTime recvTime = LocalDateTime.parse(channel.getRecvTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
			// ����ʱ���ļ��
			Duration duration = Duration.between(recvTime, now);
			long minutes = duration.toMinutes();
			long heatTime =  Long.valueOf(channel.getHeatTime()*2/60).longValue();
			if (minutes >= heatTime){
				// ��������ն��ڴ��б�
				channelList.remove(channel);
				// ͨ���۲��߷��������ն˱仯��Ϣ������ˢ�½�������
	    		String[] s1 = {"refresh terminal list","",""};
	    		Publisher.getInstance().publish(s1);
	    		// ���ͨ�Ŷ����б�
				ChannelObjs.getInstance().reMove(channel.getAddr());
			}
			//addr = /192.168.1.96:10178 logAddr = 000000030092 type= 0 ip=  port=  connectTime= 2017-04-26 09:05:36:000 recvTime= 2017-04-26 09:09:10:695 heatTime= 60
			// recvTime= 2017-04-26 09:09:10:695 heatTime= 60
		}
//		for(Channel channel: channelList ){
//			System.out.println("removed-"+channel);
//		}
	}

	public String converToString() {
		// ��ΪChannel����Object���󣬵����޷�����
		return new Gson().toJson(channelList);
	}

	public Channel getByCode(String addr) {
		Channel channel_ = null;
		for (Channel channel : channelList)
			if (channel.getAddr().equals(addr)) {
				channel_ = channel;
				break;
			}
		return channel_;
	}

	public void add(Channel channel) {
		Channel Channel_ = getByCode(channel.getAddr());
		if (Channel_ == null)
			channelList.add(channel);
	}
	public void remove(Channel channel) {
		Channel channel_ = getByCode(channel.getAddr());
		if (channel_ != null)
			channelList.remove(channel_);
	}

	public void add(Socket socket ) {
		Channel channel = new Channel();
		String addr = socket.getRemoteSocketAddress().toString();
		channel.setAddr(addr);
		add(channel);
		ChannelObjs.getInstance().add(addr,socket);
	}

	public void remove(Socket socket ) {
		Channel channel = new Channel();
		String addr = socket.getRemoteSocketAddress().toString();
		channel.setAddr(addr);
		remove(channel);
		ChannelObjs.getInstance().reMove(addr);
	}

	public void remove(String addr ) {
		Channel channel = new Channel();
		channel.setAddr(addr);
		remove(channel);
		ChannelObjs.getInstance().reMove(addr);
	}

	public void add(SerialPort sPort ) {
		Channel channel = new Channel();
		String addr = sPort.getName();
		channel.setAddr(addr);
		channel.setType("2");
		add(channel);

		ChannelObjs.getInstance().add(addr,sPort);
	}

	public List<Channel> getChannelList() {
		return channelList;
	}


	public synchronized void change(String data) {
	    Queuelock.lock();
	    try {
	    	change(data,1);
	    } finally {
	        Queuelock.unlock();
	    }
	}

	public synchronized void change(String data,int flag) {
		String addr = data.split(";")[0].split("@")[1];

		// �����ն˵�ַ�õ�channel����
		Channel channel = getByCode(addr);

		// xuky 2017.05.12 ���ܴ��ڱ����еĵ�ַ��ͨ�Ŷ�����ն˵�ַ��һ�µ����
		if (channel == null)
			return;

		if (data.indexOf("reqType")>=0){
			String reqType = data.split(";")[1].split("@")[1];
			String heartTime = data.split(";")[2].split("@")[1];
			String reqDateTime = data.split(";")[3].split("@")[1];
			String sadata = data.split(";")[4].split("@")[1];

			channel.setConnectTime(reqDateTime);
			channel.setRecvTime(reqDateTime);
			channel.setHeatTime(DataConvert.String2Int(heartTime));
			channel.setLogAddr(sadata);
			ChannelObjs co = ChannelObjs.getInstance();
			ChannelObjsByLogiAddr col = ChannelObjsByLogiAddr.getInstance();
			col.add(sadata, co.get(addr));
//			System.out.println("ChannelObjs=>"+co);
//			System.out.println("ChannelObjsByLogiAddr=>"+col);
			if (reqType.equals("0"))
				channel.setConnectTime(reqDateTime);
		}
		else{
			String sadata = data.split(";")[1].split("@")[1];
			channel.setLogAddr(sadata);
			String time  = DateTimeFun.getDateTimeSSS();
			channel.setRecvTime(time);
			ChannelObjsByLogiAddr.getInstance().add(sadata, ChannelObjs.getInstance().get(addr));
		}
	}

	public void removeAll(){
		Util698.ListReMoveAll(channelList);
	}

	public void add(MinaSerialServer minaSerialServer, String logAddr) {
		Channel channel = new Channel();
		String addr = minaSerialServer.getName();
		channel.setAddr(logAddr);
		channel.setType("2");
		channel.setLogAddr(addr);
		channel.setStatus(minaSerialServer.getSTATUS());
		add(channel);

		ChannelObjs.getInstance().add(addr,minaSerialServer);

	}

	public void add(MinaTCPClient minaSocketClient, String logAddr) {
		Channel channel = new Channel();
		String addr = minaSocketClient.getName();
		channel.setAddr(logAddr);
		channel.setType("2");
		channel.setLogAddr(addr);
		channel.setStatus(minaSocketClient.getSTATUS());
		add(channel);

		ChannelObjs.getInstance().add(addr,minaSocketClient);

	}
}
