package produce.deal;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.base.BaseController;
import javafx.base.ObjectCURD;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import produce.entity.TransImplUserManager;
import produce.entity.TransImplUserManagerOnlyPwd;
import produce.entity.UserManager;
import produce.entity.UserManagerDaoImpl;
import util.SoftParameter;

public class UserManagerCURDController extends BaseController implements Initializable {

	@FXML
	BorderPane object_crud_panel;

	ObjectCURD object_crud;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		int priority = SoftParameter.getInstance().getUserManager().getUserPriority();

		// ------------------------------------------------
		// 明细信息用字段信息 内容较为全面
		String[] detail_colNames = { "用户名", "用户姓名", "密码", "用户权限" };
		String detail_export_columns = "userid,username,userpwd,userPriority";

		String[] detail_colNames1 = { "密码" };
		String detail_export_columns1 = "userpwd";

		// 列表展示用字段信息 只是一部分内容
		String[] table_colNames = { "ID[0]", "用户名", "密码", "用户姓名", "用户权限" };
		String table_columns = "ID,userid,userpwd,username,userPriority";

		String[] table_colNames1 = { "ID[0]", "用户名", "密码", "用户姓名" };
		String table_columns1 = "ID,userid,userpwd,username";

		// 普通用户
		if (priority == 0){
			object_crud = new ObjectCURD<UserManager>(new UserManagerDaoImpl(), new TransImplUserManagerOnlyPwd(),
					detail_colNames1, detail_export_columns1, table_colNames1, table_columns1, "getUserid"," where userid='"+SoftParameter.getInstance().getUserManager().getUserid()+"'");
			object_crud.setButtonVisible();
		}
		else
			object_crud = new ObjectCURD<UserManager>(new UserManagerDaoImpl(), new TransImplUserManager(),
					detail_colNames, detail_export_columns, table_colNames, table_columns, "getUserid");
		object_crud_panel.setCenter(object_crud);

	}

}
