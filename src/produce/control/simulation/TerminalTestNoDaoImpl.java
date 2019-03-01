package produce.control.simulation;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dao.basedao.BaseDaoImpl;
import util.Util698;


public class TerminalTestNoDaoImpl extends BaseDaoImpl<TerminalTestNo> implements ITerminalTestNoDao {
    Lock Queuelock = new ReentrantLock();

    public int getNo(String StageNo, String MeterNo, String YYYYMM){
        Queuelock.lock();
        try {
        	return getNoNeedLock(StageNo, MeterNo, YYYYMM);
        } finally {
            Queuelock.unlock();
        }
    }

	// 获取到流水号的同时，进行数据保存，确保下次使用时是正确的信息
    private int getNoNeedLock(String StageNo, String MeterNo, String YYYYMM) {
		int ret = 0;
		List<TerminalTestNo> infos = retrieve(" where stageno='" + StageNo
				+ "' and meterno='" + MeterNo + "' and yyyymm='" + YYYYMM + "'", "");
		TerminalTestNo info = (TerminalTestNo) Util698.getFirstObject(infos);
		if (info != null){
			ret = info.getTestno();
		}
		ret++;
		if (info != null) {
			info.setTestno(ret);
			update(info);
		} else {
			TerminalTestNo teminalTestNo = new TerminalTestNo();
			teminalTestNo.setStageno(StageNo);
			teminalTestNo.setMeterno(MeterNo);
			teminalTestNo.setYyyymm(YYYYMM);
			teminalTestNo.setTestno(ret);
			create(teminalTestNo);
		}

		return ret;
	}
}
