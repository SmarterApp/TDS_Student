/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tds.itemrenderer.data.AccLookup;
import tds.student.services.PrintService;
import tds.student.services.abstractions.IContentService;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.TestOpportunity;
import tds.student.web.StudentSettings;
import AIR.test.framework.AbstractTest;

/**
 * @author jmambo
 * 
 */
public class ContentHandlerTest extends AbstractTest
{

  @InjectMocks
  @Spy
  private ContentHandler _contentHandler;

  @Mock
  IResponseService       responseService;

  @Mock
  StudentSettings        _studentSettings;

  @Mock
  IContentService        _contentService;

  @Mock
  PrintService           _printService;

  private MockMvc        _mockMvc;

  @Before
  public void setUp () {
    MockitoAnnotations.initMocks (this);
    _mockMvc = MockMvcBuilders.standaloneSetup (_contentHandler).build ();
  }

  @Test
  public void testLoadGroup () throws Exception {
    final TestOpportunity testOpportunity = Mockito.mock (TestOpportunity.class);
    final AccLookup accLookup = Mockito.mock (AccLookup.class);

    Mockito.doAnswer (new Answer<TestOpportunity> ()
    {
      @Override
      public TestOpportunity answer (InvocationOnMock inv) throws Throwable {
        return testOpportunity;
      }
    }).when (_contentHandler).getTestOpportunity ();

    Mockito.doAnswer (new Answer<AccLookup> ()
    {
      @Override
      public AccLookup answer (InvocationOnMock inv) throws Throwable {
        return accLookup;
      }
    }).when (_contentHandler).getAccLookup ();

    _mockMvc.perform (get ("/Content.axd/loadGroup?groupID=I-74-630&page=1&datecreated=2014-03-13%2018:43:42.817&new=true&attempt=1"))
        .andExpect (status ().isOk ());

  }

}
