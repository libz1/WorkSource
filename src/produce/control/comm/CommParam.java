package produce.control.comm;

import com.eastsoft.util.DataConvert;

import entity.SerialParam;

public class CommParam {
	String type = "1";  // 1-COM;2-RJ45
	SerialParam serialParam = null;
	RJ45Param RJ45Param = null;
	String split = ":";

	public CommParam(){

	}

	public CommParam(String param){
		// 入参为IP:port 或者COM:baudrate
		 if (param.indexOf("COM") >= 0){
			 type = "1";

			 String[] arg = param.split(split);
			 if (arg.length == 2)
				 serialParam = new SerialParam(arg[0],DataConvert.String2Int(arg[1]));
			 else
				 serialParam = new SerialParam(arg[0],DataConvert.String2Int(arg[1]),arg[2]);
		 }
		 else{
			 type = "2";
			 RJ45Param = new RJ45Param(param.split(split)[0],DataConvert.String2Int(param.split(split)[1]));
		 }
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public SerialParam getSerialParam() {
		return serialParam;
	}

	public void setSerialParam(SerialParam serialParam) {
		this.serialParam = serialParam;
	}

	public RJ45Param getRJ45Param() {
		return RJ45Param;
	}

	public void setRJ45Param(RJ45Param rJ45Param) {
		RJ45Param = rJ45Param;
	}


}
