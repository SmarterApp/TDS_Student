package tds.student.securebrowser.enums;

/**
 * Enumeration defining TDS related cookie names.
 *
 * Created by emunoz on 3/1/16.
 */
public enum TDSCookieNames {
    TDS_STUDENT_DATA("TDS-Student-Data"),

    TDS_SECURE_BROWSER_LAUNCH_PROTOCOL("TDS-SB-Launch-Protocol");

    TDSCookieNames(String cookieName) {
        this.name = cookieName;
    }

    private String name;

    public String getCookieName() {
        return this.name;
    }
}
