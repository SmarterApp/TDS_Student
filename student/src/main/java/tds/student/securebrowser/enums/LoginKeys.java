package tds.student.securebrowser.enums;

import tds.student.services.LoginService;

/**
 * Enumeration containing keys used by the {@link LoginService}
 *
 * Created by emunoz on 2/26/16.
 */
public enum LoginKeys {
    FIRSTNAME("FirstName"),

    LASTNAME("LastName"),

    STUDENTID("ID"),

    SECURE_BROWSER_LAUNCH_PROTOCOL("SBLaunchProtocol");

    private String keyName;

    LoginKeys(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() { return keyName; }
}

