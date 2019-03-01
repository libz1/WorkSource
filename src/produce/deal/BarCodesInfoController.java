package produce.deal;

import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import javafx.application.Platform;
import javafx.base.BaseController;
import javafx.base.ObjectCURD;
import javafx.base.javafxutil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import produce.entity.BarCodesInfo;
import produce.entity.BarCodesInfoDaoImpl;
import produce.entity.TransImplObject;
import util.Publisher;
import util.ReadCSV;
import util.SoftParameter;
import util.Util698;

public class BarCodesInfoController extends BaseController implements Initializable,Observer {

	@FXML
	BorderPane tableview_testresult;

	@FXML
	Label txt_process;

	@FXML
	TextField txt_date1_b, txt_date1_e;
	List<String> import_list;

	@FXML
	CheckBox cb_err, cb_operate;

	ObservableList<BarCodesInfo> dataList = null;
	ObjectCURD<BarCodesInfo> object_crud = null;
	BarCodesInfoDaoImpl barCodesInfoDaoImpl = new BarCodesInfoDaoImpl();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		 Publisher.getInstance().addObserver(this);

		txt_date1_b.setText(DateTimeFun.getDate());
		txt_date1_e.setText(DateTimeFun.getDate());

		{
			String[] detail_colNames = { "条码", "导入日期", "导入人" };
			String detail_export_columns = "barcode,opdatetime,operater";

			// 列表展示用字段信息 只是一部分内容
			String[] table_colNames = { "ID[0]", "条码[200]", "导入日期[200]", "导入人" };
			String table_columns = "ID,barcode,opdatetime,operater";

			object_crud = new ObjectCURD<BarCodesInfo>(new BarCodesInfoDaoImpl(),
					new TransImplObject(detail_export_columns, new BarCodesInfo()), detail_colNames,
					detail_export_columns, table_colNames, table_columns, "getOpdatetime", " where 1=2");
			tableview_testresult.setCenter(object_crud);
			dataList = object_crud.getData_objs();

			// 不显示CURD的按钮操作区域
			object_crud.setButtonsVisible(false);

		}

	}
	@FXML
	public void inportFromExcelAction(ActionEvent event) {
		String fileName = javafxutil.fileChoose();
		new Thread(() -> {
			importFun(fileName);
		}).start();
//		Platform.runLater(() -> {
//			importFun();
//		});

	}

	public void importFun(String fileName) {

		import_list = ReadCSV.readFileByLines(fileName);
		BarCodesInfo barCodesInfo = new BarCodesInfo();
		String opdatetime = Util698.getDateTimeSSS_new(),
				operater = SoftParameter.getInstance().getUserManager().getUserid();
		Util698.log(BarCodesInfoController.class.getName(), "开始导入条码数据" , Debug.LOG_INFO);
		int i = 0,j=0;
		for (String str : import_list) {
			barCodesInfo.setBarcode(str);
			barCodesInfo.setOpdatetime(opdatetime);
			barCodesInfo.setOperater(operater);
			barCodesInfoDaoImpl.create(barCodesInfo);
			i++;
			j++;
			if (i==500 || i==1){
				if (i==500) i = 0;
				Util698.log(BarCodesInfoController.class.getName(), "导入条码数据进度"+j , Debug.LOG_INFO);
				Object[] s = { "ProcessChanged", BarCodesInfo.class.getName(), j };
				Publisher.getInstance().publish(s);
			}
		}
//		Util698.log("效率分析", "数据库交互2 begin2" , Debug.LOG_INFO);
		Util698.log(BarCodesInfoController.class.getName(), "导入条码数据结束" , Debug.LOG_INFO);
		javafxutil.f_alert_informationDialog("操作提示", "导入数据成功！");
	}

	@FXML
	public void export2ExcelAction(ActionEvent event) {
		// exportAction
		object_crud.export2Excel();

	}

	@FXML
	public void getDataAction(ActionEvent event) {
		String b = "yyyy-mm-dd 00:00:00:000";
		String date = txt_date1_b.getText();
		date = date + b.substring(date.length());
		// yyyy-mm-dd hh:mm:ss:SSS 对时间数据进行格式补齐 起始补充为 00:00:00:000 结束补充为
		// 23:59:59:999
		String e = "yyyy-mm-dd 23:59:59:999";
		String date1 = txt_date1_e.getText();
		date1 = date1 + e.substring(date1.length());
		IBaseDao<BarCodesInfo> iBaseDao_DevInfo = new BarCodesInfoDaoImpl();
		// String where = "where (errdatetime like('" + date + "%') or
		// okdatetime like('" + date + "%'))";
		String where = "where opdatetime >= '" + date + "' and opdatetime <= '" + date1 + "'";
		// if (cb_type.getValue().equals("成功"))
		// where = where + " and status ='测试完毕(1)'";
		// if (cb_type.getValue().equals("故障"))
		// where = where + " and status ='设备故障(2)'";
		List<BarCodesInfo> result = iBaseDao_DevInfo.retrieve(where, "");
		Util698.ListReMoveAll(dataList);
		for (BarCodesInfo devInfo : result)
			dataList.add(devInfo);

	}

	@Override
	public void update(Observable o, Object arg) {
		try {
			Object[] s = (Object[]) arg;
			if (s[0].equals("ProcessChanged") && ((String) s[1]).indexOf("BarCodesInfo") >= 0) {
				Platform.runLater(() -> {
					txt_process.setText("进度信息["+DataConvert.int2String((int)s[2])+"-"+import_list.size()+"]");
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
