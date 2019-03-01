package javafx.assident;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import SocketAssident.Client;
import dao.basedao.IBaseDao;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import produce.entity.ProduceRecord;
import produce.entity.ProduceRecordDaoImpl;
import produce.entity.UserManager;
import util.SoftParameter;
import util.Util698;


// 参考https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Service.html
// 参考https://blog.csdn.net/xby1993/article/details/24811099 FirstLineService service = new FirstLineService();  ....
// 参考 https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/concurrency.htm#BABCHBEA   FirstLineService...
public class ProgressService extends Service<String>{
	static String lastTest = "";
	static String lastMoni = "";
	static String lastTime = "";
	static String lastTest_txt = "最后测试时间";
	static String lastMoni_txt = "最后监控时间";
	static String lastTime_txt = "测试间隔时间";
	static Boolean firstStop = false;
	static String ProgramName = "ProduceWare.exe";

	public void setFirstStop(Boolean firstStop) {
		this.firstStop = firstStop;
	}


	static Long waitTime = Long.valueOf(1000 * 60 * 4);
	static String show_msg = "";

	@Override
	protected Task<String> createTask() {

		return new Task<String>() {
			@Override
			protected String call() throws Exception {
				return GetAndOperate();
			}

		};

	}

	public static String GetAndOperate() {
		String now = DateTimeFun.getDateTimeSSS();
		lastMoni = now;
//		updateMessage(WriteInfo());


		Util698.log(ProgressService.class.getName(), "xuky_1",Debug.LOG_INFO);
		IBaseDao<ProduceRecord> iBaseDao_ProduceRecord = new ProduceRecordDaoImpl();
		List result = iBaseDao_ProduceRecord.retrieveBySQL(
				"select max(beginOpt) from " + ProduceRecord.class.getName() + " where opResult='测试成功'");
		String lasttime = "";
		for (Object o : result) {
			if (o != null)
				lasttime = (String) o;
		}

		Long num = (long) 0;
		if (!lasttime.equals("")) {
			num = Util698.getMilliSecondBetween_new(now, lasttime);
			lastTest = lasttime;
			lastMoni = now;
			lastTime = num.toString();
		}

		if (firstStop) {
			WriteInfo();
			firstStop = false;
			return show_msg;
		}

//		String lasttime1 = lasttime;
		Util698.log(ProgressService.class.getName(), "xuky_2",Debug.LOG_INFO);
		if (lasttime.equals("")) {
			MyReset();
			return show_msg;
		}

		// 关键代码，不是直接进行界面对象书写
//		updateMessage(WriteInfo());
		WriteInfo();


		// 如果大于等待时间
		Util698.log(ProgressService.class.getName(), "xuky_3",Debug.LOG_INFO);
		if (num > waitTime) {
			Util698.log(ProgressService.class.getName(), "num > waitTime " + num + "-" +waitTime,Debug.LOG_INFO);
			MyReset();
		}
		Util698.log(ProgressService.class.getName(), "num <= waitTime" + num + "-" +waitTime,Debug.LOG_INFO);

		Util698.log(ProgressService.class.getName(), "xuky_4",Debug.LOG_INFO);
		return show_msg;
	}

	private static String WriteInfo() {

		String str = lastTest_txt + "\r\n" + lastTest + "\r\n" + "\r\n";
		str += lastMoni_txt + "\r\n" + lastMoni + "\r\n" + "\r\n";
		str += lastTime_txt + "\r\n" + lastTime + "\r\n" + "\r\n";
		show_msg = str;
		str = str.replaceAll("\r\n", "");
		Util698.log(ProgressService.class.getName(), str, Debug.LOG_INFO);
		return str;
	}

	private static void MyReset() {
		// new Thread(() -> {
		boolean test = false;
		if (test)
			return;

		Util698.log(ProgressService.class.getName(), "MyReset", Debug.LOG_INFO);

		if (isRunning(ProgramName)) {
			KillProgram(ProgramName);
			Debug.sleep(1000);
		}

		UserManager userManager = SoftParameter.getInstance().getUserManager();
		RunProgram(ProgramName + " " + userManager.getUserid() + " " + userManager.getUserpwd());

		// }).start();

		// xuky 2018.05.03 不重启自身程序 如果不关闭当前的看门狗程序，启动的ProgramName就会异常停顿在启动串口处
		// 必须关闭看门狗，关闭前，将Client中的相关参数进行传递

		// 默认的入参是1
		String para = " 1";
		// 只要判定需要重发，就设置重发的内容 此时的入参是需要发送的报文数据
		if (Client.getInstance().getSend_time1() < Client.getInstance().getRetry()) {
			para = " " + Client.getInstance().getSDATA();
		}
		// Server.getInstance().disconnect();

		RunProgram("watchdog.exe" + para);
		Debug.sleep(3000);
		System.exit(0);
	}
	private static void RunProgram(String processName) {
		Util698.log(ProgressService.class.getName(), "看门狗软件【启动软件】" + processName, Debug.LOG_INFO);
		String cmd = processName; // 关闭软件后，自动重新执行
		Runtime rt = Runtime.getRuntime(); // 获取运行时系统
		try {
			Process proc = rt.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		} // 执行命令

	}
	private static Boolean isRunning(String processName) {
		// 如下判断QQ.exe程序是否在运行，有则返回true
		BufferedReader br = null;
		try {
			Process proc = Runtime.getRuntime().exec("tasklist -fi " + '"' + "imagename eq " + processName + '"');
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				// 判断指定的进程是否在运行
				if (line.contains(processName)) {
					Util698.log(ProgressService.class.getName(), "看门狗软件【检测】 " + processName + "正在运行...", Debug.LOG_INFO);
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception ex) {
				}
			}

		}
	}
	private static void KillProgram(String processName) {
		String cmd = "taskkill /im " + processName + " /f "; // 关闭软件
		Runtime rt = Runtime.getRuntime(); // 获取运行时系统
		try {
			Process proc = rt.exec(cmd);
			Util698.log(ProgressService.class.getName(), "看门狗软件【Kill】" + processName, Debug.LOG_INFO);
		} catch (IOException e) {
			e.printStackTrace();
		} // 执行命令
	}

}
