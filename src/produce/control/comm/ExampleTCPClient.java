package produce.control.comm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import com.eastsoft.util.Debug;

import util.Util698;

public class ExampleTCPClient {
	String SendData(String data) {
		String ret = "";
		String modifiedSentence;// 从服务器得到，并送到用户标准输出
		Socket clientSocket;
		try {
			clientSocket = new Socket("127.0.0.1", 8008);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());// 将流连接到标准输入
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));// 连接套接字的输入流
			Util698.log(ExampleTCPClient.class.getName(), "Send to Svr:" + data, Debug.LOG_INFO);

			outToServer.writeBytes(data + '\n'); // outToServer程序的输出流
			modifiedSentence = inFromServer.readLine();// 当到达服务器时，他们进入modifiedSentence字符串中
			ret = modifiedSentence;
			Util698.log(ExampleTCPClient.class.getName(), "Recv From Svr:" + modifiedSentence, Debug.LOG_INFO);
			outToServer.close();
			clientSocket.close();// 关闭套接字，tcp连接也随之关闭
		} catch (Exception e) {
			e.printStackTrace();
		} // clientSocket为定义的套接字
		return ret;
	}

	public static String[] getUsableInfo(){
		String[] ret = {"",""};
		ExampleTCPClient exampleTCPClient = new ExampleTCPClient();
		String result = "";
		List<String> names = Util698.getSrialNames(true);
		String[] SModels = {"HS5100","HS5300","HS5320"};
		Collections.reverse(names);
		Boolean break1 = false;
		for( String name:names ){
			if (break1)
				break;
			name = name.substring(3);
			for( String SModel: SModels ){
				exampleTCPClient.SendData("[Dll_Port_Close]");
				result = exampleTCPClient.SendData("[ReadMeter]type="+SModel+";port="+name+";");
				if (!result.equals("[]")){
					ret[0] = SModel;
					ret[1] = name;
					break1 = true;
					break;
				}
			}
		}
		return ret;
	}

	public static void main(String args[]) throws Exception {
//		// 自动判断串口类型和标准表类型
//		System.out.println(Util698.getDateTimeSSS_new());
//		String[] ret = getUsableInfo();
//		String type = ret[0];
//		String port = ret[1];
//		System.out.println("find:"+type+"-"+port);
//		System.out.println(Util698.getDateTimeSSS_new());
		ExampleTCPClient exampleTCPClient =new ExampleTCPClient();
		// 升源
//		exampleTCPClient.SendData("[on]type="+type+";port="+port+";");

		// 设计三个队列用于与台体的通信
		// 1、用于DLL发送数据排队（因为只有一个台体，执行了DLL调用，要等待回复后，才能进行下一个）
		// 2、载波通信排队（两个报文，一个是启动，一个是查询结果，可能需要多次查询结果）  两次交互实现一项测试
		// 3、红外通信排队  独占
		// 4、台体的每个表位需要执行测试过程队列（最多32个队列），与前三个队列进行数据交互，自己也会通过RJ与集中器的各个端口进行通信

		// 发送串口报文，切换测试模式 调整为国网或是南网模式  也可以尝试发送一下DLL控制函数进行台体测试模式切换
		// 0南网公变 1南网配变 2 国网专变 meter=65 广播
//		exampleTCPClient.SendData("[ChangeMeterInterface]meter=65;port=46;Flag=0;");

////		String sData = "";
////		sData = "6800008101166068140E04669601810116010203040506CC9416";
////		new CommWithRecv().deal_one("修改IP"+"-485_1","COM11:2400",sData);

		//
////		// 设置遥信控制方式为短接
//		exampleTCPClient.SendData("[SetFCAddVolt]meter=12;port=46;Flag=0;");
////		// 查询遥信控制方式为 ？= 0
//		exampleTCPClient.SendData("[GetFCAddVolt]meter=12;port=46;");


////		// 遥信前6路 短路  PChar
//		exampleTCPClient.SendData("[SetFSState]meter=12;port=46;Flag=11000000;");
////		// 查询遥信状态  ？= 11111000
//		exampleTCPClient.SendData("[GetFSState]meter=12;port=46;");
//
//		// 遥信前6路 开路
//		exampleTCPClient.SendData("[SetFSState]meter=12;port=46;Flag=00000000;");
//		// 查询遥信状态  ？= 00000000
//		exampleTCPClient.SendData("[GetFSState]meter=12;port=46;");

//		20、直流电压检测开始/停止
//		主机:01H+地址(A—Z) +长度+B2H(命令) +(30H/开始  31H/停止)+校验位+结束(17H)
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定)+校验位+结束(17H)
//		21、直流电压检测读取
//		主机:01H+地址(A—Z) +长度+B3H(命令) +校验位+结束(17H)
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定)+校验位+结束(17H)  ???
//		 12V 直流电压检测开始
		exampleTCPClient.SendData("[SetCheckDCState]meter=12;port=46;Flag=0;");
		// 直流电压读取
		exampleTCPClient.SendData("[GetDCValue]meter=12;port=46;");
//		// 直流电压检测结束
//		exampleTCPClient.SendData("[SetCheckDCState]meter=12;port=46;Flag=1;");
//
////		1路直流电压（0-5V）输出，1路直流电流（4-20mA）
//
//		24、直流模拟量输出选择 ------（2013-01-07增加 ）
//		主机:01H+地址(A—Z) +长度+B6H(命令) +(31H/电压  30H/电流)+校验位+结束(17H)
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定)+校验位+结束(17H)
//		25、直流模拟量输出回送 ------（2013-01-07增加 ）
//		主机:01H+地址(A—Z) +长度+B7H(命令) +校验位+结束(17H)
//		   从机:01H+地址(A—Z +长度)+ 31H(电压)/30H(电流)+校验位+结束(17H)
//		18、设置PWM占空比
//		主机:01H+地址(A—Z) +长度+B0H(命令) +高位1+高位+低位+校验位+结束(17H)
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定)+校验位+结束(17H)
//
//		19、PWM 输出/停止 控制
//		主机:01H+地址(A—Z) +长度+B1H(命令) +(30H/开始  31H/停止)+校验位+结束(17H)
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定)+校验位+结束(17H)
//		//直流模拟量输出选择  1/电压
		exampleTCPClient.SendData("[SetDCOutType]meter=12;port=46;Flag=1;");
		exampleTCPClient.SendData("[GetDCOutType]meter=12;port=46;");
		// 输出  4.88V电压
		exampleTCPClient.SendData("[SetPWMPara]meter=12;port=46;Flag=0;Double=4.88;");
		// 开始
		exampleTCPClient.SendData("[SetPWMState]meter=12;port=46;Flag=0;");
//		// 结束
//		exampleTCPClient.SendData("[SetPWMState]meter=12;port=46;Flag=1;");
//

//		//直流模拟量输出选择 0/电流
//		exampleTCPClient.SendData("[SetDCOutType]meter=12;port=46;Flag=0;");
//		exampleTCPClient.SendData("[GetDCOutType]meter=12;port=46;");
//		// 输出  18.81mA电流
//		exampleTCPClient.SendData("[SetPWMPara]meter=12;port=46;Flag=1;Double=18.81;");
//		// 开始
//		exampleTCPClient.SendData("[SetPWMState]meter=12;port=46;Flag=0;");
//		// 结束
//		exampleTCPClient.SendData("[SetPWMState]meter=12;port=46;Flag=1;");
//
//		// 断电
//		exampleTCPClient.SendData("[off]port=46;");
//		exampleTCPClient.SendData("[off]port=46;");
	}

}
