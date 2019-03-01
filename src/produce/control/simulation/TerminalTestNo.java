package produce.control.simulation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="teminaltestno")
public class TerminalTestNo {

	int ID;
	String stageno = "";  // ��̨����
	String meterno = "";  // ��λ����
	String yyyymm = "";   // ����������Ϣ
	int testno = 0;  // ���������Ϣ���仯����������ÿ�µĳ�ʼֵΪ1

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getStageno() {
		return stageno;
	}
	public void setStageno(String stageno) {
		this.stageno = stageno;
	}
	public String getMeterno() {
		return meterno;
	}
	public void setMeterno(String meterno) {
		this.meterno = meterno;
	}
	public String getYyyymm() {
		return yyyymm;
	}
	public void setYyyymm(String yyyymm) {
		this.yyyymm = yyyymm;
	}
	public int getTestno() {
		return testno;
	}
	public void setTestno(int testno) {
		this.testno = testno;
	}



}
