package nova.common.service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import nova.common.service.message.CloseChannelMessage;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
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
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.json.simple.JSONValue;

import com.google.gson.Gson;

public class SimpleServer extends SimpleChannelHandler {

	ChannelGroup allChannels = null;
	ChannelFactory factory = null;
	ServerBootstrap bootstrap = null;
	Gson gson = new Gson();

	@SuppressWarnings("rawtypes")
	Map<Class, ISimpleHandler> handlers = new HashMap<Class, ISimpleHandler>();

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

				// TODO @santa Make it HTTP friendly
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast(
						"frameDecoder",
						new DelimiterBasedFrameDecoder(8192 * 16, Delimiters
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

	@SuppressWarnings("rawtypes")
	public void registerHandler(Class klass, ISimpleHandler handler) {
		handlers.put(klass, handler);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		String msg = (String) e.getMessage();
		Map jsonMsg = (Map) JSONValue.parse(msg);
		String xtype = (String) jsonMsg.get("xtype");
		try {
			Class klass = Class.forName(xtype);
			ISimpleHandler handler = this.handlers.get(klass);
			if (handler == null) {
				throw new HandlerNotFoundException(xtype);
			}
			synchronized (gson) {
				Object xfromObj = jsonMsg.get("xfrom");
				String xfrom = null;
				if (xfromObj != null) {
					xfrom = xfromObj.toString();
				}
				this.handlers.get(klass)
						.handleMessage(
								gson.fromJson(
										gson.toJson(jsonMsg.get("xvalue")),
										klass), ctx, e, xfrom);
			}
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		if (xtype.equals(CloseChannelMessage.class.getName().toString())) {
			// System.out.println(xtype);
			e.getChannel().close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		e.getChannel().close();
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
