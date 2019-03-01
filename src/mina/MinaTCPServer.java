package mina;

import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import entity.SerialParam;
import socket.DealData;
import socket.DealSendData;
import util.Publisher;
import util.PublisherFrame;
import util.SoftParameter;
import util.Util698;

// 服务器、客户端、serial
public class MinaTCPServer implements Observer{

	private SerialServerHandlerByte handle;
	NioSocketAcceptor connector = null;
	// 串口的名称，需要与其他的进行区别，一般是COM1。。COM2...
	private String name = "";
	// 串口所绑定的终端地址
	private String logAddr = "";

	private String comID; // 测试用例中用于区分不同串口用

	private int STATUS; // 状态 0 未连接 1成功 -1不存在 -2已打开

	private SerialParam serialParam;

	public SerialParam getSerialParam() {
		return serialParam;
	}

	public void setSerialParam(SerialParam serialParam) {
		this.serialParam = serialParam;
	}

	public MinaTCPServer(SerialParam s) {
		Util698.log(MinaTCPServer.class.getName(), "开启UDP Server:"+s.getCOMM() +"-"+s.getBaudRate(), Debug.LOG_INFO);
		Publisher.getInstance().addObserver(this);
		serialParam = s;
		init();
	}

	public void init() {
		STATUS = 0;
		comID = serialParam.getCOMID(); // 用于区分不同的串口

		// xuky 2017.08.26 防止后续出现null的情况
		SoftParameter.getInstance().getRecvDataMap().put(comID,"");

		name = serialParam.getCOMM();
		logAddr = serialParam.getTerminal();

		// xuky 2017.06.22 通过串口对电表进行操作时，串口信息中的终端地址必须为空
		if (logAddr.equals("")) {
			logAddr = comID;
		}

		InetSocketAddress address = new InetSocketAddress(serialParam.getCOMM(), serialParam.getBaudRate());
		connect(address);
	}

	// 断开串口通道，从通道列表中撤出
	public void disConnect() {
		try {
			if (connector != null){
//				connector.
//				connector.dispose(true);
				connector.dispose();
				// xuky 2017.11.14 出现过异常
//				2017-11-14 15:18:28:674 [Unexpected exception.] util.DefaultExceptionMonitor
//				java.lang.InterruptedException
//					at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireSharedInterruptibly(Unknown Source)
//					at java.util.concurrent.Semaphore.acquire(Unknown Source)
//					at org.apache.mina.transport.socket.nio.NioDatagramAcceptor$Acceptor.run(NioDatagramAcceptor.java:168)
//					at org.apache.mina.util.NamePreservingRunnable.run(NamePreservingRunnable.java:64)
//					at java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
//					at java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
//					at java.lang.Thread.run(Unknown Source)


			}

//			ChannelList.getInstance().remove(logAddr);
//			ChannelObjsByLogiAddr.getInstance().reMove(logAddr);
//			String[] s = { "refresh terminal list", "", "" };
//			Publisher.getInstance().publish(s);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("MinaUDPServer disConnect err");
		}
	}

	private void connect(InetSocketAddress address) {

		connector = new NioSocketAcceptor();

		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
		handle = new SerialServerHandlerByte();
		connector.setHandler(handle);
		try {
			SocketSessionConfig dcfg = connector.getSessionConfig();
			dcfg.setReuseAddress(true);
			connector.bind(address);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendMessage(byte[] data) {
		IoSession  sendSession = handle.getSESSION();
		if (sendSession != null){
			sendSession.setAttribute("logAddr", logAddr);
			sendSession.setAttribute("name", name);
			sendSession.setAttribute("comID", comID);
			sendSession.write(data);
		}
	}

	public String getName() {
		return name;
	}

	public int getSTATUS() {
		return STATUS;
	}

	public void setSTATUS(int sTATUS) {
		STATUS = sTATUS;
	}

	public static void main(String[] args) {
		SerialParam s = new SerialParam();
		s.setCOMM("192.168.1.210");
		s.setBaudRate(9000);

		DealData.getInstance();
		MinaTCPServer minaUDPServer = new MinaTCPServer(s);

	}

	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		 if (s[0].equals("recv frame") && s[1].equals("user data")) {
			 DealData(arg);
		 }

	}

	private void DealData(Object arg) {

//		String sData = "68 23 01 00 00 00 00 68 94 00 88 16";
		String sData = "";
		sData = DealSendData.getInstance().getReplyData();
		sData = sData.replaceAll(" ", "");
		byte[] byteData = new byte[sData.length() / 2];
		// 将16进制字符串转为Byte数组
		byteData = DataConvert.hexString2ByteArray(sData);

		System.out.println("MinaUDPServer sendMessage...");
		sendMessage(byteData);

		System.out.println("MinaUDPServer disConnect...");
		disConnect();

		System.out.println("MinaUDPServer deleteObserver...");
		Publisher.getInstance().deleteObserver(this);

		String[] s = (String[]) arg;
		String[] s1 = { "recv frame", "user data", s[2], "udp-server" };
		System.out.println("MinaUDPServer publish...");
		PublisherFrame.getInstance().publish(s1);


//		DealData.getInstance().setRunning(false);

	}

}
