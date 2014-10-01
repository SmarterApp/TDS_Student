// YUI ALIASES
/*
if (YUD == null) var YUD = YAHOO.util.Dom;
if (YUE == null) var YUE = YAHOO.util.Event;
if (YUL == null) var YUL = YAHOO.util.Lang;
if ($ == null) var $ = YAHOO.util.Dom.get;
if ($$ == null) var $$ = YAHOO.util.Dom.getElementsByClassName; // get by class
var Lang = YAHOO.util.Lang;
*/

/*
MANAGER EVENTS:
init
*/

/*

CONTENT EVENTS:

"rendered": html is rendered
"available": object created
"loaded": resources have finished loading and page is ready to be shown

* onPageEvent
- rendered
- available
- ready
- show
- hide
- zoom
- keyevent
- mouseevent
* onEntityEvent
- available
- ready
- show
- hide
- zoom
- focus
- blur
- keyevent
- mouseevent
- menushow
- menuhide
* onComponentEvent
- focus
- blur

ContentManager.onPageEvent('ready', function(page) { });
ContentManager.onEntityEvent('focus', function(page, entity) { });
ContentManager.onItemEvent('focus', function(page, item) { });
ContentManager.onPassageEvent('focus', function(page, passage) { });
ContentManager.onComponentEvent('focus', function(page, entity, component) { });

*/

// The goal of the content manager is to collect events and hand them off to the widgets. It also keeps state about the current pages.
var ContentManager = {

    _pages: new Util.Structs.Map(),
    _currentPage: null, // the current page
    _contextHandlerMap: {}, // for getting context areas

    // events fired from content manager
    _events: new Util.EventManager(ContentManager),

    // events fired from content (page, item, passage, components)
    _contentEvents: new Util.EventManager(ContentManager),

    accessibilityEnabled: false,
    enableKeyEvents: true, // enable shortcuts
    enableMouseEvents: true, // enable mouse clicks (including context menu)
    enableARIA: false, // enable ARIA fixes

    _readOnly: false, // is the UI in read only mode

    // show more console logs
    _debug: true
};

// namespaces for adding modules
ContentManager.Modules = {};

// toggle if read-only is enabled for all items
ContentManager.setReadOnly = function(value) {
    this._readOnly = value;
};

// check if read-only is enabled globally
ContentManager.isReadOnly = function() {
    return this._readOnly;
};

// the base url for looking up scripts and styles
ContentManager._baseUrl = '';

ContentManager.getBaseUrl = function () {
    return this._baseUrl;
};

ContentManager.setBaseUrl = function (baseUrl) {
    this._baseUrl = baseUrl;
};

ContentManager.resolveBaseUrl = function(url) {
    // check if url is already resolved
    if (Util.String.isHttpProtocol(url)) {
        return url;
    }

    // combine url
    return this._baseUrl + url;
};

// enables accessibility features
ContentManager.enableAccessibility = function() {
    this.accessibilityEnabled = true;
    this.enableKeyEvents = false;
    this.enableMouseEvents = false;
    this.enableARIA = true;
};

// if this is true then the content manager accessibility features are enabled 
ContentManager.isAccessibilityEnabled = function() {
    return this.accessibilityEnabled;
};

// call this before using the content manager to set things up
ContentManager.init = function (baseUrl) {

    // set custom base url if provided
    if (YAHOO.lang.isString(baseUrl)) {
        ContentManager.setBaseUrl(baseUrl);
    }

    // listen for keyboard events on main document
    ContentManager.addKeyEvents(document);

    // add yui menu fix to parent (mostly for item preview dev)
    if (window != parent) {
        try {
            ContentManager.addKeyEvents(parent.document);
            ContentManager.Menu.applyDocFix(parent);
        } catch (ex) {
            // Permission denied if cross-domain parent
        }
    }

    // create context menu instance if it does not exist
    if (this.enableKeyEvents || this.enableMouseEvents) {
        ContentManager.Menu.init();
    }

    // check if this is mobile device
    if (Util.Browser.isTouchDevice()) {
        ContentManager.listenForFocus(document);
    }

    ContentManager.fireEvent('init');
};

