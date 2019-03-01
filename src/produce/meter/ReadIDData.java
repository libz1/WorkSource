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

// ���������ô���   ���յ����ݺ��ҵ�ƥ������ݽ��лظ�
public class ReadIDData extends BaseFrame implements Observer {

	// xuky 2018.11.20 ����Ķ��󴴽�����Ҫִ�и�ֵ�������ᵼ�����Ѿ������õĶ������ݴ��ң�
	// ����private JButton jButton = null;
	private JTextArea txt_qrcodes,txt_analyData, txt_errMsg;
	private String recentFrame;
	private JButton jButton;
	private String startMsg, nowMsg;

	private volatile static ReadIDData uniqueInstance;

	public static ReadIDData getInstance() {
		if (uniqueInstance == null) {
			synchronized (ReadIDData.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
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

		// �����ӿڴ����̣߳����߳����жϹ�������ID���������Ƿ����в����������
		Meter2MESThread.getInstance();

//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				Util698.log(ReadIDData.class.getName(), "�ر����"+title, Debug.LOG_INFO);
//			}
//		});

		// �ڽ��������һЩģ�����������
//		String[]  QRCodes = {"null-1230001","null-1230002","null-1230003","null-1230004"};
//		for( String str: QRCodes)
//			txt_qrcodes.setText(txt_qrcodes.getText()+str+"\n");

	}


	// 1���첽�������̣� ��4����Ҫ�Խӿڵ�update������������ʵ�֣�������Ϣ�����ߵ�����
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
	// 1���첽�������̣� ��5�����յ������ݽ���չʾ����Ϊ�漰��UI�������ܺ�ʱ�ϳ����������synchronized����֤���̲߳���ʱ�İ�ȫ��
	private synchronized void showData1(Object arg) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String comid = s[3];
		frame = frame.replaceAll(" ", "");
		frame = frame.replaceAll(",", "");
//		frame = Util698.seprateString(frame, " ");
		String showMsg = "COM-"+comid+" ��:" + frame ;
		Util698.log(ReadIDData.class.getName(), showMsg, Debug.LOG_INFO);
	}
	// 1���첽�������̣� ��5�����յ������ݽ���չʾ����Ϊ�漰��UI�������ܺ�ʱ�ϳ����������synchronized����֤���̲߳���ʱ�İ�ȫ��
	private synchronized void showData(Object arg) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String comid = s[3];
		frame = frame.replaceAll(" ", "");
		frame = frame.replaceAll(",", "");
//		frame = Util698.seprateString(frame, " ");
		String showMsg = "COM-"+comid+" ��:" + frame ;
		Util698.log(ReadIDData.class.getName(), showMsg, Debug.LOG_INFO);

		// xuky 2018.11.13 ��Ҫ�������ݻظ�
		Boolean is_testRusult = false,is_StartScan = false;
		if (frame.startsWith("689999999999996814")){
			if (frame.indexOf("FFFFEE01") >= 0)
				is_testRusult = true;
			if (frame.indexOf("FFFFEE02") >= 0)
				is_StartScan = true;
		}

		String reply = "6899999999999968940000FA16";
		SocketServerEast.sendData(reply, comid);
		showMsg = "COM-"+comid+" ��:" + reply;
		Util698.log(ReadIDData.class.getName(), showMsg, Debug.LOG_INFO);

		if (is_StartScan){
			detect_change_New();
			return;
		}
		if (!is_testRusult){
			Util698.log(ReadIDData.class.getName(), "�յ������ݲ����¸�ʽ���ģ����账��", Debug.LOG_INFO);
			return;
		}

		// �����ñ��ľ���
		//6899999999999968140054FFFFEE01000101020002011801029C01C1FB0245534131000001ACBF092E43B981B1E2480206112233445566030001011801029C01C1FB0245534131000001AD4E60A617726EA74BD1040001020A0102030405060708090A3316
		// �������ݴ���ǰ�����֮ǰ�յ��Ķ�ά����Ϣ

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
			msg +=  "ֹͣ������������ɨ���ά����Ϣ������������������4����ǰ��"+num+"!\n";
			msg += "--------------------------\n";
			msg += str;
			msg += "--------------------------\n";
			msg += "�յ�����:\n"+frame;
			if (QRCodes.equals("")){
				msg =  "ֹͣ������������ɨ���ά����Ϣ������������������4����ǰ�ǿ�����!";
			}
			txt_errMsg.setText(msg);
			Util698.log(ReadIDData.class.getName(), msg, Debug.LOG_INFO);
			return;
		}

		Util698.log(ReadIDData.class.getName(), "ɨ�赽�Ķ�ά����Ϣ��:\n"+str, Debug.LOG_INFO);

