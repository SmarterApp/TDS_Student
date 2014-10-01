/**
 *  The parsing is made up of 
 *     TTS.Parser.Container -> Wrapper interface for all the parsing logic
 *
 *     TTS.Parser.DOM  -> Parses Dom fragements / nodes (all others 'chain' into this parse method)
 *     TTS.Parser.HTML -> Parser an html string, then parsed by DOM 
 *     TTS.Parser.Selection -> Parses the user selection, then hands off to the DOM parser
 *
 *     TODO: More test cases
 *           Fix up the 'language divider' code for all the parsing elements.
 *           -Talk over attribute vs something else on the nodes themselves so that the selection
 *           code can be a bit smarter about its lookups.
 *           -Single dom walk to assign the lang divider first and only once?
 * //Example SSML tag
 * <SPAN class=”tts” ssml=”sub” ssml_alias=”the alternate text”>
 */
YUD = YAHOO.util.Dom;

TTS.Parser = {
    Types: {
        //Types of parser that you can 'suggest' the TTS.Parser.Container use
        Selection: 'Selection',
        DOM: 'DOM',
        HTML: 'HTML',
        TEXT: 'Text'
    }
};

/*
* @param parserType may be one of TTS.Parser.Types.Selection, TTS.Parser.Types.DOM or TTS.Parser.Types.HTML
* @param obj obj may be :
*           1. an array of javascript Node instances in case parserType is TTS.Parser.Types.DOM, 
*           2. a HTML string in case parserType is TTS.Parser.Types.HTML,
*           3. a frame/window or in case of opera a document in case parserType is TTS.Parser.Types.Selection
* @param mainLang the primary language of the test.
* @param altLang an optional parameter. if available it is the language at the botom of a stacked section. 
*/
TTS.Parser.Container = function(obj, parserType, mainLang, altLang) {
    this.$className = 'TTS.Parser.Container';
    this.object = obj;
    this.parserType = parserType;
    this.mainLang = mainLang;
    this.altLang = altLang;

    this.getObject = function() {
        return this.object;
    };
    this.getDOMFragForLanguage = function(language) {
        return this.docFrags[language];
    };
    this.getLanguages = function() {
        var languages = [this.mainLang];
        if (this.altLang) {
            languages.push(this.altLang);
        }
        return languages;
    };

    //Setup the actual temp variables, run the parser (more useful for command line debug)
    this.init();
};


TTS.Parser.Container.prototype.init = function() {
    this.lookup = {}; //A lookup that points to the ORIGINAL dom objects.
    this._id = 0; //used to create an id system for the lookups
    this.docFrags = [];
    this.prepareDOM();
};


//Top level function for getting the list of dom elements we are going to parse based on
//the parser type.
TTS.Parser.Container.prototype.prepareDOM = function() {

    //Break out the elements to process based on pased in parser type.
    var elementsToProcess,
        parser;

    if (this.parserType == TTS.Parser.Types.Selection) {
        parser = new TTS.Parser.Selection(this.addLookup.bind(this));
        elementsToProcess = parser.getElements(this.getObject());
    } else if (this.parserType == TTS.Parser.Types.DOM) {
        parser = new TTS.Parser.DOM();
        elementsToProcess = parser.getElements(this.getObject());
    } else if (this.parserType == TTS.Parser.Types.HTML) { //Assumes the HTML is well formed
        parser = new TTS.Parser.HTML();
        elementsToProcess = parser.getElements(this.getObject());
    } else {
        elementsToProcess = [];
    }

    //TODO: Split into something that fits into a page... or text
    //lets create document fragments for all possible languages
    var languages = this.getLanguages();

    //a stack of parent nodes index by languages available.
    var parentNodesStack = [];
    for (var j = 0; j < languages.length; ++j) {
        this.docFrags[languages[j]] = document.createDocumentFragment();

        parentNodesStack[languages[j]] = [];
        parentNodesStack[languages[j]].push(this.getDOMFragForLanguage(languages[j]));
    }
    /*
     * lets do a quick walk to make sure there is atleast 
     * one language divider element.  SB09242010 i probably can take care of it while 
     * walking the dom trying to build language text. but this is a retrofit for the time being.
    */
    this.sawLanguageDivider = TTS.Util.dfs(elementsToProcess, TTS.Util.hasLanguageAttr);

    //Walk the dom and look for language blocks and assign tts sub hacks.
    this.initialWalk(elementsToProcess, parentNodesStack, this.mainLang);
};

