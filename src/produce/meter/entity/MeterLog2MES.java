package produce.meter.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;


@Entity
@Table(name="meterlog2MES",indexes={@Index(name="index_metermes_status",columnList="status"),@Index(name="index_metermes_statusaddr",columnList="addr,status")})
public class MeterLog2MES {

	int ID;
	int resultID;   // 记录producelog表的ID
	String addr;   // 设备通信地址信息
	String priority;   // 数据优先级
	String status;  // 数据状态，  0（默认值）表示未传输，1表示传输完成，2表示传输异常
	String transtime; //  数据传输时间
	String optime_b; //  数据产生时间_开始测试
	String webinfo; // webService返回的错误信息
	String optime_e; //  数据产生时间_完成


	public MeterLog2MES(){
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getResultID() {
		return resultID;
	}

	public void setResultID(int resultID) {
		this.resultID = resultID;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
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


}
