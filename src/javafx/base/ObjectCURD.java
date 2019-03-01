package javafx.base;

import java.awt.event.ActionListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.eastsoft.fio.ReadWriteExcel;
import com.eastsoft.util.DataConvert;
import com.sun.javafx.collections.MappingChange;

import dao.basedao.IBaseDao;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import rx.Observable;
import util.Publisher;
import util.Util698;

// 通用CURD控件
public class ObjectCURD<T> extends BorderPane {

	String exportFileName = "导出数据.xls";
	// String[] exportColmunNames;
	// String exportColmuns;

	ActionListener actionListener = null;

	Object newObject = null;

	String[] detail_colNames = null;
	String detail_export_columns = "";

	String[] table_colNames = null;
	String table_columns = "";
	Class<?> clazz = null;
	// 每行显示的列数
	int colNum = 3;

	Stage parentStage = null;

	ObservableList<T> data_objs = null;

	IBaseDao objDao = null;
	TransBehavior_javafx transBehavior = null;

	String indexCols = "";

	String copyMsg = "";

	InfoClass_javafx info_detail = null;

	@FXML
	private Pane infoDetail_pane;

	@FXML
	BorderPane dataTable_pane;

	@FXML
	HBox buttons;

	@FXML
	Button btn_add, btn_del, btn_delall, btn_seek, btn_copy;

	// @FXML
	private TableView<T> objectTable;

	private String WHERE = "";

	public String getWHERE() {
		return WHERE;
	}

	public void setWHERE(String wHERE) {
		WHERE = wHERE;
	}

	private String ORDERBY = "";

	public ObjectCURD(IBaseDao objDao, TransBehavior_javafx transBehavior, String[] detail_colNames,
			String detail_export_columns, String[] table_colNames, String table_columns, String indexCols,
			String where) {
		WHERE = where;
		Button[] a = null;
		init0(objDao, transBehavior, detail_colNames, detail_export_columns, table_colNames, table_columns, indexCols,
				a);
	}

	public ObjectCURD(IBaseDao objDao, TransBehavior_javafx transBehavior, String[] detail_colNames,
			String detail_export_columns, String[] table_colNames, String table_columns, String indexCols, String where,
			String orderby) {
		WHERE = where;
		ORDERBY = orderby;
		Button[] a = null;
		init0(objDao, transBehavior, detail_colNames, detail_export_columns, table_colNames, table_columns, indexCols,
				a);
	}

	public ObjectCURD(IBaseDao objDao, TransBehavior_javafx transBehavior, String[] detail_colNames,
			String detail_export_columns, String[] table_colNames, String table_columns, String indexCols, String where,
			String orderby, int colNumPerRow) {
		WHERE = where;
		ORDERBY = orderby;
		Button[] a = null;
		colNum = colNumPerRow;
		init0(objDao, transBehavior, detail_colNames, detail_export_columns, table_colNames, table_columns, indexCols,
				a);
	}

	public ObjectCURD(IBaseDao objDao, TransBehavior_javafx transBehavior, String[] detail_colNames,
			String detail_export_columns, String[] table_colNames, String table_columns, String indexCols) {
		Button[] a = null;
		init0(objDao, transBehavior, detail_colNames, detail_export_columns, table_colNames, table_columns, indexCols,
				a);
	}

	public ObjectCURD(IBaseDao objDao, TransBehavior_javafx transBehavior, String[] detail_colNames,
			String detail_export_columns, String[] table_colNames, String table_columns, String indexCols,
			Button[] extendButtons) {
		init0(objDao, transBehavior, detail_colNames, detail_export_columns, table_colNames, table_columns, indexCols,
				extendButtons);
	}

