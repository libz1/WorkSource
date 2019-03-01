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

// ���������ͻ��ˡ�serial
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

	// ���캯���н�������
	public CommServer(CommParam CommParam) {
		this.CommParam = CommParam;
		connect();
	}

	// �Ͽ�����ͨ������ͨ���б��г���
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

			// ��ִ�д���־���
			if (is_Debug)
				Util698.log(CommServer.class.getName(),
						frameID + " connector disconnected. isDisposed=" + connector.isDisposed(), Debug.LOG_INFO);

			// Ȼ���ٽ��б����ͷ�
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

			// xuky 2018.02.01 ����ƴװ��������
			String old = SoftParameter.getInstance().getRecvDataMap().get(frameID);
			if (old == null)
				old = "";
			String recvData = old + str;
			if (!Util698.isCompleteFrame(recvData, frameID))
				return;
			// �����־��������
			SoftParameter.getInstance().getRecvDataMap().put(frameID, "");
			// ��¼�յ�����������
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
				Util698.log(CommServer.class.getName(), "����"+IP+":"+port+"[����]����������ɣ�", Debug.LOG_INFO);
				return;
			}

			if (future == null) {
				Util698.log(CommServer.class.getName(), "δ��������future����", Debug.LOG_INFO);
				return;
			}
			future.await();
			if (future.getException() != null) {
				futureErr = "NG:" + future.getException().getMessage();
				if (futureErr.indexOf("Unknown Application") >= 0)
					Util698.log(CommServer.class.getName(), frameID + " future getException: ���ڱ���������ռ�ã� " + futureErr,
							Debug.LOG_INFO);
				else
				Util698.log(CommServer.class.getName(), frameID + " future getException:" + futureErr,
						Debug.LOG_INFO);
			} else {
				// xuky 2017.08.11 ������Ҫ�ж�future.getException()Ȼ���ٽ��������getSession
				ioSession = future.getSession();
				Util698.log(CommServer.class.getName(), "����"+IP+":"+port+"[����]����������ɣ�", Debug.LOG_INFO);
//					Util698.log(CommServer.class.getName(), frameID + " future = connector.connected. ",
//							Debug.LOG_INFO);
			}
		} catch (Exception e) {
			Util698.log(CommServer.class.getName(), frameID + " connect Exception:" + e.getMessage(), Debug.LOG_INFO);
		}
	}

	// ֻ�Ƿ��ͺ���
	public String sendMessage(String data) {
		return sendMessage(data, (long) 0, 1, null, false);
	}

	// �г�ʱ�ȴ�ʱ��ķ��ͺ���
	public String sendMessage(String data, Long timeOut) {
		return sendMessage(data, timeOut, 1, null, true);
	}

	public String sendMessage(String data, Long timeOut, int sendTimes) {
		return sendMessage(data, timeOut, sendTimes, null, true);
	}

	// �г�ʱ�ȴ�ʱ�䡢�����Դ����ĵķ��ͺ�������¼���͹���
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
//			String err = "NG:δ��������future�����޷�������Ϣ";
//			baseCommLog.setResult(err);
//			Util698.log(CommServer.class.getName(), err, Debug.LOG_INFO);
//			return nullFlag;
//		}
		data = data.replaceAll(" ", "");
		// ��¼����ʱ��
		String beginTime = "";
		Long diff = (long) 0;
		SoftParameter.getInstance().getCompleteDataMap().put(frameID, nullFlag);
		// Util698.log(SerialWithRecv.class.getName(), frameID + "
		// SendData:"+data,Debug.LOG_INFO);
		int num = 1;
		while (num <= sendTimes) {
			Util698.log(CommServer.class.getName(), frameID + " nowTimes:" + num + " allTimes:" + sendTimes,
					Debug.LOG_INFO);
			// ��������Դ��������Ͷ��ִ�� ��beginTime��diff���в�����ʼ��
			beginTime = Util698.getDateTimeSSS_new();
			diff = (long) 0;
			if (baseCommLog != null) {
				if (num == 1) // ֻ���״η���ʱ��д��˷���ʱ����Ϣ
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
					String err = "NG:ioSessionΪ�գ��޷�������Ϣ";
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

			// xuky 2019.01.09 �������ȴ��ظ������ͼ���
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
					break; // ����յ����ݾ��˳�
				} else {
					// ���expect���ݲ�Ϊ�գ���ʾ��Ҫ�Խ��յ����ݽ����ж�
					String is_ok = Util698.verify(RecvData, expect);
					if (is_ok.equals("�ɹ�"))
						is_ok = "OK";
					else
						is_ok = "NG";
					if (baseCommLog != null)
						baseCommLog.setResult(is_ok);
					break;
				}
			}
			// ���Դ�������
			num++;
		}
		String recv = SoftParameter.getInstance().getCompleteDataMap().get(frameID);
		if (recv == nullFlag)
			recv = "";
		if (baseCommLog != null){
			baseCommLog.setRecv(recv);
			baseCommLog.setRecvtime(Util698.getDateTimeSSS_new());
		}
		// xuky 2019.01.09 ������ϣ������˳����ȴ��´εķ���
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
			Util698.log(CommServer.class.getName(), baseCommLog.getName() + "���Խ�� result=" + baseCommLog.getResult(),
					Debug.LOG_INFO);
		else {
			Util698.log(CommServer.class.getName(), baseCommLog.getName() + "���Խ�� result=" + baseCommLog.getResult(),
					Debug.LOG_INFO);
			Util698.log(CommServer.class.getName(), baseCommLog.getName() + "���Խ�� expect=" + baseCommLog.getExpect(),
					Debug.LOG_INFO);
			Util698.log(CommServer.class.getName(), baseCommLog.getName() + "���Խ��      recv=" + baseCommLog.getRecv(),
					Debug.LOG_INFO);
		}
		return result;

	}

	// xuky 2019.01.09  �����յ��ı��Ľ������ݻظ������һ������ʱ����
	private void deal_recvData(String recvData, String addr_str) {
		// �յ����Ʊ�̨��Դ��Դ��������лظ�
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
//		serialParam.setParity("��NONE(0)");
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
