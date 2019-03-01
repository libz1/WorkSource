package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="runtest")
public class RunTest {

	int ID;
	int subid;   // 分类编号
 	String port;  // 端口
	String recv;   // 接收
	String send;  // 回复
	int no;    // 次序

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getSubid() {
		return subid;
	}
	public void setSubid(int subid) {
		this.subid = subid;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getRecv() {
		return recv;
	}
	public void setRecv(String recv) {
		this.recv = recv;
	}
	public String getSend() {
		return send;
	}
	public void setSend(String send) {
		this.send = send;
	}
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}



}
