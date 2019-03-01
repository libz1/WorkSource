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
	String workStation; // 工位
	String opName; // 操作人
	String opTime; // 操作时间
	String operation; // 操作内容   软件测试(1)\用户装配(2)\用户重试(3)\判定异常(4)
	String opResult; // 操作结果
	Long opUsingTime; // 操作耗时
	String endTime; // 操作结束时间
	String beginOpt; // 开始操作结束时间
	String endOpt; // 结束操作
	String prevAddr;  // 前一个设备地址

	public void init(){
		addr = "";
		workStation = ""; // 工位
		opName = ""; // 操作人
		opTime = ""; // 操作时间
		operation = ""; // 操作内容   软件测试(1)\用户装配(2)\用户重试(3)\判定异常(4)
		opResult = ""; // 操作结果
		opUsingTime = (long) 0; // 操作耗时
		endTime = ""; // 操作结束时间
		beginOpt = ""; // 开始操作结束时间
		endOpt = ""; // 结束操作
		prevAddr = "";  // 前一个设备地址

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
