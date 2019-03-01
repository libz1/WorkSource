package javafx.base;

import java.io.File;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.DirectoryChooserBuilder;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class javafxutil {

	// javaFx8 自带的对话框非常好用
	// 参考http://www.cnblogs.com/shiningWish/p/6213710.html
	// 提示信息对话框
	public static void f_alert_informationDialog(String p_header, String p_message) {
		Platform.runLater(() -> {
			f_alert_informationDialog(p_header, p_message, 1);
		});
	}

	public static void f_alert_informationDialog(String p_header, String p_message, int i) {
		Alert _alert = new Alert(Alert.AlertType.INFORMATION);
		_alert.setTitle("信息");
		_alert.setHeaderText(p_header);
		_alert.setContentText(p_message);

		// xuky 2017.02.15 如果这里的stage为空，则弹窗可能会出现隐在后台的问题
		// xuky 2017.04.24 stage为空未发现问题，可能是因为之前的窗口有强制设置为在最前的问题，现在是先最前，然后取消最前来实现
		// 窗口暂时前置
		// _alert.initOwner(d_stage);
		_alert.initOwner(null);

		_alert.show();
	}

	// 允许用户进行选择的对话框（确定为true）
	public static boolean f_alert_confirmDialog(String p_header, String p_message) {
		// 按钮部分可以使用预设的也可以像这样自己 new 一个
		Alert _alert = new Alert(Alert.AlertType.CONFIRMATION, p_message, new ButtonType("取消", ButtonBar.ButtonData.NO),
				new ButtonType("确定", ButtonBar.ButtonData.YES));
		// 设置窗口的标题
		_alert.setTitle("确认");
		_alert.setHeaderText(p_header);
		// 设置对话框的 icon 图标，参数是主窗口的 stage

		// xuky 2017.04.24 发现如果设置initOwner为空，不影响软件的功能
		_alert.initOwner(null);

		// _alert.initOwner(d_stage);
		// showAndWait() 将在对话框消失以前不会执行之后的代码
		Optional<ButtonType> _buttonType = _alert.showAndWait();
		// 根据点击结果返回
		if (_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
			return true;
		} else {
			return false;
		}
	}

	// Alert alert = new Alert(AlertType.INFORMATION);
	// alert.setTitle("Gluon Desktop");
	// alert.setHeaderText("About Gluon Desktop");
	// alert.setGraphic(new ImageView(new
	// Image(MenuActions.class.getResource("/icon.png").toExternalForm(), 48,
	// 48, true, true)));
	// alert.setContentText("This is a basic Gluon Desktop Application");
	// alert.showAndWait();

	public static String directorChoose() {
		DirectoryChooserBuilder builder = DirectoryChooserBuilder.create();
		builder.title("请选择文件夹");
		String cwd = System.getProperty("user.dir");
		File file = new File(cwd);
		builder.initialDirectory(file);
		DirectoryChooser chooser = builder.build();
		File chosenDir = chooser.showDialog(new Stage());
		if (chosenDir != null)
			return chosenDir.getAbsolutePath();
		else
			return "";
	}

	public static String fileChoose() {
		DirectoryChooserBuilder builder = DirectoryChooserBuilder.create();
		builder.title("请选择文件");
		String cwd = System.getProperty("user.dir");
		FileChooser fileChooser = new FileChooser();
		File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
			return file.getAbsolutePath();
        }
        else
        	return "";
	}

}
