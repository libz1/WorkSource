package javafx.base;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import SocketAssident.Client;
import SocketAssident.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.assident.MessageCenter;
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

//public class LinkRemain extends Application implements Observer {
public class LinkRemain extends Application {

	String ver = "0.12";
	int WINDOWWIDTH = 700, WINDOWHEIGHT = 500;
	String msg3_txt = "", msg3_txt0 = "";
	String msg5_txt = "", msg5_txt0 = "";
//	HighFreqDraw highFreqDraw = null;
	Boolean ISRUNNING = true;

	Label msg1 = new Label("");
	Label msg2 = new Label("");
	Label msg3 = new Label("");
	Label msg4 = new Label("");
	Label msg5 = new Label("");
	Label msg6 = new Label("");
	Label msg7 = new Label("");

	@Override
	public void start(Stage primaryStage) throws Exception {

		// 与PublisherUI建立关系，以便展示各种状态信息
//		PublisherUI.getInstance().addObserver(this);

		Util698.log(LinkRemain.class.getName(), "开启链路维持软件", Debug.LOG_INFO);

		// 绘制界面信息
		{
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));
			Scene scene = new Scene(grid, 400, 300);
			primaryStage.setScene(scene);

			Text scenetitle = new Text("生产测试辅助系统 链路维持软件  ver " + ver);
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

			primaryStage.setTitle("生产测试辅助系统 链路维持软件");

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
//					highFreqDraw.misfire();
					ISRUNNING = false;
					Util698.log(LinkRemain.class.getName(), "关闭链路维持软件", Debug.LOG_INFO);

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

			// highFreqDraw = new HighFreqDraw(null);
			// highFreqDraw.advance(new DrawUI());

			primaryStage.show();

			// 参考 https://www.cnblogs.com/dyllove98/archive/2013/06/23/3151268.html
//			new Thread(() -> {
//				while (ISRUNNING) {
//					msg3_txt = MessagCenter.getInstance().LinkRemain_Server;
//					msg5_txt = MessagCenter.getInstance().LinkRemain_Client;
//
//
//					if (!msg3_txt.equals(msg3_txt0)) {
//						Platform.runLater(() -> msg3.setText(msg3_txt));
//						msg3_txt0 = msg3_txt;
//					}
//					if (!msg5_txt.equals(msg5_txt0)) {
//						Platform.runLater(() -> msg5.setText(msg5_txt));
//						msg5_txt0 = msg5_txt;
//					}
//					Debug.sleep(300);
//				}
//			}).start();

//			// 参考 https://blog.csdn.net/chuan_yu_chuan/article/details/53395626
//			ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
//			long initialDelay = 0;
//			long period = 300;
//			// 每隔n秒钟执行一次job1，第2次在第一次执行完成后n秒
//			service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.MILLISECONDS);

		}

		MessageCenter.getInstance();
		Server.getInstance();

		BindText();
		Debug.sleep(500);

		msg1.setText("开启链路维持代码" + DateTimeFun.getDateTimeSSS());
		msg2.setText("侦听-" + Server.getInstance().getIP_ADDR() + ":" + Server.getInstance().getPORT());
		// msg3是侦听的状态
		msg4.setText("连接-" + Client.getInstance().getIP_ADDR() + ":" + Client.getInstance().getPORT());
		// msg4是连接的状态


	}

	private void BindText() {
		Task task_3 = new Task() {
		    @Override
		    protected Object call() throws Exception {
		        while (true) {
		            this.updateMessage(MessageCenter.getInstance().LinkRemain_Server);
		            Debug.sleep(200);
		        }
		    }
		};
		Thread t_3 = new Thread(task_3);
		msg3.textProperty().bind(task_3.messageProperty());
		t_3.start();

		Task task_5 = new Task() {
		    @Override
		    protected Object call() throws Exception {
		        while (true) {
		            this.updateMessage(MessageCenter.getInstance().LinkRemain_Client);
		            Debug.sleep(200);
		        }
		    }
		};
		Thread t_5 = new Thread(task_5);
		msg5.textProperty().bind(task_5.messageProperty());
		t_5.start();
	}
	class MyScheduledExecutor implements Runnable {
		@Override
		public void run() {
			msg3_txt = MessageCenter.getInstance().LinkRemain_Server;
			msg5_txt = MessageCenter.getInstance().LinkRemain_Client;

			if (!msg3_txt.equals(msg3_txt0)) {
				Platform.runLater(() -> msg3.setText(msg3_txt));
				msg3_txt0 = msg3_txt;
			}
			if (!msg5_txt.equals(msg5_txt0)) {
				Platform.runLater(() -> msg5.setText(msg5_txt));
				msg5_txt0 = msg5_txt;
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

//	@Override
//	public void update(Observable arg0, Object arg) {
//		try {
//			Object[] s = (Object[]) arg;
//			if (s[0].equals("LinkRemain")) {
//				showMsg(arg);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	// // 1、定义内部类，实现Updatable接口，实现UI更新 实现UI对象与变更数据的绑定
	// class DrawUI implements HighFreqDrawUpdate {
	// @Override
	// public void update(double moment) {
	// // 如果数据没有更新，就没有必要进行setText操作
	// if (!msg3_txt.equals(msg3_txt0)) {
	// msg3.setText(msg3_txt);
	// msg3_txt0 = msg3_txt;
	// }
	// if (!msg5_txt.equals(msg5_txt0)) {
	// msg5.setText(msg5_txt);
	// msg5_txt0 = msg5_txt;
	// }
	// }
	// }

//	private void showMsg(Object arg) {
//		Object[] s = (Object[]) arg;
//		if (s[1].equals("Server")) {
//			// 1、直接调用
//			// 参考http://www.it1352.com/543057.html
//			// 原因是你在FX应用程序线程上安排了太多的 Runnable ，它没有时间做正常的工作（渲染UI，响应用户输入，等）
//			// Platform.runLater(()->msg3.setText((String) s[2]));
//
//			// 写入变量，在内部类DrawUI中进行UI更新
//			msg3_txt = (String) s[2];
//
//		}
//		if (s[1].equals("Client")) {
//			// 写入变量，在内部类DrawUI中进行UI更新
//			msg5_txt = (String) s[2];
//		}
//	}
}
