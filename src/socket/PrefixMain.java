package socket;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.eastsoft.util.Debug;

import entity.SerialParam;
import mina.MinaSerialServer;
import util.SoftParameter;
import util.Util698;

// ͨ��ǰ�÷�����
public class PrefixMain {

	private volatile static PrefixMain uniqueInstance;

	ServerSocket socketServer;
	List<MinaSerialServer> serialServers = new ArrayList<MinaSerialServer>();
//	Boolean serial_open = false;

	public static PrefixMain getInstance() {
		if (uniqueInstance == null) {
			synchronized (PrefixMain.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new PrefixMain();
				}
			}
		}
		return uniqueInstance;
	}

	private PrefixMain() {
		start();
	}

	public ServerSocket getServer() {
		return socketServer;
	}

	public void setServer(ServerSocket server) {
		this.socketServer = server;
	}

	public void start() {
		// ��������ͨ��
		new Thread(()->{
			openSerial();
		}).start();

		// �������ݻ�����
		RecvData.getInstance();
		// ����������ݵ��߳�
		DealData.getInstance();
		// �ն�ͨ�Ŷ��󼯺�
		ChannelList.getInstance();

		// ����tcp����
		new Thread() {
			@Override
			public void run() {
				try {
					// ����ServerSocket���������ˣ������������˿�
					int port = SoftParameter.getInstance().getPrefix_port();

					// ����û��ڲ����н������趨����ʹ���趨��ֵ������ʹ��ϵͳ��Ĭ����ֵ
					String ip = SoftParameter.getInstance().getPrefix_ip();
					if (ip.equals(""))
						socketServer = new ServerSocket(port);
					else {
						InetAddress bindAddr = InetAddress.getByName(ip);
						socketServer = new ServerSocket(port, 8, bindAddr);
					}

					while (true) {
						// ÿ�����µĿͻ������ӵ������������ʱ������һ���µ�socket <== {Socket socket =
						// server.accept();}
						// ��SocketServer.invoke�����ж���Щ��socket���д����ֱ����̣߳�������ش���
						Socket socket = socketServer.accept();
						SocketServerEast.invoke(socket);
					}

				} catch (Exception e) {
					Util698.log(PrefixMain.class.getName(), "getLocalHostName start "+e.getMessage() , Debug.LOG_INFO);
					// String msg = this.getClass().getName() + "=>" +
					// e.getMessage();
					// DebugSwing.showMsg(msg);
					// System.out.println(msg);
				}

			}
		}.start();

	}

	public void closeSerial(){
//		if (!serial_open) return;

        Util698.log(PrefixMain.class.getName(), "����ر����д���", Debug.LOG_INFO);
		for( MinaSerialServer svr:serialServers ){
			svr.disConnect();
		}

//		serial_open = false;
	}

	public void openSerial(){
//		if (serial_open) return;
		// �������ڷ���
				// ���ݲ������еǼǵĶ�����ڴ������ڷ���
				SerialList list = SerialList.getInstance();
				for (SerialParam s : list.getList()) {
					// xuky 2018.03.12 ���߳��п������ڣ�ϣ���������������ٶ�
					//
//						Util698.log(PrefixMain.class.getName(), "��������"+s.getCOMID(), Debug.LOG_INFO);
						serialServers.add(new MinaSerialServer(s));
//						Util698.log(PrefixMain.class.getName(), "��������"+s.getCOMID()+"���", Debug.LOG_INFO);
				}
				// xuky 2018.03.12 ������ڶ�������ɣ����ñ�־λ�������������и��ݴ˱�־λ���д���
				Util698.log(PrefixMain.class.getName(), "�����������", Debug.LOG_INFO);

				SoftParameter.getInstance().setSERIAL_FINISHED(true);

	}

	// xuky 2017.04.01 ���������жϷ�������Ϊ�᷵������IP�������޷��Զ��ж�
	public String getPreferredIP() {
		String ret = "";
		String[] allIP = getAllLocalHostIP();
		List<Object> arrlist = Util698.arrayToList(allIP);
		// ���ֻ��һ��ip���򷵻ؿգ���ʾ����ʹ��Ĭ�ϵ�IP����
		if (allIP.length == 1)
			return ret;
		else {
			ret = SoftParameter.getInstance().getPrefix_ip();
			if (arrlist.contains(ret))
				return ret;
			// ����ж������getPrefix_ip������֮һ����ʹ��
			// getPrefix_ip��������֮һ������ʹ�÷�192��ͷ�ĵ�һ��ip��
			for (String str : allIP) {
				if (!str.substring(0, 4).equals("192.")) {
					ret = str;
					break;
				} else if (ret.equals(""))
					ret = str;
			}
		}
		return ret;
	}

	// �ο�http://cache.baiducontent.com/c?m=9d78d513d99b12eb0bfa940f1a66a7716d57971236c0a31668d5e313d1735b305016e7ac50200705a3d20c6d16db4d4beb806d30234460e99492ce0c9fac935b32956271350b86364a831eacdc46529d77d647baef5fbcebae658ea4d1d6d45344ca245f3cdf&p=8b2a9703c59a1af708e2947d0d5d81&newp=846fd515d9c342af1cf6c32d02148e231610db2151d7d2116b82c825d7331b001c3bbfb423241206d2c17e6406af435de0f637783c0021a3dda5c91d9fb4c57479cc3c63&user=baidu&fm=sc&query=java+%BB%F1%C8%A1ip+%B6%E0%B8%F6ip&qid=c0df82a10005c9e2&p1=4
	// ��ȡ���ip
	public String getLocalHostName() {
		String hostName;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (Exception e) {
			Util698.log(PrefixMain.class.getName(), "getLocalHostName Exception "+e.getMessage() , Debug.LOG_INFO);
			hostName = "";
		}
		return hostName;
	}

	public String[] getAllLocalHostIP() {
		String[] ret = null;
		try {
			String hostName = getLocalHostName();
			if (hostName.length() > 0) {
				InetAddress[] addrs = InetAddress.getAllByName(hostName);
				if (addrs.length > 0) {
					ret = new String[addrs.length];
					for (int i = 0; i < addrs.length; i++) {
						ret[i] = addrs[i].getHostAddress();
					}
				}
			}
		} catch (Exception ex) {
			ret = null;
		}
		return ret;
	}



