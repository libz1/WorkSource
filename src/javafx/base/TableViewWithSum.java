package javafx.base;

import java.util.ArrayList;
import java.util.Set;

import com.eastsoft.util.DataConvert;

import entity.DataValues;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.module.DataItem;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TableViewWithSum extends BorderPane {

	Stage ParentStage;

	// @FXML
	private TableView table_data, table_sum;

	ObservableList<DataItem> observaleList_table = null;

	public TableViewWithSum() {
		// ֱ����ӿؼ�
		table_data = new TableView();
		this.setCenter(table_data);

		table_sum = new TableView();
		table_sum.setMaxHeight(25.0);
		this.setBottom(table_sum);

		// FXMLLoader fxmlLoader = new
		// FXMLLoader(getClass().getResource("tableviewwithsum.fxml"));
		// fxmlLoader.setRoot(this);
		// fxmlLoader.setController(this);
		// try {
		// fxmlLoader.load();
		// } catch (Exception exception) {
		// throw new RuntimeException(exception);
		// }

	}

	public <T> TableView init(Stage stage, ObservableList<T> observaleList_table, String colTitles, String colNames) {
		return init(stage, observaleList_table, colTitles.split(","), colNames,false);
	}

	public <T> TableView init(Stage stage, ObservableList<T> observaleList_table, String colTitles, String colNames, Boolean editEnable) {
		return init(stage, observaleList_table, colTitles.split(","), colNames,editEnable);
	}

	public <T> TableView init(Stage stage, ObservableList<T> observaleList_table, String[] colTitles, String colNames) {
		return init(stage, observaleList_table, colTitles, colNames,false);
	}

	public <T> TableView init(Stage stage, ObservableList<T> observaleList_table, String[] colTitles, String colNames, Boolean editEnable) {
		ParentStage = stage;

		// step1/2 �����б����ݿ��Ա༭  ����tableview���Ա༭
		// �ο�http://blog.csdn.net/mexel310/article/details/23364397
		table_data.setEditable(editEnable);

		// ������ʾ������Ϣ�ļ���
		ObservableList<DataItem> observaleList_sum = FXCollections.observableArrayList(new ArrayList<DataItem>());
		observaleList_sum.add(new DataItem());
		table_sum.setItems(observaleList_sum);

		// xuky 2017.05.27 �����ʼ����δ��ʾ��ȷ��������
		String colNumMsg = "������ "+observaleList_table.size();
		TableColumn c1 = new TableColumn(colNumMsg);
		// δ��Ч
		// c1.setStyle("-fx-alignment: CENTER_LEFT;");
		c1.setMinWidth(450);
		c1.setCellValueFactory(new PropertyValueFactory<>("dataname"));
		c1.setSortable(false);
		table_sum.getColumns().add(c1);

		// 1�������������ڴ洢tableviewչʾ����
		// ObservableList<DataItem> observaleList_table =
		// FXCollections.observableArrayList(new ArrayList<DataItem>());
		// 2����Ӽ��ϵ��¼�
		observaleList_table.addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change c) {
				String msg = "������ " + c.getList().size();
				// observaleList_sum.get(0).setDataname(msg);
//				System.out.println("initialize => onChanged size=" + msg);

				// xuky 2017.03.28 ��Ϊcssû����Ч���޷�����header�������޸�Ϊֱ������header������

				// �ο�http://blog.csdn.net/huplion/article/details/52734130
				// ��� Not on FX application thread����
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						((TableColumn) table_sum.getColumns().get(0)).setText(msg);
					}
				});
			}
		});
		// 3������lambda���������쳣�޷�����
		// observaleList_table.addListener(
		// change -> System.out.println("initialize => onChanged size="+
		// change.getList().size())
		// );

		// ��������ʽö��
		// { "id[0]","����", "����", "����" }
		// xxx[0]��ʾ����ʾ xxx[200]��ʾ����Ϊ100 xxx{r}��ʾ�Ҷ��룬����������������
		// xxx{rB} B=begin ��ʾ���µ�Ԫ��ȫ���Ҷ��룬���������������ݣ������������
		// xxx{rE} E=end ��ʾ���µ�Ԫ��ȡ��ȫ���Ҷ��룬��ǰ�������Ȼ���Ҷ���
		// String[] colNames_table2 = { "id[0]","����", "����[100]{r}", "����{r}[200]"
		// };
//		String[] colNames_table2 = colTitles.split(",");
		String[] colNames_table2 = colTitles;
		// String colNames = "id,dataname,datatype,datavalue";
		String[] table_columns_array = colNames.split(",");
		Boolean all_Right = false;
		// ���������ֶΰ�
		for (int i = 0; i < colNames_table2.length; i++) {
			String name0 = colNames_table2[i], name;
			name = name0;
			// System.out.println("TableView init=> name=" + name0);
			int pos_1, pos_2;
			pos_1 = name0.indexOf("{");
			pos_2 = name0.indexOf("[");
			if ((pos_1 == -1 || pos_1 > pos_2) && (pos_2 != -1))
				pos_1 = pos_2;
			if (pos_1 != -1) {
				name = name0.substring(0, pos_1);
			}

			c1 = new TableColumn(name);

			// step2/2 �����б����ݿ��Ա༭  ����tableview���Ա༭
			// �ο�http://blog.csdn.net/mexel310/article/details/23364397

			// xuky 2017.05.27  ���ܶ���ֵ�����ֶ�Ӱ������ʹ��  Ŀǰ����Ϊ��д״̬������
//			c1.setCellFactory(TextFieldTableCell.forTableColumn());

			// step0/2 �����б����ݿ��Ա༭  ��������Ч����������
			c1.setEditable(true);

			if (name0.indexOf("[0]") >= 0)
				c1.setVisible(false);
			else {
				if (name0.indexOf("[") >= 0 && name0.indexOf("]") >= 0) {
					String width = name0.split("\\[")[1].split("\\]")[0];
//					c1.setMinWidth(DataConvert.String2Int(width));
//					c1.setMaxWidth(DataConvert.String2Int(width));
					c1.setPrefWidth(DataConvert.String2Int(width));
				}
			}
			if (name0.indexOf("{rB}") >= 0)
				all_Right = true;
			if (name0.indexOf("{rE}") >= 0)
				all_Right = false;
			if (name0.indexOf("{r") >= 0 || all_Right)
				c1.setStyle("-fx-alignment: CENTER_RIGHT");

			c1.setCellValueFactory(new PropertyValueFactory<>(table_columns_array[i]));
			c1.setEditable(true);

			table_data.getColumns().add(c1);
		}
		table_data.setItems(observaleList_table);

		// Ĭ��ѡ�е�һ������
		if (table_data.getColumns().size() > 0)
			table_data.getSelectionModel().select(0);


		return table_data;
	}



}