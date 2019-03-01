package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="producerecord")
public class ProduceRecord {

	int ID;
	String addr;
	String workStation; // ��λ
	String opName; // ������
	String opTime; // ����ʱ��
	String operation; // ��������   �������(1)\�û�װ��(2)\�û�����(3)\�ж��쳣(4)
	String opResult; // �������
	Long opUsingTime; // ������ʱ
	String endTime; // ��������ʱ��
	String beginOpt; // ��ʼ��������ʱ��
	String endOpt; // ��������
	String prevAddr;  // ǰһ���豸��ַ

	public void init(){
		addr = "";
		workStation = ""; // ��λ
		opName = ""; // ������
		opTime = ""; // ����ʱ��
		operation = ""; // ��������   �������(1)\�û�װ��(2)\�û�����(3)\�ж��쳣(4)
		opResult = ""; // �������
		opUsingTime = (long) 0; // ������ʱ
		endTime = ""; // ��������ʱ��
		beginOpt = ""; // ��ʼ��������ʱ��
		endOpt = ""; // ��������
		prevAddr = "";  // ǰһ���豸��ַ

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
	public String getOpName() {
		return opName;
	}
	public void setOpName(String opName) {
		this.opName = opName;
	}
	public String getOpTime() {
		return opTime;
	}
	public void setOpTime(String opTime) {
		this.opTime = opTime;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getOpResult() {
		return opResult;
	}
	public void setOpResult(String opResult) {
		this.opResult = opResult;
	}
	public Long getOpUsingTime() {
		return opUsingTime;
	}
	public void setOpUsingTime(Long opUsingTime) {
		this.opUsingTime = opUsingTime;
	}
	public String getWorkStation() {
		return workStation;
	}
	public void setWorkStation(String workStation) {
		this.workStation = workStation;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getBeginOpt() {
		return beginOpt;
	}
	public void setBeginOpt(String beginOpt) {
		this.beginOpt = beginOpt;
	}
	public String getEndOpt() {
		return endOpt;
	}
	public void setEndOpt(String endOpt) {
		this.endOpt = endOpt;
	}
	public String getPrevAddr() {
		return prevAddr;
	}
	public void setPrevAddr(String prevAddr) {
		this.prevAddr = prevAddr;
	}


}
