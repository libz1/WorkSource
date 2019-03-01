package produce.deal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.net.telnet.TelnetClient;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import javafx.base.javafxutil;
import util.SoftParameter;
import util.Util698;

public class TerminalTelnetSingle {
	private volatile static TerminalTelnetSingle uniqueInstance;
	private TelnetClient TCLIENT;
	private InputStream IN;
	private OutputStream OS;
	private String READSTR = ""; // ��¼����յ������ݣ��Դ˿����жϵ�ǰ��telnet����ĳ��״̬�����ڽ��к����Ķ�Ӧ����
	private Boolean need_reConnet = false ; //xuky 2017.12.20 �Ƿ���Ҫ�������ӵı�־
	private String IP;
	// private Boolean degugMode = false;
	private String OLDCMD = "";
	private Boolean NOVERIFY = false; // �Ƿ���Ҫ��֤�ı����Ϣ

	// xuky 2017.12.18 ���õȴ�ʱ��Ϊ2.4����Ч���޸�Ϊ1.5�루�ⲿĬ�ϵĵȴ�ʱ��Ϊ2.5�룩
	// xuky 2017.12.19 ��Ϊ��Ҫ��ʱ���ԣ�����ִ�д˴��루2/2��
//	TCLIENT.setDefaultTimeout(1000);
	// xuky 2017.12.21 ���ֳ�ʱ���
	// xuky 2018.01.08 dnʱ���ֳ�ʱ���
	private int TIMEOUT_USE = 1500;

	Boolean RUNFASTER = false;

