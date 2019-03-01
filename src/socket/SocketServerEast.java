package socket;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import javafx.application.Platform;
import javafx.base.javafxutil;
import mina.MinaSerialServer;
import util.Publisher;
import util.SoftParameter;
import util.Util698;
import frame.Frame698;

// 在PrefixMain.start()函数中被激活的，处于invoke状态，随时接收数据
public class SocketServerEast {
	private static Socket CLIENT_SEND= null;

	public static void invoke(Socket client) throws IOException {
		// 开启新的线程进行相关处理
		new Thread(() -> dealNewConnect(client)).start();
	}

	public static void sendSocketData(String sendData){
		sendData(CLIENT_SEND, sendData);
	};

	public static void dealNewConnect(Socket client) {
		// 向连接端列表插入数据
		ChannelList.getInstance().add(client);
		CLIENT_SEND = client;

		String[] s = { "refresh terminal list", "", "" };
		Publisher.getInstance().publish(s);

		String devAddr = client.getRemoteSocketAddress().toString();
		Util698.log(SocketServerEast.class.getName(), "处理新的socket接入 and setRECVCLINET" +devAddr,Debug.LOG_INFO);

		SoftParameter.getInstance().setRECVCLINET("168");
		SoftParameter.getInstance().saveParam();

		InputStream in = null;
		try {
			in = client.getInputStream();
			// 循环，接收数据，始终不退出
			while (true) {
				try {
					// 按照byte流的模式读取数据
					String msg = readData(in, devAddr);
//					System.out.println("SocketServer 2018.04.12 recv=>" + msg);
				} catch (Exception e) {
					// xuky 2016.08.10 如果接收数据出现错误，就退出
//					System.out.println("SocketServer invoke=> 退出线程");
					Util698.log(SocketServerEast.class.getName(), "dealNewConnect Exception e1:"+e.getMessage(), Debug.LOG_INFO);
					// xuky 2017.05.08 还需要进行后续处理，删除界面的中的数据等
					ChannelList.getInstance().remove(client);
					// 清除通信对象列表
					ChannelObjs.getInstance().reMove(devAddr);
					// 通过观察者发布在线终端变化消息，用于刷新界面数据
					String[] s1 = { "refresh terminal list", "", "" };
					Publisher.getInstance().publish(s1);
					break;
				}

				// sleep 方法允许较低优先级的线程获得运行机会，但yield（）方法执行时，当前线程仍处在可运行状态
				// 所以不可能让出较低优先级的线程此时获取CPU占有权。
				// 在一个运行系统中，如果较高优先级的线程没有调用sleep方法，也没有受到I/O阻塞，
				// 那么较低优先级线程只能等待所有较高优先级的线程运行结束，方可有机会运行

				// sleep()使当前线程进入停滞状态，所以执行sleep()的线程在指定的时间内肯定不会执行；
				// yield()只是使当前线程重新回到可执行状态，所以执行yield()的线程有可能在进入到可执行状态后马上又被执行

				// sleep()可使优先级低的线程得到执行的机会，当然也可以让同优先级和高优先级的线程有执行的机会；
				// yield()只能使同优先级的线程有执行的机会
				Debug.sleep(100);
			}
		} catch (Exception e) {
			Util698.log(SocketServerEast.class.getName(), "dealNewConnect Exception e2:"+e.getMessage(), Debug.LOG_INFO);
		} finally {
			try {
				in.close();
				client.close();
			} catch (Exception e) {
				Util698.log(SocketServerEast.class.getName(), "dealNewConnect finally Exception e:"+e.getMessage(), Debug.LOG_INFO);
			}
		}
	}

