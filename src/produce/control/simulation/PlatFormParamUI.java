package produce.control.simulation;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import base.BaseFrame;

// ���������ô���   ���յ����ݺ��ҵ�ƥ������ݽ��лظ�
public class PlatFormParamUI extends BaseFrame {

	// xuky 2018.11.20 ����Ķ��󴴽�����Ҫִ�и�ֵ�������ᵼ�����Ѿ������õĶ������ݴ��ң�
	// ����private JButton jButton = null;
	private JTextField txt_PlatFormNO, txt_PlatFormCOM, txt_FSNum, txt_RT_MeterAddr, txt_RT_MeterAddr_NW ,txt_RT_JoinTime, txt_RT_LeftTime, txt_GPRS_IP, txt_GPRS_Port;
	private JComboBox<String> cb_TerminalModel, cb_ESAMCheckFlag, cb_ESAMModel, cb_RT_Protecol, cb_RT_AddFalg, cb_RT_ReadType, cb_RT_AddType;

	private PlatFormParam platFormParam = PlatFormParam.getInstance();
	private volatile static PlatFormParamUI uniqueInstance;

	public static PlatFormParamUI getInstance() {
		if (uniqueInstance == null) {
			synchronized (PlatFormParamUI.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new PlatFormParamUI();
				}
			}
		}
		return uniqueInstance;
	}

	@Override
	// �����ʼ��
	public void showFrame(String title) {
		super.showFrame(title);
		param2View();
	}

	private void param2View(){
		txt_PlatFormNO.setText(platFormParam.getPlatFormNO());
		txt_PlatFormCOM.setText(platFormParam.getPlatFormCOM());
		String str = platFormParam.getTerminalModel();
		if (str.equals("����"))
			cb_TerminalModel.setSelectedIndex(0);
		else
			cb_TerminalModel.setSelectedIndex(1);
		txt_FSNum.setText(platFormParam.getFSNum());
		str = platFormParam.getESAMCheckFlag();
		if (str.equals("0:����Ҫ"))
			cb_ESAMCheckFlag.setSelectedIndex(0);
		else
			cb_ESAMCheckFlag.setSelectedIndex(1);
		str = platFormParam.getESAMModel();
		if (str.equals("1:1376.1"))
			cb_ESAMModel.setSelectedIndex(0);
		else
			cb_ESAMModel.setSelectedIndex(1);
		txt_RT_MeterAddr.setText(platFormParam.getRT_MeterAddr());
		txt_RT_MeterAddr_NW.setText(platFormParam.getRT_MeterAddr_NW());
		str = platFormParam.getRT_Protecol();
		if (str.equals("00:����376.2"))
			cb_RT_Protecol.setSelectedIndex(0);
		else if (str.equals("01:�㶫16"))
			cb_RT_Protecol.setSelectedIndex(1);
		else
			cb_RT_Protecol.setSelectedIndex(2);
		str = platFormParam.getRT_AddFalg();
		if (str.equals("00:���ӱ�"))
			cb_RT_AddFalg.setSelectedIndex(0);
		else
			cb_RT_AddFalg.setSelectedIndex(1);
		txt_RT_JoinTime.setText(platFormParam.getRT_JoinTime());
		txt_RT_LeftTime.setText(platFormParam.getRT_LeftTime());
		str = platFormParam.getRT_ReadType();
		if (str.equals("00:AFN13_F1"))
			cb_RT_ReadType.setSelectedIndex(0);
		else
			cb_RT_ReadType.setSelectedIndex(1);
		str = platFormParam.getRT_AddType();
		if (str.equals("00:1376.2"))
			cb_RT_AddType.setSelectedIndex(0);
		else
			cb_RT_AddType.setSelectedIndex(1);
		txt_GPRS_IP.setText(platFormParam.getGPRS_IP());
		txt_GPRS_Port.setText(platFormParam.getGPRS_Port());
	}
	private void view2Param(){
		platFormParam.setPlatFormNO(txt_PlatFormNO.getText());
		platFormParam.setPlatFormCOM(txt_PlatFormCOM.getText());

		platFormParam.setTerminalModel((String)cb_TerminalModel.getSelectedItem());
		platFormParam.setFSNum(txt_FSNum.getText());
		platFormParam.setESAMCheckFlag((String)cb_ESAMCheckFlag.getSelectedItem());
		platFormParam.setESAMModel((String)cb_ESAMModel.getSelectedItem());
		platFormParam.setRT_MeterAddr(txt_RT_MeterAddr.getText());
		platFormParam.setRT_MeterAddr_NW(txt_RT_MeterAddr_NW.getText());
		platFormParam.setRT_Protecol((String)cb_RT_Protecol.getSelectedItem());
		platFormParam.setRT_AddFalg((String)cb_RT_AddFalg.getSelectedItem());
		platFormParam.setRT_JoinTime(txt_RT_JoinTime.getText());
		platFormParam.setRT_LeftTime(txt_RT_LeftTime.getText());
		platFormParam.setRT_ReadType((String)cb_RT_ReadType.getSelectedItem());
		platFormParam.setRT_AddType((String)cb_RT_AddType.getSelectedItem());
		platFormParam.setGPRS_IP(txt_GPRS_IP.getText());
		platFormParam.setGPRS_Port(txt_GPRS_Port.getText());

	}

	@Override
	// �����ʼ��
	protected void init() {
		int hgap = 20, vgap = 20;
		panel.setLayout(new BorderLayout(hgap, vgap));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		{
			txt_PlatFormNO = new JTextField("01");  // ̨����
			txt_PlatFormCOM = new JTextField("100");  // ̨��Ĵ��ڵ�ַ
			cb_TerminalModel = new JComboBox<String>(new String[]{"����", "����"});  // ������ģʽ(����������)
			txt_FSNum = new JTextField("6");  // ң�Ŷ������������Žڵ�
			cb_ESAMCheckFlag = new JComboBox<String>(new String[]{"0:����Ҫ","1:��Ҫ"});  // 1��ʾ����Ҫ����ESAM�Ĳ���
			cb_ESAMModel = new JComboBox<String>(new String[]{"1:1376.1", "2:698"});  // ESAM����(1-1376.1��2-698)
			txt_RT_MeterAddr = new JTextField("");  // ·�ɲ�����-���ַ���Զ����Ϊ6�ֽڣ�
			txt_RT_MeterAddr_NW = new JTextField("");  // ·�ɲ�����-���ַ���Զ����Ϊ6�ֽڣ�
			cb_RT_Protecol = new JComboBox<String>(new String[]{"00:����376.2", "01:�㶫16", "02:������չ"});  // ·�ɲ�����-Э������( 00-����376.2��01-�㶫16��02-������չ)
			cb_RT_AddFalg = new JComboBox<String>(new String[]{"00:���ӱ�", "01:�ӱ�"});  // ·�ɲ�����-�ӱ��־��00-���ӱ�01-�ӱ�
			txt_RT_JoinTime = new JTextField("0");  // ·�ɲ�����-����ʱ��(��)
			txt_RT_LeftTime = new JTextField("0");  // ·�ɲ�����-����ʱ��(��)
			cb_RT_ReadType = new JComboBox<String>(new String[]{"00:AFN13_F1", "01:AFN02_F1"});  // ·�ɲ�����-����ʽ(00-AFN13_F1��01-AFN02_F1)
			cb_RT_AddType = new JComboBox<String>(new String[]{"00:1376.2", "01:376.2"});  // ·�ɲ�����-�ӱ�ʽ(00-1376.2��01-376.2)
			txt_GPRS_IP = new JTextField("000.000.000.000");  // GPRS�������վIP��ַ
			txt_GPRS_Port = new JTextField("000");  // GPRS�������վ�˿ڵ�ַ

			JPanel jGridPanel = new JPanel();
			panel.add(jGridPanel, BorderLayout.CENTER);
			jGridPanel.setLayout(new GridLayout(0,2,10,10));

			jGridPanel.add(new JLabel("̨����"));
			jGridPanel.add(txt_PlatFormNO);

			jGridPanel.add(new JLabel("̨�崮�ڵ�ַ"));
			jGridPanel.add(txt_PlatFormCOM);

			jGridPanel.add(new JLabel("������ģʽ"));
			jGridPanel.add(cb_TerminalModel);

			jGridPanel.add(new JLabel("GPRS�������վIP��ַ"));
			jGridPanel.add(txt_GPRS_IP);
			jGridPanel.add(new JLabel("GPRS�������վ�˿ڵ�ַ"));
			jGridPanel.add(txt_GPRS_Port);

			jGridPanel.add(new JLabel("ң�Ŷ�������"));
			jGridPanel.add(txt_FSNum);
			jGridPanel.add(new JLabel("�Ƿ����ESAM����"));
			jGridPanel.add(cb_ESAMCheckFlag);
			jGridPanel.add(new JLabel("ESAM����"));
			jGridPanel.add(cb_ESAMModel);
			jGridPanel.add(new JLabel("·�ɲ�����-���ַ"));
			jGridPanel.add(txt_RT_MeterAddr);
			jGridPanel.add(new JLabel("·�ɲ�����-���ַ-����"));
			jGridPanel.add(txt_RT_MeterAddr_NW);
			jGridPanel.add(new JLabel("·�ɲ�����-Э������"));
			jGridPanel.add(cb_RT_Protecol);
			jGridPanel.add(new JLabel("·�ɲ�����-�ӱ��־"));
			jGridPanel.add(cb_RT_AddFalg);
			jGridPanel.add(new JLabel("·�ɲ�����-�����ȴ�ʱ��"));
			jGridPanel.add(txt_RT_JoinTime);
			jGridPanel.add(new JLabel("·�ɲ�����-�����ȴ�ʱ��"));
			jGridPanel.add(txt_RT_LeftTime);
			jGridPanel.add(new JLabel("·�ɲ�����-����ʽ"));
			jGridPanel.add(cb_RT_ReadType);
			jGridPanel.add(new JLabel("·�ɲ�����-�ӱ�ģʽ"));
			jGridPanel.add(cb_RT_AddType);

//			JPanel jPanel_r1 = new JPanel();
//			jPanel_r1.setPreferredSize(new Dimension(0, 60));
//			jPanel_r1.setLayout(new FlowLayout());
//			jGridPanel.add(jPanel_r1);
			JButton jButton1 = new JButton("����");
			jGridPanel.add(jButton1);
			jButton1.addActionListener(e -> {
				new Thread(() -> {
					view2Param();
					platFormParam.saveParam();
					frame.dispose();
				}).start();
			});
			JButton jButton2 = new JButton("ȡ��");
			jGridPanel.add(jButton2);
			jButton2.addActionListener(e -> {
				new Thread(() -> {
					frame.dispose();
				}).start();
			});

		}
	}

	public static void main(String[] args) {
		PlatFormParamUI.getInstance().showFrame("");
	}


}
