package produce.meter.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


// 表模块及路由信息
@Entity
@Table(name="meterresult")
public class MeterResult {

	int ID;
	String QRCode;
	String type;  // 设备类型
    String result;
    String optime;
    String operator;
	String errCode;  //

	public MeterResult(){
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getOptime() {
		return optime;
	}

	public void setOptime(String optime) {
		this.optime = optime;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getQRCode() {
		return QRCode;
	}

	public void setQRCode(String qRCode) {
		QRCode = qRCode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}





}
