package nova.common.service;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

public class SimpleServer extends SimpleChannelHandler {

	ChannelGroup allChannels = null;
	ChannelFactory factory = null;
	ServerBootstrap bootstrap = null;

	public SimpleServer() {
		this.allChannels = new DefaultChannelGroup();
		this.factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		this.bootstrap = new ServerBootstrap(factory);

		final SimpleServer thisSvr = this;

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
				pipeline.addLast("channelRecorder", new SimpleChannelHandler() {

					@Override
					public void channelConnected(ChannelHandlerContext ctx,
							ChannelStateEvent e) {
						allChannels.add(e.getChannel());
					}
				});
				pipeline.addLast("handler", thisSvr);
				return pipeline;
			}

		});

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
	}

	public Channel bind(InetSocketAddress addr) {
		Channel ch = bootstrap.bind(addr);
		this.allChannels.add(ch);
		return ch;
	}

	public void shutdown() {
		ChannelGroupFuture future = this.allChannels.close();
		future.awaitUninterruptibly();
		this.factory.releaseExternalResources();
		this.bootstrap.releaseExternalResources();
	}

}
