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

// ���������ô���   ���յ����ݺ��ҵ�ƥ������ݽ��лظ�
public class PlatFormCheckUI extends BaseFrame implements Observer {

	// xuky 2018.11.20 ����Ķ��󴴽�����Ҫִ�и�ֵ�������ᵼ�����Ѿ������õĶ������ݴ��ң�
	// ����private JButton jButton = null;
	private JTextArea txt_qrcodes,txt_analyData, txt_errMsg;
	private JButton jButton,jButton1,jButton2,jButton3,jButton4,jButton5,jButton6,jButton7,jButton8,jButton9,jButton10;
	JTextField input_meterno, input_com, input_voltage, input_current, input_FSFlag;

	private volatile static PlatFormCheckUI uniqueInstance;

	public static PlatFormCheckUI getInstance() {
		if (uniqueInstance == null) {
			synchronized (PlatFormCheckUI.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new PlatFormCheckUI();
				}
			}
		}
		return uniqueInstance;
	}


	public PlatFormCheckUI() {
		Publisher.getInstance().addObserver(this);
	}


	// 1���첽�������̣� ��4����Ҫ�Խӿڵ�update������������ʵ�֣�������Ϣ�����ߵ�����
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
	// 1���첽�������̣� ��5�����յ������ݽ���չʾ����Ϊ�漰��UI�������ܺ�ʱ�ϳ����������synchronized����֤���̲߳���ʱ�İ�ȫ��
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
	// �����ʼ��
	protected void init() {
		// xuky 2019.02.16  �رմ˴���ʱ�ر������Ľ���
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		// ����ر�ǰ��Ҫ��ʾ���������������
//		frame.addWindowListener(new WindowAdapter(){
//			public void windowClosing(WindowEvent e) {
//				int n = JOptionPane.showConfirmDialog(null, "�Ƿ�ȷ���˳����Խ��棿", "��ʾ",JOptionPane.YES_NO_OPTION);//���ص��ǰ�ť��index  i=0����1
//				if (n==0)
//					System.exit(0);
//		      }
//		});

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
			scroll_analy.setPreferredSize(new Dimension(300, 50));
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

			jButton2 = new JButton("��ʼ����");
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

		// 1��Server_Utility�������ڷ�������IP��ַ����������Ϊ129.1.22.XXX
		// 2����һ�����ñ����Խ���ң�Ŷ��ӿ��ϼ��

		// ��ȡĳ����λ��ĳ��ͨ�Žӿڵ�IP�Ͷ˿�
//		Object o = getIPParam("1","PS2")
//		IP = (String)o[0];
//		port = (int)o[1];
	}


}
