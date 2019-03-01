package socket;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.eastsoft.util.Debug;

import entity.SerialParam;
import frame.Frame698;
import util.Publisher;
import util.PublisherFrame;
import util.PublisherUI;
import util.Util698;

// 处理接收数据的线程
public class DealData {

	// xuky 2018.07.16 添加线程池的方式进行线程调用
//	ExecutorService pool = Executors.newFixedThreadPool(20);
	// xuky 2018.08.02 通过 ThreadPoolExecutor的方式
	// 参考 https://www.cnblogs.com/zedosu/p/6665306.html
	ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

	boolean RUNFASTER = false;
	private volatile static DealData uniqueInstance;
	private Boolean running = true;

	public void setRunning(Boolean running) {
		this.running = running;
		if (!running)
			uniqueInstance = null;
	}

	public static DealData getInstance() {
		if (uniqueInstance == null) {
			synchronized (DealData.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new DealData();
				}
			}
		}
		return uniqueInstance;
	}

	private DealData() {
		new Thread() {
			@Override
			public void run() {
				while (running) {
					String msg = RecvData.getInstance().pop();

					if (!msg.equals("")) {
//						Util698.log(DealData.class.getName(), "RecvData.pop msg:"+msg,
//								Debug.LOG_INFO);

						// 判断格式
						String frame_1 = msg.split(";")[1].split("@")[1];

						if (frame_1.toUpperCase().endsWith("0D")){
							frame_1 = frame_1.substring(0,frame_1.length()-2);
						}
						String frame = frame_1;

						String frameType = Util698.checkFrameType(frame);
						if (frameType.equals("698.45")){

							// 如果是普通报文，需要解析得到设备地址，
							String socketAddr = msg.split(";")[0].split("@")[1];;

							Frame698 frame689 = new Frame698(frame);
							int choiseFlag = frame689.getAPDU().getChoiseFlag();
							String sadata = frame689.getFrameAddr().getSAData();
							if (choiseFlag==1 || choiseFlag==129){
								//链路报文

								// 1、向发布者推送消息

								// xuky 2017.10.13  "rj45" 不适合，模拟回复时出现错误
								String[] s = {"recv frame","link data",frame, socketAddr};

								Util698.log(DealData.class.getName(), "recv link data:"+frame,
										Debug.LOG_INFO);
								PublisherFrame.getInstance().publish(s);


								// 2、如果是，解析得到心跳周期信息、请求类型：建立、心跳、断开，更新终端列表
								String choiseData = frame689.getAPDU().getChoiseData();
								String data = "socketAddr@"+socketAddr + ";"+ choiseData+";logaddr@"+sadata;
								ChannelList.getInstance().change(data);

								// 3、组织回复链路报文  并发送
								String respData = Frame698.buildResponseFrame(frame689);
								SocketServerEast.sendData(respData);

				        		String[] s1 = {"refresh terminal list","",""};
				        		Publisher.getInstance().publish(s1);

							}
							else{
								// 普通报文
								// 1、向发布者推送消息
								String[] s = {"recv frame","user data",frame, socketAddr};
								if (!RUNFASTER)
									Util698.log(DealData.class.getName(), "3recv 端口:"+socketAddr+" user data:"+frame,
										Debug.LOG_INFO);
								// xuky 2018.07.18 ThreadPoolxuky
								pool.submit(new PublisherThread4698(s, socketAddr, sadata));
							}
						}
						else{

							// xuky 2018.08.03 日志输出信息不要放在线程中
							String socketAddr = msg.split(";")[0].split("@")[1];
							String[] s = { "recv frame", "user data", frame, socketAddr };
							if (!RUNFASTER)
								Util698.log(DealData.class.getName(), "3recv 端口:"+socketAddr+ " user data【1】:" + frame+" arg:"+s, Debug.LOG_INFO);

							// xuky 2018.07.18 ThreadPoolxuky
							pool.submit( new PublisherThread(msg, frame, frameType));
						}
					}
					else{
						// xuky 2018.02.02 如果前面执行了任务，则无需sleep
						Debug.sleep(50);
					}
				}
			}

		}.start();
	}

	public class PublisherThread4698 extends Thread {
		String[] s;
		String socketAddr,sadata;
    	public PublisherThread4698(String[] s, String socketAddr, String sadata) {
    		this.s = s;
    		this.socketAddr = socketAddr;
    		this.sadata = sadata;
    		super.setName("PublisherThread4698 in DealData");
    	}
        @Override
        public void run() {
			PublisherFrame.getInstance().publish(s);

			// 2、更新终端列表
			String data = "socketAddr@"+socketAddr + ";logaddr@"+sadata;
			ChannelList.getInstance().change(data);

    		String[] s1 = {"refresh terminal list","",""};
    		Publisher.getInstance().publish(s1);
        }
    }

	public class PublisherThread extends Thread {
		String msg, frame, frameType;
    	public PublisherThread(String msg, String frame, String frameType) {
    		this.msg = msg;
    		this.frame = frame;
    		this.frameType = frameType;
    		super.setName("PublisherThread in DealData");
    	}
        @Override
        public void run() {
			String socketAddr = msg.split(";")[0].split("@")[1];
			// xuky 2017.08.14 特殊情况
			SerialList list = SerialList.getInstance();

			String sadata = socketAddr;
			for (SerialParam serialParam : list.getList()) {
				if (serialParam.getCOMID().equals(socketAddr)) {
					sadata = serialParam.getCOMM();
					break;
				}
			}

			String[] s = { "recv frame", "user data", frame, socketAddr };
			PublisherFrame.getInstance().publish(s);

			// 2、更新终端列表
			String data = "socketAddr@" + socketAddr + ";logaddr@" + sadata;
			ChannelList.getInstance().change(data);

			// xuky 2018.04.12 特殊处理，如果报文是PLC格式的，就再通过PublisherUI发布一次消息
			if (frameType.equals("PLC") || frameType.equals("APPLY_ADDR") ){
				s = (String[]) Util698.addArrayMaxPos(s, frameType, String.class);
				s = (String[]) Util698.addArrayMaxPos(s, data, String.class);
				PublisherUI.getInstance().publish(s);
			}
        }
    }

	public static void main(String[] args) {

	}

}
