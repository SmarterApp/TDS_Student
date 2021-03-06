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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tds.student.services.abstractions.IAdaptiveService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.data.TestConfig;
import tds.student.sql.repository.remote.ExamItemResponseRepository;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAdaptiveServiceTest {

  @Mock
  private IAdaptiveService legacyAdaptiveService;

  @Mock
  private ExamItemResponseRepository responseRepository;

  private RemoteAdaptiveService service;

  @Before
  public void setUp() {
    service = new RemoteAdaptiveService(legacyAdaptiveService, true, true, responseRepository);
  }

  @Test (expected = IllegalStateException.class)
  public void shouldThrowIfMisconfigured() {
    new RemoteAdaptiveService(legacyAdaptiveService, false, false, responseRepository);
  }

  @Test
  public void shouldFindPageGroup() throws ReturnStatusException {
    OpportunityItem item = new OpportunityItem();

    OpportunityItem legacyItem = new OpportunityItem();
    PageGroup legacyPageGroup = new PageGroup(legacyItem);

    UUID examId = UUID.randomUUID();
    OpportunityInstance opp = new OpportunityInstance(
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      examId,
      UUID.randomUUID(),
      "SBAC_PT",
      "agent");

    TestOpportunity testOpportunity = new TestOpportunity(opp, "testKey", "testId", "ENDU", new TestConfig());

    when(legacyAdaptiveService.createNextItemGroup(testOpportunity, 1 ,1)).thenReturn(legacyPageGroup);
    when(responseRepository.createNextItemGroup(examId, 1, 1)).thenReturn(Collections.singletonList(item));

    PageGroup pageGroup = service.createNextItemGroup(testOpportunity, 1, 1);

    verify(legacyAdaptiveService).createNextItemGroup(testOpportunity, 1, 1);
    verify(responseRepository).createNextItemGroup(examId, 1, 1);

    for (final ItemResponse itemResponse : pageGroup) {
      assertThat(itemResponse.isPrefetched()).isTrue();
    }

    assertThat(pageGroup).isEqualTo(legacyPageGroup);
  }

  @Test
  public void shouldOnlyCallLegacyCode() throws ReturnStatusException {
    service = new RemoteAdaptiveService(legacyAdaptiveService, false, true, responseRepository);
    OpportunityItem legacyItem = new OpportunityItem();
    PageGroup legacyPageGroup = new PageGroup(legacyItem);

    UUID examId = UUID.randomUUID();
    OpportunityInstance opp = new OpportunityInstance(
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      examId,
      UUID.randomUUID(),
      "SBAC_PT",
      "agent");

    TestOpportunity testOpportunity = new TestOpportunity(opp, "testKey", "testId", "ENDU", new TestConfig());

    when(legacyAdaptiveService.createNextItemGroup(testOpportunity, 1 ,1)).thenReturn(legacyPageGroup);

    assertThat(service.createNextItemGroup(testOpportunity, 1, 1)).isEqualTo(legacyPageGroup);

    verify(legacyAdaptiveService).createNextItemGroup(testOpportunity, 1, 1);
    verifyZeroInteractions(responseRepository);
  }

  @Test
  public void shouldOnlyCallRemote() throws ReturnStatusException {
    service = new RemoteAdaptiveService(legacyAdaptiveService, true, false, responseRepository);
    OpportunityItem item = new OpportunityItem();

    UUID examId = UUID.randomUUID();
    OpportunityInstance opp = new OpportunityInstance(
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      examId,
      UUID.randomUUID(),
      "SBAC_PT",
      "agent");

    TestOpportunity testOpportunity = new TestOpportunity(opp, "testKey", "testId", "ENDU", new TestConfig());

    when(responseRepository.createNextItemGroup(examId, 1, 1)).thenReturn(Collections.singletonList(item));

    PageGroup pageGroup = service.createNextItemGroup(testOpportunity, 1, 1);

    verifyZeroInteractions(legacyAdaptiveService);
    verify(responseRepository).createNextItemGroup(examId, 1, 1);

    assertThat(pageGroup).hasSize(1);
  }

  @Test
  public void itShouldReturnNullForARemoteExamWithNoRemainingOpportunityItems() throws Exception {
    final UUID examId = UUID.randomUUID();
    final OpportunityInstance opp = new OpportunityInstance(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        examId,
        UUID.randomUUID(),
        "SBAC_PT",
        "agent");
    final TestOpportunity testOpportunity = new TestOpportunity(opp, "testKey", "testId", "ENDU", new TestConfig());

    service = new RemoteAdaptiveService(legacyAdaptiveService, true, false, responseRepository);
    when(responseRepository.createNextItemGroup(examId, 1, 1)).thenReturn(Collections.<OpportunityItem>emptyList());

    assertThat(service.createNextItemGroup(testOpportunity, 1, 1)).isNull();
  }
}