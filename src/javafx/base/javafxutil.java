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

	// javaFx8 �Դ��ĶԻ���ǳ�����
	// �ο�http://www.cnblogs.com/shiningWish/p/6213710.html
	// ��ʾ��Ϣ�Ի���
	public static void f_alert_informationDialog(String p_header, String p_message) {
		Platform.runLater(() -> {
			f_alert_informationDialog(p_header, p_message, 1);
		});
	}

	public static void f_alert_informationDialog(String p_header, String p_message, int i) {
		Alert _alert = new Alert(Alert.AlertType.INFORMATION);
		_alert.setTitle("��Ϣ");
		_alert.setHeaderText(p_header);
		_alert.setContentText(p_message);

		// xuky 2017.02.15 ��������stageΪ�գ��򵯴����ܻ�������ں�̨������
		// xuky 2017.04.24 stageΪ��δ�������⣬��������Ϊ֮ǰ�Ĵ�����ǿ������Ϊ����ǰ�����⣬����������ǰ��Ȼ��ȡ����ǰ��ʵ��
		// ������ʱǰ��
		// _alert.initOwner(d_stage);
		_alert.initOwner(null);

		_alert.show();
	}

	// �����û�����ѡ��ĶԻ���ȷ��Ϊtrue��
	public static boolean f_alert_confirmDialog(String p_header, String p_message) {
		// ��ť���ֿ���ʹ��Ԥ���Ҳ�����������Լ� new һ��
		Alert _alert = new Alert(Alert.AlertType.CONFIRMATION, p_message, new ButtonType("ȡ��", ButtonBar.ButtonData.NO),
				new ButtonType("ȷ��", ButtonBar.ButtonData.YES));
		// ���ô��ڵı���
		_alert.setTitle("ȷ��");
		_alert.setHeaderText(p_header);
		// ���öԻ���� icon ͼ�꣬�����������ڵ� stage

		// xuky 2017.04.24 �����������initOwnerΪ�գ���Ӱ������Ĺ���
		_alert.initOwner(null);

		// _alert.initOwner(d_stage);
		// showAndWait() ���ڶԻ�����ʧ��ǰ����ִ��֮��Ĵ���
		Optional<ButtonType> _buttonType = _alert.showAndWait();
		// ���ݵ���������
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
		builder.title("��ѡ���ļ���");
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
		builder.title("��ѡ���ļ�");
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
