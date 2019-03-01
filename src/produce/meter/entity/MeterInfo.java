package produce.meter.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


// 表模块及路由信息
@Entity
@Table(name="meterinfo")
public class MeterInfo {

	int ID;
	String QRCode;
	String status;  // OK  NG
	String type;  // 设备类型  00-表模块，01-路由模块
	String errCode;  //

	String opdatetime;  //
	String operater;  //

	public MeterInfo(){
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getQRCode() {
		return QRCode;
	}

	public void setQRCode(String qRCode) {
		QRCode = qRCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getOpdatetime() {
		return opdatetime;
	}

	public void setOpdatetime(String opdatetime) {
		this.opdatetime = opdatetime;
	}

	public String getOperater() {
		return operater;
	}

	public void setOperater(String operater) {
		this.operater = operater;
	}




}
