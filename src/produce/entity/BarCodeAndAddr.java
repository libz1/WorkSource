package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

// 扫描码与地址对照表
@Entity
@Table(name="barcodeandaddr")
public class BarCodeAndAddr {

	int ID;
	String longBarCodeBegin;
	String longBarCodeEnd;
	String shortBarCodeBegin;
	String shortBarCodeEnd;
	String addrBegin;
	String addrEnd;
	public BarCodeAndAddr(){
		longBarCodeBegin = "";
		longBarCodeEnd = "";
		shortBarCodeBegin = "";
		shortBarCodeEnd = "";
		addrBegin = "";
		addrEnd = "";
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getLongBarCodeBegin() {
		return longBarCodeBegin;
	}
	public void setLongBarCodeBegin(String longBarCodeBegin) {
		this.longBarCodeBegin = longBarCodeBegin;
	}
	public String getLongBarCodeEnd() {
		return longBarCodeEnd;
	}
	public void setLongBarCodeEnd(String longBarCodeEnd) {
		this.longBarCodeEnd = longBarCodeEnd;
	}
	public String getShortBarCodeBegin() {
		return shortBarCodeBegin;
	}
	public void setShortBarCodeBegin(String shortBarCodeBegin) {
		this.shortBarCodeBegin = shortBarCodeBegin;
	}
	public String getShortBarCodeEnd() {
		return shortBarCodeEnd;
	}
	public void setShortBarCodeEnd(String shortBarCodeEnd) {
		this.shortBarCodeEnd = shortBarCodeEnd;
	}
	public String getAddrBegin() {
		return addrBegin;
	}
	public void setAddrBegin(String addrBegin) {
		this.addrBegin = addrBegin;
	}
	public String getAddrEnd() {
		return addrEnd;
	}
	public void setAddrEnd(String addrEnd) {
		this.addrEnd = addrEnd;
	}


}
