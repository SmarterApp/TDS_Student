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

import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class is used to update SQL strings to use whatever database names are configured in the settings file.
 * <p>
 *     To configure the constructor argument for auto-wiring this class, add the following to the appropriate
 *     {@code settings.xml} file:
 *
 *     <pre>
 *         {@code
 *          <bean id="legacyDbNameReplacer" class="tds.student.performance.utils.LegacyDbNameReplacer">
 *              <constructor-arg name="settingsFileName" type="java.lang.String" value="name-of-settings-where-db-names-are.xml" />
 *          </bean>
 *         }
 *     </pre>
 * </p>
 * <p>
 *     Auto-wiring the {@code ITDSSettingsSource} to get the database names (which the legacy code uses) was
 *     unfortunately not possible.  Using the {@code ITDSSettingsSource} works correctly in the actual code, but causes
 *     all unit tests to fail.  To leverage the {@code ITDSSettingsSource} would require effort to resolve issues with
 *     the application context file(s) in the test package.
 * </p>
 *
 */
@Component
public class LegacyDbNameUtility {
    private Properties properties = null;

    public enum Databases {
        Archive,
        Config,
        Itembank,
        Session
    }

    /**
     * Load the properties file that has the configured database names.
     * <p>
     *     The settings file in question must be in the classpath.
     * </p>
     *
     * @param settingsFileName The name of the settings file.
     * @throws IOException
     */
    public LegacyDbNameUtility(String settingsFileName) throws IOException {
        this.properties = new Properties();

        try (FileInputStream settingsFile = new FileInputStream(this.getClass().getClassLoader().getResource(settingsFileName).getFile())) {
            this.properties.loadFromXML(settingsFile);
        }
    }

    /**
     * Replace database name placeholders with whatever is configured in the settings file.
     * <p>
     *     The placeholders are:
     *
     *     ${archivedb} = Archive database
     *     ${configdb} = Configs database
     *     ${itembankdb} = Item Bank database
     *     ${sessiondb} = Session database
     *
     *     The replacement is case-insensitive; ${archivedb}, ${ArchiveDB} and/or ${ARCHIVEDB} will work.
     * </p>
     * <p>
     *     If one of the expected database name configuration values is missing from the settings.xml file, then a
     *     default value will be returned instead.
     * </p>
     *
     * @param sql The SQL to be updated.
     * @return The SQL with the placeholders replaced with configured database names.
     */
    public String setDatabaseNames(String sql) {
        return sql.replaceAll("(?iu)\\$\\{archivedb\\}", properties.getProperty("TDSArchiveDBName", "archive"))
                .replaceAll("(?iu)\\$\\{configdb\\}", properties.getProperty("TDSConfigsDBName", "configs"))
                .replaceAll("(?iu)\\$\\{itembankdb\\}", properties.getProperty("ItembankDBName", "itembank"))
                .replaceAll("(?iu)\\$\\{sessiondb\\}", properties.getProperty("TDSSessionDBName", "session"));
    }

    /**
     * Get the configured database name for the type of database.
     *
     * @param db The database
     * @return The name of the database as configured in the settings file.
     */
    public String getDatabaseName(Databases db) {
        switch (db) {
            case Archive:
                return properties.getProperty("TDSArchiveDBName", "archive");
            case Config:
                return properties.getProperty("TDSConfigsDBName", "configs");
            case Itembank:
                return properties.getProperty("ItembankDBName", "itembank");
            case Session:
                return properties.getProperty("TDSSessionDBName", "session");
            default:
                return null;
        }
    }
}
