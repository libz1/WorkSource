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

// 服务器、客户端、serial
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
	String meter = "", meter_addr = "";  //  表位信息

	public CommWithRecv() {

	}

	public CommWithRecv(String meter, int port, String meter_addr) {
		String str = "00"+meter;
		this.meter = str.substring(str.length()-2);
		this.port = port;
		this.meter_addr = meter_addr;
	}

	// 构造函数中进行连接
	public CommWithRecv(CommParam CommParam) {
		this.CommParam = CommParam;
		connect();
	}

	// 断开串口通道，从通道列表中撤出
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
				// 先执行此日志输出
				if (is_Debug)
					Util698.log(CommWithRecv.class.getName(),
							frameID + " connector disconnected. isDisposed=" + connector.isDisposed(), Debug.LOG_INFO);

				// 然后再进行变量释放
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

				// xuky 2019.02.28 串口不关闭，提高执行的效率
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
					Util698.log(CommWithRecv.class.getName(), "开始连接"+frameID, Debug.LOG_INFO);
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
				Util698.log(CommWithRecv.class.getName(), "开始连接"+frameID, Debug.LOG_INFO);
				future = connector.connect(address);
			}

			if (future == null) {
				Util698.log(CommWithRecv.class.getName(), "未正常创建future对象", Debug.LOG_INFO);
				return;
			}
			future.await();
			if (future.getException() != null) {
				futureErr = "NG:" + future.getException().getMessage();
				if (CommParam.getType().equals("1"))
					Util698.log(CommWithRecv.class.getName(), frameID + " 串口被占用！",Debug.LOG_INFO);
				else
					Util698.log(CommWithRecv.class.getName(), frameID + " 无法连接！",Debug.LOG_INFO);
			} else {
				// xuky 2017.08.11 首先需要判断future.getException()然后再进行这里的getSession
				ioSession = future.getSession();
					Util698.log(CommWithRecv.class.getName(), "连接"+frameID + " 成功！",Debug.LOG_INFO);
			}
		} catch (Exception e) {
			Util698.log(CommWithRecv.class.getName(), frameID + " connect Exception:" + e.getMessage(), Debug.LOG_INFO);
		}
	}

	// 有超时等待时间的发送函数
	public String sendMessage(String data, int timeOut) {
		return sendMessage(data, timeOut, 1, null);
	}

	public String sendMessage(String data, int timeOut, int sendTimes) {
		return sendMessage(data, timeOut, sendTimes, null);
	}

	// 有超时等待时间、有重试次数的的发送函数，记录发送过程
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
			String err = "NG:未正常创建future对象，无法发送消息";
			baseCommLog.setResult(err);
			Util698.log(CommWithRecv.class.getName(), err, Debug.LOG_INFO);
			return nullFlag;
		}
		data = data.replaceAll(" ", "");
		// 记录启动时间
		String beginTime = "";
		Long diff = (long) 0;
		SoftParameter.getInstance().getCompleteDataMap().put(frameID, nullFlag);
		// Util698.log(SerialWithRecv.class.getName(), frameID + "
		// SendData:"+data,Debug.LOG_INFO);
		int num = 1;
		while (num <= sendTimes) {
			Util698.log(CommWithRecv.class.getName(), frameID + " nowTimes:" + num + " allTimes:" + sendTimes,
					Debug.LOG_INFO);
			// 如果有重试次数参数就多次执行 对beginTime和diff进行参数初始化
			beginTime = Util698.getDateTimeSSS_new();
			diff = (long) 0;
			if (baseCommLog != null) {
				if (num == 1) // 只在首次发送时，写入此发送时间信息
					baseCommLog.setSendtime(beginTime);
				baseCommLog.setSendtimes(baseCommLog.getSendtimes() + 1);
			}
			Util698.log(CommWithRecv.class.getName(), frameID + " SendData:" + data, Debug.LOG_INFO);
			try {
				if (ioSession == null){
					Util698.log(CommWithRecv.class.getName(), frameID + " ioSession为空，执行连接操作", Debug.LOG_INFO);
					connect();
				}
				if (ioSession != null) {
					if (is_Debug)
						Util698.log(CommWithRecv.class.getName(),
								frameID + " ioSession.write:" + data.substring(data.length() - 4, data.length()),
								Debug.LOG_INFO);
					ioSession.write(Util698.String2ByteArray(data));
				} else {
					String err = "NG:ioSession为空，无法发送消息";
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
					break; // 如果收到数据就退出
				} else {
					// 如果expect数据不为空，表示需要对接收的数据进行判断
					String is_ok = Util698.verify(RecvData, expect,baseCommLog);
					if (is_ok.equals("成功"))
						is_ok = "OK";
					else
						is_ok = "NG";
					baseCommLog.setResult(is_ok);
					break;
				}
			}
			// 重试次数增加
			num++;
		}
		String recv = SoftParameter.getInstance().getCompleteDataMap().get(frameID);

		if (recv == nullFlag){
			recv = "";
			// xuky 2019.02.16 存在广播发送，无回应的情况
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

	// xuky 2019.01.24 独占式通信，直到超时，或者根据返回结果判断是否正确
	public BaseCommLog deal_one(String name, String param, String sData, String expect,int waittime ) {
        Lock test_Lock = null;
		if (name.indexOf("INFRA") >= 0)
			test_Lock = SoftVarSingleton.getInstance().getInfraTest_Lock();
		if (name.indexOf("路由") >= 0 || name.indexOf("RT") >= 0)
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
		// xuky 2019.02.28 为了提高效率，不释放，继续使用
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
			String[] s2 = { "result", "user data", name+"测试结果:"+baseCommLog.getResult(), param};
			PublisherFrame.getInstance().publish(s2);
		}).start();


		if (result.equals("OK"))
			Util698.log(CommWithRecv.class.getName(), baseCommLog.getName() + "测试结果 result=" + baseCommLog.getResult(),
					Debug.LOG_INFO);
		else {
			Util698.log(CommWithRecv.class.getName(), baseCommLog.getName() + "测试结果 result=" + baseCommLog.getResult(),
					Debug.LOG_INFO);
			Util698.log(CommWithRecv.class.getName(), baseCommLog.getName() + "测试结果 expect=" + baseCommLog.getExpect(),
					Debug.LOG_INFO);
			Util698.log(CommWithRecv.class.getName(), baseCommLog.getName() + "测试结果      recv=" + baseCommLog.getRecv(),
					Debug.LOG_INFO);
		}

		baseCommLog.setRunID(PlatFormParam.getInstance().getRUNID());
		IBaseDao<BaseCommLog> iBaseDao_BaseCommLog = new BaseCommLogDaoImpl();
		BaseCommLog baseCommLog_new = iBaseDao_BaseCommLog.create(baseCommLog);

		return baseCommLog_new;

	}

	private void ChanleExample() {

		String name = "信道检测", expect = "",meter = "192.168.127.96:10012";
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");

		// 以下为各种检测报文
		// 1、组织发送报文 读时间信息
//		frame645.setData_item("04 96 96 05");
//		// 2、端口检测用报文
//		frame645.setData_item("04 96 96 02");
//		frame645.setData_data("01 02 03 04 05 06 07 08 09 0A 0B");
		// 3、液晶状态检测
//		frame645.setData_item("04 96 96 12");
//		// 4、组织发送报文 写时间信息
//		frame645.setData_item("04 96 96 04");
//		// yyyy-MM-dd HH:mm:ss:SSS
//		String time = Util698.getDateTimeSSS_new();
//		time = time.substring(0,time.length()-3);
//		time = time.substring(2);
//		time = time.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
//		frame645.setData_data(time);
////		// 5、遥信状态读取
		frame645.setData_item("04 96 96 03");
//		// 读取前设置模拟终端应该回复的数据
//		SLTerminal.getInstance().getMeterFS().put("/"+meter, "3F");
//	// 6、电压
//	frame645.setData_item("04 96 96 07");
//	// 读取前设置模拟终端应该回复的数据
//	SLTerminal.getInstance().getMeterDC().put("/"+meter, "0488");
//	// 7、电流
//	frame645.setData_item("04 96 96 08");
//	// 读取前设置模拟终端应该回复的数据
//	SLTerminal.getInstance().getMeterAC().put("/"+meter, "1918");
//		// 8、按键检测
//		frame645.setData_item("04 96 96 0A");

		String sData = frame645.get645Frame();
		System.out.println("开始测试...");
		expect = "68************68940A0496960519**************16";
		 deal_one(name+"-4851","COM3:2400",sData,expect);
//		deal_one(name+"-485",meter,sData,expect);
	}

	private void FCCheck(String control) {
		String name = "遥信检测", expect = "";
		// 1、组织发送报文 读时间信息
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 03");
		String sData = frame645.get645Frame();
		System.out.println("开始测试...");
		// 680000C0A87F606894050496960300E316
		String data = control; // "00000011"; // 2路
		data = DataConvert.binStr2HexString(control, 2);
		expect = "68************68940504969603" + data + "**16";
		// 202是485-1端口
		deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
	}

	private void DCExample() {
		String name = "直流模拟量-电压检测", expect = "";
		// 1、组织发送报文 读时间信息
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 07");
		String sData = frame645.get645Frame();
		System.out.println("开始测试...");
		// 680000C0A87F60689406049696070000E816
		String data = "0488"; // 2路
		expect = "68************68940604969607" + data + "**16";
		// deal_one(name+"-PS2","COM48:9600",sData,expect);
		deal_one(name + "-485", "129.1.22.201:10012", sData, expect);
	}

	private void CURRExample() {
		String name = "直流模拟量-电流检测", expect = "";
		// 1、组织发送报文 读时间信息
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 08");
		String sData = frame645.get645Frame();
		System.out.println("开始测试...");
		// 680000C0A87F60689406049696070000E816
		String data = "1881"; // 2路
		expect = "68************68940604969608" + data + "**16";
		deal_one(name + "-485", "129.1.22.202:10012", sData, expect);
	}

	private void RTAddMeter() {
		String name = "路由加表检测", expect = "";
		// 1、组织发送报文 读时间信息
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 09");
		// String meter_addr = "000000000006";
		String meter_addr = "370116400384";
		frame645.setData_data(meter_addr + "00 00 00 00 00 00");
		String sData = frame645.get645Frame();
		System.out.println("开始测试...");
		// 680000C0A87F60689406049696070000E816
		expect = "68************689400**16";
		deal_one(name + "-PS2", "129.1.22.201:10012", sData, expect);
	}

	private void RTExample() {
		String name = "路由检测", expect = "";
		// 1、组织发送报文 读时间信息
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(meter_addr));
		frame645.setControl("14");
		frame645.setData_item("04 96 96 11");
		String sData = frame645.get645Frame();
		System.out.println("开始测试...");
		// 680000C0A87F60689406049696070000E816
		expect = "68************6894050496961100**16";
		deal_one(name + "-PS2", "129.1.22.201:10012", sData, expect);
	}

	// 通过dll，控制台体的遥信
	private Object[] FCSet_DLL(String control) {
		Object[] ret = {false,""};
		ExampleTCPClient exampleTCPClient = new ExampleTCPClient();
		// 设置遥信控制方式为短接
		String result = "";
		String flag = "0";
		result = exampleTCPClient.SendData("[SetFCAddVolt]meter="+meter+";port="+port+";Flag="+flag+";");
		if (result.equals("NG")){
			ret[1] = "SetFCAddVolt";
			return ret;
		}
		// 查询遥信控制方式为 ？= 0
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
		// 查询遥信状态 ？= 11111000
		exampleTCPClient.SendData("[GetFSState]meter="+meter+";port="+port+";");
		if (!result.equals(control)){
			ret[1] = "GetFSState:"+result +" expext:"+control;
			return ret;
		}
		ret[0] = true;
		return ret;
	}

	// 通过RS232， 向台体直接发送报文
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
		// 设置遥信控制方式为短
		String flag = "0";
