package shared.bc.com.bodyrobot.connect;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author javen73
 * @date 2018/10/23 17:12
 */
public class XTClient extends Thread{

    public static Channel channel = null;
    public InputStream fileNames = null;
    public InputStream fileNames2 = null;
    private static Bootstrap b;
    private EventLoopGroup workerGroup;

    public XTClient(Context context) {
        try {
            fileNames = context.getAssets().open("client.bks");
            fileNames2 = context.getAssets().open("client.bks");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        Connection();
    }

    public void Connection() {
        workerGroup = new NioEventLoopGroup();
        try{
            b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE,true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    SSLEngine engine = SslTwoWayContextFactory.getClientContext(fileNames,fileNames2).createSSLEngine();
                    engine.setUseClientMode(true);

                    ch.pipeline().addLast(
                            "ping",
                            new IdleStateHandler(90, 60, 30, TimeUnit.SECONDS));
                    ch.pipeline().addLast("ssl", new SslHandler(engine));
                    ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new XTClientHandler());
                }
            });
            doConnect();
        }catch (Exception e){

        } finally {

        }

    }

    public static void doConnect() throws InterruptedException {
        ChannelFuture localhost = b.connect("tcp.51xiaoti.com", 8871);
        channel = localhost.channel();
        localhost.channel().closeFuture().sync();
    }

    @Override
    public void  interrupt(){
        super.interrupt();
        workerGroup.shutdownGracefully();
    }
}
