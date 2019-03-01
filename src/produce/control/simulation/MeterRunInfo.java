package produce.control.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.eastsoft.util.Debug;

import entity.SerialParam;
import produce.control.comm.CommWithRecv;
import produce.control.comm.FramePlatform;
import produce.control.entity.BaseCommLog;
import produce.entity.ProduceCase;
import util.Frame645Control;
import util.SoftVarSingleton;
import util.Util698;

public class MeterRunInfo {
	private String Meterno;
	private String Endflag = "0";
	private String result = ""; // ���Խ��

	private List<ProduceCase> produceCases = new ArrayList<ProduceCase>();
	private int currStep = -1; // -1��ʾ��δ��ʼִ��
	private int numOfALL = 0;
	private Boolean continue_run = true;
	private String beginTime = ""; // ��ʼʱ��
	private String endTime = ""; // ����ʱ��
	private int usingTime = 0; // ���Ժ�ʱ
	private Map<String, SerialParam> SerialMap;
	private String TerminalID_MAC = "";

	public MeterRunInfo(String Meterno, List<ProduceCase> produceCases, Map<String, SerialParam> serialMap){
		Meterno = "00"+Meterno;
		Meterno = Meterno.substring(Meterno.length()-2,Meterno.length());
		this.Meterno = Meterno;
		this.produceCases = produceCases;
		this.SerialMap = serialMap;
		numOfALL = produceCases.size();
		endTime = "";
		dealTerminalIP();
	}

	public MeterRunInfo() {
		Endflag = "0";
	}

	private void dealTerminalIP(){
		// ��Դ - �����λ���е�ѹ��·�ĶϿ�����  - �Ե�һ��λ���е�ѹ��·�Ľ��봦��
		// ������Ҫ���м������Լ�������
		// ������������IP��ַ��MAC��ַ��MAC��ַ��Ϊ�Լ�������Ψһ��ʾ���й���
		// �Ƚ��鷳�����飬�������������Ҫ�������հ漯��������ĸ��£����޷�һ���Ե���ɣ���ΪĿǰ��dn����ֻ��֧��Ψһ��IP����
//		15����ѹ��·
//		����:01H+��ַ(A��Z) +����+AAH(����) +���(H-30H,A-31H,B-32H,C-33H)+(30H/�Ͽ�  31H/����)+У��λ+����(17H)
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��) +У��λ+����(17H)
		// ��Ҫ̨�������֤
		// 1����һ��λ�����Ƿ���Ч��H-30H,A-31H,B-32H,C-33H����H��Ȼ����A\B\C
		// 2���㲥��ַFFH�Ƿ���Ч
		// 3������λ  �ӵ��Ƿ���Ч  ��H��Ȼ����A\B\C

