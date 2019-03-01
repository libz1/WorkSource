package mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class ByteArrayDecoder extends ProtocolDecoderAdapter {

	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		// ��session���յ���in�����ݣ���������б��봦�����out����
		int limit = in.limit();
		byte[] bytes = new byte[limit];
		in.get(bytes);
		out.write(bytes);
	}

}
// public class ByteArrayDecoder extends CumulativeProtocolDecoder {
//
// public boolean doDecode(IoSession session, IoBuffer in,
// ProtocolDecoderOutput out) throws Exception {
// if (in.remaining() > 0) {
// // ������ʱ����ȡ 4 �ֽ��ж���Ϣ����
// byte[] sizeBytes = new byte[4];
//
// // ��ǵ�ǰλ�ã��Ա� reset
// in.mark();
//
// // ��ȡǮ 4 ���ֽ�
// in.get(sizeBytes);
//
// // NumberUtil ���Լ�д��һ�� int ת byte[] �Ĺ�����
// int size = NumberUtil.bytes2int(sizeBytes);
//
// if (size > in.remaining()) {
// // �����Ϣ���ݵĳ��Ȳ����������ã��൱�ڲ���ȡ size�������� false
// in.reset();
// // ���������ݣ���ƴ�ճ�����������~
// return false;
//
// } else {
// byte[] dataBytes = new byte[size];
// in.get(dataBytes, 0, size);
// out.write(dataBytes);
//
// if (in.remaining() > 0) {
// // �����ȡ���ݺ�ճ�˰������ø����ʣ�µ������ٸ�����һ��~
// return true;
// }
// }
// }
// // ����ɹ����ø�����н����¸���
// return false;
// }
// }
