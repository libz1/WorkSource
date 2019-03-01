package javafx.assident;

import java.util.concurrent.Semaphore;

import com.eastsoft.util.Debug;

import javafx.application.Platform;

// �ο�https://my.oschina.net/chaosannals/blog/672993
// �ؼ����ź���
public class HighFreqDraw {
	private boolean able;
	private HighFreqDrawUpdate worker;

	public HighFreqDraw(HighFreqDrawUpdate worker) {
		able = false;
		this.worker = worker;
	}
	public final synchronized void advance(HighFreqDrawUpdate renderer) {
		advance(renderer,100);
	}

	public final synchronized void advance(HighFreqDrawUpdate renderer,int sleep) {
		if (able)
			return;// ��ֹ��������߳�
		able = true;
		new Thread(() -> {
//			long timer = System.nanoTime();
			Semaphore semaphore = new Semaphore(1);
			while (able)
				try {
					// ����˲��ʱ��
//					long now = System.nanoTime();
//					double moment = (now - timer) * 0.000000001;
//					timer = now;
					// ��������
					if (worker != null)
						worker.update(0.00);
					semaphore.acquire();
					Platform.runLater(() -> {
						renderer.update(0.00);
						semaphore.release();
					});
					Debug.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}).start();
	}

	// �ǵ�Ϩ�𣬲����̲߳��������˳���
	public final void misfire() {
		able = false;
	}
}