//	public Boolean getSerial_open() {
//		return serial_open;
//	}
//
//	public void setSerial_open(Boolean serial_open) {
//		this.serial_open = serial_open;
//	}

	public List<MinaSerialServer> getSerialServers() {
		return serialServers;
	}

	public void setSerialServers(List<MinaSerialServer> serialServers) {
		this.serialServers = serialServers;
	}

	/*
	 * private static void showLogWin(){ int width = 800, height = 150;
	 * TestFrame mainFrame = TestFrame.getInstance();
	 * mainFrame.getPanel().setBounds(0, 0, width, height);
	 *
	 * JFrame frame = new JFrame(); frame.setTitle("��־ҳ��");
	 * frame.setLayout(null); frame.add(mainFrame.getPanel());
	 * frame.setSize(width, height); frame.setVisible(true);
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 * frame.setBounds(120, 580, width, height); frame.setAlwaysOnTop(true);
	 *
	 * }
	 *
	 * private static void showMainWin(){ int width = 800, height = 600;
	 * MainWindow mainFrame = new MainWindow();
	 * mainFrame.getPanel().setBounds(0, 0, width, height);
	 *
	 * JFrame frame = new JFrame(); frame.setTitle("698.45Э��Ӧ�����");
	 * frame.setLayout(null); frame.add(mainFrame.getPanel());
	 * frame.setSize(width, height); frame.setVisible(true);
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setBounds(80,
	 * 10, width, height); //DebugSwing.center(frame);
	 *
	 * }
	 *
	 * public static void main(String[] args) throws IOException {
	 * //DebugSwing.center(frame); // ���ʵ�ֹر�һ������ʱ�����ر�����һ������ //showMainWin();
	 * //showLogWin(); // PrefixMain prefixMain = new PrefixMain(); //
	 * prefixMain.start(); }
	 */
}
