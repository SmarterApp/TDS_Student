
////////////// Word list Panel logic.  Handles the singleton word list panel tool.
// This class is all static methods/data for handling word list items in general.
function WordListPanel() {
}

//////////////// globals (class static)  /////////////////////////

// This is the attribute name in the query string
WordListPanel.queryName = "key";
WordListPanel.bankKeyHdr = "bankKey";
WordListPanel.itemKeyHdr = "itemKey";
WordListPanel.indexHdr = "index";
WordListPanel.AccType = "Word List";
WordListPanel.AccNoAccs = "TDS_WL0";
WordListPanel.AccHdr = "TDS_ACCS";

// Arbitrary name of div that will contain the word list panel
WordListPanel.divId = "WordListTool";
WordListPanel.toolDiv = null; // singleton panel container div
WordListPanel.panel = null; // singleton word list panel
WordListPanel.tabView = null; // singleton panel content div
WordListPanel.tabCount = 0; // used for hotkey tabbing in WL pane
WordListPanel.tabCurrent = 0;

// Save word list yet-to-be-sent requests in a queue
WordListPanel.requestQ = [];
WordListPanel.indices = '';

WordListPanel.LoadingPageString = "<div id=\"word-list-list-div\" class=\"yui-navset\">\r\n<ul class=\"yui-nav\"> " +
    "<li class=\"selected\"><a href=\"#word-list-tab1\"><em>Loading</em></a></li></ul>\r\n" +
    "   <div class=\"yui-content\"> " +
    "<div id=\"word-list-tab1\"><p>Please Wait</p></div> </div> ";

WordListPanel.ErrorPageString = "<div id=\"word-list-list-div\" class=\"yui-navset\">\r\n<ul class=\"yui-nav\"> " +
    "<li class=\"selected\"><a href=\"#word-list-tab1\"><em>Information</em></a></li></ul>\r\n" +
    "   <div class=\"yui-content\"> " +
    "<div id=\"word-list-tab1\"><p>Content Not Found.</p></div> </div> ";

// URL that we agree on with server 
WordListPanel.xhrUrl = "Pages/API/WordList.axd";
// method name that we agree on with server, per AIR convention.
WordListPanel.xhrMethod = "resolve";

// This is the id of the content div in the word list pane.
WordListPanel.tabbedDivName = "word-list-list-div";

// Tagged span in html has the form bbb-iii:xxx, where
// bbb=bankKey, iii=itemKey, xxx=index withing the word list.
// parse it for easier consumption by the student app.
// TODO: The format of this is gonna change based on feedback from
// ITS tools team
WordListPanel.splitter = new RegExp("(\\d+):(\\w+)");

// Avoid doing deep fetch for this def, if we've already done it.
WordListPanel.contentWordCache = [];
WordListPanel.headerWordCache = [];
WordListPanel.message = [];
WordListPanel.failedRequest = [];

WordListPanel.getKeyFromQEntry = function(entry) {
    var keyIndex = YUD.getAttribute(entry.span, WordListItem.attributeName);
    var bankKey = entry.wl_item.wl_res.bankKey;
    var itemKey = entry.wl_item.wl_res.itemKey;
    return bankKey + '-' + itemKey + '-' + keyIndex;
};

/////////// Event handlers ///////////////////////////

// YUI Connection Manager callback response.  This happens when the POST response comes back.
WordListPanel.postCallback = {
    success: (function (resp) {
        // Parse the returned JSON structure, containing an array of objects
        var messages = YAHOO.lang.JSON.parse(resp.responseText);
        for (var i = 0; messages != null && i < messages.length; ++i) {
            // Create HTML for WordList dialog
            var tabString = WordListPanel.RenderHtmlTabs(messages[i]);

            var key = messages[i].EntryKey;
            var itemKey = key.split('-')[1];

            //store the data so we don't have to fetch it again.
            WordListPanel.contentWordCache[key] = tabString;
            WordListPanel.message[key] = messages[i];
        }
        
        // Tag the individual spans that returned WordList entries
        if (WordListPanel.requestQ[itemKey] == null) {
            console.log("word list: server returned empty response, returning.");
            return;
        }
        for (i = 0; i < WordListPanel.requestQ[itemKey].length; ++i) {
            var entry = WordListPanel.requestQ[itemKey][i];
            var cacheEntry = WordListPanel.message[entry.key];
            if (cacheEntry && cacheEntry.EntryFound) {
                entry.wl_item.TagSingleSpan(entry.span);
            }
        }
    }),
    argument: []
};


