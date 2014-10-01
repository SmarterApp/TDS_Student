
YAHOO.util.Event.onDOMReady(function(){

if(window.TDS && !window.TDS.Mask){ return; }

//Initialize TTS after the page has loaded (ensures any custom config can be pulled in)
var accProps = ContentManager.getAccommodationProperties();
if(accProps.getSelectedCode('Masking') == 'TDS_Masking0'){
  console.warn("Masking not enabled.");
  return;
}
 
//No debug in production (set in console if you want it to work)
TDS.Mask.Config.Debug = true;
TDS.Mask.initialize();

//Note that scripts can be loaded AFTER config and this is called too early.
ContentManager.onPageEvent('show', function(page) {
    if(!page){return;}

    var doc    = page.getElement();
    var pageId = page.id;

    //Will turn things off by default.
    TDS.Mask.setPageId(pageId);
    TDS.Mask.setPageDom(doc);
});

ContentManager.onPageEvent('hide', function(page) {
  console.log("Turn of masking on a hide.");
  TDS.Mask.hide();
});

}); //End on dom ready