	public static String readData(InputStream is, String devAddr) throws Exception {
//		try{
			// 按照byte流的模式读取数据
			byte[] receiveByte = new byte[4096];
			int messageLength = is.read(receiveByte);
			// System.out.println("readData is.read:"+messageLength);
			// xuky 2017.05.08 处理连接端断开的情况
			// 还是沿用原先的throws Exception
			// if (messageLength == -1){
			// return "";
			// }
			byte[] currReceiveByte = new byte[messageLength];
			for (int i = 0; i < messageLength; i++)
				currReceiveByte[i] = receiveByte[i];

			String recvData = "";
			if (currReceiveByte != null)
				// byte转为字符串
				recvData = DataConvert.bytes2HexString(currReceiveByte);

			// 通信地址 通信内容
			String msg = "addr@" + devAddr + ";" + "msg@" + recvData;

			// 收到数据添加到RecvData单例对象中
//			Util698.log(SocketServer.class.getName(), "RecvData.push msg:"+msg,
//			Debug.LOG_INFO);
			RecvData.getInstance().push(msg);
			return msg;
//		}
//		catch (Exception e){
//			Util698.log(SocketServerEast.class.getName(), "readData Exception:"+e.getMessage(),Debug.LOG_INFO);
//		}
	}

	// xuky 2017.06.21 向指定端口发送报文数据
	public static Boolean sendData(String sendData, String port) {
		sendData = sendData.replaceAll(" ", "");
		Util698.log(SocketServerEast.class.getName(), port + "发:"+sendData,Debug.LOG_INFO);

		if (port.equals("")){
			Util698.log(SocketServerEast.class.getName(), port + "为空，无法发送",Debug.LOG_INFO);
			return false;
		}
		// 根据port信息，找到对应的发送实体对象
		Object object = ChannelObjsByLogiAddr.getInstance().get(port);

//		Object os = null;
		String frameType = "user data";
		if (object == null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					// AlertBox.display("操作提醒", "请检查是否打开了通信服务器");
					String msg = "请检查是否打开了通信服务器 port="+port;
					javafxutil.f_alert_informationDialog("操作提醒", msg);
				}
			});
			return false;
		}

		if (object instanceof MinaSerialServer) {
//			os = object;
			sendToOs(port, sendData, frameType, object);

			 // 2、向信息中心发布消息:发送了某种类型的某个报文
			 String[] s = { "send frame", frameType, sendData };
			 Publisher.getInstance().publish(s);

		}
		return true;
	}

	// 根据报文中的终端地址自动找到对应的socket对象，尚未进行异常处理
	public static void sendData(String sendData) {

		// 1、对发送的报文进行解析，得到报文类型信息(链路报文、用户报文)
		Frame698 frame689 = new Frame698(sendData);
		int choiseFlag = frame689.getAPDU().getChoiseFlag();

		String frameType = "user data";
		if (choiseFlag == 1 || choiseFlag == 129)
			frameType = "link data";

		// 3、得到报文的发送目标设备地址
		String sadata = frame689.getFrameAddr().getSAData();

		Object os = null;
		try {
			// 4、根据设备地址找对应的物理通信对象，可能是网口也可能是串口
			Object object = ChannelObjsByLogiAddr.getInstance().get(sadata);

			Socket client = null;
			String obj_addr = "";

			if (object instanceof Socket) {
				client = (Socket) object;
				obj_addr = client.getRemoteSocketAddress().toString();
				os = client.getOutputStream();
			}
			// xuky 2017.05.11 添加对于mina串口通信方式的支持
			if (object instanceof MinaSerialServer) {
				os = object;
				obj_addr = ((MinaSerialServer) object).getName();
			}

			if (os != null) {
				sendToOs(obj_addr, sendData, frameType, os);
//				 // 5、根据物理通信对象的地址信息，找通道对象
//				 Channel channel =
//				 ChannelList.getInstance().getByCode(obj_addr);
//				 if (channel != null) {
//				 // 修改通道对象的最近通信时间
//					 channel.setRecvTime(DateTimeFun.getDateTimeSSS());
//				 }
//				 // 刷新界面中的终端通信列表
//				 String[] s1 = { "refresh terminal list", "", "" };
//				 Publisher.getInstance().publish(s1);
//				 // 2、向信息中心发布消息:发送了某种类型的某个报文
//				 String[] s = { "send frame", frameType, sendData };
//				 Publisher.getInstance().publish(s);
//				System.out.println("sendData "+sendData);

				// }
				// // 6、向物理通信对象中发送数据
				// sendData(os, sendData);
			} else {
				String[] s1 = { "send frame", "设备" + sadata + "不在线" + sendData };
				Util698.log(SocketServerEast.class.getName(), "设备" + sadata + "不在线:" + sendData, Debug.LOG_INFO);
				Publisher.getInstance().publish(s1);
			}

		} catch (Exception e1) {
			Util698.log(SocketServerEast.class.getName(), "sendData String Exception " + e1.getMessage(), Debug.LOG_INFO);
		}
	}

	public static void sendToOs(String obj_addr, String sendData, String frameType, Object os) {
		// 6、向物理通信对象中发送数据
		// xuky 2018.02.02 本来在（代码段5）之后，调整到这里，且放在线程中执行，提高运行效率
		new Thread(() -> {
			sendData(os, sendData);
		}).start();

		new Thread(() -> {
			// 5、根据物理通信对象的地址信息，找通道对象
			Channel channel = ChannelList.getInstance().getByCode(obj_addr);
			if (channel != null) {
				// 修改通道对象的最近通信时间
				channel.setRecvTime(DateTimeFun.getDateTimeSSS());
				// 刷新界面中的终端通信列表
				String[] s1 = { "refresh terminal list", "", "" };
				Publisher.getInstance().publish(s1);
				// 2、向信息中心发布消息:发送了某种类型的某个报文
				String[] s = { "send frame", frameType, sendData };
//				System.out.println("sendToOs "+sendData);
//				Util698.log(SocketServer.class.getName(), "send " + frameType + ":" + sendData, Debug.LOG_INFO);
				Publisher.getInstance().publish(s);
			}

		}).start();

	}

	public static void sendData(Socket client, String sData) {
		OutputStream os = null;
		try {
			if (client == null){
				Util698.log(SocketServerEast.class.getName(), "Socket send异常，无连接  data:"+sData, Debug.LOG_INFO);
				return;
			}
			os = client.getOutputStream();
			sendData(os, sData);
		} catch (Exception e) {
			Util698.log(SocketServerEast.class.getName(), "sendData Exception e:"+e.getMessage(), Debug.LOG_INFO);
		} finally {
			try {
				// xuky 2016.08.10 关闭OutputStream会导致socket关闭，所以不执行
				// 参考 http://blog.csdn.net/justoneroad/article/details/6962567
				// os.close();
			} catch (Exception e) {
			}
		}
	}

	public static void sendData(Object os, String sData) {
		// xuky 2017.07.04 发送数据
		if (os instanceof OutputStream) {
//			Util698.log(SocketServer.class.getName(), "send： " + sData, Debug.LOG_INFO);
			sendData((OutputStream) os, sData);
		}
		if (os instanceof MinaSerialServer) {
//			Util698.log(SocketServer.class.getName(), "send： " + sData, Debug.LOG_INFO);
			sendData((MinaSerialServer) os, sData);
		}

	}

	public static void sendData(MinaSerialServer os, String sData) {
		byte[] byteData = new byte[sData.length() / 2];
		// 将16进制字符串转为Byte数组
		byteData = DataConvert.hexString2ByteArray(sData);
		// System.out.println("sendData=>" + sData);
		os.sendMessage(byteData);
	}

	public static void sendData(OutputStream os, String sData) {
		// 发送数据
		try {
			byte[] byteData = new byte[sData.length() / 2];
			// 将16进制字符串转为Byte数组
			byteData = DataConvert.hexString2ByteArray(sData);
//			System.out.println("sendData=>" + sData);
			Util698.log(SocketServerEast.class.getName(), "sendData=>" + sData, Debug.LOG_INFO);
			os.write(byteData);
			os.flush();
		} catch (IOException e) {
			Util698.log(SocketServerEast.class.getName(), "sendData OutputStream Exception e:"+e.getMessage(), Debug.LOG_INFO);
		}
	}

	public static void main(String[] args) throws IOException {

		// 监听端口为10000
		ServerSocket server = new ServerSocket(10000);
		while (true) {
			Socket socket = server.accept();
			invoke(socket);
		}
	}

}