//		22、遥信功能选择 ------（2013-01-07增加 ）--2015-05-26 追加
//		主机:01H+地址(A—Z) +长度+B4H(命令) +(30H/短接  31H/外加电压  32/脉冲输出)+校验位+结束(17H)
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("B4");
		framePlatform.setDATA("30"); //30H/短接
		sData = framePlatform.getFrame();
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定)+校验位+结束(17H)
		framePlatform.setCONTROL("06");
		framePlatform.setDATA("");
		expect = framePlatform.getFrame();
		name = "设置遥信控制方式为短接";
//		result = deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
//		if (result.equals("NG")){
//			ret[1] = name;
//			return ret;
//		}
//		23、遥信选择回送 ------（2013-01-07增加 ）-- 2015-05-26 追加
//		主机:01H+地址(A—Z) +长度+B5H(命令) +校验位+结束(17H)
		framePlatform.setCONTROL("B5");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();
//		   从机:01H+地址(A—Z +长度)+ 30H(短接)  /31H(外加电压  32/脉冲输出)+校验位+结束(17H)
		framePlatform.setCONTROL("30");
		framePlatform.setDATA("");
		expect = framePlatform.getFrame();
		name = "查询遥信控制方式";
//		result = deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
//		if (!result.equals(flag)){
//			ret[1] = name+result +" expext:"+flag;
//			return ret;
//		}


