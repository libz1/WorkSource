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

// ��PrefixMain.start()�����б�����ģ�����invoke״̬����ʱ��������
public class SocketServerEast {
	private static Socket CLIENT_SEND= null;

	public static void invoke(Socket client) throws IOException {
		// �����µ��߳̽�����ش���
		new Thread(() -> dealNewConnect(client)).start();
	}

	public static void sendSocketData(String sendData){
		sendData(CLIENT_SEND, sendData);
	};

	public static void dealNewConnect(Socket client) {
		// �����Ӷ��б��������
		ChannelList.getInstance().add(client);
		CLIENT_SEND = client;

		String[] s = { "refresh terminal list", "", "" };
		Publisher.getInstance().publish(s);

		String devAddr = client.getRemoteSocketAddress().toString();
		Util698.log(SocketServerEast.class.getName(), "�����µ�socket���� and setRECVCLINET" +devAddr,Debug.LOG_INFO);

		SoftParameter.getInstance().setRECVCLINET("168");
		SoftParameter.getInstance().saveParam();

		InputStream in = null;
		try {
			in = client.getInputStream();
			// ѭ�����������ݣ�ʼ�ղ��˳�
			while (true) {
				try {
					// ����byte����ģʽ��ȡ����
					String msg = readData(in, devAddr);
//					System.out.println("SocketServer 2018.04.12 recv=>" + msg);
				} catch (Exception e) {
					// xuky 2016.08.10 ����������ݳ��ִ��󣬾��˳�
//					System.out.println("SocketServer invoke=> �˳��߳�");
					Util698.log(SocketServerEast.class.getName(), "dealNewConnect Exception e1:"+e.getMessage(), Debug.LOG_INFO);
					// xuky 2017.05.08 ����Ҫ���к�������ɾ��������е����ݵ�
					ChannelList.getInstance().remove(client);
					// ���ͨ�Ŷ����б�
					ChannelObjs.getInstance().reMove(devAddr);
					// ͨ���۲��߷��������ն˱仯��Ϣ������ˢ�½�������
					String[] s1 = { "refresh terminal list", "", "" };
					Publisher.getInstance().publish(s1);
					break;
				}

				// sleep ��������ϵ����ȼ����̻߳�����л��ᣬ��yield��������ִ��ʱ����ǰ�߳��Դ��ڿ�����״̬
				// ���Բ������ó��ϵ����ȼ����̴߳�ʱ��ȡCPUռ��Ȩ��
				// ��һ������ϵͳ�У�����ϸ����ȼ����߳�û�е���sleep������Ҳû���ܵ�I/O������
				// ��ô�ϵ����ȼ��߳�ֻ�ܵȴ����нϸ����ȼ����߳����н����������л�������

				// sleep()ʹ��ǰ�߳̽���ͣ��״̬������ִ��sleep()���߳���ָ����ʱ���ڿ϶�����ִ�У�
				// yield()ֻ��ʹ��ǰ�߳����»ص���ִ��״̬������ִ��yield()���߳��п����ڽ��뵽��ִ��״̬�������ֱ�ִ��

				// sleep()��ʹ���ȼ��͵��̵߳õ�ִ�еĻ��ᣬ��ȻҲ������ͬ���ȼ��͸����ȼ����߳���ִ�еĻ��᣻
				// yield()ֻ��ʹͬ���ȼ����߳���ִ�еĻ���
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
			// ����byte����ģʽ��ȡ����
			byte[] receiveByte = new byte[4096];
			int messageLength = is.read(receiveByte);
			// System.out.println("readData is.read:"+messageLength);
			// xuky 2017.05.08 �������Ӷ˶Ͽ������
			// ��������ԭ�ȵ�throws Exception
			// if (messageLength == -1){
			// return "";
			// }
			byte[] currReceiveByte = new byte[messageLength];
			for (int i = 0; i < messageLength; i++)
				currReceiveByte[i] = receiveByte[i];

			String recvData = "";
			if (currReceiveByte != null)
				// byteתΪ�ַ���
				recvData = DataConvert.bytes2HexString(currReceiveByte);

			// ͨ�ŵ�ַ ͨ������
			String msg = "addr@" + devAddr + ";" + "msg@" + recvData;

			// �յ�������ӵ�RecvData����������
//			Util698.log(SocketServer.class.getName(), "RecvData.push msg:"+msg,
//			Debug.LOG_INFO);
			RecvData.getInstance().push(msg);
			return msg;
//		}
//		catch (Exception e){
//			Util698.log(SocketServerEast.class.getName(), "readData Exception:"+e.getMessage(),Debug.LOG_INFO);
//		}
	}

	// xuky 2017.06.21 ��ָ���˿ڷ��ͱ�������
	public static Boolean sendData(String sendData, String port) {
		sendData = sendData.replaceAll(" ", "");
		Util698.log(SocketServerEast.class.getName(), port + "��:"+sendData,Debug.LOG_INFO);

		if (port.equals("")){
			Util698.log(SocketServerEast.class.getName(), port + "Ϊ�գ��޷�����",Debug.LOG_INFO);
			return false;
		}
		// ����port��Ϣ���ҵ���Ӧ�ķ���ʵ�����
		Object object = ChannelObjsByLogiAddr.getInstance().get(port);

//		Object os = null;
		String frameType = "user data";
		if (object == null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					// AlertBox.display("��������", "�����Ƿ����ͨ�ŷ�����");
					String msg = "�����Ƿ����ͨ�ŷ����� port="+port;
					javafxutil.f_alert_informationDialog("��������", msg);
				}
			});
			return false;
		}

