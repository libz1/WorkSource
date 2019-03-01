package mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class ByteArrayEncoder extends ProtocolEncoderAdapter {

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		byte[] bytes = (byte[]) message;

		IoBuffer buffer = IoBuffer.allocate(256);
		buffer.setAutoExpand(true);

		buffer.put(bytes);
		buffer.flip();

		out.write(buffer);
		out.flush();

		buffer.free();

//		byte[] dataBytes = (byte[])message;
//		byte[] sizeBytes = NumberUtil.int2bytes(dataBytes.length);
//
//		IoBuffer buffer = IoBuffer.allocate(256);
//		buffer.setAutoExpand(true);
//
//		buffer.put(sizeBytes);
//		buffer.put(dataBytes);
//
//		buffer.flip();
//		out.write(buffer);
//		out.flush();
//
//		buffer.free();
	}

}
