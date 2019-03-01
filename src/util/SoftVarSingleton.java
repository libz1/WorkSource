package util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.eastsoft.protocol.Frame645;

import dao.basedao.IBaseDao;
import produce.entity.BarCodeAndAddr;
import produce.entity.BarCodeAndAddrDaoImpl;

/**
 * 软件提高效率变量.
 * <p>
 *
 * @author xuky
 * @version 2018.04.16
 */
public class SoftVarSingleton {

	// xuky 2019.02.12 红外通信加锁（多个表位如果同时抄表会有影响。红外串扰）
    Lock InfraTest_Lock = new ReentrantLock();
	public Lock getInfraTest_Lock() {
		return InfraTest_Lock;
	}
	// xuky 2019.02.12 RT通信加锁（多个表位如果同时抄表会有影响，载波串扰）
    Lock RTTest_Lock = new ReentrantLock();
	public Lock getRTTest_Lock() {
		return RTTest_Lock;
	}
	// xuky 2019.02.12 测试台体通信加锁（使用唯一的串口进行通信）
    Lock PlatformTest_Lock = new ReentrantLock();
	public Lock getPlatformTest_Lock() {
		return PlatformTest_Lock;
	}


	private IBaseDao<BarCodeAndAddr> iBarCodeAndAddrDao = null;
	private Frame645 frame645 = null;

	public Frame645 getFrame645() {
		return frame645;
	}

	public IBaseDao<BarCodeAndAddr> getiBarCodeAndAddrDao() {
		return iBarCodeAndAddrDao;
	}

	// 单例模式：静态变量 uniqueInstance 类的唯一实例
	private volatile static SoftVarSingleton uniqueInstance;

	public static SoftVarSingleton getInstance() {
		if (uniqueInstance == null) {
			synchronized (SoftVarSingleton.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new SoftVarSingleton();
				}
			}
		}
		return uniqueInstance;
	}

	private SoftVarSingleton() {
		iBarCodeAndAddrDao = new BarCodeAndAddrDaoImpl();
		frame645 = new Frame645();
	}



}
