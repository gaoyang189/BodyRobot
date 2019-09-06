package shared.bc.com.bodyrobot.model;

import java.math.BigDecimal;

/**
 * @author goJee
 * @since 2019/4/11
 */
public class Report {


    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal impedance;

    private String img;

    private String deviceCode;

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getImpedance() {
        return impedance;
    }

    public void setImpedance(BigDecimal impedance) {
        this.impedance = impedance;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }


}
