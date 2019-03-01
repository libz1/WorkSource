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

// 通信前置服务器
public class PrefixMain {

	private volatile static PrefixMain uniqueInstance;

	ServerSocket socketServer;
	List<MinaSerialServer> serialServers = new ArrayList<MinaSerialServer>();
//	Boolean serial_open = false;

	public static PrefixMain getInstance() {
		if (uniqueInstance == null) {
			synchronized (PrefixMain.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
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
		// 开启串口通道
		new Thread(()->{
			openSerial();
		}).start();

		// 接收数据缓冲区
		RecvData.getInstance();
		// 处理接收数据的线程
		DealData.getInstance();
		// 终端通信对象集合
		ChannelList.getInstance();

		// 启动tcp服务
		new Thread() {
			@Override
			public void run() {
				try {
					// 开启ServerSocket（服务器端），开启监听端口
					int port = SoftParameter.getInstance().getPrefix_port();

					// 如果用户在参数中进行了设定，则使用设定的值，否则使用系统的默认数值
					String ip = SoftParameter.getInstance().getPrefix_ip();
					if (ip.equals(""))
						socketServer = new ServerSocket(port);
					else {
						InetAddress bindAddr = InetAddress.getByName(ip);
						socketServer = new ServerSocket(port, 8, bindAddr);
					}

					while (true) {
						// 每当有新的客户端连接到这个服务器端时，就有一个新的socket <== {Socket socket =
						// server.accept();}
						// 在SocketServer.invoke函数中对这些个socket进行处理（分别开启线程，进行相关处理）
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

        Util698.log(PrefixMain.class.getName(), "逐个关闭所有串口", Debug.LOG_INFO);
		for( MinaSerialServer svr:serialServers ){
			svr.disConnect();
		}

//		serial_open = false;
	}

	public void openSerial(){
//		if (serial_open) return;
		// 启动串口服务
				// 根据参数表中登记的多个串口创建串口服务
				SerialList list = SerialList.getInstance();
				for (SerialParam s : list.getList()) {
					// xuky 2018.03.12 在线程中开启串口，希望提高整体的启动速度
					//
//						Util698.log(PrefixMain.class.getName(), "开启串口"+s.getCOMID(), Debug.LOG_INFO);
						serialServers.add(new MinaSerialServer(s));
//						Util698.log(PrefixMain.class.getName(), "开启串口"+s.getCOMID()+"完成", Debug.LOG_INFO);
				}
				// xuky 2018.03.12 如果串口都启动完成，设置标志位，在其他程序中根据此标志位进行处理
				Util698.log(PrefixMain.class.getName(), "开启串口完成", Debug.LOG_INFO);

				SoftParameter.getInstance().setSERIAL_FINISHED(true);

	}

	// xuky 2017.04.01 放弃此种判断方法，因为会返回许多的IP，程序无法自动判断
	public String getPreferredIP() {
		String ret = "";
		String[] allIP = getAllLocalHostIP();
		List<Object> arrlist = Util698.arrayToList(allIP);
		// 如果只有一个ip，则返回空，表示继续使用默认的IP即可
		if (allIP.length == 1)
			return ret;
		else {
			ret = SoftParameter.getInstance().getPrefix_ip();
			if (arrlist.contains(ret))
				return ret;
			// 如果有多个，且getPrefix_ip是其中之一，则使用
			// getPrefix_ip不是其中之一，优先使用非192开头的第一个ip，
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

	// 参考http://cache.baiducontent.com/c?m=9d78d513d99b12eb0bfa940f1a66a7716d57971236c0a31668d5e313d1735b305016e7ac50200705a3d20c6d16db4d4beb806d30234460e99492ce0c9fac935b32956271350b86364a831eacdc46529d77d647baef5fbcebae658ea4d1d6d45344ca245f3cdf&p=8b2a9703c59a1af708e2947d0d5d81&newp=846fd515d9c342af1cf6c32d02148e231610db2151d7d2116b82c825d7331b001c3bbfb423241206d2c17e6406af435de0f637783c0021a3dda5c91d9fb4c57479cc3c63&user=baidu&fm=sc&query=java+%BB%F1%C8%A1ip+%B6%E0%B8%F6ip&qid=c0df82a10005c9e2&p1=4
	// 获取多个ip
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
	 * JFrame frame = new JFrame(); frame.setTitle("日志页面");
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
	 * JFrame frame = new JFrame(); frame.setTitle("698.45协议应用软件");
	 * frame.setLayout(null); frame.add(mainFrame.getPanel());
	 * frame.setSize(width, height); frame.setVisible(true);
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setBounds(80,
	 * 10, width, height); //DebugSwing.center(frame);
	 *
	 * }
	 *
	 * public static void main(String[] args) throws IOException {
	 * //DebugSwing.center(frame); // 如何实现关闭一个窗口时，不关闭另外一个窗口 //showMainWin();
	 * //showLogWin(); // PrefixMain prefixMain = new PrefixMain(); //
	 * prefixMain.start(); }
	 */
}
