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


// �ο�https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Service.html
// �ο�https://blog.csdn.net/xby1993/article/details/24811099 FirstLineService service = new FirstLineService();  ....
// �ο� https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/concurrency.htm#BABCHBEA   FirstLineService...
public class ProgressService extends Service<String>{
	static String lastTest = "";
	static String lastMoni = "";
	static String lastTime = "";
	static String lastTest_txt = "������ʱ��";
	static String lastMoni_txt = "�����ʱ��";
	static String lastTime_txt = "���Լ��ʱ��";
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
				"select max(beginOpt) from " + ProduceRecord.class.getName() + " where opResult='���Գɹ�'");
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

		// �ؼ����룬����ֱ�ӽ��н��������д
//		updateMessage(WriteInfo());
		WriteInfo();


		// ������ڵȴ�ʱ��
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

		// xuky 2018.05.03 ������������� ������رյ�ǰ�Ŀ��Ź�����������ProgramName�ͻ��쳣ͣ�����������ڴ�
		// ����رտ��Ź����ر�ǰ����Client�е���ز������д���

		// Ĭ�ϵ������1
		String para = " 1";
		// ֻҪ�ж���Ҫ�ط����������ط������� ��ʱ���������Ҫ���͵ı�������
		if (Client.getInstance().getSend_time1() < Client.getInstance().getRetry()) {
			para = " " + Client.getInstance().getSDATA();
		}
		// Server.getInstance().disconnect();

		RunProgram("watchdog.exe" + para);
		Debug.sleep(3000);
		System.exit(0);
	}
	private static void RunProgram(String processName) {
		Util698.log(ProgressService.class.getName(), "���Ź���������������" + processName, Debug.LOG_INFO);
		String cmd = processName; // �ر�������Զ�����ִ��
		Runtime rt = Runtime.getRuntime(); // ��ȡ����ʱϵͳ
		try {
			Process proc = rt.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		} // ִ������

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
					Util698.log(ProgressService.class.getName(), "���Ź��������⡿ " + processName + "��������...", Debug.LOG_INFO);
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
			Util698.log(ProgressService.class.getName(), "���Ź������Kill��" + processName, Debug.LOG_INFO);
		} catch (IOException e) {
			e.printStackTrace();
		} // ִ������
	}

}
