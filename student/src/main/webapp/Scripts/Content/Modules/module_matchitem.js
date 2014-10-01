
MatchItem = (typeof (MatchItem) == "undefined") ? {} : MatchItem;

ContentManager.onItemEvent('available', function (page, item) {

    // Is this a match item?
    if (item.isResponseType('MatchItem') || item.isResponseType('TableMatch')) {
        ContentManager.log("MatchItem: processing " + item.getID());

        // Create the parser.  
        var parseResult = new MatchItem.Parse(item.itemKey);

        // Parse the xml file.  It can come from renderer field or qti field.
        var qtiXml = (item.qti) ? item.qti.xml : item.rendererSpec;
        if (qtiXml == null) return;

        // parse the qti xml
        parseResult.createFromXml(qtiXml);

        // Use the parsed XML to construct a table.  Create ta table form or 
        // list form as accommodations require.
        var presentation;
        if (item.isResponseType('TableMatch') || ContentManager.isAccessibilityEnabled()) {
            presentation = new MatchItem.TableHtmlGenerator(item.position);
        } else {
            presentation = new MatchItem.ListHtmlGenerator(item.position);
        }

        var discriminator = item.position;
        var m1 = new MatchItem.Matrix(parseResult, presentation, discriminator);

        m1.isReadOnly = item.isReadOnly;

        // Get list of tabbable components from widget and add to item 
        var componentArray = m1.getComponentArray();
        for (var i = 0; i < componentArray.length; ++i) {
            item.addComponent(componentArray[i]);
        }

        // Look for the parent div
        var pageDoc = page.getDoc();
        var div = pageDoc.getElementById('MatchContainer_' + item.position);
        if (div) {
            div.innerHTML = '';
            // Get rid of the spinner.
            YUD.removeClass(div, 'loading');

            // Render the content
            m1.attachTable(div);

            // Save the matrix in the item
            item.MatchItem = m1;

            var onresize = window.onresize;

            YUE.on(page.getWin(), 'scroll', function () {
                if (onresize != null)
                    onresize();
                if (item.isVisible())
                    m1.reDrawChecks();
            });

            // Restore old values.
            if (item.value != null) {
                m1.setResultXML(item.value);
            }
        }
    }
});

// listen for key events
ContentManager.onItemEvent('keyevent', function (page, item, evt) {
    // check if mi
    if (!item.MatchItem) return;
    if (evt.type != 'keydown') return;
    if (evt.ctrlKey || evt.altKey) return; // no modifiers

    var matches = item.MatchItem;

    if (evt.key == 'Enter') {
        // ignore key events if in read-only mode
        if (ContentManager.isReadOnly()) return;

        evt.stopPropagation();

        // Notify widget of key event and fetch handler based on component ID
        var matchId = item.getActiveComponent().id;
        if (matchId) matches.handleKeyEvent(matchId, evt);
    }
});

// If we zoom, the lines in line mode get messed up.  Redraw them.
ContentManager.onItemEvent('zoom', function (page, item) {
    if (item.MatchItem != null) {
        item.MatchItem.reDrawChecks();
    }
});

ContentManager.onItemEvent('show', function (page, item) {
    if (item.MatchItem != null) {
        item.MatchItem.reDrawChecks();
    }
});

// If we leave the page, get rid of any lines that might be left.
/* ContentManager.onItemEvent('blur', function (page, item) {
    if (item.MatchItem != null) {
        item.MatchItem.eraseChecks();
    }
}); */
    
// register response getter and setter for MC questions
(function () {
    // response handler for MI questions
    var getter = function (item, response) {
        response.value = item.MatchItem.getResponseXML();

        response.isValid = item.MatchItem.isResponseValid();
        response.isSelected = response.isValid;
        response.isAvailable = true;
    };

    // response handler for MI questions
    var setter = function (item, value) {
        item.MatchItem.setResultXML(value);
    };

    ContentManager.registerResponseHandler('MatchItem', getter, setter);
    ContentManager.registerResponseHandler('TableMatch', getter, setter);
})();
