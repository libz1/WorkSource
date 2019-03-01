package produce.meter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import base.BaseFrame;
import socket.DealData;
import socket.SocketServerEast;
import util.Publisher;
import util.PublisherFrame;
import util.SoftParameter;
import util.Util698;

// 辅助测试用窗口   接收到数据后，找到匹配的数据进行回复
public class ReadIDData extends BaseFrame implements Observer {

	// xuky 2018.11.20 这里的对象创建，不要执行赋值操作，会导数据已经创建好的对象，数据错乱！
	// 例如private JButton jButton = null;
	private JTextArea txt_qrcodes,txt_analyData, txt_errMsg;
	private String recentFrame;
	private JButton jButton;
	private String startMsg, nowMsg;

	private volatile static ReadIDData uniqueInstance;

	public static ReadIDData getInstance() {
		if (uniqueInstance == null) {
			synchronized (ReadIDData.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new ReadIDData();
					Publisher.getInstance().addObserver(uniqueInstance);
				}
			}
		}
		return uniqueInstance;
	}


	public ReadIDData() {

		PublisherFrame.getInstance().addObserver(this);
		DealData.getInstance();

		// 开启接口传输线程，在线程中判断工作中心ID，来决定是否运行参数传输过程
		Meter2MESThread.getInstance();

//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				Util698.log(ReadIDData.class.getName(), "关闭软件"+title, Debug.LOG_INFO);
//			}
//		});

		// 在界面中添加一些模拟测试用数据
//		String[]  QRCodes = {"null-1230001","null-1230002","null-1230003","null-1230004"};
//		for( String str: QRCodes)
//			txt_qrcodes.setText(txt_qrcodes.getText()+str+"\n");

	}


	// 1、异步交互过程， （4）需要对接口的update函数进行自行实现，接收消息发布者的数据
	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		if (s[0].equals("recv frame") && s[1].equals("user data")) {
			try{
				showData(arg);
			}
			catch(Exception e){
				Util698.log(ReadIDData.class.getName(), "showData Exception:"+e.getMessage(), Debug.LOG_INFO);
			}
		}
	}
	// 1、异步交互过程， （5）对收到的数据进行展示，因为涉及到UI操作可能耗时较长，所以添加synchronized，保证多线程操作时的安全性
	private synchronized void showData1(Object arg) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String comid = s[3];
		frame = frame.replaceAll(" ", "");
		frame = frame.replaceAll(",", "");
//		frame = Util698.seprateString(frame, " ");
		String showMsg = "COM-"+comid+" 收:" + frame ;
		Util698.log(ReadIDData.class.getName(), showMsg, Debug.LOG_INFO);
	}
	// 1、异步交互过程， （5）对收到的数据进行展示，因为涉及到UI操作可能耗时较长，所以添加synchronized，保证多线程操作时的安全性
	private synchronized void showData(Object arg) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String comid = s[3];
		frame = frame.replaceAll(" ", "");
		frame = frame.replaceAll(",", "");
//		frame = Util698.seprateString(frame, " ");
		String showMsg = "COM-"+comid+" 收:" + frame ;
		Util698.log(ReadIDData.class.getName(), showMsg, Debug.LOG_INFO);

		// xuky 2018.11.13 需要进行数据回复
		Boolean is_testRusult = false,is_StartScan = false;
		if (frame.startsWith("689999999999996814")){
			if (frame.indexOf("FFFFEE01") >= 0)
				is_testRusult = true;
			if (frame.indexOf("FFFFEE02") >= 0)
				is_StartScan = true;
		}

		String reply = "6899999999999968940000FA16";
		SocketServerEast.sendData(reply, comid);
		showMsg = "COM-"+comid+" 发:" + reply;
		Util698.log(ReadIDData.class.getName(), showMsg, Debug.LOG_INFO);

		if (is_StartScan){
			detect_change_New();
			return;
		}
		if (!is_testRusult){
			Util698.log(ReadIDData.class.getName(), "收到的数据不是新格式报文，不予处理", Debug.LOG_INFO);
			return;
		}

		// 测试用报文举例
		//6899999999999968140054FFFFEE01000101020002011801029C01C1FB0245534131000001ACBF092E43B981B1E2480206112233445566030001011801029C01C1FB0245534131000001AD4E60A617726EA74BD1040001020A0102030405060708090A3316
		// 进行数据处理前，清空之前收到的二维码信息

		String str = txt_qrcodes.getText();
		String[] QRCodes = str.split("\n");

		txt_qrcodes.setText("");
		txt_errMsg.setText("");
		txt_analyData.setText("");

		int num = 0;
		for( String QRCode: QRCodes ){
			if (!QRCode.equals(""))
				num++;
		}
		if (num != 4){
			String msg = Util698.getDateTimeSSS_new() + "\n";
			msg +=  "停止后续操作！因扫描二维码信息数据有误，数据量不足4，当前是"+num+"!\n";
			msg += "--------------------------\n";
			msg += str;
			msg += "--------------------------\n";
			msg += "收到报文:\n"+frame;
			if (QRCodes.equals("")){
				msg =  "停止后续操作！因扫描二维码信息数据有误，数据量不足4，当前是空数据!";
			}
			txt_errMsg.setText(msg);
			Util698.log(ReadIDData.class.getName(), msg, Debug.LOG_INFO);
			return;
		}

		Util698.log(ReadIDData.class.getName(), "扫描到的二维码信息是:\n"+str, Debug.LOG_INFO);

		// xuky 2018.11.13 记录最近收到的报文，如果相同，则只是进行串口回复确认处理，不进行后续处理
		if (recentFrame != null){
			if ( recentFrame.equals(frame)){
				String msg = "收到了重复的报文，不进行解析及存储处理。\n"+recentFrame;
				txt_errMsg.setText(msg);
				Util698.log(ReadIDData.class.getName(), msg, Debug.LOG_INFO);
				return;
			}
		}
		recentFrame = frame;
