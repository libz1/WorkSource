package produce.deal;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.net.telnet.TelnetClient;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import base.BaseFrame;
import dao.basedao.IBaseDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import produce.entity.ProduceCase;
import produce.entity.ProduceCaseDaoImpl;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceCaseResultDaoImpl;
import produce.entity.ProduceLog;
import produce.entity.ProduceLogDaoImpl;
import produce.entity.ProduceRecord;
import produce.entity.ProduceRecordDaoImpl;
import produce.entity.RunTest;
import produce.entity.RunTestDaoImpl;
import socket.SocketServerEast;
import util.Publisher;
import util.Util698;

// 辅助测试用窗口   接收到数据后，找到匹配的数据进行回复
public class FrameAssitant extends BaseFrame implements Observer {

	private JTextArea txt_analy;

	private String showMsg = "";
	static List<RunTest> runTests = null;

	private volatile static FrameAssitant uniqueInstance;
	ObservableList<ProduceCaseResult> dataListFrame = null;

	public static FrameAssitant getInstance() {
		if (uniqueInstance == null) {
			synchronized (FrameAssitant.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new FrameAssitant();
					Publisher.getInstance().addObserver(uniqueInstance);
				}
			}
		}
		return uniqueInstance;
	}

	// 主界面中调用此 FrameAssitant.getInstance(1) 目的是可以不退出窗口的情况下从数据库刷新缓存数据
	public static FrameAssitant getInstance(int flag) {
		if (uniqueInstance == null) {
			synchronized (FrameAssitant.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new FrameAssitant();
					Publisher.getInstance().addObserver(uniqueInstance);
				}
			}
		}
		refreshDataFromDB();
		uniqueInstance.showMsg = "";
		return uniqueInstance;
	}

	public FrameAssitant() {
		refreshDataFromDB();
	}

	private static void refreshDataFromDB() {
		IBaseDao<RunTest> iBaseDao_RunTest = new RunTestDaoImpl();
		// 获取所有的数据，不再从数据库中查询数据
		runTests = iBaseDao_RunTest.retrieve("where 1=1", "");
	}

	// 1、异步交互过程， （4）需要对接口的update函数进行自行实现，接收消息发布者的数据
	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		if (s[0].equals("recv frame") && s[1].equals("user data")) {
			showData(arg);
		}
		if (s[0].equals("ReadUntil") ) {
			showTelnetData(arg);
		}
	}

	private synchronized void showTelnetData(Object arg) {
		String[] s = (String[]) arg;
		String txt = s[2];
		String showMsg = txt_analy.getText();
//		showMsg = showMsg + txt + "\r\n";
		showMsg = showMsg + txt ;
		txt_analy.setText(showMsg);

		txt_analy.setCaretPosition(txt_analy.getText().length());
	}

	// 1、异步交互过程， （5）对收到的数据进行展示，因为涉及到UI操作可能耗时较长，所以添加synchronized，保证多线程操作时的安全性
	private synchronized void showData(Object arg) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String comid = s[3];
		frame = frame.replaceAll(" ", "");
		frame = frame.replaceAll(",", "");
		frame = Util698.seprateString(frame, " ");
		showMsg = showMsg + "COM"+comid+"收:" + frame + "\r\n";
		txt_analy.setText(showMsg);

		// 修改为使用缓存的数据，减少频繁的访问数据库
		RunTest runTest = null;
		for (RunTest r : runTests) {
			if (r.getRecv().replaceAll(" ", "").equals(frame.replaceAll(" ", ""))) {
				runTest = r;
				break;
			}
		}
		if (runTest != null) {
			String send = runTest.getSend();
			send = send.replaceAll("\\*", "1");
//			String port = runTest.getPort();
//			if (!SocketServer.sendData(send, port)) {
			if (!SocketServerEast.sendData(send, comid)) {
				String msg = "未打开串口";
				showMsg = showMsg + msg + "\r\n";
				txt_analy.setText(showMsg);
				return;
			}
			showMsg = showMsg + "COM"+comid+"发:" + send + "\r\n";
		}
	}

	@Override
	protected void init() {
		txt_analy = new JTextArea("");
		JScrollPane scroll_analy = new JScrollPane(txt_analy);
		scroll_analy.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll_analy.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		txt_analy.setLineWrap(true);// 激活自动换行功能
		txt_analy.setWrapStyleWord(true);// 激活断行不断字功能

		int hgap = 3, vgap = 3;
		panel.setLayout(new BorderLayout(hgap, vgap));

		panel.add(scroll_analy, BorderLayout.CENTER);

		// xuky 2017.07.28 telnet交互测试


		JPanel jPanel = new JPanel();
		jPanel.setLayout(new FlowLayout());
		panel.add(jPanel, BorderLayout.SOUTH);

		JButton jb2 = new JButton("telnet");
		jPanel.add(jb2);

		JButton jb3 = new JButton("测试过程汇总");
		jPanel.add(jb3);

		JButton jb4 = new JButton("内存占用情况测试");
		jPanel.add(jb4);

		jb2.addActionListener(e -> {

			new Thread(() -> {
				telnet();
			}).start();

		});

		jb3.addActionListener(e -> {
			buildProcudeRecord();
		});

		jb4.addActionListener(e -> {
			memTest();
		});
	}

	private void memTest() {
		ProduceCaseResultDaoImpl dao = new ProduceCaseResultDaoImpl();
//		List<ProduceCaseResult> result = dao.retrieve("where caseno='1'", "");
		List<ProduceCaseResult> result = new ArrayList<ProduceCaseResult>();
		dataListFrame = FXCollections.observableArrayList(result);
		int max  = 10000;
		for( int i = 0; i< max;i++ ){
			System.out.println(i+"-"+max);
			for( int j = 0; j< 6000;j++ ){
				ProduceCaseResult produceCaseResult = new ProduceCaseResult();
//				dataListFrame.add(produceCaseResult);
				result.add(produceCaseResult);
			}
			Debug.sleep(50);
			result = null;
//			Util698.ListReMoveAll(dataListFrame);
//			dataListFrame.clear();
//			dataListFrame = null;
//			dataListFrame = FXCollections.observableArrayList(result);
		}
		dataListFrame = null;

	}

	// 计算生产测试汇总数据
	public void buildProcudeRecord() {

		// xuky 2017.08.02  需要先删除中的数据ProduceRecord 防止数据出现重复情况
		// xuky 2017.08.02 只能查询一部分ProduceLog数据用于组织ProduceRecord数据，查询某一天的数据
		IBaseDao<ProduceRecord> iBaseDao_ProduceRecord = new ProduceRecordDaoImpl();
		IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();

		List<ProduceLog> produceLogList = iBaseDao_ProduceLog.retrieve("", "");
		List<ProduceRecord> produceRecordList = new ArrayList<ProduceRecord>();

		String firstStep = "", currentStep = "", firstAddr = "";
		String firstTime = "", currentTime = "", currentAddr = "";
		for (ProduceLog produceLog : produceLogList) {
			if (firstStep.equals("")) {
				firstStep = produceLog.getOperation();
				firstTime = produceLog.getOpTime();
				firstAddr = produceLog.getAddr();
				continue;
			}
			currentStep = produceLog.getOperation();
			currentTime = produceLog.getOpTime();
			currentAddr = produceLog.getAddr();

			// if (currentTime.equals("2017-06-15 15:39:02:980"))
			// currentTime = currentTime;
			// System.out.println("buildProcudeRecord=> currentTime: "+
			// currentTime);

			// xuky 2017.06.16 如果两个操作之间的时间超过30分钟，不进行计算
			Long UsingTime = Util698.getMilliSecondBetween_new(currentTime, firstTime);
			if (UsingTime >= 1000 * 60 * 30) {
				firstStep = currentStep;
				firstTime = currentTime;
				firstAddr = currentAddr;
				continue;
			}
			if (firstStep.equals("扫描条码(1)")) {
				ProduceRecord produceRecord = new ProduceRecord();
				if (currentStep.equals("测试成功(2)"))
					produceRecord.setOpResult("测试成功(1)");
				if (currentStep.equals("测试失败(3)"))
					produceRecord.setOpResult("测试失败(2)");
				if (currentStep.equals("测试成功(2)") || currentStep.equals("测试失败(3)")) {
					produceRecord.setOpName(produceLog.getOpName());
					produceRecord.setWorkStation(produceLog.getWorkStation());
					produceRecord.setAddr(currentAddr);
					produceRecord.setOpUsingTime(UsingTime);
					produceRecord.setOpTime(firstTime);
					produceRecord.setEndTime(currentTime);
					produceRecord.setOperation("软件测试(1)");
					produceRecord.setBeginOpt(firstStep);
					produceRecord.setEndOpt(currentStep);
					produceRecord.setPrevAddr(firstAddr);
					produceRecordList.add(produceRecord);
				}
			}
			if (firstStep.equals("测试成功(2)") || firstStep.equals("设置设备异常(4)") || firstStep.equals("测试失败(3)")) {
				ProduceRecord produceRecord = new ProduceRecord();
				if (currentStep.equals("扫描条码(1)")) {
					if (firstStep.equals("测试成功(2)") || firstStep.equals("设置设备异常(4)"))
						produceRecord.setOperation("用户装配(2)");
					else
						produceRecord.setOperation("用户重试(3)");
				}
				if (currentStep.equals("设置设备异常(4)")) {
					produceRecord.setOperation("用户设置异常(4)");
				}
				produceRecord.setAddr(currentAddr);
				produceRecord.setOpName(produceLog.getOpName());
				produceRecord.setWorkStation(produceLog.getWorkStation());
				produceRecord.setOpUsingTime(UsingTime);
				produceRecord.setOpTime(firstTime);
				produceRecord.setEndTime(currentTime);
				produceRecord.setBeginOpt(firstStep);
				produceRecord.setEndOpt(currentStep);
				produceRecord.setPrevAddr(firstAddr);
				produceRecordList.add(produceRecord);
			}
			firstStep = currentStep;
			firstTime = currentTime;
			firstAddr = currentAddr;
		}

		// 2017-06-15 15:39:02:980
		iBaseDao_ProduceRecord.create(produceRecordList);

	}



	private void telnet() {
		try {
			// 对telnet进行封装
			String readStr = "";
			// -------初始化阶段---------- telnet xx.xx.xx.xx   root
			TelnetClient tc = new TelnetClient();
			tc.connect("192.168.1.96", 23);
			InputStream in = tc.getInputStream();
			OutputStream os = tc.getOutputStream();
			readStr = readUntil(":", in);
			System.out.print(readStr);
			writeUtil("root", os);
			readStr = readUntil("[root@(none) /]#", in);
			System.out.print(readStr);
			// -------初始化阶段---------- telnet xx.xx.xx.xx   root

			// --------------集中器升级-----------
			String[] s = { "ReadUntil", "", "【开始升级...】"+DateTimeFun.getDateTimeSSS()+"\r\n" };
			Publisher.getInstance().publish(s);

			writeUtil("dn 192.168.1.210", os);
			readStr = readUntil("[root@(none) /]#", in);
			System.out.print(readStr);
			String[] s1 = { "ReadUntil", "", "【升级结束！】"+DateTimeFun.getDateTimeSSS()+"\r\n" };
			Publisher.getInstance().publish(s1);
			// --------------集中器升级-----------


			// -------初始化阶段---------- init_dev
			writeUtil("init_dev", os);
			readStr = readUntil("input the choice>", in);
			System.out.print(readStr);
			// -------初始化阶段---------- init_dev

			changeParam( os,in,"2","192.168.1.216" );
			changeParam( os,in,"3","4006" );
			changeParam( os,in,"6","CMNET6" );

			// 设置前多了一步确认步骤
			changeParamWithCheck( os,in,"7","37023336" );
			changeParam( os,in,"10","card6" );
			changeParam( os,in,"11","card6" );

			// 执行命令，无需参数
			runParam( os,in,"20" );

			writeUtil("88", os);
			System.out.print(readUntil("input the choice>", in));

			writeUtil("98", os);
			System.out.print(readUntil("[root@(none) /]#", in));

			writeUtil("init_dev", os);
			System.out.print(readUntil("input the choice>", in));

//			writeUtil("dn 192.168.1.210", os);
//			System.out.print(readUntil("[root@(none) /]#", in));
			// 大部分是只需一次y/n进行确认的    7\31

			// 部分数据是枚举类型的，从待选项目中进行筛选  enter之前可能有 ,.;等无效字符，去掉·
			// 最后一个 :  part1  ;  part2  enter  [16]
//			current dmconfig setting is :1,select setting: 0--normal; 1--little),enter new s
			// 最后一个 :  part1  ;  part2  enter   [8]
//			select DEBUG com mode: 1--Print(115200,8,n);2--UP COMM.enter num>
			// 最后一个 :  part1  ;  part2  enter   [31]
//			select type :1--UDP;2--TCP;enter type>2

//			最后一个 :  part1  回车换行  part2 回车换行   confirm     [23]
//			config the 485_2 com is: 1-cascade com[mastermode]
//                    2-cascade com[slavemode]
//                    3-up/ac com[cascade mastermode]
//                    4-read ex_485 mode[cascade mastermode]
//                    		confirm enter?<1\2\3\4\n>n
//			最后一个 :  part1  回车换行  part2 回车换行   confirm     [48]
//			config the switch    is: 1-close
//            2-open
//            	confirm enter?<1\2\n>n



			// writeUtil("ip addr ", os);
			// System.out.print(readUntil("root@WiAC:~#", in));
			//
			// writeUtil("ip route ", os);
			// System.out.print(readUntil("root@WiAC:~#", in));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	// 修改参数信息，其中需要进行修改确认
	private void changeParamWithCheck(OutputStream os, InputStream in, String key,String val){
		writeUtil(key, os);
		System.out.print(readUntil(">", in));

		writeUtil("y", os);
		System.out.print(readUntil(">", in));

		writeUtil(val, os);
		System.out.print(readUntil(">", in));
		writeUtil("y", os);

		System.out.print(readUntil("input the choice>", in));
	}

	// 修改参数信息，其中无需修改前确认
	private void changeParam(OutputStream os, InputStream in, String key,String val){
		writeUtil(key, os);
		System.out.print(readUntil(">", in));

		writeUtil(val, os);
		System.out.print(readUntil(">", in));
		writeUtil("y", os);

		System.out.print(readUntil("input the choice>", in));
	}

	private void runParam(OutputStream os, InputStream in, String key){
		writeUtil(key, os);
		System.out.print(readUntil(">", in));

		writeUtil("y", os);

		System.out.print(readUntil("input the choice>", in));
	}

	// 写入命令方法
	public static void writeUtil(String cmd, OutputStream os) {
		try {
			cmd = cmd + "\n";
			os.write(cmd.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 读到指定位置,不在向下读
	public static String readUntil(String endFlag, InputStream in) {

		InputStreamReader isr = new InputStreamReader(in);

		char[] charBytes = new char[1024];
		int n = 0;
		boolean flag = false;
		String str = "";
		try {
			while ((n = isr.read(charBytes)) != -1) {
				for (int i = 0; i < n; i++) {
					char c = (char) charBytes[i];

					// xuky 2017.08.21 每收到一点数据就进行展示，在进行升级操作时可以细致的展示升级过程
					String tmp = ""+c;
					String[] s = { "ReadUntil", "", tmp };
					Publisher.getInstance().publish(s);

					str += c;
					// System.out.println("readUntil:"+str);
					// 当拼接的字符串以指定的字符串结尾时,不在继续读
					if (str.endsWith(endFlag)) {
						flag = true;
						break;
					}
				}
				if (flag) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}



		return str;
	}
}
