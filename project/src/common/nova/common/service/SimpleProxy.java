package nova.common.service;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SimpleProxy extends SimpleChannelHandler {

	ChannelGroup allChannels = null;
	ChannelFactory factory = null;
	ClientBootstrap bootstrap = null;
	ChannelFuture channel = null;
	Gson gson = new GsonBuilder().serializeNulls().create();
	InetSocketAddress replyAddr = null;

	public SimpleProxy() {
		this(null);
	}

	public SimpleProxy(InetSocketAddress replyAddr) {
		this.replyAddr = replyAddr;
		this.allChannels = new DefaultChannelGroup();
		this.factory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		this.bootstrap = new ClientBootstrap(factory);

		final SimpleProxy thisProxy = this;

		this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast(
						"frameDecoder",
						new DelimiterBasedFrameDecoder(8192, Delimiters
								.lineDelimiter()));
				pipeline.addLast("stringDecoder", new StringDecoder());
				pipeline.addLast("stringEncoder", new StringEncoder());
				pipeline.addLast("handler", thisProxy);
				return pipeline;
			}

		});

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
	}

	public ChannelFuture connect(InetSocketAddress addr) {
		this.channel = bootstrap.connect(addr);

		return this.channel;
	}

	public void close() {
		this.channel.awaitUninterruptibly();
		if (!this.channel.isSuccess()) {
			this.channel.getCause().printStackTrace();
		}
		this.channel.getChannel().getCloseFuture().awaitUninterruptibly();
		this.factory.releaseExternalResources();
	}

	protected final void sendRequest(Object req) throws UnknownHostException {
		Xpacket packet = Xpacket.createPacket(req.getClass().getName(), req,
				replyAddr);
		String message = gson.toJson(packet) + "\r\n";
		// System.out.println(message);
		ChannelFuture future = this.channel.getChannel().write(message);
		future.awaitUninterruptibly();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

}
