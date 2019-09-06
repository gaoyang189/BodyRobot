package shared.bc.com.bodyrobot.until;

/**
 * @author goJee
 * @since 2017/12/15
 */

public class SharedProtocol {

    private final static int HEAD_TEMPORARY = 1;
    private final static int HEAD_FAT = 2;
    private final static int WEIGHT_START = 6;
    private final static int WEIGHT_50 = 7;
    private final static int WEIGHT_100 = 8;
    private final static int WEIGHT_150 = 9;
    private final static int WEIGHT_END = 10;
    private final static int HEAD_WEIGHT = 11;
    private final static int HEAD_HEIGHT = 12;
    private final static int CALIBRATION_HEIGHT = 13;
    private final static int HEAD_LOCKED = 14;
    private final static int HEAD_FAT_START = 16;
    private final static int HEAD_FAT_TIMEOUT = 17;
    private final static int HEAD_ON = 4;
    private final static int HEAD_OFF = 5;

    private int mHead;

    public interface Listener {
        void locked(int status);
        void temporary(float weight);
        void Weight(float weight);
        void height(float height, int status);
        void FatStart(int status);
        void Fat(float[] fat);
        void FatTimeOut();
        void weightCalibration();
        void weight50();
        void weight100();
        void weight150();
        void weightEnd();
        void calibrationHeight();
        void on();
        void off();
    }

    private Listener listener;

    public SharedProtocol(Listener listener) {
        this.listener = listener;
    }

    public void handleWithData(byte[] data) {
        if (listener == null || data.length < 2) return;

        byte head = data[1];

        float[] fat = new float[9];


        float height;
        float weight;
        int status;
        switch (head) {
            // e.g. FA 01 03 00 00 00 02 weight=0
            // e.g. FA 01 03 00 02 EE EE weight=75.0
            case HEAD_TEMPORARY:
                if(data.length < 6) return;
                weight = getFloatBigEndian(data[4], data[5]);
                if(mHead != HEAD_TEMPORARY && weight == 0) {
                    mHead = HEAD_TEMPORARY;
                    // e.g. status = 0: 正常工作
                    //             = 1: 超重
                    //             = 2: 电池电压过低
                    //             = 3: 电池电压过低，并超重
                    listener.temporary(weight);
                }
                break;
            // e.g. FA 02 11 02 EE 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF
            case HEAD_FAT:
                if(data.length<20) return;
                if(mHead != HEAD_FAT){
                    mHead = HEAD_FAT;
                    fat[0] = getFloatBigEndian(data[4], data[5]);
                    fat[1] = getFloatBigEndian(data[6], data[7]);
                    fat[2] = getFloatBigEndian(data[8], data[9]);
                    fat[3] = getFloatBigEndian(data[10], data[11]);
                    fat[4] = getFloatBigEndian(data[12], data[13]);
                    fat[5] = data[14];
                    fat[6] = data[15];
                    fat[7] = getFloatBigEndian(data[16], data[17]);
                    fat[8] = getFloatBigEndian(data[18], data[19]);
                    listener.Fat(fat);
                }
                break;
            case HEAD_LOCKED:
                if(data.length<4) return;
                if (mHead != HEAD_LOCKED) {
                    mHead = HEAD_LOCKED;
                    status = data[3];
                    listener.locked(status);
                }
                break;
            case HEAD_HEIGHT:
                if(data.length <= 5) return;
                status = data[5];
                height = getFloatBigEndian(data[3], data[4]);
                listener.height(height, status);
                break;
            case HEAD_WEIGHT:
                if(data.length < 5) return;
                if(mHead != HEAD_WEIGHT){
                    mHead = HEAD_WEIGHT;
                    float we = getFloatBigEndian(data[3], data[4]);
                    listener.Weight(we);
                }
                break;
            case HEAD_FAT_START:
                if(data.length < 4) return;
                if(mHead != HEAD_FAT_START){
                    mHead = HEAD_FAT_START;
                    status = data[3];
                    listener.FatStart(status);

                }
                break;
            case HEAD_FAT_TIMEOUT:
                if(mHead != WEIGHT_START && mHead != WEIGHT_50 && mHead != WEIGHT_100 &&mHead!=WEIGHT_150 && mHead !=WEIGHT_END) {
                    listener.FatTimeOut();
                }
                break;
            case WEIGHT_START:
                if(mHead != WEIGHT_START && mHead != WEIGHT_50 && mHead != WEIGHT_100 &&mHead!=WEIGHT_150 && mHead !=WEIGHT_END) {
                    mHead = WEIGHT_START;
                    listener.weightCalibration();
                }
                break;
            case WEIGHT_50:
                if(mHead != WEIGHT_50) {
                    mHead = WEIGHT_50;
                    listener.weight50();
                }
                break;
            case WEIGHT_100:
                if(mHead != WEIGHT_100) {
                    mHead = WEIGHT_100;
                    listener.weight100();
                }
                break;
            case WEIGHT_150:
                if(mHead != WEIGHT_150) {
                    mHead = WEIGHT_150;
                }
                listener.weight150();
                break;
            case WEIGHT_END:
                if(mHead != WEIGHT_END) {
                    mHead = WEIGHT_END;
                    listener.weightEnd();
                }
                break;
            case CALIBRATION_HEIGHT:
                if(mHead != CALIBRATION_HEIGHT){
                    mHead = CALIBRATION_HEIGHT;
                    listener.calibrationHeight();
                }
                break;
            case HEAD_ON:
                mHead = HEAD_ON;
                listener.on();
                break;
            case HEAD_OFF:
                mHead = HEAD_ON;
                listener.off();
                break;

        }
    }

    private static int getIntBigEndian(byte... bs) {
        int len = bs.length;
        int offset;
        int n = 0;
        for (int i = 0; i < len; i++) {
            offset = 8 * (len - i - 1);
            n |= (bs[i] & 0xff) << offset;
        }
        if (len < 4) {
            n <<= 8 * (4 - len);
            n >>= 8 * (4 - len);
        }
        return n;
    }

    private static float getFloatBigEndian(byte... bs) {
        return getIntBigEndian(bs) / 10f;
    }
}
