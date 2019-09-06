package shared.bc.com.bodyrobot.connect;


import android.util.Log;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.xiaoti.robot.tzcaccess.AccessConstant;
import com.xiaoti.robot.tzcaccess.XTFile;
import com.xiaoti.robot.tzcaccess.XTInstruct;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import shared.bc.com.bodyrobot.HomeActivity;

import static com.xiaoti.robot.tzcaccess.Instruct.IMG_FIND;
import static com.xiaoti.robot.tzcaccess.Instruct.PINGPONG;
import static com.xiaoti.robot.tzcaccess.Instruct.REBOOT;
import static com.xiaoti.robot.tzcaccess.Instruct.SEND_IMG;

public class XTClientHandler extends ChannelInboundHandlerAdapter {
    public RandomAccessFile randomAccessFile;
    private XTFile xtFile;
    private int a = 0;
    public XTClientHandler() {
        xtFile = new XTFile();
    }

    /**
     * 连接成功后向服务器发送指令1，心跳包
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        HomeActivity.instance.num = 0;
        XTInstruct instruct = new XTInstruct();
        instruct.setInstruct(PINGPONG);//心跳指令
        instruct.setContent(HomeActivity.instance.IMEI); //设备编码
        ctx.writeAndFlush(instruct);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        Log.d("长期未读取数据","正在重连");
    }

    /**
     * 处理数据
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //对接受的数据进行类型判断
        if (msg instanceof XTInstruct) {
            //与服务器通道健康
//            if msg 是发图片指令，则进行发送图片
            XTInstruct instruct = (XTInstruct) msg;
            int a = instruct.getInstruct();
            switch (a) {
                //心跳指令
                case PINGPONG:
                    //服务器Echo返回的心跳包
                    //证明 通讯正常
                    break;
                //发送图片，续发指令（一次100k，若图片没有发送完成，服务器会追加续发指令）
                case SEND_IMG:
                    //服务端返回的文件读取的最后字节位置
                    XTFile xtFile = (XTFile) instruct.getContent();
                    //从最后字节位置，继续读取文件，向服务器发送
                    sendPic(ctx, xtFile);
                    break;
                //服务器通知，需要上传图片
                case IMG_FIND:
                    String imageId = (String) instruct.getContent();
                    Log.d("收到服务器寻找图片指令--->" , imageId);
                    File file = new File(UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot/"+imageId+".jpg");
                    this.xtFile.setFile(file);
                    this.xtFile.setFileName(imageId+".jpg");
                    this.xtFile.setStarPos(0); //文件起始标记游标
                    this.xtFile.setEndPos((int) file.length()); //文件末尾标记游标
                    this.xtFile.setPackageSize(AccessConstant.MAX_PACKAGE); //发送数据包的大小(在SendPic方法内，会根据文件大小再度调整的)
                    this.xtFile.setPictureId(imageId); //设置图片ID

                    //将准备好的数据，调用SendPic方法
                    sendPic(ctx, this.xtFile); //发送图片，从文件游标位置0读起
                    break;
                //服务器通知，需要重新启动机器
                case REBOOT:
                    String cmd = "/system/bin/reboot";
                    try {
                        Runtime.getRuntime().exec(cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void sendPic(ChannelHandlerContext ctx, XTFile xtFile) throws IOException {
        //游标最小从0开始读取文件信息
        if (xtFile.getStarPos() != -1) {
            randomAccessFile = new RandomAccessFile(xtFile.getFile(), "r");
            randomAccessFile.seek(xtFile.getStarPos());
            Log.d("发送剩余长度:" ,(randomAccessFile.length() - xtFile.getStarPos())+"");
            int needBt = Long.valueOf(randomAccessFile.length() - xtFile.getStarPos()).intValue();
            //当需要发送的数据包，小于服务器最大要求数据包时，则将其更改
            if (needBt < xtFile.getPackageSize()) {
                xtFile.setPackageSize(needBt);
            }
            byte[] bytes = new byte[xtFile.getPackageSize()];
            //第一条件，读取的字节大小不能为-1（读取失败）
            //第二条件，文件的大小减掉游标索引 = 文件剩余字节数，也就是说剩余字节不能为0
            boolean firstCondition = randomAccessFile.read(bytes) != -1;
            boolean secondCondition = randomAccessFile.length() - xtFile.getStarPos() > 0;
            if (firstCondition && secondCondition) {
                xtFile.setBytes(bytes);
                ctx.writeAndFlush(xtFile);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                Log.e(df.format(new Date())+"","发送文件");
            } else {
                randomAccessFile.close();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                Log.e(df.format(new Date())+"","文件已读取完成");
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        super.userEventTriggered(ctx, obj);
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (event.state().equals(IdleState.WRITER_IDLE)) {
                XTInstruct instruct = new XTInstruct();
                instruct.setInstruct(PINGPONG);//心跳指令
                instruct.setContent(HomeActivity.instance.IMEI); //设备编码
                ctx.writeAndFlush(instruct);
                Log.d("","------向服务器发送数据 发送心跳------");
            } else if (event.state().equals(IdleState.READER_IDLE)) {
//                if (XTClient.channel != null && XTClient.channel.isActive()) return;
//                XTClient.doConnect();
                Log.d("长期未读取数据","正在重连");
            } else if (event.state().equals(IdleState.ALL_IDLE)) {


            }

        }

    }
}
