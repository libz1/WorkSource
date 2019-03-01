package produce.deal;

import java.net.URL;

import com.eastsoft.util.Debug;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.base.JFXMain;
import javafx.base.SessionFactoryTone;
import javafx.base.javafxutil;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import util.DB;
import util.PublisherShowList;
import util.PublisherUI;
import util.Util698;

public class TerminalParameter extends Application {
	Stage stage;
	private Boolean IS_AUTO = false;
	TerminalParameterController control = null;


	// 1���첽�������̣� ��2�� �������˶��ʵ��ʱ�����մ������ݻ�����ظ����������ʹ�õ���ģʽ
	private volatile static TerminalParameter uniqueInstance;
	public static TerminalParameter getInstance() {
		if (uniqueInstance == null) {
			synchronized (TerminalParameter.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new TerminalParameter();
				}
			}
		}

		// ʵ�ִ��ڵ�ǰ��Ч��
		// TerminalParameter�в�����Platform.runLater����
		uniqueInstance.setAlwaysOnTop(true);
		uniqueInstance.setAlwaysOnTop(false);

		return uniqueInstance;
	}

	public static TerminalParameter getInstance(Boolean isAuto) {
		if (uniqueInstance == null) {
			synchronized (TerminalParameter.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new TerminalParameter(isAuto);
				}
			}
		}

		// ʵ�ִ��ڵ�ǰ��Ч��
		// TerminalParameter�в�����Platform.runLater����
		uniqueInstance.setAlwaysOnTop(true);
		uniqueInstance.setAlwaysOnTop(false);

		return uniqueInstance;
	}

	private TerminalParameter() {
		// �������ͺ���߳��п�������ֹswing��javafx��ͻ
		// xuky 2017.04.05 ��MainPanel�е���ʱ���Ѿ���������Platform.runLater
		// ���������������ᵼ�º�����setAlwaysOnTop(true)����
//		Platform.runLater(() -> {
			init0();
//		});
	}

	public TerminalParameter(Boolean isAuto) {
		IS_AUTO = isAuto;
		init0();
	}

	private void init0() {
//		Util698.log(TerminalParameter.class.getName(), "�������Դ���", Debug.LOG_INFO);
		stage = new Stage();

		// xuky 2017.04.05 ͨ��MainPanel���˴��ڣ����رմ��ڣ����Ǵ˶���δʵ���ͷ�
		// ͨ�����´�������ͷ�
		// �ο�http://blog.csdn.net/huplion/article/details/52718372
		//
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
            	if (!javafxutil.f_alert_confirmDialog("�ر�����","��ȷ����Ҫ�رմ˹���ģ�飡")){
            		event.consume();
            		return;
            	}
            	else{
                    Util698.log(TerminalParameter.class.getName(), "�û��������ر��������",Debug.LOG_INFO);
            	}


                Util698.log(TerminalParameter.class.getName(), "PublisherUI.getInstance().deleteObserver(control)",Debug.LOG_INFO);
                PublisherUI.getInstance().deleteObserver(control);
                PublisherShowList.getInstance().deleteObserver(control);
                control.pool.shutdown();
            	if (!IS_AUTO){
                	uniqueInstance = null;
                    stage.close();
            	}
            	else{
        			Util698.log(JFXMain.class.getName(), "�ر����3", Debug.LOG_INFO);

        			// ���ݿ����ӽ��йر� ʹ��JDBC�ķ�ʽ�������ݲ���
        			DB.getInstance().close();
        			// ʹ��hibernate�ķ�ʽ�������ݲ���
        			SessionFactoryTone.getInstance().close();

        			// xuky 2016.09.02 �ر�ǰɾ������sqlite��������ʱ�ļ�
        			Util698.deleteFiles(System.getProperty("user.dir"), "etilqs_");

        			// ����ر��˴˴��ڣ���ȫ���رճ���
        			Platform.exit();
        			System.exit(0);
            	}
            }
        });

		Scene scene = null;
		{
			Parent root = null;
			try {
				// ���沼�ַ���fxml�ļ���
				// �ؼ����¼�������controler����
				URL location = getClass().getResource("TerminalParameter.fxml");
//				URL location = getClass().getResource("BorderPaneSample.fxml");
//				URL location = getClass().getResource("BorderPaneInTab.fxml");
//				URL location = getClass().getResource("BorderPaneInTab1.fxml");


				FXMLLoader fxmlLoader = new FXMLLoader();
				fxmlLoader.setLocation(location);
				fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
				root = (javafx.scene.Parent) fxmlLoader.load(location.openStream());

				// xuky 2017.02.14 ��ȡcontrol�Ŀ��ƾ��
				// �ο�http://blog.csdn.net/sunbirdhan/article/details/38501649
				// �����ؼ���Ҫ���ؼ������������ͨ��control.setParentStage(stage)���д���
				scene = new Scene(root, 800, 600);
				scene.getStylesheets().add(TerminalParameter.class.getResource("Border.css").toExternalForm());
				control = (TerminalParameterController) fxmlLoader.getController();
				if (control != null){
					control.setParentStage(stage);
					control.setParentScene(scene);
				}
				control.setIS_AUTO(IS_AUTO);
				control.init();


				// ͨ��fx:id������ָ���Ŀؼ�
				// Button button = (Button)root.lookup("#btn_add");
				// button.setOnAction(e -> System.out.println("add"));

			} catch (Exception e) {
				Util698.log(TerminalParameter.class.getName(), "init0 IOException "+e.getMessage(), Debug.LOG_INFO);

			}
		}
		stage.setTitle("II����������");
		stage.setScene(scene);
		// stage.setAlwaysOnTop(true);
		stage.show();

		Util698.log(TerminalParameter.class.getName(), "����ģ��'II����������'", Debug.LOG_INFO);

	}

	public void setAlwaysOnTop(Boolean top){
		stage.setAlwaysOnTop(top);

		// xuky 2017.07.06 ����û��ǵ����С���ģ�Ҳ����չʾ����
		stage.setIconified(false);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
//		init0();

	}

//	@Override
//	public void stop() throws Exception {
//		System.out.println("TerminalParameter.stop");
//	}

	public static void main(String[] args) {
		launch(args);
	}

}
