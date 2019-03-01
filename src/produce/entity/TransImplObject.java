package produce.entity;

import java.util.List;
import java.util.Map;

import com.eastsoft.util.DataConvert;

import javafx.base.TransBehavior_javafx;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import util.Util698;

public class TransImplObject extends TransBehavior_javafx{


	private String detail_export_columns = "";
	Object newObject = null;
	public TransImplObject(String columns,Object newObj){
		this.detail_export_columns = columns;
		this.newObject = newObj;
	}

//	ProduceCase obj;
	@Override
	public void setData(Object object) {

		if (object == null){
			clearComponent();
			return;
		}
		// ������Ķ���ת��Ϊ��������ʾ������
//		 obj = (ProduceCase)object;

//		 subid,name,send,expect,delaytime,waittime,protocol,retrys,note,caseno

		IDs[0] = DataConvert.int2String((int)Util698.getFieldValueTypeByName("ID",object)[0]);

//		List<Map> infoList = Util698.getFiledsInfo(object);
		int i = 0;
//	   	for( Map<String, Object> info:infoList ){
	   	for( String name: detail_export_columns.split(",") ){
//			String name = info.get("name").toString();
			Object[] obs = Util698.getFieldValueTypeByName(name,object);
			if (obs[0] == null) {
				// xuky 2017.08.02 �������Ϊ�գ���Ҫ����д��""����
				((TextField) component[i]).setText("");
				i++;
				continue;
			}
			if (((String)obs[1]).indexOf("String") >= 0)
				((TextField) component[i]).setText((String) obs[0]);
			else
				((TextField) component[i]).setText(DataConvert.int2String((int) obs[0]));
			i++;
	   	}
	}

	@Override
	public Object getData() {
		// ����������ʾ������ת��Ϊ���� ��Ҫ�����������͵�ת��

		// xuky 2017.07.05 ��Ҫ�����µĶ���ʵ������������ObservableList<T> data_objs�����쳣�����
		// �ο�http://www.cnblogs.com/zhangnanblog/archive/2012/04/27/2473820.html
		try {
			newObject = Class.forName(newObject.getClass().getName()).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Util698.setFieldValueByName("ID",newObject,DataConvert.String2Int(IDs[0]));

//		List<Map> infoList = Util698.getFiledsInfo(newObject);
		int i = 0;

	   	for( String name: detail_export_columns.split(",") ){
			String type = Util698.getFieldTypeByName(name,newObject);
			if (type.indexOf("String") >= 0)
				Util698.setFieldValueByName(name,newObject,((TextField)component[i]).getText());
			else
				Util698.setFieldValueByName(name,newObject,DataConvert.String2Int(((TextField)component[i]).getText()));
			i++;
	   	}

		return newObject;
	}

	@Override
	public Object getDataWithID() {
		return null;
	}


}
