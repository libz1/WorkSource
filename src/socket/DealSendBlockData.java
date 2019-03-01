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

// 处理发送数据的线程
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

	// 阻塞的锁标志
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
					// 双重检查加锁
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

					// xuky 2018.02.06 锁标志，判断阻塞任务是否可以继续后续操作
					if (!ISBUSY) {

						if (DealSendData.getInstance().isLock(SendBlockData.getInstance().getFirst()))
							continue;

						// xuky 2018.04.24 添加新型的锁，等待所有的红外测试完毕，再进行载波通信
						if (DealSendBlockLock.getInstance().getISLOCK()) {
//							Util698.log(DealSendBlockData.class.getName(), "DealSendBlockLock is Lock!",
//									Debug.LOG_INFO);
							continue;
						}

						// 从发送队列中取出数据并执行发送过程
						p = SendBlockData.getInstance().pop();
						if (p != null) {
							// xuky 2018.01.25设定当前任务的阻塞标志
							p.setIsBlockTask(true);

							// xuky 2018.01.25 加锁当前线程，阻塞式运行
							ISBUSY = true;
							map.put(p.getPort(), p);

							if (!RUNFASTER)
								Util698.log(DealSendBlockData.class.getName(),
										"1run "+" 端口:"+p.getPort() +" 执行阻塞任务-begin taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName() ,
										Debug.LOG_INFO);
							// 调用DealSendData的代码，便于进行维护
							DealSendData.getInstance().sendData_new(map, p);
						}
					}

//					Util698.log(DealSendBlockData.class.getName(),"判断是否超时...Begin" ,Debug.LOG_INFO);
					// 遍历map判断是否存在超时的情况
					Iterator iter = map.keySet().iterator();
					int i = 0;
					while (iter.hasNext()) {

						String addr = "";
						try {
							addr = (String) iter.next();
//							Util698.log(DealSendBlockData.class.getName(),"判断是否超时..."+addr ,Debug.LOG_INFO);
						} catch (Exception e) {
							Util698.log(DealSendBlockData.class.getName(),"iter.next Exception:"+e.getMessage() ,Debug.LOG_INFO);
							Debug.sleep(100);
							continue;
						}

						p = map.get(addr);
						Util698.log(DealSendBlockData.class.getName(),"阻塞任务判断是否超时..."+p.getADDR()+"."+p.getCaseno() ,Debug.LOG_INFO);

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

						// 判断是否超时
						p.getWaittime();
						Long diff = Util698.getMilliSecondBetween_new(nowTime, beginTime);
						if ( diff > (long)(p.getWaittime()+p.getDelaytime())) {
							if (p.getSendtimes() > p.getRetrys()) {
								ISBUSY = false;
								map.remove(p.getPort());

								// xuky 2018.04.24 如果设备通信超时了，就可以解锁这个设备
								DealSendBlockLock.getInstance().removeAddr(p.getADDR(),"超时");

								if (!RUNFASTER)
									Util698.log(DealSendData.class.getName(),
										"阻塞任务 超时且重试多次 :" + p.getADDR() + "." + p.getCaseno(), Debug.LOG_INFO);

								p.setResult("超时");
								p.setRecvtime(Util698.getDateTimeSSS_new());
								iBaseDao_ProduceCaseResult.update(p);

								ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p,
										new ProduceCaseResult(), "");
								Object[] s21 = { "DealTestCase", "old", produceCaseResult_tmp, "阻塞超时"+"-"+this };
								PublisherShowList.getInstance().publish(s21);
								produceCaseResult_tmp = null;
								p = null;

							} else {
								// 调用DealSendData的代码，便于进行维护
								Util698.log(DealSendData.class.getName(),
										"阻塞任务taskID:" + p.getADDR() + "." + p.getCaseno() +" 超时重发"+i+" nowTime"+nowTime+" beginTime"+beginTime+" Waittime"+p.getWaittime()+" diff"+diff, Debug.LOG_INFO);
								DealSendData.getInstance().sendData_new(map, p);
								// xuky 2018.07.24 出现
//								2018-07-24 04:54:04:601 [阻塞任务taskID:000000098063.60 超时重发0 nowTime2018-07-24 04:54:04:601beginTime2018-07-24 04:53:27:779Waittime3500diff36822] socket.DealSendData
//								2018-07-24 04:54:04:652 [阻塞任务taskID:000000098063.60 超时重发0 nowTime2018-07-24 04:54:04:652beginTime2018-07-24 04:53:27:779Waittime3500diff36873] socket.DealSendData
//								2018-07-24 04:54:04:702 [阻塞任务taskID:000000098063.60 超时重发0 nowTime2018-07-24 04:54:04:702beginTime2018-07-24 04:53:27:779Waittime3500diff36923] socket.DealSendData
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
			// 调用DealSendData的代码，便于进行维护
			// xuky 2018.07.03 放在线程中执行，显示效果不好！！！
//			new Thread(() -> {
//				String frame = ((String[]) arg)[2];
//				Util698.log(DealSendData.class.getName(), "3.1 update 准备运行DealData（阻塞） :"+frame.substring(frame.length()-4,frame.length()), Debug.LOG_INFO);
				DealSendData.getInstance().DealData(map, arg, "阻塞处理");
//			}).start();
		}
	}

}
