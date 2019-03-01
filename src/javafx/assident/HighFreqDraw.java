package javafx.assident;

import java.util.concurrent.Semaphore;

import com.eastsoft.util.Debug;

import javafx.application.Platform;

// 参考https://my.oschina.net/chaosannals/blog/672993
// 关键词信号量
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
			return;// 防止启动多个线程
		able = true;
		new Thread(() -> {
//			long timer = System.nanoTime();
			Semaphore semaphore = new Semaphore(1);
			while (able)
				try {
					// 计算瞬间时间
//					long now = System.nanoTime();
//					double moment = (now - timer) * 0.000000001;
//					timer = now;
					// 处理事务
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

	// 记得熄火，不过线程不会马上退出。
	public final void misfire() {
		able = false;
	}
}