ContentManager.getAccommodations = function() {
    return Accommodations.Manager.getDefault();
};

ContentManager.getAccs = ContentManager.getAccommodations;

ContentManager.getAccommodationProperties = function() {
    var accommodations = this.getAccommodations();
    return new Accommodations.Properties(accommodations);
};

ContentManager.getAccProps = ContentManager.getAccommodationProperties;

// a helper function for getting the current language from accommodations
ContentManager.getLanguage = function() {
    var accProps = this.getAccommodationProperties();
    var language = accProps.getSelectedCode('Language');
    return (language) ? language : 'ENU';
};

ContentManager.getPages = function() {
    return this._pages.getValues();
};

// get a page by the ID of the iframe
ContentManager.getPage = function(id) {
    return this._pages.get(id);
};

ContentManager.setCurrentPage = function(page) {
    this._currentPage = page;
};

// get the page currently being viewed
ContentManager.getCurrentPage = function() {
    return this._currentPage;
};

// call this function to create a new content page based on json data
ContentManager.createPage = function (content) {

    // make sure content is a object and has an ID
    if (!YAHOO.lang.isObject(content) || !YAHOO.lang.isString(content.id)) {
        return null;
    }

    // check if there is an existing page
    var existingPage = ContentManager.getPage(content.id);
    if (existingPage) {
        return existingPage;
    }

    // create page object and assign internally to track
    var page = new ContentPage(content);

    // store page
    // TODO: remove any existing pages with same name
    ContentManager._pages.set(content.id, page);

    // fire page event and not entity events
    ContentManager.firePageEvent('init', page, [content], false);

    // get content passage (also make sure the element actually exists in case someone used wrong layout)
    if (content.passage) {
        var passage = ContentManager._createPassage(page, content.passage);
        ContentManager.fireEntityEvent('init', passage, [content.passage]);
    }

    // get content items
    Util.Array.each(content.items, function(itsItem) {
        var item = ContentManager._createItem(page, itsItem);
        ContentManager.fireEntityEvent('init', item, [itsItem]);
    });

    return page;
};

// set the passage from the frames its.passage object
ContentManager._createPassage = function (page, itsPassage) {

    var passage = new ContentPassage(page, itsPassage.bankKey, itsPassage.itemKey, itsPassage.filePath);

    // store
    page.setPassage(passage);

    // content data
    passage.stemTTS = itsPassage.stemTTS;
    passage.printed = itsPassage.printed;

    // resources
    passage.resources = itsPassage.resources;
    passage.attachments = itsPassage.attachments;

    // copy xml specs
    passage.specs = itsPassage.specs;

    return passage;
};

// add a item from the frames its.items[] object
ContentManager._createItem = function (page, itsItem) {

    var item = new ContentItem(page, itsItem.bankKey, itsItem.itemKey, itsItem.filePath,
        itsItem.format, itsItem.responseType, itsItem.grade, itsItem.subject, itsItem.position);

    // store item
    page.addItem(item);

    // TTS
    item.stemTTS = itsItem.stemTTS;
    item.illustrationTTS = itsItem.illustrationTTS;

    // render spec
    item.rendererSpec = itsItem.rendererSpec || null;

    // grid
    item.gridAnswerSpace = itsItem.gridAnswerSpace;

    // tutorial
    item.tutorial = itsItem.tutorial;

    // resources (should never be null)
    item.resources = itsItem.resources;
    item.attachments = itsItem.attachments;

    if (itsItem.resources) {
        item.gtr = itsItem.resources['guideToRevision']; // GTR
        item.coverPage = itsItem.resources['coverPage']; // cover sheet
    }

    // response
    item.value = itsItem.value;
    item.printed = itsItem.printed;

    // rubric (optional)
    item.rubric = itsItem.rubric;

    // qti
    if (itsItem.qti) {
        item.qti = itsItem.qti;
    }

    // copy xml specs
    item.specs = itsItem.specs;

    return item;
};