// User has clicked on a span.  Search for the definition
WordListPanel.processClick = (function (entry, headerText) {
    if (entry != null) {
        if ((WordListPanel.panel != null) && (WordListPanel.toolDiv != null)) {
            var cacheKey = WordListPanel.getKeyFromQEntry(entry);
            
            // Do we already know this word?
            if (WordListPanel.contentWordCache[cacheKey] != null) {
                // yep, look it up.

                // It is possible that multiple words/phrases map to same
                // definition.  So update the header with that word/phrase.
                WordListPanel.headerWordCache[cacheKey] = headerText;

                WordListPanel.setPanel(WordListPanel.headerWordCache[cacheKey], WordListPanel.contentWordCache[cacheKey]);
            } else {
                ContentManager.log("WordList: errantly tagged span");
            }
        }
    }
});

/////////////////// Helper logic /////////////////////

// Unpack the item string and send the word list POST request
WordListPanel.sendRequest = (function (wl_item) {
    var bankKey = wl_item.wl_res.bankKey;
    var itemKey = wl_item.wl_res.itemKey;

    // If there are no tagged spans, don't send the request at all.
    if (typeof(WordListPanel.indices) != "string" || WordListPanel.indices.length < 1)
        return;
    
    // build the form post data 
    var str = WordListPanel.bankKeyHdr + "=" + bankKey.toString();
    str = str + "&" + WordListPanel.itemKeyHdr + "=" + itemKey.toString();
    str = str + "&" + WordListPanel.indices;

    // get the selected WL accommodations
    var wlCodes = Accommodations.Manager.getCurrent().getType(WordListPanel.AccType).getCodes(true);

    // build acc codes post
    for (var i = 0, ii = wlCodes.length; i < ii; i++) {
        var wlInner = wlCodes[i].split('&');
        for (var j = 0; j < wlInner.length; ++j) {
            str += "&" + WordListPanel.AccHdr + "=" + wlInner[j];
        }
    }

    var urlString = TDS.baseUrl + WordListPanel.xhrUrl + "/" + WordListPanel.xhrMethod;
    
    // This is a hack for ItemPreview and related tools, which is different from Student.
    // If WordListPanel.xhrUrl looks like a fully-qualified URL then use it alone, otherwise
    // construct the URL from baseUrl (Student app).
    if (/^http/.test(WordListPanel.xhrUrl))
        urlString = WordListPanel.xhrUrl + "/" + WordListPanel.xhrMethod;
    
    YAHOO.util.Connect.asyncRequest('POST', urlString, WordListPanel.postCallback, str);

    // Display loading screen.
    // WordListPanel.setPanel(WordListPanel.headerWordCache[key], WordListPanel.LoadingPageString);
});

// Are there any TDS_WL configs in the accs cookie?  If not then
// don't bother to fetch any data we can't even display
WordListPanel.IsWordListEnabled = (function () {

    var accs = Accommodations.Manager.getCurrent();
    var wlType = accs.getType(WordListPanel.AccType);

    // check if type exists
    if (!wlType) {
        return false;
    }

    // check if values exist
    var wlValues = wlType.getSelected();
    if (wlValues.length == 0) {
        return false;
    }

    // If one of the acc strings is 'no word list', don't enable word list.
    for (var i = 0; i < wlValues.length; ++i) {
        var codes = wlValues[i].getCodes();
        for (var j = 0; j < codes.length; ++j) {
            if (codes[j] == WordListPanel.AccNoAccs) {
                return false;
            }
        }
    }

    // if we got here then 'TDS_WL0' was not included
    return true;
});

// Helper function to show the word view panel
// args: hd (header html) bd (body html)
WordListPanel.setPanel = function (hd, bd) {
    if ((WordListPanel.panel != null) && (WordListPanel.toolDiv != null)) {
        if (hd != null) {
            WordListPanel.panel.setHeader(hd);
        }
        WordListPanel.panel.setBody(bd);
        WordListPanel.panel.show();

        WordListPanel.tabView = new YAHOO.widget.TabView(WordListPanel.tabbedDivName);

        WordListPanel.tabCount = 0;
        while (WordListPanel.tabView.getTab(WordListPanel.tabCount) != null) {
            WordListPanel.tabCount++;
        }
        WordListPanel.tabCurrent = 0;
        
        setTimeout(function () {
            WordListPanel.postProcessAudioTags();
        }.bind(this), 1); //Panels can behave in a scary fashion
    }
};

