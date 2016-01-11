package tds.student.performance.domain;

/**
 * Represents the results of {@code SQL_QUERY2} in (@code StudentDLL._GetTesteeTestForms_SP).
 * <p>
 *     The data collected by {@code SQL_QUERY2} is already collected in the {@link ClientTestMode} and
 *     {@link ClientTestProperty}.  However, the query has a WHERE clause that states sessionType = -1 OR
 *     sessionType = [the session type passed in].  Because of including of sessionType in the WHERE clause, I'm not
 *     currently comfortable building a new object from data that has already collected.
 * </p>
 * <p>
 *     {@code TideTesteeTestWindowDto} is probably not the best name for this class, but it will have to do
 *     until I have a better understanding of what this class represents.  Right now, it is just a transport object to
 *     make sure the {@code getTesteeTestForms} method has the right data to act upon.
 * </p>
 *
 */
public class TideTesteeTestWindowDto {
    private String tideId; // maps to configs.client_testproperties.tide_id
    private Boolean requireFormWindow; // maps to configs.client_testmode.requirertsformwindow
    private String formField; // maps to configs.client_testproperties.rtsformfield
    private Boolean requireForm; // maps to configs.client_testmode.requirertsform
    private Boolean ifExists; // maps to configs.client_testmode.requirertsformifexists

    public String getTideId() {
        return tideId;
    }

    public void setTideId(String tideId) {
        this.tideId = tideId;
    }

    public Boolean getRequireFormWindow() {
        return requireFormWindow;
    }

    public void setRequireFormWindow(Boolean requireFormWindow) {
        this.requireFormWindow = requireFormWindow;
    }

    public String getFormField() {
        return formField;
    }

    public void setFormField(String formField) {
        this.formField = formField;
    }

    public Boolean getRequireForm() {
        return requireForm;
    }

    public void setRequireForm(Boolean requireForm) {
        this.requireForm = requireForm;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }
}
