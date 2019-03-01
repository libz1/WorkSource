package SocketAssident;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

public class Client {

	private volatile static Client uniqueInstance;

	static Boolean isSelfPlay = false;

	private static String IP_ADDR = "127.0.0.1";// 服务器地址
	private static int PORT = 20001;// 服务器端口号
	static int retry = 100;
	static int connectInterval = 1000 * 2;  // 连接重试间隔时间为2秒
	static int sendInterval = 1000 * 90;  // 重发间隔时间为60秒
	static int connectTime = 0;
	static int connect_retry = 100;
	static int Send_time1 = 0;
	static InputStream IN = null;
	static OutputStream OS = null;
	static String SDATA = "68313168010106201801130013020620180113001403062018011300150406000000000000050600000000000006060000000000008C16";

	static String start_frame = "68313168010106201801130013020620180113001403062018011300150406000000000000050600000000000006060000000000008C16";

	public static String getIP_ADDR() {
		return IP_ADDR;
	}
	public static int getPORT() {
		return PORT;
	}
	public static int getRetry() {
		return retry;
	}
	public static int getSend_time1() {
		return Send_time1;
	}
	public static String getSDATA() {
		return SDATA;
	}
	public static void setSDATA(String sDATA) {
		SDATA = sDATA;
	}


	public static Client getInstance() {
		if (uniqueInstance == null) {
			synchronized (Client.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new Client();
				}
			}
		}
		return uniqueInstance;
	}

	private Client(){
		// 默认的Send_time1数量与retry相等
		Send_time1 = retry;
		new Thread(() -> {
			init();
		}).start();
	}

//	public static byte[] hexString2ByteArray(String param) {
//		param = param.replaceAll(" ", "");
//		byte[] result = new byte[param.length() / 2];
//		for (int i = 0, j = 0; j < param.length(); i++) {
//			result[i] = (byte) Integer.parseInt(param.substring(j, j + 2), 16);
//			j += 2;
//		}
//		return result;
//	}
//
//	public static String bytes2HexString(byte[] b) {
//		String ret = "";
//		for (int i = 0; i < b.length; i++) {
//			String hex = Integer.toHexString(b[i] & 0xFF);
//			if (hex.length() == 1) {
//				hex = "0" + hex;
//			}
//			ret += hex;
//		}
//		ret = ret.toUpperCase();
//		return ret;
//	}
//
//	public static void sleep(int millonsec) {
//		try {
//			Thread.sleep(millonsec);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	private String readData(InputStream is,String type) throws Exception {
		// 按照byte流的模式读取数据
		byte[] receiveByte = new byte[4096];
		int messageLength = is.read(receiveByte);
		byte[] currReceiveByte = new byte[messageLength];
		for (int i = 0; i < messageLength; i++)
			currReceiveByte[i] = receiveByte[i];

		String recvData = "";
		if (currReceiveByte != null)
			// byte转为字符串
			recvData = DataConvert.bytes2HexString(currReceiveByte);
		Util698.log(Client.class.getName(), type+" recv=>" + recvData, Debug.LOG_INFO);

		// 通信地址 通信内容
		// String msg = "addr@" + devAddr + ";" + "msg@" + recvData;

		// 收到数据添加到RecvData单例对象中
		// Util698.log(SocketServer.class.getName(), "RecvData.push msg:"+msg,
		// Debug.LOG_INFO);
		// RecvData.getInstance().push(msg);
		return recvData;
	}

	public void sendData(OutputStream os,String sData,String type){
		byte[] byteData = new byte[sData.length() / 2];
		// 将16进制字符串转为Byte数组
		byteData = DataConvert.hexString2ByteArray(sData);

		try {
			Util698.log(Client.class.getName(), type+ " send=>" + sData, Debug.LOG_INFO);
			os.write(byteData);
			os.flush();
		} catch (Exception e) {
			String errMsg = e.getMessage();

			Object[] s = { "LinkRemain", "Client", "connect-" + connectTime+" 客户端异常"+errMsg };
			PublisherUI.getInstance().publish(s);

			Util698.log(Client.class.getName(), "发送异常 ERR:"+errMsg, Debug.LOG_INFO);
//			if (errMsg == null || errMsg.indexOf("closed") >= 0 || errMsg.indexOf("abort") >= 0){
				connectTime = 0;
				if (!type.equals("Client"))
					return;
				if (connect()!=null){
					os = OS;
					// 如果连接成功就发送一次
					try {

						Util698.log(Client.class.getName(), type+ " send (再次)=>" + sData, Debug.LOG_INFO);
						os.write(byteData);
						os.flush();
					}
					catch (Exception e1) {
						e1.printStackTrace();
						String errMsg1 = e1.getMessage();
						Util698.log(Client.class.getName(), "when sendData2:"+errMsg, Debug.LOG_INFO);

						Object[] s1 = { "LinkRemain", "Client", "connect-" + connectTime+"-客户端异常"+errMsg };
						PublisherUI.getInstance().publish(s1);

						if (errMsg1.indexOf("closed") >= 0){
							Util698.log(Client.class.getName(), "多次尝试，无法连接！", Debug.LOG_INFO);
						}
					}

				}
//			}
		}
	}

	public static Socket connect(){
		if (connectTime > connect_retry)
			return null;

		Socket socket = null;
		try {
			Util698.log(Client.class.getName(), "Client connect-" + connectTime +" "+IP_ADDR+":"+PORT, Debug.LOG_INFO);
			socket = new Socket(IP_ADDR, PORT);
			// xuky 2018.05.04 在赋予新值前，关闭对象
			Util698.log(Client.class.getName(), "Client connect 在赋予新值前，关闭对象OS IN", Debug.LOG_INFO);
			if (OS != null)
				OS.close();
			if (IN != null)
				IN.close();
			OS = socket.getOutputStream();
			IN = socket.getInputStream();
			Util698.log(Client.class.getName(), "Client connect ok！ IN对象:"+IN, Debug.LOG_INFO);

			Object[] s = { "LinkRemain", "Client", "启动成功" };
			PublisherUI.getInstance().publish(s);

			// 如果连接成功，累计尝试次数清空
			connectTime = 0;

		} catch (Exception e) {
//			e.printStackTrace();
			String errMsg = e.getMessage();

			Object[] s = { "LinkRemain", "Client", "connect-" + connectTime+" 客户端异常"+errMsg };
			Util698.log(Client.class.getName(), "connect err:"+errMsg +"publish:"+s, Debug.LOG_INFO);
			PublisherUI.getInstance().publish(s);

			Debug.sleep(connectInterval);
			if (errMsg.indexOf("refused") >= 0){
				connectTime ++;
				socket = connect();
			}
		}
		return socket;
	}

