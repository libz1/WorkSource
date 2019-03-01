package produce.entity;

import com.eastsoft.util.DataConvert;

import javafx.base.TransBehavior_javafx;
import javafx.scene.control.TextField;
import util.Util698;

/**
 * 电表数据转换对象
 * @author xuky
 * @version 2016.10.13
 *
 */
public class TransImplUserManagerOnlyPwd extends TransBehavior_javafx{

	//String[] ObjColumns = { "oi","对象名称","接口类","顺序号","状态" };

	// xuky 2017.06.20  可能用户只是修改部分数据，例如仅仅修改密码信息
	private UserManager obj_old;

	@Override
	public void setData(Object object) {

		if (object == null){
			clearComponent();
			return;
		}
		// 将传入的对象转变为界面中显示的数据
		obj_old = (UserManager)object;

		IDs[0] = DataConvert.int2String(obj_old.getID());
		((TextField) component[0]).setText(obj_old.getUserpwd());
	}

	@Override
	public UserManager getData() {
		// 将界面中显示的数据转变为对象 需要进行数据类型的转换

		// xuky 2017.06.20 非常奇怪 ，如果直接使用 obj_old进行数据修改，数据返回，就会出现修改完成后明细数据不刷新的状况
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
