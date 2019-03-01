package SocketAssident;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

// 接管自动测试系统的端口连接功能，通过自身的client代码实现链路及功能的自我恢复
public class Server {

	private volatile static Server uniqueInstance;

	public static int PORT = 12345;// 监听的端口号
	private static String IP_ADDR = "127.0.0.1";// 服务器地址
//	Client ClIENT = null;
	HandlerThread handlerThread = null;
	ServerSocket serverSocket = null;



	public static int getPORT() {
		return PORT;
	}

	public static String getIP_ADDR() {
		return IP_ADDR;
	}

	public static Server getInstance() {
		if (uniqueInstance == null) {
			synchronized (Server.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new Server();
				}
			}
		}
		return uniqueInstance;
	}

	public Server() {
		Util698.log(Server.class.getName(), "服务器启动...", Debug.LOG_INFO);
		new Thread(() -> init()).start();
	}

	public void sendData(String data) {
		try {
			OutputStream os = handlerThread.socket.getOutputStream();
			Client.getInstance().sendData(os, data, "server");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void disconnect() {
		try {
			if (serverSocket != null)
				serverSocket.close();

		} catch (IOException e) {
			Util698.log(Server.class.getName(), "断开Serve ERR", Debug.LOG_INFO);
			e.printStackTrace();
		}
	}

	public void init() {
		// 端口使用_new的
		PORT = SoftParameter.getInstance().getPrefix_port_new();
		IP_ADDR = SoftParameter.getInstance().getPrefix_ip();
		if (IP_ADDR.equals(""))
			IP_ADDR = "127.0.0.1";

		// 启动客户端，建立与测试程序的连接
		new Thread(() -> Client.getInstance()).start();

		//
		try {
			Util698.log(Server.class.getName(), "开启Serve："+IP_ADDR+":"+PORT, Debug.LOG_INFO);

			InetAddress bindAddr = InetAddress.getByName(IP_ADDR);
			serverSocket = new ServerSocket(PORT, 8, bindAddr);

			Object[] s = { "LinkRemain", "Server", "启动成功" };
			Util698.log(Server.class.getName(), "成功开启Serve："+IP_ADDR+":"+PORT+" publish:"+s, Debug.LOG_INFO);
			PublisherUI.getInstance().publish(s);

			while (true) {
				// 一旦有堵塞, 则表示服务器与客户端获得了连接
				Socket client = serverSocket.accept();
				// 处理这次连接
				handlerThread = new HandlerThread(client);
			}
		} catch (Exception e) {
			Util698.log(Server.class.getName(), "服务器异常: "+e.getMessage(), Debug.LOG_INFO);
			Object[] s = { "LinkRemain", "Server", "服务器异常: "+e.getMessage() };
			PublisherUI.getInstance().publish(s);
		}
	}

	private class HandlerThread implements Runnable {
		public Socket socket;

		public HandlerThread(Socket client) {
			socket = client;
			new Thread(this).start();
		}

		public void run() {
			try {
				InputStream in = socket.getInputStream();
				while (true) {
					try {
						// 收到数据就转发给客户端
						String msg = readData(in, "Server");
						// 读取客户端数据
						Util698.log(Server.class.getName(), "发起方：Server", Debug.LOG_INFO);

						Client.getInstance().setSDATA(msg);
						Client.getInstance().sendData();
					} catch (Exception e) {
//						e.printStackTrace();
					}

				}

			} catch (Exception e) {
			}
		}
	}
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


	public static void main(String[] arg) {
		Server.getInstance();
		Server.getInstance();
	}
}