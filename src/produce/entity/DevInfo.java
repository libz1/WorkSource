package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;


// 设备信息
@Entity
//@Table(name="devinfo",indexes={@Index(name="findid",columnList="addr,barCode,status")})
@Table(name="devinfo")

public class DevInfo {

	int ID;
	String addr;
	String type;  //1\II采、 2\集中器(2)、表模块(3)、路由(4)
	String barCode;  // 扫描条码信息
	String status;  // 设备状态  1测试完毕，2设备故障

	String errdatetime;  // 设置设备故障时间
	String errcomputer;  // 设置设备故障机位
	String erroperater;  // 设置设备故障人员
	String okdatetime;  // 清除设备故障时间
	String okcomputer;  // 清除设备故障机位
	String okoperater;  // 清除设备故障人员

	String errCode;  // 测试异常代码

	public DevInfo(){
		type = "II采(1)";
		status = "设备故障(2)";
	}

	public void init(){
		addr = "";
		type = "II采(1)";  //1\II采、 2\集中器(2)
		barCode = "";  // 扫描条码信息
		status = "设备故障(2)";  // 设备状态  1测试完毕，2设备故障

		errdatetime = "";  // 设置设备故障时间
		errcomputer = "";  // 设置设备故障机位
		erroperater = "";  // 设置设备故障人员
		okdatetime = "";  // 清除设备故障时间
		okcomputer = "";  // 清除设备故障机位
		okoperater = "";  // 清除设备故障人员
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}
	public String getErrdatetime() {
		return errdatetime;
	}
	public void setErrdatetime(String errdatetime) {
		this.errdatetime = errdatetime;
	}
	public String getOkdatetime() {
		return okdatetime;
	}
	public void setOkdatetime(String okdatetime) {
		this.okdatetime = okdatetime;
	}
	public String getErrcomputer() {
		return errcomputer;
	}
	public void setErrcomputer(String errcomputer) {
		this.errcomputer = errcomputer;
	}
	public String getErroperater() {
		return erroperater;
	}
	public void setErroperater(String erroperater) {
		this.erroperater = erroperater;
	}
	public String getOkcomputer() {
		return okcomputer;
	}
	public void setOkcomputer(String okcomputer) {
		this.okcomputer = okcomputer;
	}
	public String getOkoperater() {
		return okoperater;
	}
	public void setOkoperater(String okoperater) {
		this.okoperater = okoperater;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}





}
