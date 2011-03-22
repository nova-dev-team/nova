package nova.common.service;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
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
	Channel channel = null;
	Gson gson = new GsonBuilder().serializeNulls().create();

	public SimpleProxy() {
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

	public Channel connect(InetSocketAddress addr) {
		ChannelFuture future = bootstrap.connect(addr);
		this.channel = future.awaitUninterruptibly().getChannel();
		if (!future.isSuccess()) {
			future.getCause().printStackTrace();
			this.close();
			return null;
		}
		this.allChannels.add(this.channel);
		return this.channel;
	}

	public void close() {
		ChannelGroupFuture future = this.allChannels.close();
		future.awaitUninterruptibly();
		this.factory.releaseExternalResources();
		this.bootstrap.releaseExternalResources();
	}

	protected final void sendRequest(Object req) {
		String message = gson.toJson(req) + "\r\n";
		ChannelFuture future = this.channel.write(message);
		future.awaitUninterruptibly();
	}

}
