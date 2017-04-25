package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import tds.student.services.abstractions.IAdaptiveService;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.data.TestConfig;
import tds.student.sql.repository.remote.ExamItemResponseRepository;

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

    assertThat(pageGroup).hasSize(1);
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
}