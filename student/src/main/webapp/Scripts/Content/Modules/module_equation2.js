ContentManager.onItemEvent('available', function (page, item) {
    if (!item.isResponseType('EquationEditor')) return;
    // If we are in accessibility mode, we revert to using a simple text area.
    // This is a stop gap till we figure out the right way to solicit math responses from students using JAWS
    if (ContentManager.isAccessibilityEnabled()) {
        var textArea = HTML.TEXTAREA();
        item.TextArea = textArea;
        page.getDoc().getElementById('EquationEditor_' + item.position).appendChild(textArea);
        if (item.value != null) item.setResponse(item.value);
        return;
    }

    //Initial page settings for the mathjax lib, changes will be made on zoom
    if(!MathJax.Editor.Config.SETUP){
      MathJax.Editor.Config.DEBUG = false;
      MathJax.Hub.Config({
        showMathMenu: false,
        "HTML-CSS": {
            availableFonts: ["TeX"],
            imageFont: null, //work with webfonts only
          scale: MathJax.Editor.Config.Scale || 150
        }
      });
      MathJax.Editor.Config.SETUP = true;
    }

    var content = EqEditor.parseXmlToJson(item);
    if(typeof content.RestrictKeysToContent == 'undefined'){
      content.RestrictKeysToContent = true; //Always on for the content editors unless SPECIFIED
      content.TeXEntryMode = 'Allow';
    }
    content.navigation = true;
    
    // create equation editor
    item.EquationEditor = new MathJax.Editor.Widget(content);
    item.EquationEditor.addCls(content.containerId, 'no-highlight');
    
    // try to set response if there is a value (in case we are doing a review or page refresh)
    if (item.value != null) {
        item.setResponse(item.value);
    }
    
    // create and add EQ component
    var eqComponent = {
        id: 'EQ_' + item.position,
        focus: function() { item.EquationEditor.focus(); },
        blur: function() { item.EquationEditor.unfocus(); }
    };

    item.addComponent(eqComponent);

    // check when EQ component is active
    var eqContainer = item.EquationEditor.getContainerDom();

    YUE.on(eqContainer, 'click', function() {
        item.setActiveComponent(eqComponent);
    });
});

/**
 * Zooming.   Blackbox zoom levels do really scary things with text font sizes.  MathJax
 * has to position things via absolutes, and the interaction between our scary css em
 * zoom and the absolute positions can really mess up the html.   
 *
 * This sets the MathJax internal scaling = Page scaling of fonts which will then ensure
 * the cursor actually ends up in the right places when zooming in.
 */
ContentManager.onItemEvent('zoom', function(page, item){
    if (!item.isResponseType('EquationEditor')) return;
    if (item.EquationEditor) {
      var eq = item.EquationEditor;
      if(page._zoom && page._zoom.currentLevel){
        var zoom = page._zoom.currentLevel;

        if(!isNaN(zoom)) {
          var scale = {0: 0.8, 1: 1.2, 2: 1.4, 3: 1.5, 4: 1.5};
          MathJax.Hub.Config({
            showMathMenu: false,
            "HTML-CSS":{
              scale: 150 * scale[zoom] //Render the bounding boxes correctly
            }
          });

          eq.CFG.fontTest(); 

          var eds = eq.getEditors();
          for(var i =0;i<eds.length;++i){
            eds[i] && eds[i].Update && eds[i].Update();
          }
        }
      }
      eq.unfocus();
    }
});

/*
ContentManager.onItemEvent('blur', function(page, item){
    if (!item.isResponseType('EquationEditor')) return;
    item.EquationEditor.unfocus(); //Remove focus on a blur event
});

ContentManager.onItemEvent('hide', function (page, item) {
    if (!item.isResponseType('EquationEditor')) return;
    item.EquationEditor.unfocus();
});

//Is there an un-focus option?
ContentManager.onItemEvent('show', function (page, item) {
    if (!item.isResponseType('EquationEditor')) return;
    item.EquationEditor.focus();
});
*/

//Map out the events that we have provided.
ContentManager.onItemEvent('keyevent', function(page, item, evt){
    if (!item.isResponseType('EquationEditor')) return;
    //Could grab the keypress handler again, but that seems excessive.
    //probably already going to handle it via the keypress handler
});

//Perform MathJax typesetting if needed on newly loaded pages
//FB-89368 CRS item content uses MathML formatting on non-EquationEditor items, 
//         so typesetting needs to occur once the page is loaded
ContentManager.onPageEvent('loaded', function(page) {

    var pageEl = page.getElement();

    // Does browser support MathML tags natively?
    var unsupportedBrowser = 
        Util.Browser.isChrome() ||
        Util.Browser.isIE();
    
    // Does the page contain MathML tags?
    var containsMathML = pageEl.getElementsByTagName("math").length > 0;

    // Determine if we need to use MathJax to typeset the page
    var typesetConditional = 
        pageEl && 
        unsupportedBrowser &&
        containsMathML;

    if (typesetConditional) {
        MathJax.Hub.Queue(['Typeset', MathJax.Hub, pageEl.id]);
    }
});

