package tds.student.performance.services;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemrenderer.data.IITSDocument;
import tds.student.performance.domain.AccLookupWrapper;

/**
 * This service is called from ContentService and is here in order to add caching to one of the calls that relies on AccLookup.
 * ContentService will now wrap AccLookup in a AccLookupWrapper and call this helper method which will be cacheable;
 */
public interface ContentHelperService {
    IITSDocument getContent(String xmlFilePath, AccLookupWrapper accommodations) throws ReturnStatusException;
}
