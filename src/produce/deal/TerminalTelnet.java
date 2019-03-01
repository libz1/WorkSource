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
	private String readStr;  // 记录最后收到的数据，以此可以判断当前的telnet处于某个状态，便于进行后续的对应操作
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

	// 连接终端，登录root用户
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

	// 进入参数设置程序
	public Boolean init_dev(){
		Boolean ret = false;
		if (readStr.indexOf("[root@(none) /]#") >=0){
			writeUtil("init_dev", os);
			readUntil("input the choice>", in);
			ret = true;
		}
		if (readStr.indexOf("input the choice>") >=0){
			// 无需执行任何操作
			ret = true;
		}
		return ret;
	}

	// 退出参数设置程序
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

	// 读到指定位置,不在向下读
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

					// xuky 2017.08.21 每收到一点数据就进行展示，在进行升级操作时可以细致的展示升级过程
					String tmp = ""+c;
					String[] s = { "ReadUntil", "", tmp };
					Publisher.getInstance().publish(s);

					str += c;
					// System.out.println("readUntil:"+str);

					// xuky 2017.09.21 目前集中器程序可能出现此异常
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
					// 当拼接的字符串以指定的字符串结尾时,不在继续读
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

	// 写入命令方法
	public void writeUtil(String cmd, OutputStream os,Boolean changeOldCMD) {
		if (os == null)
			return;
		try {
			cmd = cmd + "\n";
			// xuky 2017.09.21 记录最近一次发出的命令内容
			if (changeOldCMD)
				oldCMD = cmd;
			os.write(cmd.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 修改参数信息，其中无需修改前确认
	public boolean changeParam(String key,String val){
		Boolean ret = false;

		if (!init_dev())
			return ret;

		writeUtil(key, os);
		readUntil(">", in);

		if (val == null || val.equals("") ){
			// xuky 2017.09.30 存在部分项目，只是执行，无需录入数据
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
	// 修改参数信息，其中需要进行修改确认
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

	// 获取整体参数数据
	public String getParam(){
		String ret = "";
		// 根据当前的状态，进行相应的处理
		if (readStr.indexOf("[root@(none) /]#")>=0){
			writeUtil("init_dev", os);
			ret = readUntil("input the choice>", in);
		}
		else if (readStr.indexOf("input the choice>")>=0){
			writeUtil("88", os);
			ret = readUntil("input the choice>", in);
		}
		else {
			ret = "异常：程序无法处理的情况-"+readStr;
		}
		return ret;
	}

	// 验证参数的数据是否正确
	public String getParam(String key){
		String allMsg = getParam();
		if (allMsg.indexOf("异常") >= 0)
			return "-1";

		String find = "-----"+key+"--";
		// -----10--set gprs apn username:cmnet1
		// 找到第1个位置：-----10--
		int pos1 = allMsg.indexOf(find);
		pos1 = pos1 + find.length();
		String str = allMsg.substring(pos1,pos1+3);
		str = str + "";
		// 找到第2个位置：换行符号
		int pos2 = allMsg.indexOf("\r",pos1);
		str = allMsg.substring(pos1,pos2);
		// 找到第3个位置：:
		int pos3 = str.indexOf(":");
		String str1 = str.substring(pos3+1);
		str1 = str1 + "";
		return str1;
	}


	// 验证参数的数据是否正确
	public String[] verify(String key,String val){
		// ret[0] 得到的数据
		// ret[1] 验证的结果  0失败  1成功
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
		// 设置参数
		terminalTelnet.changeParam(key, val);
		// 验证参数
		String verify = terminalTelnet.verify(key,val)[1];
		if (verify.equals("0"))
			System.out.println("验证结果:ok");
		else
			System.out.println("验证结果:"+verify);
		terminalTelnet.destroy();
	}
}
