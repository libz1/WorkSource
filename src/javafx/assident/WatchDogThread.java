package javafx.assident;

import java.util.Timer;
import java.util.TimerTask;

import com.eastsoft.util.Debug;

import SocketAssident.ClientSimulator;
import javafx.base.WatchDog1;
import javafx.concurrent.WorkerStateEvent;
import util.Util698;

// xuky 2018.06.04
public class WatchDogThread {

	// 判断是否进行干预的时间
	static Boolean firstStop = false;
	// 判断是否需要进行重启的时间
	Long waitTime = Long.valueOf(1000 * 60 * 4);

	private volatile static WatchDogThread uniqueInstance;

	public static WatchDogThread getInstance(Boolean flag) {
		if (uniqueInstance == null)
			synchronized (ClientSimulator.class) {
				if (uniqueInstance == null)
					// 双重检查加锁
					uniqueInstance = new WatchDogThread(flag);
			}
		return uniqueInstance;
	}

	private WatchDogThread(Boolean flag) {
		firstStop = flag;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				new Thread(() -> TaskRun()).start();

			}

			private void TaskRun() {
				Util698.log(WatchDog1.class.getName(), "watching run...", Debug.LOG_INFO);

				// xuky 2018.05.29 启用concurrent的service可以多次运行
				ProgressService service = new ProgressService();
				// 1、在这里写入数据到service，因为可能有多线程操作，将会是大量的操作

				service.setFirstStop(firstStop);
				// 2、在这里将service的数据安全的写入UI
				// https://blog.csdn.net/qzidane/article/details/72805587
				// 研究了半天，发现Task类里面的call方法还是使用传统子线程做处理
				// 只有successed, running, scheduled, cancelled,
				// failed等方法才是使用JavaFX线程工作的

				service.setOnSucceeded((WorkerStateEvent t) -> {
					// msg1.setText((String) t.getSource().getValue());
					// 尝试，无需发送消息，直接进行数据读写
					MessageCenter.getInstance().WatchDog_msg = (String) t.getSource().getValue();
					// xuky 2018.05.29 以下为关键代码
					firstStop = false;
				});
				service.restart();
			}
		}, 0, 1000 * 60 * 1);
		// }, 100, 1000 * 30 * 1);

	}

}
