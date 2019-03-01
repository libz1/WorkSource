package ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import util.DB;
import util.Util698;

import com.eastsoft.util.DebugSwing;

/**
 * 程序按钮控件.
 * <p>
 * 显示其他功能的入口
 *
 * @author xuky
 * @version 2016.08.24
 */
public class MainWindow {

	static String  ver = "0.30";

	public static void main(String[] args) {
		JPanel panel = new MainPanel(ver); // 对外展示的控件
		int WINDOWWIDTH = 800, WINDOWHEIGHT = 600;
		panel.setBounds(0, 0, WINDOWWIDTH, WINDOWHEIGHT);
		JFrame frame = new JFrame();

		frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	super.windowClosing(e);
	        	DB.getInstance().close();
	        	// xuky 2016.09.02 关闭前删除运行sqlite产生的临时文件
	        	Util698.deleteFiles(System.getProperty("user.dir"),"etilqs_");
	         }
        });

		// xuky 2016.09.02 启动前删除运行sqlite产生的临时文件
		Util698.deleteFiles(System.getProperty("user.dir"),"etilqs_");

		frame.setLayout(null);
		frame.add(panel);
		frame.setSize(WINDOWWIDTH, WINDOWHEIGHT);
		frame.setTitle("698.45协议应用软件");
		// 居中显示窗口
		DebugSwing.center(frame);
		frame.setVisible(true);

		// 关闭此frame时，关闭整个应用程序
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
