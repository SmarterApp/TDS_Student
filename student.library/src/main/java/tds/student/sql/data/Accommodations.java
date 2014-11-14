/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import tds.itemrenderer.data.AccLookup;
import TDS.Shared.Exceptions.ReadOnlyException;

/**
 * @author temp_rreddy
 * 
 */
public class Accommodations
{
  private static final Accommodations               _empty        = new Accommodations (-1);

  private final List<AccommodationType>             _types        = new ArrayList<AccommodationType> ();
  private final HashMap<String, AccommodationType>  _typesLookup  = new HashMap<String, AccommodationType> ();
  private final HashMap<String, AccommodationValue> _valuesLookup = new HashMap<String, AccommodationValue> ();
  private final HashSet<String>                     _valuesHash   = new HashSet<String> ();
  private boolean                                   _readOnly;

  private List<AccommodationDependency>             _dependencies = new ArrayList<AccommodationDependency> ();

  @JsonIgnore
  public static Accommodations getEmpty () {
    return _empty;
  }

  private int    _position;
  private String _id;
  private String _label;

  /**
   * @return the _position
   */
  @JsonProperty ("position")
  public int getPosition () {
    return _position;
  }

  /**
   * @param _position
   *          the _position to set
   */
  public void setPosition (int _position) {
    this._position = _position;
  }

  /**
   * @return the _id
   */
  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  /**
   * @param _id
   *          the _id to set
   */
  public void setId (String _id) {
    this._id = _id;
  }

  /**
   * @return the _label
   */
  @JsonProperty ("label")
  public String getLabel () {
    return _label;
  }

  /**
   * @param _label
   *          the _label to set
   */
  public void setLabel (String _label) {
    this._label = _label;
  }

  @JsonProperty ("types")
  public List<AccommodationType> getTypes () {
    return _types;
  }

  @JsonIgnore
  public HashMap<String, AccommodationValue> getValuesLookup () {
    return _valuesLookup;
  }

  /**
   * @return the _readOnly
   */
  @JsonIgnore
  public boolean isReadOnly () {
    return _readOnly;
  }

  /**
   * @param _readOnly
   *          the _readOnly to set
   */
  public void setReadOnly (boolean _readOnly) {
    this._readOnly = _readOnly;
  }

  /**
   * @return the _dependencies
   */
  @JsonProperty ("dependencies")
  public List<AccommodationDependency> getDependencies () {
    return _dependencies;
  }

  @JsonIgnore
  public HashSet<String> getValuesHash () {
    return _valuesHash;
  }

  /**
   * @param _dependencies
   *          the _dependencies to set
   */
  private void setDependencies (List<AccommodationDependency> _dependencies) {
    this._dependencies = _dependencies;
  }

  Accommodations () {
    _empty._readOnly = true;
  }

  public Accommodations (int position) {
    _position = position;
    _dependencies = new ArrayList<AccommodationDependency> ();
  }

  public Accommodations (int position, String id, String label) {
    // TODO
    this._position = position;
    _id = id;
    _label = label;
  }

  public void AddDependency (String ifType, String ifValue, String thenType, String thenValue, boolean isDefault) {
    AccommodationDependency dependency = new AccommodationDependency (ifType, ifValue, thenType, thenValue, isDefault);
    _dependencies.add (dependency);
  }

  // / <summary>
  // / Check if a type name exists.
  // / </summary>
  @JsonIgnore
  public boolean hasType (String type) {
    return _typesLookup.containsKey (type);
  }

  // / <summary>
  // / Get an accommodation by type name.
  // / </summary>
  @JsonIgnore
  public AccommodationType getType (String type) {
    AccommodationType accType;
    accType = _typesLookup.get (type);
    return accType;
  }

  // / <summary>
  // / Checks if a accommodtion value exists by one of its split codes.
  // / </summary>
  @JsonIgnore
  public boolean hasValue (String code) {
    return _valuesHash.contains (code);
  }

  // / <summary>
  // / Get a accommodation value by the combined code.
  // / </summary>
  @JsonIgnore
  public AccommodationValue getValue (String code) {
    if (StringUtils.isEmpty (code))
      return null;

    AccommodationValue accValue = _valuesLookup.get (code);
    return accValue;
  }

