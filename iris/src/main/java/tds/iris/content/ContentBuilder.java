package tds.iris.content;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.Helpers.CaseInsensitiveMap;
import AIR.Common.Utilities.SpringApplicationContext;
import tds.blackbox.ContentRequestException;
import tds.iris.abstractions.repository.ContentException;
import tds.iris.abstractions.repository.IContentBuilder;
import tds.itempreview.ConfigBuilder;
import tds.itemrenderer.data.IITSDocument;

@Component
@Scope ("prototype")
public class ContentBuilder implements IContentBuilder
{
  private static final Logger       _logger = LoggerFactory.getLogger (ContentBuilder.class);
  private String                    _contentPath;
  private Map<String, IITSDocument> _documentLookup;
  private Exception                 _error  = null;

  @PostConstruct
  public void init () throws ContentException {
    try {
      // scan the local folder.
      _contentPath = AppSettingsHelper.get ("iris.ContentPath");
      ConfigBuilder directoryScanner = new ConfigBuilder (_contentPath);
      directoryScanner.create ();
      // a side effect of the create() call is that there is now a list of
      // ITSDocuments in directoryScanner.
      Collection<IITSDocument> documents = directoryScanner.getDocuments ();

      // now run through the documents and build the key set.
      Map<String, IITSDocument> documentsMap = new CaseInsensitiveMap<IITSDocument> ();
      for (IITSDocument document : documents) {
        String id = String.format ("I-%s-%s", document.getBankKey (), document.getItemKey ());
        documentsMap.put (id, document);
      }
      _documentLookup = Collections.unmodifiableMap (documentsMap);
    } catch (Exception exp) {
      _logger.error ("Error loading IRiS content.", exp);
      _error = exp;
      throw new ContentException (exp);
    }
  }

  @Override
  public IITSDocument getITSDocument (String id) throws ContentRequestException {
    if (_error != null) {
      throw new ContentRequestException ("Content not loaded properly.", _error);
    }
    if (_documentLookup.containsKey (id)) {
      return _documentLookup.get (id);
    }
    throw new ContentRequestException (String.format ("No content found by id %s", id));
  }
}
