package tds.student.securebrowser.enums;

/**
 * Enumeration used to define languages used by TDS and their respective ISO 639 or MFC codes
 *
 * Created by emunoz on 3/3/16.
 */
public enum LanguageCode {
    ENGLISH_US("en", "ENU"),

    SPANISH("es", "ESN");

    LanguageCode(String isoCode, String mfcCode) {
        this.isoCode = isoCode;
        this.mfcCode = mfcCode;
    }

    private String isoCode;

    private String mfcCode;

    public String getIsoCode() {
        return this.isoCode;
    }

    public String getMfcCode() {
        return this.mfcCode;
    }

    public static String getMfcCodeFromIsoCode(final String isoCode) {
        String mfcCode = null;
        for (LanguageCode lc : values()) {
            // Compare the first two chars of the isoCode in the language headers
            if (isoCode.substring(0, 2).equals(lc.getIsoCode())) {
                mfcCode = lc.getMfcCode();
                break;
            }
        }

        if (mfcCode == null) {
            throw new IllegalArgumentException("No matching language code found for ISO language code " + isoCode);
        }

        return mfcCode;
    }
}
