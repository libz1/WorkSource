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
	String title = "�����Լ�̨�����";

    int WINDOWWIDTH = 800, WINDOWHEIGHT = 500;

    static String USERID = ""; // �Զ�����ʱ������û�ID
    static String USERPWD = ""; // �Զ�����ʱ������û�����
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
        Util698.log(ControlMain.class.getName(), "������� "+title + ver, Debug.LOG_INFO);
        Util698.log(ControlMain.class.getName(), "������IP��ַ " + svr_ip, Debug.LOG_INFO);
        // xuky 2018.01.25 ������ֵ�"���������в�����������ά����"
//		new Thread(() -> {

        // xuky 2018.03.20 ��Ҫ�������� ���ݿ�����
        SoftParameter.getInstance().refreshDataFromDB();
//		}).start();

        new Thread(() -> {
            PrefixWindow.getInstance().showFrame("ͨ�ŷ�����", 120, 510, 800, 200);
        }).start();


        // xuky 2018.03.13 �Զ����������Ľ���
        Util698.log(ControlMain.class.getName(), "�ж�USERID  begin", Debug.LOG_INFO);
        if (!USERID.equals("")) {
            Util698.log(ControlMain.class.getName(), "�ж�USERID  end ��ʼ�Զ�����״̬", Debug.LOG_INFO);
            IBaseDao<UserManager> userManager = new UserManagerDaoImpl();
            List<UserManager> users = userManager
                    .retrieve(" where userid='" + USERID + "' and userpwd='" + USERPWD + "'", "");
            if (users.size() > 0) {
                SoftParameter.getInstance().setUserManager(users.get(0));
                SoftParameter.getInstance().setRECVCLINET("");
                SoftParameter.getInstance().saveParam();

                // xuky 2018.03.12 ���ٿ���ģʽ
                while (true) {
//					Util698.log(JFXMain.class.getName(),
//							"getSERIAL_FINISHED��" + SoftParameter.getInstance().getSERIAL_FINISHED(), Debug.LOG_INFO);
                    // xuky 2018.03.12 ���ݴ���ͨ�ŵĿ���״̬�����к����Զ����Բ���
                    if (SoftParameter.getInstance().getSERIAL_FINISHED()) {
                        Util698.log(ControlMain.class.getName(), "׼���������Խ��� ", Debug.LOG_INFO);
                        TerminalParameter terminalParameter = TerminalParameter.getInstance(true);
                        return;
                    }
                    Debug.sleep(300);
                }

            } else {
                javafxutil.f_alert_informationDialog("������ʾ", "�û������������");
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

            // ����Label���󣬷ŵ���0�У���1��
            Label userName = new Label("�û���:");
            grid.add(userName, 0, 1);

            // �����ı�����򣬷ŵ���1�У���1��
            userTextField = new TextField();
            grid.add(userTextField, 1, 1);
            userTextField.requestFocus();

            Label pw = new Label("����:");
            grid.add(pw, 0, 2);

            pwBox = new PasswordField();
            grid.add(pwBox, 1, 2);

            Button btn = new Button("��¼");
            HBox hbBtn = new HBox(10);
            hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
            hbBtn.getChildren().add(btn);// ����ť�ؼ���Ϊ�ӽڵ�
            grid.add(hbBtn, 1, 4);// ��HBox pane�ŵ�grid�еĵ�1�У���4��


            Label ip = new Label("������IP��ַ " + svr_ip);
            grid.add(ip, 1, 5);

            userTextField.setOnAction((event) -> pwBox.requestFocus());
            pwBox.setOnAction((event) -> login(primaryStage));
            btn.setOnAction((event) -> login(primaryStage));

            primaryStage.setTitle(title);

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Util698.log(ControlMain.class.getName(), "�ر����"+title, Debug.LOG_INFO);

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
            });

            primaryStage.show();

        }
        // xuky 2017.06.20 ��ǰ�����ݿ����ӣ����ٺ�����������ȡ�ٶ�
        // xuky 2017.07.24 Ϊ���������Ŀ����ٶȣ�����ʹ��Platform.runLater
        // Platform.runLater(() -> {
        // // �������ݿ����ӵ�������
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
            javafxutil.f_alert_informationDialog("������ʾ", "�û������������");
        }
    }


    private void openMain(Stage primaryStage) {
		Button button1 = new Button("��������");
		button1.setOnAction(event->{
			new TerminalCURD();
		});

		Button button2 = new Button("��������");
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
