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
		// 明细信息用字段信息 内容较为全面
		String[] detail_colNames = { "长码起", "长码止", "短码起", "短码止", "地址起", "地址止" };
		String detail_export_columns = "longBarCodeBegin,longBarCodeEnd,shortBarCodeBegin,shortBarCodeEnd,addrBegin,addrEnd";

		String[] table_colNames = { "ID[0]", "长码起", "长码止", "短码起", "短码止", "地址起", "地址止" };
		String table_columns = "ID,longBarCodeBegin,longBarCodeEnd,shortBarCodeBegin,shortBarCodeEnd,addrBegin,addrEnd";

		object_crud = new ObjectCURD<BarCodeAndAddr>(new BarCodeAndAddrDaoImpl(), new TransImplBarCode(),
					detail_colNames, detail_export_columns, table_colNames, table_columns, "getAddrBegin");
		object_crud_panel.setCenter(object_crud);

	}

}
