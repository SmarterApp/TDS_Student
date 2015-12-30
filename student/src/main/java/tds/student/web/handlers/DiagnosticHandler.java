/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DiagnosticHandler
{

  private boolean isReusable;

  @RequestMapping (value = "DiagnosticHandler.axd/loginStudent/")
  @ResponseBody
  public String test () {
    return "Test Successfull";
  }

  public boolean getIsReusable () {
    return true;
  }

  @RequestMapping (value = "Diagnostics.axd")
  @ResponseBody
  void handleDiagnostics (HttpServletResponse response, HttpServletRequest request)
  {
    if ("GET".equals (request.getMethod ()))
    {
      String dummyContentSize = request.getParameter ("size");
      if (dummyContentSize != null)
      {
        response.setContentType ("application/x-dummybytes");
        try {
          response.getWriter ().write ("<html><head></head><body>");
          String s = "";
          for (int i = 0; i < Integer.parseInt (dummyContentSize); i++)
            s = s + "a";
          response.getWriter ().write (s);
          response.getWriter ().write ("</body></html>");
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace ();
        }
      }
    }
    else
      return;
  }

}
