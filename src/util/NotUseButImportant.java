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

		// ͨ��fx:id������ָ���Ŀؼ�
		//Button button = (Button)root.lookup("#btn_add");
		//button.setOnAction(e -> System.out.println("add"));

	}

	private void mouse(){
		// �б�ؼ���ӵ�����˫���¼�
		// �ο�http://blog.csdn.net/u011511429/article/details/38275759
		// ���н��в���������ͨ��setRowFactory����������е�˫�����в���
		class TableRowControl extends TableRow {
			public TableRowControl(TableView tableView) {
				super();
				this.setOnMouseClicked(event -> {
					// ˫���¼�
//					if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2
//							&& TableRowControl.this.getIndex() < tableView.getItems().size()) {
//						System.out.println("double click "+TableRowControl.this.getIndex());
//						// doSomething
//					}
					// �����¼� ��������˫��ʱ�����ȷ��ִ˵����¼�
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
