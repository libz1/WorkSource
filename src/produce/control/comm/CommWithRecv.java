package produce.control.comm;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.Lock;

import org.apache.mina.core.future.ConnectFuture;
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
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import entity.SerialParam;
import mina.ByteArrayCodecFactory;
import produce.control.entity.BaseCommLog;
import produce.control.entity.BaseCommLogDaoImpl;
import produce.control.simulation.PlatFormParam;
import produce.control.simulation.QuickObjects;
import produce.entity.ProduceLog;
import produce.entity.ProduceLogDaoImpl;
import util.Frame645Control;
import util.PublisherFrame;
import util.SoftParameter;
import util.SoftVarSingleton;
import util.Util698;

// ���������ͻ��ˡ�serial
public class CommWithRecv {
	Boolean is_Debug = false;
	IoSession ioSession = null;
	// private String RecvData = "";
	CommParam CommParam;
	String com_name = "", IP = "", frameID = "", nullFlag = "noData";
	int port = 0;
	IoConnector connector = null;
	ConnectFuture future = null;
	String futureErr = "";
	ServerHandlerByte handlerByte = null;
	String meter = "", meter_addr = "";  //  ��λ��Ϣ

	public CommWithRecv() {

	}

	public CommWithRecv(String meter, int port, String meter_addr) {
		String str = "00"+meter;
		this.meter = str.substring(str.length()-2);
		this.port = port;
		this.meter_addr = meter_addr;
	}

	// ���캯���н�������
	public CommWithRecv(CommParam CommParam) {
		this.CommParam = CommParam;
		connect();
	}

	// �Ͽ�����ͨ������ͨ���б��г���
	private void disConnect() {
		try {
//			if (!CommParam.getType().equals("1")){
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
					Util698.log(CommWithRecv.class.getName(),
							frameID + " connector disconnected. isDisposed=" + connector.isDisposed(), Debug.LOG_INFO);

				// Ȼ���ٽ��б����ͷ�
				ioSession = null;
				future = null;
				connector = null;
				handlerByte = null;
//			}


		} catch (Exception e) {
			Util698.log(CommWithRecv.class.getName(), frameID + " disConnect Exception:" + e.getMessage(),
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
			 Util698.log(ServerHandlerByte.class.getName(), frameID + "partRecv:"+str,Debug.LOG_INFO);

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

				// xuky 2019.02.28 ���ڲ��رգ����ִ�е�Ч��
//				future = QuickObjects.getInstance().getSerials().get(com_name);
//				if (future != null){
//					int times = QuickObjects.getInstance().getSerialsUsetimes().get(com_name);
//					times ++;
//					QuickObjects.getInstance().getSerialsUsetimes().put(com_name,times);
//				}
//				else{
					IP = com_name;
					port = serialParam.getBaudRate();
					frameID = IP + ":" + DataConvert.int2String(port);

					Parity parity = null;
					if (serialParam.getParity().indexOf("0") >= 0||serialParam.getParity().indexOf("NONE") >= 0)
						parity = Parity.NONE;
					if (serialParam.getParity().indexOf("1") >= 0||serialParam.getParity().indexOf("ODD") >= 0)
						parity = Parity.ODD;
					if (serialParam.getParity().indexOf("2") >= 0||serialParam.getParity().indexOf("EVEN") >= 0)
						parity = Parity.EVEN;
					if (serialParam.getParity().indexOf("3") >= 0||serialParam.getParity().indexOf("MARK") >= 0)
						parity = Parity.MARK;
					if (serialParam.getParity().indexOf("4") >= 0||serialParam.getParity().indexOf("SPACE") >= 0)
						parity = Parity.SPACE;

					SerialAddress address = new SerialAddress(serialParam.getCOMM(), serialParam.getBaudRate(),
							DataBits.DATABITS_8, StopBits.BITS_1, parity, FlowControl.NONE);

					connector = new SerialConnector();
					connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
					connector.setHandler(handlerByte);
					// connector.setConnectTimeoutMillis(1000);
					Util698.log(CommWithRecv.class.getName(), "��ʼ����"+frameID, Debug.LOG_INFO);
					future = connector.connect(address);
//					QuickObjects.getInstance().getSerials().put(com_name,future);
//					QuickObjects.getInstance().getSerialsUsetimes().put(com_name,1);
//				}
			}
			if (CommParam.getType().equals("2")) {
				RJ45Param RJ45Param = CommParam.getRJ45Param();
				IP = RJ45Param.getIP();
				port = RJ45Param.getPort();
				InetSocketAddress address = new InetSocketAddress(IP, port);
				frameID = IP + ":" + DataConvert.int2String(port);

				connector = new NioSocketConnector();
				connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
				connector.setHandler(handlerByte);
				connector.setConnectTimeoutMillis(500);
				Util698.log(CommWithRecv.class.getName(), "��ʼ����"+frameID, Debug.LOG_INFO);
				future = connector.connect(address);
			}

			if (future == null) {
				Util698.log(CommWithRecv.class.getName(), "δ��������future����", Debug.LOG_INFO);
				return;
			}
			future.await();
			if (future.getException() != null) {
				futureErr = "NG:" + future.getException().getMessage();
				if (CommParam.getType().equals("1"))
					Util698.log(CommWithRecv.class.getName(), frameID + " ���ڱ�ռ�ã�",Debug.LOG_INFO);
				else
					Util698.log(CommWithRecv.class.getName(), frameID + " �޷����ӣ�",Debug.LOG_INFO);
			} else {
				// xuky 2017.08.11 ������Ҫ�ж�future.getException()Ȼ���ٽ��������getSession
				ioSession = future.getSession();
					Util698.log(CommWithRecv.class.getName(), "����"+frameID + " �ɹ���",Debug.LOG_INFO);
			}
		} catch (Exception e) {
			Util698.log(CommWithRecv.class.getName(), frameID + " connect Exception:" + e.getMessage(), Debug.LOG_INFO);
		}
	}

