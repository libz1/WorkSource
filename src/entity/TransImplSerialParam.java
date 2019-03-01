package entity;

import com.eastsoft.util.DataConvert;

import javafx.base.TransBehavior_javafx;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import util.Util698;

public class TransImplSerialParam extends TransBehavior_javafx{

	//String[] ObjColumns = { "oi","对象名称","接口类","顺序号","状态" };
	private SerialParam obj_old;

	@Override
	public void setData(Object object) {

		if (object == null){
			clearComponent();
			return;
		}
		// 将传入的对象转变为界面中显示的数据
		obj_old = (SerialParam)object;
//		COMM,terminal,baudRate,dataBit,stopBit,parity,receiveTimeout
		IDs[0] = DataConvert.int2String(obj_old.getID());
		((TextField) component[0]).setText(obj_old.getCOMID());
		((TextField) component[1]).setText(obj_old.getCOMM());
		((TextField) component[2]).setText(obj_old.getTerminal());
		((TextField) component[3]).setText(DataConvert.int2String(obj_old.getBaudRate()));
		try{
			((ComboBox) component[4]).setValue(obj_old.getParity());
		}
		catch (Exception e){
			e.printStackTrace();
		}
//		((TextField) component[3]).setText(DataConvert.int2String(obj.getDataBit()));
//		((TextField) component[4]).setText(DataConvert.int2String(obj.getStopBit()));
//		((TextField) component[6]).setText(DataConvert.int2String(obj.getReceiveTimeout()));
	}

	@Override
	public SerialParam getData() {
		// 将界面中显示的数据转变为对象 需要进行数据类型的转换
		SerialParam obj1 = new SerialParam();
//		COMM,terminal,baudRate,dataBit,stopBit,parity,receiveTimeout
		obj1.setID(DataConvert.String2Int(IDs[0]));

		obj1.setCOMID(((TextField)component[0]).getText());
		obj1.setCOMM(((TextField)component[1]).getText());
		obj1.setTerminal(((TextField)component[2]).getText());
		obj1.setBaudRate(DataConvert.String2Int(((TextField)component[3]).getText()));

	    obj1.setParity(((ComboBox) component[4]).getValue().toString());

//		obj.setDataBit(DataConvert.String2Int(((TextField)component[3]).getText()));
//		obj.setStopBit(DataConvert.String2Int(((TextField)component[4]).getText()));
//		obj.setReceiveTimeout(DataConvert.String2Int(((TextField)component[6]).getText()));

		return obj1;
	}

	@Override
	public SerialParam getDataWithID() {
		return null;
	}

}
