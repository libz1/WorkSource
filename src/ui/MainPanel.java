package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import base.BaseFrame;
import javafx.application.Platform;
import produce.deal.BarCodeCURD;
import produce.deal.BarCodesInfoMgr;
import produce.deal.DevList;
import produce.deal.FrameAssitant;
import produce.deal.ProduceCaseCURD;
import produce.deal.TerminalCURD;
import produce.deal.TerminalParameter;
import produce.deal.UserManagerCURD;
import util.SoftParameter;

public class MainPanel extends JPanel{
	JPanel panel;

	private JButton[] buttonArr;
	private JRadioButton[] rdbuttonArr;
	private ButtonGroup buttonGroup;
	private JLabel lb_ver;
	String ver ;

	public MainPanel(String ver){
		this.ver = ver;
		init();

		System.out.println("MainPanel init end");
		// xuky 2017.07.24 为了提高加载程序的速度，此段代码放在 MainPanel中
		new Thread(() -> {
			SoftParameter.getInstance().refreshDataFromDB();
		}).start();

	}

	private void init() {
		panel = this;
		panel.setOpaque(false);
		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setVisible(true);

		String show_msg = "ver:"+ver;

		show_msg += "  PCID:"+SoftParameter.getInstance().getPCID();


		lb_ver = new JLabel(show_msg, JLabel.CENTER);
		lb_ver.setBounds(550, 550, 200, 60);
		panel.add(lb_ver);

		// button = new JButton("弹出新窗口");
		ButtonListener buttonListener = new ButtonListener();

		buttonArr = new JButton[25];
		Font font = new Font("宋体", Font.BOLD, 14);
		for (int i = 0; i < 25; i++) { // 通过一个循环,对按钮数组中的每一个按钮实例化.
			buttonArr[i] = new JButton();
			buttonArr[i].setForeground(Color.white);
			buttonArr[i].setFont(font);
			panel.add(buttonArr[i]);
			buttonArr[i].addActionListener(buttonListener);
		}

		buttonArr[0].setText("II采生产测试");
		buttonArr[5].setText("通信服务器");
//		buttonArr[11].setText("辅助功能");

		int priority = SoftParameter.getInstance().getUserManager().getUserPriority();
		if (priority > 1){
			buttonArr[1].setText("条码信息导入和查询");
			buttonArr[11].setText("模拟回复");
			buttonArr[10].setText("用户管理");

			buttonArr[6].setText("测试用例管理");
			buttonArr[7].setText("串口参数及运行参数设置");

			buttonArr[8].setText("扫描条码与设备地址对照表");
			buttonArr[9].setText("设备信息查询");
		}
		else
			buttonArr[10].setText("修改密码");


		buttonArr[0].setBackground(new Color(247, 175, 47));
		buttonArr[1].setBackground(new Color(169, 51, 254));
		buttonArr[2].setBackground(new Color(223, 89, 71));
		buttonArr[3].setBackground(new Color(8, 152, 249));
		buttonArr[4].setBackground(new Color(247, 175, 47));
		buttonArr[5].setBackground(new Color(0, 72, 190));
		buttonArr[6].setBackground(new Color(123, 213, 55));
		buttonArr[7].setBackground(new Color(8, 152, 249));
		buttonArr[8].setBackground(new Color(169, 51, 254));
		// new Color(125, 130, 156)灰色

		buttonArr[9].setBackground(new Color(247, 175, 47));
		buttonArr[10].setBackground(new Color(0, 72, 190));
		buttonArr[11].setBackground(new Color(123, 213, 55));
		buttonArr[12].setBackground(buttonArr[1].getBackground());

		buttonArr[13].setBackground(buttonArr[5].getBackground());
		buttonArr[14].setBackground(buttonArr[5].getBackground());
		buttonArr[15].setBackground(buttonArr[5].getBackground());
		buttonArr[16].setBackground(buttonArr[5].getBackground());

		font = new Font("宋体", Font.BOLD, 14);
		buttonGroup = new ButtonGroup();
		rdbuttonArr = new JRadioButton[4];
		for (int i = 0; i < 4; i++) { // 通过一个循环,对按钮数组中的每一个按钮实例化.
			if (i == 0) {
				rdbuttonArr[i] = new JRadioButton("", true);
			} else {
				rdbuttonArr[i] = new JRadioButton("", false);
			}

			rdbuttonArr[i].setFont(font);
			rdbuttonArr[i].setForeground(new Color(0, 114, 198));
			buttonGroup.add(rdbuttonArr[i]);
			// xuky 2016.08.26 不显示rdbutton
			//panel.add(rdbuttonArr[i]);

			rdbuttonArr[i].addActionListener(buttonListener);

		}

		rdbuttonArr[0].setText("按功能展示");
		rdbuttonArr[1].setText("按操作对象");
		rdbuttonArr[2].setText("按操作流程分类");
		// rdbuttonArr[3].setText("按协议分类");

		rdbuttonArr[0].setBounds(20, 5, 126, 30);
		rdbuttonArr[1].setBounds(145, 5, 126, 30);
		rdbuttonArr[2].setBounds(270, 5, 135, 30);
		// rdbuttonArr[3].setBounds(405, 5, 135, 30);

		showFlowFun();

	}

