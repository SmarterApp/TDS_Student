/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.backing;

import javax.annotation.PostConstruct;
import tds.itempreview.Page;
import tds.student.services.MessageService;
import tds.student.web.StudentContext;
import tds.student.web.StudentSettings;
import AIR.Common.Web.FacesContextHelper;
import AIR.Common.Web.WebHelper;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Messages.IMessageService;

public class NotificationBacking extends Page
{
  IMessageService _messageService;

  String          _context = (WebHelper.getQueryString ("context") != null) ? WebHelper.getQueryString ("context") : "Notification.xthml";

  StudentSettings _studentSettings;

  private boolean _showBtnNotificationLogin;
  private String  _labelErrorText;
  private String  _headerText;
  private String  _labelMessage;

  public boolean getShowBtnNotificationLogin ()
  {
    return _showBtnNotificationLogin;
  }

  public String getLabelErrorText ()
  {
    return _labelErrorText;
  }

  public String getHeaderText ()
  {
    return _headerText;
  }

  public String getLabelMessage ()
  {
    return _labelMessage;
  }

  @PostConstruct
  public void onInit ()
  {
    _messageService = FacesContextHelper.getBean ("messageService", MessageService.class);
    _studentSettings = FacesContextHelper.getBean ("studentSettings", StudentSettings.class);

    // set header
    setMessageHeader ();

    // set message
    setMessageText ();

    // show login button
    _showBtnNotificationLogin = WebHelper.getQueryBoolean ("login", true);

    // set activityID
    _labelErrorText = WebHelper.getQueryString ("activityId");
  }

  private void setMessageHeader ()
  {
    String language = StudentContext.getLanguage ();
    _headerText = (WebHelper.getQueryString ("header") != null) ? WebHelper.getQueryString ("header") : "Notification.Label.Problem";
    try {
      _headerText = _messageService.get (_context, language, _headerText);
    } catch (Exception e) {
    }
  }

  private void setMessageText ()
  {
    _labelMessage = (WebHelper.getQueryString ("message") != null) ? WebHelper.getQueryString ("message") : _studentSettings.getGenericErrorMessage ();
    String messageKey = WebHelper.getQueryString ("messageKey");

    // check if there is a message key
    if (messageKey != null)
    {
      // try to translate message key
      String language = StudentContext.getLanguage ();
      try {
        _labelMessage = _messageService.get (_context, language, messageKey);
      } catch (ReturnStatusException e) {
      }
    }
  }
}
