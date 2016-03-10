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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class is used to update SQL strings to use whatever database names are configured in program management.
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
    @Value("${student.TDSArchiveDBName:archive}")
    private String archiveDbName;

    @Value("${student.TDSConfigsDBName:configs}")
    private String configsDbName;

    @Value("${student.ItembankDBName:itembank}")
    private String itembankDbName;

    @Value("${student.TDSSessionDBName:session}")
    private String sessionDbName;

    public enum Databases {
        Archive,
        Config,
        Itembank,
        Session
    }

    /**
     * Replace database name placeholders with whatever is configured.
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
     *     If one of the expected database name configuration values is missing, then a default value will be returned
     *     instead.
     * </p>
     *
     * @param sql The SQL to be updated.
     * @return The SQL with the placeholders replaced with configured database names.
     */
    public String setDatabaseNames(String sql) {
        return sql.replaceAll("(?iu)\\$\\{archivedb\\}", archiveDbName)
                .replaceAll("(?iu)\\$\\{configdb\\}", configsDbName)
                .replaceAll("(?iu)\\$\\{itembankdb\\}", itembankDbName)
                .replaceAll("(?iu)\\$\\{sessiondb\\}", sessionDbName);
    }

    /**
     * Get the configured database name for the type of database.
     *
     * @param db The database
     * @return The name of the database as configured.
     */
    public String getDatabaseName(Databases db) {
        switch (db) {
            case Archive:
                return archiveDbName;
            case Config:
                return configsDbName;
            case Itembank:
                return itembankDbName;
            case Session:
                return sessionDbName;
            default:
                return null;
        }
    }
}