// apply required fixes to a pages window and document for content rendering
ContentManager._processPage = function (page) {

    var pageDoc = page.getDoc(),
        pageWin = page.getWin();

    // check if we already processed this doc
    if (pageWin.__tds_processed) {
        return;
    }

    // apply YUI menu events to iframe (NOTE: you must create a menu at some point for this to work)
    ContentManager.Menu.applyDocFix(pageDoc);

    // listen for keyboard events on the pages document
    ContentManager.addKeyEvents(pageDoc);

    // stop drag/scroll DOM events
    Util.Dom.stopDragEvents(pageDoc);

    // stop right click on regular browsers
    Util.Dom.stopAllEvents(pageDoc, 'contextmenu');

    // stop middle click
    YUE.on(pageDoc, 'mousedown', function (ev) {
        if (ev.button == 1) {
            YUE.stopEvent(ev);
        }
    });

    pageWin.__tds_processed = true;
};

// go through all the pages and find an item that matches this position
ContentManager.getItem = function (position) {

    var pages = this.getPages();

    for (var i = 0; i < pages.length; i++) {
        var page = pages[i];
        var item = page.getItem(position);
        if (item) {
            return item;
        }
    }

    return null;
};

// call this function to set an item as being completed (e.x., when last option is chosen in scaffolding)
ContentManager.setItemCompleted = function(item) {

    if (item == null) {
        return;
    }

    // fire completed event for this item
    ContentManager.fireEntityEvent('completed', item);

    // fire completed event for this page if it only has one item
    var page = item.getPage();
    var items = page.getItems();
    if (items.length == 1) {
        ContentManager.firePageEvent('completed', page);
    }
};

// removes page
ContentManager.removePage = function (page) {

    // hide page if it is showing
    if (page.isShowing()) {
        page.hide();
    }

    // remove page from DOM
    ContentManager.Renderer.removePage(page);

    // remove from hash
    this._pages.remove(page.id);

    // dispose of page object
    page.dispose();
};

/******************************************************************************************/

// define helper functions for subscribing to events
ContentManager.onEvent = function(name, callback, cancellable) {
    this._events.subscribe(name, callback, null, cancellable);
};

ContentManager.onPageEvent = function(name, callback, cancellable) {
    this._contentEvents.subscribe('page' + name, callback, null, cancellable);
};

ContentManager.onEntityEvent = function(name, callback) {
    this._contentEvents.subscribe('entity' + name, callback);
};

ContentManager.onItemEvent = function(name, callback) {
    this._contentEvents.subscribe('item' + name, callback);
};

ContentManager.onPassageEvent = function(name, callback) {
    this._contentEvents.subscribe('passage' + name, callback);
};

ContentManager.onComponentEvent = function(name, callback) {
    this._contentEvents.subscribe('component' + name, callback);
};

// fire a generic event for the content manager
ContentManager.fireEvent = function(name, args) {
    var ret = this._events.notify(name, args);
    return ret;
};

// fire an event for the page and each passage/items
ContentManager.firePageEvent = function (name, page, args, fireEntityEvents) {

    // if the page argument is a string then lookup the page
    if (YLang.isString(page)) {
        page = ContentManager.getPage(page);
    }

    // check if there is a page
    if (!(page instanceof ContentPage)) {
        return null;
    }

    // fire event for page (return value will be NULL if there are no subscribers)
    var pageArgs = [page].concat(args || []);
    var ret = this._contentEvents.notify('page' + name, pageArgs);

    // check if firing entity events is allowed and the page events didn't get cancelled
    // note: event can only be cancelled if when subscribe() was called cancellable param was set to 'true'
    if (fireEntityEvents === true && ret !== false) {

        // fire event for passage if any
        var passage = page.getPassage();

        if (passage) {
            this.fireEntityEvent(name, passage, args);
        }

        // fire event for each item
        var items = page.getItems();

        for (var i = 0; i < items.length; i++) {
            this.fireEntityEvent(name, items[i], args);
        }
    }

    return ret;
};

