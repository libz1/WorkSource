package ui;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import base.BaseFrame;
import entity.SerialParam;
import mina.MinaSerialServer;
import socket.Channel;
import socket.ChannelList;
import socket.PrefixMain;
import socket.SerialList;
import util.Publisher;

/**
 * 通信设备列表
 *
 * @author xuky
 * @version 2016-08-16
 *
 */
public class DevListWindow extends BaseFrame implements Observer{
	private JTable table_List;
	private DefaultTableModel model_List;
	static String[] colNames = {"地址","设备地址","类型","连接时间","通信时间","心跳周期"};
	private JTextArea txt_frame;
	private JLabel lb_analy;
	public JLabel lb_status;

	private volatile static DevListWindow uniqueInstance;

	public static DevListWindow getInstance() {
		if (uniqueInstance == null) {
			synchronized (DevListWindow.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new DevListWindow();
					// 加入Publisher
					Publisher.getInstance().addObserver(uniqueInstance);
				}
			}
		}
		return uniqueInstance;
	}

	private DevListWindow(){

	}


	private void init_table(){
		table_List = new JTable();

		// 设置列表为单选模式
		table_List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// xuky 2017.03.29 可能因这里的定义，导致后续的
//		Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: 5 >= 3
		Object[][] data_attrs = new Object[0][6];

		model_List = new DefaultTableModel(data_attrs, colNames) {
			public boolean isCellEditable(int row, int column) {
				if (column == 0)
					return true;// 返回true表示能编辑，false表示不能编辑
				else
					return false;// 返回true表示能编辑，false表示不能编辑
			}
		};
		table_List.setModel(model_List);
		table_List.setRowHeight(20);
		refreshTable();

		panel.setLayout(new BorderLayout());


		JPanel jPanel = new JPanel();

		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.LEFT);
		frame.setLayout(fl);
		jPanel.setLayout(fl);


		panel.add(jPanel, BorderLayout.NORTH);

		JButton but_close = new JButton("关闭串口");
		jPanel.add(but_close);

		JButton but_open = new JButton("开启串口");
		jPanel.add(but_open);

		JScrollPane scroll_obj_attr = new JScrollPane(table_List);
		panel.add(scroll_obj_attr,BorderLayout.CENTER);
//		scroll_obj_attr.setBounds(5, 5, 800, 100);

		but_close.addActionListener(e -> {
			close_serial();
		});
		but_open.addActionListener(e -> {
			open_serial();
		});

	}

	private void open_serial() {
		PrefixMain.getInstance().openSerial();
	}

	private void close_serial() {
		PrefixMain.getInstance().closeSerial();

	}

	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		if (s[0].equals("refresh terminal list")){
			refreshTable();
		}
	}


	private synchronized void refreshTable() {
		int num = ChannelList.getInstance().getChannelList().size();
		Object[][] data_obj_attr = new Object[num][6];
		//data_obj_attr = DB.getInstance().getOADList(OPERATETYPE);
		int i = 0;
		String type= "";
    	for (Channel c:ChannelList.getInstance().getChannelList()){
    		data_obj_attr[i][0] = c.getAddr();
    		data_obj_attr[i][1] = c.getLogAddr();
    		type = c.getType();
    		if (type.equals("0"))
    			data_obj_attr[i][2] = "TCP";
    		else if (type.equals("1"))
    			data_obj_attr[i][2] = "UDP";
    		else{
    			data_obj_attr[i][2] = "COM正常";
    			if (c.getStatus() == -1)
        			data_obj_attr[i][2] = "COM" +"不存在";
    			if (c.getStatus() == -2)
        			data_obj_attr[i][2] = "COM" +"被占用";
    		}
    		data_obj_attr[i][3] = c.getConnectTime();
    		data_obj_attr[i][4] = c.getRecvTime();
    		data_obj_attr[i][5] = c.getHeatTime();
    		i++;
    	}
    	try{
//    		System.out.println("refreshTable=> begin");
    		// xuky 2017.03.30 正常执行时会随机的出现错误：Exception in thread "AWT-EventQueue-0"
    		// java.lang.ArrayIndexOutOfBoundsException: 1 >= 0
    		// 参考https://community.oracle.com/thread/1350100?start=0
    		SwingUtilities.invokeLater(() ->
    			model_List.setDataVector(data_obj_attr, colNames));
//    		System.out.println("refreshTable=> end");
    	}
    	catch(Exception e){
    		System.out.println("model_List.setDataVector err=>");
    		e.printStackTrace();
    	}
	}

	@Override
	protected void init() {
		init_table();
	}




	public static void main(String[] args) {

	}



}
