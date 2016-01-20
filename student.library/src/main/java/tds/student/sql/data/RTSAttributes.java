/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.time.DateTime;

/**
 * @author temp_rreddy
 * 
 */
public class RTSAttributes extends ArrayList<RTSAttribute>
{
  private static Logger       _logger       = LoggerFactory.getLogger (RTSAttributes.class);
  
  public RTSAttribute get (final String id) {
    // TODO
    RTSAttribute rtsAttribute = (RTSAttribute) CollectionUtils.find (this, new Predicate ()
    {
      @Override
      public boolean evaluate (Object object) {
        RTSAttribute rtsAttr = (RTSAttribute) object;
        return rtsAttr.getId ().equalsIgnoreCase (id);
      }
    });
    return rtsAttribute;
    // return (RTSAttribute) CollectionUtils.select (this, new PositionPredicate
    // (id));
    // return this.Find(a => a.ID == id);
  }

  public String getValue (String id) {
    RTSAttribute attribute = get (id);

    if (attribute != null && attribute.getValue () != null) {
      return attribute.getValue ();
    }

    return null;
  }

  public Date getDateTime (String id) {
    String value = getValue (id);
    return value != null ? parseBirthday (value) : DateTime.getMinValue ();
  }

  // TODO Ravi/Shiva : does not seem to be in use.
  /*
   * public T GetValue<T>(String id) { String value = GetValue(id); return value
   * != null ? value.ConvertTo<T>() : default(T); }
   */

  private static Date parseBirthday (String value) {
    if (StringUtils.isEmpty (value))
      return DateTime.getMinValue ();

    Date birthday = null;
    try {
      if (value.contains ("/")) {
        // TODO
        // for PT
        // birthday = DateTime.ParseExact(value, "M/d/yyyy", null);

        birthday = (new SimpleDateFormat ("M/d/yyyy")).parse (value);

      } else {
        // for OP
        // birthday = DateTime.ParseExact(value, "MMddyyyy", null);
        birthday = (new SimpleDateFormat ("MMddyyyy")).parse (value);
      }
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      _logger.error(e.getMessage (), e);
      return null;
    }
    return birthday;
  }

  class PositionPredicate implements Predicate
  {
    private String _position = null;

    public PositionPredicate (String position) {
      this._position = position;
    }

    public boolean evaluate (Object object) {
      RTSAttribute data = (RTSAttribute) object;
      return data.getId ().equalsIgnoreCase (_position);
    }
  }
}
