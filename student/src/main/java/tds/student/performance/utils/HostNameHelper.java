package tds.student.performance.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by jjohnson on 12/25/15.
 */
public class HostNameHelper {
    /**
     * Emulates the CommonDLL.getLocalhostName (in tds.dll.mysql package).  Consider moving this to a utilities/helper
     * class.
     * @return The host name of the machine. If an {@code UnknownHostException} is caught, a {@code String} stating "Unknown
     * host name" will be returned instead.
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown host name";
        }
    }
}
