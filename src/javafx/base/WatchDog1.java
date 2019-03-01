package javafx.base;

import java.io.BufferedWriter;
import java.io.FileWriter;

import com.eastsoft.fio.FileToWrite;
import com.eastsoft.util.Debug;

import SocketAssident.Client;
import SocketAssident.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.assident.HighFreqDraw;
import javafx.assident.HighFreqDrawUpdate;
import javafx.assident.MessageCenter;
import javafx.assident.WatchDogThread;
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
import util.Util698;

public class WatchDog1 extends Application {

	// 判断是否进行干预的时间
	static Boolean firstStop = false;

	String ver = "0.40";
	int WINDOWWIDTH = 700, WINDOWHEIGHT = 500;

	Label msg1 = new Label("");
	String msg1_txt = "", msg1_txt0 = "";
	HighFreqDraw highFreqDraw = null, highFreqDraw1 = null;
	Boolean ISRUNNING = true;

	@Override
	public void start(Stage primaryStage) throws Exception {

		Util698.log(WatchDog1.class.getName(), "开启看门狗软件", Debug.LOG_INFO);

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

			primaryStage.setTitle("生产测试辅助系统 监控软件");

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					ISRUNNING = false;
					// highFreqDraw.misfire();
					Util698.log(WatchDog1.class.getName(), "关闭看门狗软件", Debug.LOG_INFO);

					// 使用hibernate的方式进行数据操作
					SessionFactoryTone.getInstance().close();

					// 如果关闭了此窗口，则全部关闭程序
					Platform.exit();
					System.exit(0);
				}
			});

			primaryStage.show();

			// xuky 2018.06.04 不在界面程序中进行定时器操作，放在其他程序中
			WatchDogThread.getInstance(firstStop);

			// 参考
			// https://www.cnblogs.com/dyllove98/archive/2013/06/23/3151268.html
			// new Thread(() -> {
			// while (ISRUNNING) {
			// msg1_txt = MessagCenter.getInstance().getWatchDog_msg();
			// if (!msg1_txt.equals(msg1_txt0)) {
			// Platform.runLater(() -> msg1.setText(msg1_txt));
			//// msg1.setText(msg1_txt);
			// msg1_txt0 = msg1_txt;
			// }
			// Debug.sleep(300);
			// }
			// }).start();

			// xuky 2018.06.05
			highFreqDraw = new HighFreqDraw(null);
			highFreqDraw.advance(new DrawUI());

			highFreqDraw1 = new HighFreqDraw(null);
			highFreqDraw1.advance(new DrawUI1(),1000);

			// 有一定效果，但是无法完全解决
			// Timer timer = new Timer();
			// timer.scheduleAtFixedRate(new TimerTask() {
			// public void run() {
			// Platform.runLater(() -> {
			// msg1.setText(msg1_txt);
			// });
			// }
			// }, 100, 1000 );
		}
	}

	// 1、定义内部类，实现Updatable接口，实现UI更新 实现UI对象与变更数据的绑定
	class DrawUI implements HighFreqDrawUpdate {
		@Override
		public void update(double moment) {
			msg1_txt = MessageCenter.getInstance().WatchDog_msg;
			// 如果数据没有更新，就没有必要进行setText操作
			if (!msg1_txt.equals(msg1_txt0)) {
//				writeFile(msg1_txt);
				FileToWrite.writeLocalFile1("logs\\实时监控log.log", msg1_txt);
				msg1.setText(msg1_txt);
				msg1_txt0 = msg1_txt;
				// xuky 2018.06。05异常：当执行定时任务时，频繁进行界面切换，就会导致界面无响应，直至下次执行
				// 如果界面无响应，下次执行定时任务时，界面会自动恢复
				// 无效
				// Debug.sleep(100);
				// msg1.setText(msg1_txt);
				// Debug.sleep(100);
				// msg1.setText(msg1_txt);
			}
		}
	}

	private static void writeFile(String data){
		FileWriter writer;
		try {
			writer = new FileWriter("c://test2.txt");
	        BufferedWriter bw = new BufferedWriter(writer);
	        bw.write(data);

	        bw.close();
	        writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
       }

	// // 有一定效果，但是无法完全解决  某些情况下，界面隐藏了，打开时无效
	// // 1、定义内部类，实现Updatable接口，实现UI更新 实现UI对象与变更数据的绑定
	class DrawUI1 implements HighFreqDrawUpdate {
		@Override
		public void update(double moment) {
			msg1.setText(msg1_txt);
		}
	}

	public static void main(String[] args) {
		// 根据入参的数量进行判断是否为初次运行
		if (args.length > 0) {
			firstStop = true;
			if (!args[0].equals("1")) {
				// 如果入参不是1，意思就是需要恢复到之前的重发报文状态
				Client.getInstance().setSDATA(args[0]);
				Util698.log(WatchDog1.class.getName(), "看门狗软件【传递参数】" + args[0], Debug.LOG_INFO);
				Util698.log(Server.class.getName(), "发起方：WatchDog", Debug.LOG_INFO);
				Client.getInstance().sendData();
			}
		}

		launch(args);
	}

}