//Our means of language tagging is REALLY bad.    
TTS.Parser.Container.prototype.initialWalk = function(elementsToProcess, parentNodeStack, language) {
    for (var i = 0; i < elementsToProcess.length; ++i) {
        var el = elementsToProcess[i];
        this.walkDOM([el], parentNodeStack, language);
    }
};


//Remember there may be only one language divider in a section and its subsections!
TTS.Parser.Container.prototype.walkDOM = function(arrayOfDOMNodes, parentNodesStack, languageDetected) {
    var languageDividerNode = TTS.Util.hasLanguageAttributes(arrayOfDOMNodes);
    for (var i = 0; i < arrayOfDOMNodes.length; ++i) {
        var domNode = arrayOfDOMNodes[i];
        if (domNode == null) {
            continue;
        }

        //If this is a boundary node, the following content is another language.
        if (domNode == languageDividerNode) {
            languageDetected = this.altLang;
        } else {
            var nodeToAdd = domNode.cloneNode(false);

            //There will be some DUMB that comes out of this.
            this.addLookup(nodeToAdd, domNode);

            //Don't reset language tag if they were set by 'top' level walk dom parsing
            var lang = TTS.Util.getLang(domNode);
            if (!lang) {
                TTS.Util.markLang(domNode, languageDetected);
                lang = languageDetected;
            }

            //Add the elements to the stack.
            this.pushStackNode(nodeToAdd, parentNodesStack, lang);


            //ok so now we are done with adding child nodes. lets unwind the stack.
            this.walkDOM(domNode.childNodes, parentNodesStack, lang);

            //After we walk the child nodes, attempt to pop the element off.
            if (parentNodesStack && parentNodesStack[lang]) {
                parentNodesStack[lang].pop();
            }

            //In case this element was an input element, we actually have to hack up the 
            //language stacks...
            if (this.doInputHack(nodeToAdd, parentNodesStack)) {
                var langHack = (lang != this.mainLang) ? this.mainLang : this.altLang;

                //have to clone so it doesn't remove from the primary stack
                nodeToAdd = nodeToAdd.cloneNode(false);
                this.pushStackNode(nodeToAdd, parentNodesStack, langHack);
                if (parentNodesStack[langHack]) { //Remove the hacked on input.
                    parentNodesStack[langHack].pop();
                }
            }

        }
    }
};


/**
 *  Selection clones the original doc fragment, we want to ensure that when we parse the
 *  selection created doc fragment, we can keep a reference to the ORIGINAL html to highlight.
 */
TTS.Parser.Container.prototype.getLookup = function() {
    return this.lookup;
};

//We want to reference "above" the text node level and ensure our 'double' copy in the case
//of a selection allows us to highligh the actual dom elements.
TTS.Parser.Container.prototype.addLookup = function(clone, original) {
    if (!clone || !original) {
        console.error("Trying to createa  lookup with nothing.", clone, original);
        return;
    }

    if (this.lookup[original.id]) {
        original = this.lookup[original.id];
        clone.id = original.id;
    } else {
        clone.id = 'clone_' + (++this._id); //ALWAYS stomp the id of a clone to ensure it is unique.
    }
    this.lookup[clone.id] = original; //Original Node, first time points to the original dom
};

/**
 *  Horrible hack so that you can hear the tts parsing say "Option A" or "Option B" etc.
 */
TTS.Parser.Container.prototype.doInputHack = function(inp, stacks) {
    if (inp && inp.nodeName != 'INPUT') {
        return false;
    }

    var val = YUD.getAttribute(inp, 'value');
    if (val == null) {
        return false;
    }

    var langs = this.getLanguages();
    for (var i = 0; i < langs.length; ++i) {
        var lang = langs[i];
        //In case, we have some input elements that need specific TTS prefixes spoken out,
        // the element may bring with it the TTS key to look up in the i18N infrastructure (MS for example)
        // Otherwise, we look up based on a key we make up (legacy MC)
        var ttsKey = YUD.getAttribute(inp, 'data-tts-prefix');
        if (ttsKey == null) {
            ttsKey = 'TDSTTS.Speak.Option' + val;
        }
        var msg = Messages.getAlt(ttsKey+ "." + lang, " ");
        YUD.addClass(inp, 'tts');
        YUD.setAttribute(inp, lang, msg);
    }
    return true;
};


