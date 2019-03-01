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

		// ��PublisherUI������ϵ���Ա�չʾ����״̬��Ϣ
//		PublisherUI.getInstance().addObserver(this);

		Util698.log(LinkRemain.class.getName(), "������·ά�����", Debug.LOG_INFO);

		// ���ƽ�����Ϣ
		{
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));
			Scene scene = new Scene(grid, 400, 300);
			primaryStage.setScene(scene);

			Text scenetitle = new Text("�������Ը���ϵͳ ��·ά�����  ver " + ver);
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

			primaryStage.setTitle("�������Ը���ϵͳ ��·ά�����");

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
//					highFreqDraw.misfire();
					ISRUNNING = false;
					Util698.log(LinkRemain.class.getName(), "�ر���·ά�����", Debug.LOG_INFO);

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

			// highFreqDraw = new HighFreqDraw(null);
			// highFreqDraw.advance(new DrawUI());

			primaryStage.show();

			// �ο� https://www.cnblogs.com/dyllove98/archive/2013/06/23/3151268.html
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

//			// �ο� https://blog.csdn.net/chuan_yu_chuan/article/details/53395626
//			ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
//			long initialDelay = 0;
//			long period = 300;
//			// ÿ��n����ִ��һ��job1����2���ڵ�һ��ִ����ɺ�n��
//			service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.MILLISECONDS);

		}

		MessageCenter.getInstance();
		Server.getInstance();

		BindText();
		Debug.sleep(500);

		msg1.setText("������·ά�ִ���" + DateTimeFun.getDateTimeSSS());
		msg2.setText("����-" + Server.getInstance().getIP_ADDR() + ":" + Server.getInstance().getPORT());
		// msg3��������״̬
		msg4.setText("����-" + Client.getInstance().getIP_ADDR() + ":" + Client.getInstance().getPORT());
		// msg4�����ӵ�״̬


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

	// // 1�������ڲ��࣬ʵ��Updatable�ӿڣ�ʵ��UI���� ʵ��UI�����������ݵİ�
	// class DrawUI implements HighFreqDrawUpdate {
	// @Override
	// public void update(double moment) {
	// // �������û�и��£���û�б�Ҫ����setText����
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
//			// 1��ֱ�ӵ���
//			// �ο�http://www.it1352.com/543057.html
//			// ԭ��������FXӦ�ó����߳��ϰ�����̫��� Runnable ����û��ʱ���������Ĺ�������ȾUI����Ӧ�û����룬�ȣ�
//			// Platform.runLater(()->msg3.setText((String) s[2]));
//
//			// д����������ڲ���DrawUI�н���UI����
//			msg3_txt = (String) s[2];
//
//		}
//		if (s[1].equals("Client")) {
//			// д����������ڲ���DrawUI�н���UI����
//			msg5_txt = (String) s[2];
//		}
//	}
}
