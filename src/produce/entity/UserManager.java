package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="usermanager")
public class UserManager {
	int ID;
	String userid;   // ��¼ϵͳʱ���û�����Ϣ
	String username;
	String userpwd;  // ĿǰΪ���� eastsoft ����ʹ��MD5������
	int userPriority;  // �û�Ȩ�ޣ�0��ͨ��1�߼���2����Ա

	public UserManager(){
		userPriority = 0;
		userpwd = "eastsoft";
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserpwd() {
		return userpwd;
	}
	public void setUserpwd(String userpwd) {
		this.userpwd = userpwd;
	}
	public int getUserPriority() {
		return userPriority;
	}
	public void setUserPriority(int userPriority) {
		this.userPriority = userPriority;
	}


}
