package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;


// �豸��Ϣ
@Entity
//@Table(name="devinfo",indexes={@Index(name="findid",columnList="addr,barCode,status")})
@Table(name="devinfo")

public class DevInfo {

	int ID;
	String addr;
	String type;  //1\II�ɡ� 2\������(2)����ģ��(3)��·��(4)
	String barCode;  // ɨ��������Ϣ
	String status;  // �豸״̬  1������ϣ�2�豸����

	String errdatetime;  // �����豸����ʱ��
	String errcomputer;  // �����豸���ϻ�λ
	String erroperater;  // �����豸������Ա
	String okdatetime;  // ����豸����ʱ��
	String okcomputer;  // ����豸���ϻ�λ
	String okoperater;  // ����豸������Ա

	String errCode;  // �����쳣����

	public DevInfo(){
		type = "II��(1)";
		status = "�豸����(2)";
	}

	public void init(){
		addr = "";
		type = "II��(1)";  //1\II�ɡ� 2\������(2)
		barCode = "";  // ɨ��������Ϣ
		status = "�豸����(2)";  // �豸״̬  1������ϣ�2�豸����

		errdatetime = "";  // �����豸����ʱ��
		errcomputer = "";  // �����豸���ϻ�λ
		erroperater = "";  // �����豸������Ա
		okdatetime = "";  // ����豸����ʱ��
		okcomputer = "";  // ����豸���ϻ�λ
		okoperater = "";  // ����豸������Ա
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
