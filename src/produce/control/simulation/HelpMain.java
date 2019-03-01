package produce.control.simulation;

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
import produce.deal.TerminalParameter;
import produce.entity.UserManager;
import produce.entity.UserManagerDaoImpl;
import util.DB;
import util.SoftParameter;
import util.Util698;

public class HelpMain extends Application {
	String ver = " ver 0.01";
	String title = "校表辅助串口软件";

    int WINDOWWIDTH = 800, WINDOWHEIGHT = 500;

    @Override
    public void start(Stage primaryStage) throws Exception {

        {

            primaryStage.setTitle(title);

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Util698.log(HelpMain.class.getName(), "关闭软件"+title, Debug.LOG_INFO);

                     // 如果关闭了此窗口，则全部关闭程序
                    Platform.exit();
                    System.exit(0);
                }
            });

            primaryStage.show();

        }
     }


	public static void main(String[] args) {
        launch(args);
    }

}
