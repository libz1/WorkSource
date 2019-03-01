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

// ����ͳһ����
// ��ϸ���ڣ����˵�����֡����¼��� ������¼���Ϊֻ�����ԣ����Ӱ�ť���е���ѡ��
public class InfoClass_javafx {

	// ����ת��������ȡΪ�ӿ�
	public TransBehavior_javafx trans;
	private AnchorPane panel;
	protected int ID; // xuky �洢ԭ�ȵ�ID��Ϣ���ڱ������ݵ�ʱ����Ҫʹ��

	protected Label[] labelArr;
	public TextField[] textFieldArr;
	protected TextArea[] textAreaArr;
	public Control[] component;

	private TextField selectData;

	private int ROWNUM = 0;

	// ÿ����ʾ�ֶ�����
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

	// �û����������趨ÿ����ʾ��������Ĭ��Ϊ���У���ÿ����ʾ�����ֶε����ݣ���ĳЩ�������Ҫÿ��ֻ��ʾһ���ֶε�����
	public InfoClass_javafx(String[] ObjColumns, int colNum) {
		// һ�е�����
		COLUMNNUM = colNum;
		init(ObjColumns, 0, 0);
	}

	public void setTrans(TransBehavior_javafx transBehavior) {
		this.trans = transBehavior;
		// ע�����´��룬���� this.trans
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

	// ������Σ������ֶ���Ϣ������֯������ʾ���ݣ� ������ʾ��Ϣ+�ؼ�����д�ı���ֻ���ı���+����ѡ��ť��
	// ObjColumns �����ⶨ��
	// colNames_info = { "code", "��code", "˳���", "��������", "��������;noButton", "Ĭ��ֵ",
	// "���ݶ���;TextArea", "��ע" }
	// ��������;noButton ��ʾ �����������ֶΣ����ǲ���Ҫѡ��ť
	// ���ݶ���;TextArea ��ʾ ���ݶ����ֶ���һ��TextArea���͵����ݣ���ؼ����������ͣ�����ʾ������������루�����ˣ�û������

	protected void init(String[] ObjColumns, int txtWidth, int inputWidth) {
		panel = new AnchorPane();

		labelArr = new Label[15];
		textFieldArr = new TextField[15];
		textAreaArr = new TextArea[15];
		component = new Control[15];

		Font font = new Font("����", Font.BOLD, 14);
		for (int i = 0; i < 15; i++) { // ͨ��һ��ѭ��,�԰�ť�����е�ÿһ����ťʵ����.
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
					// textAreaArr[i].setLineWrap(true);// �����Զ����й���
					// textAreaArr[i].setWrapStyleWord(true);// ������в����ֹ���
				} else if (colName.indexOf(";code:") >= 0) {
					// ������;code:PortRateType
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
		// �����ֶ����� �Զ��õ����� ���֣�¼��� �������б������͵�
		// ok ���Ƿ�Ϊ���н�����ʾ ����Ϊ������ ���ϵ���
		// ok ÿ��˵�����ֺ���ʾ���ֶ��ǹ̶���� ���������
		// ������������ ��ν������� ������ι���
		// err���ݲ�֧�֣��������������������ʾ�ģ�����������Ϊֻ��״̬

		// ѭ���Ĵ��� ������2����ȡ��
		int iNum = maxNum / COLUMNNUM;
		if (iNum * COLUMNNUM < maxNum) {
			iNum++;
		}
		int HEIGHT = 30; // ���ָ߶�
		int INTERVALX = 5; // �м���
		int INTERVALY = 3; // �м���
		int WEIGHTLABEL = 60; // ˵��������ʾ���
		int WEIGHTTEXT = 210; // �ı������ʾ���
		int TOPX = 5, TOPY = 5; // ��ʼ����
		int x = TOPX, y = TOPY; // ���ƿؼ�������
		int f = 0; // �ֶ����

		if (txtWidth != 0)
			WEIGHTLABEL = txtWidth;
		if (inputWidth != 0)
			WEIGHTTEXT = inputWidth;

		if (COLUMNNUM == 3) {
			INTERVALX = 5;
			WEIGHTLABEL = 70;
			WEIGHTTEXT = 175;
		}

		// for (int i = 1; i <= iNum; i++)// ����ѭ��
		while (f < maxNum) {
			// ��X����ظ�����ʼλ��
			x = TOPX;
			for (int j = 1; j <= COLUMNNUM; j++) {
				if (f < maxNum) // ѭ���ܴ���ҪС���ܵ��ֶ�����
				{

					String colTitle = ObjColumns[f];
//					System.out.println("init colTitle:" + colTitle);

					if (colTitle.indexOf("-����") >= 0) {

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

						// �����ı����λ��
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

					// ÿ����COLUMNNUM�У�����ѭ��
					// ����˵�����ֵ�λ��
					// labelArr[f - 1].setBounds(x, y, WEIGHTLABEL, HEIGHT);
					labelArr[f].setMinSize(WEIGHTLABEL, HEIGHT);
					labelArr[f].setMaxSize(WEIGHTLABEL, HEIGHT);
					AnchorPane.setLeftAnchor(labelArr[f], (double) x);
					AnchorPane.setTopAnchor(labelArr[f], (double) y);
					labelArr[f].setVisible(true);

					// �����ı����λ��
					// component[f - 1].setBounds(x + WEIGHTLABEL, y,
					// WEIGHTTEXT,HEIGHT);
					component[f].setMinSize(WEIGHTTEXT, HEIGHT);
					component[f].setMaxSize(WEIGHTTEXT, HEIGHT);
					AnchorPane.setLeftAnchor(component[f], (double) x + WEIGHTLABEL);
					AnchorPane.setTopAnchor(component[f], (double) y);
					component[f].setVisible(true);

				}
				// ѭ����������
				f++;
				// ÿ��ѭ���Ժ��޸�X���� �ƶ�һ�е�λ��
				x = x + WEIGHTLABEL + WEIGHTTEXT + INTERVALX * 2;
			}
			// ÿ��ѭ���Ժ��޸�y����
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