// RESPONSE HANDLER: EQUATION EDITOR
(function() {

    // HACK: Fix legacy responses (e.x., "<response type="plaintext"><![CDATA[51]]&gt;</response>")
    function cleanTextXml(xmlStr) {
        xmlStr = xmlStr.replace('<![CDATA[', '');
        xmlStr = xmlStr.replace(']]&gt;', '');
        xmlStr = xmlStr.replace(']]>', '');
        return xmlStr;
    }

    // create xml response wrapper 
    function createTextXml(text) {
        var xmlDoc = Util.Xml.parseFromString('<response type="plaintext"></response>');
        var textNode = xmlDoc.createTextNode(text);
        xmlDoc.documentElement.appendChild(textNode);
        return Util.Xml.serializeToString(xmlDoc);
    }

    // parse the xml response wrapper for the text
    function parseTextXml(xmlStr) {
        xmlStr = cleanTextXml(xmlStr);
        var xmlDoc = Util.Xml.parseFromString(xmlStr);
        if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes[0]) {
            return Util.Xml.getNodeText(xmlDoc.documentElement.childNodes[0]);
        }
        return null;
    }

    function getResponse(item, response) {

        // Support for cases where EQ items are used in accessibility UI and rendered as text areas.
        if (item.TextArea) {
            var text = item.TextArea.value;
            response.value = createTextXml(text);
            response.isAvailable = true;
            response.isValid = text.length > 0;
            response.isSelected = response.isValid;
            return;
        }

        try {
            var eq = item.EquationEditor;
            var ed = eq.getEdit();
            if (ed) { //If the user is typing then we want to enter their math
                ed.insertPending();
            }

            var mArr = eq.toMathML();
            var val = '<response>' + mArr.join('') + '</response>';

            console.log("Get response is: ", val);
            response.value = val;

            response.isAvailable = true;
            response.isSelected = !eq.isEmpty(); // Indicates that there is a non-empty response from the student
            response.isValid = !eq.isEmpty(); // Non-empty response is always valid for now. TBD - if widget is configured syntax validation, this flag would be based on that check

        } catch (e) {
            console.error("Failed to get a response for the EQ item.", item.EquationEditor, e);
        }
    }

    function setResponse(item, value) {

        // Support for cases where EQ items are used in accessibility UI and rendered as text areas.
        if (item.TextArea) {
            if (value) {
                var text = parseTextXml(value);
                item.TextArea.value = text || '';
            }
            return;
        }

        try { //Needs to handle multiple saved editors better, damn xml
            if (value) {
                value = value.replace(/<response>/gm, '');
                value = value.replace(/<\/response>/gm, '');

                value = EqEditor.mathToArr(value);

                var eq = item.EquationEditor;
                var cb = eq.updateEditors.bind(eq, value);
                MathJax.Hub.Queue(cb);
            }
        } catch (e) {
            console.error("Faield to update the mathML with this value.", value, e);
        }
    }

    ContentManager.registerResponseHandler('EquationEditor', getResponse, setResponse);

})();

// load equation editor
if(!window.EqEditor){
  EqEditor = {}; 
  EqEditor.getMathMLArr = function(xmlStr){
    var mathML = xmlStr.match(/<mathML>([\w\W]*)<\/mathML>/m);
    if(mathML){
      mathML = mathML[1];   
    }else{
      return [];
    }
    //Deals with XML screwing up an array
    mathML = mathML.replace(/<mathML>/gm, '');
    mathML = mathML.replace(/<\/mathML>/gm, '');

    return EqEditor.mathToArr(mathML);
  };
  EqEditor.mathToArr = function(mathML){
    var fin  = [];
    mathML = mathML.trim().split('</math>');
    for(var m in mathML){
      if(mathML[m]){
         fin.push(mathML[m] + '</math>');
      }
    }
    return fin;
  };
  EqEditor.parseBooleans  = function(content){
    if(!content) return {};
    for(var c in content){
      if(content[c] === 'true') content[c]  = true; 
      if(content[c] === 'false') content[c] = false; 
    }
    return content;
  };
  EqEditor.parseXmlToJson = function(item){
    var content = {};
    try{
      var xmlStr = decodeURIComponent(item.rendererSpec);
      if(xmlStr){
        content        = JSON.parse(xml2jsonv2(parseXml(xmlStr), '')); //JSON.parse(xml2jsonv2(parseXml(xmlStr), ''));
        content        = content.editorconfig;
        content.mathML = EqEditor.getMathMLArr(xmlStr);
        content        = EqEditor.parseBooleans(content);
      }
    }catch(e){
      console.error('Failed to parse the Eq rendererSpec', item.rendererSpec, e);
    }
    content.containerId = 'EquationEditor_' + item.position;
    content.id = null;
    return content;
  };
}
