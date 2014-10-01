/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.controls;

import java.io.IOException;

import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

/*
 * TODO Shiva/Sajib This probably is not used anywhere. May be we should remove
 * it.
 */
public class SectionControl extends UINamingContainer
{  
  
  @Override
  public String getFamily () {
    return "javax.faces.NamingContainer";
  }
  
}
