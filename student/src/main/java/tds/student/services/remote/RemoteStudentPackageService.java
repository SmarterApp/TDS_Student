/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.remote;

import javax.annotation.PostConstruct;

import org.opentestsystem.shared.trapi.ITrClient;
import org.opentestsystem.shared.trapi.data.TestStatus;
import org.opentestsystem.shared.trapi.exception.TrApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import tds.student.services.abstractions.IStudentPackageService;
import tds.student.sql.abstractions.IOpportunityRepository;
import AIR.Common.Utilities.UrlEncoderDecoderUtils;

@Component
@Scope ("singleton")
@EnableAsync
public class RemoteStudentPackageService implements IStudentPackageService
{

  private static final Logger _logger   = LoggerFactory.getLogger (RemoteStudentPackageService.class);

  @Autowired
  private ITrClient           _trClient = null;
  
  @Autowired
  private IOpportunityRepository _oppRepository;

  @Value ("${StateCode}")
  private String              _stateCode;

  @PostConstruct
  private void init() {
    _stateCode = _stateCode.toUpperCase ();
  }
  
  @Override
  public String getStudentPackage (String ssidOrExternalSsid, boolean isSsid) {
    if (ssidOrExternalSsid.equalsIgnoreCase ("guest")) {
       return null;
    }
    ssidOrExternalSsid = UrlEncoderDecoderUtils.encode (ssidOrExternalSsid);

    // SB-326
    ssidOrExternalSsid = ssidOrExternalSsid.toUpperCase ();
 
    String urlPath = null;
    try {
      String ssidOrExternalSsidParam = (isSsid) ? "ssid" : "externalId";
      urlPath = "studentpackage?" + ssidOrExternalSsidParam + "=" + ssidOrExternalSsid + "&stateabbreviation=" + _stateCode;
      return _trClient.getPackage (urlPath);
    } catch (Exception e) {
       _logger.error (e.getMessage (), e);
       return null;
    }

  }

  @Override
  @Async
  public void sendTestStatus ( TestStatus testStatus ) {
    try {
      testStatus.setStateAbbreviation (_stateCode.toUpperCase ());
      TestStatus[] testStatuses = new TestStatus[1];
      testStatuses[0] = testStatus;
      _trClient.put ("testStatus", testStatuses);
    } catch (TrApiException e) {
       if (!e.isErrorExempted ()) {
          _logger.warn ("RemoteStudentPackageService.sendTestStatus: " + e.getErrorMessage ());
       }
    } catch (Exception e) {
      _logger.warn ("RemoteStudentPackageService.sendTestStatus: " + e.getMessage ());
    }
    
  }

 

}
