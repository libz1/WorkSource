package javafx.base;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

// 参考http://blog.csdn.net/sweetStreet/article/details/52168045
public class PopBox {

//	public Object[] display(String title, InfoClass_javafx info_detail,Stage window,EventHandler<ActionEvent> ok_action) {
//		return display(title, info_detail,window,ok_action,null);
//	}


	public Object[] display(String title, InfoClass_javafx info_detail,Stage window,EventHandler<ActionEvent> ok_action,EventHandler<ActionEvent> cancle_action) {
		Object[] ret_obj = {null};
		window.setTitle(title);
		// modality要使用Modality.APPLICATION_MODEL
		window.initModality(Modality.APPLICATION_MODAL);

		window.setMinWidth(600);
		window.setMinHeight(250);

		Button btn_ok = new Button("确定");
		btn_ok.setOnAction(ok_action);


		Button btn_cancle = new Button("取消");
		if (cancle_action == null)
			btn_cancle.setOnAction(e -> {
				window.close();
			});
		else
			// xuky 2017.11.01 解决修改窗口取消后，明细信息不变化的异常
			btn_cancle.setOnAction(cancle_action);

		AnchorPane info = new AnchorPane();
		info.setPadding(new Insets(5, 5, 5, 5));
		info.getChildren().add(info_detail.getPanel());

		HBox hbox = new HBox(10);
		hbox.setAlignment(Pos.CENTER_RIGHT);
		// xuky 2017.02.18 设置边距信息
		hbox.setPadding(new Insets(5, 5, 5, 5));
		btn_ok.setMinSize(70, 23);
		btn_ok.setMaxSize(70, 23);
		btn_cancle.setMinSize(70, 23);
		btn_cancle.setMaxSize(70, 23);
		hbox.getChildren().addAll(btn_ok, btn_cancle);

		VBox layout = new VBox(10);
		layout.getChildren().addAll(info, hbox);
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout);
		window.setScene(scene);
		// xuky 2017.05.10 无需这个setAlwaysOnTop代码，showAndWait已经确保了其优先处理性·
//		window.setAlwaysOnTop(true);
		// 使用showAndWait()先处理这个窗口，而如果不处理，main中的那个窗口不能响应
		window.showAndWait();
		return ret_obj;
	}

	public Object[] display(String title, InfoClass_javafx info_detail) {
		Object[] ret_obj = {null};
		Stage window = new Stage();
		window.setTitle(title);
		// modality要使用Modality.APPLICATION_MODEL
		window.initModality(Modality.APPLICATION_MODAL);

		window.setMinWidth(600);
		window.setMinHeight(250);

		Button btn_ok = new Button("确定");
		btn_ok.setOnAction(e -> {
			ret_obj[0] = info_detail.trans.getData();
			window.close();
		});
		Button btn_cancle = new Button("取消");
		btn_cancle.setOnAction(e -> {
			window.close();
		});

		AnchorPane info = new AnchorPane();
		info.setPadding(new Insets(5, 5, 5, 5));
		info.getChildren().add(info_detail.getPanel());

		HBox hbox = new HBox(10);
		hbox.setAlignment(Pos.CENTER_RIGHT);
		// xuky 2017.02.18 设置边距信息
		hbox.setPadding(new Insets(5, 5, 5, 5));
		btn_ok.setMinSize(70, 23);
		btn_ok.setMaxSize(70, 23);
		btn_cancle.setMinSize(70, 23);
		btn_cancle.setMaxSize(70, 23);
		hbox.getChildren().addAll(btn_ok, btn_cancle);

		VBox layout = new VBox(10);
		layout.getChildren().addAll(info, hbox);
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout);
		window.setScene(scene);
		// 使用showAndWait()先处理这个窗口，而如果不处理，main中的那个窗口不能响应
		window.setAlwaysOnTop(true);
		window.showAndWait();
		return ret_obj;
	}
}
