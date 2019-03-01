package mina;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.eastsoft.util.DataConvert;

import entity.SerialParam;
import socket.ChannelList;
import socket.ChannelObjsByLogiAddr;
import util.Publisher;
import util.SoftParameter;
// 服务器、客户端、serial
public class MinaTCPClient {

	// xuky 2017.05.12 不能使用static 会导致多个MinaSerialServer实例的变量都一样
	private IoSession ioSession = null;
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

	public MinaTCPClient(SerialParam s) {
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

		// 配置串口连接
		// if (runTime == 0)
		InetSocketAddress address = new InetSocketAddress(serialParam.getCOMM(), serialParam.getBaudRate());
		// SerialAddress address = new SerialAddress(name, 9600,
		// DataBits.DATABITS_8, StopBits.BITS_1,
		// Parity.EVEN, FlowControl.NONE);
		connect(address);
	}

	// 断开串口通道，从通道列表中撤出
	public void disConnect() {
		try {
			if (ioSession != null)
				ioSession.closeNow();

			ChannelList.getInstance().remove(logAddr);
			ChannelObjsByLogiAddr.getInstance().reMove(logAddr);
			String[] s = { "refresh terminal list", "", "" };
			Publisher.getInstance().publish(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void connect(InetSocketAddress address) {

		NioSocketConnector connector = new NioSocketConnector();
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
		connector.setHandler(new SerialServerHandlerByte());
		try {
			ConnectFuture future = connector.connect(address);
			future.await();
			if (future.getException() != null) {
				String msg = future.getException().getMessage();
				if (msg.equals("Serial port not found"))
					STATUS = -1;
				else
					STATUS = -2;
				// System.out.println("MinaSerialServer
				// connect:"+address.getName() + msg );
			} else {
				// xuky 2017.08.11 首先需要判断future.getException()然后再进行这里的getSession
				ioSession = future.getSession();
				ioSession.setAttribute("logAddr", logAddr);
				ioSession.setAttribute("name", name);
				ioSession.setAttribute("comID", comID);
				STATUS = 1;
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		Util698.log(MinaSerialServer.class.getName(), "future = connector.connect end " + DateTimeFun.getDateTimeSSS(),
//				Debug.LOG_INFO);

		ChannelList.getInstance().add(this, logAddr);
		ChannelObjsByLogiAddr.getInstance().add(logAddr, this);
		String[] s = { "refresh terminal list", "", "" };
		Publisher.getInstance().publish(s);

	}

	public void sendMessage(byte[] data) {
		// 获取当前连接的session
		ioSession.write(data);
		// IoSession session;
		// Map<?, ?> conMap = connector.getManagedSessions();
		// Iterator<?> iter = conMap.keySet().iterator();
		// while (iter.hasNext()) {
		// Object key = iter.next();
		// session = (IoSession) conMap.get(key);
		// session.write(data);
		// }
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
		// 继续使用串口对象存储通信参数
		SerialParam s = new SerialParam();
		s.setCOMM("192.168.1.96");
		s.setBaudRate(7000);

		MinaTCPClient minaSocketClient = new MinaTCPClient(s);
		String sData = "68 15 00 43 03 11 11 11 11 00 60 6C 05 01 01 40 01 02 00 00 C6 07 16";
		sData = sData.replaceAll(" ", "");
		byte[] byteData = new byte[sData.length() / 2];
		// 将16进制字符串转为Byte数组
		byteData = DataConvert.hexString2ByteArray(sData);
		minaSocketClient.sendMessage(byteData);
//

	}

}