	private void init0(IBaseDao objDao, TransBehavior_javafx transBehavior, String[] detail_colNames,
			String detail_export_columns, String[] table_colNames, String table_columns, String indexCols,
			Button[] extendButtons) {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("object_curd.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();

			this.objDao = objDao;
			this.transBehavior = transBehavior;

			this.detail_colNames = detail_colNames;
			this.detail_export_columns = detail_export_columns;

			this.table_colNames = table_colNames;
			this.table_columns = table_columns;
			this.indexCols = indexCols;

			// xuky 2017.05.02 从当前类中，无法获取泛型T的类型，但是可以从objDao中得到
			Type type = objDao.getClass().getGenericSuperclass();
			clazz = ((Class<?>) (((ParameterizedType) (type)).getActualTypeArguments()[0]));

			// xuky 2017.05.31 根据用户设定动态添加按钮
			if (extendButtons != null) {
				for (Button addButton : extendButtons)
					buttons.getChildren().add(addButton);
			}

			table_init0();

		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	public void setButtonVisible() {
		btn_add.setVisible(false);
		seek_info.setVisible(false);
		btn_del.setVisible(false);
		btn_delall.setVisible(false);
		btn_seek.setVisible(false);
		;
	}

	// 初始化列表
	public void table_init0() {
		// 构建明细区域
		info_detail = new InfoClass_javafx(detail_colNames, colNum);
		// xuky 2017.05.26 特别注意：transBehavior只有一份实例，在后续的弹窗中如果进行了赋值，将会导致这里的控制失效
		info_detail.setTrans(transBehavior);
		infoDetail_pane.getChildren().add(info_detail.getPanel());

		// 根据明细信息的个数自动计算控件的占用总高度
		// double tmp = detail_colNames.length * 100 / colNum;
		// tmp = tmp / 100;
		// int rowNum = (int) Math.ceil(tmp);

		// xuky 2017.07.03 根据实际的行数进行计算
		int rowNum = info_detail.getROWNUM();

		infoDetail_pane.setMinHeight(rowNum * 37);
		infoDetail_pane.setMaxHeight(rowNum * 37);

		// // 构建列表控件 使用[0]表示对字段不予显示
		// String[] table_columns_array = table_columns.split(",");
		// // 进行数据字段绑定
		// for (int i = 0; i < table_colNames.length; i++) {
		// String name = table_colNames[i];
		// TableColumn c1 = new TableColumn(name);
		// if (name.indexOf("[0]") > 0)
		// c1.setVisible(false);
		// c1.setMinWidth(100);
		// c1.setCellValueFactory(new
		// PropertyValueFactory<>(table_columns_array[i]));
		// objectTable.getColumns().add(c1);
		// }
		//
		// // 列表控件添加行选中事件
		// // 不能使用lambd表达式，出现ambiguous错误

		// 刷新列表中的数据
		refreshTableData();
	}

	public void refreshTableData() {
		refreshTableData(-1);
	}

	// 获取列表数据
	public void refreshTableData(int row) {

		// xuky 2017.02.17 使用FXJava进行异步调用
		Observable<String> get_tabelData = Observable.create(string -> {
			// getParentScene().setCursor(Cursor.WAIT);
			List<T> result = objDao.retrieve(WHERE, ORDERBY);
			// xuky 2018.04.18 希望通过这样的方式，释放内存数据，防止内存泄漏
			data_objs = null;
			data_objs = FXCollections.observableArrayList(result);
			string.onNext("");
		});
		// xuky 2017.03.01 如果使用了如下的代码，在进行combox类型控件进行赋值的时候会出现异常
		// .subscribeOn(Schedulers.io())
		get_tabelData.subscribe((string) -> {

			TableViewWithSum tableViewWithSum = new TableViewWithSum();
			// ObservableList<FreezeDay> dayDataDB =
			// FXCollections.observableArrayList(new ArrayList<FreezeDay>());
			// // 2、确定列表的字段标题、字段显示内容及样式
			// // 3、进行对象初始化
			// String[] table_colNames = null;
			// String table_columns = "";
			objectTable = tableViewWithSum.init(parentStage, data_objs, table_colNames, table_columns);
			//// // 4、添加到界面进行展示
			dataTable_pane.setCenter(tableViewWithSum);

			// xuky 2017.10.27 可以进行多选
			// 参考 http://www.cnblogs.com/SEC-fsq/p/6825955.html
			objectTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

			objectTable.getSelectionModel().getSelectedCells().addListener(new ListChangeListener() {
				@Override
				public void onChanged(Change c) {
					while (c.next()) {
						// xuky 2017.02.17 自行摸索 获得当前行
						MappingChange mc = (MappingChange) c;
						ObservableList ol = mc.getList();
						if (ol.size() <= 0)
							return;
						TablePosition o = (TablePosition) ol.get(0);
						int row = o.getRow();
						// xkuy 2017.02.17 在首行默认选中时，出现异常，得到的行号为-1
						// System.out.println("change:"+terminalTable.getSelectionModel().getSelectedIndex());
						if (row >= 0) {
							T object = (T) data_objs.get(row);
							// SoftParameter.getInstance().setSendTerminal(terminal.getTerminalCode0xH());
							// SoftParameter.getInstance().saveParam();
							info_detail.setData(object);

							// xuky 2017.07.06 添加行号变化消息
							Object[] s = { "RowChanged", object.getClass().getName(), object };
							Publisher.getInstance().publish(s);

						}
					}
				}
			});

			// xuky 2017.05.31 刷新列表中数据后，定位到指定行
			if (row > 0) {
				if (row < objectTable.getColumns().size())
					objectTable.getSelectionModel().select(row);
			} else if (objectTable.getColumns().size() > 0)
				objectTable.getSelectionModel().select(0);

			// // 列表控件插入数据
			// objectTable.setItems(data_objs);
			// // xuky 默认选中第一行
			// // getParentScene().setCursor(Cursor.DEFAULT);
		});

	}

	// 参考http://blog.csdn.net/zzq900503/article/details/36202353
	// private static <T> T newTclass(Class<T> clazz) throws
	// InstantiationException, IllegalAccessException {
	// T a = clazz.newInstance();
	// return a;
	// }
	// 创建一个Class的对象来获取泛型的class，实现BaseDao的关键
	private Class<?> clz;

	public Class<?> getClz() {
		if (clz == null) {
			Type type = this.getClass().getGenericSuperclass();
			clz = ((Class<?>) (((ParameterizedType) (type)).getActualTypeArguments()[0]));
			// 获取泛型的Class对象 ParameterizedType getActualTypeArguments()[0]取得第一个
			clz = ((Class<?>) (((ParameterizedType) (this.getClass().getGenericSuperclass()))
					.getActualTypeArguments()[0]));
		}
		return clz;
	}

	@FXML
	public void refreshAction(ActionEvent event) {
		refreshTableData(-1);

	}

	@FXML
	public void addAction(ActionEvent event) {
		InfoClass_javafx info_detail_pop = new InfoClass_javafx(detail_colNames, 3);
		info_detail_pop.setTrans(transBehavior);
		// 使用默认构建数据作为初始化数据、
		// clazz.newInstance() 相当于new Terminal();等构建函数
		try {
			info_detail_pop.setData(clazz.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// xuky 2017.05.10 CRUD弹窗的业务逻辑处理与界面展示代码分离 1
		Stage window = new Stage();

		// xuky 2017.05.10 增加数据时，根据关键字段，判断是否为重复数据
		EventHandler<ActionEvent> ok_action = e -> {
			T obj = (T) info_detail_pop.trans.getData();
			if (obj != null) {
				// 根据关键字段数据，检查用户添加的数据是否重复
				String getter = indexCols;
				String value = (String) Util698.getObjectAttrs(obj, getter);
				Object tmp = getByCode(value, getter);
				if (tmp != null) {
					String msg = "关键数据重复，请检查! \r\n关键数据字段名:" + getter + "关键数据内容:" + value;
					javafxutil.f_alert_informationDialog("操作提示", msg);
					// 不关闭弹窗，允许用户进行相关数据修改后，再次提交、增加
				} else {
					// 后台数据增加
					objDao.create(obj);
					// 前台界面列表数据增加
					data_objs.add((T) obj);

					// xuky 2017.05.26 特殊处理，解决增加后，行变化时，明细信息不随之变化问题
					info_detail.setTrans(transBehavior);

					// 选中新增的行号
//					objectTable.getSelectionModel().select(obj);

					// 选中新增的行号  1、取消之前的选择  2、选中最后一行数据
					objectTable.getSelectionModel().clearSelection();
					objectTable.getSelectionModel().selectLast();
					// 关闭弹窗
					window.close();
				}
			}
		};

		EventHandler<ActionEvent> cancle_action = e -> {
			// xuky 2017.05.26 特殊处理，解决增加后，行变化时，明细信息不随之变化问题
			info_detail.setTrans(transBehavior);
			// 关闭弹窗
			window.close();
		};


		// xuky 2017.05.10 CRUD弹窗的业务逻辑处理与界面展示代码分离 2
		new PopBox().display("增加", info_detail_pop, window, ok_action, cancle_action);

		if (actionListener != null)
			actionListener.actionPerformed(null);
	}

	public Object getByCode(String code, String getter) {
		return getByCode(code, getter, -1);
	}

	// 判断是否有重复数据
	public Object getByCode(String code, String getter, int row) {
		Object object = null;
		try {
			int i = 0;
			for (Object o : data_objs) {
				// xuky 2017.05.10 在修改时，进行查重比较，不要与自己比较
				if (row == i) {
					i++;
					continue;
				}

				String value = (String) Util698.getObjectAttrs(o, getter);
				if (value.equals(code)) {
					object = o;
					break;
				}
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	@FXML
	TextField seek_info;

	@FXML
	public void seekAction(ActionEvent event) {
		T find_obj = null;
		String info = seek_info.getText();
		int row = objectTable.getSelectionModel().getSelectedIndex();
		find_obj = findObj(row, info);
		if (find_obj == null) {
			// xuky 2017.05.10 找不到的原因可能是到了行末，可以从头再找
			find_obj = findObj(-1, info);
			if (find_obj == null)
				javafxutil.f_alert_informationDialog("查找定位结果", "没有找到");
			else
				objectTable.getSelectionModel().select(find_obj);
		} else {
			objectTable.getSelectionModel().select(find_obj);
		}
	}

	private T findObj(int row, String info) {
		T find_obj = null;
		for (int i = row + 1; i < data_objs.size(); i++) {
			T obj = data_objs.get(i);
			String value = (String) Util698.getObjectAttrs(obj, indexCols);
			if (value.indexOf(info) >= 0) {
				find_obj = obj;
				break;
			}
		}
		return find_obj;
	}

	@FXML
	public void updateAction(ActionEvent event) {
		// 通过TableViewSelectionModel对选中行进行操作
		// http://lujin55.iteye.com/blog/1720004
		TableViewSelectionModel sm = objectTable.getSelectionModel();
		int row = sm.getSelectedIndex();
		if (row < 0) {
			// new AlertBox().display("操作提示", "请先选中需要修改的行");
			javafxutil.f_alert_informationDialog("操作提示", "请先选中需要修改的行");
			return;
		}
		T obj_old = (T) data_objs.get(row);

		InfoClass_javafx info_detail_pop = new InfoClass_javafx(detail_colNames, 3);
		info_detail_pop.setTrans(transBehavior);
		// 使用默认构建数据作为初始化数据
		info_detail_pop.setData(obj_old);

		// xuky 2017.05.10 CRUD弹窗的业务逻辑处理与界面展示代码分离 1
		Stage window = new Stage();

		// xuky 2017.05.10 增加数据时，根据关键字段，判断是否为重复数据
		// xuky 2017.05.10 修改数据时，需要特殊处理，可以与原先的自己相等，但是不能与其他的相等
		EventHandler<ActionEvent> ok_action = e -> {
			T obj = (T) info_detail_pop.trans.getData();
			if (obj != null) {
				// 根据关键字段数据，检查用户添加的数据是否重复
				String getter = indexCols;
				String value = (String) Util698.getObjectAttrs(obj, getter);
				Object tmp = getByCode(value, getter, row);
				if (tmp != null) {
					String msg = "关键数据重复，请检查! \r\n关键数据字段名:" + getter + "关键数据内容:" + value;
					javafxutil.f_alert_informationDialog("操作提示", msg);
					// 不关闭弹窗，允许用户进行相关数据修改后，再次提交、增加
				} else {
					// 后台数据增加
					objDao.update(obj);
					// 前台界面列表数据增加
					data_objs.set(row, obj);

					// xuky 2017.05.26 特殊处理，解决增加后，行变化时，明细信息不随之变化问题
					info_detail.setTrans(transBehavior);

					objectTable.getSelectionModel().select(obj);
					// 关闭弹窗
					window.close();
				}
			}
		};
		EventHandler<ActionEvent> cancle_action = e -> {
			// xuky 2017.05.26 特殊处理，解决增加后，行变化时，明细信息不随之变化问题
			info_detail.setTrans(transBehavior);
			// 关闭弹窗
			window.close();
		};


		// xuky 2017.05.10 CRUD弹窗的业务逻辑处理与界面展示代码分离 2
		new PopBox().display("修改", info_detail_pop, window, ok_action, cancle_action);

		// Object[] object = new PopBox().display("修改", info_detail);
		// obj = (T) object[0];
		//
		// if (obj != null) {
		// // 后台数据维护
		// objDao.update(obj);
		// // 界面数据刷新
		// data_objs.set(row, obj);
		// objectTable.getSelectionModel().select(obj);
		// }

		if (actionListener != null)
			actionListener.actionPerformed(null);

	}

	@FXML
	public void deleteAction(ActionEvent event) {

		if (data_objs.size() <= 0)
			return;

		// 通过TableViewSelectionModel对选中行进行操作
		// http://lujin55.iteye.com/blog/1720004
		TableViewSelectionModel sm = objectTable.getSelectionModel();
		int row = sm.getSelectedIndex();
		if (row < 0) {
			// new AlertBox().display("操作提示", "请先选中需要修改的行");
			javafxutil.f_alert_informationDialog("操作提示", "请先选中需要删除的行");
			return;
		}
		if (javafxutil.f_alert_confirmDialog("操作提示", "是否确认删除选中的行？") == false)
			return;

		T obj = data_objs.get(row);
		objDao.delete(obj);
		data_objs.remove(row);
	}

	@FXML
	public void deleteAllAction(ActionEvent event) {
		// 获取选中的数据
		ObservableList<T> data_objs_selected = objectTable.getSelectionModel().getSelectedItems();
		int num = data_objs_selected.size();
		if (num <= 0)
			return;
		if (javafxutil.f_alert_confirmDialog("操作提示", "是否确认删除选中数据？") == false)
			return;

		Object[] objs = new Object[num];

		// xuky 2017.10.27 随着数据的删除，data_objs_selected的内容也在变化，所以首先记录下选中的数据
		for (int i = 0; i < num; i++) {
			T obj = data_objs_selected.get(i);
			objs[i] = obj;
		}
		for (int i = 0; i < num; i++) {
			Object obj = objs[i];
			if (obj != null) {
//				System.out.println("deleteAllAction ok data-" + i);
				objDao.delete(obj);
				data_objs.remove(obj);
			} else
				System.out.println("deleteAllAction null data-" + i);
		}

		// if (data_objs.size() <= 0)
		// return;
		//
		//
		// for (T obj : data_objs)
		// objDao.delete(obj);
		//
		// data_objs.remove(0, data_objs.size());

	}

	@FXML
	public void copyAction(ActionEvent event) {
		if (data_objs.size() <= 0)
			return;

		// 通过TableViewSelectionModel对选中行进行操作
		// http://lujin55.iteye.com/blog/1720004
		TableViewSelectionModel sm = objectTable.getSelectionModel();
		int row = sm.getSelectedIndex();
		if (row < 0) {
			// new AlertBox().display("操作提示", "请先选中需要修改的行");
			javafxutil.f_alert_informationDialog("操作提示", "请先选中需要复制的行");
			return;
		}
		if (javafxutil.f_alert_confirmDialog("操作提示", "是否确认复制选中的行？") == false)
			return;

		T obj = data_objs.get(row);
		try {
			obj = (T) objDao.create(obj);
			info_detail.setTrans(transBehavior);
			data_objs.add((T) obj);
			// xuky 2017.07.03 这里的处理有一点点小问题 obj实际上还是原先的那个
			// 选择最后一行数据
			objectTable.getSelectionModel().select(data_objs.size() - 1);

			javafxutil.f_alert_informationDialog("操作提示", copyMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// // xuky 进行用户操作提示
		// if (!copyMsg.equals(""))
		// javafxutil.f_alert_informationDialog("操作提示", copyMsg);

	}

	private static <T> T newTclass(Class<T> clazz) throws InstantiationException, IllegalAccessException {
		T a = clazz.newInstance();
		return a;

	}

	public int getSelectRow() {
		TableViewSelectionModel sm = objectTable.getSelectionModel();
		int row = sm.getSelectedIndex();
		if (row < 0) {
			// new AlertBox().display("操作提示", "请先选中需要修改的行");
			javafxutil.f_alert_informationDialog("操作提示", "请先选中需要修改的行");
		}
		return row;
	}

	public T getSelectObj(int row) {
		if (row < 0) {
			// new AlertBox().display("操作提示", "请先选中需要修改的行");
			javafxutil.f_alert_informationDialog("操作提示", "请先选中需要修改的行");
			return null;
		}
		if (row >= data_objs.size()) {
			javafxutil.f_alert_informationDialog("操作提示", "行数错误");
			return null;
		}
		T obj_old = (T) data_objs.get(row);
		return obj_old;
	}

	public String getCopyMsg() {
		return copyMsg;
	}

	public void setData_objs(ObservableList<T> data_objs) {
		this.data_objs = data_objs;
	}

	public void setCopyMsg(String copyMsg) {
		this.copyMsg = copyMsg;
	}

	public ObservableList<T> getData_objs() {
		return data_objs;
	}

	public void setButtonsVisible(Boolean visible) {
		buttons.setMaxHeight(0);
		buttons.setMinHeight(0);
		buttons.setPrefHeight(0);
		buttons.setVisible(visible);
	}

	public void setDetailVisible(Boolean visible) {
		infoDetail_pane.setMaxHeight(0);
		infoDetail_pane.setMinHeight(0);
		infoDetail_pane.setPrefHeight(0);
		infoDetail_pane.setVisible(visible);
	}

	@FXML
	public void exportAction(ActionEvent event) {
		export2Excel();
	}

	public void export2Excel(){
		// String filePath = DebugSwing.directorChoose();

		String filePath = javafxutil.directorChoose();
		if (!filePath.equals("")) {
			String fileName = filePath + exportFileName;

			// 将电表对象转换为字符串数组
			String[][] data = getStringArray();

			// 将字符串数据转存为excel文件
			String ret = ReadWriteExcel.stringArray2Excel(data, fileName);

			// 之前作废的函数，将表格中的数据导出为excel文件
			// String ret = ReadWriteUtil.table2Excel(colNames,
			// defaultModel,fileName);

			// 根据返回结果，进行用户提醒
			if (ret.equals(""))
				javafxutil.f_alert_informationDialog("操作提示", "数据导出到\"" + fileName + "\"成功！");
			else
				javafxutil.f_alert_informationDialog("操作提示", "数据导出失败！");
		}

	}

	public String[][] getStringArray() {
		// List objList = new ArrayList<T>();
		String[][] data = null;
		int aRow = data_objs.size();
		int aCol = detail_colNames.length;
		data = new String[aRow + 1][aCol];
		String[] colmuns = detail_export_columns.split(",");
		// 第一行是对象属性信息
		for (int j = 0; j < aCol; j++) {
			String col_name = detail_colNames[j];
			if (col_name.indexOf(";") >= 0)
				col_name = col_name.split(";")[0];
			data[0][j] = col_name;
		}

		if (aRow != 0) {
			Object object = data_objs.get(0);


			for (int i = 0; i < aRow; i++) {
				for (int j = 0; j < aCol; j++) {

					// 得到获取对象属性的方法名称
					String getter = Util698.getGetter(colmuns[j]);

					object = data_objs.get(i);
					try {
						// 动态 反射调用相关获取对象属性的函数

						// xuky 2017.03.21 添加对于字段属性的判断
						Object val = Util698.getObjectAttr(object, getter);
						if (val == null) {
							// xuky 添加对于空值的处理
							data[i + 1][j] = "";
						} else {
							String type = val.getClass().getTypeName();
							if (type.toLowerCase().indexOf("int") >= 0)
								data[i + 1][j] = DataConvert.int2String((int) val);
							else
								data[i + 1][j] = (String) val;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return data;
	}

	@FXML
	public void importAction(ActionEvent event) {
		// String fileName = DebugSwing.fileChoose();
		String fileName = javafxutil.fileChoose();
		String[][] data = null;
		if (!fileName.equals("")) {
			data = ReadWriteExcel.excel2StringArray(fileName);
			deleteAll();
			converFormStringArray(data);
		}
		// DebugSwing.showMsg("导入数据成功！");
		javafxutil.f_alert_informationDialog("操作提示", "导入数据成功！");
	}

	private void deleteAll() {
		if (data_objs != null)
			if (data_objs.size() > 0) {
				for (int i = data_objs.size() - 1; i >= 0; i--)
					data_objs.remove(i);
			}
	}

	public void converFormStringArray(String[][] data) {
		// 接收外部的String Array数据，得到对象列表
		String[] colmuns = detail_export_columns.split(",");

		if (data == null || data.length == 0)
			return;
		else {
			int aRow = data.length;
			int aCol = data[0].length;
			for (int i = 0; i < aRow; i++) {
				Object object = null;
				try {
					object = Class.forName(newObject.getClass().getName()).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}

				for (int j = 0; j < aCol; j++) {
					String setter = Util698.getSetter(colmuns[j]);
					if (setter.equals("setCaseno"))
						setter = setter;
					try {
						// 注意 new Class[] {String.class} 其中的String.class表示
						// fun(String.class);
						// System.out.println("converFormStringArray
						// setter:"+setter);
						Util698.setFieldValueByName(colmuns[j], object, data[i][j]);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
					// meter.
				}
				data_objs.add((T) object);
				objDao.create(object);
			}
		}
	}

	public void setNewObject(Object newObject) {
		this.newObject = newObject;
	}

	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public ActionListener getActionListener() {
		return actionListener;
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

}