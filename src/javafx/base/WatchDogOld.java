package javafx.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import SocketAssident.Client;
import SocketAssident.Server;
import dao.basedao.IBaseDao;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import produce.entity.ProduceRecord;
import produce.entity.ProduceRecordDaoImpl;
import produce.entity.UserManager;
import util.SoftParameter;
import util.Util698;

public class WatchDogOld extends Application {

	// �ж��Ƿ���и�Ԥ��ʱ��
	static Boolean firstStop = false;

	// �ж��Ƿ���Ҫ����������ʱ��
	Long waitTime = Long.valueOf(1000 * 60 * 4);
//	Long waitTime = Long.valueOf(1000 * 60);

	String ProgramName = "ProduceWare.exe";
	String LinkRemain = "LinkRemain.exe";


	String ver = "0.20";
	int WINDOWWIDTH = 700, WINDOWHEIGHT = 500;

	Label msg1 = new Label("������ʱ��");
	Label msg2 = new Label("");
	Label msg3 = new Label("���ʱ��");
	Label msg4 = new Label("");
	Label msg5 = new Label("���ʱ��");
	Label msg6 = new Label("");
	Label msg7 = new Label("");

	@Override
	public void start(Stage primaryStage) throws Exception {

		Util698.log(WatchDogOld.class.getName(), "�������Ź����", Debug.LOG_INFO);


		// xuky 2018.05.04 �Զ�������·ά�ֳ���
//		if (!isRunning(LinkRemain)){
//			RunProgram(LinkRemain);
//		}


		// ���ƽ�����Ϣ
		{
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));
			Scene scene = new Scene(grid, 400, 300);
			primaryStage.setScene(scene);

			Text scenetitle = new Text("�������Ը���ϵͳ ������  ver " + ver);
			scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 18));
			grid.add(scenetitle, 0, 0, 2, 1);

			// ����Label���󣬷ŵ���0�У���1��
			grid.add(msg1, 0, 1);
			grid.add(msg2, 0, 2);
			grid.add(msg3, 0, 3);
			grid.add(msg4, 0, 4);
			grid.add(msg5, 0, 5);
			grid.add(msg6, 0, 6);
			grid.add(msg7, 0, 7);

			primaryStage.setTitle("�������Ը���ϵͳ ������");

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					Util698.log(WatchDogOld.class.getName(), "�رտ��Ź����", Debug.LOG_INFO);

					// // ���ݿ����ӽ��йر� ʹ��JDBC�ķ�ʽ�������ݲ���
					// DB.getInstance().close();
					// // xuky 2016.09.02 �ر�ǰɾ������sqlite��������ʱ�ļ�
					// Util698.deleteFiles(System.getProperty("user.dir"),
					// "etilqs_");

					// ʹ��hibernate�ķ�ʽ�������ݲ���
					SessionFactoryTone.getInstance().close();

					// ����ر��˴˴��ڣ���ȫ���رճ���
					Platform.exit();
					System.exit(0);
				}
			});

			primaryStage.show();
//			Platform.runLater(new Runnable() {
//				@Override
//				public void run() {
//					watching();
//				}
//			});
			watching();

		}

		// ������·ά�ִ���
//		Server.getInstance();
//		msg7.setText("������·ά�ִ���");

	}


	public void watching() {
		// http://blog.csdn.net/xinyuan_java/article/details/51602088
		// ���ʱ��Ϊ6����
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				doSomething();
			}
		}, 100, 1000 * 60 * 2);


