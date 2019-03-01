package produce.deal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.net.telnet.TelnetClient;

import util.Publisher;

public class TerminalTelnet {
	private TelnetClient tc;
	private InputStream in;
	private OutputStream os;
	private String readStr;  // ��¼����յ������ݣ��Դ˿����жϵ�ǰ��telnet����ĳ��״̬�����ڽ��к����Ķ�Ӧ����
	private String IP;
	private Boolean degugMode = true;
	private String oldCMD = "";

//	public String getReadStr() {
//		return readStr;
//	}

	public TerminalTelnet(String terminalIP){
		IP = terminalIP;
		root();
	}

	public void destroy(){
		if (tc!=null)
			try {
				tc.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	// �����նˣ���¼root�û�
	public void root(){
		tc = new TelnetClient();
		try {
			tc.connect(IP, 23);
		} catch (Exception e) {
			e.printStackTrace();
		}
		in = tc.getInputStream();
		os = tc.getOutputStream();
		readUntil(":", in);
		writeUtil("root", os);
		readUntil("[root@(none) /]#", in);

	}

	// ����������ó���
	public Boolean init_dev(){
		Boolean ret = false;
		if (readStr.indexOf("[root@(none) /]#") >=0){
			writeUtil("init_dev", os);
			readUntil("input the choice>", in);
			ret = true;
		}
		if (readStr.indexOf("input the choice>") >=0){
			// ����ִ���κβ���
			ret = true;
		}
		return ret;
	}

	// �˳��������ó���
	public Boolean init_dev_quit(){
		Boolean ret = false;
		if (readStr.indexOf("[root@(none) /]#") >=0){
			ret = true;
		}
		if (readStr.indexOf("input the choice>") >=0){
			writeUtil("98", os);
			readUntil("[root@(none) /]#", in);
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
		boolean flag = false;
		String str = "";
		try {
			while ((n = isr.read(charBytes)) != -1) {
				for (int i = 0; i < n; i++) {
					char c = (char) charBytes[i];

					// xuky 2017.08.21 ÿ�յ�һ�����ݾͽ���չʾ���ڽ�����������ʱ����ϸ�µ�չʾ��������
					String tmp = ""+c;
					String[] s = { "ReadUntil", "", tmp };
					Publisher.getInstance().publish(s);

					str += c;
					// System.out.println("readUntil:"+str);

					// xuky 2017.09.21 Ŀǰ������������ܳ��ִ��쳣
//					input the choice>88
//					Segmentation fault
//					[root@(none) /]#
					if (str.indexOf("Segmentation fault")>=0 && str.endsWith("#"))
					{
						writeUtil("init_dev", os, false);
						readUntil("input the choice>", in);
						writeUtil(oldCMD, os);
						str = readUntil("input the choice>", in);
						flag = true;
						break;
					}
					// ��ƴ�ӵ��ַ�����ָ�����ַ�����βʱ,���ڼ�����
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

		readStr = str;
		if (degugMode)
			System.out.println(readStr);
		return str;
	}
	public void writeUtil(String cmd, OutputStream os) {
		writeUtil(cmd,os,true);
	}

	// д�������
	public void writeUtil(String cmd, OutputStream os,Boolean changeOldCMD) {
		if (os == null)
			return;
		try {
			cmd = cmd + "\n";
			// xuky 2017.09.21 ��¼���һ�η�������������
			if (changeOldCMD)
				oldCMD = cmd;
			os.write(cmd.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// �޸Ĳ�����Ϣ�����������޸�ǰȷ��
	public boolean changeParam(String key,String val){
		Boolean ret = false;

		if (!init_dev())
			return ret;

		writeUtil(key, os);
		readUntil(">", in);

		if (val == null || val.equals("") ){
			// xuky 2017.09.30 ���ڲ�����Ŀ��ֻ��ִ�У�����¼������
		}
		else{
			writeUtil(val, os);
			readUntil(">", in);
		}

		writeUtil("y", os);
		readUntil("input the choice>", in);

//		if (!init_dev_quit())
//			return ret;
		ret = true;
		return ret;
	}
	// �޸Ĳ�����Ϣ��������Ҫ�����޸�ȷ��
	private void changeParamWithCheck(String key,String val){
		writeUtil(key, os);
		readUntil(">", in);

		writeUtil("y", os);
		readUntil(">", in);

		writeUtil(val, os);
		readUntil(">", in);
		writeUtil("y", os);

		readUntil("input the choice>", in);
	}

	private void runParam(String key){
		writeUtil(key, os);
		readUntil(">", in);

		writeUtil("y", os);

		readUntil("input the choice>", in);
	}

	// ��ȡ�����������
	public String getParam(){
		String ret = "";
		// ���ݵ�ǰ��״̬��������Ӧ�Ĵ���
		if (readStr.indexOf("[root@(none) /]#")>=0){
			writeUtil("init_dev", os);
			ret = readUntil("input the choice>", in);
		}
		else if (readStr.indexOf("input the choice>")>=0){
			writeUtil("88", os);
			ret = readUntil("input the choice>", in);
		}
		else {
			ret = "�쳣�������޷���������-"+readStr;
		}
		return ret;
	}

	// ��֤�����������Ƿ���ȷ
	public String getParam(String key){
		String allMsg = getParam();
		if (allMsg.indexOf("�쳣") >= 0)
			return "-1";

		String find = "-----"+key+"--";
		// -----10--set gprs apn username:cmnet1
		// �ҵ���1��λ�ã�-----10--
		int pos1 = allMsg.indexOf(find);
		pos1 = pos1 + find.length();
		String str = allMsg.substring(pos1,pos1+3);
		str = str + "";
		// �ҵ���2��λ�ã����з���
		int pos2 = allMsg.indexOf("\r",pos1);
		str = allMsg.substring(pos1,pos2);
		// �ҵ���3��λ�ã�:
		int pos3 = str.indexOf(":");
		String str1 = str.substring(pos3+1);
		str1 = str1 + "";
		return str1;
	}


	// ��֤�����������Ƿ���ȷ
	public String[] verify(String key,String val){
		// ret[0] �õ�������
		// ret[1] ��֤�Ľ��  0ʧ��  1�ɹ�
 		String[] ret = {"",""};
		String str1 = getParam(key);
		ret[0] = str1;
		if (str1.equals("-1"))
			ret[1] = "0";
		if (str1.equals(val))
			ret[1] = "1";
		else
			ret[1] = "0";
		return ret;
	}

	public static void main(String[] arg){

		TerminalTelnet terminalTelnet = new TerminalTelnet("192.168.1.96");

		String key = "10",val =  "cmnet3";
		// ���ò���
		terminalTelnet.changeParam(key, val);
		// ��֤����
		String verify = terminalTelnet.verify(key,val)[1];
		if (verify.equals("0"))
			System.out.println("��֤���:ok");
		else
			System.out.println("��֤���:"+verify);
		terminalTelnet.destroy();
	}
}
