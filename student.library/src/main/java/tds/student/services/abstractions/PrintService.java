package tds.student.services.abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.itemrenderer.data.AccLookup;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;

/**
 * A service for creating print requests
 */
public interface PrintService {
  /**
   * Creates a print request to print a passage
   *
   * @param oppInstance       The opportunity instance containing exam information
   * @param pageGroupToPrint  The page group to print
   * @param requestParameters Additional request parameters for the print request
   * @return {@code true} if successful, {@code false} otherwise
   * @throws ReturnStatusException thrown if xml path for resource is null or empty
   */
  boolean printPassage(OpportunityInstance oppInstance, PageGroup pageGroupToPrint, String requestParameters) throws ReturnStatusException;

  /**
   * Creates a print request to print a page
   *
   * @param oppInstance       The opportunity instance containing exam information
   * @param pageGroupToPrint  The page group to print
   * @param requestParameters Additional request parameters for the print request
   * @return {@code true} if successful, {@code false} otherwise
   * @throws ReturnStatusException thrown if xml path for resource is null or empty
   */
  boolean printPage(OpportunityInstance oppInstance, PageGroup pageGroupToPrint, String requestParameters) throws ReturnStatusException;

  /**
   * Creates a print request to print an item
   *
   * @param oppInstance       The opportunity instance containing exam information
   * @param responseToPrint   The item to print
   * @param requestParameters Additional request parameters for the print request
   * @return {@code true} if successful, {@code false} otherwise
   * @throws ReturnStatusException thrown if xml path for resource is null or empty
   */
  boolean printItem(OpportunityInstance oppInstance, ItemResponse responseToPrint, String requestParameters) throws ReturnStatusException;

  /**
   * Creates a print request to emboss a passage
   *
   * @param testOpp          The test opportunity object containing exam information
   * @param pageGroupToPrint The page group to emboss
   * @param accLookup        The map of exam accommodations
   * @return {@code true} if successful, {@code false} otherwise
   * @throws ReturnStatusException thrown if xml path for resource is null or empty
   */
  boolean printPassageBraille(TestOpportunity testOpp, PageGroup pageGroupToPrint, AccLookup accLookup) throws ReturnStatusException;

  /**
   * Creates a print request to emboss a page
   *
   * @param testOpp          The test opportunity object containing exam information
   * @param pageGroupToPrint The page group to emboss
   * @param accLookup        The map of exam accommodations
   * @return {@code true} if successful, {@code false} otherwise
   * @throws ReturnStatusException thrown if xml path for resource is null or empty
   */
  boolean printPageBraille(TestOpportunity testOpp, PageGroup pageGroupToPrint, AccLookup accLookup) throws ReturnStatusException;

  /**
   * Creates a print request to emboss an item
   *
   * @param testOpp         The test opportunity object containing exam information
   * @param responseToPrint The item to emboss
   * @param accLookup       The map of exam accommodations
   * @return {@code true} if successful, {@code false} otherwise
   * @throws ReturnStatusException thrown if xml path for resource is null or empty
   */
  boolean printItemBraille(TestOpportunity testOpp, ItemResponse responseToPrint, AccLookup accLookup) throws ReturnStatusException;
}
