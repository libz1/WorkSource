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

// ͨ��CURD�ؼ�
public class ObjectCURD<T> extends BorderPane {

	String exportFileName = "��������.xls";
	// String[] exportColmunNames;
	// String exportColmuns;

	ActionListener actionListener = null;

	Object newObject = null;

	String[] detail_colNames = null;
	String detail_export_columns = "";

	String[] table_colNames = null;
	String table_columns = "";
	Class<?> clazz = null;
	// ÿ����ʾ������
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

			// xuky 2017.05.02 �ӵ�ǰ���У��޷���ȡ����T�����ͣ����ǿ��Դ�objDao�еõ�
			Type type = objDao.getClass().getGenericSuperclass();
			clazz = ((Class<?>) (((ParameterizedType) (type)).getActualTypeArguments()[0]));

			// xuky 2017.05.31 �����û��趨��̬��Ӱ�ť
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

	// ��ʼ���б�
	public void table_init0() {
		// ������ϸ����
		info_detail = new InfoClass_javafx(detail_colNames, colNum);
		// xuky 2017.05.26 �ر�ע�⣺transBehaviorֻ��һ��ʵ�����ں����ĵ�������������˸�ֵ�����ᵼ������Ŀ���ʧЧ
		info_detail.setTrans(transBehavior);
		infoDetail_pane.getChildren().add(info_detail.getPanel());

		// ������ϸ��Ϣ�ĸ����Զ�����ؼ���ռ���ܸ߶�
		// double tmp = detail_colNames.length * 100 / colNum;
		// tmp = tmp / 100;
		// int rowNum = (int) Math.ceil(tmp);

		// xuky 2017.07.03 ����ʵ�ʵ��������м���
		int rowNum = info_detail.getROWNUM();

		infoDetail_pane.setMinHeight(rowNum * 37);
		infoDetail_pane.setMaxHeight(rowNum * 37);

		// // �����б�ؼ� ʹ��[0]��ʾ���ֶβ�����ʾ
		// String[] table_columns_array = table_columns.split(",");
		// // ���������ֶΰ�
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
		// // �б�ؼ������ѡ���¼�
		// // ����ʹ��lambd���ʽ������ambiguous����

		// ˢ���б��е�����
		refreshTableData();
	}

	public void refreshTableData() {
		refreshTableData(-1);
	}

	// ��ȡ�б�����
	public void refreshTableData(int row) {

		// xuky 2017.02.17 ʹ��FXJava�����첽����
		Observable<String> get_tabelData = Observable.create(string -> {
			// getParentScene().setCursor(Cursor.WAIT);
			List<T> result = objDao.retrieve(WHERE, ORDERBY);
			// xuky 2018.04.18 ϣ��ͨ�������ķ�ʽ���ͷ��ڴ����ݣ���ֹ�ڴ�й©
			data_objs = null;
			data_objs = FXCollections.observableArrayList(result);
			string.onNext("");
		});
		// xuky 2017.03.01 ���ʹ�������µĴ��룬�ڽ���combox���Ϳؼ����и�ֵ��ʱ�������쳣
		// .subscribeOn(Schedulers.io())
		get_tabelData.subscribe((string) -> {

			TableViewWithSum tableViewWithSum = new TableViewWithSum();
			// ObservableList<FreezeDay> dayDataDB =
			// FXCollections.observableArrayList(new ArrayList<FreezeDay>());
			// // 2��ȷ���б���ֶα��⡢�ֶ���ʾ���ݼ���ʽ
			// // 3�����ж����ʼ��
			// String[] table_colNames = null;
			// String table_columns = "";
			objectTable = tableViewWithSum.init(parentStage, data_objs, table_colNames, table_columns);
			//// // 4����ӵ��������չʾ
			dataTable_pane.setCenter(tableViewWithSum);

			// xuky 2017.10.27 ���Խ��ж�ѡ
			// �ο� http://www.cnblogs.com/SEC-fsq/p/6825955.html
			objectTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

			objectTable.getSelectionModel().getSelectedCells().addListener(new ListChangeListener() {
				@Override
				public void onChanged(Change c) {
					while (c.next()) {
						// xuky 2017.02.17 �������� ��õ�ǰ��
						MappingChange mc = (MappingChange) c;
						ObservableList ol = mc.getList();
						if (ol.size() <= 0)
							return;
						TablePosition o = (TablePosition) ol.get(0);
						int row = o.getRow();
						// xkuy 2017.02.17 ������Ĭ��ѡ��ʱ�������쳣���õ����к�Ϊ-1
						// System.out.println("change:"+terminalTable.getSelectionModel().getSelectedIndex());
						if (row >= 0) {
							T object = (T) data_objs.get(row);
							// SoftParameter.getInstance().setSendTerminal(terminal.getTerminalCode0xH());
							// SoftParameter.getInstance().saveParam();
							info_detail.setData(object);

							// xuky 2017.07.06 ����кű仯��Ϣ
							Object[] s = { "RowChanged", object.getClass().getName(), object };
							Publisher.getInstance().publish(s);

						}
					}
				}
			});

			// xuky 2017.05.31 ˢ���б������ݺ󣬶�λ��ָ����
			if (row > 0) {
				if (row < objectTable.getColumns().size())
					objectTable.getSelectionModel().select(row);
			} else if (objectTable.getColumns().size() > 0)
				objectTable.getSelectionModel().select(0);

			// // �б�ؼ���������
			// objectTable.setItems(data_objs);
			// // xuky Ĭ��ѡ�е�һ��
			// // getParentScene().setCursor(Cursor.DEFAULT);
		});

	}