	// �г�ʱ�ȴ�ʱ��ķ��ͺ���
	public String sendMessage(String data, int timeOut) {
		return sendMessage(data, timeOut, 1, null);
	}

	public String sendMessage(String data, int timeOut, int sendTimes) {
		return sendMessage(data, timeOut, sendTimes, null);
	}

	// �г�ʱ�ȴ�ʱ�䡢�����Դ����ĵķ��ͺ�������¼���͹���
	public String sendMessage(String data, int timeOut, int sendTimes, BaseCommLog baseCommLog) {
		String expect = "";
		if (baseCommLog != null) {
			baseCommLog.setSend(data);
			baseCommLog.setRetrys(sendTimes);
			baseCommLog.setWaittime(timeOut);
			baseCommLog.setCommparm(frameID);
			expect = baseCommLog.getExpect();
		}
		if (future == null) {
			String err = "NG:δ��������future�����޷�������Ϣ";
			baseCommLog.setResult(err);
			Util698.log(CommWithRecv.class.getName(), err, Debug.LOG_INFO);
			return nullFlag;
		}
		data = data.replaceAll(" ", "");
		// ��¼����ʱ��
		String beginTime = "";
		Long diff = (long) 0;
		SoftParameter.getInstance().getCompleteDataMap().put(frameID, nullFlag);
		// Util698.log(SerialWithRecv.class.getName(), frameID + "
		// SendData:"+data,Debug.LOG_INFO);
		int num = 1;
		while (num <= sendTimes) {
			Util698.log(CommWithRecv.class.getName(), frameID + " nowTimes:" + num + " allTimes:" + sendTimes,
					Debug.LOG_INFO);
			// ��������Դ��������Ͷ��ִ�� ��beginTime��diff���в�����ʼ��
			beginTime = Util698.getDateTimeSSS_new();
			diff = (long) 0;
			if (baseCommLog != null) {
				if (num == 1) // ֻ���״η���ʱ��д��˷���ʱ����Ϣ
					baseCommLog.setSendtime(beginTime);
				baseCommLog.setSendtimes(baseCommLog.getSendtimes() + 1);
			}
			Util698.log(CommWithRecv.class.getName(), frameID + " SendData:" + data, Debug.LOG_INFO);
			try {
				if (ioSession == null){
					Util698.log(CommWithRecv.class.getName(), frameID + " ioSessionΪ�գ�ִ�����Ӳ���", Debug.LOG_INFO);
					connect();
				}
				if (ioSession != null) {
					if (is_Debug)
						Util698.log(CommWithRecv.class.getName(),
								frameID + " ioSession.write:" + data.substring(data.length() - 4, data.length()),
								Debug.LOG_INFO);
					ioSession.write(Util698.String2ByteArray(data));
				} else {
					String err = "NG:ioSessionΪ�գ��޷�������Ϣ";
					if (!futureErr.equals(""))
						err = futureErr;
					baseCommLog.setResult(err);
					Util698.log(CommWithRecv.class.getName(), err, Debug.LOG_INFO);
				}
			} catch (Exception e) {
				Util698.log(CommWithRecv.class.getName(), frameID + " SendData Error:" + e.getMessage(),
						Debug.LOG_INFO);
			}
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
					baseCommLog.setResult("OK");
					break; // ����յ����ݾ��˳�
				} else {
					// ���expect���ݲ�Ϊ�գ���ʾ��Ҫ�Խ��յ����ݽ����ж�
					String is_ok = Util698.verify(RecvData, expect,baseCommLog);
					if (is_ok.equals("�ɹ�"))
						is_ok = "OK";
					else
						is_ok = "NG";
					baseCommLog.setResult(is_ok);
					break;
				}
			}
			// ���Դ�������
			num++;
		}
		String recv = SoftParameter.getInstance().getCompleteDataMap().get(frameID);

		if (recv == nullFlag){
			recv = "";
			// xuky 2019.02.16 ���ڹ㲥���ͣ��޻�Ӧ�����
			if (expect.equals("")){
				baseCommLog.setResult("OK");
			}
		}
		baseCommLog.setRecv(recv);
		baseCommLog.setRecvtime(Util698.getDateTimeSSS_new());
		disConnect();
		return recv;
	}

	public BaseCommLog deal_one(String name, String param, String sData, String expect ) {
		return deal_one(name,param,sData,expect,3000);
	}

	// xuky 2019.01.24 ��ռʽͨ�ţ�ֱ����ʱ�����߸��ݷ��ؽ���ж��Ƿ���ȷ
	public BaseCommLog deal_one(String name, String param, String sData, String expect,int waittime ) {
        Lock test_Lock = null;
		if (name.indexOf("INFRA") >= 0)
			test_Lock = SoftVarSingleton.getInstance().getInfraTest_Lock();
		if (name.indexOf("·��") >= 0 || name.indexOf("RT") >= 0)
			test_Lock = SoftVarSingleton.getInstance().getRTTest_Lock();
		if (param.indexOf("COM") >= 0)
			test_Lock = SoftVarSingleton.getInstance().getPlatformTest_Lock();

		if (test_Lock != null){
			test_Lock.lock();
	        try {
				return deal_oneWithLock(name, param, sData, expect, waittime );
	        } finally {
	        	test_Lock.unlock();
	        }
		}
		else{
			return deal_oneWithLock(name, param, sData, expect, waittime );
		}
	}
	public BaseCommLog deal_oneWithLock(String name, String param, String sData, String expect,int waittime ) {
		// xuky 2019.02.28 Ϊ�����Ч�ʣ����ͷţ�����ʹ��
		CommWithRecv minaSocketClient = null;
//		if (param.indexOf("COM") >= 0){
//			minaSocketClient = QuickObjects.getInstance().getSerials().get(param);
//			if (minaSocketClient == null){
//				minaSocketClient = new CommWithRecv(new CommParam(param));
//				QuickObjects.getInstance().getSerials().put(param,minaSocketClient);
//				QuickObjects.getInstance().getSerialsUsetimes().put(param,0);
//			}
//			QuickObjects.getInstance().getSerialsUsetimes().put(param,QuickObjects.getInstance().getSerialsUsetimes().get(param)+1);
//		}
//		else
			minaSocketClient = new CommWithRecv(new CommParam(param));

		BaseCommLog baseCommLog = new BaseCommLog();
		baseCommLog.setName(name);
		baseCommLog.setExpect(expect);

		new Thread(() -> {
			String[] s = { "send frame", "user data", sData, param};
			PublisherFrame.getInstance().publish(s);
		}).start();

//		new Thread(() -> {
//		}).start();


		minaSocketClient.sendMessage(sData, waittime, 1, baseCommLog);
		String result = baseCommLog.getResult();

		new Thread(() -> {
			String[] s1 = { "recv frame", "user data", baseCommLog.getRecv(), param};
			PublisherFrame.getInstance().publish(s1);
			String[] s2 = { "result", "user data", name+"���Խ��:"+baseCommLog.getResult(), param};
			PublisherFrame.getInstance().publish(s2);
		}).start();


		if (result.equals("OK"))
			Util698.log(CommWithRecv.class.getName(), baseCommLog.getName() + "���Խ�� result=" + baseCommLog.getResult(),
					Debug.LOG_INFO);
		else {
			Util698.log(CommWithRecv.class.getName(), baseCommLog.getName() + "���Խ�� result=" + baseCommLog.getResult(),
					Debug.LOG_INFO);
			Util698.log(CommWithRecv.class.getName(), baseCommLog.getName() + "���Խ�� expect=" + baseCommLog.getExpect(),
					Debug.LOG_INFO);
			Util698.log(CommWithRecv.class.getName(), baseCommLog.getName() + "���Խ��      recv=" + baseCommLog.getRecv(),
					Debug.LOG_INFO);
		}

		baseCommLog.setRunID(PlatFormParam.getInstance().getRUNID());
		IBaseDao<BaseCommLog> iBaseDao_BaseCommLog = new BaseCommLogDaoImpl();
		BaseCommLog baseCommLog_new = iBaseDao_BaseCommLog.create(baseCommLog);

		return baseCommLog_new;

	}

	private void ChanleExample() {

		String name = "�ŵ����", expect = "",meter = "192.168.127.96:10012";
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");

		// ����Ϊ���ּ�ⱨ��
		// 1����֯���ͱ��� ��ʱ����Ϣ
//		frame645.setData_item("04 96 96 05");
//		// 2���˿ڼ���ñ���
//		frame645.setData_item("04 96 96 02");
//		frame645.setData_data("01 02 03 04 05 06 07 08 09 0A 0B");
		// 3��Һ��״̬���
//		frame645.setData_item("04 96 96 12");
//		// 4����֯���ͱ��� дʱ����Ϣ
//		frame645.setData_item("04 96 96 04");
//		// yyyy-MM-dd HH:mm:ss:SSS
//		String time = Util698.getDateTimeSSS_new();
//		time = time.substring(0,time.length()-3);
//		time = time.substring(2);
//		time = time.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
//		frame645.setData_data(time);
////		// 5��ң��״̬��ȡ
		frame645.setData_item("04 96 96 03");
//		// ��ȡǰ����ģ���ն�Ӧ�ûظ�������
//		SLTerminal.getInstance().getMeterFS().put("/"+meter, "3F");
//	// 6����ѹ
//	frame645.setData_item("04 96 96 07");
//	// ��ȡǰ����ģ���ն�Ӧ�ûظ�������
//	SLTerminal.getInstance().getMeterDC().put("/"+meter, "0488");
//	// 7������
//	frame645.setData_item("04 96 96 08");
//	// ��ȡǰ����ģ���ն�Ӧ�ûظ�������
//	SLTerminal.getInstance().getMeterAC().put("/"+meter, "1918");
//		// 8���������
//		frame645.setData_item("04 96 96 0A");

		String sData = frame645.get645Frame();
		System.out.println("��ʼ����...");
		expect = "68************68940A0496960519**************16";
		 deal_one(name+"-4851","COM3:2400",sData,expect);
//		deal_one(name+"-485",meter,sData,expect);
	}

	private void FCCheck(String control) {
		String name = "ң�ż��", expect = "";
		// 1����֯���ͱ��� ��ʱ����Ϣ
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 03");
		String sData = frame645.get645Frame();
		System.out.println("��ʼ����...");
		// 680000C0A87F606894050496960300E316
		String data = control; // "00000011"; // 2·
		data = DataConvert.binStr2HexString(control, 2);
		expect = "68************68940504969603" + data + "**16";
		// 202��485-1�˿�
		deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
	}

	private void DCExample() {
		String name = "ֱ��ģ����-��ѹ���", expect = "";
		// 1����֯���ͱ��� ��ʱ����Ϣ
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 07");
		String sData = frame645.get645Frame();
		System.out.println("��ʼ����...");
		// 680000C0A87F60689406049696070000E816
		String data = "0488"; // 2·
		expect = "68************68940604969607" + data + "**16";
		// deal_one(name+"-PS2","COM48:9600",sData,expect);
		deal_one(name + "-485", "129.1.22.201:10012", sData, expect);
	}

	private void CURRExample() {
		String name = "ֱ��ģ����-�������", expect = "";
		// 1����֯���ͱ��� ��ʱ����Ϣ
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 08");
		String sData = frame645.get645Frame();
		System.out.println("��ʼ����...");
		// 680000C0A87F60689406049696070000E816
		String data = "1881"; // 2·
		expect = "68************68940604969608" + data + "**16";
		deal_one(name + "-485", "129.1.22.202:10012", sData, expect);
	}

	private void RTAddMeter() {
		String name = "·�ɼӱ���", expect = "";
		// 1����֯���ͱ��� ��ʱ����Ϣ
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 09");
		// String meter_addr = "000000000006";
		String meter_addr = "370116400384";
		frame645.setData_data(meter_addr + "00 00 00 00 00 00");
		String sData = frame645.get645Frame();
		System.out.println("��ʼ����...");
		// 680000C0A87F60689406049696070000E816
		expect = "68************689400**16";
		deal_one(name + "-PS2", "129.1.22.201:10012", sData, expect);
	}

	private void RTExample() {
		String name = "·�ɼ��", expect = "";
		// 1����֯���ͱ��� ��ʱ����Ϣ
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 11");
		String sData = frame645.get645Frame();
		System.out.println("��ʼ����...");
		// 680000C0A87F60689406049696070000E816
		expect = "68************6894050496961100**16";
		deal_one(name + "-PS2", "129.1.22.201:10012", sData, expect);
	}

	// ͨ��dll������̨���ң��
	private Object[] FCSet_DLL(String control) {
		Object[] ret = {false,""};
		ExampleTCPClient exampleTCPClient = new ExampleTCPClient();
		// ����ң�ſ��Ʒ�ʽΪ�̽�
		String result = "";
		String flag = "0";
		result = exampleTCPClient.SendData("[SetFCAddVolt]meter="+meter+";port="+port+";Flag="+flag+";");
		if (result.equals("NG")){
			ret[1] = "SetFCAddVolt";
			return ret;
		}
		// ��ѯң�ſ��Ʒ�ʽΪ ��= 0
		result = exampleTCPClient.SendData("[GetFCAddVolt]meter="+meter+";port="+port+";");
		if (!result.equals(flag)){
			ret[1] = "GetFCAddVolt:"+result +" expext:"+flag;
			return ret;
		}
		result = exampleTCPClient.SendData("[SetFSState]meter="+meter+";port="+port+";Flag="+control+";");
		if (result.equals("NG")){
			ret[1] = "SetFSState";
			return ret;
		}
		// ��ѯң��״̬ ��= 11111000
		exampleTCPClient.SendData("[GetFSState]meter="+meter+";port="+port+";");
		if (!result.equals(control)){
			ret[1] = "GetFSState:"+result +" expext:"+control;
			return ret;
		}
		ret[0] = true;
		return ret;
	}

	// ͨ��RS232�� ��̨��ֱ�ӷ��ͱ���
	private Object[] FCSet_RS232(String control) {
		String result = "", sData= "",expect = "", name = "";
		Object[] ret = {false,""};
		FramePlatform framePlatform = new FramePlatform();


		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("A2");
		framePlatform.setDATA("00");
		sData = framePlatform.getFrame();

		framePlatform.setCONTROL("B3");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();

		sData = sData + "";



		ExampleTCPClient exampleTCPClient = new ExampleTCPClient();
		// ����ң�ſ��Ʒ�ʽΪ��
		String flag = "0";
//		22��ң�Ź���ѡ�� ------��2013-01-07���� ��--2015-05-26 ׷��
//		����:01H+��ַ(A��Z) +����+B4H(����) +(30H/�̽�  31H/��ӵ�ѹ  32/�������)+У��λ+����(17H)
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("B4");
		framePlatform.setDATA("30"); //30H/�̽�
		sData = framePlatform.getFrame();
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��)+У��λ+����(17H)
		framePlatform.setCONTROL("06");
		framePlatform.setDATA("");
		expect = framePlatform.getFrame();
		name = "����ң�ſ��Ʒ�ʽΪ�̽�";
