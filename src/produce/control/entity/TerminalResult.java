package produce.control.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="terminalresult")

public class TerminalResult {

	int ID;
	private int runID = 0;  //测试数据归属测试记录
	private String devID = "";  //设备的唯一标示信息
	private String meterno = "";  // 表位信息
	private String name = "";  // 测试项目
	private String result = "";  // 测试结果
	private String recvtime = "";  //  测试完成时间
	private int resultID = 0;  //测试的具体信息  与BaseCommLo的对应关系
	private String note1 = "";  //备注信息1
	private String note2 = "";  //备注信息2


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getRunID() {
		return runID;
	}
	public void setRunID(int runID) {
		this.runID = runID;
	}
	public String getDevID() {
		return devID;
	}
	public void setDevID(String devID) {
		this.devID = devID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getRecvtime() {
		return recvtime;
	}
	public void setRecvtime(String recvtime) {
		this.recvtime = recvtime;
	}
	public int getResultID() {
		return resultID;
	}
	public void setResultID(int resultID) {
		this.resultID = resultID;
	}
	public String getNote1() {
		return note1;
	}
	public void setNote1(String note1) {
		this.note1 = note1;
	}
	public String getNote2() {
		return note2;
	}
	public void setNote2(String note2) {
		this.note2 = note2;
	}
	public String getMeterno() {
		return meterno;
	}
	public void setMeterno(String meterno) {
		this.meterno = meterno;
	}


}
