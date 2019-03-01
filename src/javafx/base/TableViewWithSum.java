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
		// 直接添加控件
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

		// step1/2 设置列表内容可以编辑  设置tableview可以编辑
		// 参考http://blog.csdn.net/mexel310/article/details/23364397
		table_data.setEditable(editEnable);

		// 用于显示行数信息的集合
		ObservableList<DataItem> observaleList_sum = FXCollections.observableArrayList(new ArrayList<DataItem>());
		observaleList_sum.add(new DataItem());
		table_sum.setItems(observaleList_sum);

		// xuky 2017.05.27 解决初始化后未显示正确行数问题
		String colNumMsg = "总行数 "+observaleList_table.size();
		TableColumn c1 = new TableColumn(colNumMsg);
		// 未生效
		// c1.setStyle("-fx-alignment: CENTER_LEFT;");
		c1.setMinWidth(450);
		c1.setCellValueFactory(new PropertyValueFactory<>("dataname"));
		c1.setSortable(false);
		table_sum.getColumns().add(c1);

		// 1、创建集合用于存储tableview展示数据
		// ObservableList<DataItem> observaleList_table =
		// FXCollections.observableArrayList(new ArrayList<DataItem>());
		// 2、添加集合的事件
		observaleList_table.addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change c) {
				String msg = "总行数 " + c.getList().size();
				// observaleList_sum.get(0).setDataname(msg);
//				System.out.println("initialize => onChanged size=" + msg);

				// xuky 2017.03.28 因为css没有生效，无法隐藏header，所以修改为直接设置header的内容

				// 参考http://blog.csdn.net/huplion/article/details/52734130
				// 解决 Not on FX application thread问题
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						((TableColumn) table_sum.getColumns().get(0)).setText(msg);
					}
				});
			}
		});
		// 3、进行lambda处理，出现异常无法正常
		// observaleList_table.addListener(
		// change -> System.out.println("initialize => onChanged size="+
		// change.getList().size())
		// );

		// 标题栏格式枚举
		// { "id[0]","名称", "类型", "数据" }
		// xxx[0]表示不显示 xxx[200]表示长度为100 xxx{r}表示右对齐，用于数字类型数据
		// xxx{rB} B=begin 表示余下的元素全部右对齐，用于数字类型数据，免于逐个设置
		// xxx{rE} E=end 表示余下的元素取消全部右对齐，当前的这个依然是右对齐
		// String[] colNames_table2 = { "id[0]","名称", "类型[100]{r}", "数据{r}[200]"
		// };
//		String[] colNames_table2 = colTitles.split(",");
		String[] colNames_table2 = colTitles;
		// String colNames = "id,dataname,datatype,datavalue";
		String[] table_columns_array = colNames.split(",");
		Boolean all_Right = false;
		// 进行数据字段绑定
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

			// step2/2 设置列表内容可以编辑  设置tableview可以编辑
			// 参考http://blog.csdn.net/mexel310/article/details/23364397

			// xuky 2017.05.27  可能对数值类型字段影响正常使用  目前设置为可写状态有问题
//			c1.setCellFactory(TextFieldTableCell.forTableColumn());

			// step0/2 设置列表内容可以编辑  此设置无效，不起作用
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

		// 默认选中第一行数据
		if (table_data.getColumns().size() > 0)
			table_data.getSelectionModel().select(0);


		return table_data;
	}



}