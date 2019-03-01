package socket;

import java.io.IOException;
import java.io.InputStream;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import entity.SerialParam;
import util.Publisher;

public class SerialServer {

	public SerialServer(SerialParam s) {
		SerialPort sPort = null;
		String comm_str = "";
		try {
			// ������ĳ�����ڵļ���
			comm_str = s.getCOMM();
			CommPortIdentifier portId = CommPortIdentifier
					.getPortIdentifier(comm_str);
			sPort = (SerialPort) portId.open("shipment", 1000);
			SerialParam param = s;
			// ���ô���ͨ�Ų���
			sPort.setSerialPortParams(((SerialParam) param).getBaudRate(),
					((SerialParam) param).getDataBit(),
					((SerialParam) param).getStopBit(),
					DataConvert.String2Int(((SerialParam) param).getParity().split("(")[1].split(")")[0]));
			invoke(sPort);
		} catch (Exception e) {
			String msg = e.getClass().getName();
			msg = msg + "";
			if (msg.indexOf("NoSuchPortException") >= 0)
				System.out.println("�޷���ָ���Ĵ���:"+comm_str);
			else
				e.printStackTrace();
		}
	}

	public static void invoke(final SerialPort sPort) throws IOException {
		// �����̣߳���������  �������ݴ�����SocketServer��
		new Thread(new Runnable() {
			public void run() {
				ChannelList.getInstance().add(sPort);

				String[] s = { "refresh terminal list", "", "" };
				Publisher.getInstance().publish(s);

				String devAddr = sPort.getName();
				InputStream in = null;
				try {
					in = sPort.getInputStream();
					while (true) {
						// ����byte����ģʽ��ȡ����
						try {
							String msg = SocketServerEast.readData(in, devAddr);
							System.out.println("SerialServer recv=>" + msg);
						} catch (Exception e) {
							
							// xuky 2016.08.10 ����������ݳ��ִ��󣬾��˳�
							System.out.println("SerialServer invoke=> �˳��߳�");
							break;
						}
						Debug.sleep(100);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					try {
						in.close();
						sPort.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public static void main(String[] args) throws IOException {
		//new SerialServer();
	}

}
