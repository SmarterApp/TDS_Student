/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.backing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.web.StudentContext;
import AIR.Common.Web.WebHelper;
import AIR.Common.Web.Session.HttpContext;
import TDS.Shared.Web.BasePage;

/**
 * @author mpatel
 * This Managed Bean is used as integration with the DialogFrame.xhtml.
 * DialogFrameBacking calls Rest API for getting content for rendering DialogFrame.
 * 
 */
public class DialogFrameBacking extends BasePage
{
  private static Logger       _logger      = LoggerFactory.getLogger (DialogFrameBacking.class);

  private String diaglogFrameContent = "";
  
  
  public String getDiaglogFrameContent () {
    return diaglogFrameContent;
  }

  public void setDiaglogFrameContent (String diaglogFrameContent) {
    this.diaglogFrameContent = diaglogFrameContent;
  }

  public DialogFrameBacking ()
  {
    
  }

  @PostConstruct
  public void setOnLoad ()
  {
    // get query String params
    long bankKey = WebHelper.getQueryValueLong ("bankKey");
    long itemKey = WebHelper.getQueryValueLong ("itemKey");
    diaglogFrameContent =  getContent (bankKey, itemKey);
  }

  
  private String  getContent(long bankKey,long itemKey)  {
    HttpURLConnection conn   =null;
    try {
        HttpServletRequest request = HttpContext.getCurrentContext ().getRequest ();
        StringBuilder urlSB = new StringBuilder();
        urlSB.append (request.getRequestURL ().toString ().replace ("DialogFrame.xhtml", "API/DialogFrame.axd/getContent"));
        urlSB.append ("?language=").append (StudentContext.getLanguage ());
        urlSB.append ("&bankKey=").append (bankKey);
        urlSB.append ("&itemKey=").append (itemKey);
        _logger.debug ("REST API URL for getting Dialog Frame Content :: "+urlSB.toString ());
        URL url = new URL(urlSB.toString ());
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");
     
        if (conn.getResponseCode() != 200) {
          throw new RuntimeException("Failed : HTTP error code : "
              + conn.getResponseCode());
        }
     
        BufferedReader br = new BufferedReader(new InputStreamReader(
          (conn.getInputStream())));
        String output;
        StringBuilder sb = new StringBuilder();
        while ((output = br.readLine()) != null) {
          sb.append (output);
        }
        _logger.debug  ("DialogFrame Content :: "+sb.toString ());
        return sb.toString ();
      }  catch (Exception e) {
        _logger.error (e.getMessage (),e);
        return "Error while getting Content";
      } finally {
        if(conn!=null) {
          conn.disconnect();
        }
      }
  }
  
}
