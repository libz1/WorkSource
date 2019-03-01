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
	private String READSTR = ""; // 记录最后收到的数据，以此可以判断当前的telnet处于某个状态，便于进行后续的对应操作
	private Boolean need_reConnet = false ; //xuky 2017.12.20 是否需要重新连接的标志
	private String IP;
	// private Boolean degugMode = false;
	private String OLDCMD = "";
	private Boolean NOVERIFY = false; // 是否需要验证的标记信息

	// xuky 2017.12.18 设置等待时间为2.4秒无效，修改为1.5秒（外部默认的等待时间为2.5秒）
	// xuky 2017.12.19 因为需要超时重试，必须执行此代码（2/2）
//	TCLIENT.setDefaultTimeout(1000);
	// xuky 2017.12.21 出现超时情况
	// xuky 2018.01.08 dn时出现超时情况
	private int TIMEOUT_USE = 1500;

	Boolean RUNFASTER = false;

	// public String getReadStr() {
	// return readStr;
	// }
	public static TerminalTelnetSingle getInstance(String terminalIP) {
		if (uniqueInstance == null) {
			synchronized (TerminalTelnetSingle.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
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
					// 双重检查加锁
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


	// 连接终端，登录root用户
	public Boolean root() {

		if (RUNFASTER) return true;

		Boolean ret = false;
		try {

			// xuky 2017.12.19 建立新的之前，释放之前的
			// xuky 2017.12.19 因为需要超时重试，必须执行此代码（1/2）
			if (TCLIENT != null)
				TCLIENT.disconnect();
			TCLIENT = new TelnetClient();

			// xuky 2018.01.08 使用系统默认的超时时间
			if (TIMEOUT_USE != 0)
				TCLIENT.setDefaultTimeout(TIMEOUT_USE);

			Util698.log(TerminalTelnetSingle.class.getName(), "root =>【 connect... " + IP + "】", Debug.LOG_INFO);

			TCLIENT.connect(IP, 23);  // 默认telnet端口号

			IN = TCLIENT.getInputStream();
			OS = TCLIENT.getOutputStream();
			if (readUntil(":", IN).equals(""))
				ret = false;
			else {
				// 写入用户名信息 root
				writeUtil("root", OS);
				if (readUntil("[root@(none) /]#", IN).equals(""))
					ret = false;
				else
					ret = true;
			}
			Util698.log(TerminalTelnetSingle.class.getName(), "root 【 " + ret + "】", Debug.LOG_INFO);
		} catch (Exception e) {
			String errMsg = e.getMessage();
			if (errMsg.indexOf("timed out") >= 0 || errMsg.indexOf("refuse") >= 0) {
//				javafxutil.f_alert_informationDialog("操作提示", "无法连接" + IP + "终端，请检查");
//				Util698.log(TerminalTelnetSingle.class.getName(), "root Exception => 【 无法连接" + IP + "终端，请检查】",
//						Debug.LOG_INFO);
			} else {
				e.printStackTrace();
				Util698.log(TerminalTelnetSingle.class.getName(), "root Exception =>【 " + e.getMessage() + "】",
						Debug.LOG_INFO);
			}
			ret = false;
		}
		return ret;

	}

	// 进入参数设置程序
	public Boolean init_dev() {
		Util698.log(TerminalTelnetSingle.class.getName(), "init_dev 开始...", Debug.LOG_INFO);
		// 通过如下操作进行验证
		Boolean ret = false;
		if (READSTR.endsWith("[root@(none) /]#")) {
			Util698.log(TerminalTelnetSingle.class.getName(), "验证是否有效", Debug.LOG_INFO);
			writeUtil("init_dev", OS);
			// xuky 2017.12.14 根据readUntil的返回值判断是否执行是有效的
			if (readUntil("input the choice>", IN).equals(""))
				ret = false;
			else
				ret = true;
		} else if (READSTR.endsWith("input the choice>")) {
			// xuky 2017.12.20 为了提高执行的效率，删减此处的操作，
			// xuky 2017.12.20 为了提高执行的效率，在执行完毕时，根据情况readUntil函数中添加删除READSTR的操作
//			Util698.log(TerminalTelnetSingle.class.getName(), "验证是否有效", Debug.LOG_INFO);
//			writeUtil("88", OS);
//			if (readUntil("input the choice>", IN).equals(""))
//				ret = false;
//			else
//				ret = true;
			ret = true;
		}


		// xuky 2017.12.20 根据标志进行流程调整
		if (need_reConnet){
			need_reConnet = false;
			ret = false;
		}


		if (ret == false) {
			// xuky 2017.11.27 可能网络断开，重新进行
			// 需要重新进行连接
			Util698.log(TerminalTelnetSingle.class.getName(), "重新进行连接", Debug.LOG_INFO);
			ret = root();
			if (ret) {
				writeUtil("init_dev", OS);
				if (readUntil("input the choice>", IN).equals(""))
					ret = false;
				else
					ret = true;
			}
		}

		Util698.log(TerminalTelnetSingle.class.getName(), "init_dev 结束...状态" + ret, Debug.LOG_INFO);

		return ret;
	}

	// 退出参数设置程序
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

	// 读到指定位置,不在向下读
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

					// // xuky 2017.08.21 每收到一点数据就进行展示，在进行升级操作时可以细致的展示升级过程
					// String tmp = ""+c;
					// String[] s = { "ReadUntil", "", tmp };
					// Publisher.getInstance().publish(s);

					str += c;

					// xuky 2017.09.21 目前集中器程序可能出现此异常
					// input the choice>88
					// Segmentation fault
					// [root@(none) /]#
					if (str.indexOf("Segmentation fault") >= 0 && str.endsWith("#"))
						Util698.log(TerminalTelnetSingle.class.getName(), "Segmentation fault =>【" + str + "】",
								Debug.LOG_INFO);

					// 当拼接的字符串以指定的字符串结尾时,不再继续读
					if (str.endsWith(endFlag)) {
						is_find = true; // 设置找到标志，用于退出while循环
						break; // 退出当前for 循环
					}
				}

				// 退出当前while 循环
				if (is_find)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// xuky 2017.12.11 还有一种情况，前面的执行是n=-1

		READSTR = str;
		if (SoftParameter.getInstance().getLOG_Level().equals("1")) {
			Util698.log(TerminalTelnetSingle.class.getName(),
					"readUntil(endFlag【" + endFlag + "】) getData=>【" + READSTR + "】", Debug.LOG_INFO);
			// System.out.println(readStr);
		}

		// xuky 2017.12.20 为了提高效率，避免88的执行，增加此代码
		// 如果READSTR中包含了DELAY RESET信息，表示执行了重启类的操作，后续执行telnet的写操作前应该重新连接
		if (READSTR.indexOf("DELAY RESET") >= 0)
			need_reConnet = true;

		return str;
	}

	private Boolean writeUtil(String cmd, OutputStream os) {
		if (os == null)
			return false;
		return writeUtil(cmd, os, true);
	}

	// 写入命令方法
	private Boolean writeUtil(String cmd, OutputStream os, Boolean changeOldCMD) {
		if (os == null)
			return false;
		try {
			cmd = cmd + "\n";
			// xuky 2017.09.21 记录最近一次发出的命令内容
			if (changeOldCMD)
				OLDCMD = cmd;

			if (SoftParameter.getInstance().getLOG_Level().equals("1")) {
				Util698.log(TerminalTelnetSingle.class.getName(), "write =>【" + cmd + "】", Debug.LOG_INFO);
				// System.out.println(readStr);
			}
			os.write(cmd.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	// 修改参数信息，其中无需修改前确认
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

		// xuky 2017.12.14 根据得到的提示信息进行判断
		if (READSTR.endsWith("input the choice>")) {
			// 无需继续操作，退出即可
			NOVERIFY = true;
			return true;
		}

		if (val == null || val.equals("")) {
			// xuky 2017.09.30 存在部分项目，只是执行，无需录入数据
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

	// 修改参数信息，其中需要进行修改确认
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

	// 获取整体参数数据
	public String getParam() {
		String ret = "";
		// 根据当前的状态，进行相应的处理
		if (READSTR.indexOf("[root@(none) /]#") >= 0) {
			writeUtil("init_dev", OS);
			ret = readUntil("input the choice>", IN);
		} else if (READSTR.indexOf("input the choice>") >= 0) {
			writeUtil("88", OS);
			ret = readUntil("input the choice>", IN);
		} else {
			ret = "异常：程序无法处理的情况-" + READSTR;
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

	// 验证参数的数据是否正确
	public String getParam(String key, String allMsg) {
		String str1 = "";
		try {
			if (allMsg.indexOf("异常") >= 0)
				return "-1";

			String find = "-----" + key + "--";
			// -----10--set gprs apn username:cmnet1
			// 找到第1个位置：-----10--
			int pos1 = allMsg.indexOf(find);
			pos1 = pos1 + find.length();
			String str = allMsg.substring(pos1, pos1 + 3);
			str = str + "";
			// 找到第2个位置：换行符号
			int pos2 = allMsg.indexOf("\r", pos1);
			str = allMsg.substring(pos1, pos2);
			// 找到第3个位置：:
			int pos3 = str.indexOf(":");
			str1 = str.substring(pos3 + 1);
			str1 = str1 + "";
		} catch (Exception e) {
			Util698.log(TerminalTelnetSingle.class.getName(),
					"getParam err :allMsg=>【" + allMsg + "】 find=>【" + key + "】", Debug.LOG_INFO);
			e.printStackTrace();
		}
		return str1;
	}

	// 验证参数的数据是否正确
	public String[] verify(String key, String val) {
		// ret[0] 得到的数据
		// ret[1] 验证的结果 0失败 1成功
		String[] ret = { "", "0" };
		String str1 = getParam(key).trim();
		ret[0] = str1;
		if (str1.equals("-1"))
			ret[1] = "0";
		if (str1.equals(val))
			ret[1] = "1";
		else {
			// xuky 2017.11.13 进行时钟误差比较
			if (val.startsWith("[误差")) {
				// verify("17-11-13 14:29:45","[误差<5分钟]") // 测试用例的格式为 [误差<nn分钟]
				String now = DateTimeFun.getDateTimeSSS();
				// // xuky 2017.11.13 返回的数据前面有空格
				str1 = str1.trim();
				Long val_teminal = Math.abs(Util698.getMilliSecondBetween_new("20" + str1 + ":000", now));
				String val_expect = val.split("<")[1];
				val_expect = val_expect.substring(0, val_expect.length() - 3); // 去掉分钟]以后的数据
				ret[0] = "20" + ret[0] + "<==>" + now;
				// xuky 2017.11.13 在此进行数据替换，防止后面的 xxx.split("-")[1] 只能显示部分数据
				// 注意不要使用-作为信息显示用内容
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

		// 设置参数 验证参数
		// {
		// // 设置参数
		// terminalTelnet.changeParam(key, val);
		// // 验证参数
		// String verify = terminalTelnet.verify(key,val)[1];
		// if (verify.equals("1"))
		// System.out.println("验证结果:ok");
		// else
		// System.out.println("验证结果:"+verify);
		// }
		// // 更新终端程序
		// {
		// String command = "dn 192.168.1.210";
		// String ret = terminalTelnet.writeThenReadUtil(command,"[root@(none)
		// /]#");
		// System.out.println(ret);
		// }

		// 更新终端程序
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