//		result = deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
//		if (result.equals("NG")){
//			ret[1] = name;
//			return ret;
//		}
//		23��ң��ѡ����� ------��2013-01-07���� ��-- 2015-05-26 ׷��
//		����:01H+��ַ(A��Z) +����+B5H(����) +У��λ+����(17H)
		framePlatform.setCONTROL("B5");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();
//		   �ӻ�:01H+��ַ(A��Z +����)+ 30H(�̽�)  /31H(��ӵ�ѹ  32/�������)+У��λ+����(17H)
		framePlatform.setCONTROL("30");
		framePlatform.setDATA("");
		expect = framePlatform.getFrame();
		name = "��ѯң�ſ��Ʒ�ʽ";
//		result = deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
//		if (!result.equals(flag)){
//			ret[1] = name+result +" expext:"+flag;
//			return ret;
//		}


//		8��	����̨��ң��״̬
//		����:01H+��ַ(A��Z) +����+A2H(����)+ ң��״̬��(1�ֽ�)+У��λ+����(17H)
//		��˵������ң��״̬�֡�Ϊ1�ֽڣ�8λ����16���Ƶ��ַ�����bit0-bit7��Ӧң��	1-ң��8��bitx=1��ң�������
		control = DataConvert.binStr2HexString(control, 2);
		framePlatform.setCONTROL("A2");
		framePlatform.setDATA(control);
		sData = framePlatform.getFrame();
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��)+У��λ+����(17H)
		framePlatform.setCONTROL("06");
		framePlatform.setDATA("");
		expect = framePlatform.getFrame();
		name = "����̨��ң��״̬";
