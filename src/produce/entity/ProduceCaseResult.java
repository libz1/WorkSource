package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import util.SoftParameter;

@Entity
@Table(name="producecaseresult")
public class ProduceCaseResult {
    int ID;
    String computer;  // ����ʶ����
    String subid;  //����
    String name;  // ������������
    String send;  // ���ͱ���
    String recv;  // ���ձ���
    int delaytime;  // ִ��ǰ��ʱʱ��
    int waittime;  // ִ�еȴ�ʱ��
    String protocol;  //Э������
    int retrys; // ���Դ���
    String expect;  // ��֤����
    String note; // "������"��־   �����˿ڱ�־��һ�β��Թ����У�������Ҫ���·�˿ڽ���ͨ��  ���ڡ����ڣ�
    String analys; // ���Ľ���
    int caseno; // �������Ⱥ����   ����ȷ��ִ��ʱ���Ⱥ����

    int sendtimes; // ���ʹ���
    String sendtime; // ����ʱ�� ��¼���һ�η���ʱ��
    String recvtime; // ����ʱ��
    String result = ""; // �жϽ��
    int runID ;  // ִ�м�¼ID   ProduceLog.ID operation="ɨ������(1)" ɨ�������õ��豸��ַ���ɽ��в���ִ��
    String port ; // ʵ��ִ�еĶ˿���Ϣ
    String ADDR ; // �豸��ַ����ṹ��û�е�������Ŀ ����@Transientʵ��

    // xuky  2018.01.23 ������¼ԭ�ȵı��ģ������ڷ��ͺ�ȴ��ظ��Ĳ�������
    String send0 = "";  // ���ͱ���-ԭ
    String expect0  = "";  // ��֤����-ԭ
    Boolean waitReply = false; // �ȴ��ϱ��ظ�
    Boolean isBlockTask = false; // ��ǰ�����Ƿ�Ϊ��������

    public void init(){
    	computer = SoftParameter.getInstance().getPCID();  // ����ʶ����
    	subid = "";  //����
    	name = "";  // ������������
    	send = "";  // ���ͱ���
    	recv = "";  // ���ձ���
    	delaytime = 0;  // ִ��ǰ��ʱʱ��
    	waittime = 0;  // ִ�еȴ�ʱ��
    	protocol = "";  //Э������
    	retrys = 0; // ���Դ���
    	expect = "";  // ��֤����
    	note = ""; // "������"��־   �����˿ڱ�־��һ�β��Թ����У�������Ҫ���·�˿ڽ���ͨ��  ���ڡ����ڣ�
    	analys = ""; // ���Ľ���
    	caseno = 0; // �������Ⱥ����   ����ȷ��ִ��ʱ���Ⱥ����

    	sendtimes = 0; // ���ʹ���
    	sendtime = ""; // ����ʱ�� ��¼���һ�η���ʱ��
    	recvtime = ""; // ����ʱ��
    	result = "" ; // �жϽ��
    	runID  = 0;  // ִ�м�¼ID   ProduceLog.ID operation="ɨ������(1)" ɨ�������õ��豸��ַ���ɽ��в���ִ��
    	port  = ""; // ʵ��ִ�еĶ˿���Ϣ
    	ADDR  = ""; // �豸��ַ����ṹ��û�е�������Ŀ ����@Transientʵ��

    	    // xuky  2018.01.23 ������¼ԭ�ȵı��ģ������ڷ��ͺ�ȴ��ظ��Ĳ�������
    	send0 = "";  // ���ͱ���-ԭ
    	expect0  = "";  // ��֤����-ԭ
    	waitReply = false; // �ȴ��ϱ��ظ�
    	isBlockTask = false; // ��ǰ�����Ƿ�Ϊ��������
    }

	@Transient
	public Boolean getIsBlockTask() {
		return isBlockTask;
	}

	public void setIsBlockTask(Boolean isBlockTask) {
		this.isBlockTask = isBlockTask;
	}

	@Transient
    public Boolean getWaitReply() {
		return waitReply;
	}

	public void setWaitReply(Boolean waitReply) {
		this.waitReply = waitReply;
	}

	@Transient
	public String getSend0() {
		return send0;
	}

	public void setSend0(String send0) {
		this.send0 = send0;
	}

	@Transient
	public String getExpect0() {
		return expect0;
	}

	public void setExpect0(String expect0) {
		this.expect0 = expect0;
	}


	public ProduceCaseResult(){
    	sendtimes = 0;
    	recv = "";
//    	computer = Debug.getHdSerialInfo();
    	computer = SoftParameter.getInstance().getPCID();

//    	result = "";
    }

	@Transient
	public String getADDR() {
		return ADDR;
	}

	public void setADDR(String aDDR) {
		ADDR = aDDR;
	}



	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getSubid() {
		return subid;
	}
	public void setSubid(String subid) {
		this.subid = subid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSend() {
		return send;
	}
	public void setSend(String send) {
		this.send = send;
	}
	public String getRecv() {
		return recv;
	}
	public void setRecv(String recv) {
		this.recv = recv;
	}
	public int getDelaytime() {
		return delaytime;
	}
	public void setDelaytime(int delaytime) {
		this.delaytime = delaytime;
	}
	public int getWaittime() {
		return waittime;
	}
	public void setWaittime(int waittime) {
		this.waittime = waittime;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public int getRetrys() {
		return retrys;
	}
	public void setRetrys(int retrys) {
		this.retrys = retrys;
	}
	public String getExpect() {
		return expect;
	}
	public void setExpect(String expect) {
		this.expect = expect;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getAnalys() {
		return analys;
	}
	public void setAnalys(String analys) {
		this.analys = analys;
	}
	public int getSendtimes() {
		return sendtimes;
	}
	public void setSendtimes(int sendtimes) {
		this.sendtimes = sendtimes;
	}
	public String getSendtime() {
		return sendtime;
	}
	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}
	public String getRecvtime() {
		return recvtime;
	}
	public void setRecvtime(String recvtime) {
		this.recvtime = recvtime;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public int getRunID() {
		return runID;
	}
	public void setRunID(int runID) {
		this.runID = runID;
	} //���м�¼ID
	public int getCaseno() {
		return caseno;
	}
	public void setCaseno(int caseno) {
		this.caseno = caseno;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

	public String getComputer() {
		return computer;
	}

	public void setComputer(String computer) {
		this.computer = computer;
	}


}
