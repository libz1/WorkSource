package produce.meter.write;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.eastsoft.util.Debug;

import base.BaseFrame;
import mina.MinaSerialServer;
import produce.meter.ReadIDData;
import socket.DealData;
import socket.PrefixMain;
import socket.SocketServerEast;
import util.Frame645New;
import util.Publisher;
import util.PublisherFrame;
import util.Util698;

// 辅助测试用窗口   接收到数据后，找到匹配的数据进行回复

// 表是645   2400  正面放置
// 路由是376.2  9600
// 读取芯片ID，  与扫描到的条码信息   两两进行关联，数据进行存储
// 通信速率不通，用户在生产前需要进行串口参数调整

public class WriteIDData extends BaseFrame implements Observer {

	// xuky 2018.11.20 这里的对象创建，不要执行赋值操作，会导数据已经创建好的对象，数据错乱！
	// 例如private JButton jButton = null;
	private JTextArea txt_qrcodes, txt_analyData, txt_errMsg;
	private JButton jButton;

	private volatile static WriteIDData uniqueInstance;

	public static WriteIDData getInstance() {
		if (uniqueInstance == null) {
			synchronized (WriteIDData.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new WriteIDData();
					Publisher.getInstance().addObserver(uniqueInstance);
				}
			}
		}
		return uniqueInstance;
	}

	public WriteIDData() {

		PublisherFrame.getInstance().addObserver(this);
		DealData.getInstance();

	}

	// 1、异步交互过程， （4）需要对接口的update函数进行自行实现，接收消息发布者的数据
	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		if (s[0].equals("recv frame") && s[1].equals("user data")) {
			try {
				showData(arg);
			} catch (Exception e) {
				Util698.log(WriteIDData.class.getName(), "showData Exception:" + e.getMessage(), Debug.LOG_INFO);
			}
		}
	}

	// 1、异步交互过程， （5）对收到的数据进行展示，因为涉及到UI操作可能耗时较长，所以添加synchronized，保证多线程操作时的安全性
	private synchronized void showData(Object arg) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String comid = s[3];
		frame = frame.replaceAll(" ", "");
		frame = frame.replaceAll(",", "");
		// frame = Util698.seprateString(frame, " ");
		String showMsg = "COM-" + comid + " 收:" + frame;
		Util698.log(WriteIDData.class.getName(), showMsg, Debug.LOG_INFO);

		if (frame.indexOf("AAAAAAAAAAAA") >= 0 && frame.indexOf("6817004345") >= 0) {
			// FE FE FE FE 68 17 00 43 45 AA AA AA AA AA AA 00 5B 4F 05 01 01 40
			// 01 02 00 00 C6 07 16
			String reply = "68 21 00 C3 05 13 30 04 22 17 20 10 7D A0 85 01 01 40 01 02 00 01 09 06 20 17 22 04 30 13 00 00 4E EB 16";
			SocketServerEast.sendData(reply, comid);
		}

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
			txt_qrcodes.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));

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
			// panel_child.setPreferredSize(new Dimension(300, 450));
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
			txt_errMsg.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
			// 调整显示的字体，无效
			// txt_errMsg.setForeground(Color.RED);
			// txt_errMsg.setBackground(Color.RED);

			// 将多行文本框控件添加到JScrollPane对象中
			JScrollPane scroll_err = new JScrollPane(txt_errMsg);
			scroll_err.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll_err.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scroll_err.setPreferredSize(new Dimension(300, 170));

			panel_child.add(scroll_err, BorderLayout.NORTH);

			JPanel jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout());
			// jPanel.setSize(100, 800);
			panel_child.add(jPanel, BorderLayout.SOUTH);

			jButton = new JButton("发送报文");
			jPanel.add(jButton);
		}
		jButton.addActionListener(e -> {
			new Thread(() -> {
				manual_send();
			}).start();
		});

		new Thread(() -> {
			detect_startCode();
		}).start();

		txt_qrcodes.setText("4330070401000000000001");

		txt_errMsg.setText("68 13 30 04 22 17 20 68 1F 00 8F 16");
	}

	private void manual_send() {
		String comid = getUseableCOM();
		String send = txt_errMsg.getText();

		SocketServerEast.sendData(send, comid);

		// 发送数据
		// 等待数据回复
	}

	private String getUseableCOM() {
		String comid = "";
		List<MinaSerialServer> serials = PrefixMain.getInstance().getSerialServers();
		if (serials.size() != 0) {
			// 如果只有一个
			if (serials.size() == 1) {
				comid = serials.get(0).getComID();
			} else {
				// 如果能找到名为RT的
				for (MinaSerialServer m : serials)
					if (m.getName().equals("RT"))
						comid = m.getComID();
			}
			// 取用第一个
			if (comid.equals(""))
				comid = serials.get(0).getComID();
		}
		return comid;
	}

	private void detect_startCode() {
		String recv_txt = "", startqcode = "";
		while (true) {
			recv_txt = txt_qrcodes.getText();
			if (recv_txt.indexOf("\n") >= 0) {
				txt_qrcodes.setText("");
				// 输出日志
				String msg = "扫描收到数据:" + recv_txt;
				Util698.log(ReadIDData.class.getName(), "detect_startCode " + msg, Debug.LOG_INFO);
				txt_analyData.setText(msg);

				Frame645New frame645 = new Frame645New();
				frame645.setControl("1F");
				frame645.setAddr("201722043013");
				frame645.setData_item("50545354");
				frame645.setData_data(recv_txt.substring(0, recv_txt.length() - 1) + "0B");
				String frame = frame645.get645Frame();
				String comid = getUseableCOM();
				SocketServerEast.sendData(frame, comid);

			}
			Debug.sleep(50);
		}

	}

	public static void main(String[] args) {
		WriteIDData.getInstance().showFrame("");
	}

}