		// xuky 2019.02.13 ��ȡΨһ��MAC��ַ
		Object[] ret =  PlatFormUtil.getMAC("201902", 1, "01");
		TerminalID_MAC = (String)ret[1];

	}

	private void dealData(ProduceCase produceCase){

		String caseName = produceCase.getName();
		String caseID = Meterno + "-"+produceCase.getCaseno();

        Util698.log(MeterRunInfo.class.getName(), "ID��"+caseID +" ���Կ�ʼ"+caseName, Debug.LOG_INFO);
		// �ɽ���ͨ�ŵ��ŵ� PS2\485-1��TCP-1��
		// 485-1	129.1.22.201:10001-10032
		// PS2		129.1.22.202:10001-10032

		// �����ŵ�	129.1.22.205:10001-10032��TCP-2��

		// RJ45  ����IP�����  ʲôʱ����и��³��� ���³���ʱ���в��Լ��ɣ�
		// 129.1.22.96 -> 129.1.22.1-129.1.22.32:7000
		// 485-2	COM01-COM32  У����  У��ʱ���Լ���
        String protocol = produceCase.getProtocol();
    	// xuky 2019.02.14 ̨�����Э��
        if (protocol.equals("platform")){
    		String port = produceCase.getNote();
    		port = port.substring(port.indexOf("=")+1);
    		SerialParam serialParam = SerialMap.get(port);
    		String tempData = produceCase.getSend();
    		// ��֯��Ҫ���͵�����
    		FramePlatform framePlatform = new FramePlatform();
    		framePlatform.setADDR(Meterno);
    		framePlatform.setCONTROL(tempData.substring(31,33));
    		String len = tempData.substring(28,30);
    		if (len.equals("06"))
    			framePlatform.setDATA("");
    		else if (len.equals("07"))
    			framePlatform.setDATA(tempData.substring(34,36));
    		else if (len.equals("08"))
    			framePlatform.setDATA(tempData.substring(34,39));
    		else if (len.equals("09"))
    			framePlatform.setDATA(tempData.substring(34,42));
    		String sData = framePlatform.getFrame();
    		// ��֯��Ҫ���յ�����
    		tempData = produceCase.getExpect();
    		framePlatform.setCONTROL(tempData.substring(31,33));
    		len = tempData.substring(28,30);
    		if (len.equals("06"))
    			framePlatform.setDATA("");
    		else if (len.equals("07"))
    			framePlatform.setDATA(tempData.substring(34,36));
    		else if (len.equals("08"))
    			framePlatform.setDATA(tempData.substring(34,39));
    		else if (len.equals("09"))
    			framePlatform.setDATA(tempData.substring(34,42));
    		String expect = framePlatform.getFrame();
    		// ��ʵ���豸����ͨ��
    		CommWithRecv commWithRecv = new CommWithRecv();
//    		String result = commWithRecv.deal_one("TCP", "192.168.127.120:100"+Meterno, sData, expect);
//    		String tmp = serialParam.getCOMM()+":"+serialParam.getBaudRate();
//    		tmp = commWithRecv.deal_one("COM", tmp, sData, expect);
        }
    	// xuky 2019.02.14 645Э��
        if (protocol.equals("") || protocol.indexOf("645")>=0){
//        	String chanel = "192.168.127.120:100"+Meterno;
        	String chanel = "129.1.22.202:100"+Meterno;
        	// ��ַ��ΪIP��ַ��Ϣ��������Ҫ���ж�̬����
        	// ��ʱʹ��Ĭ�ϵĵ�ַ��Ϣ
    		String tempData = produceCase.getSend();
    		Frame645Control frame645 = new Frame645Control(tempData);
    		frame645.setAddr(Util698.StrIP2HEX("129.1.22.96"));
    		String sData = frame645.get645Frame();
    		String expect = produceCase.getExpect();
    		CommWithRecv commWithRecv = new CommWithRecv();
    		BaseCommLog tmp = commWithRecv.deal_one("TCP", chanel, sData, expect);
        }

        // ȱ�ٹ��̼�¼�����
        // ���Կ�ʼִ��ʱ������¼��������Ϣ����ϸ��Ϣ   �������еĲ��Լ�¼
        //

		// ģ����к���ͨ�ŵȵĽ�����ʱ����
//		int sleep = (int)(Math.random()*1000);
//		if (caseName.indexOf("����") >= 0)
//			sleep = sleep * 3;
//		caseID += " sleep="+sleep;
//		Debug.sleep(sleep);
        Util698.log(MeterRunInfo.class.getName(), "ID��"+caseID +" ���Խ���"+caseName, Debug.LOG_INFO);
	};

	// �Ѿ���ǰ�趨��̨��Ĺ���ģʽ������������
	public void run() {
		beginTime = Util698.getDateTimeSSS_new();
		// ����produceCases����ִ�о���Ĳ��Թ���
		while( continue_run ){
			currStep ++;
			// �ж��Ƿ�ִ�е�����ĩ�Ĳ�������
			if (currStep == numOfALL){
	            Util698.log(MeterRunInfo.class.getName(), "��λ��"+ Meterno+"�������", Debug.LOG_INFO);
	    		endTime = Util698.getDateTimeSSS_new();
	    		usingTime = Util698.getMilliSecondBetween_new(endTime, beginTime).intValue();
				break;
			}
			ProduceCase produceCase = produceCases.get(currStep);
			String caseName = produceCase.getName();
//			String caseID = Meterno+"-"+produceCase.getCaseno();

			String protocol = produceCase.getProtocol();
            Util698.log(MeterRunInfo.class.getName(), "��λ��"+ Meterno  +" ���Խ��ȣ�"+(currStep+1)+"/"+numOfALL+" ��������:"+caseName, Debug.LOG_INFO);

            Lock test_Lock = null;
			if (caseName.indexOf("����") >= 0)
				test_Lock = SoftVarSingleton.getInstance().getInfraTest_Lock();
			if (caseName.indexOf("·��") >= 0 || caseName.indexOf("RT") >= 0)
				test_Lock = SoftVarSingleton.getInstance().getRTTest_Lock();
			if (protocol.indexOf("platform") >= 0)
				test_Lock = SoftVarSingleton.getInstance().getPlatformTest_Lock();


			if (test_Lock != null){
				test_Lock.lock();
		        try {
		            dealData(produceCase);
		        } finally {
		        	test_Lock.unlock();
		        }
			}
			else{
	            dealData(produceCase);
			}

		}
		// 1������ֻ�ܷ��͵����Ͷ����У�ʲôʱ��õ�����ǲ�ȷ���ģ��õ���Ҳ��������Ч�Ľ����������Ҫ���·���
		// �������ݣ��������ݣ�����Ӧ���ݣ�����Ҫ�б�ʾ��������Դ

		// QT �γ�5-23:59
		// 2����ͣ�ļ�������������Ϣ���ж����е�������
		// ���������룬���͵���Ӧ�Ĵ�������У�������һ�������ݴ������
		// �ֹ�Э��������һ����������ֻ����������ݺͻ������жϣ��յ����ݺ�����жϱ�ʾ������ת�浽������
		// IP:port ��λ��Ϣ
		// COM:Rate ��λ��Ϣ
		// �����������ʾ�������  M V C  M(ģ�͡�����ģ��)��V(��ͼ��ֱ�ӽ���)��ͨ��Cʵ��  ������ά���ɱ�

//		3��CommWithRecv ��deal_one�Ѿ�����Ʒ��͵����������Ǳջ��ģ����Բ����ڽ��պ��Ҳ���Դͷ������
		//����������Ҫ���� ��Դ��ռ�Ŀ��ƣ����һ����Դ���ڱ�deal_oneʹ�ã������Ľ��̾�Ӧ�õȴ����ͷ�
		// RTͨ�űȽ����⣬�Ƿ���Բ��������Ŀ��Ʒ�ʽ������Ϊ��һ��RT���Թ��̣����ʱ���Գ�һЩ�ģ����ж�����ݳ���
//			��ǰ��ʱ��ÿ�η���ʱ����Ҫ�ϸ�ִ�е� ok 2019�괺��ǰ�������
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getUsingTime() {
		return usingTime;
	}

	public void setUsingTime(int usingTime) {
		this.usingTime = usingTime;
	}

	public String getMeterno() {
		return Meterno;
	}

	public void setMeterno(String meterno) {
		Meterno = meterno;
	}

	public String getEndflag() {
		return Endflag;
	}
	public void setEndflag(String endflag) {
		Endflag = endflag;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}


}
