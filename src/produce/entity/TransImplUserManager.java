package produce.entity;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import com.eastsoft.util.DataConvert;

import base.TransBehavior;
import javafx.base.TransBehavior_javafx;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import util.Util698;

/**
 * �������ת������
 * @author xuky
 * @version 2016.10.13
 *
 */
public class TransImplUserManager extends TransBehavior_javafx{

	//String[] ObjColumns = { "oi","��������","�ӿ���","˳���","״̬" };

	UserManager obj;
	@Override
	public void setData(Object object) {

		if (object == null){
			clearComponent();
			return;
		}
		// ������Ķ���ת��Ϊ��������ʾ������
		 obj = (UserManager)object;

		IDs[0] = DataConvert.int2String(obj.getID());
		((TextField) component[0]).setText(obj.getUserid());
		((TextField) component[1]).setText(obj.getUsername());
		((TextField) component[2]).setText(obj.getUserpwd());
		((TextField) component[3]).setText(DataConvert.int2String(obj.getUserPriority()));
	}

	@Override
	public UserManager getData() {
		// ����������ʾ������ת��Ϊ���� ��Ҫ�����������͵�ת��
		UserManager obj = new UserManager();

		obj.setID(DataConvert.String2Int(IDs[0]));
		obj.setUserid(((TextField)component[0]).getText());
		obj.setUsername(((TextField)component[1]).getText());
		obj.setUserpwd(((TextField)component[2]).getText());
		obj.setUserPriority(DataConvert.String2Int(((TextField)component[3]).getText()));

		return obj;
	}

	@Override
	public UserManager getDataWithID() {
		return null;
	}

}
