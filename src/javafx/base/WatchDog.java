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

	// �ж��Ƿ���и�Ԥ��ʱ��
	static Boolean firstStop = false;

	String ver = "0.40";
	int WINDOWWIDTH = 700, WINDOWHEIGHT = 500;

	Label msg1 = new Label("");
	String msg1_txt = "", msg1_txt0 = "";
	HighFreqDraw highFreqDraw = null, highFreqDraw1 = null;
	Boolean ISRUNNING = true;

	@Override
	public void start(Stage primaryStage) throws Exception {

		Util698.log(WatchDog.class.getName(), "�������Ź����", Debug.LOG_INFO);

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

			primaryStage.setTitle("�������Ը���ϵͳ ������");

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					ISRUNNING = false;
					// highFreqDraw.misfire();
					Util698.log(WatchDog.class.getName(), "�رտ��Ź����", Debug.LOG_INFO);

					// ʹ��hibernate�ķ�ʽ�������ݲ���
					SessionFactoryTone.getInstance().close();

					// ����ر��˴˴��ڣ���ȫ���رճ���
					Platform.exit();
					System.exit(0);
				}
			});

			primaryStage.show();

			// �ο� https://blog.csdn.net/chuan_yu_chuan/article/details/53395626
//			ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
//			long initialDelay = 0;
////			long period = 30;
////			// ÿ��n����ִ��һ��job1����2���ڵ�һ��ִ����ɺ�n��
////			service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.SECONDS);
//			long period = 4;
//			// ÿ��n����ִ��һ��job1����2���ڵ�һ��ִ����ɺ�n��
//			service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.MINUTES);

			Task task = new Task() {
			    @Override
			    protected Object call() throws Exception {
			        while (true) {
						progressService.setFirstStop(firstStop);
						String msg = progressService.GetAndOperate();
			            this.updateMessage(msg);
						firstStop = false;
						FileToWrite.writeLocalFile1("logs\\ʵʱ���log.log", msg);
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
			// xuky 2018.06.11 �ؼ�����
			firstStop = false;
			FileToWrite.writeLocalFile1("logs\\ʵʱ���log.log", msg);
		}
	}


	public static void main(String[] args) {
		// ������ε����������ж��Ƿ�Ϊ��������
		if (args.length > 0) {
			firstStop = true;
			if (!args[0].equals("1")) {
				// �����β���1����˼������Ҫ�ָ���֮ǰ���ط�����״̬
				Client.getInstance().setSDATA(args[0]);
				Util698.log(WatchDog.class.getName(), "���Ź���������ݲ�����" + args[0], Debug.LOG_INFO);
				Util698.log(Server.class.getName(), "���𷽣�WatchDog", Debug.LOG_INFO);
				Client.getInstance().sendData();
			}
		}

		launch(args);
	}

}
