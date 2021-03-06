package nova.common.service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import nova.common.service.message.CloseChannelMessage;

import org.apache.log4j.Logger;
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
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.json.simple.JSONValue;

import sun.net.ftp.FtpProtocolException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * A simple netty powered server.
 * 
 * @author santa
 * 
 */
public class SimpleServer extends SimpleChannelHandler {

    /**
     * Maximum packet size.
     */
    public static final int MAX_PACKET_SIZE = 65536;

    ChannelGroup allChannels = null;
    ChannelFactory factory = null;
    ServerBootstrap bootstrap = null;
    Gson gson = new Gson();
    Logger log = null;

    @SuppressWarnings("rawtypes")
    Map<Class, SimpleHandler> handlers = new HashMap<Class, SimpleHandler>();

    public SimpleServer() {
        this.log = Logger.getLogger(this.getClass());
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

                // TODO @santa auto determine if need port unification
                boolean usePortUnification = true;
                if (usePortUnification) {
                    pipeline.addLast("portUnification",
                            new PortUnificationDecoder());
                } else {
                    pipeline.addLast(
                            "frameDecoder",
                            new DelimiterBasedFrameDecoder(
                                    SimpleServer.MAX_PACKET_SIZE, Delimiters
                                            .lineDelimiter()));
                    pipeline.addLast("stringDecoder", new StringDecoder());
                    pipeline.addLast("stringEncoder", new StringEncoder());

                }

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
    public void registerHandler(Class klass, SimpleHandler handler) {
        handlers.put(klass, handler);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        if (e.getMessage() instanceof DefaultHttpRequest) {
            httpMessageReceived(ctx, e);
        } else {
            jsonMessageReceived(ctx, e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void httpMessageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        DefaultHttpRequest req = (DefaultHttpRequest) e.getMessage();
        SimpleHandler handler = this.handlers.get(DefaultHttpRequest.class);
        if (handler == null) {
            throw new HandlerNotFoundException(
                    DefaultHttpRequest.class.getName());
        }
        try {
            handler.handleMessage(req, ctx, e, null);
        } catch (FtpProtocolException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void jsonMessageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        String msg = (String) e.getMessage();
        Map jsonMsg = (Map) JSONValue.parse(msg);
        String xtype = (String) jsonMsg.get("xtype");
        try {
            Class klass = Class.forName(xtype);
            SimpleHandler handler = this.handlers.get(klass);
            if (handler == null) {
                throw new HandlerNotFoundException(xtype);
            }
            synchronized (gson) {
                SimpleAddress xreply = gson
                        .fromJson(gson.toJson(jsonMsg.get("xreply")),
                                SimpleAddress.class);
                try {
                    handler.handleMessage(gson.fromJson(
                            gson.toJson(jsonMsg.get("xvalue")), klass), ctx, e,
                            xreply);
                } catch (JsonSyntaxException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (FtpProtocolException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        } catch (ClassNotFoundException ex) {
            log.error("Error parsing json message", ex);
        }

        if (xtype.equals(CloseChannelMessage.class.getName().toString())) {
            // System.out.println(xtype);
            e.getChannel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        log.error("Exception caught", e.getCause());
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
