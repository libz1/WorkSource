package produce.meter.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


// 表模块及路由信息
@Entity
@Table(name="meterinfoid")
public class MeterInfoID {

	int ID;
	int infoid;
	int resultid;
	String QRCode;
	String type;  //
	String data;  //
	String optime;

	public MeterInfoID(){
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getInfoid() {
		return infoid;
	}

	public void setInfoid(int infoid) {
		this.infoid = infoid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getQRCode() {
		return QRCode;
	}

	public void setQRCode(String qRCode) {
		QRCode = qRCode;
	}

	public int getResultid() {
		return resultid;
	}

	public void setResultid(int resultid) {
		this.resultid = resultid;
	}

	public String getOptime() {
		return optime;
	}

	public void setOptime(String optime) {
		this.optime = optime;
	}



}