// fire an entity event (if this returns true then the event was cancelled)
ContentManager.fireEntityEvent = function (name, entity, args) {

    // check if item is unsupported
    var unsupported = (entity instanceof ContentItem && entity.isSupported() === false);
    if (unsupported && name != 'unsupported') { // HACK: allow 'unsupported' event
        return; // ignore this event
    }

    var page = entity.getPage();
    var entityArgs = [page, entity].concat(args || []);

    // first fire entity event
    this._contentEvents.notify('entity' + name, entityArgs);

    // then fire item or passage event
    if (entity instanceof ContentItem) {
        this._contentEvents.notify('item' + name, entityArgs);
    } else if (entity instanceof ContentPassage) {
        this._contentEvents.notify('passage' + name, entityArgs);
    }
};

ContentManager.fireComponentEvent = function(name, entity, component, args) {
    var page = entity.getPage();
    var componentArgs = [page, entity, component].concat(args || []);
    this._contentEvents.notify('component' + name, componentArgs);
};

/******************************************************************************************/

// response type handler
(function(CM) {

    var lookup = {};

    // register a response handler
    // @responseType = items response types you want to work with
    // @callback = the function used to return data for this action
    CM.registerResponseHandler = function(responseType, callbackGet, callbackSet) {
        lookup[responseType.toLowerCase()] = {
            getter: callbackGet,
            setter: callbackSet
        };
    };

    CM.getResponseHandler = function(responseType) {
        return lookup[responseType.toLowerCase()];
    };

    CM.getResponseHandlers = function() {
        return Util.Object.getValues(lookup);
    };

    CM.removeResponseHandler = function(responseType) {
        return Util.Object.remove(lookup, responseType);
    };

    CM.removeResponseHandlers = function() {
        var responseTypes = Util.Object.keys(lookup);
        for (var i = 0; i < responseTypes.length; i++) {
            CM.removeResponseHandler(responseTypes[i]);
        }
    };

})(ContentManager);

/******************************************************************************************/

// response type support
(function(CM) {

    var lookup = {};

    CM.registerSupportHandler = function(responseType, check) {
        lookup[responseType.toLowerCase()] = check;
    };

    CM.getSupportHandler = function(responseType) {
        return lookup[responseType.toLowerCase()];
    };

    CM.getSupportHandlers = function() {
        return Util.Object.getValues(lookup);
    };

    CM.removeSupportHandler = function(responseType) {
        return Util.Object.remove(lookup, responseType);
    };

    CM.removeSupportHandlers = function() {
        var responseTypes = Util.Object.keys(lookup);
        for (var i = 0; i < responseTypes.length; i++) {
            CM.removeSupportHandler(responseTypes[i]);
        }
    };

})(ContentManager);

/******************************************************************************************/

