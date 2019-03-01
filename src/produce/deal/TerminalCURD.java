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
		// 单独的滞后的线程中开启，防止swing与javafx冲突
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
				// 界面布局放在fxml文件中
				// 控件的事件代码在controler类中
				URL location = getClass().getResource("TerminalCURD.fxml");
		        FXMLLoader fxmlLoader = new FXMLLoader();
		        fxmlLoader.setLocation(location);
		        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		        root = (javafx.scene.Parent) fxmlLoader.load(location.openStream());

		        // xuky 2017.02.14 获取control的控制句柄
		        // 参考http://blog.csdn.net/sunbirdhan/article/details/38501649
		        // 弹窗控件需要父控件句柄，在这里通过control.setParentStage(stage)进行传递
		        scene = new Scene(root, 800, 600);
		        TerminalCURDController control=(TerminalCURDController)fxmlLoader.getController();
		        control.setParentStage(stage);
		        control.setParentScene(scene);

				// 通过fx:id来查找指定的控件
//				Button button = (Button)root.lookup("#btn_add");
//	            button.setOnAction(e -> System.out.println("add"));


			} catch (IOException e) {
				e.printStackTrace();
			}
		}


        stage.setTitle("终端档案管理");
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
