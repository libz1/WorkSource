package javafx.base;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eastsoft.fio.FileToWrite;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import SocketAssident.Client;
import SocketAssident.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.assident.HighFreqDraw;
import javafx.assident.MessageCenter;
import javafx.assident.ProgressService;
import javafx.concurrent.Task;
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

public class WatchDog extends Application {

	ProgressService progressService = new ProgressService();

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

		Util698.log(WatchDog.class.getName(), "开启看门狗软件", Debug.LOG_INFO);

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
					Util698.log(WatchDog.class.getName(), "关闭看门狗软件", Debug.LOG_INFO);

					// 使用hibernate的方式进行数据操作
					SessionFactoryTone.getInstance().close();

					// 如果关闭了此窗口，则全部关闭程序
					Platform.exit();
					System.exit(0);
				}
			});

			primaryStage.show();

			// 参考 https://blog.csdn.net/chuan_yu_chuan/article/details/53395626
//			ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
//			long initialDelay = 0;
////			long period = 30;
////			// 每隔n秒钟执行一次job1，第2次在第一次执行完成后n秒
////			service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.SECONDS);
//			long period = 4;
//			// 每隔n秒钟执行一次job1，第2次在第一次执行完成后n秒
//			service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.MINUTES);

			Task task = new Task() {
			    @Override
			    protected Object call() throws Exception {
			        while (true) {
						progressService.setFirstStop(firstStop);
						String msg = progressService.GetAndOperate();
			            this.updateMessage(msg);
						firstStop = false;
						FileToWrite.writeLocalFile1("logs\\实时监控log.log", msg);
			            Debug.sleep(1000*60*4);
			        }
			    }
			};
			Thread t = new Thread(task);
			msg1.textProperty().bind(task.messageProperty());
			t.start();

		}
	}

	class MyScheduledExecutor implements Runnable {
		@Override
		public void run() {
			progressService.setFirstStop(firstStop);
			String msg = progressService.GetAndOperate();
			Platform.runLater(() -> {
				msg1.setText(msg);
			});
			// xuky 2018.06.11 关键代码
			firstStop = false;
			FileToWrite.writeLocalFile1("logs\\实时监控log.log", msg);
		}
	}


	public static void main(String[] args) {
		// 根据入参的数量进行判断是否为初次运行
		if (args.length > 0) {
			firstStop = true;
			if (!args[0].equals("1")) {
				// 如果入参不是1，意思就是需要恢复到之前的重发报文状态
				Client.getInstance().setSDATA(args[0]);
				Util698.log(WatchDog.class.getName(), "看门狗软件【传递参数】" + args[0], Debug.LOG_INFO);
				Util698.log(Server.class.getName(), "发起方：WatchDog", Debug.LOG_INFO);
				Client.getInstance().sendData();
			}
		}

		launch(args);
	}

}
