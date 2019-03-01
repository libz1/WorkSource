package javafx.base;

import java.awt.Font;

import entity.Constant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import util.Util698;

// 用于统一调用
// 明细窗口，多个说明文字、多个录入框 （部分录入框为只读属性，增加按钮进行弹窗选择）
public class InfoClass_javafx {

	// 数据转换功能提取为接口
	public TransBehavior_javafx trans;
	private AnchorPane panel;
	protected int ID; // xuky 存储原先的ID信息，在保存数据的时候需要使用

	protected Label[] labelArr;
	public TextField[] textFieldArr;
	protected TextArea[] textAreaArr;
	public Control[] component;

	private TextField selectData;

	private int ROWNUM = 0;

	// 每行显示字段数量
	private int COLUMNNUM = 2;

	public InfoClass_javafx(String[] ObjColumns) {
		init(ObjColumns, 0, 0);
	}

	public InfoClass_javafx(String[] ObjColumns, TransBehavior_javafx transBehavior) {
		init(ObjColumns, 0, 0);
		setTrans(transBehavior);
	}

	public InfoClass_javafx(String[] ObjColumns, int txtWidth, int inputWidth) {
		init(ObjColumns, txtWidth, inputWidth);
	}

	// 用户可以自行设定每行显示的列数，默认为两列，即每行显示两个字段的数据，在某些情况下需要每行只显示一个字段的数据
	public InfoClass_javafx(String[] ObjColumns, int colNum) {
		// 一行的列数
		COLUMNNUM = colNum;
		init(ObjColumns, 0, 0);
	}

