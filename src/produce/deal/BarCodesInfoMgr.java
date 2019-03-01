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

public class BarCodesInfoMgr extends Application {
	Stage stage;

	// 1、异步交互过程， （2） 当开启了多个实例时，接收处理数据会出现重复情况，所以使用单例模式
	private volatile static BarCodesInfoMgr uniqueInstance;
	public static BarCodesInfoMgr getInstance() {
		if (uniqueInstance == null) {
			synchronized (BarCodesInfoMgr.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new BarCodesInfoMgr();
				}
			}
		}

		// 实现窗口的前置效果
		// TerminalParameter中不能有Platform.runLater操作
		uniqueInstance.setAlwaysOnTop(true);
		uniqueInstance.setAlwaysOnTop(false);

		return uniqueInstance;
	}

	private BarCodesInfoMgr() {
		// 单独的滞后的线程中开启，防止swing与javafx冲突
		// xuky 2017.04.05 在MainPanel中调用时，已经进行了了Platform.runLater
		// 如果这里继续，将会导致后续的setAlwaysOnTop(true)出错
//		Platform.runLater(() -> {
			init0();
//		});
	}

	private void init0() {
		stage = new Stage();

		// xuky 2017.04.05 通过MainPanel打开了窗口，随后关闭窗口，但是此对象未实际释放
		// 通过如下代码进行释放
		// 参考http://blog.csdn.net/huplion/article/details/52718372
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
				// 界面布局放在fxml文件中
				// 控件的事件代码在controler类中
				URL location = getClass().getResource("BarCodesInfo.fxml");
//				URL location = getClass().getResource("BorderPaneSample.fxml");
//				URL location = getClass().getResource("BorderPaneInTab.fxml");
//				URL location = getClass().getResource("BorderPaneInTab1.fxml");


				FXMLLoader fxmlLoader = new FXMLLoader();
				fxmlLoader.setLocation(location);
				fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
				root = (javafx.scene.Parent) fxmlLoader.load(location.openStream());

				// xuky 2017.02.14 获取control的控制句柄
				// 参考http://blog.csdn.net/sunbirdhan/article/details/38501649
				// 弹窗控件需要父控件句柄，在这里通过control.setParentStage(stage)进行传递
				scene = new Scene(root, 800, 600);
				scene.getStylesheets().add(BarCodesInfoMgr.class.getResource("Border.css").toExternalForm());
				BaseController control = (BaseController) fxmlLoader.getController();
				if (control != null){
					control.setParentStage(stage);
					control.setParentScene(scene);
				}
				// control.table_init0();

				// 通过fx:id来查找指定的控件
				// Button button = (Button)root.lookup("#btn_add");
				// button.setOnAction(e -> System.out.println("add"));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stage.setTitle("导入条码及条码查询");
		stage.setScene(scene);
		// stage.setAlwaysOnTop(true);
		stage.show();
	}

	public void setAlwaysOnTop(Boolean top){
		stage.setAlwaysOnTop(top);

		// xuky 2017.07.06 如果用户是点击最小化的，也可以展示出来
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
