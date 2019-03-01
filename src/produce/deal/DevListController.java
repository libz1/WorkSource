package produce.deal;

import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import com.eastsoft.util.DateTimeFun;

import dao.basedao.IBaseDao;
import entity.DataValues;
import javafx.application.Platform;
import javafx.base.BaseController;
import javafx.base.ObjectCURD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import produce.entity.DevInfo;
import produce.entity.DevInfoDaoImpl;
import produce.entity.ProduceCaseDaoImpl;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceCaseResultDaoImpl;
import produce.entity.ProduceLog;
import produce.entity.ProduceLogDaoImpl;
import produce.entity.TransImplObject;
import util.Publisher;
import util.Util698;

public class DevListController extends BaseController implements Initializable, Observer {

	@FXML
	BorderPane tableview_testresult, tableview_sumresult, tableview_result, tableview_log;

	@FXML
	ChoiceBox<String> cb_type;

	@FXML
	TextField txt_date1_b, txt_date2_b, txt_date1_e, txt_date2_e, txt_addr;

	@FXML
	CheckBox cb_err, cb_operate;

	ObservableList<DevInfo> dataList = null;
	ObservableList<DataValues> dataList_sum;
	ObservableList<ProduceCaseResult> dataListFrame = null;
	ObservableList<ProduceLog> dataListFrame_log = null;

	int rep2_colnum;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Publisher.getInstance().addObserver(this);

		txt_date1_b.setText(DateTimeFun.getDate());
		txt_date1_e.setText(DateTimeFun.getDate());
		txt_date2_b.setText(DateTimeFun.getDate());
		txt_date2_e.setText(DateTimeFun.getDate());

		cb_type.setItems(FXCollections.observableArrayList("全部", "成功", "故障"));
		cb_type.setValue("全部");
		{
			String[] detail_colNames = { "类型", "设备地址", "设备状态", "故障主因", "故障时间", "故障机位", "操作人员", "成功时间", "成功机位", "操作人员" };
			String detail_export_columns = "type,addr,status,barCode,errdatetime,errcomputer,erroperater,okdatetime,okcomputer,okoperater";

			// 列表展示用字段信息 只是一部分内容
			String[] table_colNames = { "ID[0]", "类型", "设备地址", "设备状态", "故障主因", "故障时间", "故障机位", "操作人员", "成功时间", "成功机位",
					"操作人员" };
			String table_columns = "ID,type,addr,status,barCode,errdatetime,errcomputer,erroperater,okdatetime,okcomputer,okoperater";

			ObjectCURD<DevInfo> object_crud = new ObjectCURD<DevInfo>(new DevInfoDaoImpl(),
					new TransImplObject(detail_export_columns, new DevInfo()), detail_colNames, detail_export_columns,
					table_colNames, table_columns, "getAddr,getErrdatetime", " where 1=2");
			tableview_testresult.setCenter(object_crud);
			dataList = object_crud.getData_objs();

			// 不显示CURD的按钮操作区域
			object_crud.setButtonsVisible(false);

		}

		// -------------------------------------
		{
			String[] detail_colNames = { "名称", "结果", "发送次数", "发送-单行", "接收-单行", "期望-单行", "发送时间", "接收时间", "发前延时", "超时等待",
					"重试次数", "端口" };
			String detail_export_columns = "name,result,sendtimes,send,recv,expect,sendtime,recvtime,delaytime,waittime,retrys,port";

			// 列表展示用字段信息 只是一部分内容
			String[] table_colNames = { "ID[0]", "名称", "结果", "发送次数", "发送", "发送时间", "接收", "接收时间", "端口", "期望", "发前延时",
					"超时等待", "重试次数" };
			String table_columns = "ID,name,result,sendtimes,send,sendtime,recv,recvtime,port,expect,delaytime,waittime,retrys";

			ObjectCURD<ProduceCaseResult> object_crud = new ObjectCURD<ProduceCaseResult>(new ProduceCaseDaoImpl(),
					new TransImplObject(detail_export_columns, new ProduceCaseResult()), detail_colNames,
					detail_export_columns, table_colNames, table_columns, "getSubid,getName", " where subid='999'");
			tableview_result.setCenter(object_crud);
			dataListFrame = object_crud.getData_objs();
			// 不显示CURD的按钮操作区域
			object_crud.setButtonsVisible(false);

		}
		{
			String[] detail_colNames = { "操作时间", "操作人", "PCID"};
			String detail_export_columns = "opTime,opName,workStation";

			// 列表展示用字段信息 只是一部分内容
			String[] table_colNames = { "ID[0]", "操作时间[200]", "操作人[80]", "PCID[150]" };
			String table_columns = "ID,opTime,opName,workStation";

			ObjectCURD<ProduceLog> object_crud = new ObjectCURD<ProduceLog>(new ProduceLogDaoImpl(),
					new TransImplObject(detail_export_columns, new ProduceLog()), detail_colNames,
					detail_export_columns, table_colNames, table_columns, "getOpTime", " where opName='999'");
			tableview_log.setCenter(object_crud);
			dataListFrame_log = object_crud.getData_objs();
			// 不显示CURD的按钮操作区域
			object_crud.setButtonsVisible(false);

		}


