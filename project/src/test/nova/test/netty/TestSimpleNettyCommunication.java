package nova.test.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.junit.Test;

public class TestSimpleNettyCommunication {

	static final ChannelGroup allDiscardChannels = new DefaultChannelGroup(
			"discard-server");

	static final int MAX_CONNECTIONS = 100;
	private static int nConnections = 0;
	static final int BIND_PORT = 9876;

	private void connectToDiscardServer() {
		nConnections++;
		synchronized (this) {
			this.notify();
		}

		ChannelFactory factory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast(
						"frameDecoder",
						new DelimiterBasedFrameDecoder(8192, Delimiters
								.lineDelimiter()));
				pipeline.addLast("stringDecoder", new StringDecoder());
				pipeline.addLast("stringEncoder", new StringEncoder());
				pipeline.addLast("handler", new DiscardClientHandler());
				return pipeline;
			}

		});

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		ChannelFuture f = bootstrap.connect(new InetSocketAddress("localhost",
				BIND_PORT));
		f.awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}

	private void startDiscardServer() {
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast(
						"frameDecoder",
						new DelimiterBasedFrameDecoder(8192, Delimiters
								.lineDelimiter()));
				pipeline.addLast("stringDecoder", new StringDecoder());
				pipeline.addLast("stringEncoder", new StringEncoder());
				pipeline.addLast("handler", new DiscardServerHandler());
				return pipeline;
			}

		});

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		Channel ch = bootstrap.bind(new InetSocketAddress(BIND_PORT));
		allDiscardChannels.add(ch);
		waitForDiscardServerShutdown();
		ChannelGroupFuture future = allDiscardChannels.close();
		future.awaitUninterruptibly();
		factory.releaseExternalResources();
		synchronized (this) {
			this.notify();
		}
	}

	private void waitForDiscardServerShutdown() {
		synchronized (this) {
			this.notify();
		}
		while (nConnections < MAX_CONNECTIONS) {
			try {
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testDiscardServer() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				startDiscardServer();
			}

		}).start();

		try {
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < MAX_CONNECTIONS; i++) {
			connectToDiscardServer();
		}

		try {
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class DiscardServerHandler extends SimpleChannelHandler {

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		String msg = "wtf\r\n";
		ChannelFuture f = e.getChannel().write(msg);
		f.addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}

class DiscardClientHandler extends SimpleChannelHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		e.getChannel().close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
