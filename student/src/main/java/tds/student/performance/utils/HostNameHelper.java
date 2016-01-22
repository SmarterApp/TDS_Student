/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
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
