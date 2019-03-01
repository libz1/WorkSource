package produce.deal;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TerminalCURD extends Application{
	public TerminalCURD(){
		// �������ͺ���߳��п�������ֹswing��javafx��ͻ
		Platform.runLater(()->{
			init0();
		});
	}

	private void init0() {
		Stage stage = new Stage();
		Scene scene = null;
		{
	        Parent root = null;
			try {
				// ���沼�ַ���fxml�ļ���
				// �ؼ����¼�������controler����
				URL location = getClass().getResource("TerminalCURD.fxml");
		        FXMLLoader fxmlLoader = new FXMLLoader();
		        fxmlLoader.setLocation(location);
		        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		        root = (javafx.scene.Parent) fxmlLoader.load(location.openStream());

		        // xuky 2017.02.14 ��ȡcontrol�Ŀ��ƾ��
		        // �ο�http://blog.csdn.net/sunbirdhan/article/details/38501649
		        // �����ؼ���Ҫ���ؼ������������ͨ��control.setParentStage(stage)���д���
		        scene = new Scene(root, 800, 600);
		        TerminalCURDController control=(TerminalCURDController)fxmlLoader.getController();
		        control.setParentStage(stage);
		        control.setParentScene(scene);

				// ͨ��fx:id������ָ���Ŀؼ�
//				Button button = (Button)root.lookup("#btn_add");
//	            button.setOnAction(e -> System.out.println("add"));


			} catch (IOException e) {
				e.printStackTrace();
			}
		}


        stage.setTitle("�ն˵�������");
        stage.setScene(scene);
//        stage.setAlwaysOnTop(true);
        stage.show();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
	}

	public static void main(String[] args) {
		launch(args);
	}

}