//Push a node onto the proper stack for this language
TTS.Parser.Container.prototype.pushStackNode = function(node, stacks, lng) {
    try {
        if (node && stacks && lng && stacks[lng]) {
            var stack = stacks[lng];
            var p = stack[stack.length - 1];
            p.appendChild(node);
            stack.push(node);
        }
    } catch(e) {
        console.error("Could not insert node, into stacks, with lang", node, stacks, lng);
    }

};

/** 
 *  For when you just want to get the play string (probably testing purposes till more is
 *  fixed up in the control).   Returning all the playInfo will be important for
 *  the actual highlight tracking.
 */
TTS.Parser.Container.prototype.getPlayText = function(lang, doNotAddTags) {
    var textArr = this.getPlayInfo(lang);
    var playStr = this.createPlayString(textArr, doNotAddTags);
    return playStr;
};

/**
 *  Make this cleaner for the get play info by lang lookup.
 */
TTS.Parser.Container.prototype.getPlayInfo = function(lang) {
    lang = lang || this.mainLang;
    var text = "";
    var obj = this.getDOMFragForLanguage(lang);
    if (obj == null) {
        return text;
    }

    //Use the DOM parser on the created fragment.  Very intentional new create vs ref
    //The text array is useful for when you actually want to do tracking.
    var parser = new TTS.Parser.DOM();
    parser.setLookup(this.getLookup());
    return parser.parse([obj]);
};


TTS.Parser.Container.prototype.createPlayString = function(textArr, doNotAddTags) {
    //TODO: Make this a final function vs a lookup string = word boundary hell.
    //Assemble the full string we will send to the final parser.
    var text;
    var fin = '';
    for (var i = 0; i < textArr.length; ++i) {
        var entry = textArr[i];
        text = entry.text;
        if (text != null && text != '') {
            fin = this.concatText(fin, text);
        }
        if (entry.forceSpace) { //If we added space to something like a table heading, ul etc.
            fin += ' ';
        }
        if (entry.forceSilence) { // If we added a silence marker
            fin += (entry.duration > 0) ? '{silence:' + entry.duration + '}' : '{silence}';
        }
    }
    return TTS.Util.cleanText(fin, doNotAddTags);
};

/*
 * We add a space if there is punctuation, otherwise we assume a space is actually placed based only
 * on actual text.  All other html <span> elements across text are considered markup.
 */