//			}, 100, 1000 * 1);
	}

	public void doSomething() {

		String now = DateTimeFun.getDateTimeSSS();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				msg4.setText(now);
			}
		});

		if (firstStop){
			firstStop = false;
			return;
		}

		// 1���жϳ����ڲ���
		// 1.1��������ھ��Զ�ִ����
		// 1.2����������ڣ��ж��Ƿ�������ִ�У����ĳЩ���е����ݣ�������ʱ����뵱ǰʱ��Ͼã�����ɱ��ԭ�ȵĳ���Ȼ������ִ����
		// select max(beginOpt) from producerecord;
		// ���뵱ǰʱ�䳬��5����

		// 1��ֱ���������ݿ⣬�жϲ��Խ�չ
		// ������뵱ǰʱ�䳬��5���ӣ�δ�����κβ�����������Ԥ����
		// ��Ԥ���̣�
		// 1���жϳ����Ƿ���ڣ�������ھ�kill
		// 2���Ӳ����ļ��л�ȡ�û���Ϣ��������ִ�г���

		IBaseDao<ProduceRecord> iBaseDao_ProduceRecord = new ProduceRecordDaoImpl();
		List result = iBaseDao_ProduceRecord.retrieveBySQL("select max(beginOpt) from " + ProduceRecord.class.getName()+ " where opResult='���Գɹ�'");
		String lasttime = "";
		for (Object o : result) {
			if (o != null)
				lasttime = (String) o;
		}
		String lasttime1 = lasttime;
		if (lasttime1.equals("")){
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					msg2.setText(lasttime1);
					msg4.setText(now);
//					msg6.setText(num.toString());
				}
			});

			reset();
			return;
		}



		Long num = Util698.getMilliSecondBetween_new(now, lasttime);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				msg2.setText(lasttime1);
				msg4.setText(now);
				msg6.setText(num.toString());
			}
		});

		Util698.log(WatchDogOld.class.getName(), "���Ź��������ء�" + "������ʱ��" + lasttime + " ���ʱ��" + now + " ���ʱ��" + num,
				Debug.LOG_INFO);
//		SessionFactoryTone.getInstance().close();

		// ����������ʱ��ֻ��չʾ���ݣ������������̸�Ԥ
		// ��Ϊǰһ���ر��˿��Ź������Ѿ������˲��Գ��򣬲��Գ���ִ����Ч����Ҫһ����ʱ��

		// ������ڵȴ�ʱ��
		if (num > waitTime) {
			reset();

		}

		// Boolean isRunning = isRunning(ProgramName);
		// System.out.println(ProgramName);
		//
		// if (!isRunning)
		// RunProgram(ProgramName);

	}


	private void reset() {
//		new Thread(() -> {

			if (isRunning(ProgramName)){
				KillProgram(ProgramName);
				Debug.sleep(1000);
			}

			UserManager userManager = SoftParameter.getInstance().getUserManager();
			RunProgram(ProgramName+" "+userManager.getUserid()+" "+userManager.getUserpwd());

//		}).start();



		// xuky 2018.05.03  ������������� ������رյ�ǰ�Ŀ��Ź�����������ProgramName�ͻ��쳣ͣ�����������ڴ�
		// ����رտ��Ź����ر�ǰ����Client�е���ز������д���

		// Ĭ�ϵ������1
		String para = " 1";
		// ֻҪ�ж���Ҫ�ط����������ط�������  ��ʱ���������Ҫ���͵ı�������
		if (Client.getInstance().getSend_time1() < Client.getInstance().getRetry()){
			para = " "+Client.getInstance().getSDATA();
		}
//		Server.getInstance().disconnect();

		RunProgram("watchdog.exe"+ para);
		Debug.sleep(3000);
		System.exit(0);
	}

	private Boolean isRunning(String processName) {
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

	private void KillProgram(String processName) {
		String cmd = "taskkill /im " + processName + " /f "; // �ر����
		Runtime rt = Runtime.getRuntime(); // ��ȡ����ʱϵͳ
		try {
			Process proc = rt.exec(cmd);
			Util698.log(WatchDogOld.class.getName(), "���Ź������Kill��" + processName, Debug.LOG_INFO);
		} catch (IOException e) {
			e.printStackTrace();
		} // ִ������
	}

	private void RunProgram(String processName) {
		Util698.log(WatchDogOld.class.getName(), "���Ź���������������" + processName, Debug.LOG_INFO);
		String cmd = processName; // �ر�������Զ�����ִ��
		Runtime rt = Runtime.getRuntime(); // ��ȡ����ʱϵͳ
		try {
			Process proc = rt.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		} // ִ������

	}

	public static void main(String[] args) {
		// ������ε����������ж��Ƿ�Ϊ��������
		if (args.length > 0) {
			firstStop = true;
			if (!args[0].equals("1")){
				// �����β���1����˼������Ҫ�ָ���֮ǰ���ط�����״̬
				Client.getInstance().setSDATA(args[0]);
				Util698.log(WatchDogOld.class.getName(), "���Ź���������ݲ�����" + args[0], Debug.LOG_INFO);
				Util698.log(Server.class.getName(), "���𷽣�WatchDog", Debug.LOG_INFO);
				Client.getInstance().sendData();
			}
		}

		launch(args);
	}
}
