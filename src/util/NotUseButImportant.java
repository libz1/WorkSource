package util;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

public class NotUseButImportant {


	private void getFxmlControl(){

//		URL location = getClass().getResource("TerminalCURD.fxml");
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        fxmlLoader.setLocation(location);
//        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
//        root = (javafx.scene.Parent) fxmlLoader.load(location.openStream());

		// 通过fx:id来查找指定的控件
		//Button button = (Button)root.lookup("#btn_add");
		//button.setOnAction(e -> System.out.println("add"));

	}

	private void mouse(){
		// 列表控件添加单击或双击事件
		// 参考http://blog.csdn.net/u011511429/article/details/38275759
		// 对行进行操作，可以通过setRowFactory。如下面对行的双击进行操作
		class TableRowControl extends TableRow {
			public TableRowControl(TableView tableView) {
				super();
				this.setOnMouseClicked(event -> {
					// 双击事件
//					if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2
//							&& TableRowControl.this.getIndex() < tableView.getItems().size()) {
//						System.out.println("double click "+TableRowControl.this.getIndex());
//						// doSomething
//					}
					// 单击事件 在鼠标进行双击时会优先发现此单击事件
					if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1
							&& TableRowControl.this.getIndex() < tableView.getItems().size()) {
						// doSomething
						System.out.println("single click"+TableRowControl.this.getIndex());
					}
				});
			}
		}

		TableView terminalTable = null;

		terminalTable.setRowFactory(tableView -> {
			return new TableRowControl(terminalTable);
		});

	}

}