//		txt_analy.setText(showMsg);


//		String[]  QRCodes = {"1230001","1230002","1230003","1230004"};


		showMsg = "工装上报报文为:\n"+ frame+"\n";

		String[] recvMsg = null;
		MeterDataAnly meterDataAnly = new MeterDataAnly();
		try{
			recvMsg = meterDataAnly.saveMeterInfo(QRCodes,frame,SoftParameter.getInstance().getUserManager().getUserid());
		}
		catch(Exception e){
			Util698.log(ReadIDData.class.getName(), "saveMeterInfo Exception:"+e.getMessage(), Debug.LOG_INFO);
		}

		showMsg = showMsg + recvMsg[0];
		txt_analyData.setText(showMsg);

		txt_errMsg.setText(recvMsg[1]);
	}

	@Override
	// 界面初始化
	protected void init() {
		// 用于记录扫描到的二维码
		int hgap = 3, vgap = 3;
		panel.setLayout(new BorderLayout(hgap, vgap));
		{
			txt_qrcodes = new JTextArea("");
			txt_qrcodes.setLineWrap(true);// 激活自动换行功能
			txt_qrcodes.setWrapStyleWord(true);// 激活断行不断字功能
			txt_qrcodes.setFont(new Font(Font.DIALOG_INPUT,Font.BOLD,14));

			// 将多行文本框控件添加到JScrollPane对象中
			JScrollPane scroll_analy = new JScrollPane(txt_qrcodes);
			scroll_analy.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll_analy.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scroll_analy.setPreferredSize(new Dimension(300, 130));
			panel.add(scroll_analy, BorderLayout.NORTH);

		}
		// 用于展示测试情况以及添加测试用的控制按钮
		{
			JPanel panel_child;
			panel_child = new JPanel();
			panel_child.setLayout(new BorderLayout(hgap, vgap));
//			panel_child.setPreferredSize(new Dimension(300, 450));
			panel.add(panel_child, BorderLayout.CENTER);

			txt_analyData = new JTextArea("");
			txt_analyData.setLineWrap(true);// 激活自动换行功能
			txt_analyData.setWrapStyleWord(true);// 激活断行不断字功能

			// 将多行文本框控件添加到JScrollPane对象中
			JScrollPane scroll_analy = new JScrollPane(txt_analyData);
			scroll_analy.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll_analy.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			panel_child.add(scroll_analy, BorderLayout.CENTER);

			txt_errMsg = new JTextArea("");
			txt_errMsg.setLineWrap(true);// 激活自动换行功能
			txt_errMsg.setWrapStyleWord(true);// 激活断行不断字功能

			// 调整字体大小 https://bbs.csdn.net/topics/370192751
			txt_errMsg.setFont(new Font(Font.DIALOG_INPUT,Font.BOLD,14));
			// 调整显示的字体，无效
//			txt_errMsg.setForeground(Color.RED);
//			txt_errMsg.setBackground(Color.RED);


			// 将多行文本框控件添加到JScrollPane对象中
			JScrollPane scroll_err = new JScrollPane(txt_errMsg);
			scroll_err.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll_err.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scroll_err.setPreferredSize(new Dimension(300, 170));

			panel_child.add(scroll_err, BorderLayout.NORTH);

			JPanel jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout());
//			jPanel.setSize(100, 800);
			panel_child.add(jPanel, BorderLayout.SOUTH);

			startMsg = "开始获取扫描头输入";
			nowMsg = startMsg;
			jButton = new JButton(startMsg);
			jPanel.add(jButton);

			jButton.addActionListener(e -> {
				detect_change_New();
			});

		}
		new Thread(() -> {
			detect_startCode();
		}).start();
	}

	// 判断录入框中的数据，如果收到了启动码，则开始捕获扫描头数据
	private void detect_startCode() {
		String recv_txt = "", startqcode = "";
		while( true){
			startqcode = SoftParameter.getInstance().getSTARTQCODE();
			if (startqcode != null){
				if (!startqcode.equals("")){
					recv_txt = txt_qrcodes.getText();
					if (recv_txt.equals(startqcode+"\n")){
						// 清空数据
						txt_qrcodes.setText("");
						// 开始捕获扫描头数据
						new Thread(() -> {
							detect_change_New();
						}).start();
						// 输出日志
						Util698.log(ReadIDData.class.getName(), "detect_startCode 收到启动数据:"+recv_txt, Debug.LOG_INFO);
					}
				}
			}

			Debug.sleep(50);

		}

	}


	private void detect_change_New(){
		new Thread(() -> {
			detect_change_New0();
		}).start();
	}
	// 开启后5秒为间隔，5秒内一定要收到数据，否则自动填充空行，总共有4个就齐全了
	private void detect_change_New0(){


		// 根据按钮的显示信息判断状态，如果已经处在等待状态，则退出
		if ((nowMsg.indexOf(startMsg)) < 0 && !nowMsg.equals("输入结束!")){
			txt_errMsg.setText("正在等待数据输入...");
			Util698.log(ReadIDData.class.getName(), "扫描过程中，用户重复点击按钮，return", Debug.LOG_INFO);
			return;
		}

		txt_errMsg.setText(startMsg);

		// 此数据来源于“运行参数管理”参数设置界面中的
		int interval = DataConvert.String2Int(SoftParameter.getInstance().getTESTALL_TIME_OUT());
		nowMsg = "等待数据输入...检测周期为"+interval+"(毫秒)";
		Util698.log(ReadIDData.class.getName(), nowMsg, Debug.LOG_INFO);
		jButton.setText(nowMsg);
		Long val;
		txt_qrcodes.setText("");
		txt_qrcodes.requestFocus();

		String begin = Util698.getDateTimeSSS_new(),now = "",text = "";
		int i = 0,num = 0;
		while (i<4){
			now = Util698.getDateTimeSSS_new();
			val = Util698.getMilliSecondBetween_new(begin, now);
			if ( val > interval){
				i++;
				text = txt_qrcodes.getText();
				num = (Util698.numOfStr(text, "\n"));
				Util698.log(ReadIDData.class.getName(), "到达第"+i+"次检测,检测到"+num+"个数据,数据为:\n"+text, Debug.LOG_INFO);
				if ( num == 4){
					// 检测如果达到了指定的数量，可以退出检测循环
					break;
				}
				if ( num < i){
					txt_qrcodes.setText(text+"null-"+i+"\n");
					txt_qrcodes.setCaretPosition(txt_qrcodes.getText().length());
					Util698.log(ReadIDData.class.getName(), "检测不达标，添加空数据(null-"+i+")", Debug.LOG_INFO);
				}
				begin = Util698.getDateTimeSSS_new();
			}
			if ( val > 1000){
				text = txt_qrcodes.getText();
				num = (Util698.numOfStr(text, "\n"));
				if ( num == 4){
					// 检测如果达到了指定的数量，可以退出检测循环
					break;
				}
			}

			Debug.sleep(50);
		}

		Util698.log(ReadIDData.class.getName(), "输入结束!最终结果为:\n"+txt_qrcodes.getText(), Debug.LOG_INFO);
		jButton.setText(startMsg);
		nowMsg = "输入结束!";
		txt_errMsg.setText(nowMsg);
//		txt_analy.setText(txt_analy.getText()+"--The end--"+"\n");
	}


	public static void main(String[] args) {
//		String str = "\na1\n221a\n313a\n";
//		System.out.println(str);
//		System.out.println(numOfStr(str,"\n"));
		ReadIDData.getInstance().showFrame("");
	}


}
