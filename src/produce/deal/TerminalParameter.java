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


	// 1、异步交互过程， （2） 当开启了多个实例时，接收处理数据会出现重复情况，所以使用单例模式
	private volatile static TerminalParameter uniqueInstance;
	public static TerminalParameter getInstance() {
		if (uniqueInstance == null) {
			synchronized (TerminalParameter.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new TerminalParameter();
				}
			}
		}

		// 实现窗口的前置效果
		// TerminalParameter中不能有Platform.runLater操作
		uniqueInstance.setAlwaysOnTop(true);
		uniqueInstance.setAlwaysOnTop(false);

		return uniqueInstance;
	}

	public static TerminalParameter getInstance(Boolean isAuto) {
		if (uniqueInstance == null) {
			synchronized (TerminalParameter.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new TerminalParameter(isAuto);
				}
			}
		}

		// 实现窗口的前置效果
		// TerminalParameter中不能有Platform.runLater操作
		uniqueInstance.setAlwaysOnTop(true);
		uniqueInstance.setAlwaysOnTop(false);

		return uniqueInstance;
	}

	private TerminalParameter() {
		// 单独的滞后的线程中开启，防止swing与javafx冲突
		// xuky 2017.04.05 在MainPanel中调用时，已经进行了了Platform.runLater
		// 如果这里继续，将会导致后续的setAlwaysOnTop(true)出错
//		Platform.runLater(() -> {
			init0();
//		});
	}

	public TerminalParameter(Boolean isAuto) {
		IS_AUTO = isAuto;
		init0();
	}

	private void init0() {
//		Util698.log(TerminalParameter.class.getName(), "开启测试窗口", Debug.LOG_INFO);
		stage = new Stage();

		// xuky 2017.04.05 通过MainPanel打开了窗口，随后关闭窗口，但是此对象未实际释放
		// 通过如下代码进行释放
		// 参考http://blog.csdn.net/huplion/article/details/52718372
		//
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
            	if (!javafxutil.f_alert_confirmDialog("关闭提醒","请确认需要关闭此功能模块！")){
            		event.consume();
            		return;
            	}
            	else{
                    Util698.log(TerminalParameter.class.getName(), "用户操作，关闭了软件！",Debug.LOG_INFO);
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
        			Util698.log(JFXMain.class.getName(), "关闭软件3", Debug.LOG_INFO);

        			// 数据库连接进行关闭 使用JDBC的方式进行数据操作
        			DB.getInstance().close();
        			// 使用hibernate的方式进行数据操作
        			SessionFactoryTone.getInstance().close();

        			// xuky 2016.09.02 关闭前删除运行sqlite产生的临时文件
        			Util698.deleteFiles(System.getProperty("user.dir"), "etilqs_");

        			// 如果关闭了此窗口，则全部关闭程序
        			Platform.exit();
        			System.exit(0);
            	}
            }
        });

		Scene scene = null;
		{
			Parent root = null;
			try {
				// 界面布局放在fxml文件中
				// 控件的事件代码在controler类中
				URL location = getClass().getResource("TerminalParameter.fxml");
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
				scene.getStylesheets().add(TerminalParameter.class.getResource("Border.css").toExternalForm());
				control = (TerminalParameterController) fxmlLoader.getController();
				if (control != null){
					control.setParentStage(stage);
					control.setParentScene(scene);
				}
				control.setIS_AUTO(IS_AUTO);
				control.init();


				// 通过fx:id来查找指定的控件
				// Button button = (Button)root.lookup("#btn_add");
				// button.setOnAction(e -> System.out.println("add"));

			} catch (Exception e) {
				Util698.log(TerminalParameter.class.getName(), "init0 IOException "+e.getMessage(), Debug.LOG_INFO);

			}
		}
		stage.setTitle("II采生产测试");
		stage.setScene(scene);
		// stage.setAlwaysOnTop(true);
		stage.show();

		Util698.log(TerminalParameter.class.getName(), "开启模块'II采生产测试'", Debug.LOG_INFO);

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
