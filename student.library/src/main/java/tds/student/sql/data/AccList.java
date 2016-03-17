/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemrenderer.data.AccLookup;
import AIR.Common.DB.results.DbResultRecord;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReadOnlyException;
import TDS.Shared.Exceptions.ReturnStatusException;

public class AccList extends ArrayList<Data>
{
  private static final long serialVersionUID = 1L;
  public List<Dependency> _dependencies;
  private static final Logger _logger = LoggerFactory.getLogger (AccList.class);
  
  public AccList () {
    _dependencies = new ArrayList<Dependency> ();
  }

  @SuppressWarnings ("unchecked")
  public Collection<Data> getSegment (final int position) {
    // TODO Ravi/Shiva figure out where to get to use version 4.0
    return CollectionUtils.select (this, new Predicate ()
    {
      public boolean evaluate (Object object) {
        Data data = (Data) object;
        return data.getSegmentPosition () == position;
      }
    });
  }

  public Accommodations createAccommodations (int position, String id, String label) throws ReadOnlyException {
    //id = null;
    //label = null;
    Accommodations accommodations = new Accommodations (position, id, label);

    // get all the acc data for this position
    for (Data accData : getSegment (position)) {
      accommodations.create (accData.getType (), accData.getCode (), accData.getValue (), accData.isVisible (), accData.isSelectable (), accData.isAllowChange (), accData.isStudentControl (),
          accData.getDependsOnToolType (), accData.isDisableOnGuestSession (), accData.isDefault (), accData.isAllowCombine (), false);
    }

    // if this is position 0 then add dependencies (segments don't support
    // this)
    if (position == 0) {
      for (Dependency accDepends : getDependencies ()) {
        accommodations.AddDependency (accDepends.getIfType (), accDepends.getIfValue (), accDepends.getThenType (), accDepends.getThenValue (), accDepends.isDefault ());
      }
    }

    return accommodations;
  }

  /**
   * @return the _dependencies
   */
  public List<Dependency> getDependencies () {
    return _dependencies;
  }

  /**
   * @param _dependencies
   *          the _dependencies to set
   */
  public void setDependencies (List<Dependency> _dependencies) {
    this._dependencies = _dependencies;
  }

  public AccLookup createLookup (int position) {
    // TODO
    AccLookup accLookup = new AccLookup ();

    // get all the acc data for this position
    for (Data accData : getSegment (position)) {
      String[] strArray = new String[] { accData.getValue () };
      accLookup.add (accData.getType (), strArray);
    }

    return accLookup;
  }

  

  
}
