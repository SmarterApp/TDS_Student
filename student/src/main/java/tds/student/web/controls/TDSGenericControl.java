/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.controls;

import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;

public abstract class TDSGenericControl extends UINamingContainer
{
  // TODO Shiva/Sajib it seems we are not using the _tag. Remove it
  // for optimization purpose only once it has been verified that this is
  // indeed not in use.
  private String _tag = null;

  public String getTag ()
  {
    return _tag;
  }

  public void setTag (String value)
  {
    _tag = value;
  }

  protected TDSGenericControl ()
  {
    super ();
  }

  protected SectionControl GetSectionControl ()
  {
    UIComponent control = this;

    while ((control = control.getParent ()) != null)
    {
      if (control instanceof SectionControl)
        return (SectionControl) control;
    }

    return null;
  }
}
