package base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.eastsoft.util.Debug;
import com.eastsoft.util.DebugSwing;

import util.Util698;


//用于统一调用
public abstract class BaseFrame {
	public String title = "";
	public JFrame frame;
	public JPanel panel;
	protected String OPERATETYPE = "";

	public BaseFrame(){
		init0();
	}

	public void setCursor(int cursorType){
		frame.setCursor(cursorType);
	}

	public BaseFrame(String type){
		OPERATETYPE = type;
		init0();
	}

	// 最先的初始化程序
	private void init0() {

		// 设置panel为可以随窗口大小调整
		frame = new JFrame();

		Container c = frame.getContentPane();

		c.setLayout(new BorderLayout());

		panel = new JPanel(null);
		panel.setBackground(Color.white);
		panel.setVisible(true);

		// BorderLayout.CENTER是随着窗口大小而变化的
		// 整个界面都随窗口大小调整
		c.add(panel, BorderLayout.CENTER);

		// xuky 2016.12.21 设置边距信息
		panel.setBorder(new EmptyBorder(3, 3, 3, 3));

//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 在后面的showFrame函数中，将会实现窗口的相关设置

		// 模板模式开发：在这里编写算法执行过程，其中init是抽象函数，需要在子类中具体实现，initComponent是公共内容的父类实现的具体函数
		try{
			init();
			initComponent(panel);
		}
		catch(Exception e){
			Util698.log(BaseFrame.class.getName(), "init0 Exception:"+e.getMessage(),Debug.LOG_INFO);
		}
	}


	// 1、抽象函数，需要在子类中具体实现，对子类的各种具体变量赋值等
	protected abstract void init();


	// 2、递归方式将各个容器中的控件进行属性设置
	protected void initComponent(JComponent jComponent) {
		Component[] c = jComponent.getComponents();
		String class_name = "";
		for (Component co : c) {
			class_name = co.getClass().toString();
			//System.out.println("class_name=>"+class_name);
			if (class_name.indexOf("JButton") >= 0) {
				//按钮为蓝底白字
				co.setBackground(new Color(0, 114, 198));
//				co.setBackground(new Color(0, 255, 255));
				co.setForeground(Color.white);
			}
			if (class_name.indexOf("JLabel") >= 0
					|| class_name.indexOf("JTextField") >= 0
					|| class_name.indexOf("JComboBox") >= 0
					|| class_name.indexOf("JCheckBox") >= 0
					|| class_name.indexOf("JTable") >= 0
					|| class_name.indexOf("JTextArea") >= 0) {
				//蓝字 白底
				co.setForeground(new Color(0, 114, 198));
				// xuky 2016.12.27
				if (class_name.indexOf("JTextField") >= 0 && ((JTextField)co).isEditable() ){
					// 不可编辑时，应该是默认的灰色
				}
				else
					co.setBackground(Color.white);
			}
			if (class_name.indexOf("JTable") >= 0) {
				// 设置列表的表头部分字体颜色
				((JTable)co).getTableHeader().setForeground(new Color(0, 114, 198));
			}

			if (class_name.indexOf("JScrollPane") >= 0
					|| class_name.indexOf("JPanel") >= 0
					|| class_name.indexOf("JTable") >= 0
					|| class_name.indexOf("JViewport") >= 0
					|| class_name.indexOf("JSplitPane") >= 0) {
				// 设置背景为白色
				co.setBackground(Color.white);
				// 递归方式对其子控件进行设置
				initComponent((JComponent) co);
			}
			//System.out.println(class_name);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

	public void showFrame(String title) {
		showFrame(title,0,0,0,0);
	}

	public void showFrame(String title,int x,int y,int input_width,int input_height) {
		this.title = title;
		int width = 800, height = 600;
		if (frame != null)
			frame.dispose();

//		// 设置界面所在Panel的位置等信息
//		getPanel().setBounds(0, 0, width, height);

		frame.setTitle(title);
//		frame.setLayout(null);
//		frame.add(getPanel());

		// xuky 子窗口关闭的时候，不关闭主窗口，而且退出后不会出现javaw残余
		// xuky 2017.04.12 添加如下代码
		// 添加 AssitantDataMgr中的frame.addWindowListener(new WindowListener() 。。。 uniqueInstance = null;
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// xuky 2017.04.12 参考http://blog.csdn.net/zhenshiyiqie/article/details/8440806
		// 当我们关闭窗口时，程序结束运行
		// 不能使用如下的参数会导致整个程序退出
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		if (x == 0){
			frame.setSize(width, height);
			DebugSwing.center(frame);
		}
		else{
//			frame.setSize(input_width, input_height);
			frame.setBounds(x, y, input_width, input_height);
		}
//		frame.pack();
		frame.setVisible(true);

		// 2017.02.13 使用了javafx，则需要通过setAlwaysOnTop(true)来确保打开窗口的正常
			//  先开启，后关闭的方式，确保新开的窗口显示在最前面，又不会总是在前面
			frame.setAlwaysOnTop(true);
			frame.setAlwaysOnTop(false);

		after_show();
	}

	// 在开启界面后需要执行的代码
	protected void after_show(){
		// 例如FrameAnaly 的splitPane.setDividerLocation(0.7)代码
		// 必须在frame.setVisible(true);之后执行才能有效
	};

}
