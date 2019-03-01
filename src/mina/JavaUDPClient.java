package mina;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;

import util.SoftParameter;

public class JavaUDPClient {

	public String sendAndRecv(String host, int port, String sData) {
		String ret = "";
		try {
			// 创建UDP连接
			// xuky 2017.10.25 根据孙发东编写的程序，需要约定发起方的端口，目前约定为9000
			InetAddress address=InetAddress.getByName(SoftParameter.getInstance().getUDPSVR_IP());
			DatagramSocket socket = new DatagramSocket(SoftParameter.getInstance().getUDPSVR_PORT(),address);
			socket.setSoTimeout(5000);
			socket.connect(InetAddress.getByName(host), port);
			// 数据转为byte数组
			sData = sData.replaceAll(" ", "");
			byte[] byteData = new byte[sData.length() / 2];
			byteData = DataConvert.hexString2ByteArray(sData);
			DatagramPacket sendPacket = new DatagramPacket(byteData, byteData.length);
			DatagramPacket recvPacket = new DatagramPacket(new byte[5000], 5000);
			socket.send(sendPacket);
			socket.receive(recvPacket);
			int bbLen = recvPacket.getLength();
			byte[] bb = recvPacket.getData();
			byte[] newbb = new byte[bbLen];
			for (int i = 0; i < bbLen; i++) {
				newbb[i] = bb[i];
			}
			ret = DataConvert.bytes2HexString(newbb);
			socket.disconnect();
			socket.close();
		} catch (Exception e) {
			String errMsg = e.getMessage();
			if (errMsg.indexOf("timed out")>=0)
				System.out.println("连接或发送数据超时"+errMsg);
			else
				System.out.println("errMsg "+errMsg);
			return "";
		}
		return ret;
	}

	public static void main(String[] args) {
//		String data = "68 15 00 43 03 11 11 11 11 00 60 6C 05 01 01 40 01 02 00 00 C6 07 16";
//		String data = "68 00 15 00 43 03 11 11 11 11 00 60 6C 05 01 01 40 01 02 00 00 C6 07 16";
		String data = "68 23 01 00 00 00 00 68 14 0D 34 C9 C9 37 37 44 44 44 87 86 74 78 34 42 16";

		// 发送数据时，直接接收数据
		System.out.println("b "+ DateTimeFun.getDateTimeSSS());
//		String getMsg = new JavaUDPClient().sendAndRecv("192.168.1.96", 7001, data);
		String getMsg = new JavaUDPClient().sendAndRecv("192.168.1.210", 9000, data);
		System.out.println("send "+ data);
		System.out.println("recv "+ getMsg);
		System.out.println("e "+ DateTimeFun.getDateTimeSSS());
	}

}