		if (object instanceof MinaSerialServer) {
//			os = object;
			sendToOs(port, sendData, frameType, object);

			 // 2������Ϣ���ķ�����Ϣ:������ĳ�����͵�ĳ������
			 String[] s = { "send frame", frameType, sendData };
			 Publisher.getInstance().publish(s);

		}
		return true;
	}

	// ���ݱ����е��ն˵�ַ�Զ��ҵ���Ӧ��socket������δ�����쳣����
	public static void sendData(String sendData) {

		// 1���Է��͵ı��Ľ��н������õ�����������Ϣ(��·���ġ��û�����)
		Frame698 frame689 = new Frame698(sendData);
		int choiseFlag = frame689.getAPDU().getChoiseFlag();

		String frameType = "user data";
		if (choiseFlag == 1 || choiseFlag == 129)
			frameType = "link data";

		// 3���õ����ĵķ���Ŀ���豸��ַ
		String sadata = frame689.getFrameAddr().getSAData();

		Object os = null;
		try {
			// 4�������豸��ַ�Ҷ�Ӧ������ͨ�Ŷ��󣬿���������Ҳ�����Ǵ���
			Object object = ChannelObjsByLogiAddr.getInstance().get(sadata);

			Socket client = null;
			String obj_addr = "";

			if (object instanceof Socket) {
				client = (Socket) object;
				obj_addr = client.getRemoteSocketAddress().toString();
				os = client.getOutputStream();
			}
			// xuky 2017.05.11 ��Ӷ���mina����ͨ�ŷ�ʽ��֧��
			if (object instanceof MinaSerialServer) {
				os = object;
				obj_addr = ((MinaSerialServer) object).getName();
			}

			if (os != null) {
				sendToOs(obj_addr, sendData, frameType, os);
//				 // 5����������ͨ�Ŷ���ĵ�ַ��Ϣ����ͨ������
//				 Channel channel =
//				 ChannelList.getInstance().getByCode(obj_addr);
//				 if (channel != null) {
//				 // �޸�ͨ����������ͨ��ʱ��
//					 channel.setRecvTime(DateTimeFun.getDateTimeSSS());
//				 }
//				 // ˢ�½����е��ն�ͨ���б�
//				 String[] s1 = { "refresh terminal list", "", "" };
//				 Publisher.getInstance().publish(s1);
//				 // 2������Ϣ���ķ�����Ϣ:������ĳ�����͵�ĳ������
//				 String[] s = { "send frame", frameType, sendData };
//				 Publisher.getInstance().publish(s);
//				System.out.println("sendData "+sendData);

				// }
				// // 6��������ͨ�Ŷ����з�������
				// sendData(os, sendData);
			} else {
				String[] s1 = { "send frame", "�豸" + sadata + "������" + sendData };
				Util698.log(SocketServerEast.class.getName(), "�豸" + sadata + "������:" + sendData, Debug.LOG_INFO);
				Publisher.getInstance().publish(s1);
			}

		} catch (Exception e1) {
			Util698.log(SocketServerEast.class.getName(), "sendData String Exception " + e1.getMessage(), Debug.LOG_INFO);
		}
	}

	public static void sendToOs(String obj_addr, String sendData, String frameType, Object os) {
		// 6��������ͨ�Ŷ����з�������
		// xuky 2018.02.02 �����ڣ������5��֮�󣬵���������ҷ����߳���ִ�У��������Ч��
		new Thread(() -> {
			sendData(os, sendData);
		}).start();

		new Thread(() -> {
			// 5����������ͨ�Ŷ���ĵ�ַ��Ϣ����ͨ������
			Channel channel = ChannelList.getInstance().getByCode(obj_addr);
			if (channel != null) {
				// �޸�ͨ����������ͨ��ʱ��
				channel.setRecvTime(DateTimeFun.getDateTimeSSS());
				// ˢ�½����е��ն�ͨ���б�
				String[] s1 = { "refresh terminal list", "", "" };
				Publisher.getInstance().publish(s1);
				// 2������Ϣ���ķ�����Ϣ:������ĳ�����͵�ĳ������
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
				Util698.log(SocketServerEast.class.getName(), "Socket send�쳣��������  data:"+sData, Debug.LOG_INFO);
				return;
			}
			os = client.getOutputStream();
			sendData(os, sData);
		} catch (Exception e) {
			Util698.log(SocketServerEast.class.getName(), "sendData Exception e:"+e.getMessage(), Debug.LOG_INFO);
		} finally {
			try {
				// xuky 2016.08.10 �ر�OutputStream�ᵼ��socket�رգ����Բ�ִ��
				// �ο� http://blog.csdn.net/justoneroad/article/details/6962567
				// os.close();
			} catch (Exception e) {
			}
		}
	}

	public static void sendData(Object os, String sData) {
		// xuky 2017.07.04 ��������
		if (os instanceof OutputStream) {
//			Util698.log(SocketServer.class.getName(), "send�� " + sData, Debug.LOG_INFO);
			sendData((OutputStream) os, sData);
		}
		if (os instanceof MinaSerialServer) {
//			Util698.log(SocketServer.class.getName(), "send�� " + sData, Debug.LOG_INFO);
			sendData((MinaSerialServer) os, sData);
		}

	}

	public static void sendData(MinaSerialServer os, String sData) {
		byte[] byteData = new byte[sData.length() / 2];
		// ��16�����ַ���תΪByte����
		byteData = DataConvert.hexString2ByteArray(sData);
		// System.out.println("sendData=>" + sData);
		os.sendMessage(byteData);
	}

	public static void sendData(OutputStream os, String sData) {
		// ��������
		try {
			byte[] byteData = new byte[sData.length() / 2];
			// ��16�����ַ���תΪByte����
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

		// �����˿�Ϊ10000
		ServerSocket server = new ServerSocket(10000);
		while (true) {
			Socket socket = server.accept();
			invoke(socket);
		}
	}

}
