package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.UUID;

import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ExamSegmentRepository;

@Service("integrationResponseService")
@Scope("prototype")
public class RemoteResponseService implements IResponseService {
  private final boolean isRemoteExamCallsEnabled;
  private final boolean isLegacyCallsEnabled;
  private final IResponseService legacyResponseService;
  private final ExamSegmentRepository examSegmentRepository;

  @Autowired
  public RemoteResponseService(final IResponseService legacyResponseService,
                               final @Value("${tds.exam.remote.enabled}") Boolean remoteExamCallsEnabled,
                               final @Value("${tds.exam.legacy.enabled}") Boolean legacyCallsEnabled,
                               final ExamSegmentRepository examSegmentRepository) {
    if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
    }

    this.legacyResponseService = legacyResponseService;
    this.examSegmentRepository = examSegmentRepository;
    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.isLegacyCallsEnabled = legacyCallsEnabled;
  }

  @Override
  public PageGroup insertItems(final OpportunityInstance oppInstance, final AdaptiveGroup adaptiveGroup, final boolean isMsb) throws ReturnStatusException {
    return legacyResponseService.insertItems(oppInstance, adaptiveGroup, isMsb);
  }

  @Override
  public PageList getOpportunityItems(final OpportunityInstance oppInstance, final boolean validate) throws ReturnStatusException {
    return legacyResponseService.getOpportunityItems(oppInstance, validate);
  }

  @Override
  public PageGroup getItemGroup(final OpportunityInstance oppInstance, final int page, final String groupID, final String dateCreated, final boolean validate) throws ReturnStatusException {
    return legacyResponseService.getItemGroup(oppInstance, page, groupID, dateCreated, validate);
  }

  @Override
  public boolean isTestComplete(final UUID examId) throws ReturnStatusException {
    boolean isComplete = false;

    if (isLegacyCallsEnabled) {
      isComplete = legacyResponseService.isTestComplete(examId);
    }

    if (!isRemoteExamCallsEnabled) {
      return isComplete;
    }

    return examSegmentRepository.checkSegmentsSatisfied(examId);
  }

  @Override
  public void removeResponse(final OpportunityInstance oppInstance, final int position, final String itemID, final String dateCreated) throws ReturnStatusException {
    legacyResponseService.removeResponse(oppInstance, position, itemID, dateCreated);
  }
}
