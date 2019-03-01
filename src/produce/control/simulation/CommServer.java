package produce.control.simulation;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.serial.SerialAddress;
import org.apache.mina.transport.serial.SerialAddress.DataBits;
import org.apache.mina.transport.serial.SerialAddress.FlowControl;
import org.apache.mina.transport.serial.SerialAddress.Parity;
import org.apache.mina.transport.serial.SerialAddress.StopBits;
import org.apache.mina.transport.serial.SerialConnector;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import entity.SerialParam;
import mina.ByteArrayCodecFactory;
import produce.control.comm.CommParam;
import produce.control.comm.RJ45Param;
import produce.control.entity.BaseCommLog;
import util.SoftParameter;
import util.Util698;

// 服务器、客户端、serial
public class CommServer {
	Boolean is_Debug = false;
	IoSession ioSession = null;
	// private String RecvData = "";
	CommParam CommParam;
	String com_name = "", IP = "", frameID = "", nullFlag = "noData";
	int port = 0;
	IoConnector connector = null;
	IoAcceptor acceptor = null;
	ConnectFuture future = null;
	String futureErr = "";
	ServerHandlerByte handlerByte = null;
	public CommServer() {

	}

	// 构造函数中进行连接
	public CommServer(CommParam CommParam) {
		this.CommParam = CommParam;
		connect();
	}

	// 断开串口通道，从通道列表中撤出
	private void disConnect() {
		try {
			if (ioSession != null) {
				ioSession.closeNow();
				connector.dispose(true);
			}
			if (connector != null) {
				future.cancel();
				connector.dispose(true);
			}

			// 先执行此日志输出
			if (is_Debug)
				Util698.log(CommServer.class.getName(),
						frameID + " connector disconnected. isDisposed=" + connector.isDisposed(), Debug.LOG_INFO);

			// 然后再进行变量释放
			ioSession = null;
			future = null;
			connector = null;
			handlerByte = null;

		} catch (Exception e) {
			Util698.log(CommServer.class.getName(), frameID + " disConnect Exception:" + e.getMessage(),
					Debug.LOG_INFO);
		}
	}

	public class ServerHandlerByte extends IoHandlerAdapter {
		@Override
		public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			// Util698.log(ServerHandlerByte.class.getName(), frameID + "
			// exceptionCaught:"+cause.getMessage(),Debug.LOG_INFO);
		}

		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
			byte[] byteStr = (byte[]) message;
			String str = DataConvert.bytes2HexString(byteStr);
//			 Util698.log(ServerHandlerByte.class.getName(), frameID + " msgRecv:"+str,Debug.LOG_INFO);

