/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import TDS.Shared.Exceptions.ReadOnlyException;

/**
 * @author temp_rreddy
 * 
 */
public class AccommodationType
{

  @JsonIgnore
  private Accommodations                 _parentAccommodations;
  @JsonManagedReference
  private final List<AccommodationValue> _values = new ArrayList<AccommodationValue> ();
  private String                         _name;
  private boolean                        _isVisible;
  private boolean                        _isSelectable;
  private boolean                        _allowChange;
  private boolean                        _studentControl;
  private String                         _dependsOnToolType;
  private boolean                        _disableOnGuestSession;
  // / <summary>
  // / Get all the values
  // / </summary>

  private List<AccommodationValue>       Values;

  /**
   * @return the _parentAccommodations
   */
  @JsonIgnore
  public Accommodations getParentAccommodations () {
    return _parentAccommodations;
  }

  /**
   * @param _parentAccommodations
   *          the _parentAccommodations to set
   */
  public void setParentAccommodations (Accommodations parentAccommodations) {
    this._parentAccommodations = parentAccommodations;
  }

  /**
   * @return the _name
   */
  @JsonProperty ("name")
  public String getName () {
    return _name;
  }

  /**
   * @param _name
   *          the _name to set
   */
  public void setName (String name) {
    this._name = name;
  }

  /**
   * @return the _isVisible
   */
  @JsonProperty ("isVisible")
  public boolean isVisible () {
    return _isVisible;
  }

  /**
   * @param _isVisible
   *          the _isVisible to set
   */
  public void setIsVisible (boolean isVisible) {
    this._isVisible = isVisible;
  }

  /**
   * @return the _isSelectable
   */
  @JsonProperty ("isSelectable")
  public boolean isSelectable () {
    return _isSelectable;
  }

  /**
   * @param _isSelectable
   *          the _isSelectable to set
   */
  public void setIsSelectable (boolean isSelectable) {
    this._isSelectable = isSelectable;
  }

  /**
   * @return the _allowChange
   */
  @JsonProperty ("allowChange")
  public boolean isAllowChange () {
    return _allowChange;
  }

  /**
   * @param _allowChange
   *          the _allowChange to set
   */
  public void setAllowChange (boolean allowChange) {
    this._allowChange = allowChange;
  }

  /**
   * @return the _studentControl
   */
  @JsonProperty ("studentControl")
  public boolean isStudentControl () {
    return _studentControl;
  }

  /**
   * @param _studentControl
   *          the _studentControl to set
   */
  public void setStudentControl (boolean studentControl) {
    this._studentControl = studentControl;
  }

  /**
   * @return the _dependsOnToolType
   */
  @JsonProperty ("dependsOn")
  public String getDependsOnToolType () {
    return _dependsOnToolType;
  }

  /**
   * @param _dependsOnToolType
   *          the _dependsOnToolType to set
   */
  public void setDependsOnToolType (String dependsOnToolType) {
    this._dependsOnToolType = dependsOnToolType;
  }

  /**
   * @return the _disableOnGuestSession
   */
  @JsonIgnore
  public boolean isDisableOnGuestSession () {
    return _disableOnGuestSession;
  }

  /**
   * @param _disableOnGuestSession
   *          the _disableOnGuestSession to set
   */
  public void setDisableOnGuestSession (boolean disableOnGuestSession) {
    this._disableOnGuestSession = disableOnGuestSession;
  }

  protected AccommodationType (Accommodations parentAccommodations, String name, boolean isVisible, boolean isSelectable, boolean allowChange, boolean studentControl, String dependsOnToolType,
      boolean disableOnGuestSession) {
    _parentAccommodations = parentAccommodations;
    _name = name;
    _isVisible = isVisible;
    _isSelectable = isSelectable;
    _allowChange = allowChange;
    _studentControl = studentControl;
    _dependsOnToolType = dependsOnToolType;
    _disableOnGuestSession = disableOnGuestSession;
  }

  // / <summary>
  // / This creates a new accommodation value within this types value
  // collection and returns it.
  // / </summary>

  public AccommodationValue createValue (String code, String value, boolean isDefault, boolean allowCombine) throws Exception {
    // TODO Exceptions
    if (_parentAccommodations.isReadOnly ())
      throw new ReadOnlyException ("Accommodations is read only.");

    if (_parentAccommodations.getValuesLookup ().containsKey (code)) {
      throw new Exception ("Accommodation code already exists.");
    }

    AccommodationValue accValue = new AccommodationValue (this, code, value, isDefault, allowCombine);
    addValue (accValue);
    return accValue;
  }

  // / <summary>
  // / Internal function for adding a value to all the various data
  // structures.
  // / </summary>
  private boolean addValue (AccommodationValue accValue) {
    // check if acc code already exists
    // DO NOT USE: if (HasValue(accValue.Code)) return;
    if (_parentAccommodations.getValuesLookup ().containsKey (accValue.getCode ()))
      return false;

    // add value for lookup
    _values.add (accValue);
    _parentAccommodations.getValuesLookup ().put (accValue.getCode (), accValue);

    // add split values for comparison
    for (String code : accValue.getCodes ()) {
      _parentAccommodations.getValuesHash ().add (code);
    }

    return true;
  }

  // / <summary>
  // / Remove an accommodation value from this type and accommodation
  // collection.
  // / </summary>
  public void remove (AccommodationValue value) throws ReadOnlyException {
    if (_parentAccommodations.isReadOnly ())
      throw new ReadOnlyException ("Accommodations is read only.");

    // remove from type values
    _values.remove (value);

    // remove from lookup
    _parentAccommodations.getValuesLookup ().remove (value.getCode ());

    // remove from hash
    for (String code : value.getCodes ()) {
      _parentAccommodations.getValuesHash ().remove (code);
    }
  }

  @JsonProperty ("values")
  public Collection<AccommodationValue> getValues () {
    return _values;
  }

  // / <summary>
  // / Get the first value
  // / </summary>
  @JsonIgnore
  public AccommodationValue getFirstValue () {
    return Values.get (0);
    // TODO
    // Values.FirstOrDefault ();
  }

  // / <summary>
  // / Get the default value.
  // / </summary>
  @JsonIgnore
  public AccommodationValue getDefault () {
    AccommodationValue tempList = (AccommodationValue) CollectionUtils.find (getValues (), new Predicate ()
    {
      @Override
      public boolean evaluate (Object object) {
        if (((AccommodationValue) object).getIsDefault ())
          return true;
        return false;
      }
    });
    return tempList;
  }

  /*
   * /// <summary> /// Gets all the selected values. /// </summary> ///
   * <returns></returns> public IEnumerable<Value> GetSelected() { return
   * Values.Where(value => value.IsSelected); }
   */

  public boolean equals (AccommodationType other) {
    /*
     * if (ReferenceEquals(null, other)) return false; if (ReferenceEquals(this,
     * other)) return true; return equals(other.getName(), Name);
     */
    if (other == null)
      return false;
    if (other == this)
      return true;
    return other.getName ().equalsIgnoreCase (_name);

  }

  @Override
  public boolean equals (Object obj) {
    // TODO
    /*
     * if (ReferenceEquals(null, obj)) return false; if (ReferenceEquals(this,
     * obj)) return true; if (obj.GetType() != typeof(AccommodationType)) return
     * false; return equals((AccommodationType) obj);
     */

    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (!(obj instanceof AccommodationType))
      return false;
    return equals ((AccommodationType) obj);

  }

  @Override
  public int hashCode () {
    return (_name != null ? _name.hashCode () : 0);
  }

  @Override
  public String toString () {
    return _name;
  }

}
