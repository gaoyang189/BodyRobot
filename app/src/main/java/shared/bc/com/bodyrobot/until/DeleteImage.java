package shared.bc.com.bodyrobot.until;

import com.jiangdg.usbcamera.UVCCameraHelper;

import java.io.File;

/**
 * @author goJee
 * @since 2019/4/17
 */
public class DeleteImage implements Runnable {
//    private final static String dirPathLog = UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot/log";    //日志文件夹
    private final static String dirPathDel = UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot";  //临时文件夹
    private final static String dirPathDellog = UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot Log";  //临时文件夹
    private static int a = 0;
    @Override
    public void run() {
        //把要删除的文件包装成文件
        a = 0;
        File file = new File(dirPathDel);
        File filelog = new File(dirPathDellog);
        if(file.exists()) {   //文件或文件夹是否存在
            if(file.isDirectory()) {   //判断是否是目录
                info(file);
                infolog(filelog);
            }
        }
    }

    public static void info(File file) {
        File[] files = file.listFiles();
        //创建您要写入的日志文件
//        String file1 = dirPathLog + "/del_path" + System.currentTimeMillis() + ".txt";   //写入的是否操作
        //遍历files里面的所有文件及文件夹

//        FileWriter fw = null;
//        BufferedWriter bw = null;

        for (File f : files) {

            //获得绝对路径下的文件及文件夹
            File absFile = f.getAbsoluteFile();

            //计算时间
            long day = 1;
            long hour = 24;
            long minute = 60;
            long second = 60;
            long mmcond = 1000;
            long currTime = System.currentTimeMillis();   //当前时间


            long lastTime = absFile.lastModified();     //文件被最后一次修改的时间

            //时间差
            long diffen = currTime - lastTime;

            long thDay = day * hour * minute * second * mmcond;

            if (diffen >= thDay) {
                absFile.delete();
                if (absFile.isDirectory()) {
                    info(absFile);
                    absFile.delete();
                }
            }

//            Log.d("当前时间：", currTime + "");
//            Log.d("文件最后被修改的时间", lastTime + "");
//            Log.d("时间差：", diffen + "");
//
//            Log.d("1天的时间：", thDay + "");
//
//
//            long delTime = lastTime + thDay;   //要删除文件及文件夹的时间--毫秒数
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
//            String formatTime = sdf.format(delTime);

//            String strDel = "删除该文件的时间是：" + "\t" + formatTime + "\t" + "\t删除的文件是：" + absFile.getAbsolutePath();     //absFile.getAbsolutePath()获得你要删除文件的绝对路径
//            try {
//                fw = new FileWriter(file1, true);
//                bw = new BufferedWriter(fw);
//                bw.write(strDel);
//                bw.newLine();    //换行
//                bw.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    bw.close();
//                    fw.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
    public static void infolog(File file) {
        File[] files = file.listFiles();

        for (File f : files) {

            //获得绝对路径下的文件及文件夹
            File absFile2 = f.getAbsoluteFile();

            //计算时间
            long day = 7;
            long hour = 24;
            long minute = 60;
            long second = 60;
            long mmcond = 1000;
            long currTime = System.currentTimeMillis();   //当前时间


            long lastTime = absFile2.lastModified();     //文件被最后一次修改的时间

            //时间差
            long diffen = currTime - lastTime;

            long thDay = day * hour * minute * second * mmcond;

            if (diffen >= thDay) {
                absFile2.delete();
                if (absFile2.isDirectory()) {
                    info(absFile2);
                    absFile2.delete();
                }
            }
        }
    }
}