	public void setTrans(TransBehavior_javafx transBehavior) {
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

	public void setBounds(int x, int y, int width, int height) {
		panel.setMinSize(width, height);
		// panel.setBounds(x, y, width,height);
	}

	// 根据入参（各列字段信息），组织界面显示数据， 文字提示信息+控件（可写文本框、只读文本框+弹窗选择按钮）
	// ObjColumns 的特殊定义
	// colNames_info = { "code", "父code", "顺序号", "数据名称", "数据类型;noButton", "默认值",
	// "数据定义;TextArea", "备注" }
	// 数据类型;noButton 表示 是数据类型字段，但是不需要选择按钮
	// 数据定义;TextArea 表示 数据定义字段是一个TextArea类型的数据，其控件是特殊类型，其显示代码是特殊代码（做死了，没有灵活处理）

	protected void init(String[] ObjColumns, int txtWidth, int inputWidth) {
		panel = new AnchorPane();

		labelArr = new Label[15];
		textFieldArr = new TextField[15];
		textAreaArr = new TextArea[15];
		component = new Control[15];

		Font font = new Font("宋体", Font.BOLD, 14);
		for (int i = 0; i < 15; i++) { // 通过一个循环,对按钮数组中的每一个按钮实例化.
			labelArr[i] = new Label();
			// labelArr[i].setForeground(new Color(0, 114, 198));
			// labelArr[i].setFont(font);
			// panel.add(labelArr[i]);
			panel.getChildren().add(labelArr[i]);
			labelArr[i].setVisible(false);

			if ((i == 400)) {
			} else {
				String colName = "";
				if (i < ObjColumns.length)
					colName = ObjColumns[i];

				if (colName.indexOf("TextArea") >= 0) {
					textAreaArr[i] = new TextArea();
					component[i] = textAreaArr[i];
					// textAreaArr[i].setLineWrap(true);// 激活自动换行功能
					// textAreaArr[i].setWrapStyleWord(true);// 激活断行不断字功能
				} else if (colName.indexOf(";code:") >= 0) {
					// 波特率;code:PortRateType
					// Vector<String> v = new Vector<String>();
					String name = colName.split(":")[1];
					String[] array = (String[]) Util698.getFieldValueByName(name, new Constant());
					ObservableList<String> v = FXCollections.observableArrayList(array);
					component[i] = new ComboBox<String>(v);
				} else {
					textFieldArr[i] = new TextField();
					component[i] = textFieldArr[i];
				}

			}
			// component[i].setForeground(new Color(0, 114, 198));
			// component[i].setBackground(Color.white);
			// component[i].setFont(font);
			// panel.add(component[i]);
			panel.getChildren().add(component[i]);
			component[i].setVisible(false);
		}

		int maxNum = ObjColumns.length;

		String name = "";
		for (int i = 0; i < maxNum; i++) {
			name = ObjColumns[i];
			if (name.indexOf(";") >= 0)
				name = name.split(";")[0];
			if (name.indexOf("-") >= 0)
				name = name.split("-")[0];
			labelArr[i].setText(name);
		}

		// xuky
		// 设置字段属性 自动得到界面 文字，录入框 ，其中有编码类型的
		// ok 考虑分为两行进行显示 次序为从左到右 从上到下
		// ok 每个说明文字和显示文字都是固定宽度 都是左对齐
		// 编码类型数据 如何进行设置 编码如何管理？
		// err（暂不支持）如果整个界面是用来显示的，则整个设置为只读状态

		// 循环的次数 总数除2向上取整
		int iNum = maxNum / COLUMNNUM;
		if (iNum * COLUMNNUM < maxNum) {
			iNum++;
		}
		int HEIGHT = 30; // 文字高度
		int INTERVALX = 5; // 列间间隔
		int INTERVALY = 3; // 行间间隔
		int WEIGHTLABEL = 60; // 说明文字显示宽度
		int WEIGHTTEXT = 210; // 文本框的显示宽度
		int TOPX = 5, TOPY = 5; // 起始坐标
		int x = TOPX, y = TOPY; // 绘制控件的坐标
		int f = 0; // 字段序号

		if (txtWidth != 0)
			WEIGHTLABEL = txtWidth;
		if (inputWidth != 0)
			WEIGHTTEXT = inputWidth;

		if (COLUMNNUM == 3) {
			INTERVALX = 5;
			WEIGHTLABEL = 70;
			WEIGHTTEXT = 175;
		}

		// for (int i = 1; i <= iNum; i++)// 逐行循环
		while (f < maxNum) {
			// 将X坐标回复到起始位置
			x = TOPX;
			for (int j = 1; j <= COLUMNNUM; j++) {
				if (f < maxNum) // 循环总次数要小于总的字段数量
				{

					String colTitle = ObjColumns[f];
//					System.out.println("init colTitle:" + colTitle);

					if (colTitle.indexOf("-单行") >= 0) {

						x = TOPX;
						if (j != 1) {
							y = y + HEIGHT + INTERVALY;
							ROWNUM++;
						}

						labelArr[f].setMinSize(WEIGHTLABEL, HEIGHT);
						labelArr[f].setMaxSize(WEIGHTLABEL, HEIGHT);
						AnchorPane.setLeftAnchor(labelArr[f], (double) x);
						AnchorPane.setTopAnchor(labelArr[f], (double) y);
						labelArr[f].setVisible(true);

						// 设置文本框的位置
						// component[f - 1].setBounds(x + WEIGHTLABEL, y,
						// WEIGHTTEXT,HEIGHT);
						// WEIGHTLABEL + WEIGHTTEXT + INTERVALX
						component[f].setMinSize(
								WEIGHTTEXT * COLUMNNUM + WEIGHTLABEL * (COLUMNNUM - 1) + INTERVALX * (COLUMNNUM + 1),
								HEIGHT);
						component[f].setMaxSize(
								WEIGHTTEXT * COLUMNNUM + WEIGHTLABEL * (COLUMNNUM - 1) + INTERVALX * (COLUMNNUM + 1),
								HEIGHT);
						AnchorPane.setLeftAnchor(component[f], (double) x + WEIGHTLABEL);
						AnchorPane.setTopAnchor(component[f], (double) y);
						component[f].setVisible(true);

						f++;

						break;

					}

					// 每行有COLUMNNUM列，逐列循环
					// 设置说明文字的位置
					// labelArr[f - 1].setBounds(x, y, WEIGHTLABEL, HEIGHT);
					labelArr[f].setMinSize(WEIGHTLABEL, HEIGHT);
					labelArr[f].setMaxSize(WEIGHTLABEL, HEIGHT);
					AnchorPane.setLeftAnchor(labelArr[f], (double) x);
					AnchorPane.setTopAnchor(labelArr[f], (double) y);
					labelArr[f].setVisible(true);

					// 设置文本框的位置
					// component[f - 1].setBounds(x + WEIGHTLABEL, y,
					// WEIGHTTEXT,HEIGHT);
					component[f].setMinSize(WEIGHTTEXT, HEIGHT);
					component[f].setMaxSize(WEIGHTTEXT, HEIGHT);
					AnchorPane.setLeftAnchor(component[f], (double) x + WEIGHTLABEL);
					AnchorPane.setTopAnchor(component[f], (double) y);
					component[f].setVisible(true);

				}
				// 循环次数自增
				f++;
				// 每列循环以后，修改X坐标 移动一列的位置
				x = x + WEIGHTLABEL + WEIGHTTEXT + INTERVALX * 2;
			}
			// 每行循环以后，修改y坐标
			y = y + HEIGHT + INTERVALY;
			ROWNUM++;
		}

		textFieldArr[0].requestFocus();
	}

	public void setFocus() {
	}

	public Pane getPanel() {
		return panel;
	}

	public int getROWNUM() {
		return ROWNUM;
	}

}
