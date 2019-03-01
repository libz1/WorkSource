package produce.control.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

// 通用的通信对象类，实现首次发送时间的记录和发送次数的记录
@Entity
@Table(name="basecommlog")

public class BaseCommLog {

	int ID;
	private int runID = 0;  //测试数据归属测试记录
	private String devID = "";  //设备的唯一标示信息
	private String name = "";
	private String commparm = "";
	private String sendtime = "";
	private String expect = "";
	private String recv = "";
	private String recvtime = "";
	private String result = "NG:超时";
	private String send = "";
	private int sendtimes = 0;
	private int retrys = 0;
	private int waittime = 0;
	private String specialData = "";  // 存储根据规则解析得到的数据
	private String specialRule = "";  // 存储解析规则

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}

	public String getSendtime() {
		return sendtime;
	}

	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}

	public int getSendtimes() {
		return sendtimes;
	}

	public void setSendtimes(int sendtimes) {
		this.sendtimes = sendtimes;
	}

	public String getExpect() {
		return expect;
	}

	public void setExpect(String expect) {
		this.expect = expect;
	}

	public String getRecv() {
		return recv;
	}

	public void setRecv(String recv) {
		this.recv = recv;
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

	public String getSend() {
		return send;
	}

	public void setSend(String send) {
		this.send = send;
	}

	public int getRetrys() {
		return retrys;
	}

	public void setRetrys(int retrys) {
		this.retrys = retrys;
	}

	public int getWaittime() {
		return waittime;
	}

	public void setWaittime(int waittime) {
		this.waittime = waittime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommparm() {
		return commparm;
	}

	public void setCommparm(String commparm) {
		this.commparm = commparm;
	}

	public String getSpecialData() {
		return specialData;
	}

	public void setSpecialData(String specialData) {
		this.specialData = specialData;
	}

	public String getSpecialRule() {
		return specialRule;
	}

	public void setSpecialRule(String specialRule) {
		this.specialRule = specialRule;
	}

	public String getDevID() {
		return devID;
	}

	public void setDevID(String devID) {
		this.devID = devID;
	}

	public int getRunID() {
		return runID;
	}

	public void setRunID(int runID) {
		this.runID = runID;
	}

}
