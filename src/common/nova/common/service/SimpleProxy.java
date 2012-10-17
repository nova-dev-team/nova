package nova.common.service;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
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

/**
 * A simple netty powered client.
 * 
 * @author santa
 * 
 */
public abstract class SimpleProxy extends SimpleChannelHandler {

    ChannelGroup allChannels = null;
    ChannelFactory factory = null;
    ClientBootstrap bootstrap = null;
    ChannelFuture channel = null;
    Gson gson = new GsonBuilder().serializeNulls().create();
    SimpleAddress replyAddr = null;

    Logger log = null;

    public SimpleProxy(InetSocketAddress replyAddr) {
        this(new SimpleAddress(replyAddr));
    }

    public SimpleProxy(SimpleAddress replyAddr) {
        this.log = Logger.getLogger(this.getClass());
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
                        new DelimiterBasedFrameDecoder(
                                SimpleServer.MAX_PACKET_SIZE, Delimiters
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

        // Using addlistener instead of awaitUninterruptibly to avoid deadlock
        ChannelConnectFutureListener chfl = new ChannelConnectFutureListener();
        this.channel.addListener(chfl);
        while (false == chfl.flag) {
            synchronized (chfl.sem) {
                try {
                    chfl.sem.wait();
                } catch (InterruptedException e) {
                    log.error("Thread interrupted", e);
                }
            }
        }
        if (!this.channel.isSuccess()) {
            log.error("Channel connection failure", this.channel.getCause());
        }
        return this.channel;
    }

    public void close() {
        this.channel.getChannel().getCloseFuture().awaitUninterruptibly();
        this.factory.releaseExternalResources();
    }

    protected final void sendRequest(Object req) {
        Xpacket packet = Xpacket.createPacket(req.getClass().getName(), req,
                this.replyAddr);
        String message = gson.toJson(packet) + "\r\n";
        if (message.length() > SimpleServer.MAX_PACKET_SIZE) {
            throw new MessageTooLongException();
        }
        // System.err.println(message);
        this.channel.getChannel().write(message);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        log.error("Exception caught", e.getCause());
        e.getChannel().close();
    }

}

class ChannelConnectFutureListener implements ChannelFutureListener {
    // Using addlistener to avoid deadlock
    public boolean flag = false;
    public Object sem = new Object();

    public ChannelConnectFutureListener() {

    }

    @Override
    public void operationComplete(ChannelFuture arg0) throws Exception {
        this.flag = true;
        synchronized (this.sem) {
            this.sem.notifyAll();
        }
    }

}
