package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.eastsoft.util.Debug;

import util.SoftParameter;

@Entity
@Table(name="produceparam")
public class ProduceParam {
    int ID;
    String computer;  // 电脑识别码
    String type;  // 参数类型
    String name;  // 名称
    String keyname;  // 键
    String value;  // 值
    String note1;  // 说明1
    String note2;  // 说明2

    public ProduceParam(){
    	computer = SoftParameter.getInstance().getPCID();

    }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getComputer() {
		return computer;
	}
	public void setComputer(String computer) {
		this.computer = computer;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKeyname() {
		return keyname;
	}
	public void setKeyname(String keyname) {
		this.keyname = keyname;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getNote1() {
		return note1;
	}
	public void setNote1(String note1) {
		this.note1 = note1;
	}
	public String getNote2() {
		return note2;
	}
	public void setNote2(String note2) {
		this.note2 = note2;
	}


}
