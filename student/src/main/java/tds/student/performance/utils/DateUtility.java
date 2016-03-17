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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Date;

@Component
public class DateUtility {
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Date getDbDate() {
        return jdbcTemplate.queryForObject("SELECT now(3)", Date.class);
    }

    public Date getLocalDate() {
        return new Date();
    }

    public Timestamp getTimestamp() {
        return new Timestamp(getLocalDate().getTime());
    }

    public static Long minutesDiff (Date from, Date to) {
        if (from == null || to == null)
            return null;
        return (to.getTime () - from.getTime ()) / 1000 / 60;
    }
}