// Set up the word list pane.  Should only be done once.
WordListPanel.InitializePane = (function () {
    
    // This next line fixes a bug in YUI2 where active tab
    // has a title attribute which generates a tooltip
    YAHOO.widget.Tab.prototype.ACTIVE_TITLE = '';

    var toolDiv = document.getElementById("tools");
    if (toolDiv != null) {
        WordListPanel.toolDiv = document.createElement("div");
        YUD.addClass(WordListPanel.toolDiv, "yui-dialog focused");
        YUD.setAttribute(WordListPanel.toolDiv, 'id', WordListPanel.divId);
        var clink = document.createElement("link");
        YUD.setAttribute(clink, 'type', 'text/css');
        YUD.setAttribute(clink, 'rel', 'stylesheet');
        YUD.setAttribute(clink, 'media', 'screen');
        YUD.setAttribute(clink, 'href', ContentManager.resolveBaseUrl('Scripts/Libraries/YUI/tabview/assets/skins/sam/tabview.css'));
        WordListPanel.toolDiv.appendChild(clink);

        toolDiv.appendChild(WordListPanel.toolDiv);
        WordListPanel.panel = new YAHOO.widget.Panel("wordListPanel", { width: "320px", zindex: 1004, visible:false, constraintoviewport: true });
        WordListPanel.panel.render(WordListPanel.toolDiv);
    }
});

// From parsed JSON POST response, construct the YUI Tab formats with the response.
WordListPanel.RenderHtmlTabs = function (messages) {
    var tabString = "<div id=\"" + WordListPanel.tabbedDivName + "\" class=\"yui-navset\"> \r\n";
    tabString = tabString + "<ul class=\"yui-nav\">\r\n";
    var contentString = " <div class=\"yui-content\">\r\n";
    var i;
    for (i = 0; i < messages.Entries.length; ++i) {
        tabString = tabString + "<li";
        if (i == 0) {
            tabString = tabString + " class=\"selected\"";
        }
        tabString = tabString + "> <a href=\"#word-list-" + i.toString();

        // Try to find wl in i18n
        var wlTypeTranslation = Messages.get('TDS.WordList.' + messages.Entries[i].wlType);
        tabString = tabString + "\">" + wlTypeTranslation + "</a></li>\r\n";
        contentString = contentString + "<div id=\"word-list-" + i.toString() + "\"><p>" + messages.Entries[i].wlContent + "</p></div>";
    }
    tabString = tabString + "</ul>";
    tabString = tabString + contentString + "</div>";
    return tabString;
};

/**
 *  We must support the creation of audio elements in word panels, this requires us to
 *  process things into sound manager sounds for certain browser types.
 */
WordListPanel.postProcessAudioTags = function () {
    if (!window.TDS || !window.TDS.Audio || !window.TDS.Audio.Widget) { return; }

    var bd = WordListPanel.panel.body;
    try {
        if (!bd) { return; }

        // Url resolver puts the encoded path in the anchor tags.
        var audioEls = YAHOO.util.Selector.query('a', bd) || [];

        for (var i = 0; i < audioEls.length; ++i) {
            var span = audioEls[i];
            if ((span) && (span.href)) {
                var href = span.href;

                // Is this an audio span?
                // Bug 132322: use indexOf instead of contains for x-browser compatibilty
                if (href.indexOf('.ogg') != -1 || (href.indexOf('.m4a') != -1)) {

                    // give it some stylin'                    
                    YUD.addClass(span, 'sound_repeat');

                    // Url decoder expects stuff to go inside html so we need to unHTML encode the href:
                    href = href.replace(/&amp;/gmi, '&');
                    span.href = href;

                    // Now we have a valid href in an anchor span.  So replace the anchor with an audio span
                    // This handles things like show/hide audio widget.
                    TDS.Audio.Widget.createPlayer(span);
                }
            }
        }
    } catch (e) {
        console.error("Error creating players in the word list panel (error, dom).", e, bd);
    }

};

