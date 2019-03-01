package produce.entity;

import com.eastsoft.util.DataConvert;

import javafx.base.TransBehavior_javafx;
import javafx.scene.control.TextField;

public class TransImplProduceCase extends TransBehavior_javafx{

	//String[] ObjColumns = { "oi","对象名称","接口类","顺序号","状态" };

	ProduceCase obj;
	@Override
	public void setData(Object object) {

		if (object == null){
			clearComponent();
			return;
		}
		// 将传入的对象转变为界面中显示的数据
		 obj = (ProduceCase)object;

//		 subid,name,send,expect,delaytime,waittime,protocol,retrys,note,caseno

		IDs[0] = DataConvert.int2String(obj.getID());
		((TextField) component[0]).setText(obj.getSubid());
		((TextField) component[1]).setText(obj.getName());
		((TextField) component[2]).setText(obj.getSend());
		((TextField) component[3]).setText(obj.getExpect());
		((TextField) component[4]).setText(DataConvert.int2String(obj.getDelaytime()));
		((TextField) component[5]).setText(DataConvert.int2String(obj.getWaittime()));
		((TextField) component[6]).setText(obj.getProtocol());
		((TextField) component[7]).setText(DataConvert.int2String(obj.getRetrys()));
		((TextField) component[8]).setText(obj.getNote());
		((TextField) component[9]).setText(DataConvert.int2String(obj.getCaseno()));
	}

	@Override
	public ProduceCase getData() {
		// 将界面中显示的数据转变为对象 需要进行数据类型的转换
		ProduceCase obj = new ProduceCase();

		obj.setID(DataConvert.String2Int(IDs[0]));

		obj.setSubid(((TextField)component[0]).getText());
		obj.setName(((TextField)component[1]).getText());
		obj.setSend(((TextField)component[2]).getText());
		obj.setExpect(((TextField)component[3]).getText());
		obj.setDelaytime(DataConvert.String2Int(((TextField)component[4]).getText()));
		obj.setWaittime(DataConvert.String2Int(((TextField)component[5]).getText()));
		obj.setProtocol(((TextField)component[6]).getText());
		obj.setRetrys(DataConvert.String2Int(((TextField)component[7]).getText()));
		obj.setNote(((TextField)component[8]).getText());
		obj.setCaseno(DataConvert.String2Int(((TextField)component[9]).getText()));

		return obj;
	}

	@Override
	public ProduceCase getDataWithID() {
		return null;
	}


}
