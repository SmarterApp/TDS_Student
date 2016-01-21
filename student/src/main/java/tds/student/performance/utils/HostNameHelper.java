package tds.student.performance.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostNameHelper {
    /**
     * <p>
     *     Emulates the CommonDLL.getLocalhostName (in tds.dll.mysql package).
     * </p>
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
