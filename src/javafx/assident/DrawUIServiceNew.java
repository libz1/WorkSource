package javafx.assident;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;

// ²Î¿¼ https://blog.csdn.net/qzidane/article/details/72805587
public class DrawUIServiceNew extends Service<Object[]>{
	Object[] showData ;
	public void setShowText(Object[] showData) {
		this.showData = showData;
	}

	public DrawUIServiceNew(Object[] showData, EventHandler eventHandler){
		this.showData = showData;
		setOnSucceeded(eventHandler);
		start();
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
//	DrawUIServiceNew service = new DrawUIServiceNew((String) s[2], new EventHandler<WorkerStateEvent>() {
//		@Override
//		public void handle(WorkerStateEvent t) {
//			msg3.setText((String) t.getSource().getValue());
//		}
//	});

}
