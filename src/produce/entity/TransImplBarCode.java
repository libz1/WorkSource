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
public class TransImplBarCode extends TransBehavior_javafx{

	//String[] ObjColumns = { "oi","��������","�ӿ���","˳���","״̬" };

	BarCodeAndAddr obj;
	@Override
	public void setData(Object object) {

		if (object == null){
			clearComponent();
			return;
		}
		// ������Ķ���ת��Ϊ��������ʾ������
		 obj = (BarCodeAndAddr)object;

		IDs[0] = DataConvert.int2String(obj.getID());
		((TextField) component[0]).setText(obj.getLongBarCodeBegin());
		((TextField) component[1]).setText(obj.getLongBarCodeEnd());
		((TextField) component[2]).setText(obj.getShortBarCodeBegin());
		((TextField) component[3]).setText(obj.getShortBarCodeEnd());
		((TextField) component[4]).setText(obj.getAddrBegin());
		((TextField) component[5]).setText(obj.getAddrEnd());
	}

	@Override
	public BarCodeAndAddr getData() {
		// ����������ʾ������ת��Ϊ���� ��Ҫ�����������͵�ת��
		BarCodeAndAddr obj = new BarCodeAndAddr();

		obj.setID(DataConvert.String2Int(IDs[0]));
		obj.setLongBarCodeBegin(((TextField)component[0]).getText());
		obj.setLongBarCodeEnd(((TextField)component[1]).getText());
		obj.setShortBarCodeBegin(((TextField)component[2]).getText());
		obj.setShortBarCodeEnd(((TextField)component[3]).getText());
		obj.setAddrBegin(((TextField)component[4]).getText());
		obj.setAddrEnd(((TextField)component[5]).getText());

		return obj;
	}

	@Override
	public UserManager getDataWithID() {
		return null;
	}

}
