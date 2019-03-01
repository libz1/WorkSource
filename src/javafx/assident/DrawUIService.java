package javafx.assident;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

// ²Î¿¼ https://blog.csdn.net/qzidane/article/details/72805587
public class DrawUIService extends Service<Object[]>{
	Object[] showData = {null,null};
	public void init(Object[] showData, EventHandler<WorkerStateEvent> eventHandler) {
		this.showData = showData;
		setOnSucceeded(eventHandler);
	}
	@Override
	protected Task<Object[]> createTask() {
		return new Task<Object[]>() {
			@Override
			protected Object[] call() throws Exception {
				return showData;
			}
		};
	}
}