//		result = deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
//		if (result.equals("NG")){
//			ret[1] = name;
//			return ret;
//		}
//		7��
//		����:01H+��ַ(A��Z) +����+A1H(����) +У��λ+����(17H)
//		   �ӻ�:01H+��ַ(A��Z +����)+ ң��״̬��(1�ֽ�)+У��λ+����(17H)
//		��˵������ң��״̬�֡�Ϊ1�ֽڣ�8λ����16���Ƶ��ַ�����bit0-bit7��Ӧң��	1-ң��8��bitx=1��ң�������
//		ע���ڶ̽ӷ�ʽ��0X30������ӵ�Դ��ʽ��0X31��ʱ������״̬����
//		    �����巽ʽʱ�������巢�ͽ��� ����0X00��δ���� ���� 0Xff;--201505
		framePlatform.setCONTROL("A1");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��)+У��λ+����(17H)
		framePlatform.setCONTROL(control);
		expect = framePlatform.getFrame();
		name = "�õ�̨��ң��״̬";
//		result = deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
//		if (!result.equals(control)){
//			ret[1] = name+result +" expext:"+control;
//			return ret;
//		}
//		ret[0] = true;
		return ret;
	}

	public static void main(String[] args) {
//		SLTerminal.getInstance();

		ExampleTCPClient exampleTCPClient = new ExampleTCPClient();

		// ��ȡ���ñ�׼����Ϣ�ʹ�����Ϣ��������ϢҪ������������
		int port = 0;
//		port = DataConvert.String2Int(exampleTCPClient.getUsableInfo()[1]);
//		CommWithRecv commWithRecv = new CommWithRecv("12",port,"129.1.22.96");
		CommWithRecv commWithRecv = new CommWithRecv("12",port,"192.168.127.96");
//		CommWithRecv commWithRecv = new CommWithRecv();

//		 commWithRecv.ChanleExample();

//		// ̨����ƺ���м��
		String control = "00111111"; // 2·  1��2
		Object[] result = null;
//		result = commWithRecv.FCSet_DLL(DataConvert.reverseString(control));
		result = commWithRecv.FCSet_RS232(control);
//		System.out.println("FCSet_DLL-"+control +" result="+result[0]+" "+result[1]);
//		commWithRecv.FCCheck(control);

//		control = "00000000";
//		commWithRecv.FCSet_DLL("");
//		commWithRecv.FCCheck(control);

//		commWithRecv.DCExample();
		// commWithRecv.CURRExample();
		// �ұ�󣬼��·��
		// new CommWithRecv().RTAddMeter();
		// ��Ҫ��һ������ʱ���ȴ�·�ɳ���
		// new CommWithRecv().RTExample();
	}

}