	// public String getReadStr() {
	// return readStr;
	// }
	public static TerminalTelnetSingle getInstance(String terminalIP) {
		if (uniqueInstance == null) {
			synchronized (TerminalTelnetSingle.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new TerminalTelnetSingle(terminalIP);
				}
			}
		}
		return uniqueInstance;
	}
	public static TerminalTelnetSingle getInstance(String terminalIP,int timeout) {
		if (uniqueInstance == null) {
			synchronized (TerminalTelnetSingle.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new TerminalTelnetSingle(terminalIP,timeout);
				}
			}
		}
		return uniqueInstance;
	}

	private TerminalTelnetSingle(String terminalIP) {
		IP = terminalIP;
		READSTR = "";

		root();
	}

	private TerminalTelnetSingle(String terminalIP,int timeout) {
		IP = terminalIP;
		READSTR = "";
		TIMEOUT_USE = timeout;
		root();
	}

	public void destroy() {
		if (TCLIENT != null)
			try {
				TCLIENT.disconnect();
				uniqueInstance = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
	}


	public int getTIMEOUT_USE() {
		return TIMEOUT_USE;
	}

	public void setTIMEOUT_USE(int tIMEOUT_USE) {
		TIMEOUT_USE = tIMEOUT_USE;
	}


	// �����նˣ���¼root�û�
	public Boolean root() {

		if (RUNFASTER) return true;

		Boolean ret = false;
		try {

			// xuky 2017.12.19 �����µ�֮ǰ���ͷ�֮ǰ��
			// xuky 2017.12.19 ��Ϊ��Ҫ��ʱ���ԣ�����ִ�д˴��루1/2��
			if (TCLIENT != null)
				TCLIENT.disconnect();
			TCLIENT = new TelnetClient();

			// xuky 2018.01.08 ʹ��ϵͳĬ�ϵĳ�ʱʱ��
			if (TIMEOUT_USE != 0)
				TCLIENT.setDefaultTimeout(TIMEOUT_USE);

			Util698.log(TerminalTelnetSingle.class.getName(), "root =>�� connect... " + IP + "��", Debug.LOG_INFO);

			TCLIENT.connect(IP, 23);  // Ĭ��telnet�˿ں�

			IN = TCLIENT.getInputStream();
			OS = TCLIENT.getOutputStream();
			if (readUntil(":", IN).equals(""))
				ret = false;
			else {
				// д���û�����Ϣ root
				writeUtil("root", OS);
				if (readUntil("[root@(none) /]#", IN).equals(""))
					ret = false;
				else
					ret = true;
			}
			Util698.log(TerminalTelnetSingle.class.getName(), "root �� " + ret + "��", Debug.LOG_INFO);
		} catch (Exception e) {
			String errMsg = e.getMessage();
			if (errMsg.indexOf("timed out") >= 0 || errMsg.indexOf("refuse") >= 0) {
//				javafxutil.f_alert_informationDialog("������ʾ", "�޷�����" + IP + "�նˣ�����");
//				Util698.log(TerminalTelnetSingle.class.getName(), "root Exception => �� �޷�����" + IP + "�նˣ����顿",
//						Debug.LOG_INFO);
			} else {
				e.printStackTrace();
				Util698.log(TerminalTelnetSingle.class.getName(), "root Exception =>�� " + e.getMessage() + "��",
						Debug.LOG_INFO);
			}
			ret = false;
		}
		return ret;

	}

	// ����������ó���
	public Boolean init_dev() {
		Util698.log(TerminalTelnetSingle.class.getName(), "init_dev ��ʼ...", Debug.LOG_INFO);
		// ͨ�����²���������֤
		Boolean ret = false;
		if (READSTR.endsWith("[root@(none) /]#")) {
			Util698.log(TerminalTelnetSingle.class.getName(), "��֤�Ƿ���Ч", Debug.LOG_INFO);
			writeUtil("init_dev", OS);
			// xuky 2017.12.14 ����readUntil�ķ���ֵ�ж��Ƿ�ִ������Ч��
			if (readUntil("input the choice>", IN).equals(""))
				ret = false;
			else
				ret = true;
		} else if (READSTR.endsWith("input the choice>")) {
			// xuky 2017.12.20 Ϊ�����ִ�е�Ч�ʣ�ɾ���˴��Ĳ�����
			// xuky 2017.12.20 Ϊ�����ִ�е�Ч�ʣ���ִ�����ʱ���������readUntil���������ɾ��READSTR�Ĳ���
//			Util698.log(TerminalTelnetSingle.class.getName(), "��֤�Ƿ���Ч", Debug.LOG_INFO);
//			writeUtil("88", OS);
//			if (readUntil("input the choice>", IN).equals(""))
//				ret = false;
//			else
//				ret = true;
			ret = true;
		}


		// xuky 2017.12.20 ���ݱ�־�������̵���
		if (need_reConnet){
			need_reConnet = false;
			ret = false;
		}


		if (ret == false) {
			// xuky 2017.11.27 ��������Ͽ������½���
			// ��Ҫ���½�������
			Util698.log(TerminalTelnetSingle.class.getName(), "���½�������", Debug.LOG_INFO);
			ret = root();
			if (ret) {
				writeUtil("init_dev", OS);
				if (readUntil("input the choice>", IN).equals(""))
					ret = false;
				else
					ret = true;
			}
		}

		Util698.log(TerminalTelnetSingle.class.getName(), "init_dev ����...״̬" + ret, Debug.LOG_INFO);

		return ret;
	}

	// �˳��������ó���
	public Boolean init_dev_quit() {
		Boolean ret = false;
		if (READSTR.indexOf("[root@(none) /]#") >= 0) {
			ret = true;
		}
		if (READSTR.indexOf("input the choice>") >= 0) {
			writeUtil("98", OS);
			if (readUntil("[root@(none) /]#", IN).equals(""))
				ret = false;
			else
				ret = true;
		}
		return ret;
	}

	// ����ָ��λ��,�������¶�
	public String readUntil(String endFlag, InputStream in) {

		if (in == null)
			return "";

		InputStreamReader isr = new InputStreamReader(in);

		char[] charBytes = new char[1024];
		int n = 0;
		boolean is_find = false;
		String str = "", need_str = "";
		try {
			while ((n = isr.read(charBytes)) != -1) {
				for (int i = 0; i < n; i++) {
					char c = (char) charBytes[i];

					// // xuky 2017.08.21 ÿ�յ�һ�����ݾͽ���չʾ���ڽ�����������ʱ����ϸ�µ�չʾ��������
					// String tmp = ""+c;
					// String[] s = { "ReadUntil", "", tmp };
					// Publisher.getInstance().publish(s);

					str += c;

					// xuky 2017.09.21 Ŀǰ������������ܳ��ִ��쳣
					// input the choice>88
					// Segmentation fault
					// [root@(none) /]#
					if (str.indexOf("Segmentation fault") >= 0 && str.endsWith("#"))
						Util698.log(TerminalTelnetSingle.class.getName(), "Segmentation fault =>��" + str + "��",
								Debug.LOG_INFO);

					// ��ƴ�ӵ��ַ�����ָ�����ַ�����βʱ,���ټ�����
					if (str.endsWith(endFlag)) {
						is_find = true; // �����ҵ���־�������˳�whileѭ��
						break; // �˳���ǰfor ѭ��
					}
				}

				// �˳���ǰwhile ѭ��
				if (is_find)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// xuky 2017.12.11 ����һ�������ǰ���ִ����n=-1

		READSTR = str;
		if (SoftParameter.getInstance().getLOG_Level().equals("1")) {
			Util698.log(TerminalTelnetSingle.class.getName(),
					"readUntil(endFlag��" + endFlag + "��) getData=>��" + READSTR + "��", Debug.LOG_INFO);
			// System.out.println(readStr);
		}

		// xuky 2017.12.20 Ϊ�����Ч�ʣ�����88��ִ�У����Ӵ˴���
		// ���READSTR�а�����DELAY RESET��Ϣ����ʾִ����������Ĳ���������ִ��telnet��д����ǰӦ����������
		if (READSTR.indexOf("DELAY RESET") >= 0)
			need_reConnet = true;

		return str;
	}

	private Boolean writeUtil(String cmd, OutputStream os) {
		if (os == null)
			return false;
		return writeUtil(cmd, os, true);
	}

	// д�������
	private Boolean writeUtil(String cmd, OutputStream os, Boolean changeOldCMD) {
		if (os == null)
			return false;
		try {
			cmd = cmd + "\n";
			// xuky 2017.09.21 ��¼���һ�η�������������
			if (changeOldCMD)
				OLDCMD = cmd;

			if (SoftParameter.getInstance().getLOG_Level().equals("1")) {
				Util698.log(TerminalTelnetSingle.class.getName(), "write =>��" + cmd + "��", Debug.LOG_INFO);
				// System.out.println(readStr);
			}
			os.write(cmd.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	// �޸Ĳ�����Ϣ�����������޸�ǰȷ��
	public boolean changeParam(String key, String val) {
		Boolean ret = false;

		// Util698.log(TerminalTelnetSingle.class.getName(), "init_dev [begin]
		// key=" + key, Debug.LOG_INFO);

		if (!init_dev())
			return ret;

		// Util698.log(TerminalTelnetSingle.class.getName(), "init_dev [end]
		// key=" + key, Debug.LOG_INFO);

		writeUtil(key, OS);

		if (readUntil(">", IN).equals(""))
			return false;

		// xuky 2017.12.14 ���ݵõ�����ʾ��Ϣ�����ж�
		if (READSTR.endsWith("input the choice>")) {
			// ��������������˳�����
			NOVERIFY = true;
			return true;
		}

		if (val == null || val.equals("")) {
			// xuky 2017.09.30 ���ڲ�����Ŀ��ֻ��ִ�У�����¼������
		} else {
			writeUtil(val, OS);
			if (readUntil(">", IN).equals(""))
				return false;
		}

		writeUtil("y", OS);
		if (readUntil("input the choice>", IN).equals(""))
			return false;

		// if (!init_dev_quit())
		// return ret;
		ret = true;
		return ret;
	}

	// �޸Ĳ�����Ϣ��������Ҫ�����޸�ȷ��
	private void changeParamWithCheck(String key, String val) {
		writeUtil(key, OS);
		if (readUntil(">", IN).equals(""))
			return;

		writeUtil("y", OS);
		if (readUntil(">", IN).equals(""))
			return;

		writeUtil(val, OS);
		if (readUntil(">", IN).equals(""))
			return;
		writeUtil("y", OS);

		if (readUntil("input the choice>", IN).equals(""))
			return;
	}

	private void runParam(String key) {
		writeUtil(key, OS);
		if (readUntil(">", IN).equals(""))
			return;

		writeUtil("y", OS);

		if (readUntil("input the choice>", IN).equals(""))
			return;
	}

	public String writeThenReadUtil(String cmd, String data) {
		if (READSTR.endsWith("input the choice>")) {
			writeUtil("98", OS);
			if (readUntil("[root@(none) /]#", IN).equals(""))
				return "";
		}
		writeUtil(cmd, OS, true);
		if (readUntil(data, IN).equals(""))
			return "";
		return READSTR;
	}

	// ��ȡ�����������
	public String getParam() {
		String ret = "";
		// ���ݵ�ǰ��״̬��������Ӧ�Ĵ���
		if (READSTR.indexOf("[root@(none) /]#") >= 0) {
			writeUtil("init_dev", OS);
			ret = readUntil("input the choice>", IN);
		} else if (READSTR.indexOf("input the choice>") >= 0) {
			writeUtil("88", OS);
			ret = readUntil("input the choice>", IN);
		} else {
			ret = "�쳣�������޷���������-" + READSTR;
		}
		return ret;
	}

	public String getParam(String key) {
		String ret = "";
		try {
			String allMsg = getParam();
			ret = getParam(key, allMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	// ��֤�����������Ƿ���ȷ
	public String getParam(String key, String allMsg) {
		String str1 = "";
		try {
			if (allMsg.indexOf("�쳣") >= 0)
				return "-1";

			String find = "-----" + key + "--";
			// -----10--set gprs apn username:cmnet1
			// �ҵ���1��λ�ã�-----10--
			int pos1 = allMsg.indexOf(find);
			pos1 = pos1 + find.length();
			String str = allMsg.substring(pos1, pos1 + 3);
			str = str + "";
			// �ҵ���2��λ�ã����з���
			int pos2 = allMsg.indexOf("\r", pos1);
			str = allMsg.substring(pos1, pos2);
			// �ҵ���3��λ�ã�:
			int pos3 = str.indexOf(":");
			str1 = str.substring(pos3 + 1);
			str1 = str1 + "";
		} catch (Exception e) {
			Util698.log(TerminalTelnetSingle.class.getName(),
					"getParam err :allMsg=>��" + allMsg + "�� find=>��" + key + "��", Debug.LOG_INFO);
			e.printStackTrace();
		}
		return str1;
	}

	// ��֤�����������Ƿ���ȷ
	public String[] verify(String key, String val) {
		// ret[0] �õ�������
		// ret[1] ��֤�Ľ�� 0ʧ�� 1�ɹ�
		String[] ret = { "", "0" };
		String str1 = getParam(key).trim();
		ret[0] = str1;
		if (str1.equals("-1"))
			ret[1] = "0";
		if (str1.equals(val))
			ret[1] = "1";
		else {
			// xuky 2017.11.13 ����ʱ�����Ƚ�
			if (val.startsWith("[���")) {
				// verify("17-11-13 14:29:45","[���<5����]") // ���������ĸ�ʽΪ [���<nn����]
				String now = DateTimeFun.getDateTimeSSS();
				// // xuky 2017.11.13 ���ص�����ǰ���пո�
				str1 = str1.trim();
				Long val_teminal = Math.abs(Util698.getMilliSecondBetween_new("20" + str1 + ":000", now));
				String val_expect = val.split("<")[1];
				val_expect = val_expect.substring(0, val_expect.length() - 3); // ȥ������]�Ժ������
				ret[0] = "20" + ret[0] + "<==>" + now;
				// xuky 2017.11.13 �ڴ˽��������滻����ֹ����� xxx.split("-")[1] ֻ����ʾ��������
				// ע�ⲻҪʹ��-��Ϊ��Ϣ��ʾ������
				ret[0] = ret[0].replaceAll("\\-", "\\\\") + " ~ " + val_teminal;
				if (Long.parseLong(val_expect) * 60 * 1000 > val_teminal)
					ret[1] = "1";
				else
					ret[1] = "0";

			}
		}
		return ret;
	}

	public static void main(String[] arg) {

		TerminalTelnetSingle terminalTelnet = TerminalTelnetSingle.getInstance("129.1.22.96");

		String key = "10", val = "cmnet3";
		terminalTelnet.getParam(key);

		// ���ò��� ��֤����
		// {
		// // ���ò���
		// terminalTelnet.changeParam(key, val);
		// // ��֤����
		// String verify = terminalTelnet.verify(key,val)[1];
		// if (verify.equals("1"))
		// System.out.println("��֤���:ok");
		// else
		// System.out.println("��֤���:"+verify);
		// }
		// // �����ն˳���
		// {
		// String command = "dn 192.168.1.210";
		// String ret = terminalTelnet.writeThenReadUtil(command,"[root@(none)
		// /]#");
		// System.out.println(ret);
		// }

		// �����ն˳���
		{
			String command = "cat /etc/macaddr";
			String end = "[root@(none) /]#";
			String ret = terminalTelnet.writeThenReadUtil(command, end);
			ret = ret.substring(command.length() + 2, ret.length() - end.length() - 1);
			System.out.println(ret);
		}

		terminalTelnet.destroy();
	}

	public Boolean getNOVERIFY() {
		return NOVERIFY;
	}

	public void setNOVERIFY(Boolean nOVERIFY) {
		NOVERIFY = nOVERIFY;
	}

	public String getREADSTR() {
		return READSTR;
	}

}
