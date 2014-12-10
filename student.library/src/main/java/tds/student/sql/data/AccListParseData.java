/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package tds.student.sql.data;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.DB.results.DbResultRecord;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author mpatel
 *
 */
public class AccListParseData
{
  
  private static Logger _logger = LoggerFactory.getLogger (AccListParseData.class);
  public static Data parseData (ColumnResultSet reader) throws ReturnStatusException {
    long startTime = System.currentTimeMillis ();
    Data acc = null;
    try {
      acc = new Data ();
      acc.setType (reader.getString ("AccType"));
      acc.setValue (reader.getString ("AccValue"));
      acc.setCode (reader.getString ("AccCode"));

      acc.setIsDefault (reader.getBoolean ("IsDefault"));
      acc.setAllowCombine (reader.getBoolean ("AllowCombine"));
      acc.setAllowChange (reader.getBoolean ("AllowChange"));
      acc.setIsSelectable (reader.getBoolean ("IsSelectable"));
      acc.setIsVisible (reader.getBoolean ("IsVisible"));
      acc.setStudentControl (reader.getBoolean ("StudentControl"));
      acc.setIsFunctional (reader.getBoolean ("IsFunctional"));

      // for 2012 read the segment column
      if (reader.hasColumn ("Segment")) {
        acc.setSegmentPosition (reader.getInt ("Segment"));
      }
      // for 2011 set to segment position when column exists
      else if (reader.hasColumn ("SegmentPosition")) {
        acc.setSegmentPosition (reader.getInt ("SegmentPosition"));
      }
      // for 2011 set to 0 when TestKey exists
      else if (reader.hasColumn ("TestKey")) {
        acc.setSegmentPosition (0);
      }
      // when everything above is missing set to -1 for global accommodations
      else {
        acc.setSegmentPosition (-1);
      }

      // we only have these when loading test accommodations
      if (reader.hasColumn ("DependsOnToolType")) {
        acc.setDependsOnToolType (reader.getString ("DependsOnToolType"));
      }

      if (reader.hasColumn ("DisableOnGuestSession")) {
        acc.setDisableOnGuestSession (reader.getBoolean ("DisableOnGuestSession"));
      }

      // check if sort orders
      if (reader.hasColumn ("ToolTypeSortOrder") && reader.hasColumn ("ToolValueSortOrder")) {
        acc.setToolTypeSortOrder (reader.getInt ("ToolTypeSortOrder"));
        acc.setToolValueSortOrder (reader.getInt ("ToolValueSortOrder"));
      }

    } catch (SQLException e) {
      _logger.error (e.getMessage (),e);
      throw new ReturnStatusException (e);
    }
    _logger.info ("<<<<<<<<< parseData(ColumnResultSet reader) Total: "+((System.currentTimeMillis ()-startTime)) + " ms.  ThreadId: " +Thread.currentThread ().getId ());
    return acc;
  }

  public static Data parseData (DbResultRecord reader) throws ReturnStatusException {
    long startTime = System.currentTimeMillis ();
    Data acc = null;
    acc = new Data ();
    acc.setType (reader.<String> get ("AccType"));
    acc.setValue (reader.<String> get ("AccValue"));
    acc.setCode (reader.<String> get ("AccCode"));
    acc.setIsDefault (reader.<Boolean> get ("IsDefault"));
    acc.setAllowCombine (reader.<Boolean> get ("AllowCombine"));
    acc.setAllowChange (reader.<Boolean> get ("AllowChange"));
    acc.setIsSelectable (reader.<Boolean> get ("IsSelectable"));
    acc.setIsVisible (reader.<Boolean> get ("IsVisible"));
    acc.setStudentControl (reader.<Boolean> get ("StudentControl"));
    acc.setIsFunctional (reader.<Boolean> get ("IsFunctional"));
    // for 2012 read the segment column
    if (reader.hasColumn ("Segment")) {
      acc.setSegmentPosition (reader.<Integer> get ("Segment"));
    }
    // for 2011 set to segment position when column exists
    else if (reader.hasColumn ("SegmentPosition")) {
      acc.setSegmentPosition (reader.<Integer> get ("SegmentPosition"));
    }
    // for 2011 set to 0 when TestKey exists
    else if (reader.hasColumn ("TestKey")) {
      acc.setSegmentPosition (0);
    }
    // when everything above is missing set to -1 for global accommodations
    else {
      acc.setSegmentPosition (-1);
    }
    // we only have these when loading test accommodations
    if (reader.hasColumn ("DependsOnToolType")) {
      acc.setDependsOnToolType (reader.<String> get ("DependsOnToolType"));
    }
    if (reader.hasColumn ("DisableOnGuestSession")) {
      acc.setDisableOnGuestSession (reader.<Boolean> get ("DisableOnGuestSession"));
    }
    // check if sort orders
    if (reader.hasColumn ("ToolTypeSortOrder") && reader.hasColumn ("ToolValueSortOrder")) {
      acc.setToolTypeSortOrder (reader.<Integer> get ("ToolTypeSortOrder"));
      acc.setToolValueSortOrder (reader.<Integer> get ("ToolValueSortOrder"));
    }
    _logger.info ("<<<<<<<<< parseData Total: "+((System.currentTimeMillis ()-startTime)) + " ms.  ThreadId: " +Thread.currentThread ().getId ());
    return acc;
  }

  public static Dependency parseDependency (DbResultRecord record) throws SQLException {
    Dependency dependency = new Dependency ();
    dependency.setContextType (record.<String> get ("ContextType"));
    dependency.setContext (record.<String> get ("Context"));
    dependency.setIfType (record.<String> get ("IfType"));
    dependency.setIfValue (record.<String> get ("IfValue"));
    dependency.setThenType (record.<String> get ("ThenType"));
    dependency.setThenValue (record.<String> get ("ThenValue"));
    dependency.setIsDefault (record.<Boolean> get ("IsDefault"));

    return dependency;
  }
}
