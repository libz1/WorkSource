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
	String stageno = "";  // 表台编码
	String meterno = "";  // 表位编码
	String yyyymm = "";   // 数据年月信息
	int testno = 0;  // 如果年月信息不变化，就自增，每月的初始值为1

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
