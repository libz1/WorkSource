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
	String userid;   // 登录系统时的用户名信息
	String username;
	String userpwd;  // 目前为明文 eastsoft 将来使用MD5计算结果
	int userPriority;  // 用户权限，0普通，1高级、2管理员

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
