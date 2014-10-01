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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import com.fasterxml.jackson.annotation.JsonProperty;

import tds.itemrenderer.data.AccLookup;
import AIR.Common.DB.results.DbResultRecord;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.ReadOnlyException;

/**
 * @author temp_rreddy
 * 
 */
public class AccList extends ArrayList<AccList.Data>
{
  public List<Dependency> _dependencies;

  public AccList () {
    _dependencies = new ArrayList<Dependency> ();
  }

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
          accData.getDependsOnToolType (), accData.isDisableOnGuestSession (), accData.isDefault (), accData.isAllowCombine ());
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

  public class Data
  {
    private int     _segmentPosition;
    private String  _type;
    private String  _code;
    private String  _value;
    private boolean _isVisible;
    private boolean _isSelectable;
    private boolean _allowChange;
    private boolean _studentControl;
    private boolean _isDefault;
    private boolean _allowCombine;
    private boolean _isFunctional;
    private String  _dependsOnToolType;
    private boolean _disableOnGuestSession;
    private int     _toolTypeSortOrder;
    private int     _toolValueSortOrder;

    /**
     * @return the _segmentPosition
     */
    @JsonProperty ("SegmentPosition")
    public int getSegmentPosition () {
      return _segmentPosition;
    }

    /**
     * @param _segmentPosition
     *          the _segmentPosition to set
     */
    public void setSegmentPosition (int _segmentPosition) {
      this._segmentPosition = _segmentPosition;
    }

    /**
     * @return the _type
     */
    @JsonProperty ("Type")
    public String getType () {
      return _type;
    }

    /**
     * @param _type
     *          the _type to set
     */
    public void setType (String _type) {
      this._type = _type;
    }

    /**
     * @return the _code
     */
    @JsonProperty ("Code")
    public String getCode () {
      return _code;
    }

    /**
     * @param _code
     *          the _code to set
     */
    public void setCode (String _code) {
      this._code = _code;
    }

    /**
     * @return the _value
     */
    @JsonProperty ("Value")
    public String getValue () {
      return _value;
    }

    /**
     * @param _value
     *          the _value to set
     */
    public void setValue (String _value) {
      this._value = _value;
    }

    /**
     * @return the _isVisible
     */
    @JsonProperty ("IsVisible")
    public boolean isVisible () {
      return _isVisible;
    }

    /**
     * @param _isVisible
     *          the _isVisible to set
     */
    public void setIsVisible (boolean _isVisible) {
      this._isVisible = _isVisible;
    }

    /**
     * @return the _isSelectable
     */
    @JsonProperty ("IsSelectable")
    public boolean isSelectable () {
      return _isSelectable;
    }

    /**
     * @param _isSelectable
     *          the _isSelectable to set
     */
    public void setIsSelectable (boolean _isSelectable) {
      this._isSelectable = _isSelectable;
    }

    /**
     * @return the _allowChange
     */
    @JsonProperty ("AllowChange")
    public boolean isAllowChange () {
      return _allowChange;
    }

    /**
     * @param _allowChange
     *          the _allowChange to set
     */
    public void setAllowChange (boolean _allowChange) {
      this._allowChange = _allowChange;
    }

    /**
     * @return the _studentControl
     */
    @JsonProperty ("StudentControl")
    public boolean isStudentControl () {
      return _studentControl;
    }

    /**
     * @param _studentControl
     *          the _studentControl to set
     */
    public void setStudentControl (boolean _studentControl) {
      this._studentControl = _studentControl;
    }

    /**
     * @return the _isDefault
     */
    @JsonProperty ("IsDefault")
    public boolean isDefault () {
      return _isDefault;
    }

    /**
     * @param _isDefault
     *          the _isDefault to set
     */
    public void setIsDefault (boolean _isDefault) {
      this._isDefault = _isDefault;
    }

    /**
     * @return the _allowCombine
     */
    @JsonProperty ("AllowCombine")
    public boolean isAllowCombine () {
      return _allowCombine;
    }

    /**
     * @param _allowCombine
     *          the _allowCombine to set
     */
    public void setAllowCombine (boolean _allowCombine) {
      this._allowCombine = _allowCombine;
    }

    /**
     * @return the _isFunctional
     */
    @JsonProperty ("IsFunctional")
    public boolean isFunctional () {
      return _isFunctional;
    }

    /**
     * @param _isFunctional
     *          the _isFunctional to set
     */
    public void setIsFunctional (boolean _isFunctional) {
      this._isFunctional = _isFunctional;
    }

    /**
     * @return the _dependsOnToolType
     */
    @JsonProperty ("DependsOnToolType")
    public String getDependsOnToolType () {
      return _dependsOnToolType;
    }

    /**
     * @param _dependsOnToolType
     *          the _dependsOnToolType to set
     */
    public void setDependsOnToolType (String _dependsOnToolType) {
      this._dependsOnToolType = _dependsOnToolType;
    }

    /**
     * @return the _disableOnGuestSession
     */
    @JsonProperty ("DisableOnGuestSession")
    public boolean isDisableOnGuestSession () {
      return _disableOnGuestSession;
    }

    /**
     * @param _disableOnGuestSession
     *          the _disableOnGuestSession to set
     */
    public void setDisableOnGuestSession (boolean _disableOnGuestSession) {
      this._disableOnGuestSession = _disableOnGuestSession;
    }

    /**
     * @return the _toolTypeSortOrder
     */
    @JsonProperty ("ToolTypeSortOrder")
    public int getToolTypeSortOrder () {
      return _toolTypeSortOrder;
    }

    /**
     * @param _toolTypeSortOrder
     *          the _toolTypeSortOrder to set
     */
    public void setToolTypeSortOrder (int _toolTypeSortOrder) {
      this._toolTypeSortOrder = _toolTypeSortOrder;
    }

    /**
     * @return the _toolValueSortOrder
     */
    @JsonProperty ("ToolValueSortOrder")
    public int getToolValueSortOrder () {
      return _toolValueSortOrder;
    }

    /**
     * @param _toolValueSortOrder
     *          the _toolValueSortOrder to set
     */
    public void setToolValueSortOrder (int _toolValueSortOrder) {
      this._toolValueSortOrder = _toolValueSortOrder;
    }

  }

  public class Dependency
  {
    public String  _contextType;
    public String  _context;
    public String  _ifType;
    public String  _ifValue;
    public String  _thenType;
    public String  _thenValue;
    public boolean _isDefault;

    /**
     * @return the _contextType
     */
    @JsonProperty ("ContextType")
    public String getContextType () {
      return _contextType;
    }

    /**
     * @param _contextType
     *          the _contextType to set
     */
    public void setContextType (String _contextType) {
      this._contextType = _contextType;
    }

    /**
     * @return the _context
     */
    @JsonProperty ("Context")
    public String getContext () {
      return _context;
    }

    /**
     * @param _context
     *          the _context to set
     */
    public void setContext (String _context) {
      this._context = _context;
    }

    /**
     * @return the _ifType
     */
    @JsonProperty ("IfType")
    public String getIfType () {
      return _ifType;
    }

    /**
     * @param _ifType
     *          the _ifType to set
     */
    public void setIfType (String _ifType) {
      this._ifType = _ifType;
    }

    /**
     * @return the _ifValue
     */
    @JsonProperty ("IfValue")
    public String getIfValue () {
      return _ifValue;
    }

    /**
     * @param _ifValue
     *          the _ifValue to set
     */
    public void setIfValue (String _ifValue) {
      this._ifValue = _ifValue;
    }

    /**
     * @return the _thenType
     */
    @JsonProperty ("ThenType")
    public String getThenType () {
      return _thenType;
    }

    /**
     * @param _thenType
     *          the _thenType to set
     */
    public void setThenType (String _thenType) {
      this._thenType = _thenType;
    }

    /**
     * @return the _thenValue
     */
    @JsonProperty ("ThenValue")
    public String getThenValue () {
      return _thenValue;
    }

    /**
     * @param _thenValue
     *          the _thenValue to set
     */
    public void setThenValue (String _thenValue) {
      this._thenValue = _thenValue;
    }

    /**
     * @return the _isDefault
     */
    @JsonProperty ("IsDefault")
    public boolean isDefault () {
      return _isDefault;
    }

    /**
     * @param _isDefault
     *          the _isDefault to set
     */
    public void setIsDefault (boolean _isDefault) {
      this._isDefault = _isDefault;
    }

  }

  public static Data parseData (ColumnResultSet reader) throws ReturnStatusException {
    Data acc = null;
    try {
      acc = new AccList ().new Data ();
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
      // TODO Auto-generated catch block
      e.printStackTrace ();
      throw new ReturnStatusException (e);
    }
    return acc;
  }

  public static Data parseData (DbResultRecord reader) throws ReturnStatusException {
    Data acc = null;
    acc = new AccList ().new Data ();
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
    return acc;
  }

  public static Dependency parseDependency (DbResultRecord record) throws SQLException {
    Dependency dependency = new AccList ().new Dependency ();
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
