package javafx.base;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import produce.deal.TerminalParameter;
import produce.entity.UserManager;
import produce.entity.UserManagerDaoImpl;
import ui.MainPanel;
import ui.PrefixWindow;
import util.DB;
import util.SoftParameter;
import util.Util698;

public class JFXMain extends Application {
    String ver = "2.20";
    int WINDOWWIDTH = 700, WINDOWHEIGHT = 500;
    // Boolean isDebug = true;
    Boolean isDebug = false;
    // Boolean QUICKRUN = false;admin
    // Boolean QUICKRUN = true; // �Ƿ��Զ����Ա�־

    static String USERID = ""; // �Զ�����ʱ������û�ID
    static String USERPWD = ""; // �Զ�����ʱ������û�����
    // @Override

    // public void stop(){
    // // ����ر��˴˴��ڣ���ȫ���رճ���
    // System.exit(0);
    // }
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
            Util698.log(JFXMain.class.getName(), "������IP��ַ " + svr_ip, Debug.LOG_INFO);

        } catch (Exception e) {
            Util698.log(JFXMain.class.getName(), "start Exception"+e.getMessage(), Debug.LOG_INFO);
        }

        // xuky 2018.03.14 ���ִ��Ч�� ȥ�������չʾ����
        SoftParameter.getInstance().setRUNFASTER(false);

        SoftParameter.getInstance().setSERIAL_FINISHED(false);

        Util698.log(JFXMain.class.getName(), "������� v"+ver, Debug.LOG_INFO);
        // xuky 2018.01.25 ������ֵ�"���������в�����������ά����"
//		new Thread(() -> {

        // xuky 2018.03.20 ��Ҫ�������� ���ݿ�����
        SoftParameter.getInstance().refreshDataFromDB();
//		}).start();

        new Thread(() -> {
            PrefixWindow.getInstance().showFrame("ͨ�ŷ�����", 120, 510, 800, 200);
        }).start();

    	// xuky 2018.10.25 ��������󣬾Ϳ���MES���ݽӿ�
        PLC2MESThread.getInstance();


        // xuky 2018.03.13 �Զ����������Ľ���
        Util698.log(JFXMain.class.getName(), "�ж�USERID  begin", Debug.LOG_INFO);
        if (!USERID.equals("")) {
            Util698.log(JFXMain.class.getName(), "�ж�USERID  end ��ʼ�Զ�����״̬", Debug.LOG_INFO);
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
                        Util698.log(JFXMain.class.getName(), "׼���������Խ��� ", Debug.LOG_INFO);
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
            Scene scene = new Scene(grid, 400, 300);
            primaryStage.setScene(scene);

            Text scenetitle = new Text("��ӭʹ���������Ը���ϵͳ  ver " + ver);
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

            primaryStage.setTitle("�������Ը���ϵͳ");

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Util698.log(JFXMain.class.getName(), "�ر����1", Debug.LOG_INFO);

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
        if (isDebug) {
            user = "admin";
            pwd = "6867";
        }
        List<UserManager> users = userManager.retrieve(" where userid='" + user + "' and userpwd='" + pwd + "'", "");
        System.out.println("login end");
        if (users.size() > 0) {
            SoftParameter.getInstance().setUserManager(users.get(0));
            SoftParameter.getInstance().saveParam();
            primaryStage.close();
            // xuky 2017.11.07 ��������ʱ�����ñ���ʱ��
            openMain(primaryStage);
        } else {
            javafxutil.f_alert_informationDialog("������ʾ", "�û������������");
        }
    }

    public void openMain(Stage primaryStage) {

        final SwingNode swingNode = new SwingNode();
        createSwingContent(swingNode);

        StackPane root = new StackPane();
        root.getChildren().add(swingNode);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("�������Ը���ϵͳ");
        primaryStage.setScene(scene);

        // �ο�https://zhidao.baidu.com/question/1579040972785018580.html
        primaryStage.setResizable(false);

        // ����ر��˴˴��ڣ���ȫ���رճ���
        primaryStage.setOnCloseRequest(e -> {
            Util698.log(JFXMain.class.getName(), "�ر����2", Debug.LOG_INFO);

            // ���ݿ����ӽ��йر� ʹ��JDBC�ķ�ʽ�������ݲ���
            DB.getInstance().close();
            // ʹ��hibernate�ķ�ʽ�������ݲ���
            SessionFactoryTone.getInstance().close();

            // xuky 2016.09.02 �ر�ǰɾ������sqlite��������ʱ�ļ�
            Util698.deleteFiles(System.getProperty("user.dir"), "etilqs_");

            // ����ر��˴˴��ڣ���ȫ���رճ���
            Platform.exit();
            System.exit(0);
        });

        // xuky 2017.02.13 ����Ϊʼ����ǰ ----
        // primaryStage.setAlwaysOnTop(true);
        // SoftParameter.getInstance().setUsingJavaFX(true);

        // xuky ----
        primaryStage.show();

    }

    private void createSwingContent(SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            // ͨ��JavaFX�������棬�������⣬ģ�鴰���е��������ģ��������������֮��û���ᵽǰ��
            JPanel panel = new MainPanel(ver);
            panel.setBounds(0, 0, WINDOWWIDTH, WINDOWHEIGHT);
            swingNode.setContent(panel);
        });
    }

    public static void main(String[] args) {
        // xuky 2018.05.10 ʹ���ⲿ�������ļ�
        Util698.InitLog4jConfig();

        Util698.log(JFXMain.class.getName(), "�����θ���:" + args.length, Debug.LOG_INFO);
        for (int i = 0; i < args.length; i++)
            Util698.log(JFXMain.class.getName(), "������" + i + ":" + args[i], Debug.LOG_INFO);
        if (args.length == 2) {
            USERID = args[0];
            USERPWD = args[1];
        }
        launch(args);
    }

}
