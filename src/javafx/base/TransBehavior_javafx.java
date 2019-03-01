package javafx.base;

import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * 动作接口抽象类，通过模板方式封装了部分
 * @author xuky
 *
 * @version 2016.09.23
 *
 */
public abstract class TransBehavior_javafx {

	// 存储用于显示的控件的指针信息
	public Control[] component  = new Control[15];
	// 存储ID信息
	public String[] IDs = {""};

	// 所有实现的setComponent代码是一致的
	public void setComponent(Control[] component) {
		int i = 0;
		for(Control c:component){
			this.component[i] =c;
			i++;
		}
	}

	public void clearComponent() {
		for(Control c:component){
			String class_name = c.getClass().toString();
			if (class_name.indexOf("TextField") >= 0)
				((TextField) c).setText("");
			if (class_name.indexOf("TextArea") >= 0)
				((TextArea) c).setText("");
		}
	}

	public abstract void setData(Object object);

	public abstract Object getData();

	public abstract Object getDataWithID();

}
