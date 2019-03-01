package ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.eastsoft.util.Debug;

import base.BaseFrame;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import socket.PrefixMain;
import util.SoftParameter;

/**
 * 通信服务器
 *
 * @author xuky
 * @version 2016-08-16
 *
 */

public class PrefixWindow extends BaseFrame implements Observer{

    private JTabbedPane jTabbedpane;// 存放选项卡的组件
    private static String[] tabNames = { "通信链路", "收发报文" };
	private volatile static PrefixWindow uniqueInstance;

	public static PrefixWindow getInstance() {
		if (uniqueInstance == null) {
			synchronized (PrefixWindow.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new PrefixWindow();
				}
			}
		}
		return uniqueInstance;
	}

    private PrefixWindow() {
    }

	@Override
	protected void init() {
        layoutComponents();
        // 启动前置机示例
        PrefixMain.getInstance();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Debug.sleep(500);
				String msg = "";
				if (PrefixMain.getInstance().getServer() != null)
					msg = "前置机开启=> 监听信息"+PrefixMain.getInstance().getServer().getLocalSocketAddress().toString();
				else
					msg = "前置机开启=> 端口被占用，未成功开启";
		        LogWindow.getInstance().lb_status.setText(msg);
			}
		});

	}


    private void layoutComponents() {
        int i = 0;
        // 第一个标签下的JPanel
        jTabbedpane = new JTabbedPane();// 存放选项卡的组件
        JPanel jpanelFirst = new JPanel(new BorderLayout()); // 无layout
        jTabbedpane.addTab(tabNames[i++], null, jpanelFirst, tabNames[0]);// 加入第一个页面
        BaseFrame mainFrame = DevListWindow.getInstance();
		jpanelFirst.add(mainFrame.getPanel(),BorderLayout.CENTER);
//		mainFrame.getPanel().setBounds(0, 0, 800, 150);

        // 第二个标签下的JPanel
        JPanel jpanelSecond = new JPanel(new BorderLayout()); // 无layout
        jTabbedpane.addTab(tabNames[i++], null, jpanelSecond, tabNames[1]);// 加入第一个页面

        mainFrame = LogWindow.getInstance();
		jpanelSecond.add(mainFrame.getPanel(),BorderLayout.CENTER);
//		mainFrame.getPanel().setBounds(0, 0, 800, 150);

        panel.setLayout(new GridLayout(1, 1));
        panel.add(jTabbedpane);

    }

	public static void main(String[] args) {
	}


	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}



}
