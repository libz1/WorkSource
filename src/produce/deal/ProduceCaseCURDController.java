package produce.deal;

import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import dao.basedao.IBaseDao;
import javafx.application.Platform;
import javafx.base.BaseController;
import javafx.base.ObjectCURD;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import produce.entity.DevInfo;
import produce.entity.DevInfoDaoImpl;
import produce.entity.ProduceCase;
import produce.entity.ProduceCaseDaoImpl;
import produce.entity.ProduceCaseSub;
import produce.entity.ProduceCaseSubDaoImpl;
import produce.entity.RunTest;
import produce.entity.RunTestDaoImpl;
import produce.entity.TransImplObject;
import produce.entity.TransImplProduceCase;
import util.Publisher;
import util.SoftParameter;
import util.Util698;

public class ProduceCaseCURDController extends BaseController implements Initializable, Observer {

	// @FXML
	// BorderPane object_crud_panel,object_crud_panel_casesub ;

	@FXML
	BorderPane object_crud_panel_case1, object_crud_panel_sub1, object_crud_panel_assitant;

	// ObjectCURD object_crud, object_crud_casesub;

	@FXML
	ComboBox<String> cb_pcid;

	ObjectCURD object_case1, object_crud_sub1, object_crud_runtest;

	String PCID = "";

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// �����Ϣ
		Publisher.getInstance().addObserver(this);


		{
			// ���Է�������------------------------------------------------
			String[] detail_colNames_sub = { "������", "��������", "��ע1", "��ע2", "��ע3" };
			String detail_export_columns_sub = "no,name,note1,note2,note3";

			String[] table_colNames_sub = { "ID[0]", "PCID", "������", "��������", "��ע1", "��ע2", "��ע3" };
			String table_columns_sub = "ID,computer,no,name,note1,note2,note3";

			object_crud_sub1 = new ObjectCURD<ProduceCaseSub>(new ProduceCaseSubDaoImpl(),
					new TransImplObject(detail_export_columns_sub, new ProduceCaseSub()), detail_colNames_sub,
					detail_export_columns_sub, table_colNames_sub, table_columns_sub, "getName,getNote1",
					"where computer='" + SoftParameter.getInstance().getPCID() + "'", "");
			object_crud_panel_sub1.setCenter(object_crud_sub1);

			object_crud_sub1.setDetailVisible(false);


			// ������������------------------------------------------------
			String[] detail_colNames = { "������", "ִ�д���", "��������", "���ͱ���-����", "��֤����-����", "��ǰ��ʱ", "��ʱ�ȴ�", "Э������", "���Դ���", "ͨ�Ŷ˿�"
					 };
			String detail_export_columns = "subid,caseno,name,send,expect,delaytime,waittime,protocol,retrys,note";

			String[] table_colNames = { "ID[0]", "������","ִ�д���", "��������", "���ͱ���", "��֤����", "��ǰ��ʱ", "��ʱ�ȴ�", "Э������", "���Դ���", "ͨ�Ŷ˿�",
					 };
			String table_columns = "ID,subid,caseno,name,send,expect,delaytime,waittime,protocol,retrys,note";

			// ����ΪgetSubid,getCaseno �����������ͬ��Caseno�����ᵼ��ִ��ʱ��λ�Ĵ���
//			object_case1 = new ObjectCURD<ProduceCase>(new ProduceCaseDaoImpl(), new TransImplProduceCase(),
//					detail_colNames, detail_export_columns, table_colNames, table_columns, "getSubid,getCaseno",
//					"where subid='9999'", "order by subid,caseno");

			object_case1 = new ObjectCURD<ProduceCase>(new ProduceCaseDaoImpl(), new TransImplObject(detail_export_columns,new ProduceCase()),
			detail_colNames, detail_export_columns, table_colNames, table_columns, "getSubid,getCaseno",
			"where subid='9999'", "order by subid,caseno");
			object_crud_panel_case1.setCenter(object_case1);
			object_case1.setCopyMsg("�������ݺ���ע���޸ķ�������Ϣ");
			object_case1.setNewObject(new ProduceCase());
			object_case1.setExportFileName("����������������.xls");

			// ���Ը�������------------------------------------------------
			String[] detail_colNames_test = { "������", "�˿�", "���ձ���-����", "�ظ�����-����", "����" };
			String detail_export_columns_test = "subid,port,recv,send,no";

			String[] table_colNames_test = { "ID[0]", "������", "�˿�", "���ձ���-����", "�ظ�����-����", "����" };
			String table_columns_test = "ID,subid,port,recv,send,no";

			object_crud_runtest = new ObjectCURD<RunTest>(new RunTestDaoImpl(),
					new TransImplObject(detail_export_columns_test, new RunTest()), detail_colNames_test,
					detail_export_columns_test, table_colNames_test, table_columns_test, "getRecv", "", "order by no");
			object_crud_panel_assitant.setCenter(object_crud_runtest);
			object_crud_runtest.setNewObject(new RunTest());
			object_crud_runtest.setExportFileName("���Ը��������ݵ�������.xls");
		}


		// ----�г����е�PCID��Ϣ----
		PCID = SoftParameter.getInstance().getPCID();

		IBaseDao<DevInfo> iBaseDao_DevInfo = new DevInfoDaoImpl();
		List result = iBaseDao_DevInfo
				.retrieveBySQL("select computer from " + ProduceCaseSub.class.getName() + " group by computer ");
		for (Object o : result)
			cb_pcid.getItems().add((String) o);

		cb_pcid.setValue(PCID);

		// xuky 2017.08.16 lambda��ʽ���������ؼ�ѡ�������¼�
		cb_pcid.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			PCID = newValue;
			object_crud_sub1.setWHERE("where computer='" + PCID + "'");
		});


	}

	@Override
	public void update(Observable o, Object arg) {
		try {
			Object[] s = (Object[]) arg;
			if (s[0].equals("RowChanged") && ((String) s[1]).indexOf("ProduceCaseSub") >= 0) {
				Platform.runLater(() -> {
					ProduceCaseSub produceCaseSub = (ProduceCaseSub) s[2];
					List<ProduceCase> result = new ProduceCaseDaoImpl().retrieve("where subid='"
							+ produceCaseSub.getNo() + "' and computer='" + PCID + "'",
							" order by caseno");
					ObservableList<ProduceCase> list = object_case1.getData_objs();

					Util698.ListReMoveAll(list);

					for (ProduceCase produceCase : result)
						list.add(produceCase);

				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