	private void showFlowFun() {
		panel.setVisible(false);
		int HEIGHT = 73;
		int INTERVAL = 2;
		int WEIGHT = 220;
		int TOPX = 35;
		int TOPY = 30;

		for (int i = 12; i <= 16; i++) {
			buttonArr[i].setVisible(false);
		}

		// 左1
		int x = TOPX;
		int y = TOPY;
		buttonArr[0].setBounds(x, y, WEIGHT, HEIGHT * 2);

		// 左2
		y = y + HEIGHT * 2 + INTERVAL;
		buttonArr[1].setBounds(x, y, WEIGHT, HEIGHT);
		buttonArr[1].setVisible(true);

		// 左3
		y = y + HEIGHT + INTERVAL;
		buttonArr[2].setBounds(x, y, WEIGHT, HEIGHT * 2);

		// 左4
		y = y + HEIGHT * 2 + INTERVAL;
		buttonArr[7].setBounds(x, y, WEIGHT * 2, HEIGHT);

		// 中1
		y = TOPY;
		x = TOPX + WEIGHT + INTERVAL;
		buttonArr[3].setBounds(x, y, WEIGHT, HEIGHT);

		// 右1
		x = x + WEIGHT + INTERVAL;
		buttonArr[4].setBounds(x, y, WEIGHT, HEIGHT);

		// 中2
		x = TOPX + WEIGHT + INTERVAL;
		y = y + HEIGHT + INTERVAL;
		buttonArr[5].setBounds(x, y, WEIGHT * 2 + INTERVAL, HEIGHT * 3);
		buttonArr[5].setVisible(true);

		// 中3
		y = y + HEIGHT * 3 + INTERVAL;
		buttonArr[6].setBounds(x, y, WEIGHT * 2 + INTERVAL, HEIGHT);

		// 中4
		y = y + HEIGHT + INTERVAL;
		x = x + WEIGHT + INTERVAL;
		buttonArr[8].setBounds(x, y, WEIGHT, HEIGHT);

		y = y + HEIGHT + INTERVAL;
		x = TOPX;
		buttonArr[9].setBounds(x, y, WEIGHT, HEIGHT);
		x = x + WEIGHT + INTERVAL;
		buttonArr[10].setBounds(x, y, WEIGHT, HEIGHT);
		x = x + WEIGHT + INTERVAL;
		buttonArr[11].setBounds(x, y, WEIGHT, HEIGHT);
		panel.setVisible(true);
	}


	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String buttonName = e.getActionCommand();

			BaseFrame mainFrame = null;

			if (buttonName.equals("通信服务器")) {
				PrefixWindow.getInstance().showFrame(buttonName,120,510,800,200);
			}
			else if (buttonName.equals("模拟回复")) {
				FrameAssitant.getInstance(1).showFrame(buttonName);
			}
			else if (buttonName.equals("串口参数及运行参数设置")) {
				new TerminalCURD();
			}
			else if (buttonName.equals("用户管理")||buttonName.equals("修改密码")) {
				new UserManagerCURD();
			}
			else if (buttonName.equals("测试用例管理")) {
				new ProduceCaseCURD();
			}

			else if (buttonName.equals("扫描条码与设备地址对照表")) {
				new BarCodeCURD();
			}
			else if (buttonName.equals("II采生产测试")) {
				// xuky 2017.04.01 通过如下方式，实现窗口的放在最前
				Platform.runLater(() -> {
					// Platform.runLater解决 Not on FX application thread; currentThread = AWT-EventQueue-0
					TerminalParameter  terminalParameter = TerminalParameter.getInstance();
					// xuky 2017.04.06 在getInstance中进行setAlwaysOnTop前置操作，简化这里的外部程序调用
//					terminalParameter.setAlwaysOnTop(true);
//					terminalParameter.setAlwaysOnTop(false);
				});
			}
			else if (buttonName.equals("设备信息查询")) {
				// xuky 2017.04.01 通过如下方式，实现窗口的放在最前
				Platform.runLater(() -> {
					// Platform.runLater解决 Not on FX application thread; currentThread = AWT-EventQueue-0
					DevList  devList = DevList.getInstance();
					// xuky 2017.04.06 在getInstance中进行setAlwaysOnTop前置操作，简化这里的外部程序调用
//					terminalParameter.setAlwaysOnTop(true);
//					terminalParameter.setAlwaysOnTop(false);
				});
			}
			else if (buttonName.equals("条码信息导入和查询")) {
				Platform.runLater(() -> {
					BarCodesInfoMgr  barCodesInfoMgr = BarCodesInfoMgr.getInstance();
				});
			}
			else if (mainFrame!=null){
//				// 除“通信服务器”以外的其他的功能界面在这里控制显示
//				mainFrame.getPanel().setBounds(0, 0, width, height);
//				JFrame frame = new JFrame();
//				frame.setTitle(buttonName);
//				frame.setLayout(null);
//				frame.add(mainFrame.getPanel());
//				frame.setSize(width, height);
//				frame.setVisible(true);
//				DebugSwing.center(frame);
			}

		}
	}

}
