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

// ����������ݵ��߳�
public class DealData {

	// xuky 2018.07.16 ����̳߳صķ�ʽ�����̵߳���
//	ExecutorService pool = Executors.newFixedThreadPool(20);
	// xuky 2018.08.02 ͨ�� ThreadPoolExecutor�ķ�ʽ
	// �ο� https://www.cnblogs.com/zedosu/p/6665306.html
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
					// ˫�ؼ�����
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

						// �жϸ�ʽ
						String frame_1 = msg.split(";")[1].split("@")[1];

						if (frame_1.toUpperCase().endsWith("0D")){
							frame_1 = frame_1.substring(0,frame_1.length()-2);
						}
						String frame = frame_1;

						String frameType = Util698.checkFrameType(frame);
						if (frameType.equals("698.45")){

							// �������ͨ���ģ���Ҫ�����õ��豸��ַ��
							String socketAddr = msg.split(";")[0].split("@")[1];;

							Frame698 frame689 = new Frame698(frame);
							int choiseFlag = frame689.getAPDU().getChoiseFlag();
							String sadata = frame689.getFrameAddr().getSAData();
							if (choiseFlag==1 || choiseFlag==129){
								//��·����

								// 1���򷢲���������Ϣ

								// xuky 2017.10.13  "rj45" ���ʺϣ�ģ��ظ�ʱ���ִ���
								String[] s = {"recv frame","link data",frame, socketAddr};

								Util698.log(DealData.class.getName(), "recv link data:"+frame,
										Debug.LOG_INFO);
								PublisherFrame.getInstance().publish(s);


								// 2������ǣ������õ�����������Ϣ���������ͣ��������������Ͽ��������ն��б�
								String choiseData = frame689.getAPDU().getChoiseData();
								String data = "socketAddr@"+socketAddr + ";"+ choiseData+";logaddr@"+sadata;
								ChannelList.getInstance().change(data);

								// 3����֯�ظ���·����  ������
								String respData = Frame698.buildResponseFrame(frame689);
								SocketServerEast.sendData(respData);

				        		String[] s1 = {"refresh terminal list","",""};
				        		Publisher.getInstance().publish(s1);

							}
							else{
								// ��ͨ����
								// 1���򷢲���������Ϣ
								String[] s = {"recv frame","user data",frame, socketAddr};
								if (!RUNFASTER)
									Util698.log(DealData.class.getName(), "3recv �˿�:"+socketAddr+" user data:"+frame,
										Debug.LOG_INFO);
								// xuky 2018.07.18 ThreadPoolxuky
								pool.submit(new PublisherThread4698(s, socketAddr, sadata));
							}
						}
						else{

							// xuky 2018.08.03 ��־�����Ϣ��Ҫ�����߳���
							String socketAddr = msg.split(";")[0].split("@")[1];
							String[] s = { "recv frame", "user data", frame, socketAddr };
							if (!RUNFASTER)
								Util698.log(DealData.class.getName(), "3recv �˿�:"+socketAddr+ " user data��1��:" + frame+" arg:"+s, Debug.LOG_INFO);

							// xuky 2018.07.18 ThreadPoolxuky
							pool.submit( new PublisherThread(msg, frame, frameType));
						}
					}
					else{
						// xuky 2018.02.02 ���ǰ��ִ��������������sleep
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

			// 2�������ն��б�
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
			// xuky 2017.08.14 �������
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

			// 2�������ն��б�
			String data = "socketAddr@" + socketAddr + ";logaddr@" + sadata;
			ChannelList.getInstance().change(data);

			// xuky 2018.04.12 ���⴦�����������PLC��ʽ�ģ�����ͨ��PublisherUI����һ����Ϣ
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
