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

	// �ж��Ƿ���и�Ԥ��ʱ��
	static Boolean firstStop = false;
	// �ж��Ƿ���Ҫ����������ʱ��
	Long waitTime = Long.valueOf(1000 * 60 * 4);

	private volatile static WatchDogThread uniqueInstance;

	public static WatchDogThread getInstance(Boolean flag) {
		if (uniqueInstance == null)
			synchronized (ClientSimulator.class) {
				if (uniqueInstance == null)
					// ˫�ؼ�����
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

				// xuky 2018.05.29 ����concurrent��service���Զ������
				ProgressService service = new ProgressService();
				// 1��������д�����ݵ�service����Ϊ�����ж��̲߳����������Ǵ����Ĳ���

				service.setFirstStop(firstStop);
				// 2�������ｫservice�����ݰ�ȫ��д��UI
				// https://blog.csdn.net/qzidane/article/details/72805587
				// �о��˰��죬����Task�������call��������ʹ�ô�ͳ���߳�������
				// ֻ��successed, running, scheduled, cancelled,
				// failed�ȷ�������ʹ��JavaFX�̹߳�����

				service.setOnSucceeded((WorkerStateEvent t) -> {
					// msg1.setText((String) t.getSource().getValue());
					// ���ԣ����跢����Ϣ��ֱ�ӽ������ݶ�д
					MessageCenter.getInstance().WatchDog_msg = (String) t.getSource().getValue();
					// xuky 2018.05.29 ����Ϊ�ؼ�����
					firstStop = false;
				});
				service.restart();
			}
		}, 0, 1000 * 60 * 1);
		// }, 100, 1000 * 30 * 1);

	}

}
