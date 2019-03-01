package util;
import java.util.Enumeration;
import java.util.Vector;

import produce.entity.ProduceRecord;

// 对象池 xuky 2018.03.22
// 参考http://blog.csdn.net/shimiso/article/details/9814917
// 调整代码Bug，调整为整体的单例对象
public class ObjectPoolProduceRecord {
	private volatile static ObjectPoolProduceRecord uniqueInstance;

	Boolean PRINT_MSG = false;
	private int numObjects = 6; // 对象池的大小
	private int maxObjects = 50; // 对象池最大的大小
	private Vector<PooledObject> OBJECTS = null; // 存放对象池中对象的向量( PooledObject类型)

	public static ObjectPoolProduceRecord getInstance() {
		if (uniqueInstance == null) {
			synchronized (ObjectPoolProduceRecord.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new ObjectPoolProduceRecord();
				}
			}
		}
		return uniqueInstance;
	}

	/**
	 * 内部使用的用于保存对象池中对象的类。 此类中有两个成员，一个是对象，另一个是指示此对象是否正在使用的标志 。
	 */
	class PooledObject {
		Object objection = null;// 对象
		boolean busy = false; // 此对象是否正在使用的标志，默认没有正在使用
		// 构造函数，根据一个 Object 构告一个 PooledObject 对象

		public PooledObject(Object objection) {
			this.objection = objection;
		}

		// 返回此对象中的对象
		public Object getObject() {
			return objection;
		}

		// 设置此对象的，对象
		public void setObject(Object objection) {
			this.objection = objection;
		}

		// 获得对象对象是否忙
		public boolean isBusy() {
			return busy;
		}

		// 设置对象的对象正在忙
		public void setBusy(boolean busy) {
			this.busy = busy;
		}
	}

	private ObjectPoolProduceRecord() {
		createPool();
	}

	/*** 创建一个对象池 ***/
	private synchronized void createPool() {
		// 确保对象池没有创建。如果创建了，保存对象的向量 objects 不会为空
		if (OBJECTS != null) {
			return; // 如果己经创建，则返回
		}

		// 创建保存对象的向量 , 初始时有 0 个元素
		OBJECTS = new Vector<PooledObject>();

		// 根据 numObjects 中设置的值，循环创建指定数目的对象
		for (int x = 0; x < numObjects; x++) {
			// xuky 2018.03.22 原始代码中的if判断有问题
			// if ((OBJECTS.size() == 0) && (OBJECTS.size() < maxObjects)) {
			Object obj = new ProduceRecord();
			OBJECTS.addElement(new PooledObject(obj));
			// }
		}
	}

	public synchronized Object getObject() {
		// 确保对象池己被创建
		if (OBJECTS == null) {
			return null; // 对象池还没创建，则返回 null
		}

		Object conn = getFreeObject(); // 获得一个可用的对象

		// 如果目前没有可以使用的对象，即所有的对象都在使用中
		while (conn == null) {
			wait(250);
			conn = getFreeObject(); // 重新再试，直到获得可用的对象，如果
			// getFreeObject() 返回的为 null，则表明创建一批对象后也不可获得可用对象
		}

		return conn;// 返回获得的可用的对象
	}

	/**
	 * 本函数从对象池对象 objects 中返回一个可用的的对象，如果 当前没有可用的对象，则创建几个对象，并放入对象池中。
	 * 如果创建后，所有的对象都在使用中，则返回 null
	 */
	private Object getFreeObject() {

		// 从对象池中获得一个可用的对象
		Object obj = findFreeObject();

		if (obj == null) {

			// createObjects(incrementalObjects); // 如果目前对象池中没有可用的对象，创建一些对象、
			// xuky 创建一些对象，放在对象池中
			addObjToPool();

			// 重新从池中查找是否有可用对象
			obj = findFreeObject();

			// 如果创建对象后仍获得不到可用的对象，则返回 null
			if (obj == null) {
				return null;
			}
		}

		return obj;
	}

	private void addObjToPool() {
		if (OBJECTS == null)
			createPool();

		if (PRINT_MSG)
			System.out.println("addObjToPool");

		if (OBJECTS.size() < maxObjects) {
			Object obj = new ProduceRecord();
			OBJECTS.addElement(new PooledObject(obj));
		}

	}

	/**
	 * 查找对象池中所有的对象，查找一个可用的对象， 如果没有可用的对象，返回 null
	 */
	private Object findFreeObject() {

		Object obj = null;
		PooledObject pObj = null;

		// 获得对象池向量中所有的对象
		Enumeration<PooledObject> enumerate = OBJECTS.elements();

		// 遍历所有的对象，看是否有可用的对象
		int i = 1;
		while (enumerate.hasMoreElements()) {
			pObj = (PooledObject) enumerate.nextElement();

			if (PRINT_MSG)
				System.out.println("findFreeObject 遍历(" + i + ") " + pObj);
			i++;

			// 如果此对象不忙，则获得它的对象并把它设为忙
			if (!pObj.isBusy()) {
				if (PRINT_MSG)
					System.out.println("PooledObject is free" + pObj);
				obj = pObj.getObject();
				pObj.setBusy(true);
				// xuky 2018.03.22 如果找到了就退出查找的循环
				break;
			}
		}

		return obj;// 返回找到到的可用对象
	}

	/**
	 * 此函数返回一个对象到对象池中，并把此对象置为空闲。 所有使用对象池获得的对象均应在不使用此对象时返回它。
	 */

	public void returnObject(Object obj) {

		// 确保对象池存在，如果对象没有创建（不存在），直接返回
		if (OBJECTS == null) {
			return;
		}

		PooledObject pObj = null;

		Enumeration<PooledObject> enumerate = OBJECTS.elements();

		// 遍历对象池中的所有对象，找到这个要返回的对象对象
		while (enumerate.hasMoreElements()) {
			pObj = (PooledObject) enumerate.nextElement();

			// 先找到对象池中的要返回的对象对象
			if (obj == pObj.getObject()) {
				if (PRINT_MSG)
					System.out.println("释放对象 "+pObj);

				// 找到了 , 设置此对象为空闲状态
				pObj.setBusy(false);
				break;
			}
		}
	}

	public void setNewObject(Object obj, Object newObj) {

		// 确保对象池存在，如果对象没有创建（不存在），直接返回
		if (OBJECTS == null) {
			return;
		}

		PooledObject pObj = null;

		Enumeration<PooledObject> enumerate = OBJECTS.elements();

		// 遍历对象池中的所有对象，找到这个要返回的对象对象
		while (enumerate.hasMoreElements()) {
			pObj = (PooledObject) enumerate.nextElement();

			// 先找到对象池中的要返回的对象对象
			if (obj == pObj.getObject()) {

				// 找到了 , 设置此对象为空闲状态
				pObj.setObject(newObj);
				break;
			}
		}
	}

	/**
	 * 关闭对象池中所有的对象，并清空对象池。
	 */
	public synchronized void closeObjectPool() {

		// 确保对象池存在，如果不存在，返回
		if (OBJECTS == null) {
			return;
		}

		PooledObject pObj = null;

		Enumeration<PooledObject> enumerate = OBJECTS.elements();

		int i = 1;
		while (enumerate.hasMoreElements()) {

			pObj = (PooledObject) enumerate.nextElement();

			if (PRINT_MSG)
				System.out.println("removeElement(" + i + ")" + pObj);

			i++;

			// 如果忙，等 5 秒
			if (pObj.isBusy()) {
				// wait(5000); // 等 5 秒
				wait(1000); // xuky 等 1 秒
			}

			// 从对象池向量中删除它

			OBJECTS.removeElement(pObj);

			// xuky 2018.03.22 必须添加这里的代码，否则遍历时会访问不到一些节点
			// 一旦删除后，将会导致顺序(链表)信息错误
			enumerate = OBJECTS.elements();
		}

		// 置对象池为空
		OBJECTS = null;
	}

	/**
	 * 使程序等待给定的毫秒数
	 */
	private void wait(int mSeconds) {
		try {

			if (PRINT_MSG)
				System.out.println("sleep " + mSeconds);

			Thread.sleep(mSeconds);
		} catch (InterruptedException e) {
		}
	}
}
