package shared.bc.com.bodyrobot.connect;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author goJee
 * @since 2019/4/16
 */
public class HeartNettyClientFilter  extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline ph = ch.pipeline();
        //因为服务端设置的超时时间是5秒，所以客户端设置4秒
        ph.addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));

        // 解码和编码，应和服务端一致
        ph.addLast(new StringDecoder());
        ph.addLast(new StringEncoder());

        //处理客户端的业务逻辑
        ph.addLast(new XTClientHandler());
    }
}
