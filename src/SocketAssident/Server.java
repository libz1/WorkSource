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

// �ӹ��Զ�����ϵͳ�Ķ˿����ӹ��ܣ�ͨ�������client����ʵ����·�����ܵ����һָ�
public class Server {

	private volatile static Server uniqueInstance;

	public static int PORT = 12345;// �����Ķ˿ں�
	private static String IP_ADDR = "127.0.0.1";// ��������ַ
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
					// ˫�ؼ�����
					uniqueInstance = new Server();
				}
			}
		}
		return uniqueInstance;
	}

	public Server() {
		Util698.log(Server.class.getName(), "����������...", Debug.LOG_INFO);
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
			Util698.log(Server.class.getName(), "�Ͽ�Serve ERR", Debug.LOG_INFO);
			e.printStackTrace();
		}
	}

	public void init() {
		// �˿�ʹ��_new��
		PORT = SoftParameter.getInstance().getPrefix_port_new();
		IP_ADDR = SoftParameter.getInstance().getPrefix_ip();
		if (IP_ADDR.equals(""))
			IP_ADDR = "127.0.0.1";

		// �����ͻ��ˣ���������Գ��������
		new Thread(() -> Client.getInstance()).start();

		//
		try {
			Util698.log(Server.class.getName(), "����Serve��"+IP_ADDR+":"+PORT, Debug.LOG_INFO);

			InetAddress bindAddr = InetAddress.getByName(IP_ADDR);
			serverSocket = new ServerSocket(PORT, 8, bindAddr);

			Object[] s = { "LinkRemain", "Server", "�����ɹ�" };
			Util698.log(Server.class.getName(), "�ɹ�����Serve��"+IP_ADDR+":"+PORT+" publish:"+s, Debug.LOG_INFO);
			PublisherUI.getInstance().publish(s);

			while (true) {
				// һ���ж���, ���ʾ��������ͻ��˻��������
				Socket client = serverSocket.accept();
				// �����������
				handlerThread = new HandlerThread(client);
			}
		} catch (Exception e) {
			Util698.log(Server.class.getName(), "�������쳣: "+e.getMessage(), Debug.LOG_INFO);
			Object[] s = { "LinkRemain", "Server", "�������쳣: "+e.getMessage() };
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
						// �յ����ݾ�ת�����ͻ���
						String msg = readData(in, "Server");
						// ��ȡ�ͻ�������
						Util698.log(Server.class.getName(), "���𷽣�Server", Debug.LOG_INFO);

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
		// ����byte����ģʽ��ȡ����
		byte[] receiveByte = new byte[4096];
		int messageLength = is.read(receiveByte);
		byte[] currReceiveByte = new byte[messageLength];
		for (int i = 0; i < messageLength; i++)
			currReceiveByte[i] = receiveByte[i];

		String recvData = "";
		if (currReceiveByte != null)
			// byteתΪ�ַ���
			recvData = DataConvert.bytes2HexString(currReceiveByte);
		Util698.log(Client.class.getName(), type+" recv=>" + recvData, Debug.LOG_INFO);

		// ͨ�ŵ�ַ ͨ������
		// String msg = "addr@" + devAddr + ";" + "msg@" + recvData;

		// �յ�������ӵ�RecvData����������
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