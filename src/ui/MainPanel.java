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
		// xuky 2017.07.24 Ϊ����߼��س�����ٶȣ��˶δ������ MainPanel��
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

		// button = new JButton("�����´���");
		ButtonListener buttonListener = new ButtonListener();

		buttonArr = new JButton[25];
		Font font = new Font("����", Font.BOLD, 14);
		for (int i = 0; i < 25; i++) { // ͨ��һ��ѭ��,�԰�ť�����е�ÿһ����ťʵ����.
			buttonArr[i] = new JButton();
			buttonArr[i].setForeground(Color.white);
			buttonArr[i].setFont(font);
			panel.add(buttonArr[i]);
			buttonArr[i].addActionListener(buttonListener);
		}

		buttonArr[0].setText("II����������");
		buttonArr[5].setText("ͨ�ŷ�����");
//		buttonArr[11].setText("��������");

		int priority = SoftParameter.getInstance().getUserManager().getUserPriority();
		if (priority > 1){
			buttonArr[1].setText("������Ϣ����Ͳ�ѯ");
			buttonArr[11].setText("ģ��ظ�");
			buttonArr[10].setText("�û�����");

			buttonArr[6].setText("������������");
			buttonArr[7].setText("���ڲ��������в�������");

			buttonArr[8].setText("ɨ���������豸��ַ���ձ�");
			buttonArr[9].setText("�豸��Ϣ��ѯ");
		}
		else
			buttonArr[10].setText("�޸�����");


		buttonArr[0].setBackground(new Color(247, 175, 47));
		buttonArr[1].setBackground(new Color(169, 51, 254));
		buttonArr[2].setBackground(new Color(223, 89, 71));
		buttonArr[3].setBackground(new Color(8, 152, 249));
		buttonArr[4].setBackground(new Color(247, 175, 47));
		buttonArr[5].setBackground(new Color(0, 72, 190));
		buttonArr[6].setBackground(new Color(123, 213, 55));
		buttonArr[7].setBackground(new Color(8, 152, 249));
		buttonArr[8].setBackground(new Color(169, 51, 254));
		// new Color(125, 130, 156)��ɫ

		buttonArr[9].setBackground(new Color(247, 175, 47));
		buttonArr[10].setBackground(new Color(0, 72, 190));
		buttonArr[11].setBackground(new Color(123, 213, 55));
		buttonArr[12].setBackground(buttonArr[1].getBackground());

		buttonArr[13].setBackground(buttonArr[5].getBackground());
		buttonArr[14].setBackground(buttonArr[5].getBackground());
		buttonArr[15].setBackground(buttonArr[5].getBackground());
		buttonArr[16].setBackground(buttonArr[5].getBackground());

		font = new Font("����", Font.BOLD, 14);
		buttonGroup = new ButtonGroup();
		rdbuttonArr = new JRadioButton[4];
		for (int i = 0; i < 4; i++) { // ͨ��һ��ѭ��,�԰�ť�����е�ÿһ����ťʵ����.
			if (i == 0) {
				rdbuttonArr[i] = new JRadioButton("", true);
			} else {
				rdbuttonArr[i] = new JRadioButton("", false);
			}

			rdbuttonArr[i].setFont(font);
			rdbuttonArr[i].setForeground(new Color(0, 114, 198));
			buttonGroup.add(rdbuttonArr[i]);
			// xuky 2016.08.26 ����ʾrdbutton
			//panel.add(rdbuttonArr[i]);

			rdbuttonArr[i].addActionListener(buttonListener);

		}

		rdbuttonArr[0].setText("������չʾ");
		rdbuttonArr[1].setText("����������");
		rdbuttonArr[2].setText("���������̷���");
		// rdbuttonArr[3].setText("��Э�����");

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

		// ��1
		int x = TOPX;
		int y = TOPY;
		buttonArr[0].setBounds(x, y, WEIGHT, HEIGHT * 2);

		// ��2
		y = y + HEIGHT * 2 + INTERVAL;
		buttonArr[1].setBounds(x, y, WEIGHT, HEIGHT);
		buttonArr[1].setVisible(true);

		// ��3
		y = y + HEIGHT + INTERVAL;
		buttonArr[2].setBounds(x, y, WEIGHT, HEIGHT * 2);

		// ��4
		y = y + HEIGHT * 2 + INTERVAL;
		buttonArr[7].setBounds(x, y, WEIGHT * 2, HEIGHT);

		// ��1
		y = TOPY;
		x = TOPX + WEIGHT + INTERVAL;
		buttonArr[3].setBounds(x, y, WEIGHT, HEIGHT);

		// ��1
		x = x + WEIGHT + INTERVAL;
		buttonArr[4].setBounds(x, y, WEIGHT, HEIGHT);

		// ��2
		x = TOPX + WEIGHT + INTERVAL;
		y = y + HEIGHT + INTERVAL;
		buttonArr[5].setBounds(x, y, WEIGHT * 2 + INTERVAL, HEIGHT * 3);
		buttonArr[5].setVisible(true);

		// ��3
		y = y + HEIGHT * 3 + INTERVAL;
		buttonArr[6].setBounds(x, y, WEIGHT * 2 + INTERVAL, HEIGHT);

		// ��4
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

			if (buttonName.equals("ͨ�ŷ�����")) {
				PrefixWindow.getInstance().showFrame(buttonName,120,510,800,200);
			}
			else if (buttonName.equals("ģ��ظ�")) {
				FrameAssitant.getInstance(1).showFrame(buttonName);
			}
			else if (buttonName.equals("���ڲ��������в�������")) {
				new TerminalCURD();
			}
			else if (buttonName.equals("�û�����")||buttonName.equals("�޸�����")) {
				new UserManagerCURD();
			}
			else if (buttonName.equals("������������")) {
				new ProduceCaseCURD();
			}

			else if (buttonName.equals("ɨ���������豸��ַ���ձ�")) {
				new BarCodeCURD();
			}
			else if (buttonName.equals("II����������")) {
				// xuky 2017.04.01 ͨ�����·�ʽ��ʵ�ִ��ڵķ�����ǰ
				Platform.runLater(() -> {
					// Platform.runLater��� Not on FX application thread; currentThread = AWT-EventQueue-0
					TerminalParameter  terminalParameter = TerminalParameter.getInstance();
					// xuky 2017.04.06 ��getInstance�н���setAlwaysOnTopǰ�ò�������������ⲿ�������
//					terminalParameter.setAlwaysOnTop(true);
//					terminalParameter.setAlwaysOnTop(false);
				});
			}
			else if (buttonName.equals("�豸��Ϣ��ѯ")) {
				// xuky 2017.04.01 ͨ�����·�ʽ��ʵ�ִ��ڵķ�����ǰ
				Platform.runLater(() -> {
					// Platform.runLater��� Not on FX application thread; currentThread = AWT-EventQueue-0
					DevList  devList = DevList.getInstance();
					// xuky 2017.04.06 ��getInstance�н���setAlwaysOnTopǰ�ò�������������ⲿ�������
//					terminalParameter.setAlwaysOnTop(true);
//					terminalParameter.setAlwaysOnTop(false);
				});
			}
			else if (buttonName.equals("������Ϣ����Ͳ�ѯ")) {
				Platform.runLater(() -> {
					BarCodesInfoMgr  barCodesInfoMgr = BarCodesInfoMgr.getInstance();
				});
			}
			else if (mainFrame!=null){
//				// ����ͨ�ŷ�����������������Ĺ��ܽ��������������ʾ
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