			// xuky 2018.02.01 用于拼装完整报文
			String old = SoftParameter.getInstance().getRecvDataMap().get(frameID);
			if (old == null)
				old = "";
			String recvData = old + str;
			if (!Util698.isCompleteFrame(recvData, frameID))
				return;
			// 清空日志缓存区域
			SoftParameter.getInstance().getRecvDataMap().put(frameID, "");
			// 记录收到的完整报文
			SoftParameter.getInstance().getCompleteDataMap().put(frameID, recvData);
			Util698.log(ServerHandlerByte.class.getName(), frameID + " RecvComplete:" + recvData, Debug.LOG_INFO);
			ioSession = session;
			String addr_str = ioSession.getLocalAddress().toString();
			deal_recvData(recvData,addr_str);
		}

	}


	private void connect() {
		try {
			handlerByte = new ServerHandlerByte();
			SoftParameter.getInstance().getRecvDataMap().put(frameID, "");
			SoftParameter.getInstance().getCompleteDataMap().put(frameID, nullFlag);

			if (CommParam.getType().equals("1")) {
				SerialParam serialParam = CommParam.getSerialParam();
				com_name = serialParam.getCOMM();
				IP = com_name;
				port = serialParam.getBaudRate();
				frameID = IP + ":" + DataConvert.int2String(port);

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

				SerialAddress address = new SerialAddress(serialParam.getCOMM(), serialParam.getBaudRate(),
						DataBits.DATABITS_8, StopBits.BITS_1, parity, FlowControl.NONE);

				connector = new SerialConnector();
				connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
				connector.setHandler(handlerByte);
				// connector.setConnectTimeoutMillis(1000);
				future = connector.connect(address);
			}
			if (CommParam.getType().equals("2")) {
				RJ45Param RJ45Param = CommParam.getRJ45Param();
				IP = RJ45Param.getIP();
				port = RJ45Param.getPort();
				InetSocketAddress address = new InetSocketAddress(IP, port);
				frameID = IP + ":" + DataConvert.int2String(port);

//				connector = new NioSocketConnector();
				acceptor = new NioSocketAcceptor();

				acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
				acceptor.setHandler(handlerByte);
//				future =
				acceptor.bind(address);
				Util698.log(CommServer.class.getName(), "建立"+IP+":"+port+"[网口]监听服务完成！", Debug.LOG_INFO);
				return;
			}

			if (future == null) {
				Util698.log(CommServer.class.getName(), "未正常创建future对象", Debug.LOG_INFO);
				return;
			}
			future.await();
			if (future.getException() != null) {
				futureErr = "NG:" + future.getException().getMessage();
				if (futureErr.indexOf("Unknown Application") >= 0)
					Util698.log(CommServer.class.getName(), frameID + " future getException: 串口被其他程序占用！ " + futureErr,
							Debug.LOG_INFO);
				else
				Util698.log(CommServer.class.getName(), frameID + " future getException:" + futureErr,
						Debug.LOG_INFO);
			} else {
				// xuky 2017.08.11 首先需要判断future.getException()然后再进行这里的getSession
				ioSession = future.getSession();
				Util698.log(CommServer.class.getName(), "建立"+IP+":"+port+"[串口]监听服务完成！", Debug.LOG_INFO);
//					Util698.log(CommServer.class.getName(), frameID + " future = connector.connected. ",
//							Debug.LOG_INFO);
			}
		} catch (Exception e) {
			Util698.log(CommServer.class.getName(), frameID + " connect Exception:" + e.getMessage(), Debug.LOG_INFO);
		}
	}

	// 只是发送函数
	public String sendMessage(String data) {
		return sendMessage(data, (long) 0, 1, null, false);
	}

	// 有超时等待时间的发送函数
	public String sendMessage(String data, Long timeOut) {
		return sendMessage(data, timeOut, 1, null, true);
	}

	public String sendMessage(String data, Long timeOut, int sendTimes) {
		return sendMessage(data, timeOut, sendTimes, null, true);
	}

	// 有超时等待时间、有重试次数的的发送函数，记录发送过程
	public String sendMessage(String data, Long timeOut, int sendTimes, BaseCommLog baseCommLog, Boolean needWait) {
		String expect = "";
		if (baseCommLog != null) {
			baseCommLog.setSend(data);
			baseCommLog.setRetrys(sendTimes);
			baseCommLog.setWaittime(timeOut.intValue());
			baseCommLog.setCommparm(frameID);
			expect = baseCommLog.getExpect();
		}
//		if (future == null) {
//			String err = "NG:未正常创建future对象，无法发送消息";
//			baseCommLog.setResult(err);
//			Util698.log(CommServer.class.getName(), err, Debug.LOG_INFO);
//			return nullFlag;
//		}
		data = data.replaceAll(" ", "");
		// 记录启动时间
		String beginTime = "";
		Long diff = (long) 0;
		SoftParameter.getInstance().getCompleteDataMap().put(frameID, nullFlag);
		// Util698.log(SerialWithRecv.class.getName(), frameID + "
		// SendData:"+data,Debug.LOG_INFO);
		int num = 1;
		while (num <= sendTimes) {
			Util698.log(CommServer.class.getName(), frameID + " nowTimes:" + num + " allTimes:" + sendTimes,
					Debug.LOG_INFO);
			// 如果有重试次数参数就多次执行 对beginTime和diff进行参数初始化
			beginTime = Util698.getDateTimeSSS_new();
			diff = (long) 0;
			if (baseCommLog != null) {
				if (num == 1) // 只在首次发送时，写入此发送时间信息
					baseCommLog.setSendtime(beginTime);
				baseCommLog.setSendtimes(baseCommLog.getSendtimes() + 1);
			}
			Util698.log(CommServer.class.getName(), frameID + " SendData:" + data, Debug.LOG_INFO);
			try {
				if (ioSession == null)
					connect();
				if (ioSession != null) {
					if (is_Debug)
						Util698.log(CommServer.class.getName(),
								frameID + " ioSession.write:" + data.substring(data.length() - 4, data.length()),
								Debug.LOG_INFO);
					ioSession.write(Util698.String2ByteArray(data));
				} else {
					String err = "NG:ioSession为空，无法发送消息";
					if (!futureErr.equals(""))
						err = futureErr;
					if (baseCommLog != null)
						baseCommLog.setResult(err);
					Util698.log(CommServer.class.getName(), err, Debug.LOG_INFO);
				}
			} catch (Exception e) {
				Util698.log(CommServer.class.getName(), frameID + " SendData Error:" + e.getMessage(),
						Debug.LOG_INFO);
			}

			// xuky 2019.01.09 如果无需等待回复，发送即可
			if (!needWait)
				return "OK";

			while (SoftParameter.getInstance().getCompleteDataMap().get(frameID).equals(nullFlag) && diff < timeOut) {
				// Util698.log(TCPClientWithRecv.class.getName(), frameID + "
				// RecvData:"+RecvData +" diff:"+diff +"
				// waitTime:"+timeOut,Debug.LOG_INFO);
				Debug.sleep(10);
				diff = Util698.getMilliSecondBetween_new(Util698.getDateTimeSSS_new(), beginTime);
			}
			String RecvData = SoftParameter.getInstance().getCompleteDataMap().get(frameID);
			if (!RecvData.equals(nullFlag)) {
				SoftParameter.getInstance().getCompleteDataMap().put(frameID, RecvData);
				if (expect.equals("")) {
					if (baseCommLog != null)
						baseCommLog.setResult("OK");
					break; // 如果收到数据就退出
				} else {
					// 如果expect数据不为空，表示需要对接收的数据进行判断
					String is_ok = Util698.verify(RecvData, expect);
					if (is_ok.equals("成功"))
						is_ok = "OK";
					else
						is_ok = "NG";
					if (baseCommLog != null)
						baseCommLog.setResult(is_ok);
					break;
				}
			}
			// 重试次数增加
			num++;
		}
		String recv = SoftParameter.getInstance().getCompleteDataMap().get(frameID);
		if (recv == nullFlag)
			recv = "";
		if (baseCommLog != null){
			baseCommLog.setRecv(recv);
			baseCommLog.setRecvtime(Util698.getDateTimeSSS_new());
		}
		// xuky 2019.01.09 发送完毕，不能退出，等待下次的发送
//		disConnect();
		return recv;
	}

	public String deal_one(String name, String param, String sData, String expect) {
		CommServer minaSocketClient = new CommServer(new CommParam(param));
		BaseCommLog baseCommLog = new BaseCommLog();
		baseCommLog.setName(name);
		baseCommLog.setExpect(expect);
		minaSocketClient.sendMessage(sData, (long) 5000, 1, baseCommLog, true);
		String result = baseCommLog.getResult();
		if (result.equals("OK"))
			Util698.log(CommServer.class.getName(), baseCommLog.getName() + "测试结果 result=" + baseCommLog.getResult(),
					Debug.LOG_INFO);
		else {
			Util698.log(CommServer.class.getName(), baseCommLog.getName() + "测试结果 result=" + baseCommLog.getResult(),
					Debug.LOG_INFO);
			Util698.log(CommServer.class.getName(), baseCommLog.getName() + "测试结果 expect=" + baseCommLog.getExpect(),
					Debug.LOG_INFO);
			Util698.log(CommServer.class.getName(), baseCommLog.getName() + "测试结果      recv=" + baseCommLog.getRecv(),
					Debug.LOG_INFO);
		}
		return result;

	}

	// xuky 2019.01.09  根据收到的报文进行数据回复，添加一定的延时处理
	private void deal_recvData(String recvData, String addr_str) {
		// 收到控制表台升源降源的命令，进行回复
		if (recvData.startsWith("F9F9F9F9F9") && recvData.length() == 92){
			Debug.sleep(2000);
			sendMessage(SLPlatform.getReply(recvData));
		}
		else{
			sendMessage(SLTerminal.getInstance().getReply(recvData,addr_str));
		}
	}


	public static void main(String[] args) {
		CommParam commParam = null;
//		SerialParam serialParam = new SerialParam();
//		serialParam.setCOMM("COM16");
//		serialParam.setBaudRate(9600);
//		serialParam.setParity("无NONE(0)");
//		CommParam commParam = new CommParam();
//		commParam.setType("1");
//		commParam.setSerialParam(serialParam);
//		CommServer serialServer = new CommServer(commParam);

		RJ45Param rJ45Param = new RJ45Param("192.168.127.120", 10012);
		commParam = new CommParam();
		commParam.setType("2");
		commParam.setRJ45Param(rJ45Param);
		CommServer tcpServer = new CommServer(commParam);

	}

}
