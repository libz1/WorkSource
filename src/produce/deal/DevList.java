package produce.deal;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.base.BaseController;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DevList extends Application {
	Stage stage;

	// 1���첽�������̣� ��2�� �������˶��ʵ��ʱ�����մ������ݻ�����ظ����������ʹ�õ���ģʽ
	private volatile static DevList uniqueInstance;
	public static DevList getInstance() {
		if (uniqueInstance == null) {
			synchronized (DevList.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new DevList();
				}
			}
		}

		// ʵ�ִ��ڵ�ǰ��Ч��
		// TerminalParameter�в�����Platform.runLater����
		uniqueInstance.setAlwaysOnTop(true);
		uniqueInstance.setAlwaysOnTop(false);

		return uniqueInstance;
	}

	private DevList() {
		// �������ͺ���߳��п�������ֹswing��javafx��ͻ
		// xuky 2017.04.05 ��MainPanel�е���ʱ���Ѿ���������Platform.runLater
		// ���������������ᵼ�º�����setAlwaysOnTop(true)����
//		Platform.runLater(() -> {
			init0();
//		});
	}

	private void init0() {
		stage = new Stage();

		// xuky 2017.04.05 ͨ��MainPanel���˴��ڣ����رմ��ڣ����Ǵ˶���δʵ���ͷ�
		// ͨ�����´�������ͷ�
		// �ο�http://blog.csdn.net/huplion/article/details/52718372
		//
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
            	uniqueInstance = null;
//                System.out.println("TerminalParameter setOnCloseRequest");
                stage.close();
            }
        });

		Scene scene = null;
		{
			Parent root = null;
			try {
				// ���沼�ַ���fxml�ļ���
				// �ؼ����¼�������controler����
				URL location = getClass().getResource("DevList.fxml");
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
				scene.getStylesheets().add(DevList.class.getResource("Border.css").toExternalForm());
				BaseController control = (BaseController) fxmlLoader.getController();
				if (control != null){
					control.setParentStage(stage);
					control.setParentScene(scene);
				}
				// control.table_init0();

				// ͨ��fx:id������ָ���Ŀؼ�
				// Button button = (Button)root.lookup("#btn_add");
				// button.setOnAction(e -> System.out.println("add"));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stage.setTitle("�豸��Ϣ��ѯ");
		stage.setScene(scene);
		// stage.setAlwaysOnTop(true);
		stage.show();
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
