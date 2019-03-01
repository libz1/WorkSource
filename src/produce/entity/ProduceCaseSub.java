package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import util.SoftParameter;


// 测试用例分类信息
@Entity
@Table(name="producecasesub")
public class ProduceCaseSub {
    int ID;
    String computer;  // 电脑识别码
    String no;  //方案id信息
    String name;  //分类名称
    String note1;  // 备注信息
    String note2;  // 备注信息
    String note3;  // 备注信息

    public ProduceCaseSub(){
//    	computer = Debug.getHdSerialInfo();
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getNote3() {
		return note3;
	}
	public void setNote3(String note3) {
		this.note3 = note3;
	}
	public String getComputer() {
		return computer;
	}
	public void setComputer(String computer) {
		this.computer = computer;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}



}
