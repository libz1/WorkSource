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

// ���������ô���   ���յ����ݺ��ҵ�ƥ������ݽ��лظ�

// ����645   2400  �������
// ·����376.2  9600
// ��ȡоƬID��  ��ɨ�赽��������Ϣ   �������й��������ݽ��д洢
// ͨ�����ʲ�ͨ���û�������ǰ��Ҫ���д��ڲ�������

public class WriteIDData extends BaseFrame implements Observer {

	// xuky 2018.11.20 ����Ķ��󴴽�����Ҫִ�и�ֵ�������ᵼ�����Ѿ������õĶ������ݴ��ң�
	// ����private JButton jButton = null;
	private JTextArea txt_qrcodes, txt_analyData, txt_errMsg;
	private JButton jButton;

	private volatile static WriteIDData uniqueInstance;

	public static WriteIDData getInstance() {
		if (uniqueInstance == null) {
			synchronized (WriteIDData.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
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

	// 1���첽�������̣� ��4����Ҫ�Խӿڵ�update������������ʵ�֣�������Ϣ�����ߵ�����
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

	// 1���첽�������̣� ��5�����յ������ݽ���չʾ����Ϊ�漰��UI�������ܺ�ʱ�ϳ����������synchronized����֤���̲߳���ʱ�İ�ȫ��
	private synchronized void showData(Object arg) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String comid = s[3];
		frame = frame.replaceAll(" ", "");
		frame = frame.replaceAll(",", "");
		// frame = Util698.seprateString(frame, " ");
		String showMsg = "COM-" + comid + " ��:" + frame;
		Util698.log(WriteIDData.class.getName(), showMsg, Debug.LOG_INFO);

		if (frame.indexOf("AAAAAAAAAAAA") >= 0 && frame.indexOf("6817004345") >= 0) {
			// FE FE FE FE 68 17 00 43 45 AA AA AA AA AA AA 00 5B 4F 05 01 01 40
			// 01 02 00 00 C6 07 16
			String reply = "68 21 00 C3 05 13 30 04 22 17 20 10 7D A0 85 01 01 40 01 02 00 01 09 06 20 17 22 04 30 13 00 00 4E EB 16";
			SocketServerEast.sendData(reply, comid);
		}

	}

	@Override
	// �����ʼ��
	protected void init() {
		// ���ڼ�¼ɨ�赽�Ķ�ά��
		int hgap = 3, vgap = 3;
		panel.setLayout(new BorderLayout(hgap, vgap));
		{
			txt_qrcodes = new JTextArea("");
			txt_qrcodes.setLineWrap(true);// �����Զ����й���
			txt_qrcodes.setWrapStyleWord(true);// ������в����ֹ���
			txt_qrcodes.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));

			// �������ı���ؼ���ӵ�JScrollPane������
			JScrollPane scroll_analy = new JScrollPane(txt_qrcodes);
			scroll_analy.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll_analy.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scroll_analy.setPreferredSize(new Dimension(300, 130));
			panel.add(scroll_analy, BorderLayout.NORTH);

		}
		// ����չʾ��������Լ���Ӳ����õĿ��ư�ť
		{
			JPanel panel_child;
			panel_child = new JPanel();
			panel_child.setLayout(new BorderLayout(hgap, vgap));
			// panel_child.setPreferredSize(new Dimension(300, 450));
			panel.add(panel_child, BorderLayout.CENTER);

			txt_analyData = new JTextArea("");
			txt_analyData.setLineWrap(true);// �����Զ����й���
			txt_analyData.setWrapStyleWord(true);// ������в����ֹ���

			// �������ı���ؼ���ӵ�JScrollPane������
			JScrollPane scroll_analy = new JScrollPane(txt_analyData);
			scroll_analy.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll_analy.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			panel_child.add(scroll_analy, BorderLayout.CENTER);

			txt_errMsg = new JTextArea("");
			txt_errMsg.setLineWrap(true);// �����Զ����й���
			txt_errMsg.setWrapStyleWord(true);// ������в����ֹ���

			// ���������С https://bbs.csdn.net/topics/370192751
			txt_errMsg.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
			// ������ʾ�����壬��Ч
			// txt_errMsg.setForeground(Color.RED);
			// txt_errMsg.setBackground(Color.RED);

			// �������ı���ؼ���ӵ�JScrollPane������
			JScrollPane scroll_err = new JScrollPane(txt_errMsg);
			scroll_err.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll_err.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scroll_err.setPreferredSize(new Dimension(300, 170));

			panel_child.add(scroll_err, BorderLayout.NORTH);

			JPanel jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout());
			// jPanel.setSize(100, 800);
			panel_child.add(jPanel, BorderLayout.SOUTH);

			jButton = new JButton("���ͱ���");
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

		// ��������
		// �ȴ����ݻظ�
	}

	private String getUseableCOM() {
		String comid = "";
		List<MinaSerialServer> serials = PrefixMain.getInstance().getSerialServers();
		if (serials.size() != 0) {
			// ���ֻ��һ��
			if (serials.size() == 1) {
				comid = serials.get(0).getComID();
			} else {
				// ������ҵ���ΪRT��
				for (MinaSerialServer m : serials)
					if (m.getName().equals("RT"))
						comid = m.getComID();
			}
			// ȡ�õ�һ��
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
				// �����־
				String msg = "ɨ���յ�����:" + recv_txt;
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
