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

// 终端通信对象集合
public class ChannelList {

	private List<Channel> channelList = new ArrayList<Channel>();

	private volatile static ChannelList uniqueInstance;
    Lock Queuelock = new ReentrantLock();

	public static ChannelList getInstance() {
		if (uniqueInstance == null) {
			synchronized (ChannelList.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new ChannelList();
				}
			}
		}
		return uniqueInstance;
	}

	private ChannelList(){

		// xuky 2017.04.25 定时器，循环进行在线终端列表处理
		// 1、间隔时间为javafx.util.Duration.seconds(1) 1秒
		// 2、间隔执行的代码为dealArray()
		// 3、执行的次数为Animation.INDEFINITE 无限次数据
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

	// 循环进行在线终端列表处理
	private void dealArray() {
		// 判断终端的最近通信时间，判断终端的心跳周期，如果两倍心跳周期以上的时间无新报文，视为终端断线，则删除此通信端口
//		System.out.println("dealArray" +  LocalDateTime.now());
		for(Channel channel: channelList ){
			if (channel.getType().equals("2"))
				continue;
//			System.out.println("all-"+channel);
			if (channel.getRecvTime().equals(""))
				continue;
			// 当前时间点
			LocalDateTime now = LocalDateTime.now();
			// 最后一次通信时间
			LocalDateTime recvTime = LocalDateTime.parse(channel.getRecvTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
			// 两个时间点的间隔
			Duration duration = Duration.between(recvTime, now);
			long minutes = duration.toMinutes();
			long heatTime =  Long.valueOf(channel.getHeatTime()*2/60).longValue();
			if (minutes >= heatTime){
				// 清除在线终端内存列表
				channelList.remove(channel);
				// 通过观察者发布在线终端变化消息，用于刷新界面数据
	    		String[] s1 = {"refresh terminal list","",""};
	    		Publisher.getInstance().publish(s1);
	    		// 清除通信对象列表
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
		// 因为Channel中有Object对象，导致无法导出
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

		// 根据终端地址得到channel对象
		Channel channel = getByCode(addr);

		// xuky 2017.05.12 可能存在报文中的地址与通信对象的终端地址不一致的情况
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
