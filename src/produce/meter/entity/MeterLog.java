package produce.meter.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;


@Entity
@Table(name="meterlog")
public class MeterLog {

	int ID;
	String QRCodes;   // ��ȡ���Ķ�ά����Ϣ
	String frame;   // ��ȡ���Ĺ�װ�ϱ�����
	String optime; //  ���ݲ���ʱ��
	String analydata;


	public MeterLog(){
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getQRCodes() {
		return QRCodes;
	}

	public void setQRCodes(String qRCodes) {
		QRCodes = qRCodes;
	}

	@Column(length=2000)
	public String getFrame() {
		return frame;
	}

	public void setFrame(String frame) {
		this.frame = frame;
	}

	public String getOptime() {
		return optime;
	}

	public void setOptime(String optime) {
		this.optime = optime;
	}

	@Column(length=2000)
	public String getAnalydata() {
		return analydata;
	}

	public void setAnalydata(String analydata) {
		this.analydata = analydata;
	}



}