  // / <summary>
  // / Get the first accommodation code for a type
  // / </summary>
  @JsonIgnore
  public String getCode (String type) {
    AccommodationType accType = getType (type);
    return (accType != null) ? accType.getFirstValue ().getCode () : null;
  }

  public void selectCodes (List<String> codes) {
    List<AccommodationValue> existingValues = getValues (codes);
    for (AccommodationValue value : existingValues)
      value.setIsSelected (true);
  }

  // / <summary>
  // / This creates a new accommodation type within this collection and
  // returns
  // it.
  // / </summary>
  @JsonIgnore
  public AccommodationType createType (String name, boolean isVisible, boolean isSelectable, boolean allowChange, boolean studentControl, String dependsOnToolType, boolean disableOnGuestSession)
      throws ReadOnlyException {
    if (_readOnly)
      throw new ReadOnlyException ("Accommodations is read only.");

    AccommodationType accType = new AccommodationType (this, name, isVisible, isSelectable, allowChange, studentControl, dependsOnToolType, disableOnGuestSession);
    _types.add (accType);
    _typesLookup.put (accType.getName (), accType);

    return accType;
  }

  // / <summary>
  // / Add an accommodation type and value.
  // / </summary>
  @JsonIgnore
  public AccommodationValue create (String type, String code, String value, boolean isVisible, boolean isSelectable, boolean allowChange, boolean studentControl, String dependsOnToolType,
      boolean disableOnGuestSession, boolean isDefault, boolean allowCombine, boolean isSelected) throws ReadOnlyException {
    if (StringUtils.isEmpty (type) || StringUtils.isEmpty (code))
      return null;
    if (StringUtils.isEmpty (value))
      value = code;

    AccommodationType myType = getType (type);
    AccommodationType accType = myType != null ? myType : createType (type, isVisible, isSelectable, allowChange, studentControl, dependsOnToolType, disableOnGuestSession);

    // check to make sure the value doesn't already exis
    AccommodationValue accValue = getValue (code);

    if (accValue == null)
      try {
        accValue = accType.createValue (code, value, isDefault, allowCombine);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
        throw new ReadOnlyException ("Accommodations is read only.");
      }
    else
      accValue.setIsDefault (isDefault);
    accValue.setIsSelected (isSelected);
    return accValue;
  }

  @JsonIgnore
  public AccommodationValue create (String type, String code) throws ReadOnlyException {
    return create (type, code, code, true, true, true, true, null, false, false, false, false);
  }

  // / <summary>
  // / Modifies the current accommodations object to contain the value that
  // / is present in the specified value.
  // / </summary>
  private void cloneValue (AccommodationValue accValue) throws ReadOnlyException {
    AccommodationType accType = accValue.getParentType ();

    this.create (accType.getName (), accValue.getCode (), accValue.getName (), accType.isVisible (), accType.isSelectable (), accType.isAllowChange (), accType.isStudentControl (),
        accType.getDependsOnToolType (), accType.isDisableOnGuestSession (), accValue.getIsDefault (), accValue.isAllowCombine (), accValue.getIsSelected ());
  }

  // / <summary>
  // / Modifies the current accommodations object to contain all values that
  // / are present in both itself and in the specified values.
  // / </summary>
  // / <param name="values"></param>
  private void cloneValues (List<AccommodationValue> values) throws ReadOnlyException {
    // TODO
    for (AccommodationValue accValue : values) {
      cloneValue (accValue);
    }
  }

  // / <summary>
  // / Sorts the accommodations by type name.
  // / </summary>
  public void sort () {
    Collections.sort (_types, new Comparator<AccommodationType> ()
    {
      public int compare (AccommodationType t1, AccommodationType t2) {
        return t1.getName ().compareTo (t2.getName ());
      }
    });
  }

  // / <summary>
  // / Get a list of all the values that are set as default.
  // / </summary>
  @SuppressWarnings ("unchecked")
  @JsonIgnore
  public List<AccommodationValue> getDefaults () {
    // TODO Shiva/Ravi Verify below Linq query has been properly converted.
    /*
     * return from type in Types from value in type.Values where value.IsDefault
     * select value;
     */
    List<AccommodationValue> defaultAccommodationValues = new ArrayList<AccommodationValue> ();

    for (AccommodationType type : getTypes ()) {
      defaultAccommodationValues.addAll (CollectionUtils.select (type.getValues (), new Predicate ()
      {
        public boolean evaluate (Object o) {
          AccommodationValue v = (AccommodationValue) o;
          return v.getIsDefault ();
        }
      }));

    }
    return defaultAccommodationValues;

  }