//	public static void main(String[] args) {
	private void init() {
		Socket socket = null;
		try {
			// 如果用户在参数中进行了设定，则使用设定的值，否则使用系统的默认数值
			PORT = SoftParameter.getInstance().getPrefix_port();
			IP_ADDR = SoftParameter.getInstance().getPrefix_ip();
			if (IP_ADDR.equals(""))
				IP_ADDR = "127.0.0.1";

			connectTime = 0;
			socket = connect();
			// 1、发出第一个启动测试的报文
//			Send_time1 = 0;
//			sendBegin(os);
			connectTime = connectTime + 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		try {
			while (true) {
				try {
					// 按照byte流的模式读取数据

					Util698.log(Client.class.getName(), "Client readData 等待接收数据 IN对象:"+IN , Debug.LOG_INFO);

					String msg = readData(IN,"Client");
					Util698.log(Client.class.getName(), "Client readData:"+msg , Debug.LOG_INFO);

					if (msg.substring(0, 10).equals("6801016881")){
						// 如果收到了回复报文，就无需重复发送启动报文
						// xuky 2018.05.05 注释如下代码，只有收到了最后的报文，才无需重新发送
//						Send_time1 = retry+1;
//						Util698.log(Client.class.getName(), "Client 收到启动报文回复数据，停止重发启动报文", Debug.LOG_INFO);

						if (!isSelfPlay)
							Server.getInstance().sendData(msg);
					}

					if (msg.substring(6, 10).equals("6803")){
						// 如果收到了结果报文，就无需重复发送启动报文
						// xuky 2018.05.05 注释如下代码，只有收到了最后的报文，才无需重新发送
//						Send_time1 = retry+1;
//						Util698.log(Client.class.getName(), "Client 收到测试结果1，停止重发启动报文", Debug.LOG_INFO);

						if (isSelfPlay)
							sendData(OS,"68010168835516","Client");
						else
							Server.getInstance().sendData(msg);
					}
					if (msg.substring(6, 10).equals("6802")){
						// 如果收到了结果报文，就无需重复发送启动报文
						Send_time1 = retry+1;
						Util698.log(Client.class.getName(), "Client 收到最终测试结果，停止重发启动报文，等待下次测试...", Debug.LOG_INFO);

						Object[] s = { "LinkRemain", "Client", "停止重发启动报文，等待下次测试..." };
						PublisherUI.getInstance().publish(s);

						if (isSelfPlay)
							sendData(OS,"68010168825416","Client");
						else
							Server.getInstance().sendData(msg);


//						sleep(2000);
//
//						// 2、开始发送新的启动测试报文
//						SDATA = SDATA;
//
//						Send_time1 = 0;
//						sendBegin(os);
					}
				} catch (Exception e) {
					if (IN != null)
						IN.close();
//					Util698.log(Client.class.getName(), "Client 重新连接", Debug.LOG_INFO);

					// xuky 2018.07.09 需要进行重新连接
					Debug.sleep(500);
					socket = connect();

					// xuky 2018.05.04 无需进行如下信息的输出，如果测试软件关闭，这里会出现大量的提示信息
//					Util698.log(Client.class.getName(), "Client readData出现异常"+e.getMessage(), Debug.LOG_INFO);
//					Util698.log(Client.class.getName(), "Client readData等待一会，会有其他程序进行重新连接的", Debug.LOG_INFO);
//					// xuky 2018.05.04 还是进行输出的好，便于日志分析，但是延长等待时间
//					Debug.sleep(10000);
				}
				Debug.sleep(300);
			}
		} catch (Exception ex) {
			Util698.log(Client.class.getName(), "Client read 跳出了循环", Debug.LOG_INFO);

			ex.printStackTrace();

		} finally {
			try {
				if (IN != null)
					IN.close();
				if (socket != null)
					socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void sendBegin(OutputStream os) {
		new Thread(() -> {
			Util698.log(Client.class.getName(), "开启sendBegin线程", Debug.LOG_INFO);
			String sendTime = "";
			while (true) {
				if (Send_time1 == retry){
					Util698.log(Client.class.getName(), "Client sendBegin 达到重试上限，不再重发", Debug.LOG_INFO);
					Util698.log(Client.class.getName(), "结束sendBegin线程", Debug.LOG_INFO);
					break;
				}
				if (Send_time1 > retry){
					Util698.log(Client.class.getName(), "Client sendBegin Send_time1 > retry，不再重发", Debug.LOG_INFO);
					Util698.log(Client.class.getName(), "结束sendBegin线程", Debug.LOG_INFO);
					// 间隔一定时间
					// xuky 2018.04.28 以下代码为模拟自动测试代码，取消不要执行
//					Send_time1 = 0;
//					sleep(connectInterval);
//					// 开始进入等待状态，等待上报测试结果
//					sendBegin(os);
					break;
				}

				if (sendTime == "" || Util698.getMilliSecondBetween_new(DateTimeFun.getDateTimeSSS(),sendTime) > sendInterval ){
					sendTime = DateTimeFun.getDateTimeSSS();
					Util698.log(Client.class.getName(), "Client sendBegin 重发启动报文", Debug.LOG_INFO);

					// 需要重发启动报文
					sendData(os,start_frame,"Client");
					Send_time1++;

					Object[] s = { "LinkRemain", "Client", "重发启动报文-"+Send_time1 };
					PublisherUI.getInstance().publish(s);

				}
				Debug.sleep(100); //
			}
		}).start();
	}

	public void sendData() {
		// 如果收到的是启动测试报文，就需要进行多次重试，即执行sendBegin(OS) 代码
		if (SDATA.substring(0, 10).equals("6831316801")){
			Send_time1 = 0;
			// 将启动报文进行保存，便于后续进行重发操作
			start_frame = SDATA;
			sendBegin(OS);
		}
		else
			sendData(OS,SDATA,"Client");
	}


}
