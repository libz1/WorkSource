package produce.entity;

import com.eastsoft.util.DataConvert;

import javafx.base.TransBehavior_javafx;
import javafx.scene.control.TextField;
import util.Util698;

/**
 * �������ת������
 * @author xuky
 * @version 2016.10.13
 *
 */
public class TransImplUserManagerOnlyPwd extends TransBehavior_javafx{

	//String[] ObjColumns = { "oi","��������","�ӿ���","˳���","״̬" };

	// xuky 2017.06.20  �����û�ֻ���޸Ĳ������ݣ���������޸�������Ϣ
	private UserManager obj_old;

	@Override
	public void setData(Object object) {

		if (object == null){
			clearComponent();
			return;
		}
		// ������Ķ���ת��Ϊ��������ʾ������
		obj_old = (UserManager)object;

		IDs[0] = DataConvert.int2String(obj_old.getID());
		((TextField) component[0]).setText(obj_old.getUserpwd());
	}

	@Override
	public UserManager getData() {
		// ����������ʾ������ת��Ϊ���� ��Ҫ�����������͵�ת��

		// xuky 2017.06.20 �ǳ���� �����ֱ��ʹ�� obj_old���������޸ģ����ݷ��أ��ͻ�����޸���ɺ���ϸ���ݲ�ˢ�µ�״��
		UserManager obj1 = new UserManager();
		Util698.objClone(obj_old, obj1, "");

		obj1.setID(DataConvert.String2Int(IDs[0]));
		obj1.setUserpwd(((TextField)component[0]).getText());
		return obj1;

//		obj_old.setUserpwd(((TextField)component[0]).getText());
//		return obj_old;


	}

	@Override
	public UserManager getDataWithID() {
		return null;
	}

}
