package javafx.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import produce.entity.ProduceRecord;
import produce.entity.ProduceRecordDaoImpl;
import produce.entity.UserManager;
import util.SoftParameter;
import util.Util698;

public class WatchDogDOS {

	public static void main(String[] args) {
		start();
	}

	// 判断是否进行干预的时间
	static Long waitTime = Long.valueOf(1000 * 60 * 2);
	// Long waitTime = Long.valueOf(1000 * 60);

	static String ProgramName = "ProduceWare.exe";

	static String ver = "0.02";

	public static void start() {

		// Util698.log(WatchDog.class.getName(), "看门狗软件【运行】ver " + ver,
		// Debug.LOG_INFO);
		new Thread(() -> {
			watching();
		}).start();

	}

	public static void watching() {
		// http://blog.csdn.net/xinyuan_java/article/details/51602088
		// 间隔时间为6分钟
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				doSomething();
			}
		}, 100, 1000 * 60 * 3);
		// }, 100, 1000 * 1);
	}

	public static void doSomething() {
		IBaseDao<ProduceRecord> iBaseDao_ProduceRecord = new ProduceRecordDaoImpl();
		List result = iBaseDao_ProduceRecord
				.retrieveBySQL("select max(beginOpt) from " + ProduceRecord.class.getName());
		String lasttime = "";
		for (Object o : result) {
			if (o != null)
				lasttime = (String) o;
		}

		String now = DateTimeFun.getDateTimeSSS();
		Long num = Util698.getMilliSecondBetween_new(now, lasttime);
		Util698.log(WatchDogOld.class.getName(), "看门狗软件【监控】" + "最后测试时间" + lasttime + " 监控时间" + now + " 间隔时间" + num,
				Debug.LOG_INFO);

		// 如果大于等待时间
		if (num > waitTime) {
			if (isRunning(ProgramName))
				KillProgram(ProgramName);

			UserManager userManager = SoftParameter.getInstance().getUserManager();
			RunProgram(ProgramName + " " + userManager.getUserid() + " " + userManager.getUserpwd());
			RunProgram(ProgramName + " admin 6867");
		}
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
					Util698.log(WatchDogOld.class.getName(), "看门狗软件【检测】 " + processName + "正在运行...", Debug.LOG_INFO);
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
			Util698.log(WatchDogOld.class.getName(), "看门狗软件【Kill】" + processName, Debug.LOG_INFO);
		} catch (IOException e) {
			e.printStackTrace();
		} // 执行命令
	}

	private static void RunProgram(String processName) {
		String cmd = processName; // 关闭软件后，自动重新执行
		Runtime rt = Runtime.getRuntime(); // 获取运行时系统
		 Util698.log(WatchDogOld.class.getName(), "看门狗软件【启动】" + processName, Debug.LOG_INFO);
		try {
			Process proc = rt.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		} // 执行命令

	}

}