		// String[] detail_colNames1 = { "时间", "操作人", "类别", "数量" };
		// String detail_export_columns1 = "value1,value2,value3,data1";

		init_rep2();
	}

	private void init_rep2() {
		String[] table_colNames1 = null;

		rep2_colnum = 3;
		if (cb_operate.isSelected())
			rep2_colnum++;
		if (cb_err.isSelected())
			rep2_colnum++;
		table_colNames1 = new String[rep2_colnum];
		table_colNames1[0] = "时间";
		table_colNames1[1] = "数量";
		table_colNames1[2] = "类别";
		int j = 2;
		if (cb_operate.isSelected()) {
			j++;
			table_colNames1[j] = "操作人";
		}
		if (cb_err.isSelected()) {
			j++;
			table_colNames1[j] = "故障主因";
		}

		String table_columns1 = "value1,data1,value2";
		for (int i = 3; i < rep2_colnum; i++) {
			table_columns1 += ",value" + i;
		}

		// 列表展示用字段信息 只是一部分内容
		String[] detail_colNames1 = { "时间", "数量" };
		String detail_export_columns1 = "value1,data1";

		ObjectCURD<DataValues> object_crud_sum = new ObjectCURD<DataValues>(new DevInfoDaoImpl(),
				new TransImplObject(detail_export_columns1, new DataValues()), detail_colNames1, detail_export_columns1,
				table_colNames1, table_columns1, "getAddr,getErrdatetime", " where 1=2");
		tableview_sumresult.setCenter(object_crud_sum);
		dataList_sum = object_crud_sum.getData_objs();

		// 不显示CURD的按钮操作区域
		object_crud_sum.setButtonsVisible(false);
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
		IBaseDao<DevInfo> iBaseDao_DevInfo = new DevInfoDaoImpl();
		// String where = "where (errdatetime like('" + date + "%') or
		// okdatetime like('" + date + "%'))";
		String where = "where (errdatetime >= '" + date + "' and errdatetime <= '" + date1 + "') or (okdatetime >= '"
				+ date + "' and okdatetime <= '" + date1 + "'))";
		if (cb_type.getValue().equals("成功"))
			where = where + " and status ='测试完毕(1)'";
		if (cb_type.getValue().equals("故障"))
			where = where + " and status ='设备故障(2)'";
		List<DevInfo> result = iBaseDao_DevInfo.retrieve(where, "");
		Util698.ListReMoveAll(dataList);
		for (DevInfo devInfo : result)
			dataList.add(devInfo);

	}

	@FXML
	public void getSumDataAction(ActionEvent event) {
		init_rep2();

		String b = "yyyy-mm-dd 00:00:00:000";
		String date = txt_date2_b.getText();
		date = date + b.substring(date.length());

		String e = "yyyy-mm-dd 23:59:59:999";
		String date1 = txt_date1_e.getText();
		date1 = date1 + e.substring(date1.length());

		Util698.ListReMoveAll(dataList_sum);

		IBaseDao<DevInfo> iBaseDao_DevInfo = new DevInfoDaoImpl();
		// List result = iBaseDao_DevInfo.retrieveBySQL(
		// "select substring(errdatetime,1,10) as datetime,erroperater as
		// operater,status,count(*) as count from "
		// + DevInfo.class.getName()
		// + " where status = '设备故障(2)' and errdatetime like('" + date + "%')
		// group by substring(errdatetime,1,10),erroperater,status ");
		String sql_select = "select substring(errdatetime,1,10) as datetime,count(*) as count,status";
		String sql_select_ok = "select substring(okdatetime,1,10) as datetime,count(*) as count,status";
		String sql_group = "group by substring(errdatetime,1,10),status";
		String sql_group_ok = "group by substring(okdatetime,1,10),status";

		if (cb_operate.isSelected()) {
			sql_select += ",erroperater as operater";
			sql_group += ",erroperater";
			sql_select_ok += ",okoperater as operater";
			sql_group_ok += ",okoperater";
		}
		if (cb_err.isSelected()) {
			sql_select += ",barCode";
			sql_group += ",barCode";
			sql_select_ok += ",barCode";
			sql_group_ok += ",barCode";
		}
		List result = iBaseDao_DevInfo.retrieveBySQL(
				sql_select + " from " + DevInfo.class.getName() + " where status = '设备故障(2)' and (errdatetime >= '"
						+ date + "' and errdatetime <= '" + date1 + "') " + sql_group);
		for (Object o : result) {
			Object[] objs = new Object[] {};
			objs = (Object[]) o;
			DataValues dataValues = new DataValues();
			dataValues.setValue1((String) objs[0]);
			dataValues.setData1(((Long) objs[1]).intValue());
			dataValues.setValue2((String) objs[2]);

			for (int i = 3; i < rep2_colnum; i++) {
				Util698.setFieldValueByName("Value" + i, dataValues, objs[i]);
			}
			dataList_sum.add(dataValues);
		}
		result = iBaseDao_DevInfo.retrieveBySQL(
				sql_select_ok + " from " + DevInfo.class.getName() + " where status = '测试完毕(1)' and (okdatetime >= '"
						+ date + "' and okdatetime <= '" + date1 + "') " + sql_group_ok);
		for (Object o : result) {
			Object[] objs = new Object[] {};
			objs = (Object[]) o;
			DataValues dataValues = new DataValues();
			dataValues.setValue1((String) objs[0]);
			dataValues.setData1(((Long) objs[1]).intValue());
			dataValues.setValue2((String) objs[2]);
			for (int i = 3; i < rep2_colnum; i++) {
				Util698.setFieldValueByName("Value" + i, dataValues, objs[i]);
			}
			dataList_sum.add(dataValues);
		}
	}

	@FXML
	public void getResultAction(ActionEvent event) {
		String addr = txt_addr.getText();
		if (addr.length() < 12)
			addr = Util698.leftPaddingZero(addr, 12);

		Util698.ListReMoveAll(dataListFrame_log);
		IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();
		List result = iBaseDao_ProduceLog.retrieve(" where addr='"+addr, "' and operation='扫描条码(1)' order by opTime desc");
		for (Object o : result) {
			dataListFrame_log.add((ProduceLog)o);
		}




//		Util698.ListReMoveAll(dataListFrame);
//
//		IBaseDao<ProduceCaseResult> iBaseDao_ProduceCaseResult = new ProduceCaseResultDaoImpl();
//
//		String sql = "select max(ID) from " + ProduceLog.class.getName() + " where addr='" + addr + "' and operation='扫描条码(1)'";
//		int runid = -1;
//		result = iBaseDao_ProduceCaseResult.retrieveBySQL(sql);
//		if (result != null)
//			if (result.size() >= 0) {
//				if (result.get(0) != null)
//					runid = (int) result.get(0);
//				else
//					return;
//			}
//
//		if (runid != -1){
//			// xuky 2017.08.08  取最大的，但是需要是扫描的那个
////			runid = runid -1;
//			result = iBaseDao_ProduceCaseResult.retrieve(" where runID="+runid, " order by sendtime");
//			for (Object o : result) {
//				dataListFrame.add((ProduceCaseResult)o);
//			}
//		}
	}

	@Override
	public void update(Observable o, Object arg) {
		try {
			Object[] s = (Object[]) arg;
			if (s[0].equals("RowChanged") && ((String) s[1]).indexOf("ProduceLog") >= 0) {
				Platform.runLater(() -> {
					ProduceLog produceLog = (ProduceLog) s[2];
					List<ProduceCaseResult> result = new ProduceCaseResultDaoImpl().retrieve("where runid="
							+ produceLog.getID(),	" order by sendtime");

					Util698.ListReMoveAll(dataListFrame);

					for (ProduceCaseResult produceCase : result)
						dataListFrame.add(produceCase);

				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