		// xuky 2018.11.13 ��¼����յ��ı��ģ������ͬ����ֻ�ǽ��д��ڻظ�ȷ�ϴ��������к�������
		if (recentFrame != null){
			if ( recentFrame.equals(frame)){
				String msg = "�յ����ظ��ı��ģ������н������洢����\n"+recentFrame;
				txt_errMsg.setText(msg);
				Util698.log(ReadIDData.class.getName(), msg, Debug.LOG_INFO);
				return;
			}
		}
		recentFrame = frame;
//		txt_analy.setText(showMsg);


//		String[]  QRCodes = {"1230001","1230002","1230003","1230004"};


		showMsg = "��װ�ϱ�����Ϊ:\n"+ frame+"\n";

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
	// �����ʼ��
	protected void init() {
		// ���ڼ�¼ɨ�赽�Ķ�ά��
		int hgap = 3, vgap = 3;
		panel.setLayout(new BorderLayout(hgap, vgap));
		{
			txt_qrcodes = new JTextArea("");
			txt_qrcodes.setLineWrap(true);// �����Զ����й���
			txt_qrcodes.setWrapStyleWord(true);// ������в����ֹ���
			txt_qrcodes.setFont(new Font(Font.DIALOG_INPUT,Font.BOLD,14));

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
//			panel_child.setPreferredSize(new Dimension(300, 450));
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
			txt_errMsg.setFont(new Font(Font.DIALOG_INPUT,Font.BOLD,14));
			// ������ʾ�����壬��Ч
//			txt_errMsg.setForeground(Color.RED);
//			txt_errMsg.setBackground(Color.RED);


			// �������ı���ؼ���ӵ�JScrollPane������
			JScrollPane scroll_err = new JScrollPane(txt_errMsg);
			scroll_err.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll_err.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scroll_err.setPreferredSize(new Dimension(300, 170));

			panel_child.add(scroll_err, BorderLayout.NORTH);

			JPanel jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout());
//			jPanel.setSize(100, 800);
			panel_child.add(jPanel, BorderLayout.SOUTH);

			startMsg = "��ʼ��ȡɨ��ͷ����";
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

	// �ж�¼����е����ݣ�����յ��������룬��ʼ����ɨ��ͷ����
	private void detect_startCode() {
		String recv_txt = "", startqcode = "";
		while( true){
			startqcode = SoftParameter.getInstance().getSTARTQCODE();
			if (startqcode != null){
				if (!startqcode.equals("")){
					recv_txt = txt_qrcodes.getText();
					if (recv_txt.equals(startqcode+"\n")){
						// �������
						txt_qrcodes.setText("");
						// ��ʼ����ɨ��ͷ����
						new Thread(() -> {
							detect_change_New();
						}).start();
						// �����־
						Util698.log(ReadIDData.class.getName(), "detect_startCode �յ���������:"+recv_txt, Debug.LOG_INFO);
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
	// ������5��Ϊ�����5����һ��Ҫ�յ����ݣ������Զ������У��ܹ���4������ȫ��
	private void detect_change_New0(){


		// ���ݰ�ť����ʾ��Ϣ�ж�״̬������Ѿ����ڵȴ�״̬�����˳�
		if ((nowMsg.indexOf(startMsg)) < 0 && !nowMsg.equals("�������!")){
			txt_errMsg.setText("���ڵȴ���������...");
			Util698.log(ReadIDData.class.getName(), "ɨ������У��û��ظ������ť��return", Debug.LOG_INFO);
			return;
		}

		txt_errMsg.setText(startMsg);

		// ��������Դ�ڡ����в��������������ý����е�
		int interval = DataConvert.String2Int(SoftParameter.getInstance().getTESTALL_TIME_OUT());
		nowMsg = "�ȴ���������...�������Ϊ"+interval+"(����)";
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
				Util698.log(ReadIDData.class.getName(), "�����"+i+"�μ��,��⵽"+num+"������,����Ϊ:\n"+text, Debug.LOG_INFO);
				if ( num == 4){
					// �������ﵽ��ָ���������������˳����ѭ��
					break;
				}
				if ( num < i){
					txt_qrcodes.setText(text+"null-"+i+"\n");
					txt_qrcodes.setCaretPosition(txt_qrcodes.getText().length());
					Util698.log(ReadIDData.class.getName(), "��ⲻ��꣬��ӿ�����(null-"+i+")", Debug.LOG_INFO);
				}
				begin = Util698.getDateTimeSSS_new();
			}
			if ( val > 1000){
				text = txt_qrcodes.getText();
				num = (Util698.numOfStr(text, "\n"));
				if ( num == 4){
					// �������ﵽ��ָ���������������˳����ѭ��
					break;
				}
			}

			Debug.sleep(50);
		}

		Util698.log(ReadIDData.class.getName(), "�������!���ս��Ϊ:\n"+txt_qrcodes.getText(), Debug.LOG_INFO);
		jButton.setText(startMsg);
		nowMsg = "�������!";
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