// attach mouse events to an element for a specific item
ContentManager.addMouseEvents = function (entity, element) {

    if (!this.enableMouseEvents) {
        return false;
    }
    if (element == null) {
        return false;
    }
    if (element.__tds_mouseEventsEnabled === true) {
        return false;
    }

    var page = entity.getPage();

    // Set entity as focused when clicked.
    // NOTE: You can't use 'touchstart' for tablets or scrolling will stop working
    // Bug 119445 for iOS, use touchstart, otherwise touch-hold won't properly select text
    //            Verified that scrolling still works on iPads with this fix
    var activeElementEvent = Util.Browser.isIOS() ? 'touchstart' : 'mousedown';

    YUE.on(element, activeElementEvent, function (evt) {

        // ignore setting active element if we are editing
        // if (Util.Dom.isTextInput(document.activeElement)) return;

        // set focus to this entity
        entity.setActive(evt);

        // set focus to the component
        var target = YUE.getTarget(evt);

        // check if the user right clicked on a component and focus on it
        var clickedComponent = entity.findComponent(target);

        // if a component was found then set it as active
        if (clickedComponent) {
            entity.setActiveComponent(clickedComponent);
        }
        // if no component was found and there isn't an active one then set the default
        else if (entity.getActiveComponent() == null) {
            entity.resetComponent();
        }

    }, this, true);

    // listen for html events on the item
    var fireMouseEvent = function (evt) {

        // fire off mouse events
        this.firePageEvent('mouseevent', page, [evt], false);
        this.fireEntityEvent('mouseevent', entity, [evt]);

        // check if context menu was activated
        if (Util.Browser.isSecure() && Util.Browser.isMac()) {
            // BUG: In SB on OS X the 'contextmenu' event does not fire so we 
            // need to check if right click button was pressed on 'mousedown'
            if (evt.type == 'mousedown' && evt.button == 2) {
                // BUG: must be scheduled or menu closes right away after showing
                setTimeout(function() {
                    ContentManager.Menu.show(evt);
                }, 0);
            }
        } else if (evt.type == 'contextmenu') {
            ContentManager.Menu.show(evt);
        }
    };

    // set entity as focused when clicked
    YUE.on(element, 'mousedown', fireMouseEvent, this, true);
    YUE.on(element, 'mouseup', fireMouseEvent, this, true);
    YUE.on(element, 'contextmenu', fireMouseEvent, this, true);

    element.__tds_mouseEventsEnabled = true;
    return true;
};

/******************************************************************************************/

ContentManager.getEventXY = function(domEvent, clickXY) {
    var element = YUE.getTarget(domEvent);
    if (!clickXY) {
        clickXY = YUE.getXY(domEvent);
    }

    // check if the elements document is contained in an iframe
    while (Util.Dom.getWindow(element.ownerDocument).frameElement &&
        !Util.Dom.getWindow(element.ownerDocument).its) {
        element = Util.Dom.getWindow(element.ownerDocument).frameElement;

        var elementXY = YUD.getXY(element);

        clickXY[0] = clickXY[0] + elementXY[0];
        clickXY[1] = clickXY[1] + elementXY[1];
    }

    return clickXY;
};

// turn java string object into js string
ContentManager.getJavaString = function(value) {
    if (value != null && typeof (value) == 'object') {
        value = new String(value).toString();
    }

    return value;
};

// this resets the active component for an item
ContentManager.resetActiveComponent = function() {
    var page = ContentManager.getCurrentPage();
    if (page) {
        var entity = page.getActiveEntity();
        if (entity instanceof ContentItem) {
            entity.resetComponent();
        }
    }
};

// Enable/disable caret mode (call with no arguments to toggle mode)
ContentManager.enableCaretMode = function(enable) {
    var enabled = Mozilla.enableCaretMode(enable);

    if (enabled) {
        var entity = ContentManager.getCurrentPage().getActiveEntity();
        entity.resetCaretPosition();
    }
};

ContentManager.focus = function(obj) {
    if (ContentManager.enableARIA) {
        return false;
    }
    return Util.Dom.focus(obj);
};

ContentManager.blur = function(obj) {
    if (ContentManager.enableARIA) {
        return false;
    }
    return Util.Dom.blur(obj);
};

// call this to prevent focus on an element
ContentManager.preventFocus = function(el) {
    YUE.onFocus(el, function() {
        ContentManager.blur(el);
    });
};

// check if an anchor tag is clickable
ContentManager.isLinkClickable = function(link) {
    var mediaType = link.getAttribute('type');
    if (mediaType == null) {
        return false;
    }

    // check if link has allowed media type
    var supportedMediaTypes = ['application/rtf', 'application/pdf'];
    return (supportedMediaTypes.indexOf(mediaType) != -1);
};