TTS.Parser.Container.prototype.concatText = function(txt, toadd) {
    if (toadd != null && toadd != '') {
        if (txt.match(/[\.!\?\#,]$/)) {
            txt = txt + " " + toadd;
        } else {
            txt += toadd;
        }
    }
    return txt;
};


/************************************************************************************************************/

TTS.Parser.Text = function() {
    this.getElements = function(text) {
        return [TTS.Util.createDiv(text)];
    };
};
TTS.Parser.HTML = function() { //Existing logic HTML==Text, but maybe more parsing should be done later?
    this.getElements = function(htmlStr) {
        return [TTS.Util.createDiv(htmlStr)];
    };
};
//Handles parsing the user selection into a documentFragment.
TTS.Parser.Selection = function(lookupFn) {
    this.addLookup = lookupFn;
};


TTS.Parser.Selection.prototype.getElements = function(obj) {
    obj = obj || document;

    //Because why not write the same code in the selection logic...
    var elementsToProcess = null;
    var selection = obj.getSelection();
    var commonAncestor = null;
    var hasAltInAncestor = false;
    var hasEmptySelection = true;

    if (selection.rangeCount > 0) {
        var sR = selection.getRangeAt(0);
        commonAncestor = sR.commonAncestorContainer;
        hasAltInAncestor = TTS.Util.bubble(commonAncestor, TTS.Util.hasAlt);
        hasEmptySelection = false;
    }

    if (!hasAltInAncestor && !hasEmptySelection) {
        var nodeToProcess = TTS.Util.bubble(commonAncestor, TTS.Util.getSub);
        if (nodeToProcess == null) {
            var docFrag = this.constructSelDocFrag(selection);
            elementsToProcess = docFrag.childNodes;
        } else {
            elementsToProcess = [nodeToProcess];
        }
    } else {
        elementsToProcess = [document.createElement('div')];
    }
    return elementsToProcess;
};

//THIS DOES DESERVE A CLASS.
//This is like a scary bubble up but only half the tree to find a lang block.
TTS.Parser.Selection.prototype.hasLanguageDividerInAncestry = function(node) {
    while (node && !("BODY" == node.nodeName || "IFRAME" == node.nodeName)) {
        var childNodesArray = node.childNodes;
        var languageDivider = TTS.Util.hasLanguageAttributes(childNodesArray);

        if (languageDivider != null) {
            return node;
        } else {
            node = node.parentNode;
        }
    }
    return null;
};

//Add nodes to DocFragment (ok, so why does it do this at all?)
TTS.Parser.Selection.prototype.addNodesToDocFrag = function (selection, wouldBeParent, currentNode) {
    
    //Actually required?
    var sR = selection.getRangeAt(0);

    var nodeToAdd = null;

    //if this node has language attributes then we just add it to the document fragment.
    if (TTS.Util.hasLanguageAttributes(currentNode)) {
        wouldBeParent.appendChild(currentNode);
    } else if (selection.containsNode(currentNode, true)) {

        //Add a lookup to the actual dom back into the container (assuming it was set)
        //is the node partially in there.
        //only startcontainer and endcountainers would be partially in the selection.
        if (sR.endContainer.nodeValue && sR.endContainer == currentNode) {
            //if start and end nodes are the same.
            var text = "";
            if (sR.endContainer == sR.startContainer) {
                text = sR.endContainer.nodeValue.substring(sR.startOffset, sR.endOffset);
            } else {
                text = sR.endContainer.nodeValue.substring(0, sR.endOffset);
            }

            //Determine Lang?"
            nodeToAdd = TTS.Util.createDiv(text);
            TTS.Util.markLang(nodeToAdd, TTS.Util.getLang(sR.endContainer));
            wouldBeParent.appendChild(nodeToAdd);
        } else if (sR.startContainer.nodeValue && sR.startContainer == currentNode) {
            var text = sR.startContainer.nodeValue.substring(
                sR.startOffset,
                sR.startContainer.nodeValue.length
            );
            nodeToAdd = TTS.Util.createDiv(text);

            TTS.Util.markLang(nodeToAdd, TTS.Util.getLang(sR.startContainer));
            wouldBeParent.appendChild(nodeToAdd);
        } else { //node is completely inside.
            nodeToAdd = currentNode.cloneNode(false);

            //TTS.Util.markLang(nodeToAdd, TTS.Util.getLang(currentNode));
            wouldBeParent.appendChild(nodeToAdd);
        }

        this.addLookup(nodeToAdd, currentNode);
    }

    //Now recurse through its children.
    if (currentNode != null) { // <-- in Justin's code this could of been null.. not sure what to do?
        for (var i = 0; i < currentNode.childNodes.length; ++i) {
            this.addNodesToDocFrag(selection, nodeToAdd, currentNode.childNodes[i]);
        }
    }
};

TTS.Parser.Selection.prototype.constructSelDocFrag = function(selection) {
    var sR = selection.getRangeAt(0);
    var commonAncestor = sR.commonAncestorContainer;
    var docFrag = document.createDocumentFragment();

    //we will check up in the hierarchy to see if there is a language divider.
    var languageDividerParent = this.hasLanguageDividerInAncestry(commonAncestor);

    //remember there may be only one language divider in a section.
    //so if we have language divider in the ancestry then we dont need to worry about having 
    //language divider in child nodes.
    if (languageDividerParent == null) {
        this.addNodesToDocFrag(selection, docFrag, commonAncestor);
    } else {
        this.addNodesToDocFrag(selection, docFrag, languageDividerParent);
    }
    return docFrag;
};


/**
 *  This is the actual parser that does all the work to get text strings, everything
 * else eventually just feeds into this parser.
 */
TTS.Parser.DOM = function(filterOutContextAreaAlt /* true/false param */) {
    this._filterOutContextAreaAlt = filterOutContextAreaAlt;
    this.TEXT_NODE = 3;
    this.ELEMENT_NODE = 1;
    this.SHOULD_ADD_SPACE = {
        H: true,
        H2: true,
        DT: true,
        DD: true,
        UL: true,
        LI: true
    };
    this._processFurther = true; //Hacky global sauce
    this.textArr = [];
    this.lookup = {};
};

//We expect only dom nodes, so just pass it back in the array it expects
TTS.Parser.DOM.prototype.getElements = function(obj) {
    if (obj && obj.length) {
        return obj; //Should already be an array / dom node?
    } else if (obj) {
        return [obj];
    }
    return [];
};

//Does all the actual text parsing.
TTS.Parser.DOM.prototype.parse = function(domArray) {
    //Parse through all the fragements in the document.
    for (var i = 0; i < domArray.length; i++) {
        this.parseDOMNode(domArray[i]);
    }
    return this.textArr;
};

TTS.Parser.DOM.prototype.parseDOMNode = function(domNode) {
    //Quick bail out statements that signal we don't want to parse down further.
    if (YUD.getAttribute(domNode, 'data-tts-skip') == 'true') {
        return;
    }
    if (TTS.Util.hasAlt(domNode)) {
        return;
    }

    /* Lets process the current node first. When we say process the current node - 
     * we mean process the attributes of the current node unless it is a text node.
     */
    this._processFurther = true;
    this.elementTypeAttributesToParserDelegate(domNode.nodeName.toUpperCase(), domNode);

    if (this._processFurther) {
        /*
        * this._processFurther is a global variable which is reset by a call to elementTypeAttributesToParserDelegate
        * if there are attributes which indicate that we should not process this node further.
        */
        var kids = domNode.childNodes;
        for (var i = 0; i < kids.length; i++) {
            var kid = kids[i];
            if (kid.nodeType == this.TEXT_NODE) {
                this.addText(kid.data, kid);
            } else if (kid.nodeType == this.ELEMENT_NODE) {
                this.elementTypeToParserDelegate(kid.nodeName.toUpperCase(), kid);
            }
        }
        if (this.SHOULD_ADD_SPACE[domNode.nodeName]) {
            this.forceSpace();
        }
    }
};

/**
 *  Create a map of the text nodes and ssml hacks we care about pointing to the nodes
 *  they represent in the actual dom (not our copied fragment)
 */
TTS.Parser.DOM.prototype.setLookup = function(lookup) {
    this.lookup = lookup;
};

//Add the text to a lookup that we will later use to generate the string sent to the speech engine
TTS.Parser.DOM.prototype.addText = function(text, node, selectAll) {
    if (text != null && !TTS.Util.ignore(node)) {
        text = text ? text.replace(/(\r\n|\n|\r)/gm, " ").replace(/\s+/gm, ' ') : '';
        if (text != '') {
            this.textArr.push({ text: text, node: this.lookup[node.id], selectAll: selectAll });
        }
    }
    return text;
};

//Force a space in between certain dom elements, ie table.   Good for fixing spacing of words where
//whitespace is missing.
TTS.Parser.DOM.prototype.forceSpace = function() {
    var last = this.textArr[this.textArr.length - 1];
    if (last && !last.forceSpace) {
        this.textArr.push({ text: ' ', forceSpace: true });
    }
};

//Force a {silence} in between certain dom elements, ie table.  
TTS.Parser.DOM.prototype.forceSilence = function (durationinMs) {
    var last = this.textArr[this.textArr.length - 1];
    if (last && !last.forceSilence) {
        this.textArr.push({ text: ' ', forceSilence: true, duration: durationinMs });
    }
};

/* We will need to parse attributes for some types of nodes e.g. the "alt" attribute for IMG nodes*/
TTS.Parser.DOM.prototype.elementTypeAttributesToParserDelegate = function(elmName, elmObj) {
    var text = null;
    if (elmName == "IMG") {
        text = this.parseImageElementAttributes(elmObj);
    } else if (elmName.toUpperCase() == 'MATH') {
        this._processFurther = false; //Stop the walker from going further into the mathml children
        text = this.parseMathMLElementAttributes(elmObj);
    } else if (elmObj.nodeType == this.TEXT_NODE) {
        text = elmObj.data;
    } else {
        var ssml = TTS.Util.getSub(elmObj, 'ssml_alias');
        if (ssml != null) {
            this._processFurther = false;
            text = ssml;
        }
    }
    this.addText(text, elmObj);
};

/* Check if the image has "alt" text. If it has alt text then return alt text else return empty */
TTS.Parser.DOM.prototype.parseImageElementAttributes = function(imgElm) {
    return (imgElm && imgElm.alt != null) ? imgElm.alt + ' ' : '';
};

/* Check if the MathMl node has "alt" text. If it has alt text then return alt text else return empty */
TTS.Parser.DOM.prototype.parseMathMLElementAttributes = function(mathElm) {
    if ("alt" in mathElm.attributes) {
        var altText = mathElm.attributes["alt"].nodeValue;
        if (altText != null) {
            return altText;
        }
    }
    return " ";
};

// used for parsing tables summary text
TTS.Parser.DOM.prototype.parseTableElementAttributes = function(table) {
    var summary = YUD.getAttribute(table, 'summary');
    if (summary != null) {
        this.addText(summary + " ", table, true);
    }
    return (summary != null) ? summary : '';
};

//Parser delegate
TTS.Parser.DOM.prototype.elementTypeToParserDelegate = function(elmName, elmObj) {
    if (elmName == "TABLE") {
        return this.parseTableElement(elmObj);
    }
    return this.parseDOMNode(elmObj);
};

/**
 * Summary information plus an empty space default
 */
TTS.Parser.DOM.prototype.parseTableElement = function(tbElm) {
    var tbNodes = tbElm.childNodes;

    // get table summary
    this.parseTableElementAttributes(tbElm);
    for (var i = 0; i < tbNodes.length; i++) {
        if (tbNodes[i].nodeName.toUpperCase() == "TBODY") {
            this.parseTableBody(tbNodes[i]);
        }
    }
};

/** 
 *  Parse out the SSML tag information. if it exists, else process
 */
TTS.Parser.DOM.prototype.getSSMLOrProcess = function(elmObj, alternateProcessDelegate) {
    //if this node is span then we will check for SSML tags.
    var ssml = TTS.Util.getSub(elmObj, 'ssml_alias');
    if (ssml) {
        this.addText(ssml, elmObj);
        return ssml;
    }

    //if not then use the alternate processing mechanism to extract text.
    if (typeof alternateProcessDelegate == 'function') {
        return alternateProcessDelegate.call(this, elmObj);
    }

    return null; // we didn't process anything
};

TTS.Parser.DOM.prototype.parseTableBody = function (tbBody) {

    if (TTS.Util.ignore(tbBody)) {
        return null;
    }

    var tbNodes = tbBody.childNodes;
    var txt = "";
    for (var i = 0; i < tbNodes.length; ++i) {
        var tn = tbNodes[i];
        if (tn.nodeName.toUpperCase() == "TR") {
            this.getSSMLOrProcess(tn, this.getTableRowText.bind(this));
        }
    }
    
    return txt;
};

/**
 * txt += " " + this.getTableCellText(tbNodes[i]) + " ";
 * SB06232011: We now want to introduce a pause as some voice packs coalesce text from 
 * different columns and read differently e.g. column 1 has the number 1 and column 2 has the 
 * number "November 1980". In such a case without the pause marker some voice packs read that 
 *  as "November 1st 1980". We do not want to add multiple consecutive {silence} tags.
 */
TTS.Parser.DOM.prototype.getTableRowText = function(tbRow) {
    if (TTS.Util.ignore(tbRow)) {
        return;
    }

    var tbNodes = tbRow.childNodes;
    var node = null;
    var name = null;
    var content = null;
    for (var i = 0; i < tbNodes.length; i++) {
        node = tbNodes[i];
        name = node && node.nodeName ? node.nodeName.toUpperCase() : '';
        if (name == "TH" || name == "TD") {
            this.getSSMLOrProcess(node, this.getTableCellText.bind(this));
            this.forceSilence(); //Force a space in between these elements.
        }
    }
};

//Get the text from a table cell.
TTS.Parser.DOM.prototype.getTableCellText = function(cellNode) {
    if (TTS.Util.ignore(cellNode)) {
        return;
    }

    var cellChildren = cellNode.childNodes;
    var txt = '';
    for (var i = 0; i < cellChildren.length; i++) {
        // TODO: why don't we use elementTypeAttributesToParserDelegate here?  The processFurther hack...
        var cellChild = cellChildren[i];
        if (cellChild.nodeType == this.TEXT_NODE) {
            this.addText(cellChild.data, cellChild);
        } else if (cellChild.nodeType == this.ELEMENT_NODE) {
            if (cellChild.nodeName == 'IMG') { // parse <img> in cell
                this.addText(this.parseImageElementAttributes(cellChild), cellChild);
            } else if (cellChild.nodeName.toUpperCase() == 'MATH') {
                this.addText(this.parseMathMLElementAttributes(cellChild), cellChild);
            } else { // process cell element (most likely <p> or <span>)
                this.getSSMLOrProcess(cellChild, this.getTableCellText.bind(this));
            }
        }
    }
};