  // / <summary>
  // / Set a list of values as the defaults.
  // / </summary>
  public void setDefaults (List<String> codes) {
    for (AccommodationValue value : getValues (codes)) {
      value.setIsDefault (true);
    }
  }

  // / <summary>
  // / Get all the values for the codes passed in.
  // / </summary>
  private List<AccommodationValue> getValues (List<String> codes) {

    // TODO Ravi/Shiva: Please unit test this.
    // return codes.Select(code => GetValue(code)).Where(accValue => accValue !
    // null);

    List<AccommodationValue> tempList = ((List<AccommodationValue>) CollectionUtils.collect (codes, new Transformer ()
    {
      @Override
      public Object transform (Object input) {
        return getValue (input.toString ());
      }
    }, new java.util.ArrayList<AccommodationValue> ()));
    CollectionUtils.filter (tempList, new Predicate ()
    {
      @Override
      public boolean evaluate (Object object) {
        if (object != null)
          return true;
        return false;
      }
    });
    return tempList;
  }

  // / <summary>
  // / Make a copy of the current accommodations data.
  // @param filter is a Predicate which accept a parameter of type
  // AccommodationType
  // / </summary>
  public Accommodations clone (Predicate filter) throws ReadOnlyException {
    // create new empty accommodations and merge them the current
    // accommodations
    Accommodations accommodations = new Accommodations (this._position, this._id, this._label);

    // copy all existing dependencies
    accommodations.setDependencies (this._dependencies);

    // copy existing accs and apply filter if provided
    accommodations.unionWith (this, filter);
    return accommodations;
  }

