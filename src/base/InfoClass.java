package base;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import entity.Constant;
import util.Util698;


// 用于统一调用
// 明细窗口，多个说明文字、多个录入框 （部分录入框为只读属性，增加按钮进行弹窗选择）
public class InfoClass {

	// 数据转换功能提取为接口
	public TransBehavior trans;
	private JPanel panel;
	protected int ID; // xuky 存储原先的ID信息，在保存数据的时候需要使用

	protected JLabel[] labelArr;
	public JTextField[] textFieldArr;
	protected JTextArea[] textAreaArr;
	public JComponent[] component;

	private JTextField selectData;

	// 每行显示字段数量
	private int COLUMNNUM = 2;

	public InfoClass(String[] ObjColumns){
		init(ObjColumns,0,0);
	}

	public InfoClass(String[] ObjColumns,TransBehavior transBehavior){
		init(ObjColumns,0,0);
		setTrans(transBehavior);
	}

	public InfoClass(String[] ObjColumns, int txtWidth,int inputWidth){
		init(ObjColumns,txtWidth,inputWidth);
	}

	// 用户可以自行设定每行显示的列数，默认为两列，即每行显示两个字段的数据，在某些情况下需要每行只显示一个字段的数据
	public InfoClass(String[] ObjColumns,int colNum){
		// 一行的列数
		COLUMNNUM = colNum;
		init(ObjColumns,0,0);
	}

	public void setTrans(TransBehavior transBehavior) {
		this.trans = transBehavior;
		// 注意如下代码，必须 this.trans
		this.trans.setComponent(component);
	}

	public void setData(Object object) {
		trans.setData(object);
	}

	public void clearComponent() {
		trans.clearComponent();
	}

	public void setBounds(int x,int y,int width,int height){
		panel.setBounds(x, y, width,height);
	}

	// 根据入参（各列字段信息），组织界面显示数据， 文字提示信息+控件（可写文本框、只读文本框+弹窗选择按钮）
	// ObjColumns 的特殊定义
	//colNames_info = { "code", "父code", "顺序号", "数据名称", "数据类型;noButton", "默认值",
	//		"数据定义;TextArea", "备注" }
	//  数据类型;noButton  表示  是数据类型字段，但是不需要选择按钮
	// 数据定义;TextArea 表示 数据定义字段是一个TextArea类型的数据，其控件是特殊类型，其显示代码是特殊代码（做死了，没有灵活处理）

	protected void init(String[] ObjColumns,int txtWidth,int inputWidth) {
		panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setVisible(true);

		labelArr = new JLabel[15];
		textFieldArr = new JTextField[15];
		textAreaArr = new JTextArea[15];
		component = new JComponent[15];

		Font font = new Font("宋体", Font.BOLD, 14);
		for (int i = 0; i < 15; i++) { // 通过一个循环,对按钮数组中的每一个按钮实例化.
			labelArr[i] = new JLabel();
			labelArr[i].setForeground(new Color(0, 114, 198));
			labelArr[i].setFont(font);
			panel.add(labelArr[i]);

			if ((i == 400)) {
				// 传入一个Vector对象
				Vector<String> v = null;
				if (i == 400) {
					v = new Vector<String>();
					v.addElement("1");
					v.addElement("2");
				}
				component[i] = new JComboBox(v);
			} else {
				String colName = "";
				if (i < ObjColumns.length)
					colName = ObjColumns[i];


				if (colName.indexOf("TextArea") >= 0){
					textAreaArr[i] = new JTextArea();
					component[i] = textAreaArr[i];
					textAreaArr[i].setLineWrap(true);// 激活自动换行功能
					textAreaArr[i].setWrapStyleWord(true);// 激活断行不断字功能
				}
				else if (colName.indexOf(";code:") >= 0){
					// 波特率;code:PortRateType
					Vector<String> v = new Vector<String>();
					String name = colName.split(":")[1];
					String[] array = (String[]) Util698.getFieldValueByName(name, new Constant());
					for( String s: array )
						v.addElement(s);
					component[i] = new JComboBox<String>(v);
				}
				else{
					textFieldArr[i] = new JTextField();
					component[i] = textFieldArr[i];
				}

			}
			component[i].setForeground(new Color(0, 114, 198));
			component[i].setBackground(Color.white);
			component[i].setFont(font);
			panel.add(component[i]);
		}


		int manNum = ObjColumns.length;

		String name = "";
		for(int i=0;i<manNum;i++){
			name = ObjColumns[i];
			if (name.indexOf(";") >= 0)
				name = name.split(";")[0];
			labelArr[i].setText(name);
		}

		// xuky
		// 设置字段属性 自动得到界面 文字，录入框 ，其中有编码类型的
		// ok 考虑分为两行进行显示 次序为从左到右 从上到下
		// ok 每个说明文字和显示文字都是固定宽度 都是左对齐
		// 编码类型数据 如何进行设置 编码如何管理？
		// err（暂不支持）如果整个界面是用来显示的，则整个设置为只读状态

		// 循环的次数 总数除2向上取整
		int iNum = manNum / COLUMNNUM;
		if (iNum * COLUMNNUM < manNum) {
			iNum++;
		}
		int HEIGHT = 30; // 文字高度
		int INTERVALX = 10; // 列间间隔
		int INTERVALY = 3; // 行间间隔
		int WEIGHTLABEL = 70; // 说明文字显示宽度
		int WEIGHTTEXT = 220; // 文本框的显示宽度
		int TOPX = 5, TOPY = 5; // 起始坐标
		int x = TOPX, y = TOPY; // 绘制控件的坐标
		int f = 1; // 字段序号


		if (txtWidth != 0)
			WEIGHTLABEL = txtWidth;
		if (inputWidth != 0)
			WEIGHTTEXT = inputWidth;

		if (COLUMNNUM == 3){
			INTERVALX = 5;
			WEIGHTLABEL = 70;
			WEIGHTTEXT = 175;
		}

		for (int i = 1; i <= iNum; i++)// 逐行循环
		{
			// 将X坐标回复到起始位置
			x = TOPX;
			for (int j = 1; j <= COLUMNNUM; j++) {
				// 每行有两个列，逐列循环
				if (f <= manNum) // 循环总次数要小于总的字段数量
				{
					// 设置说明文字的位置
					labelArr[f - 1].setBounds(x, y, WEIGHTLABEL, HEIGHT);
					// 设置文本框的位置

					String colName = "";
					if (f-1 < ObjColumns.length)
						colName = ObjColumns[f-1];


					//if (f == 7){
					if (colName.indexOf("TextArea") >= 0){
						// 第7列比较特殊，是一个多行文本框
						JScrollPane scroll = new JScrollPane(component[f - 1]);
						scroll.setBounds(x + WEIGHTLABEL, y, WEIGHTTEXT,HEIGHT*4);
						panel.add(scroll);
						component[f - 1].setBounds(0, 0, WEIGHTTEXT,HEIGHT*4);
					}
					else
						component[f - 1].setBounds(x + WEIGHTLABEL, y, WEIGHTTEXT,HEIGHT);

					// 循环次数自增
					f++;
				}
				// 每列循环以后，修改X坐标
				x = x + WEIGHTLABEL + WEIGHTTEXT + INTERVALX * 2;
			}
			// 每行循环以后，修改y坐标
			y = y + HEIGHT + INTERVALY;
		}

		textFieldArr[0].requestFocus();
	}

	public void setFocus(){
	}



	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}


}
