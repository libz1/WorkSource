package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;


@Entity
@Table(name="producelog2MES",indexes={@Index(name="index_mes_status",columnList="status"),@Index(name="index_mes_statusaddr",columnList="addr,status")})


public class ProduceLog2MES {

	int ID;
	int producelogID;   // ��¼producelog���ID
	String addr;   // �豸ͨ�ŵ�ַ��Ϣ
	String priority;   // �������ȼ�
	String status;  // ����״̬��  0��Ĭ��ֵ����ʾδ���䣬1��ʾ������ɣ�2��ʾ�����쳣
	String transtime; //  ���ݴ���ʱ��
	String optime_b; //  ���ݲ���ʱ��_��ʼ����
	String webinfo; // webService���صĴ�����Ϣ
	String optime_e; //  ���ݲ���ʱ��_���
	// 2019.02.13 ��Ϊ�豸��Ϣ�ǵ�һ�ģ�֮ǰ��NG�ᱻ�����OK����
	String op_status; //  ���ݲ���ʱ�̵��豸״̬��Ϣ


	public ProduceLog2MES(){
	}

	public void init(){
		producelogID = 0;
		addr = "";
		priority = "";
		status = "0";
		transtime = "";
		optime_b = "";
		webinfo = "";
		optime_e = "";

	}


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}

	public int getProducelogID() {
		return producelogID;
	}

	public void setProducelogID(int producelogID) {
		this.producelogID = producelogID;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTranstime() {
		return transtime;
	}

	public void setTranstime(String transtime) {
		this.transtime = transtime;
	}

	public String getOptime_b() {
		return optime_b;
	}

	public void setOptime_b(String optime_b) {
		this.optime_b = optime_b;
	}

	public String getWebinfo() {
		return webinfo;
	}

	public void setWebinfo(String webinfo) {
		this.webinfo = webinfo;
	}

	public String getOptime_e() {
		return optime_e;
	}

	public void setOptime_e(String optime_e) {
		this.optime_e = optime_e;
	}

	public String getOp_status() {
		return op_status;
	}

	public void setOp_status(String op_status) {
		this.op_status = op_status;
	}


}
