package produce.control.simulation;

import java.util.concurrent.ConcurrentHashMap;

import produce.control.comm.CommParam;
import produce.control.comm.RJ45Param;
import util.Frame645Control;
import util.Util698;

//ģ�⼯����
public class SLTerminal {
	private volatile static SLTerminal uniqueInstance;
	// ��λ��ң��״̬���ձ�
	private ConcurrentHashMap<String, String> meterFS = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> meterDC = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> meterAC = new ConcurrentHashMap<>();
	public static SLTerminal getInstance() {
		if (uniqueInstance == null) {
			synchronized (SLTerminal.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new SLTerminal();
				}
			}
		}
		return uniqueInstance;
	}


	public ConcurrentHashMap<String, String> getMeterFS() {
		return meterFS;
	}
	public ConcurrentHashMap<String, String> getMeterDC() {
		return meterDC;
	}
	public ConcurrentHashMap<String, String> getMeterAC() {
		return meterAC;
	}


	// �������ڣ��������ݻظ�
	private SLTerminal() {
		CommParam commParam = new CommParam();
		// ����32��
		for( int i=1;i<=32;i++){
			RJ45Param rJ45Param = new RJ45Param("192.168.127.120", 10000+i);
			commParam.setType("2");
			commParam.setRJ45Param(rJ45Param);
			CommServer tcpServer = new CommServer(commParam);
		}
	}

	// �����յ��ı��ģ��õ���Ҫ�ظ��ı���
	public String getReply(String recv, String addr_str){
		// ͨ��addr_str�жϱ�λ��Ϣ��������ز���
		String ret ="",data = "";
		Frame645Control frame645 = new Frame645Control(recv);
		if (frame645.getControl().equals("14")){
			// ����������Ϊ94
			frame645.setControl("94");
			String dataitem = frame645.getData_item();
			if (dataitem.equals("04969603")){
				// ��Ҫ��ǰ��ģ���豸�������ݻظ�����
				data = meterFS.get(addr_str);
				frame645.setData_data(data);
			}
			else if (dataitem.equals("04969605")){
				String time = Util698.getDateTimeSSS_new();
				time = time.substring(0,time.length()-3);
				time = time.substring(2);
				time = time.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				frame645.setData_data(time);
			}
			else if (dataitem.equals("04969607")){
				data = meterDC.get(addr_str);
				frame645.setData_data(data);
			}
			else if (dataitem.equals("04969608")){
				data = meterAC.get(addr_str);
				frame645.setData_data(data);
			}
			else if (dataitem.equals("0496960A") || dataitem.equals("0496960B") || dataitem.equals("0496960C")||dataitem.equals("0496960D") ){
				frame645.setData_data("00");
			}
			else{
				// Ĭ�ϻظ�ȷ�ϱ���
				frame645.setData_data("00");
			}
		}
		ret = frame645.get645Frame();
		return ret;
	}

	public static void main(String[] args) {
	}

}
