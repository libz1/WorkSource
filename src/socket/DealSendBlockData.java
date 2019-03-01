package socket;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceCaseResultDaoImpl;
import util.Publisher;
import util.PublisherFrame;
import util.PublisherShowList;
import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

// ���������ݵ��߳�
public class DealSendBlockData implements Observer {
	private volatile static DealSendBlockData uniqueInstance;
	private Boolean isRuning = true;
	public Boolean getIsRuning() {
		return isRuning;
	}
	public void setIsRuning(Boolean isRuning) {
		this.isRuning = isRuning;
	}


	Map<String, ProduceCaseResult> map = new LinkedHashMap<String, ProduceCaseResult>();
	IBaseDao<ProduceCaseResult> iBaseDao_ProduceCaseResult;

	// ����������־
	Boolean ISBUSY = false;
	Boolean RUNFASTER = SoftParameter.getInstance().getRUNFASTER();

	public Boolean getISBUSY() {
		return ISBUSY;
	}

	public void setISBUSY(Boolean iSBUSY) {
		ISBUSY = iSBUSY;
	}

	public static DealSendBlockData getInstance() {
		if (uniqueInstance == null) {
			synchronized (DealSendBlockData.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new DealSendBlockData();
				}
			}
		}
		return uniqueInstance;
	}

	private DealSendBlockData() {
		Publisher.getInstance().addObserver(this);
		PublisherUI.getInstance().addObserver(this);
		PublisherFrame.getInstance().addObserver(this);
		iBaseDao_ProduceCaseResult = new ProduceCaseResultDaoImpl();
		new Thread() {
			@Override
			public void run() {
				while (isRuning) {
					Debug.sleep(500);

					ProduceCaseResult p = null;

					// System.out.println("DealSendBlockData ISBUSY:"+ISBUSY);

					// xuky 2018.02.06 ����־���ж����������Ƿ���Լ�����������
					if (!ISBUSY) {

						if (DealSendData.getInstance().isLock(SendBlockData.getInstance().getFirst()))
							continue;

						// xuky 2018.04.24 ������͵������ȴ����еĺ��������ϣ��ٽ����ز�ͨ��
						if (DealSendBlockLock.getInstance().getISLOCK()) {
//							Util698.log(DealSendBlockData.class.getName(), "DealSendBlockLock is Lock!",
//									Debug.LOG_INFO);
							continue;
						}

						// �ӷ��Ͷ�����ȡ�����ݲ�ִ�з��͹���
						p = SendBlockData.getInstance().pop();
						if (p != null) {
							// xuky 2018.01.25�趨��ǰ�����������־
							p.setIsBlockTask(true);

							// xuky 2018.01.25 ������ǰ�̣߳�����ʽ����
							ISBUSY = true;
							map.put(p.getPort(), p);

							if (!RUNFASTER)
								Util698.log(DealSendBlockData.class.getName(),
										"1run "+" �˿�:"+p.getPort() +" ִ����������-begin taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName() ,
										Debug.LOG_INFO);
							// ����DealSendData�Ĵ��룬���ڽ���ά��
							DealSendData.getInstance().sendData_new(map, p);
						}
					}

//					Util698.log(DealSendBlockData.class.getName(),"�ж��Ƿ�ʱ...Begin" ,Debug.LOG_INFO);
					// ����map�ж��Ƿ���ڳ�ʱ�����
					Iterator iter = map.keySet().iterator();
					int i = 0;
					while (iter.hasNext()) {

						String addr = "";
						try {
							addr = (String) iter.next();
//							Util698.log(DealSendBlockData.class.getName(),"�ж��Ƿ�ʱ..."+addr ,Debug.LOG_INFO);
						} catch (Exception e) {
							Util698.log(DealSendBlockData.class.getName(),"iter.next Exception:"+e.getMessage() ,Debug.LOG_INFO);
							Debug.sleep(100);
							continue;
						}

						p = map.get(addr);
						Util698.log(DealSendBlockData.class.getName(),"���������ж��Ƿ�ʱ..."+p.getADDR()+"."+p.getCaseno() ,Debug.LOG_INFO);

						String nowTime = Util698.getDateTimeSSS_new();
						if (p == null) {
							System.out.println("DealSendBlockData.DealSendBlockData  p == null " + nowTime);
							Debug.sleep(300);
							continue;
						}

						String beginTime = p.getSendtime();
						if (beginTime == null || beginTime.equals("")) {
							// System.out.println("DealSendBlockData.DealSendBlockData
							// p.getSendtime() == null " + nowTime);
							Debug.sleep(300);
							continue;
						}

						// �ж��Ƿ�ʱ
						p.getWaittime();
						Long diff = Util698.getMilliSecondBetween_new(nowTime, beginTime);
						if ( diff > (long)(p.getWaittime()+p.getDelaytime())) {
							if (p.getSendtimes() > p.getRetrys()) {
								ISBUSY = false;
								map.remove(p.getPort());

								// xuky 2018.04.24 ����豸ͨ�ų�ʱ�ˣ��Ϳ��Խ�������豸
								DealSendBlockLock.getInstance().removeAddr(p.getADDR(),"��ʱ");

								if (!RUNFASTER)
									Util698.log(DealSendData.class.getName(),
										"�������� ��ʱ�����Զ�� :" + p.getADDR() + "." + p.getCaseno(), Debug.LOG_INFO);

								p.setResult("��ʱ");
								p.setRecvtime(Util698.getDateTimeSSS_new());
								iBaseDao_ProduceCaseResult.update(p);

								ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p,
										new ProduceCaseResult(), "");
								Object[] s21 = { "DealTestCase", "old", produceCaseResult_tmp, "������ʱ"+"-"+this };
								PublisherShowList.getInstance().publish(s21);
								produceCaseResult_tmp = null;
								p = null;

							} else {
								// ����DealSendData�Ĵ��룬���ڽ���ά��
								Util698.log(DealSendData.class.getName(),
										"��������taskID:" + p.getADDR() + "." + p.getCaseno() +" ��ʱ�ط�"+i+" nowTime"+nowTime+" beginTime"+beginTime+" Waittime"+p.getWaittime()+" diff"+diff, Debug.LOG_INFO);
								DealSendData.getInstance().sendData_new(map, p);
								// xuky 2018.07.24 ����
//								2018-07-24 04:54:04:601 [��������taskID:000000098063.60 ��ʱ�ط�0 nowTime2018-07-24 04:54:04:601beginTime2018-07-24 04:53:27:779Waittime3500diff36822] socket.DealSendData
//								2018-07-24 04:54:04:652 [��������taskID:000000098063.60 ��ʱ�ط�0 nowTime2018-07-24 04:54:04:652beginTime2018-07-24 04:53:27:779Waittime3500diff36873] socket.DealSendData
//								2018-07-24 04:54:04:702 [��������taskID:000000098063.60 ��ʱ�ط�0 nowTime2018-07-24 04:54:04:702beginTime2018-07-24 04:53:27:779Waittime3500diff36923] socket.DealSendData
//								Debug.sleep(300);
							}
						}
						i++;
					}
				}
			}

		}.start();
	}

	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		if (s[0].equals("recv frame") && s[1].equals("user data")) {
			// ����DealSendData�Ĵ��룬���ڽ���ά��
			// xuky 2018.07.03 �����߳���ִ�У���ʾЧ�����ã�����
//			new Thread(() -> {
//				String frame = ((String[]) arg)[2];
//				Util698.log(DealSendData.class.getName(), "3.1 update ׼������DealData�������� :"+frame.substring(frame.length()-4,frame.length()), Debug.LOG_INFO);
				DealSendData.getInstance().DealData(map, arg, "��������");
//			}).start();
		}
	}

}
