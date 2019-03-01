package javafx.base;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.assident.MessageCenter;
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

//public class PLCSimulator extends Application implements Observer{
public class PLC2MESApp extends Application{

	String ver = "0.01";
	int WINDOWWIDTH = 700, WINDOWHEIGHT = 500;

	Label msg1 = new Label("");
	Label msg2 = new Label("");
	Label msg3 = new Label("");
	Label msg4 = new Label("");
	Label msg5 = new Label("");
	Label msg6 = new Label("");
	Label msg7 = new Label("");
	String msg3_txt = "",msg3_txt0 = "";
//	HighFreqDraw highFreqDraw = null;
	Boolean ISRUNNING = true;

	@SuppressWarnings("static-access")
	@Override
	public void start(Stage primaryStage) throws Exception {
		String softName = "II采转MES";

		Util698.log(PLC2MESApp.class.getName(), "开启"+softName+"软件", Debug.LOG_INFO);

		// 绘制界面信息
		{
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));
			Scene scene = new Scene(grid, 400, 300);
			primaryStage.setScene(scene);

			Text scenetitle = new Text("生产测试辅助系统  II采转MES  ver " + ver);
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

			primaryStage.setTitle("生产测试辅助系统 "+ softName);

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					ISRUNNING = false;
//					highFreqDraw.misfire();

					Util698.log(PLC2MESApp.class.getName(), "关闭"+softName+"软件", Debug.LOG_INFO);

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
			PLC2MESThread.getInstance();

			// 参考 https://blog.csdn.net/chuan_yu_chuan/article/details/53395626
			ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
			long initialDelay = 0;
			long period = 300;
			// 每隔n秒钟执行一次job1，第2次在第一次执行完成后n秒
			service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.MILLISECONDS);
		}

		Debug.sleep(500);
		msg1.setText("开启"+softName+"软件"+DateTimeFun.getDateTimeSSS());
//		msg2.setText("连接-"+ClientSimulator.getInstance().getIP_ADDR()+":"+ClientSimulator.getInstance().getPORT());
	}

	class MyScheduledExecutor implements Runnable {
		@Override
		public void run() {
			msg3_txt = MessageCenter.getInstance().PLC2MES_msg;
			if (!msg3_txt.equals(msg3_txt0)) {
				Platform.runLater(() -> msg3.setText("系统正在运行..."+msg3_txt));
				msg3_txt0 = msg3_txt;
			}
		}
	}


	public static void main(String[] args) {
		launch(args);
	}

}
