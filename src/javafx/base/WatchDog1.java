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

		Util698.log(WatchDog1.class.getName(), "�������Ź����", Debug.LOG_INFO);

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
					Util698.log(WatchDog1.class.getName(), "�رտ��Ź����", Debug.LOG_INFO);

					// ʹ��hibernate�ķ�ʽ�������ݲ���
					SessionFactoryTone.getInstance().close();

					// ����ر��˴˴��ڣ���ȫ���رճ���
					Platform.exit();
					System.exit(0);
				}
			});

			primaryStage.show();

			// xuky 2018.06.04 ���ڽ�������н��ж�ʱ����������������������
			WatchDogThread.getInstance(firstStop);

			// �ο�
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

			// ��һ��Ч���������޷���ȫ���
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

	// 1�������ڲ��࣬ʵ��Updatable�ӿڣ�ʵ��UI���� ʵ��UI�����������ݵİ�
	class DrawUI implements HighFreqDrawUpdate {
		@Override
		public void update(double moment) {
			msg1_txt = MessageCenter.getInstance().WatchDog_msg;
			// �������û�и��£���û�б�Ҫ����setText����
			if (!msg1_txt.equals(msg1_txt0)) {
//				writeFile(msg1_txt);
				FileToWrite.writeLocalFile1("logs\\ʵʱ���log.log", msg1_txt);
				msg1.setText(msg1_txt);
				msg1_txt0 = msg1_txt;
				// xuky 2018.06��05�쳣����ִ�ж�ʱ����ʱ��Ƶ�����н����л����ͻᵼ�½�������Ӧ��ֱ���´�ִ��
				// �����������Ӧ���´�ִ�ж�ʱ����ʱ��������Զ��ָ�
				// ��Ч
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

	// // ��һ��Ч���������޷���ȫ���  ĳЩ����£����������ˣ���ʱ��Ч
	// // 1�������ڲ��࣬ʵ��Updatable�ӿڣ�ʵ��UI���� ʵ��UI�����������ݵİ�
	class DrawUI1 implements HighFreqDrawUpdate {
		@Override
		public void update(double moment) {
			msg1.setText(msg1_txt);
		}
	}

	public static void main(String[] args) {
		// ������ε����������ж��Ƿ�Ϊ��������
		if (args.length > 0) {
			firstStop = true;
			if (!args[0].equals("1")) {
				// �����β���1����˼������Ҫ�ָ���֮ǰ���ط�����״̬
				Client.getInstance().setSDATA(args[0]);
				Util698.log(WatchDog1.class.getName(), "���Ź���������ݲ�����" + args[0], Debug.LOG_INFO);
				Util698.log(Server.class.getName(), "���𷽣�WatchDog", Debug.LOG_INFO);
				Client.getInstance().sendData();
			}
		}

		launch(args);
	}

}
