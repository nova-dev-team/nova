package nova.common.service;

import java.util.NoSuchElementException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

/**
 * This decoder uses the same port to handle JSON and HTTP messages.
 * 
 * @author santa
 * 
 */
public class PortUnificationDecoder extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {

		if (buffer.readableBytes() < 2) {
			return null;
		}

		final int magic1 = buffer.getUnsignedByte(buffer.readerIndex());
		final int magic2 = buffer.getUnsignedByte(buffer.readerIndex() + 1);

		if (isHttp(magic1, magic2)) {
			switchToHttp(ctx);
		} else {
			// JSON
			switchToJson(ctx);
		}

		// Forward the current read buffer as is to the new handlers.
		return buffer.readBytes(buffer.readableBytes());
	}

	private boolean isHttp(int magic1, int magic2) {
		return magic1 == 'G' && magic2 == 'E' || // GET
				magic1 == 'P' && magic2 == 'O' || // POST
				magic1 == 'P' && magic2 == 'U' || // PUT
				magic1 == 'H' && magic2 == 'E' || // HEAD
				magic1 == 'O' && magic2 == 'P' || // OPTIONS
				magic1 == 'P' && magic2 == 'A' || // PATCH
				magic1 == 'D' && magic2 == 'E' || // DELETE
				magic1 == 'T' && magic2 == 'R' || // TRACE
				magic1 == 'C' && magic2 == 'O'; // CONNECT
	}

	private void remove(ChannelPipeline p, String name) {
		try {
			p.remove(name);
		} catch (NoSuchElementException e) {
			// just ignore
		}
	}

	private void addAfter(ChannelPipeline p, String baseName, String name,
			ChannelHandler handler) {
		if (p.get(name) == null) {
			p.addAfter(baseName, name, handler);
		}
	}

	private void switchToHttp(ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.getPipeline();

		remove(p, "frameDecoder");
		remove(p, "stringDecoder");
		remove(p, "stringEncoder");

		addAfter(p, "portUnification", "httpDecoder", new HttpRequestDecoder());
		addAfter(p, "httpDecoder", "httpAggregator", new HttpChunkAggregator(
				65536));
		addAfter(p, "httpAggregator", "httpEncoder", new HttpResponseEncoder());
		addAfter(p, "httpEncoder", "httpChunkedWriter",
				new ChunkedWriteHandler());

		// p.remove(this);
	}

	private void switchToJson(ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.getPipeline();

		remove(p, "httpDecoder");
		remove(p, "httpAggregator");
		remove(p, "httpEncoder");
		remove(p, "httpChunkedWriter");

		addAfter(p, "portUnification", "frameDecoder",
				new DelimiterBasedFrameDecoder(SimpleServer.MAX_PACKET_SIZE,
						Delimiters.lineDelimiter()));
		addAfter(p, "frameDecoder", "stringDecoder", new StringDecoder());
		addAfter(p, "stringDecoder", "stringEncoder", new StringEncoder());
		// p.remove(this);
	}
}
