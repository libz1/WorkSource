package mina;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.eastsoft.util.DataConvert;

import entity.SerialParam;
import socket.ChannelList;
import socket.ChannelObjsByLogiAddr;
import util.Publisher;
import util.SoftParameter;
// ���������ͻ��ˡ�serial
public class MinaTCPClient {

	// xuky 2017.05.12 ����ʹ��static �ᵼ�¶��MinaSerialServerʵ���ı�����һ��
	private IoSession ioSession = null;
	// ���ڵ����ƣ���Ҫ�������Ľ�������һ����COM1����COM2...
	private String name = "";
	// �������󶨵��ն˵�ַ
	private String logAddr = "";

	private String comID; // �����������������ֲ�ͬ������

	private int STATUS; // ״̬ 0 δ���� 1�ɹ� -1������ -2�Ѵ�

	private SerialParam serialParam;

	public SerialParam getSerialParam() {
		return serialParam;
	}

	public void setSerialParam(SerialParam serialParam) {
		this.serialParam = serialParam;
	}

	public MinaTCPClient(SerialParam s) {
		serialParam = s;
		init();
	}

	public void init() {
		STATUS = 0;
		comID = serialParam.getCOMID(); // �������ֲ�ͬ�Ĵ���

		// xuky 2017.08.26 ��ֹ��������null�����
		SoftParameter.getInstance().getRecvDataMap().put(comID,"");

		name = serialParam.getCOMM();
		logAddr = serialParam.getTerminal();

		// xuky 2017.06.22 ͨ�����ڶԵ����в���ʱ��������Ϣ�е��ն˵�ַ����Ϊ��
		if (logAddr.equals("")) {
			logAddr = comID;
		}

		// ���ô�������
		// if (runTime == 0)
		InetSocketAddress address = new InetSocketAddress(serialParam.getCOMM(), serialParam.getBaudRate());
		// SerialAddress address = new SerialAddress(name, 9600,
		// DataBits.DATABITS_8, StopBits.BITS_1,
		// Parity.EVEN, FlowControl.NONE);
		connect(address);
	}

	// �Ͽ�����ͨ������ͨ���б��г���
	public void disConnect() {
		try {
			if (ioSession != null)
				ioSession.closeNow();

			ChannelList.getInstance().remove(logAddr);
			ChannelObjsByLogiAddr.getInstance().reMove(logAddr);
			String[] s = { "refresh terminal list", "", "" };
			Publisher.getInstance().publish(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void connect(InetSocketAddress address) {

		NioSocketConnector connector = new NioSocketConnector();
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
		connector.setHandler(new SerialServerHandlerByte());
		try {
			ConnectFuture future = connector.connect(address);
			future.await();
			if (future.getException() != null) {
				String msg = future.getException().getMessage();
				if (msg.equals("Serial port not found"))
					STATUS = -1;
				else
					STATUS = -2;
				// System.out.println("MinaSerialServer
				// connect:"+address.getName() + msg );
			} else {
				// xuky 2017.08.11 ������Ҫ�ж�future.getException()Ȼ���ٽ��������getSession
				ioSession = future.getSession();
				ioSession.setAttribute("logAddr", logAddr);
				ioSession.setAttribute("name", name);
				ioSession.setAttribute("comID", comID);
				STATUS = 1;
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		Util698.log(MinaSerialServer.class.getName(), "future = connector.connect end " + DateTimeFun.getDateTimeSSS(),
//				Debug.LOG_INFO);

		ChannelList.getInstance().add(this, logAddr);
		ChannelObjsByLogiAddr.getInstance().add(logAddr, this);
		String[] s = { "refresh terminal list", "", "" };
		Publisher.getInstance().publish(s);

	}

	public void sendMessage(byte[] data) {
		// ��ȡ��ǰ���ӵ�session
		ioSession.write(data);
		// IoSession session;
		// Map<?, ?> conMap = connector.getManagedSessions();
		// Iterator<?> iter = conMap.keySet().iterator();
		// while (iter.hasNext()) {
		// Object key = iter.next();
		// session = (IoSession) conMap.get(key);
		// session.write(data);
		// }
	}

	public String getName() {
		return name;
	}

	public int getSTATUS() {
		return STATUS;
	}

	public void setSTATUS(int sTATUS) {
		STATUS = sTATUS;
	}

	public static void main(String[] args) {
		// ����ʹ�ô��ڶ���洢ͨ�Ų���
		SerialParam s = new SerialParam();
		s.setCOMM("192.168.1.96");
		s.setBaudRate(7000);

		MinaTCPClient minaSocketClient = new MinaTCPClient(s);
		String sData = "68 15 00 43 03 11 11 11 11 00 60 6C 05 01 01 40 01 02 00 00 C6 07 16";
		sData = sData.replaceAll(" ", "");
		byte[] byteData = new byte[sData.length() / 2];
		// ��16�����ַ���תΪByte����
		byteData = DataConvert.hexString2ByteArray(sData);
		minaSocketClient.sendMessage(byteData);
//

	}

}
