package produce.deal;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.base.BaseController;
import javafx.base.ObjectCURD;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import produce.entity.BarCodeAndAddr;
import produce.entity.BarCodeAndAddrDaoImpl;
import produce.entity.TransImplBarCode;

public class BarCodeCURDController extends BaseController implements Initializable {

	@FXML
	BorderPane object_crud_panel;

	ObjectCURD<BarCodeAndAddr> object_crud;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// ------------------------------------------------
		// ��ϸ��Ϣ���ֶ���Ϣ ���ݽ�Ϊȫ��
		String[] detail_colNames = { "������", "����ֹ", "������", "����ֹ", "��ַ��", "��ַֹ" };
		String detail_export_columns = "longBarCodeBegin,longBarCodeEnd,shortBarCodeBegin,shortBarCodeEnd,addrBegin,addrEnd";

		String[] table_colNames = { "ID[0]", "������", "����ֹ", "������", "����ֹ", "��ַ��", "��ַֹ" };
		String table_columns = "ID,longBarCodeBegin,longBarCodeEnd,shortBarCodeBegin,shortBarCodeEnd,addrBegin,addrEnd";

		object_crud = new ObjectCURD<BarCodeAndAddr>(new BarCodeAndAddrDaoImpl(), new TransImplBarCode(),
					detail_colNames, detail_export_columns, table_colNames, table_columns, "getAddrBegin");
		object_crud_panel.setCenter(object_crud);

	}

}
