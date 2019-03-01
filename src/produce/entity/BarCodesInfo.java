package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;


// ������Ϣ  �û����ⲿ�������Ϣ��ɨ����ж�ʹ��
@Entity
@Table(name="barcodesinfo",indexes={@Index(name="index_barcode_opdate",columnList="barcode,opdatetime")})

public class BarCodesInfo {

	int ID;
	String barcode;  // ������Ϣ
	String operater;  // ������Ա
	String opdatetime;  // ��������

	public BarCodesInfo(){
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getOperater() {
		return operater;
	}

	public void setOperater(String operater) {
		this.operater = operater;
	}

	public String getOpdatetime() {
		return opdatetime;
	}

	public void setOpdatetime(String opdatetime) {
		this.opdatetime = opdatetime;
	}

}