ContentManager.getAncestor = Util.Dom.getAncestor;
ContentManager.isElement = Util.Dom.isElement;
ContentManager.isVisible = Util.Dom.isVisible;

// CSS CLASSES
ContentManager.CSS = {
    CONTEXT_AREA: 'contextArea',
    CONTEXT_AREA_ALT: 'contextArea-hasalt',
    CONTEXT_AREA_FOCUS: 'contextArea-focused',
    CONTEXT_BODY_FOCUS: 'contextBody-focused',
    CONTEXT_AREA_SELECT: 'contextArea-selected',
    CONTEXT_BODY_SELECT: 'contextBody-selected',
    MARK_REVIEW_OFF: 'markReview',
    MARK_REVIEW_ON: 'markReviewMarked',
    HIGHLIGHT: 'highlight'
};

// LABELS
ContentManager._labels = {};
ContentManager._labels['TTS_SPEAKTEXT'] = { classname: 'speaktext', text: 'TDSContentJS.Label.SpeakSelected' };
ContentManager._labels['TTS_SPEAKSECTION'] = { classname: 'speaksection', text: 'Speak {title}' };
ContentManager._labels['TTS_SPEAKSECTION_ALT'] = { classname: 'speaksection', text: 'Speak {title}' }; // (Alt)
ContentManager._labels['TTS_STOP'] = { classname: 'speakstop', text: 'TDSContentJS.Label.StopSpeaking' };
ContentManager._labels['HIGHLIGHT_TEXT'] = { classname: 'highlighttext', text: 'TDSContentJS.Label.Highlight' };
ContentManager._labels['HIGHLIGHT_CLEAR'] = { classname: 'highlightclear', text: 'TDSContentJS.Label.ResetHighlight' };

ContentManager.getLabel = function(id) {
    var label = ContentManager._labels[id.toUpperCase()];
    if (label) {
        return { classname: label.classname, text: Messages.get(label.text) };
    }
    return null;
};

/*
TDS_SC0	None	            1   Student comments feature is disabled
TDS_SCDropDown	DropDown	0	Student comments rendered as dropdown
TDS_SCNotepad	Notepad	    0   Students comments rendered as a notepad
TDS_SCTextArea	TextArea	0	Student comments rendered as a textarea
*/
ContentManager.getCommentCode = function() {
    var accProps = Accommodations.Manager.getCurrentProperties();
    return accProps.getStudentComments();
};

// get comments dialog header
ContentManager.getCommentLabel = function() {
    var commentCode = ContentManager.getCommentCode();
    var label = 'Comments';
    if (commentCode == 'TDS_SCDropDown') {
        label = Messages.getAlt('TestShell.Comments.DropDown', 'Comments');
    } else if (commentCode == 'TDS_SCNotepad') {
        label = Messages.getAlt('TestShell.Comments.Notepad', 'Notepad');
    } else if (commentCode == 'TDS_SCTextArea') {
        label = Messages.getAlt('TestShell.Comments.TextArea', 'Comments');
    }
    return label;
};

ContentManager.isDialogShowing = function() {
    return YUD.hasClass(document.body, 'showingLoading') ||
        YUD.hasClass(document.body, 'showingDialog');
};

ContentManager.log = function(message) {
    if (typeof Util == 'object') {
        Util.log(message); // TDS logs
    } 
    else if (typeof console == 'object') {
        console.log(message); // firebug logs
    } 
};

// Does this browser require the Mac OS X secure browser selection hack
ContentManager.requiresSelectionFix = function() {
    // check if mac and SB is less than 4.0
    return (Util.Browser.isMac() &&
            Util.Browser.getSecureVersion() > 0 &&
            Util.Browser.getSecureVersion() < 4.0);
};

