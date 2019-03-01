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

	private static String IP_ADDR = "127.0.0.1";// ��������ַ
	private static int PORT = 20001;// �������˿ں�
	static int retry = 100;
	static int connectInterval = 1000 * 2;  // �������Լ��ʱ��Ϊ2��
	static int sendInterval = 1000 * 90;  // �ط����ʱ��Ϊ60��
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
					// ˫�ؼ�����
					uniqueInstance = new Client();
				}
			}
		}
		return uniqueInstance;
	}

	private Client(){
		// Ĭ�ϵ�Send_time1������retry���
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

	public void sendData(OutputStream os,String sData,String type){
		byte[] byteData = new byte[sData.length() / 2];
		// ��16�����ַ���תΪByte����
		byteData = DataConvert.hexString2ByteArray(sData);

		try {
			Util698.log(Client.class.getName(), type+ " send=>" + sData, Debug.LOG_INFO);
			os.write(byteData);
			os.flush();
		} catch (Exception e) {
			String errMsg = e.getMessage();

			Object[] s = { "LinkRemain", "Client", "connect-" + connectTime+" �ͻ����쳣"+errMsg };
			PublisherUI.getInstance().publish(s);

			Util698.log(Client.class.getName(), "�����쳣 ERR:"+errMsg, Debug.LOG_INFO);
//			if (errMsg == null || errMsg.indexOf("closed") >= 0 || errMsg.indexOf("abort") >= 0){
				connectTime = 0;
				if (!type.equals("Client"))
					return;
				if (connect()!=null){
					os = OS;
					// ������ӳɹ��ͷ���һ��
					try {

						Util698.log(Client.class.getName(), type+ " send (�ٴ�)=>" + sData, Debug.LOG_INFO);
						os.write(byteData);
						os.flush();
					}
					catch (Exception e1) {
						e1.printStackTrace();
						String errMsg1 = e1.getMessage();
						Util698.log(Client.class.getName(), "when sendData2:"+errMsg, Debug.LOG_INFO);

						Object[] s1 = { "LinkRemain", "Client", "connect-" + connectTime+"-�ͻ����쳣"+errMsg };
						PublisherUI.getInstance().publish(s1);

						if (errMsg1.indexOf("closed") >= 0){
							Util698.log(Client.class.getName(), "��γ��ԣ��޷����ӣ�", Debug.LOG_INFO);
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
			// xuky 2018.05.04 �ڸ�����ֵǰ���رն���
			Util698.log(Client.class.getName(), "Client connect �ڸ�����ֵǰ���رն���OS IN", Debug.LOG_INFO);
			if (OS != null)
				OS.close();
			if (IN != null)
				IN.close();
			OS = socket.getOutputStream();
			IN = socket.getInputStream();
			Util698.log(Client.class.getName(), "Client connect ok�� IN����:"+IN, Debug.LOG_INFO);

			Object[] s = { "LinkRemain", "Client", "�����ɹ�" };
			PublisherUI.getInstance().publish(s);

			// ������ӳɹ����ۼƳ��Դ������
			connectTime = 0;

		} catch (Exception e) {
//			e.printStackTrace();
			String errMsg = e.getMessage();

			Object[] s = { "LinkRemain", "Client", "connect-" + connectTime+" �ͻ����쳣"+errMsg };
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
			// ����û��ڲ����н������趨����ʹ���趨��ֵ������ʹ��ϵͳ��Ĭ����ֵ
			PORT = SoftParameter.getInstance().getPrefix_port();
			IP_ADDR = SoftParameter.getInstance().getPrefix_ip();
			if (IP_ADDR.equals(""))
				IP_ADDR = "127.0.0.1";

			connectTime = 0;
			socket = connect();
			// 1��������һ���������Եı���
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
					// ����byte����ģʽ��ȡ����

					Util698.log(Client.class.getName(), "Client readData �ȴ��������� IN����:"+IN , Debug.LOG_INFO);

					String msg = readData(IN,"Client");
					Util698.log(Client.class.getName(), "Client readData:"+msg , Debug.LOG_INFO);

					if (msg.substring(0, 10).equals("6801016881")){
						// ����յ��˻ظ����ģ��������ظ�������������
						// xuky 2018.05.05 ע�����´��룬ֻ���յ������ı��ģ����������·���
//						Send_time1 = retry+1;
//						Util698.log(Client.class.getName(), "Client �յ��������Ļظ����ݣ�ֹͣ�ط���������", Debug.LOG_INFO);

						if (!isSelfPlay)
							Server.getInstance().sendData(msg);
					}

					if (msg.substring(6, 10).equals("6803")){
						// ����յ��˽�����ģ��������ظ�������������
						// xuky 2018.05.05 ע�����´��룬ֻ���յ������ı��ģ����������·���
//						Send_time1 = retry+1;
//						Util698.log(Client.class.getName(), "Client �յ����Խ��1��ֹͣ�ط���������", Debug.LOG_INFO);

						if (isSelfPlay)
							sendData(OS,"68010168835516","Client");
						else
							Server.getInstance().sendData(msg);
					}
					if (msg.substring(6, 10).equals("6802")){
						// ����յ��˽�����ģ��������ظ�������������
						Send_time1 = retry+1;
						Util698.log(Client.class.getName(), "Client �յ����ղ��Խ����ֹͣ�ط��������ģ��ȴ��´β���...", Debug.LOG_INFO);

						Object[] s = { "LinkRemain", "Client", "ֹͣ�ط��������ģ��ȴ��´β���..." };
						PublisherUI.getInstance().publish(s);

						if (isSelfPlay)
							sendData(OS,"68010168825416","Client");
						else
							Server.getInstance().sendData(msg);


//						sleep(2000);
//
//						// 2����ʼ�����µ��������Ա���
//						SDATA = SDATA;
//
//						Send_time1 = 0;
//						sendBegin(os);
					}
				} catch (Exception e) {
					if (IN != null)
						IN.close();
//					Util698.log(Client.class.getName(), "Client ��������", Debug.LOG_INFO);

					// xuky 2018.07.09 ��Ҫ������������
					Debug.sleep(500);
					socket = connect();

					// xuky 2018.05.04 �������������Ϣ������������������رգ��������ִ�������ʾ��Ϣ
//					Util698.log(Client.class.getName(), "Client readData�����쳣"+e.getMessage(), Debug.LOG_INFO);
//					Util698.log(Client.class.getName(), "Client readData�ȴ�һ�ᣬ����������������������ӵ�", Debug.LOG_INFO);
//					// xuky 2018.05.04 ���ǽ�������ĺã�������־�����������ӳ��ȴ�ʱ��
//					Debug.sleep(10000);
				}
				Debug.sleep(300);
			}
		} catch (Exception ex) {
			Util698.log(Client.class.getName(), "Client read ������ѭ��", Debug.LOG_INFO);

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
			Util698.log(Client.class.getName(), "����sendBegin�߳�", Debug.LOG_INFO);
			String sendTime = "";
			while (true) {
				if (Send_time1 == retry){
					Util698.log(Client.class.getName(), "Client sendBegin �ﵽ�������ޣ������ط�", Debug.LOG_INFO);
					Util698.log(Client.class.getName(), "����sendBegin�߳�", Debug.LOG_INFO);
					break;
				}
				if (Send_time1 > retry){
					Util698.log(Client.class.getName(), "Client sendBegin Send_time1 > retry�������ط�", Debug.LOG_INFO);
					Util698.log(Client.class.getName(), "����sendBegin�߳�", Debug.LOG_INFO);
					// ���һ��ʱ��
					// xuky 2018.04.28 ���´���Ϊģ���Զ����Դ��룬ȡ����Ҫִ��
//					Send_time1 = 0;
//					sleep(connectInterval);
//					// ��ʼ����ȴ�״̬���ȴ��ϱ����Խ��
//					sendBegin(os);
					break;
				}

				if (sendTime == "" || Util698.getMilliSecondBetween_new(DateTimeFun.getDateTimeSSS(),sendTime) > sendInterval ){
					sendTime = DateTimeFun.getDateTimeSSS();
					Util698.log(Client.class.getName(), "Client sendBegin �ط���������", Debug.LOG_INFO);

					// ��Ҫ�ط���������
					sendData(os,start_frame,"Client");
					Send_time1++;

					Object[] s = { "LinkRemain", "Client", "�ط���������-"+Send_time1 };
					PublisherUI.getInstance().publish(s);

				}
				Debug.sleep(100); //
			}
		}).start();
	}

	public void sendData() {
		// ����յ������������Ա��ģ�����Ҫ���ж�����ԣ���ִ��sendBegin(OS) ����
		if (SDATA.substring(0, 10).equals("6831316801")){
			Send_time1 = 0;
			// ���������Ľ��б��棬���ں��������ط�����
			start_frame = SDATA;
			sendBegin(OS);
		}
		else
			sendData(OS,SDATA,"Client");
	}


}
