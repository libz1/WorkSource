package ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import util.DB;
import util.Util698;

import com.eastsoft.util.DebugSwing;

/**
 * ����ť�ؼ�.
 * <p>
 * ��ʾ�������ܵ����
 *
 * @author xuky
 * @version 2016.08.24
 */
public class MainWindow {

	static String  ver = "0.30";

	public static void main(String[] args) {
		JPanel panel = new MainPanel(ver); // ����չʾ�Ŀؼ�
		int WINDOWWIDTH = 800, WINDOWHEIGHT = 600;
		panel.setBounds(0, 0, WINDOWWIDTH, WINDOWHEIGHT);
		JFrame frame = new JFrame();

		frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	super.windowClosing(e);
	        	DB.getInstance().close();
	        	// xuky 2016.09.02 �ر�ǰɾ������sqlite��������ʱ�ļ�
	        	Util698.deleteFiles(System.getProperty("user.dir"),"etilqs_");
	         }
        });

		// xuky 2016.09.02 ����ǰɾ������sqlite��������ʱ�ļ�
		Util698.deleteFiles(System.getProperty("user.dir"),"etilqs_");

		frame.setLayout(null);
		frame.add(panel);
		frame.setSize(WINDOWWIDTH, WINDOWHEIGHT);
		frame.setTitle("698.45Э��Ӧ�����");
		// ������ʾ����
		DebugSwing.center(frame);
		frame.setVisible(true);

		// �رմ�frameʱ���ر�����Ӧ�ó���
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
