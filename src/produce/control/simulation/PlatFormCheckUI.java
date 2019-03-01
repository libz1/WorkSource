package produce.control.simulation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eastsoft.util.Debug;

import base.BaseFrame;
import util.Publisher;
import util.PublisherFrame;
import util.Util698;

// 辅助测试用窗口   接收到数据后，找到匹配的数据进行回复
public class PlatFormCheckUI extends BaseFrame implements Observer {

	// xuky 2018.11.20 这里的对象创建，不要执行赋值操作，会导数据已经创建好的对象，数据错乱！
	// 例如private JButton jButton = null;
	private JTextArea txt_qrcodes,txt_analyData, txt_errMsg;
	private JButton jButton,jButton1,jButton2,jButton3,jButton4,jButton5,jButton6,jButton7,jButton8,jButton9,jButton10;
	JTextField input_meterno, input_com, input_voltage, input_current, input_FSFlag;

	private volatile static PlatFormCheckUI uniqueInstance;

	public static PlatFormCheckUI getInstance() {
		if (uniqueInstance == null) {
			synchronized (PlatFormCheckUI.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new PlatFormCheckUI();
				}
			}
		}
		return uniqueInstance;
	}


	public PlatFormCheckUI() {
		Publisher.getInstance().addObserver(this);
	}


	// 1、异步交互过程， （4）需要对接口的update函数进行自行实现，接收消息发布者的数据
	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		if ((s[0].equals("recv frame") || s[0].equals("send frame")|| s[0].equals("result"))  && s[1].equals("user data")) {
			try{
				showData(arg);
			}
			catch(Exception e){
				Util698.log(PlatFormCheckUI.class.getName(), "showData Exception:"+e.getMessage(), Debug.LOG_INFO);
			}
		}
	}
	// 1、异步交互过程， （5）对收到的数据进行展示，因为涉及到UI操作可能耗时较长，所以添加synchronized，保证多线程操作时的安全性
	private synchronized void showData(Object arg) {
		String[] s = (String[]) arg;
		String frame = s[2];
//		frame = frame.replaceAll(" ", "");
//		frame = frame.replaceAll(",", "");
		String showMsg = "";
		if (s[0].equals("result")){
			showMsg = Util698.getDateTimeSSS_new()+" "+ frame ;
		}

		String txt = txt_analyData.getText();
		txt += showMsg + '\n';
		txt_analyData.setText(txt);
		txt_analyData.setCaretPosition(txt_analyData.getText().length());
	}

	@Override
	// 界面初始化
	protected void init() {
		// xuky 2019.02.16  关闭此窗口时关闭整个的界面
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		// 如果关闭前需要提示，可以在这里进行
//		frame.addWindowListener(new WindowAdapter(){
//			public void windowClosing(WindowEvent e) {
//				int n = JOptionPane.showConfirmDialog(null, "是否确认退出测试界面？", "提示",JOptionPane.YES_NO_OPTION);//返回的是按钮的index  i=0或者1
//				if (n==0)
//					System.exit(0);
//		      }
//		});

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
			scroll_analy.setPreferredSize(new Dimension(300, 50));
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
			scroll_err.setPreferredSize(new Dimension(300, 50));

			panel_child.add(scroll_err, BorderLayout.NORTH);

			JPanel jGridPanel = new JPanel();
			jGridPanel.setLayout(new GridLayout(1,1,1,1));
			panel_child.add(jGridPanel, BorderLayout.SOUTH);


			int rowHeight = 35;
			JPanel jPanel_r1 = new JPanel();
			jPanel_r1.setPreferredSize(new Dimension(300, rowHeight));
			jPanel_r1.setLayout(new FlowLayout());
			jGridPanel.add(jPanel_r1);

			jButton2 = new JButton("开始测试");
			jPanel_r1.add(jButton2);
			jButton2.addActionListener(e -> {
				new Thread(() -> {
					TerminalCheck terminalCheck = new TerminalCheck();
					terminalCheck.AllCheck(PlatFormParam.getInstance().getPlatFormCOM());
				}).start();
			});

		}
	}


	public static void main(String[] args) {
//		String str = "\na1\n221a\n313a\n";
//		System.out.println(str);
//		System.out.println(numOfStr(str,"\n"));
		PlatFormCheckUI.getInstance().showFrame("");

		// 1、Server_Utility调整串口服务器的IP地址，调整网段为129.1.22.XXX
		// 2、找一个万用表，可以进行遥信端子开合检测

		// 获取某个表位的某个通信接口的IP和端口
//		Object o = getIPParam("1","PS2")
//		IP = (String)o[0];
//		port = (int)o[1];
	}


}