// call this function on an iframe that is used in the content (e.x., html editor, simulator)
ContentManager.fixItemFrame = function (item, win, doc) {

    // add menu fixes to the frame
    ContentManager.Menu.applyDocFix(win);

    // add content manager events into frame
    ContentManager.addMouseEvents(item, doc);
    ContentManager.addKeyEvents(doc);

    // stop right click on regular browsers
    Util.Dom.stopAllEvents(doc, 'contextmenu');

    // BUG #12516: Mac OS X secure browser selection hack
    if (ContentManager.requiresSelectionFix()) {
        YUE.addListener(doc, 'mousedown', function(e) {
            ContentManager.focus(top);
        });

        YUE.addListener(doc, 'mouseup', function(e) {
            ContentManager.focus(win);
        });
    }

    var page = item.getPage();

    // add accommodations
    var pageAccommodations = page.getAccommodations();
    pageAccommodations.applyCSS(doc.body);

    // add zoom
    var zoom = page.getZoom();
    zoom.addDocument(doc);
    zoom.refresh();
};

// sets event handlers which disable input when the item is in a read only state
ContentManager.setReadOnlyKeyEvent = function(item, input) {
    // check for read-only
    var readOnlyFunc = function(evt) {
        if (item.isReadOnly()) {
            YUE.stopEvent(evt);
        }
    };

    YUE.on(input, 'keypress', readOnlyFunc);
    YUE.on(input, 'mousedown', readOnlyFunc);
};

// create a DOM range (using rangy)
ContentManager.createRange = function(doc) {
    if (typeof window.rangy == 'object') {
        try {
            return window.rangy.createRange(doc);
        } catch (ex) {
        }
    }
    return null;
};

// get the mouse selection (using rangy)
ContentManager.getSelection = function(doc) {
    if (typeof window.rangy == 'object') {
        try {
            return window.rangy.getSelection(doc);
        } catch (ex) {
        }
    }
    return null;
};

// adds the forceRedraw class and then removes it
ContentManager.applyRedrawFix = function() {

    // both calls need to be in a timer to be reliable
    setTimeout(function() {

        // apply fix
        // console.log('forceRedraw: add');
        YUD.addClass(document.body, 'forceRedraw');

        setTimeout(function() {
            // remove fix
            // console.log('forceRedraw: remove');
            YUD.removeClass(document.body, 'forceRedraw');
        }, 0);

    }, 0);

};

// Listen for when an input has focus. This can be used for
// fixing tablets that open up keyboard.
ContentManager.listenForFocus = function(doc) {

    var focused = false;

    var setFocused = function() {
        // console.log('inputFocus: add');
        YUD.addClass(document.body, 'inputFocus');
        focused = true;
    };

    var setBlurred = function() {
        // console.log('inputFocus: remove');
        YUD.removeClass(document.body, 'inputFocus');
        focused = false;
    };

    YUE.onFocus(doc, function(ev) {

        if (Util.Event.inTextInput(ev)) {

            var targetEl = YUE.getTarget(ev);

            // check if autocorrect is disabled
            if (YUD.getAttribute(targetEl, 'autocorrect') != 'off') {
                YUD.setAttribute(targetEl, 'autocorrect', 'off');
                YUD.setAttribute(targetEl, 'autocapitalize', 'off');
                YUD.setAttribute(targetEl, 'autocomplete', 'off');
            }

            ContentManager.applyRedrawFix();
            setFocused();
        }
    });

    YUE.onBlur(doc, function(ev) {
        setBlurred();
    });

    var win = Util.Dom.getWindow(doc);

    // if when scrolling we notice the active element is not a text area then remove focus
    YUE.on(win, 'scroll', function(ev) {
        if (focused && !Util.Dom.isTextInput(document.activeElement)) {
            setBlurred();
        }
    });

};

/***************/
/* YUI HELPERS */
/***************/

// YUI DOM HELPER METHOD: If the className exists on the node it is removed, if it doesn't exist it is added.
YUD.toggleClass = function(node, className, force) {
    var add = (force !== undefined) ? force : !(YUD.hasClass(node, className));
    if (add) {
        YUD.addClass(node, className);
    } else {
        YUD.removeClass(node, className);
    }
};
