/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 * <p/>
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 * <p/>
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/

package tds.student.diagnostic.services.impl;

import com.google.common.base.Stopwatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.student.diagnostic.dao.ReadTestDao;
import tds.student.diagnostic.dao.WriteTestDao;
import tds.student.diagnostic.domain.*;
import tds.student.diagnostic.exceptions.DiagnosticException;
import tds.student.diagnostic.services.DiagnosticDatabaseService;
import tds.student.performance.utils.LegacyDbNameUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiagnosticDatabaseServiceImpl implements DiagnosticDatabaseService {

    @Autowired
    ReadTestDao readTestDao;

    @Autowired
    WriteTestDao writeTestDao;

    @Autowired
    LegacyDbNameUtility legacyDbNameUtility;

    //private static final Logger logger = LoggerFactory.getLogger(DiagnosticDatabaseServiceImpl.class);

    public Database readLevelTest() {
        return databaseTest(false);
    }

    public Database writeLevelTest() {
        return databaseTest(true);
    }

    private Database databaseTest(Boolean write) {

        List<Repository> repositories = new ArrayList<>();

        for (LegacyDbNameUtility.Databases databaseName : LegacyDbNameUtility.Databases.values()) {

            List<DatabaseOperation> operations = new ArrayList<>();
            operations.add(readOperation(databaseName));
            if (write) {
                operations.add(writeOperation(databaseName));
            }
            repositories.add(new Repository(Rating.IDEAL, databaseName.toString(), operations));
        }
        return new Database(Rating.IDEAL, repositories);
    }

    private DatabaseOperation readOperation(LegacyDbNameUtility.Databases dbName) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            switch (dbName) {
                case Archive:
                    readTestDao.readArchiveDatabase();
                    break;
                case Config:
                    readTestDao.readConfigsDatabase();
                    break;
                case Itembank:
                    readTestDao.readItemBankDatabase();
                    break;
                case Session:
                    readTestDao.readSessionDatabase();
                    break;
            }
        } catch (DiagnosticException diagnosticException) {
            stopwatch.stop();
            return new DatabaseOperation(Rating.FAILED, DatabaseOperationType.READ, stopwatch.elapsed(TimeUnit.MILLISECONDS), diagnosticException.getMessage());
        }
        return new DatabaseOperation(Rating.IDEAL, DatabaseOperationType.READ, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    private DatabaseOperation writeOperation(LegacyDbNameUtility.Databases dbName) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            switch (dbName) {
                case Archive:
                    writeTestDao.writeArchiveDatabase();
                    break;
                case Config:
                    writeTestDao.writeConfigsDatabase();
                    break;
                case Itembank:
                    writeTestDao.writeItemBankDatabase();
                    break;
                case Session:
                    writeTestDao.writeSessionDatabase();
                    break;
            }
        } catch (DiagnosticException diagnosticException) {
            stopwatch.stop();
            return new DatabaseOperation(Rating.FAILED, DatabaseOperationType.WRITE, stopwatch.elapsed(TimeUnit.MILLISECONDS), diagnosticException.getMessage());
        }
        return new DatabaseOperation(Rating.IDEAL, DatabaseOperationType.WRITE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

}