//		8、	设置台体遥信状态
//		主机:01H+地址(A—Z) +长度+A2H(命令)+ 遥信状态字(1字节)+校验位+结束(17H)
//		【说明：“遥信状态字”为1字节（8位）的16进制的字符串，bit0-bit7对应遥信	1-遥信8；bitx=1：遥信输出】
		control = DataConvert.binStr2HexString(control, 2);
		framePlatform.setCONTROL("A2");
		framePlatform.setDATA(control);
		sData = framePlatform.getFrame();
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定)+校验位+结束(17H)
		framePlatform.setCONTROL("06");
		framePlatform.setDATA("");
		expect = framePlatform.getFrame();
		name = "设置台体遥信状态";
//		result = deal_one(name + "-485", "129.1.22.202:100"+meter, sData, expect);
//		if (result.equals("NG")){
//			ret[1] = name;
//			return ret;
//		}
//		7、
//		主机:01H+地址(A—Z) +长度+A1H(命令) +校验位+结束(17H)
//		   从机:01H+地址(A—Z +长度)+ 遥信状态字(1字节)+校验位+结束(17H)
//		【说明：“遥信状态字”为1字节（8位）的16进制的字符串，bit0-bit7对应遥信	1-遥信8；bitx=1：遥信输出】
//		注：在短接方式（0X30）、外加电源方式（0X31）时，回送状态量；
//		    在脉冲方式时，若脉冲发送结束 回送0X00，未结束 发送 0Xff;--201505
		framePlatform.setCONTROL("A1");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定)+校验位+结束(17H)
		framePlatform.setCONTROL(control);
		expect = framePlatform.getFrame();
		name = "得到台体遥信状态";
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

		// 获取可用标准表信息和串口信息，串口信息要做其他控制用
		int port = 0;
//		port = DataConvert.String2Int(exampleTCPClient.getUsableInfo()[1]);
//		CommWithRecv commWithRecv = new CommWithRecv("12",port,"129.1.22.96");
		CommWithRecv commWithRecv = new CommWithRecv("12",port,"192.168.127.96");
//		CommWithRecv commWithRecv = new CommWithRecv();

//		 commWithRecv.ChanleExample();

//		// 台体控制后进行检测
		String control = "00111111"; // 2路  1和2
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
		// 挂表后，检测路由
		// new CommWithRecv().RTAddMeter();
		// 需要有一定的延时，等待路由抄表
		// new CommWithRecv().RTExample();
	}

}
