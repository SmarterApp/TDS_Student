/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.backing;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.myfaces.shared.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.util.UriComponentsBuilder;
import tds.student.web.StudentContext;
import tds.student.web.handlers.DialogFrameHandler;
import AIR.Common.Web.WebHelper;
import AIR.Common.Web.Session.HttpContext;
import TDS.Shared.Web.BasePage;
import TDS.Shared.Web.client.GenericRestAPIClient;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author mpatel This Managed Bean is used as integration with the
 *         DialogFrame.xhtml.
 */

public class DialogFrameBacking extends BasePage
{
  private static Logger _logger             = LoggerFactory.getLogger (DialogFrameBacking.class);

  private final static String X_FORWARDED_HOST = "x-forwarded-host";
  private final static String X_FORWARDED_PORT = "x-forwarded-port";
  private final static String X_FORWARDED_PROTOCOL = "x-forwarded-proto";

  private String        diaglogFrameContent = "";

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
    diaglogFrameContent = getContent (bankKey, itemKey);

  }

  private String getContent (long bankKey, long itemKey) {
    try {
      HttpServletRequest request = HttpContext.getCurrentContext ().getRequest ();
      final UriComponentsBuilder uriBuilder = UriComponentsBuilder
          .fromHttpUrl(request.getRequestURL ().toString ().replace ("DialogFrame.aspx", "API/DialogFrame.axd/getContent"))
          .queryParam("language", StudentContext.getLanguage())
          .queryParam("bankKey", bankKey)
          .queryParam("itemKey", itemKey);

      if (isNotBlank(request.getHeader(X_FORWARDED_HOST))) {
          uriBuilder.host(request.getHeader(X_FORWARDED_HOST));
      }
      if (isNotBlank(request.getHeader(X_FORWARDED_PORT))) {
          uriBuilder.port(Integer.valueOf(request.getHeader(X_FORWARDED_PORT), 10));
      }
      if (isNotBlank(request.getHeader(X_FORWARDED_PROTOCOL))) {
          uriBuilder.scheme(request.getHeader(X_FORWARDED_PROTOCOL));
      }

      if (_logger.isDebugEnabled ()) {
         _logger.debug ("REST API URL for getting Dialog Frame Content :: " + uriBuilder.build().toUriString());
      }
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
      HttpEntity<Object> httpEntity = new HttpEntity<Object>(headers);
      GenericRestAPIClient restApiClient = new GenericRestAPIClient(uriBuilder.build().toUriString());
      ResponseEntity<String> responseEntity = restApiClient.exchange(HttpMethod.GET, httpEntity, String.class);

      if (responseEntity.getStatusCode () != HttpStatus.OK) {
        throw new RuntimeException ("Failed : HTTP error code : "
            + responseEntity.getStatusCode ());
      }
      if (_logger.isDebugEnabled ()) {
         _logger.debug ("DialogFrame Content :: " + responseEntity.getBody ());
      }

      return responseEntity.getBody ().trim ();
      
    } catch (Exception e) {
      _logger.error (e.getMessage (), e);
      return "Error while getting Content";
    }
  }
}

// TODO Shajib/Shiva: We need to use this class instead, because
// DialogFrameBacking.getContent was a hack. DialogFrameBackingNew is not
// generating iframe in DialogFrame.xhtml, not loading the video though
// generated path is same, the reason is not clear.
class DialogFrameBackingNew extends BasePage
{
  private static Logger _logger             = LoggerFactory.getLogger (DialogFrameBacking.class);

  private String        diaglogFrameContent = "";

  public String getDiaglogFrameContent () {
    return diaglogFrameContent;
  }

  public void setDiaglogFrameContent (String diaglogFrameContent) {
    this.diaglogFrameContent = diaglogFrameContent;
  }

  public DialogFrameBackingNew ()
  {

  }

  @PostConstruct
  public void setOnLoad ()
  {
    diaglogFrameContent = getContent ();
    diaglogFrameContent = StringUtils.replace (diaglogFrameContent, "browser_firefox browserVer_34_0 platform_windows", "browser_java browserVer_1_7 platform_unknown");
    System.err.println ("");
    System.err.println (diaglogFrameContent);
  }

  private String getContent () {
    try {
      DialogFrameHandler dialogFrameHandler = getBean ("dialogFrameHandler", DialogFrameHandler.class);
      return dialogFrameHandler.getDialogFrameContent ();
    } catch (Exception e) {
      _logger.error (e.getMessage (), e);
      return "Error while getting Content";
    } finally {

    }
  }

}
