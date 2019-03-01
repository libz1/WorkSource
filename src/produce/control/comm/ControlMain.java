package produce.control.comm;

import java.util.List;
import java.util.Map;

import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.base.SessionFactoryTone;
import javafx.base.javafxutil;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import produce.deal.TerminalCURD;
import produce.deal.TerminalParameter;
import produce.entity.UserManager;
import produce.entity.UserManagerDaoImpl;
import ui.PrefixWindow;
import util.DB;
import util.SoftParameter;
import util.Util698;

public class ControlMain extends Application {
	String ver = " ver 0.01";
	String title = "整机自检台体软件";

    int WINDOWWIDTH = 800, WINDOWHEIGHT = 500;

    static String USERID = ""; // 自动测试时传入的用户ID
    static String USERPWD = ""; // 自动测试时传入的用户密码
    TextField userTextField;
    PasswordField pwBox;

    @Override
    public void start(Stage primaryStage) throws Exception {
        String svr_ip = "";
        try {
            Map<String, String> map = Util698.praseXml(SessionFactoryTone.getXML());
            String str = map.get("session-factory");
            String[] strs = str.split("/");
            svr_ip = strs[2];
        } catch (Exception e) {
            Util698.log(ControlMain.class.getName(), "start Exception"+e.getMessage(), Debug.LOG_INFO);
        }

    	SoftParameter.getInstance();
        Util698.log(ControlMain.class.getName(), "开启软件 "+title + ver, Debug.LOG_INFO);
        Util698.log(ControlMain.class.getName(), "服务器IP地址 " + svr_ip, Debug.LOG_INFO);
        // xuky 2018.01.25 解决出现的"请对软件运行参数进行数据维护！"
//		new Thread(() -> {

        // xuky 2018.03.20 需要首先启动 数据库连接
        SoftParameter.getInstance().refreshDataFromDB();
//		}).start();

        new Thread(() -> {
            PrefixWindow.getInstance().showFrame("通信服务器", 120, 510, 800, 200);
        }).start();


        // xuky 2018.03.13 自动开启后续的界面
        Util698.log(ControlMain.class.getName(), "判断USERID  begin", Debug.LOG_INFO);
        if (!USERID.equals("")) {
            Util698.log(ControlMain.class.getName(), "判断USERID  end 开始自动测试状态", Debug.LOG_INFO);
            IBaseDao<UserManager> userManager = new UserManagerDaoImpl();
            List<UserManager> users = userManager
                    .retrieve(" where userid='" + USERID + "' and userpwd='" + USERPWD + "'", "");
            if (users.size() > 0) {
                SoftParameter.getInstance().setUserManager(users.get(0));
                SoftParameter.getInstance().setRECVCLINET("");
                SoftParameter.getInstance().saveParam();

                // xuky 2018.03.12 快速开启模式
                while (true) {
//					Util698.log(JFXMain.class.getName(),
//							"getSERIAL_FINISHED：" + SoftParameter.getInstance().getSERIAL_FINISHED(), Debug.LOG_INFO);
                    // xuky 2018.03.12 根据串口通信的开启状态，进行后续自动测试操作
                    if (SoftParameter.getInstance().getSERIAL_FINISHED()) {
                        Util698.log(ControlMain.class.getName(), "准备开启测试界面 ", Debug.LOG_INFO);
                        TerminalParameter terminalParameter = TerminalParameter.getInstance(true);
                        return;
                    }
                    Debug.sleep(300);
                }

            } else {
                javafxutil.f_alert_informationDialog("操作提示", "用户名或密码错误");
            }
            return;
        }

        new Thread(() -> {
            Util698.setSystemDateTime(Util698.getURLDateTime());
        }).start();

        // System.out.println("\r\n is for windows");

        {
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(25, 25, 25, 25));
            Scene scene = new Scene(grid, 600, 300);
            primaryStage.setScene(scene);

            Text scenetitle = new Text(title + ver);
            scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
            grid.add(scenetitle, 0, 0, 2, 1);

            // 创建Label对象，放到第0列，第1行
            Label userName = new Label("用户名:");
            grid.add(userName, 0, 1);

            // 创建文本输入框，放到第1列，第1行
            userTextField = new TextField();
            grid.add(userTextField, 1, 1);
            userTextField.requestFocus();

            Label pw = new Label("密码:");
            grid.add(pw, 0, 2);

            pwBox = new PasswordField();
            grid.add(pwBox, 1, 2);

            Button btn = new Button("登录");
            HBox hbBtn = new HBox(10);
            hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
            hbBtn.getChildren().add(btn);// 将按钮控件作为子节点
            grid.add(hbBtn, 1, 4);// 将HBox pane放到grid中的第1列，第4行


            Label ip = new Label("服务器IP地址 " + svr_ip);
            grid.add(ip, 1, 5);

            userTextField.setOnAction((event) -> pwBox.requestFocus());
            pwBox.setOnAction((event) -> login(primaryStage));
            btn.setOnAction((event) -> login(primaryStage));

            primaryStage.setTitle(title);

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Util698.log(ControlMain.class.getName(), "关闭软件"+title, Debug.LOG_INFO);

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
            });

            primaryStage.show();

        }
        // xuky 2017.06.20 提前打开数据库连接，加速后续的数据提取速度
        // xuky 2017.07.24 为了提高软件的开启速度，所以使用Platform.runLater
        // Platform.runLater(() -> {
        // // 创建数据库连接单例对象
        // SessionFactoryTone.getInstance();
        // });

    }

    private void login(Stage primaryStage) {
        // System.out.println("login begin");
        IBaseDao<UserManager> userManager = new UserManagerDaoImpl();

        String user = userTextField.getText();
        String pwd = pwBox.getText();
        List<UserManager> users = userManager.retrieve(" where userid='" + user + "' and userpwd='" + pwd + "'", "");
//        System.out.println("login end");
        if (users.size() > 0) {
            SoftParameter.getInstance().setUserManager(users.get(0));
            SoftParameter.getInstance().saveParam();
//            primaryStage.close();
            openMain(primaryStage);
        } else {
            javafxutil.f_alert_informationDialog("操作提示", "用户名或密码错误");
        }
    }


    private void openMain(Stage primaryStage) {
		Button button1 = new Button("参数管理");
		button1.setOnAction(event->{
			new TerminalCURD();
		});

		Button button2 = new Button("开启测试");
		button2.setOnAction(event->{
		});

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		grid.add(button2, 0, 1);
		grid.add(button1, 1, 1);

        Text scenetitle = new Text(title + ver);
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

		Scene scene = new Scene(grid, 600, 300);
		primaryStage.setScene(scene);

//		ReadIDData.getInstance().showFrame(title+ver );

	}

	public static void main(String[] args) {
        launch(args);
    }

}
