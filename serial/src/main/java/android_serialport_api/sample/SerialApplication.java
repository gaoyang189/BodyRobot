
package android_serialport_api.sample;

import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * @author goJee
 * @since 2018/3/21
 */

public interface SerialApplication {

  SerialPort getSerialPort(String path) throws SecurityException, IOException, InvalidParameterException;

  void closeSerialPort();

  void changeSerialPort();
}