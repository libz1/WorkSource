package mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

// ²Î¿¼http://www.cnblogs.com/snake-hand/p/3206389.html

public class ByteArrayCodecFactory implements ProtocolCodecFactory {
	private ByteArrayDecoder decoder;
	private ByteArrayEncoder encoder;

	public ByteArrayCodecFactory() {
		encoder = new ByteArrayEncoder();
		decoder = new ByteArrayDecoder();
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return encoder;
	}

}
