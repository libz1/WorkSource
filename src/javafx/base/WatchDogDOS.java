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

	// �ж��Ƿ���и�Ԥ��ʱ��
	static Long waitTime = Long.valueOf(1000 * 60 * 2);
	// Long waitTime = Long.valueOf(1000 * 60);

	static String ProgramName = "ProduceWare.exe";

	static String ver = "0.02";

	public static void start() {

		// Util698.log(WatchDog.class.getName(), "���Ź���������С�ver " + ver,
		// Debug.LOG_INFO);
		new Thread(() -> {
			watching();
		}).start();

	}

	public static void watching() {
		// http://blog.csdn.net/xinyuan_java/article/details/51602088
		// ���ʱ��Ϊ6����
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
		Util698.log(WatchDogOld.class.getName(), "���Ź��������ء�" + "������ʱ��" + lasttime + " ���ʱ��" + now + " ���ʱ��" + num,
				Debug.LOG_INFO);

		// ������ڵȴ�ʱ��
		if (num > waitTime) {
			if (isRunning(ProgramName))
				KillProgram(ProgramName);

			UserManager userManager = SoftParameter.getInstance().getUserManager();
			RunProgram(ProgramName + " " + userManager.getUserid() + " " + userManager.getUserpwd());
			RunProgram(ProgramName + " admin 6867");
		}
	}

	private static Boolean isRunning(String processName) {
		// �����ж�QQ.exe�����Ƿ������У����򷵻�true
		BufferedReader br = null;
		try {
			Process proc = Runtime.getRuntime().exec("tasklist -fi " + '"' + "imagename eq " + processName + '"');
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				// �ж�ָ���Ľ����Ƿ�������
				if (line.contains(processName)) {
					Util698.log(WatchDogOld.class.getName(), "���Ź��������⡿ " + processName + "��������...", Debug.LOG_INFO);
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
		String cmd = "taskkill /im " + processName + " /f "; // �ر����
		Runtime rt = Runtime.getRuntime(); // ��ȡ����ʱϵͳ
		try {
			Process proc = rt.exec(cmd);
			Util698.log(WatchDogOld.class.getName(), "���Ź������Kill��" + processName, Debug.LOG_INFO);
		} catch (IOException e) {
			e.printStackTrace();
		} // ִ������
	}

	private static void RunProgram(String processName) {
		String cmd = processName; // �ر�������Զ�����ִ��
		Runtime rt = Runtime.getRuntime(); // ��ȡ����ʱϵͳ
		 Util698.log(WatchDogOld.class.getName(), "���Ź������������" + processName, Debug.LOG_INFO);
		try {
			Process proc = rt.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		} // ִ������

	}

}
