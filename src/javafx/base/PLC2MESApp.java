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
		String softName = "II��תMES";

		Util698.log(PLC2MESApp.class.getName(), "����"+softName+"���", Debug.LOG_INFO);

		// ���ƽ�����Ϣ
		{
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));
			Scene scene = new Scene(grid, 400, 300);
			primaryStage.setScene(scene);

			Text scenetitle = new Text("�������Ը���ϵͳ  II��תMES  ver " + ver);
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

			primaryStage.setTitle("�������Ը���ϵͳ "+ softName);

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					ISRUNNING = false;
//					highFreqDraw.misfire();

					Util698.log(PLC2MESApp.class.getName(), "�ر�"+softName+"���", Debug.LOG_INFO);

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
			PLC2MESThread.getInstance();

			// �ο� https://blog.csdn.net/chuan_yu_chuan/article/details/53395626
			ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
			long initialDelay = 0;
			long period = 300;
			// ÿ��n����ִ��һ��job1����2���ڵ�һ��ִ����ɺ�n��
			service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.MILLISECONDS);
		}

		Debug.sleep(500);
		msg1.setText("����"+softName+"���"+DateTimeFun.getDateTimeSSS());
//		msg2.setText("����-"+ClientSimulator.getInstance().getIP_ADDR()+":"+ClientSimulator.getInstance().getPORT());
	}

	class MyScheduledExecutor implements Runnable {
		@Override
		public void run() {
			msg3_txt = MessageCenter.getInstance().PLC2MES_msg;
			if (!msg3_txt.equals(msg3_txt0)) {
				Platform.runLater(() -> msg3.setText("ϵͳ��������..."+msg3_txt));
				msg3_txt0 = msg3_txt;
			}
		}
	}


	public static void main(String[] args) {
		launch(args);
	}

}
