package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import util.SoftParameter;

@Entity
@Table(name="producelog")
public class ProduceLog {

	int ID;
	String addr;
	String workStation; // 工位
	String opName; // 操作人
	String opTime; // 操作时间
	String operation; // 操作内容  扫描条码(1)、测试成功(2)、测试失败(3)、用户设置异常(4)
	String opResult;
	String stageno;  // 检测台体编号

	public ProduceLog(){
//		workStation = Debug.getHdSerialInfo();
		workStation = SoftParameter.getInstance().getPCID();

	}

	public void init(){
		addr = "";
		workStation = workStation = SoftParameter.getInstance().getPCID();; // 工位
		opName = ""; // 操作人
		opTime = ""; // 操作时间
		operation = ""; // 操作内容  扫描条码(1)、测试成功(2)、测试失败(3)、用户设置异常(4)
		opResult = "";

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
	public String getWorkStation() {
		return workStation;
	}
	public void setWorkStation(String workStation) {
		this.workStation = workStation;
	}

	public String getStageno() {
		return stageno;
	}

	public void setStageno(String stageno) {
		this.stageno = stageno;
	}

}
