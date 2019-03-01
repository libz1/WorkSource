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

	// 判断是否进行干预的时间
	static Boolean firstStop = false;

	// 判断是否需要进行重启的时间
	Long waitTime = Long.valueOf(1000 * 60 * 4);
//	Long waitTime = Long.valueOf(1000 * 60);

	String ProgramName = "ProduceWare.exe";
	String LinkRemain = "LinkRemain.exe";


	String ver = "0.20";
	int WINDOWWIDTH = 700, WINDOWHEIGHT = 500;

	Label msg1 = new Label("最后测试时间");
	Label msg2 = new Label("");
	Label msg3 = new Label("监控时间");
	Label msg4 = new Label("");
	Label msg5 = new Label("间隔时间");
	Label msg6 = new Label("");
	Label msg7 = new Label("");

	@Override
	public void start(Stage primaryStage) throws Exception {

		Util698.log(WatchDogOld.class.getName(), "开启看门狗软件", Debug.LOG_INFO);


		// xuky 2018.05.04 自动启动链路维持程序
//		if (!isRunning(LinkRemain)){
//			RunProgram(LinkRemain);
//		}


		// 绘制界面信息
		{
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));
			Scene scene = new Scene(grid, 400, 300);
			primaryStage.setScene(scene);

			Text scenetitle = new Text("生产测试辅助系统 监控软件  ver " + ver);
			scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 18));
			grid.add(scenetitle, 0, 0, 2, 1);

			// 创建Label对象，放到第0列，第1行
			grid.add(msg1, 0, 1);
			grid.add(msg2, 0, 2);
			grid.add(msg3, 0, 3);
			grid.add(msg4, 0, 4);
			grid.add(msg5, 0, 5);
			grid.add(msg6, 0, 6);
			grid.add(msg7, 0, 7);

			primaryStage.setTitle("生产测试辅助系统 监控软件");

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					Util698.log(WatchDogOld.class.getName(), "关闭看门狗软件", Debug.LOG_INFO);

					// // 数据库连接进行关闭 使用JDBC的方式进行数据操作
					// DB.getInstance().close();
					// // xuky 2016.09.02 关闭前删除运行sqlite产生的临时文件
					// Util698.deleteFiles(System.getProperty("user.dir"),
					// "etilqs_");

					// 使用hibernate的方式进行数据操作
					SessionFactoryTone.getInstance().close();

					// 如果关闭了此窗口，则全部关闭程序
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

		// 开启链路维持代码
//		Server.getInstance();
//		msg7.setText("开启链路维持代码");

	}


	public void watching() {
		// http://blog.csdn.net/xinyuan_java/article/details/51602088
		// 间隔时间为6分钟
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

		// 1、判断程序在不在
		// 1.1、如果不在就自动执行它
		// 1.2、如果程序在，判断是否在正常执行，检查某些表中的数据，如果最大时间距离当前时间较久，首先杀死原先的程序，然后重新执行它
		// select max(beginOpt) from producerecord;
		// 距离当前时间超过5分钟

		// 1、直接连接数据库，判断测试进展
		// 如果距离当前时间超过5分钟，未进行任何操作，开启干预流程
		// 干预流程：
		// 1、判断程序是否存在，如果存在就kill
		// 2、从参数文件中获取用户信息，带参数执行程序

		IBaseDao<ProduceRecord> iBaseDao_ProduceRecord = new ProduceRecordDaoImpl();
		List result = iBaseDao_ProduceRecord.retrieveBySQL("select max(beginOpt) from " + ProduceRecord.class.getName()+ " where opResult='测试成功'");
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

		Util698.log(WatchDogOld.class.getName(), "看门狗软件【监控】" + "最后测试时间" + lasttime + " 监控时间" + now + " 间隔时间" + num,
				Debug.LOG_INFO);
//		SessionFactoryTone.getInstance().close();

		// 带参数运行时，只是展示数据，而不进行流程干预
		// 因为前一个关闭了看门狗程序已经启动了测试程序，测试程序执行生效还需要一定的时间

		// 如果大于等待时间
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



		// xuky 2018.05.03  不重启自身程序 如果不关闭当前的看门狗程序，启动的ProgramName就会异常停顿在启动串口处
		// 必须关闭看门狗，关闭前，将Client中的相关参数进行传递

		// 默认的入参是1
		String para = " 1";
		// 只要判定需要重发，就设置重发的内容  此时的入参是需要发送的报文数据
		if (Client.getInstance().getSend_time1() < Client.getInstance().getRetry()){
			para = " "+Client.getInstance().getSDATA();
		}
//		Server.getInstance().disconnect();

		RunProgram("watchdog.exe"+ para);
		Debug.sleep(3000);
		System.exit(0);
	}

	private Boolean isRunning(String processName) {
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

	private void KillProgram(String processName) {
		String cmd = "taskkill /im " + processName + " /f "; // 关闭软件
		Runtime rt = Runtime.getRuntime(); // 获取运行时系统
		try {
			Process proc = rt.exec(cmd);
			Util698.log(WatchDogOld.class.getName(), "看门狗软件【Kill】" + processName, Debug.LOG_INFO);
		} catch (IOException e) {
			e.printStackTrace();
		} // 执行命令
	}

	private void RunProgram(String processName) {
		Util698.log(WatchDogOld.class.getName(), "看门狗软件【启动软件】" + processName, Debug.LOG_INFO);
		String cmd = processName; // 关闭软件后，自动重新执行
		Runtime rt = Runtime.getRuntime(); // 获取运行时系统
		try {
			Process proc = rt.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		} // 执行命令

	}

	public static void main(String[] args) {
		// 根据入参的数量进行判断是否为初次运行
		if (args.length > 0) {
			firstStop = true;
			if (!args[0].equals("1")){
				// 如果入参不是1，意思就是需要恢复到之前的重发报文状态
				Client.getInstance().setSDATA(args[0]);
				Util698.log(WatchDogOld.class.getName(), "看门狗软件【传递参数】" + args[0], Debug.LOG_INFO);
				Util698.log(Server.class.getName(), "发起方：WatchDog", Debug.LOG_INFO);
				Client.getInstance().sendData();
			}
		}

		launch(args);
	}
}
