package mina;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.serial.SerialAddress;
import org.apache.mina.transport.serial.SerialAddress.DataBits;
import org.apache.mina.transport.serial.SerialAddress.FlowControl;
import org.apache.mina.transport.serial.SerialAddress.Parity;
import org.apache.mina.transport.serial.SerialAddress.StopBits;
import org.apache.mina.transport.serial.SerialConnector;

import com.eastsoft.util.Debug;

import entity.SerialParam;
import socket.ChannelList;
import socket.ChannelObjsByLogiAddr;
import util.Publisher;
import util.SoftParameter;
import util.Util698;

// 服务器、客户端、serial
public class MinaSerialServer {

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

	public MinaSerialServer(SerialParam s) {
		serialParam = s;
		init();
	}

	public void init() {

//		Util698.log(MinaSerialServer.class.getName(), "init", Debug.LOG_INFO);

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

		Parity parity = null;
		if (serialParam.getParity().indexOf("0") >= 0)
			parity = Parity.NONE;
		if (serialParam.getParity().indexOf("1") >= 0)
			parity = Parity.ODD;
		if (serialParam.getParity().indexOf("2") >= 0)
			parity = Parity.EVEN;
		if (serialParam.getParity().indexOf("3") >= 0)
			parity = Parity.MARK;
		if (serialParam.getParity().indexOf("4") >= 0)
			parity = Parity.SPACE;

		// 配置串口连接
		// if (runTime == 0)
		SerialAddress address = new SerialAddress(serialParam.getCOMM(), serialParam.getBaudRate(), DataBits.DATABITS_8,
				StopBits.BITS_1, parity, FlowControl.NONE);
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
			Util698.log(MinaSerialServer.class.getName(), "disConnect Exception"+e.getMessage(), Debug.LOG_INFO);
		}
	}

	private void connect(SerialAddress address) {
		Util698.log(MinaSerialServer.class.getName(), "开启串口"+address+"开始", Debug.LOG_INFO);

//		Util698.log(MinaSerialServer.class.getName(), "connect", Debug.LOG_INFO);

		SerialConnector connector = new SerialConnector();
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
//		Util698.log(MinaSerialServer.class.getName(), "addLast end", Debug.LOG_INFO);
		connector.setHandler(new SerialServerHandlerByte());

//		Util698.log(MinaSerialServer.class.getName(), "setHandler end", Debug.LOG_INFO);
//		Util698.log(MinaSerialServer.class.getName(), "开启串口"+address+"...1", Debug.LOG_INFO);

		try {
			ConnectFuture future = connector.connect(address);
//			Util698.log(MinaSerialServer.class.getName(), "开启串口"+address+"...2", Debug.LOG_INFO);
//			Util698.log(MinaSerialServer.class.getName(), "connector.connect end", Debug.LOG_INFO);

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
//				Util698.log(MinaSerialServer.class.getName(), "开启串口"+address+"...3", Debug.LOG_INFO);
				// xuky 2017.08.11 首先需要判断future.getException()然后再进行这里的getSession
				ioSession = future.getSession();
				ioSession.setAttribute("logAddr", logAddr);
				ioSession.setAttribute("name", name);
				ioSession.setAttribute("comID", comID);
				STATUS = 1;
//				Util698.log(MinaSerialServer.class.getName(), "ioSession", Debug.LOG_INFO);
			}

		} catch (Exception e) {
			Util698.log(MinaSerialServer.class.getName(), "connect Exception"+e.getMessage(), Debug.LOG_INFO);
		}
//		Util698.log(MinaSerialServer.class.getName(), "future = connector.connect end " + DateTimeFun.getDateTimeSSS(),
//				Debug.LOG_INFO);
//		Util698.log(MinaSerialServer.class.getName(), "开启串口"+address+"...4", Debug.LOG_INFO);

		ChannelList.getInstance().add(this, logAddr);
		ChannelObjsByLogiAddr.getInstance().add(logAddr, this);
		String[] s = { "refresh terminal list", "", "" };
		Publisher.getInstance().publish(s);

		Util698.log(MinaSerialServer.class.getName(), "开启串口"+address+"完成", Debug.LOG_INFO);

	}

	public void sendMessage(byte[] data) {
		// 获取当前连接的session
		if (ioSession != null)
			ioSession.write(data);
		else
			Util698.log(MinaSerialServer.class.getName(), "sendMessage 错误 ：ioSession为空！", Debug.LOG_INFO);
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
	}

	public String getComID() {
		return comID;
	}

	public void setComID(String comID) {
		this.comID = comID;
	}


}
