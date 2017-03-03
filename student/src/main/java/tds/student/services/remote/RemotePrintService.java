package tds.student.services.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSAttachment;
import tds.itemrenderer.data.ITSContent;
import tds.student.services.PrintServiceImpl;
import tds.student.services.abstractions.IContentService;
import tds.student.services.abstractions.PrintService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.ExamRepository;

@Service("integrationPrintService")
@Scope("prototype")
public class RemotePrintService implements PrintService {
  private static final Logger LOG = LoggerFactory.getLogger(RemotePrintService.class);
  private static final int PASSAGE_PRINT_ITEM_POSITION_DEFAULT = 0;

  private final boolean isRemoteExamCallsEnabled;
  private final boolean isLegacyCallsEnabled;
  private final ExamRepository examRepository;
  private final IContentService contentService;
  private final PrintService legacyPrintService;

  @Autowired
  public RemotePrintService(
    @Qualifier("legacyPrintService") final PrintService legacyPrintService,
    @Value("${tds.exam.remote.enabled}") final Boolean remoteExamCallsEnabled,
    @Value("${tds.exam.legacy.enabled}") final Boolean legacyCallsEnabled,
    final ExamRepository examRepository,
    final IContentService contentService) {

    if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
      throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
    }

    this.isRemoteExamCallsEnabled = remoteExamCallsEnabled;
    this.isLegacyCallsEnabled = legacyCallsEnabled;
    this.examRepository = examRepository;
    this.contentService = contentService;
    this.legacyPrintService = legacyPrintService;
  }

  @Override
  public boolean printPassage(final OpportunityInstance oppInstance, final PageGroup pageGroupToPrint, final String requestParameters) throws ReturnStatusException {
    return printPassage(ExamPrintRequest.REQUEST_TYPE_PRINT_PASSAGE, oppInstance, pageGroupToPrint, requestParameters);
  }

  @Override
  public boolean printPage(final OpportunityInstance oppInstance, final PageGroup pageGroupToPrint, final String requestParameters) throws ReturnStatusException {
    return printPassage(ExamPrintRequest.REQUEST_TYPE_PRINT_PAGE, oppInstance, pageGroupToPrint, requestParameters);
  }

  /* PrintService - printPassage() - line 117 */
  @Override
  public boolean printPassage(final String requestType, final OpportunityInstance oppInstance, final PageGroup pageGroupToPrint, final String requestParameters) throws ReturnStatusException {
    boolean isSuccessful = false;

    if (isLegacyCallsEnabled) {
      isSuccessful = legacyPrintService.printPassage(requestType, oppInstance, pageGroupToPrint, requestParameters);
    }

    //This isn't ideal, but due to the way the progman properties are loaded within the system this lives within the service rather than the callers.
    if (!isRemoteExamCallsEnabled) {
      return isSuccessful;
    }

    if (pageGroupToPrint == null || pageGroupToPrint.size() == 0) {
      return false;
    }

    final String requestValue = pageGroupToPrint.getFilePath();

    if (StringUtils.isEmpty(requestValue)) {
      return false;
    }

    final ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
      .withExamId(oppInstance.getExamId())
      .withSessionId(oppInstance.getSessionKey())
      .withPagePosition(pageGroupToPrint.getNumber())
      .withItemPosition(PASSAGE_PRINT_ITEM_POSITION_DEFAULT)
      .withType(requestType)
      .withDescription(getPassageLabel(pageGroupToPrint, false))
      .withParameters(requestParameters)
      .withValue(requestValue.replace("\\", "\\\\"))
      .build();

    examRepository.createPrintRequest(request);

    return true;
  }

  @Override
  public boolean printItem(final OpportunityInstance opportunityInstance, final ItemResponse responseToPrint, final String requestParameters) throws ReturnStatusException {
    boolean isSuccessful = false;

    if (isLegacyCallsEnabled) {
      isSuccessful = legacyPrintService.printItem(opportunityInstance, responseToPrint, requestParameters);
    }

    //This isn't ideal, but due to the way the progman properties are loaded within the system this lives within the service rather than the callers.
    if (!isRemoteExamCallsEnabled) {
      return isSuccessful;
    }

    if (responseToPrint == null) {
      return false;
    }

    final String requestValue = responseToPrint.getFilePath();

    if (StringUtils.isEmpty(requestValue)) {
      return false;
    }

    final ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
      .withExamId(opportunityInstance.getExamId())
      .withSessionId(opportunityInstance.getSessionKey())
      .withPagePosition(responseToPrint.getPage())
      .withItemPosition(responseToPrint.getPosition()) // if this is page, this val is 0 - look into combinging print page/item
      .withType(ExamPrintRequest.REQUEST_TYPE_PRINT_ITEM)
      .withDescription(getItemLabel(responseToPrint, false))
      .withParameters(requestParameters)
      .withValue(requestValue.replace("\\", "\\\\"))
      .build();

    examRepository.createPrintRequest(request);

    return true;
  }

  @Override
  public boolean printPassageBraille(String requestType, TestOpportunity testOpp, PageGroup pageGroupToPrint, AccLookup accLookup) throws ReturnStatusException {
    boolean isSuccessful = false;

    if (isLegacyCallsEnabled) {
      isSuccessful = legacyPrintService.printPassageBraille(requestType, testOpp, pageGroupToPrint, accLookup);
    }

    //This isn't ideal, but due to the way the progman properties are loaded within the system this lives within the service rather than the callers.
    if (!isRemoteExamCallsEnabled) {
      return isSuccessful;
    }

    if (pageGroupToPrint == null || pageGroupToPrint.size() == 0) {
      return false;
    }

    String xmlPath = pageGroupToPrint.getFilePath();

    if (StringUtils.isEmpty(xmlPath)) {
      throw new ReturnStatusException(String.format("PrintPassageBraille: Invalid xml file path for group %1$s.", pageGroupToPrint.getId()));
    }

    IITSDocument document = pageGroupToPrint.getDocument();

    // try and load document if it isn't already loaded
    if (document == null) {
      document = contentService.getContent(xmlPath, accLookup);

      if (document == null) {
        return false;
      }

      pageGroupToPrint.setDocument(document);
    }

    ITSContent content = document.getContent(testOpp.getLanguage());

    if (content == null) {
      return false;
    }

    // get attachments for the accommodations braille type
    List<ITSAttachment> brailleAttachments = content.GetBrailleTypeAttachment(accLookup);

    if (brailleAttachments.isEmpty()) { // no need for null check, this is initialized as an empty list
      LOG.warn("PrintPassageBraille: Cannot find a matching braille attachment for the test {} and passage {}.",
        testOpp.getTestKey(), pageGroupToPrint.getId());
      return false;
    }

    ITSAttachment mainBrailleAttachment = brailleAttachments.get(0);
    String requestValue = mainBrailleAttachment.getFile();
    String requestParameters = "FileFormat:" + mainBrailleAttachment.getType().toUpperCase(); // name:value;name:value

    boolean isTranscript = isTranscriptRequest(brailleAttachments);

    if (isTranscript) {
      requestValue += ";" + brailleAttachments.get(1).getFile();
    }

    String requestDescription = String.format("%1$s (%2$s)", getPassageLabel(pageGroupToPrint, isTranscript),
      mainBrailleAttachment.getType());

    OpportunityInstance opportunityInstance = testOpp.getOppInstance();

    final ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
      .withExamId(opportunityInstance.getExamId())
      .withSessionId(opportunityInstance.getSessionKey())
      .withPagePosition(pageGroupToPrint.getNumber())
      .withItemPosition(PASSAGE_PRINT_ITEM_POSITION_DEFAULT)
      .withType(requestType)
      .withDescription(requestDescription)
      .withParameters(requestParameters)
      .withValue(requestValue.replace("\\", "\\\\"))
      .build();

    examRepository.createPrintRequest(request);

    isSuccessful = true;
    pageGroupToPrint.setPrinted(isSuccessful);

    return isSuccessful;
  }

  @Override
  public boolean printItemBraille(TestOpportunity testOpp, ItemResponse responseToPrint, AccLookup accLookup) throws ReturnStatusException {
    boolean isSuccessful = false;

    if (isLegacyCallsEnabled) {
      isSuccessful = legacyPrintService.printItemBraille(testOpp, responseToPrint, accLookup);
    }

    //This isn't ideal, but due to the way the progman properties are loaded within the system this lives within the service rather than the callers.
    if (!isRemoteExamCallsEnabled) {
      return isSuccessful;
    }

    String xmlPath = responseToPrint.getFilePath();

    if (StringUtils.isEmpty(xmlPath)) {
      throw new ReturnStatusException(String.format("PrintItemBraille: Invalid xml file path for item %1$s.", responseToPrint.getItemID()));
    }

    IITSDocument document = responseToPrint.getDocument();

    // try and load document if it isn't already loaded
    if (document == null) {
      document = contentService.getContent(xmlPath, accLookup);

      if (document == null) {
        return false;
      }

      responseToPrint.setDocument(document);
    }

    ITSContent content = document.getContent(testOpp.getLanguage());

    if (content == null) {
      return false;
    }

    // get attachments for the accommodations braille type
    List<ITSAttachment> brailleAttachments = content.GetBrailleTypeAttachment(accLookup);

    if (brailleAttachments.isEmpty()) { // no need for null check, this is initialized as an empty list
      LOG.warn("PrintItemBraille: Cannot find a matching braille attachment for the test {} and item {}.",
        testOpp.getTestKey(), responseToPrint.getItemID());
      return false;
    }

    ITSAttachment mainBrailleAttachment = brailleAttachments.get(0);
    String requestValue = mainBrailleAttachment.getFile();
    String requestParameters = "FileFormat:" + mainBrailleAttachment.getType().toUpperCase(); // name:value;name:value

    boolean isTranscript = isTranscriptRequest(brailleAttachments);

    if (isTranscript) {
      requestValue += ";" + brailleAttachments.get(1).getFile();
    }

    String requestDescription = String.format("%1$s (%2$s)", getItemLabel(responseToPrint, isTranscript),
      mainBrailleAttachment.getType());

    OpportunityInstance opportunityInstance = testOpp.getOppInstance();

    final ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
      .withExamId(opportunityInstance.getExamId())
      .withSessionId(opportunityInstance.getSessionKey())
      .withPagePosition(responseToPrint.getPage())
      .withItemPosition(responseToPrint.getPosition())
      .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_ITEM)
      .withDescription(requestDescription)
      .withParameters(requestParameters)
      .withValue(requestValue.replace("\\", "\\\\"))
      .build();

    examRepository.createPrintRequest(request);

    isSuccessful = true;
    responseToPrint.setPrinted(isSuccessful);

    return isSuccessful;
  }

  @Override
  public boolean printPassageBraille(TestOpportunity testOpp, PageGroup pageGroupToPrint, AccLookup accLookup) throws ReturnStatusException {
    return printPassageBraille("EMBOSSPASSAGE", testOpp, pageGroupToPrint, accLookup);
  }

  @Override
  public boolean printPageBraille(TestOpportunity testOpp, PageGroup pageGroupToPrint, AccLookup accLookup) throws ReturnStatusException {
    return printPassageBraille("EMBOSSPAGE", testOpp, pageGroupToPrint, accLookup);
  }

  private String getPassageLabel(PageGroup group, boolean isTranscript) throws ReturnStatusException {
    StringBuilder label = new StringBuilder("Passage");

    if (isTranscript) {
      label.append(" and Transcript");
    }

    if (group.size() > 0) {
      label.append(" for ");

      ItemResponse firstResponse = group.getFirst();
      if (group.size() == 1) {
        String postion = String.format("Item %1$d", firstResponse.getPosition());
        label.append(postion);

      } else {
        ItemResponse lastResponse = group.getLast();
        String postion = String.format("Items %1$d-%2$d", firstResponse.getPosition(), lastResponse.getPosition());
        label.append(postion);
      }
    }
    return label.toString();
  }

  private String getItemLabel(final ItemResponse response, final boolean isTranscript) {
    return String.format("%1$s %2$d", isTranscript ? "Item and Transcript" : "Item", response.getPosition());
  }

  /*
   * Determines if the list of attachments means that this is a Braille Transcript embossing request
   */
  private static boolean isTranscriptRequest(List<ITSAttachment> brailleAttachments) {
    return brailleAttachments.size() == 2 && brailleAttachments.get(1).getSubType().endsWith("_transcript");
  }
}