	// �ο�http://blog.csdn.net/zzq900503/article/details/36202353
	// private static <T> T newTclass(Class<T> clazz) throws
	// InstantiationException, IllegalAccessException {
	// T a = clazz.newInstance();
	// return a;
	// }
	// ����һ��Class�Ķ�������ȡ���͵�class��ʵ��BaseDao�Ĺؼ�
	private Class<?> clz;

	public Class<?> getClz() {
		if (clz == null) {
			Type type = this.getClass().getGenericSuperclass();
			clz = ((Class<?>) (((ParameterizedType) (type)).getActualTypeArguments()[0]));
			// ��ȡ���͵�Class���� ParameterizedType getActualTypeArguments()[0]ȡ�õ�һ��
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
		// ʹ��Ĭ�Ϲ���������Ϊ��ʼ�����ݡ�
		// clazz.newInstance() �൱��new Terminal();�ȹ�������
		try {
			info_detail_pop.setData(clazz.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// xuky 2017.05.10 CRUD������ҵ���߼����������չʾ������� 1
		Stage window = new Stage();

		// xuky 2017.05.10 ��������ʱ�����ݹؼ��ֶΣ��ж��Ƿ�Ϊ�ظ�����
		EventHandler<ActionEvent> ok_action = e -> {
			T obj = (T) info_detail_pop.trans.getData();
			if (obj != null) {
				// ���ݹؼ��ֶ����ݣ�����û���ӵ������Ƿ��ظ�
				String getter = indexCols;
				String value = (String) Util698.getObjectAttrs(obj, getter);
				Object tmp = getByCode(value, getter);
				if (tmp != null) {
					String msg = "�ؼ������ظ�������! \r\n�ؼ������ֶ���:" + getter + "�ؼ���������:" + value;
					javafxutil.f_alert_informationDialog("������ʾ", msg);
					// ���رյ����������û�������������޸ĺ��ٴ��ύ������
				} else {
					// ��̨��������
					objDao.create(obj);
					// ǰ̨�����б���������
					data_objs.add((T) obj);

					// xuky 2017.05.26 ���⴦��������Ӻ��б仯ʱ����ϸ��Ϣ����֮�仯����
					info_detail.setTrans(transBehavior);

					// ѡ���������к�
//					objectTable.getSelectionModel().select(obj);

					// ѡ���������к�  1��ȡ��֮ǰ��ѡ��  2��ѡ�����һ������
					objectTable.getSelectionModel().clearSelection();
					objectTable.getSelectionModel().selectLast();
					// �رյ���
					window.close();
				}
			}
		};

		EventHandler<ActionEvent> cancle_action = e -> {
			// xuky 2017.05.26 ���⴦��������Ӻ��б仯ʱ����ϸ��Ϣ����֮�仯����
			info_detail.setTrans(transBehavior);
			// �رյ���
			window.close();
		};


		// xuky 2017.05.10 CRUD������ҵ���߼����������չʾ������� 2
		new PopBox().display("����", info_detail_pop, window, ok_action, cancle_action);

		if (actionListener != null)
			actionListener.actionPerformed(null);
	}

	public Object getByCode(String code, String getter) {
		return getByCode(code, getter, -1);
	}

	// �ж��Ƿ����ظ�����
	public Object getByCode(String code, String getter, int row) {
		Object object = null;
		try {
			int i = 0;
			for (Object o : data_objs) {
				// xuky 2017.05.10 ���޸�ʱ�����в��رȽϣ���Ҫ���Լ��Ƚ�
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
			// xuky 2017.05.10 �Ҳ�����ԭ������ǵ�����ĩ�����Դ�ͷ����
			find_obj = findObj(-1, info);
			if (find_obj == null)
				javafxutil.f_alert_informationDialog("���Ҷ�λ���", "û���ҵ�");
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
		// ͨ��TableViewSelectionModel��ѡ���н��в���
		// http://lujin55.iteye.com/blog/1720004
		TableViewSelectionModel sm = objectTable.getSelectionModel();
		int row = sm.getSelectedIndex();
		if (row < 0) {
			// new AlertBox().display("������ʾ", "����ѡ����Ҫ�޸ĵ���");
			javafxutil.f_alert_informationDialog("������ʾ", "����ѡ����Ҫ�޸ĵ���");
			return;
		}
		T obj_old = (T) data_objs.get(row);

		InfoClass_javafx info_detail_pop = new InfoClass_javafx(detail_colNames, 3);
		info_detail_pop.setTrans(transBehavior);
		// ʹ��Ĭ�Ϲ���������Ϊ��ʼ������
		info_detail_pop.setData(obj_old);

		// xuky 2017.05.10 CRUD������ҵ���߼����������չʾ������� 1
		Stage window = new Stage();

		// xuky 2017.05.10 ��������ʱ�����ݹؼ��ֶΣ��ж��Ƿ�Ϊ�ظ�����
		// xuky 2017.05.10 �޸�����ʱ����Ҫ���⴦��������ԭ�ȵ��Լ���ȣ����ǲ��������������
		EventHandler<ActionEvent> ok_action = e -> {
			T obj = (T) info_detail_pop.trans.getData();
			if (obj != null) {
				// ���ݹؼ��ֶ����ݣ�����û���ӵ������Ƿ��ظ�
				String getter = indexCols;
				String value = (String) Util698.getObjectAttrs(obj, getter);
				Object tmp = getByCode(value, getter, row);
				if (tmp != null) {
					String msg = "�ؼ������ظ�������! \r\n�ؼ������ֶ���:" + getter + "�ؼ���������:" + value;
					javafxutil.f_alert_informationDialog("������ʾ", msg);
					// ���رյ����������û�������������޸ĺ��ٴ��ύ������
				} else {
					// ��̨��������
					objDao.update(obj);
					// ǰ̨�����б���������
					data_objs.set(row, obj);

					// xuky 2017.05.26 ���⴦��������Ӻ��б仯ʱ����ϸ��Ϣ����֮�仯����
					info_detail.setTrans(transBehavior);

					objectTable.getSelectionModel().select(obj);
					// �رյ���
					window.close();
				}
			}
		};
		EventHandler<ActionEvent> cancle_action = e -> {
			// xuky 2017.05.26 ���⴦��������Ӻ��б仯ʱ����ϸ��Ϣ����֮�仯����
			info_detail.setTrans(transBehavior);
			// �رյ���
			window.close();
		};


		// xuky 2017.05.10 CRUD������ҵ���߼����������չʾ������� 2
		new PopBox().display("�޸�", info_detail_pop, window, ok_action, cancle_action);

		// Object[] object = new PopBox().display("�޸�", info_detail);
		// obj = (T) object[0];
		//
		// if (obj != null) {
		// // ��̨����ά��
		// objDao.update(obj);
		// // ��������ˢ��
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

		// ͨ��TableViewSelectionModel��ѡ���н��в���
		// http://lujin55.iteye.com/blog/1720004
		TableViewSelectionModel sm = objectTable.getSelectionModel();
		int row = sm.getSelectedIndex();
		if (row < 0) {
			// new AlertBox().display("������ʾ", "����ѡ����Ҫ�޸ĵ���");
			javafxutil.f_alert_informationDialog("������ʾ", "����ѡ����Ҫɾ������");
			return;
		}
		if (javafxutil.f_alert_confirmDialog("������ʾ", "�Ƿ�ȷ��ɾ��ѡ�е��У�") == false)
			return;

		T obj = data_objs.get(row);
		objDao.delete(obj);
		data_objs.remove(row);
	}

	@FXML
	public void deleteAllAction(ActionEvent event) {
		// ��ȡѡ�е�����
		ObservableList<T> data_objs_selected = objectTable.getSelectionModel().getSelectedItems();
		int num = data_objs_selected.size();
		if (num <= 0)
			return;
		if (javafxutil.f_alert_confirmDialog("������ʾ", "�Ƿ�ȷ��ɾ��ѡ�����ݣ�") == false)
			return;

		Object[] objs = new Object[num];

		// xuky 2017.10.27 �������ݵ�ɾ����data_objs_selected������Ҳ�ڱ仯���������ȼ�¼��ѡ�е�����
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

		// ͨ��TableViewSelectionModel��ѡ���н��в���
		// http://lujin55.iteye.com/blog/1720004
		TableViewSelectionModel sm = objectTable.getSelectionModel();
		int row = sm.getSelectedIndex();
		if (row < 0) {
			// new AlertBox().display("������ʾ", "����ѡ����Ҫ�޸ĵ���");
			javafxutil.f_alert_informationDialog("������ʾ", "����ѡ����Ҫ���Ƶ���");
			return;
		}
		if (javafxutil.f_alert_confirmDialog("������ʾ", "�Ƿ�ȷ�ϸ���ѡ�е��У�") == false)
			return;

		T obj = data_objs.get(row);
		try {
			obj = (T) objDao.create(obj);
			info_detail.setTrans(transBehavior);
			data_objs.add((T) obj);
			// xuky 2017.07.03 ����Ĵ�����һ���С���� objʵ���ϻ���ԭ�ȵ��Ǹ�
			// ѡ�����һ������
			objectTable.getSelectionModel().select(data_objs.size() - 1);

			javafxutil.f_alert_informationDialog("������ʾ", copyMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// // xuky �����û�������ʾ
		// if (!copyMsg.equals(""))
		// javafxutil.f_alert_informationDialog("������ʾ", copyMsg);

	}

	private static <T> T newTclass(Class<T> clazz) throws InstantiationException, IllegalAccessException {
		T a = clazz.newInstance();
		return a;

	}

	public int getSelectRow() {
		TableViewSelectionModel sm = objectTable.getSelectionModel();
		int row = sm.getSelectedIndex();
		if (row < 0) {
			// new AlertBox().display("������ʾ", "����ѡ����Ҫ�޸ĵ���");
			javafxutil.f_alert_informationDialog("������ʾ", "����ѡ����Ҫ�޸ĵ���");
		}
		return row;
	}

	public T getSelectObj(int row) {
		if (row < 0) {
			// new AlertBox().display("������ʾ", "����ѡ����Ҫ�޸ĵ���");
			javafxutil.f_alert_informationDialog("������ʾ", "����ѡ����Ҫ�޸ĵ���");
			return null;
		}
		if (row >= data_objs.size()) {
			javafxutil.f_alert_informationDialog("������ʾ", "��������");
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

			// ��������ת��Ϊ�ַ�������
			String[][] data = getStringArray();

			// ���ַ�������ת��Ϊexcel�ļ�
			String ret = ReadWriteExcel.stringArray2Excel(data, fileName);

			// ֮ǰ���ϵĺ�����������е����ݵ���Ϊexcel�ļ�
			// String ret = ReadWriteUtil.table2Excel(colNames,
			// defaultModel,fileName);

			// ���ݷ��ؽ���������û�����
			if (ret.equals(""))
				javafxutil.f_alert_informationDialog("������ʾ", "���ݵ�����\"" + fileName + "\"�ɹ���");
			else
				javafxutil.f_alert_informationDialog("������ʾ", "���ݵ���ʧ�ܣ�");
		}

	}

	public String[][] getStringArray() {
		// List objList = new ArrayList<T>();
		String[][] data = null;
		int aRow = data_objs.size();
		int aCol = detail_colNames.length;
		data = new String[aRow + 1][aCol];
		String[] colmuns = detail_export_columns.split(",");
		// ��һ���Ƕ���������Ϣ
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

					// �õ���ȡ�������Եķ�������
					String getter = Util698.getGetter(colmuns[j]);

					object = data_objs.get(i);
					try {
						// ��̬ ���������ػ�ȡ�������Եĺ���

						// xuky 2017.03.21 ��Ӷ����ֶ����Ե��ж�
						Object val = Util698.getObjectAttr(object, getter);
						if (val == null) {
							// xuky ��Ӷ��ڿ�ֵ�Ĵ���
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
		// DebugSwing.showMsg("�������ݳɹ���");
		javafxutil.f_alert_informationDialog("������ʾ", "�������ݳɹ���");
	}

	private void deleteAll() {
		if (data_objs != null)
			if (data_objs.size() > 0) {
				for (int i = data_objs.size() - 1; i >= 0; i--)
					data_objs.remove(i);
			}
	}

	public void converFormStringArray(String[][] data) {
		// �����ⲿ��String Array���ݣ��õ������б�
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
						// ע�� new Class[] {String.class} ���е�String.class��ʾ
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