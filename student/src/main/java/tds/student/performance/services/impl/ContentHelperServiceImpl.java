package tds.student.performance.services.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import tds.itemrenderer.ITSDocumentFactory;
import tds.itemrenderer.data.IITSDocument;
import tds.student.performance.caching.CacheType;
import tds.student.performance.domain.AccLookupWrapper;
import tds.student.performance.services.ContentHelperService;

/**
 * This service is called from ContentService and is here in order to add caching to one of the calls that relies on AccLookup.
 * ContentService will now wrap AccLookup in a AccLookupWrapper and call this helper method which will be cacheable;
 */
@Component
public class ContentHelperServiceImpl implements ContentHelperService {
    @Override
    @Cacheable(CacheType.LongTerm)
    public IITSDocument getContent(String xmlFilePath, AccLookupWrapper accommodations) throws ReturnStatusException {
        if (StringUtils.isEmpty (xmlFilePath))
            return null;

        return ITSDocumentFactory.load (xmlFilePath, accommodations.getValue(), true);
    }
}
