package produce.control.simulation;

// 模拟表台
public class SLPlatform {
	// 监听多个socket端口，进行数据回复

	// 根据收到的报文，得到序言回复的报文
	public static String getReply(String recv){
		String ret ="";
		if (recv.startsWith("F9F9F9F9F9") && recv.length() == 92){
			// 台体升源、降源控制报文
			ret = recv;
		}
		if (recv.startsWith("FEFEFEFEFE01")){
			// 台体-终端测试相关控制报文
			ret = "FEFEFEFEFE01"+"";
		}
		return ret;
	}
}
