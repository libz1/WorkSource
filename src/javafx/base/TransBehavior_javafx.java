package javafx.base;

import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * �����ӿڳ����࣬ͨ��ģ�巽ʽ��װ�˲���
 * @author xuky
 *
 * @version 2016.09.23
 *
 */
public abstract class TransBehavior_javafx {

	// �洢������ʾ�Ŀؼ���ָ����Ϣ
	public Control[] component  = new Control[15];
	// �洢ID��Ϣ
	public String[] IDs = {""};

	// ����ʵ�ֵ�setComponent������һ�µ�
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
