package mina;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import socket.ChannelList;
import socket.RecvData;
import util.Publisher;
import util.SoftParameter;
import util.Util698;

public class SerialServerHandlerByte extends IoHandlerAdapter {

	private IoSession SESSION;

	// xuky 2018.07.16 添加线程池的方式进行线程调用
//	ExecutorService pool = Executors.newFixedThreadPool(20);
	// xuky 2018.08.02 通过 ThreadPoolExecutor的方式
	// 参考 https://www.cnblogs.com/zedosu/p/6665306.html
	ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

	public IoSession getSESSION() {
		return SESSION;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		Util698.log(SerialServerHandlerByte.class.getName(), "exceptionCaught:"+cause.getMessage(),Debug.LOG_INFO);
		// xuky 2018.07.25 建议出现这样的情况以后，重启以后，回复之前的测试
        Util698.log(SerialServerHandlerByte.class.getName(), "重新开启，且自动执行测试过程", Debug.LOG_INFO);
        Util698.ResetApp();
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		SESSION = session;
		String comID = (String) session.getAttribute("comID");
		byte[] str = (byte[]) message;
		// xuky 2018.02.01 用于拼装完整报文
		String old = SoftParameter.getInstance().getRecvDataMap().get(comID);
		if (old == null) old = "";
		String thisRecv = DataConvert.bytes2HexString(str);
//		Util698.log("SerialServerHandlerByte", "thisRecv:"+thisRecv,Debug.LOG_INFO);
		final String recvData = old + thisRecv;

		if (!Util698.isCompleteFrame(recvData,comID))
			return;
		SoftParameter.getInstance().getRecvDataMap().put(comID,"");

//		if (!recvData.substring(recvData.length() - 2).equals("16")) {
//
//			SoftParameter.getInstance().getRecvDataMap().put(comID,recvData);
////			Util698.log("SerialServerHandlerByte", "comID"+session.getAttribute("comID")+" messageReceived: continue..."+recvData,
////					Debug.LOG_INFO);
//			return;
//		} else {
////			Util698.log("SerialServerHandlerByte", "comID"+session.getAttribute("comID")+" messageReceived ALL:"+recvData,
////					Debug.LOG_INFO);
//
//			int pos1 = recvData.indexOf("68");
//			int pos2 = recvData.indexOf("68", pos1+1);
//			System.out.println("68 pos ->" + (pos2-pos1) );
//
//			// xuky 2017.07.19 出现了设备地址中有16的情况
//			// 645报文中应该有两处68和最后的16
//			if (pos1 < 0 || pos2 < 0  ){
//				SoftParameter.getInstance().getRecvDataMap().put(comID,recvData);
//				return;
//			}
//
//			// xuky 2017.09.20 出现了设备地址中有68的情况 000000611668
//			// FE FE FE FE 68 68 16 61 00 00 00 68 95 00 11 16
//			// FE FE FE FE 68 68 16
//			if (pos2 - pos1 < 14 ){
//				// 判断两个68之间的字符个数
//				int pos3 = recvData.indexOf("68", pos2+1);
//				if (pos3 < 0){
//					SoftParameter.getInstance().getRecvDataMap().put(comID,recvData);
//					return;
//				}
//			}

//		}

			// xuky 2018.07.18  以下代码(RecvData.getInstance().push(msg))不能放在线程中执行，出现过以下情况

//			2018-07-18 11:15:51,326 [3push  端口:1 serial recv:nullFEFE686180090000006895004F16] mina.SerialServerHandlerByte
//			2018-07-18 11:15:51,336 [3push  端口:2 serial recv:nullFEFE686280090000006895005016] mina.SerialServerHandlerByte
//			2018-07-18 11:15:51,406 [3recv 端口:2 user data【1】:nullFEFE686280090000006895005016] socket.DealData
//			2018-07-18 11:15:51,465 [3recv 端口:1 user data【1】:nullFEFE686180090000006895004F16] socket.DealData

		String msg = "addr@" + session.getAttribute("logAddr") + ";msg@" + recvData + ";comID@" + comID;
		Util698.log(SerialServerHandlerByte.class.getName(), "3push 端口:"+comID+" serial recv:"+recvData.substring(recvData.length()-4,recvData.length()),
					Debug.LOG_INFO);
		RecvData.getInstance().push(msg);

		pool.submit(new PublisherThread(session));
	}

	public class PublisherThread extends Thread {
    	IoSession session;
    	public PublisherThread(IoSession session) {
    		this.session = session;
    		super.setName("PublisherThread in Serial");
    	}
        @Override
        public void run() {
			// 发布收到的消息
			// 更新内存中的终端列表，更新其中的通信时间等
			String data = "socketAddr@" + session.getAttribute("name") + ";logaddr@" + session.getAttribute("logAddr");
			ChannelList.getInstance().change(data);

			// 更新界面显示的终端列表，更新其中的通信时间等
			String[] s1 = { "refresh terminal list", "", "" };
			Publisher.getInstance().publish(s1);
        }
    }

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
//		System.out.println("IDLE " + session.getIdleCount(status));
	}


	public static void main(String[] arg){
		SerialServerHandlerByte serialServerHandlerByte = new SerialServerHandlerByte();

		String str = "FE FE FE FE 68 68 16";
		str = str.replaceAll(" ", "");


//		System.out.println(str +"->"+serialServerHandlerByte.checkData(str,"1"));
//		serialServerHandlerByte.checkData(str,"1");
		str = "68056145520700689F1D878687634B3435CF34F42E357886746433333516472DB902DEE91B16";
		str = "6899999999999968140054FFFFEE01000101020002011801029C01C1FB0245534131000001ACBF092E43B981B1E2480206112233445566030001011801029C01C1FB0245534131000001AD4E60A617726EA74BD1040001020A0102030405060708090A3316";
		str = str.replaceAll(" ", "");
		System.out.println(str +"->"+Util698.isCompleteFrame(str,"1"));
		str = "68056145520700689F1D878687634B3435CF34F42E357886746433333516472DB902DEE91B16A5E816";
		str = str.replaceAll(" ", "");
		System.out.println(str +"->"+Util698.isCompleteFrame(str,"1"));

//		str = "FE FE FE FE 68 68 16 61 00 00 00 68 95 00 11 16";
//		str = str.replaceAll(" ", "");
//		System.out.println(str +"->"+serialServerHandlerByte.checkData(str,"1"));
//
//		str = "FE FE FE FE 68 68 16 68 16";
//		str = str.replaceAll(" ", "");
//		System.out.println(str +"->"+serialServerHandlerByte.checkData(str,"1"));
//
	}
}
