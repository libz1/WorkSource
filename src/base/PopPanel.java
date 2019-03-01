package base;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;


public class PopPanel extends JPanel{

	public PopPanel(InfoClass info_pop,TransBehavior transBehavior, Object object, ActionListener actionOk,ActionListener actionCancle){
		this.setLayout(null);
		this.setBounds(0, 0, 760, 500);

		info_pop.setTrans(transBehavior);
		this.add(info_pop.getPanel());
		info_pop.getPanel().setBounds(5, 5, 760, 500);

		info_pop.setData(object);

		// ���������� ��Ҫ��ȷ�Ϻ�ȡ����ť
		JButton buttonOk = new JButton("ȷ��");
		buttonOk.setBackground(new Color(0, 114, 198));
		buttonOk.setForeground(Color.white);
		buttonOk.setBounds(250+100, 240, 100, 30);
		this.add(buttonOk);

		JButton buttonCancle = new JButton("ȡ��");
		buttonCancle.setBackground(new Color(0, 114, 198));
		buttonCancle.setForeground(Color.white);
		buttonCancle.setBounds(355+100, 240, 100, 30);
		this.add(buttonCancle);

		buttonOk.addActionListener(actionOk);
		buttonCancle.addActionListener(actionCancle);


	}
}
