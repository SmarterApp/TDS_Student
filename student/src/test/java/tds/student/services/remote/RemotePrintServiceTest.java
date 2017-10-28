/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSAttachment;
import tds.itemrenderer.data.ITSContent;
import tds.itemrenderer.data.ITSDocument;
import tds.student.services.abstractions.IContentService;
import tds.student.services.abstractions.PrintService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.data.TestConfig;
import tds.student.sql.repository.remote.ExamRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemotePrintServiceTest {
  @Mock
  private PrintService legacyPrintService;

  @Mock
  private ExamRepository mockExamRepository;

  @Mock
  private IContentService mockContentService;

  private PrintService service;

  @Captor
  private ArgumentCaptor<ExamPrintRequest> examPrintRequestCaptor;

  private final OpportunityInstance mockOpportunityInstance = new OpportunityInstance(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

  @Before
  public void setUp() {
    service = new RemotePrintService(legacyPrintService, true, false, mockExamRepository, mockContentService);
  }

  @Test
  public void shouldPrintPassageSuccessfullyMultiItem() throws ReturnStatusException {
    final String requestParams = "request";

    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setStimulusFile("/path/to/stim");
    opportunityItem1.setPosition(1);
    final OpportunityItem opportunityItem2 = new OpportunityItem();
    opportunityItem2.setPosition(2);

    final PageGroup pageGroup = new PageGroup(opportunityItem1);
    pageGroup.add(new ItemResponse(opportunityItem1));
    pageGroup.add(new ItemResponse(opportunityItem2));

    boolean isSuccessful = service.printPassage(mockOpportunityInstance, pageGroup, requestParams);
    assertThat(isSuccessful).isTrue();

    verify(mockExamRepository).createPrintRequest(examPrintRequestCaptor.capture());
    ExamPrintRequest printRequest = examPrintRequestCaptor.getValue();

    assertThat(printRequest.getExamId()).isEqualTo(mockOpportunityInstance.getExamId());
    assertThat(printRequest.getSessionId()).isEqualTo(mockOpportunityInstance.getSessionKey());
    assertThat(printRequest.getId()).isNotNull();
    assertThat(printRequest.getDescription()).isEqualTo("Passage for Items 1-2");
    assertThat(printRequest.getItemPosition()).isEqualTo(0);
    assertThat(printRequest.getPagePosition()).isEqualTo(pageGroup.getNumber());
    assertThat(printRequest.getParameters()).isEqualTo(requestParams);
    assertThat(printRequest.getChangedAt()).isNull();
    assertThat(printRequest.getReasonDenied()).isNull();
    assertThat(printRequest.getValue()).isEqualTo("/path/to/stim");
    assertThat(printRequest.getType()).isEqualTo(ExamPrintRequest.REQUEST_TYPE_PRINT_PASSAGE);
  }

  @Test
  public void shouldReturnFalseForEmptyPageGroupPrintPassage() throws ReturnStatusException {
    final String requestParams = "request";
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    final PageGroup pageGroup = new PageGroup(opportunityItem1);

    boolean isSuccessful = service.printPassage(mockOpportunityInstance, pageGroup, requestParams);
    assertThat(isSuccessful).isFalse();
    verify(mockExamRepository, never()).createPrintRequest(isA(ExamPrintRequest.class));
  }

  @Test
  public void shouldReturnFalseForEmptyRequestValPrintPassage() throws ReturnStatusException {
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    final PageGroup pageGroup = new PageGroup(opportunityItem1);
    pageGroup.add(new ItemResponse(opportunityItem1));

    boolean isSuccessful = service.printPassage(mockOpportunityInstance, pageGroup, StringUtils.EMPTY);
    assertThat(isSuccessful).isFalse();
    verify(mockExamRepository, never()).createPrintRequest(isA(ExamPrintRequest.class));
  }

  @Test
  public void shouldPrintItemSuccessfully() throws ReturnStatusException {
    final String requestParams = "request";

    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setItemFile("/path/to/stim");
    opportunityItem1.setPosition(1);
    final ItemResponse itemResponse = new ItemResponse(opportunityItem1);

    boolean isSuccessful = service.printItem(mockOpportunityInstance, itemResponse, requestParams);
    assertThat(isSuccessful).isTrue();

    verify(mockExamRepository).createPrintRequest(examPrintRequestCaptor.capture());
    ExamPrintRequest printRequest = examPrintRequestCaptor.getValue();

    assertThat(printRequest.getExamId()).isEqualTo(mockOpportunityInstance.getExamId());
    assertThat(printRequest.getSessionId()).isEqualTo(mockOpportunityInstance.getSessionKey());
    assertThat(printRequest.getId()).isNotNull();
    assertThat(printRequest.getDescription()).isEqualTo("Item 1");
    assertThat(printRequest.getItemPosition()).isEqualTo(opportunityItem1.getPosition());
    assertThat(printRequest.getPagePosition()).isEqualTo(opportunityItem1.getPage());
    assertThat(printRequest.getParameters()).isEqualTo(requestParams);
    assertThat(printRequest.getChangedAt()).isNull();
    assertThat(printRequest.getReasonDenied()).isNull();
    assertThat(printRequest.getValue()).isEqualTo("/path/to/stim");
    assertThat(printRequest.getType()).isEqualTo(ExamPrintRequest.REQUEST_TYPE_PRINT_ITEM);
  }

  @Test
  public void shouldFailToPrintNullResponseToPrintItem() throws ReturnStatusException {
    final String requestParams = "request";
    boolean isSuccessful = service.printItem(mockOpportunityInstance, null, requestParams);
    assertThat(isSuccessful).isFalse();
    verify(mockExamRepository, never()).createPrintRequest(isA(ExamPrintRequest.class));
  }

  @Test
  public void shouldFailToPrintEmptyFilePathToPrintItem() throws ReturnStatusException {
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setPosition(1);
    final ItemResponse itemResponse = new ItemResponse(opportunityItem1);

    boolean isSuccessful = service.printItem(mockOpportunityInstance, itemResponse, StringUtils.EMPTY);
    assertThat(isSuccessful).isFalse();
    verify(mockExamRepository, never()).createPrintRequest(isA(ExamPrintRequest.class));
  }

  @Test
  public void shouldSuccessfullyPrintPassageBrailleNoTranscript() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());

    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setStimulusFile("/path/to/stim");
    opportunityItem1.setPosition(1);

    final OpportunityItem opportunityItem2 = new OpportunityItem();
    opportunityItem2.setPosition(2);

    final ITSAttachment brailleAttachment = new ITSAttachment();
    brailleAttachment.setType("Braille Type");
    brailleAttachment.setFile("/path/to/braille/file");
    brailleAttachment.setSubType("uncontracted");
    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(Arrays.asList(brailleAttachment));
    final ITSDocument document = new ITSDocument();
    document.addContent(content);

    final PageGroup pageGroup = new PageGroup(opportunityItem1);
    pageGroup.setDocument(document);
    pageGroup.add(new ItemResponse(opportunityItem1));
    pageGroup.add(new ItemResponse(opportunityItem2));

    AccLookup accLookup = new AccLookup();
    accLookup.add("Braille Type", "TDS_BT_G1");

    boolean isSuccessful = service.printPassageBraille(testOpportunity, pageGroup, accLookup);
    assertThat(isSuccessful).isTrue();

    verify(mockExamRepository).createPrintRequest(examPrintRequestCaptor.capture());
    ExamPrintRequest printRequest = examPrintRequestCaptor.getValue();

    assertThat(printRequest.getExamId()).isEqualTo(mockOpportunityInstance.getExamId());
    assertThat(printRequest.getSessionId()).isEqualTo(mockOpportunityInstance.getSessionKey());
    assertThat(printRequest.getId()).isNotNull();
    assertThat(printRequest.getDescription()).isEqualTo("Passage for Items 1-2 (Braille Type)");
    assertThat(printRequest.getItemPosition()).isEqualTo(0);
    assertThat(printRequest.getPagePosition()).isEqualTo(opportunityItem1.getPage());
    assertThat(printRequest.getParameters()).isEqualTo("FileFormat:BRAILLE TYPE");
    assertThat(printRequest.getChangedAt()).isNull();
    assertThat(printRequest.getReasonDenied()).isNull();
    assertThat(printRequest.getValue()).isEqualTo("/path/to/braille/file");
    assertThat(printRequest.getType()).isEqualTo(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE);
    assertThat(pageGroup.isPrinted()).isTrue();
  }

  @Test
  public void shouldSuccessfullyPrintPassageBrailleWithTranscript() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());

    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setStimulusFile("/path/to/stim");
    opportunityItem1.setPosition(1);

    final OpportunityItem opportunityItem2 = new OpportunityItem();
    opportunityItem2.setPosition(2);

    final ITSAttachment brailleAttachment1 = new ITSAttachment();
    brailleAttachment1.setType("Braille Type");
    brailleAttachment1.setFile("/path/to/braille/file");
    brailleAttachment1.setSubType("uncontracted");

    final ITSAttachment brailleAttachment2 = new ITSAttachment();
    brailleAttachment2.setType("Braille Transcript");
    brailleAttachment2.setFile("/path/to/braille/file2");
    brailleAttachment2.setSubType("uncontracted_transcript");

    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(Arrays.asList(brailleAttachment1, brailleAttachment2));
    final ITSDocument document = new ITSDocument();
    document.addContent(content);

    final PageGroup pageGroup = new PageGroup(opportunityItem1);
    pageGroup.setDocument(document);
    pageGroup.add(new ItemResponse(opportunityItem1));
    pageGroup.add(new ItemResponse(opportunityItem2));

    AccLookup accLookup = new AccLookup();
    accLookup.add("Braille Type", "TDS_BT_G1");
    accLookup.add("Braille Transcript", "TDS_BrailleTrans1");

    boolean isSuccessful = service.printPassageBraille(testOpportunity, pageGroup, accLookup);
    assertThat(isSuccessful).isTrue();

    verify(mockExamRepository).createPrintRequest(examPrintRequestCaptor.capture());
    ExamPrintRequest printRequest = examPrintRequestCaptor.getValue();

    assertThat(printRequest.getExamId()).isEqualTo(mockOpportunityInstance.getExamId());
    assertThat(printRequest.getSessionId()).isEqualTo(mockOpportunityInstance.getSessionKey());
    assertThat(printRequest.getId()).isNotNull();
    assertThat(printRequest.getDescription()).isEqualTo("Passage and Transcript for Items 1-2 (Braille Type)");
    assertThat(printRequest.getItemPosition()).isEqualTo(0);
    assertThat(printRequest.getPagePosition()).isEqualTo(opportunityItem1.getPage());
    assertThat(printRequest.getParameters()).isEqualTo("FileFormat:BRAILLE TYPE");
    assertThat(printRequest.getChangedAt()).isNull();
    assertThat(printRequest.getReasonDenied()).isNull();
    assertThat(printRequest.getValue()).isEqualTo("/path/to/braille/file;/path/to/braille/file2");
    assertThat(printRequest.getType()).isEqualTo(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE);
    assertThat(pageGroup.isPrinted()).isTrue();
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForEmptyXmlPath() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setStimulusFile(StringUtils.EMPTY);
    final OpportunityItem opportunityItem2 = new OpportunityItem();
    opportunityItem2.setPosition(2);

    final PageGroup pageGroup = new PageGroup(opportunityItem1);
    pageGroup.add(new ItemResponse(opportunityItem1));
    pageGroup.add(new ItemResponse(opportunityItem2));

    AccLookup accLookup = new AccLookup();

    service.printPassageBraille(testOpportunity, pageGroup, accLookup);
  }

  @Test
  public void shouldReturnFalseForNoContentPrintPassage() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final String passagePath = "/path/to/stim";
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setStimulusFile(passagePath);
    opportunityItem1.setPosition(1);

    final OpportunityItem opportunityItem2 = new OpportunityItem();
    opportunityItem2.setPosition(2);

    final ITSAttachment brailleAttachment = new ITSAttachment();
    brailleAttachment.setType("Braille Type");
    brailleAttachment.setFile("/path/to/braille/file");
    brailleAttachment.setSubType("uncontracted");
    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(Arrays.asList(brailleAttachment));
    final ITSDocument document = new ITSDocument();

    final PageGroup pageGroup = new PageGroup(opportunityItem1);
    pageGroup.add(new ItemResponse(opportunityItem1));
    pageGroup.add(new ItemResponse(opportunityItem2));

    AccLookup accLookup = new AccLookup();
    accLookup.add("Braille Type", "TDS_BT_G1");

    when(mockContentService.getContent(passagePath, accLookup)).thenReturn(document);

    boolean isSuccessful = service.printPassageBraille(testOpportunity, pageGroup, accLookup);
    assertThat(isSuccessful).isFalse();

    verify(mockContentService).getContent(passagePath, accLookup);
    verify(mockExamRepository, never()).createPrintRequest(isA(ExamPrintRequest.class));
  }

  @Test
  public void shouldReturnFalseForEmptyBrailleAttachments() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final String passagePath = "/path/to/stim";
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setStimulusFile(passagePath);
    opportunityItem1.setPosition(1);

    final OpportunityItem opportunityItem2 = new OpportunityItem();
    opportunityItem2.setPosition(2);

    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(new ArrayList<ITSAttachment>());

    final ITSDocument document = new ITSDocument();
    document.addContent(content);

    final PageGroup pageGroup = new PageGroup(opportunityItem1);
    pageGroup.add(new ItemResponse(opportunityItem1));
    pageGroup.add(new ItemResponse(opportunityItem2));

    AccLookup accLookup = new AccLookup();
    accLookup.add("Braille Type", "TDS_BT_G1");

    when(mockContentService.getContent(passagePath, accLookup)).thenReturn(document);

    boolean isSuccessful = service.printPassageBraille(testOpportunity, pageGroup, accLookup);
    assertThat(isSuccessful).isFalse();

    verify(mockContentService).getContent(passagePath, accLookup);
    verify(mockExamRepository, never()).createPrintRequest(isA(ExamPrintRequest.class));
  }

  @Test
  public void shouldSuccessfullyPrintPassageBrailleNoTranscriptContentFromContentService() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final String passagePath = "/path/to/stim";
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setStimulusFile(passagePath);
    opportunityItem1.setPosition(1);

    final OpportunityItem opportunityItem2 = new OpportunityItem();
    opportunityItem2.setPosition(2);

    final ITSAttachment brailleAttachment = new ITSAttachment();
    brailleAttachment.setType("Braille Type");
    brailleAttachment.setFile("/path/to/braille/file");
    brailleAttachment.setSubType("uncontracted");

    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(Arrays.asList(brailleAttachment));

    final ITSDocument document = new ITSDocument();
    document.addContent(content);

    final PageGroup pageGroup = new PageGroup(opportunityItem1);
    pageGroup.add(new ItemResponse(opportunityItem1));
    pageGroup.add(new ItemResponse(opportunityItem2));

    AccLookup accLookup = new AccLookup();
    accLookup.add("Braille Type", "TDS_BT_G1");

    when(mockContentService.getContent(passagePath, accLookup)).thenReturn(document);

    boolean isSuccessful = service.printPassageBraille(testOpportunity, pageGroup, accLookup);
    assertThat(isSuccessful).isTrue();

    verify(mockContentService).getContent(passagePath, accLookup);
    verify(mockExamRepository).createPrintRequest(examPrintRequestCaptor.capture());
    ExamPrintRequest printRequest = examPrintRequestCaptor.getValue();

    assertThat(printRequest.getExamId()).isEqualTo(mockOpportunityInstance.getExamId());
    assertThat(printRequest.getSessionId()).isEqualTo(mockOpportunityInstance.getSessionKey());
    assertThat(printRequest.getId()).isNotNull();
    assertThat(printRequest.getDescription()).isEqualTo("Passage for Items 1-2 (Braille Type)");
    assertThat(printRequest.getItemPosition()).isEqualTo(0);
    assertThat(printRequest.getPagePosition()).isEqualTo(opportunityItem1.getPage());
    assertThat(printRequest.getParameters()).isEqualTo("FileFormat:BRAILLE TYPE");
    assertThat(printRequest.getChangedAt()).isNull();
    assertThat(printRequest.getReasonDenied()).isNull();
    assertThat(printRequest.getValue()).isEqualTo("/path/to/braille/file");
    assertThat(printRequest.getType()).isEqualTo(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE);
  }

  @Test
  public void shouldSuccessfullyPrintItemBrailleNoTranscriptContentFromContentService() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final String itemPath = "/path/to/item";
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setPosition(1);

    final ITSAttachment brailleAttachment = new ITSAttachment();
    brailleAttachment.setType("Braille Type");
    brailleAttachment.setFile("/path/to/braille/file");
    brailleAttachment.setSubType("uncontracted");

    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(Arrays.asList(brailleAttachment));

    final ITSDocument document = new ITSDocument();
    document.addContent(content);

    final ItemResponse itemResponse = new ItemResponse(opportunityItem1);
    itemResponse.setFilePath(itemPath);

    AccLookup accLookup = new AccLookup();
    accLookup.add("Braille Type", "TDS_BT_G1");

    when(mockContentService.getContent(itemPath, accLookup)).thenReturn(document);

    boolean isSuccessful = service.printItemBraille(testOpportunity, itemResponse, accLookup);
    assertThat(isSuccessful).isTrue();

    verify(mockContentService).getContent(itemPath, accLookup);
    verify(mockExamRepository).createPrintRequest(examPrintRequestCaptor.capture());
    ExamPrintRequest printRequest = examPrintRequestCaptor.getValue();

    assertThat(printRequest.getExamId()).isEqualTo(mockOpportunityInstance.getExamId());
    assertThat(printRequest.getSessionId()).isEqualTo(mockOpportunityInstance.getSessionKey());
    assertThat(printRequest.getId()).isNotNull();
    assertThat(printRequest.getDescription()).isEqualTo("Item 1 (Braille Type)");
    assertThat(printRequest.getItemPosition()).isEqualTo(1);
    assertThat(printRequest.getPagePosition()).isEqualTo(opportunityItem1.getPage());
    assertThat(printRequest.getParameters()).isEqualTo("FileFormat:BRAILLE TYPE");
    assertThat(printRequest.getChangedAt()).isNull();
    assertThat(printRequest.getReasonDenied()).isNull();
    assertThat(printRequest.getValue()).isEqualTo("/path/to/braille/file");
    assertThat(printRequest.getType()).isEqualTo(ExamPrintRequest.REQUEST_TYPE_EMBOSS_ITEM);
  }

  @Test
  public void shouldSuccessfullyPrintItemBrailleWithTranscript() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final String itemPath = "/path/to/item";
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setPosition(1);

    final ITSAttachment brailleAttachment1 = new ITSAttachment();
    brailleAttachment1.setType("Braille Type");
    brailleAttachment1.setFile("/path/to/braille/file");
    brailleAttachment1.setSubType("uncontracted");

    final ITSAttachment brailleAttachment2 = new ITSAttachment();
    brailleAttachment2.setType("Braille Transcript");
    brailleAttachment2.setFile("/path/to/braille/file2");
    brailleAttachment2.setSubType("uncontracted_transcript");

    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(Arrays.asList(brailleAttachment1, brailleAttachment2));

    final ITSDocument document = new ITSDocument();
    document.addContent(content);

    final ItemResponse itemResponse = new ItemResponse(opportunityItem1);
    itemResponse.setFilePath(itemPath);
    itemResponse.setDocument(document);

    AccLookup accLookup = new AccLookup();
    accLookup.add("Braille Type", "TDS_BT_G1");
    accLookup.add("Braille Transcript", "TDS_BrailleTrans1");

    boolean isSuccessful = service.printItemBraille(testOpportunity, itemResponse, accLookup);
    assertThat(isSuccessful).isTrue();

    verify(mockContentService, never()).getContent(itemPath, accLookup);
    verify(mockExamRepository).createPrintRequest(examPrintRequestCaptor.capture());
    ExamPrintRequest printRequest = examPrintRequestCaptor.getValue();

    assertThat(printRequest.getExamId()).isEqualTo(mockOpportunityInstance.getExamId());
    assertThat(printRequest.getSessionId()).isEqualTo(mockOpportunityInstance.getSessionKey());
    assertThat(printRequest.getId()).isNotNull();
    assertThat(printRequest.getDescription()).isEqualTo("Item and Transcript 1 (Braille Type)");
    assertThat(printRequest.getItemPosition()).isEqualTo(1);
    assertThat(printRequest.getPagePosition()).isEqualTo(opportunityItem1.getPage());
    assertThat(printRequest.getParameters()).isEqualTo("FileFormat:BRAILLE TYPE");
    assertThat(printRequest.getChangedAt()).isNull();
    assertThat(printRequest.getReasonDenied()).isNull();
    assertThat(printRequest.getValue()).isEqualTo("/path/to/braille/file;/path/to/braille/file2");
    assertThat(printRequest.getType()).isEqualTo(ExamPrintRequest.REQUEST_TYPE_EMBOSS_ITEM);
  }

  @Test(expected = ReturnStatusException.class)
  public void shouldThrowForEmptyXmlPathPrintItemBraille() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setPosition(1);

    final ITSAttachment brailleAttachment = new ITSAttachment();
    brailleAttachment.setType("Braille Type");
    brailleAttachment.setFile("/path/to/braille/file");
    brailleAttachment.setSubType("uncontracted");

    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(Arrays.asList(brailleAttachment));

    final ITSDocument document = new ITSDocument();
    document.addContent(content);

    final ItemResponse itemResponse = new ItemResponse(opportunityItem1);
    itemResponse.setDocument(document);
    AccLookup accLookup = new AccLookup();

    service.printItemBraille(testOpportunity, itemResponse, accLookup);
  }

  @Test
  public void shouldReturnFalseForNoContentPrintItemBraille() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final String itemPath = "/path/to/item";
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setPosition(1);

    final ITSDocument document = new ITSDocument();

    final ItemResponse itemResponse = new ItemResponse(opportunityItem1);
    itemResponse.setFilePath(itemPath);
    itemResponse.setDocument(document);

    AccLookup accLookup = new AccLookup();

    boolean isSuccessful = service.printItemBraille(testOpportunity, itemResponse, accLookup);
    assertThat(isSuccessful).isFalse();

    verify(mockContentService, never()).getContent(itemPath, accLookup);
    verify(mockExamRepository, never()).createPrintRequest(isA(ExamPrintRequest.class));
  }

  @Test
  public void shouldReturnFalseForEmptyBrailleAttachmentsPrintItemBraille() throws ReturnStatusException {
    final TestOpportunity testOpportunity = new TestOpportunity(mockOpportunityInstance, "testKey", "testId", "ENU", new TestConfig());
    final String itemPath = "/path/to/item";
    final OpportunityItem opportunityItem1 = new OpportunityItem();
    opportunityItem1.setPage(3);
    opportunityItem1.setPosition(1);

    final ITSContent content = new ITSContent();
    content.setLanguage("ENU");
    content.setAttachments(new ArrayList<ITSAttachment>());

    final ITSDocument document = new ITSDocument();
    document.addContent(content);

    final ItemResponse itemResponse = new ItemResponse(opportunityItem1);
    itemResponse.setFilePath(itemPath);
    itemResponse.setDocument(document);

    AccLookup accLookup = new AccLookup();

    boolean isSuccessful = service.printItemBraille(testOpportunity, itemResponse, accLookup);
    assertThat(isSuccessful).isFalse();

    verify(mockContentService, never()).getContent(itemPath, accLookup);
    verify(mockExamRepository, never()).createPrintRequest(isA(ExamPrintRequest.class));
  }
}