  // / <summary>
  // / Make a copy of the current accommodations data.
  // / </summary>
  public Accommodations clone () {
    Accommodations accommodations = null;
    try {
      accommodations = clone (null);
    } catch (ReadOnlyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return accommodations;
  }

  // / <summary>
  // / Modifies the current accommodations object to contain all values that
  // are
  // / present in both itself and in the specified accommodations.
  // @param filter is a Predicate which accepts a parameter of type
  // AccommodationType
  // / </summary>
  public void unionWith (Accommodations other, Predicate filter) throws ReadOnlyException {
    if (other == null)
      return;

    Collection<AccommodationType> otherTypes = other.getTypes ();

    if (filter != null) {
      otherTypes = CollectionUtils.select (otherTypes, filter);
    }

    for (AccommodationType otherType : otherTypes) {
      // add all the values to the current accommodations
      cloneValues ((List<AccommodationValue>) otherType.getValues ());
    }
  }

  public void unionWith (Accommodations other) throws ReadOnlyException {
    unionWith (other, null);
  }

  // / <summary>
  // / Modifies the current accommodations object to replace or add the types
  // that
  // / are present in itself with the ones in the specified accommodations.
  // / </summary>
  public void replaceWith (Accommodations other, Predicate filter) throws ReadOnlyException {
    if (other == null)
      return;

    // replace the existing id/label with the other accommodations
    this._id = other._id;
    this._label = other._label;

    Collection<AccommodationType> otherTypes = other.getTypes ();

    if (filter != null) {
      otherTypes = CollectionUtils.select (otherTypes, filter);
    }

    // replace all the existing types with the types in the other
    // accommodations
    for (AccommodationType otherType : otherTypes) {
      // check if this type already exists
      AccommodationType existingType = this.getType (otherType.getName ());

      if (existingType != null) {
        // remove all current values from this type
        // TODO
        List<AccommodationValue> existingValues = (List<AccommodationValue>) existingType.getValues (); // clone
        // list

        for (AccommodationValue existingValue : existingValues) {
          existingType.remove (existingValue);
        }
      }

      // add all the values to the current accommodations
      cloneValues ((List<AccommodationValue>) otherType.getValues ());
    }
  }

  public void replaceWith (Accommodations other) throws ReadOnlyException {
    replaceWith (other, null);
  }

  // TODO
  public Accommodations getSubset (List<String> codes, Predicate filter) throws ReadOnlyException {
    Accommodations accommodations = new Accommodations (this._position, this._id, this._label);
    List<AccommodationValue> existingValues = getValues (codes);

    if (filter == null)
      accommodations.cloneValues (existingValues);
    else
      accommodations = (Accommodations) CollectionUtils.select (existingValues, filter);

    return accommodations;
  }

  // / <summary>
  // / Get a subset of the current accommodations with just the values of the
  // codes passed in.
  // / </summary>
  // / <param name="codes">Value codes</param>
  public Accommodations getSubset (List<String> codes) throws ReadOnlyException {
    return getSubset (codes, null);
  }

  // / <summary>
  // / Creates a JSON javascript data structure
  // / </summary>
  public String toJson () {
    StringBuilder json = new StringBuilder ();

    json.append ("[");

    for (int i = 0; i < _types.size (); i++) {
      AccommodationType accType = _types.get (i);
      if (i > 0)
        json.append (",");
      json.append ("{ ");

      json.append ("name: '{0}'," + accType.getName ());
      // json.AppendFormat("label: '{0}',", accType.DisplayLabel);
      // TODO
      json.append ("isVisible: {0}," + new Boolean (accType.isVisible ()).toString ().toLowerCase ());
      json.append ("isSelectable: {0}," + new Boolean (accType.isSelectable ()).toString ().toLowerCase ());
      json.append ("allowChange: {0}," + new Boolean (accType.isAllowChange ()).toString ().toLowerCase ());
      json.append ("studentControl: {0}," + new Boolean (accType.isStudentControl ()).toString ().toLowerCase ());

      // TODO
      List<AccommodationValue> values = (List<AccommodationValue>) accType.getValues ();

      json.append ("values: [");
      for (int j = 0; j < values.size (); j++) {
        AccommodationValue accValue = values.get (j);
        if (j > 0)
          json.append (", ");
        json.append ("{ ");
        json.append ("name: '{0}', " + accValue.getName ());
        // json.AppendFormat("label: '{0}', ", accValue.DisplayLabel);
        json.append ("code: '{0}', " + accValue.getCode ());
        json.append ("isDefault: {0}," + new Boolean (accValue.getIsDefault ()).toString ().toLowerCase ());
        json.append ("allowCombine: {0}" + new Boolean (accValue.isAllowCombine ()).toString ().toLowerCase ());
        json.append (" }");
      }
      json.append ("]");

      json.append ("}");
    }

    json.append ("]");

    return json.toString ();
  }

  // / <summary>
  // / Get a pipe delimited list of codes.
  // / </summary>
  // / <returns>TDS_CC0;TDS_F_S12;TDS_FT_San-Serif</returns>
  public String toPipeDelimited (String seperator) {
    List<String> codes = new ArrayList<String> ();

    for (AccommodationType accType : _types) {
      for (AccommodationValue accValue : accType.getValues ()) {
        codes.add (accValue.getCode ());
      }
    }
    // TODO
    return seperator + codes.toArray ();
    // return String.Join(seperator, codes.toArray ());
  }

  // / <summary>
  // / Parse a pipe delimited list of codes
  // / </summary>
  // / <param
  // name="accommodationString">TDS_CC0;TDS_F_S12;TDS_FT_San-Serif</param>
  public Accommodations parsePipeDelimited (String accommodationString, String seperator) throws ReadOnlyException {
    // check if the name value collection is empty
    if (StringUtils.isEmpty (accommodationString))
      return null;
    // TODO
    // String[] codes = accommodationString.Split (new String[] { seperator },
    // StringSplitOptions.RemoveEmptyEntries);
    String[] codes = accommodationString.trim ().split (seperator);
    return getSubset (Arrays.asList (codes));
  }

  @JsonIgnore
  public AccLookup getCollection () {
    AccLookup lookup = new AccLookup (this._position, this._id);
    // TODO
    for (AccommodationType accType : _types) {
      for (AccommodationValue accValue : accType.getValues ()) {
        String[] strArray = new String[] { accValue.getCode () };
        lookup.add (accType.getName (), strArray);
      }
    }

    return lookup;
  }
}
