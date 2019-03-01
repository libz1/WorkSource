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
		// ��ϸ��Ϣ���ֶ���Ϣ ���ݽ�Ϊȫ��
		String[] detail_colNames = { "�û���", "�û�����", "����", "�û�Ȩ��" };
		String detail_export_columns = "userid,username,userpwd,userPriority";

		String[] detail_colNames1 = { "����" };
		String detail_export_columns1 = "userpwd";

		// �б�չʾ���ֶ���Ϣ ֻ��һ��������
		String[] table_colNames = { "ID[0]", "�û���", "����", "�û�����", "�û�Ȩ��" };
		String table_columns = "ID,userid,userpwd,username,userPriority";

		String[] table_colNames1 = { "ID[0]", "�û���", "����", "�û�����" };
		String table_columns1 = "ID,userid,userpwd,username";

		// ��ͨ�û�
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
