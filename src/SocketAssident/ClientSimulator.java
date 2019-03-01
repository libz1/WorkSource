package SocketAssident;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

public class ClientSimulator {

	private volatile static ClientSimulator uniqueInstance;

	static Boolean isSelfPlay = false;

	private static String IP_ADDR = "127.0.0.1";// 服务器地址
	private static int PORT = 12345;// 服务器端口号
	static InputStream IN = null;
	static OutputStream OS = null;
	// static String SDATA =
	// "68313168010106000000098061020600000009806203060000000980630406000000000000050600000000000006060000000000002D16
	// static String SDATA =
	// "68313168010106201801130013020620180113001403062018011300150406000000000000050600000000000006060000000000008C16";

	// static String SDATA =
	// "683D3D6801011830333030315A433030303030303231373030363538363636021830333030315A43303030303030323137303036353836363703000400050006007116";

	// 98061`98064
	static String SDATA = "68252568010106303938303631020630393830363203063039383036330406303938303634050006002E16";

	// 记录累计模拟操作的次数
	int num_opt = 0;

	public static String getIP_ADDR() {
		return IP_ADDR;
	}

	public static int getPORT() {
		return PORT;
	}

	public static ClientSimulator getInstance() {
		if (uniqueInstance == null) {
			synchronized (ClientSimulator.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new ClientSimulator();
				}
			}
		}
		return uniqueInstance;
	}

	private ClientSimulator() {
		new Thread(() -> init()).start();
	}

	private String readData(InputStream is, String type) throws Exception {
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
		Util698.log(ClientSimulator.class.getName(), type + " recv=>" + recvData, Debug.LOG_INFO);

		return recvData;
	}

	public void sendData(OutputStream os, String sData, String type) {
		byte[] byteData = new byte[sData.length() / 2];
		// 将16进制字符串转为Byte数组
		byteData = DataConvert.hexString2ByteArray(sData);

		try {
			Util698.log(ClientSimulator.class.getName(), type + " send=>" + sData, Debug.LOG_INFO);
			os.write(byteData);
			os.flush();
		} catch (Exception e) {
			String errMsg = e.getMessage();

			Util698.log(ClientSimulator.class.getName(), "发送异常 ERR:" + errMsg, Debug.LOG_INFO);
			if (connect() != null) {
				os = OS;
				// 如果连接成功就发送一次
				try {

					Util698.log(ClientSimulator.class.getName(), type + " send (再次)=>" + sData, Debug.LOG_INFO);
					os.write(byteData);
					os.flush();
				} catch (Exception e1) {
					e1.printStackTrace();
					String errMsg1 = e1.getMessage();
					Util698.log(ClientSimulator.class.getName(), "when sendData2:" + errMsg, Debug.LOG_INFO);

					if (errMsg1.indexOf("closed") >= 0) {
						Util698.log(ClientSimulator.class.getName(), "多次尝试，无法连接！", Debug.LOG_INFO);
					}
				}

			}
			// }
		}
	}

	public static Socket connect() {
		Socket socket = null;
		try {
			Util698.log(ClientSimulator.class.getName(), "Client " + " " + IP_ADDR + ":" + PORT, Debug.LOG_INFO);
			socket = new Socket(IP_ADDR, PORT);
			// xuky 2018.05.04 在赋予新值前，关闭对象
			Util698.log(ClientSimulator.class.getName(), "Client connect 在赋予新值前，关闭对象OS IN", Debug.LOG_INFO);
			if (OS != null)
				OS.close();
			if (IN != null)
				IN.close();
			OS = socket.getOutputStream();
			IN = socket.getInputStream();
			Util698.log(ClientSimulator.class.getName(), "Client connect ok！ IN对象:" + IN, Debug.LOG_INFO);

		} catch (Exception e) {
			// e.printStackTrace();
			String errMsg = e.getMessage();

			Util698.log(ClientSimulator.class.getName(), "connect err:" + errMsg, Debug.LOG_INFO);
			// 间隔2秒后，进行重拾
			Debug.sleep(2000);
			socket = connect();
		}
		return socket;
	}

	private void init() {
		Socket socket = null;
		try {
			PORT = SoftParameter.getInstance().getPrefix_port_new();
			IP_ADDR = SoftParameter.getInstance().getPrefix_ip();

			socket = connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 发送启动报文
		sendData(OS, SDATA, "Client");
		Util698.log(ClientSimulator.class.getName(), "Client 发出启动报文", Debug.LOG_INFO);
		//
		try {
			while (true) {
				try {
					// 按照byte流的模式读取数据

					Util698.log(ClientSimulator.class.getName(), "Client readData 等待接收数据 IN对象:" + IN, Debug.LOG_INFO);

					String msg = readData(IN, "Client");

					Util698.log(ClientSimulator.class.getName(), "Client readData 接收数据 :" + msg, Debug.LOG_INFO);

					if (msg.substring(0, 10).equals("6801016881")) {
						Util698.log(ClientSimulator.class.getName(), "Client 收到启动报文的回复", Debug.LOG_INFO);
					}

					if (msg.substring(6, 10).equals("6803")) {
						Util698.log(ClientSimulator.class.getName(), "Client 收到结果1", Debug.LOG_INFO);
						sendData(OS, "68010168835516", "Client");
						Util698.log(ClientSimulator.class.getName(), "Client 发出对结果1的回复", Debug.LOG_INFO);
					}
					if (msg.substring(6, 10).equals("6802")) {
						Util698.log(ClientSimulator.class.getName(), "Client 收到结果2", Debug.LOG_INFO);
						sendData(OS, "68010168825416", "Client");
						// 显示执行进度信息
						num_opt++;
						new Thread(() -> {
							Object[] s = { "PLCSimulator", DataConvert.int2String(num_opt), "" };
							PublisherUI.getInstance().publish(s);
							Util698.log(ClientSimulator.class.getName(), "测试数量："+num_opt, Debug.LOG_INFO);
						}).start();

						Util698.log(ClientSimulator.class.getName(), "Client 发出对结果2的回复", Debug.LOG_INFO);
						Debug.sleep(1000);
						sendData(OS, SDATA, "Client");
						Util698.log(ClientSimulator.class.getName(), "Client 发出启动报文", Debug.LOG_INFO);
					}
				} catch (Exception e) {
					if (IN != null)
						IN.close();
					Util698.log(ClientSimulator.class.getName(), "Client readData出现异常" + e.getMessage(),
							Debug.LOG_INFO);
					// Util698.log(ClientSimulator.class.getName(), "Client
					// readData等待一会，会有其他程序进行重新连接的", Debug.LOG_INFO);
					connect();
					// xuky 2018.05.04 还是进行输出的好，便于日志分析，但是延长等待时间
					Debug.sleep(10000);
				}
				Debug.sleep(300);
			}
		} catch (Exception ex) {
			Util698.log(ClientSimulator.class.getName(), "Client read 跳出了循环", Debug.LOG_INFO);
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

	public void sendData() {
		sendData(OS, SDATA, "Client");
	}

	public static void main(String[] args) {
		ClientSimulator.getInstance();
	}

}
