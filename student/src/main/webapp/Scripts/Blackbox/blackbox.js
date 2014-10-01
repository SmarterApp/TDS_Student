/* The internal blackbox API */

var Blackbox = {};

// EVENTS: init, available, ready
Blackbox.fireEvent = function(name, obj) {
    
    Util.log('BLACKBOX EVENT: ' + name);

    // try and get blackbox event on current page
    if (typeof(window.blackboxEvent) == 'function') {
        window.blackboxEvent(window, name, obj);
    }

    var parentBlackboxFunc;

    // try and get parent blackbox event func (this will throw exception if cross domain)
    try {
        if (window != parent && typeof(parent.blackboxEvent) == 'function') {
            parentBlackboxFunc = parent.blackboxEvent;
        }
    } catch(ex) {
        Util.log('BLACKBOX ERROR: Parent frame is cross domain.');
    }

    if (parentBlackboxFunc) {
        parentBlackboxFunc(window, name, obj);
    }
};

Blackbox.getConfig = function() {
    return (typeof blackboxConfig == 'object') ? blackboxConfig : {};
};

Blackbox.getClient = function() {
    var queryObj = this._getQueryObject();
    return queryObj.getValue('client');
};

Blackbox.getClientStylePath = function() {
    var queryObj = this._getQueryObject();
    return queryObj.getValue('clientstyle');
};

// check if HTML5 doctype was used
Blackbox.isHTML5Doctype = function() {
    // http://stackoverflow.com/questions/6088972/get-doctype-of-an-html-as-string-with-javascript
    var node = document.doctype;
    return (node != null && node.publicId === '');
};

// get the shell name (modern, classic or accessibility)
Blackbox.getShell = function() {
    var queryObj = this._getQueryObject();
    var shell = queryObj.getValue('shell');

    // cleanup shell value
    if (shell != null) {
        shell = shell.toLowerCase(); // make lowercase
        shell = shell.replace('shell', ''); // remove 'shell' from name
    } else {
        shell = 'modern';
    }

    return shell;
};

// figures out the best renderer to use for content manager
Blackbox.getRenderer = function() {

    var queryObj = this._getQueryObject();

    var renderer = queryObj.getValue('renderer');

    // cleanup renderer value
    if (renderer) {
        renderer = renderer.toLowerCase();
    }

    // check if someone requested a specific renderer
    switch (renderer) {
        case 'direct':
            return ContentManager.Renderer.Direct;
        case 'singleframe':
            return ContentManager.Renderer.SingleFrame;
        case 'multiframe':
            return ContentManager.Renderer.MultiFrame;
        default: {
            var shell = Blackbox.getShell();

            // since no renderer was defined try and and figure it out using shell
            switch (shell) {
                case 'accessibility':
                case 'modern':
                    return ContentManager.Renderer.Direct;
                case 'classic':
                    return ContentManager.Renderer.MultiFrame;
                default: {
                    // since nobody specific anything lets make a guess using doctype
                    if (Blackbox.isHTML5Doctype()) {
                        return ContentManager.Renderer.Direct;
                    } else {
                        return ContentManager.Renderer.MultiFrame;
                    }
                }
            }
        }
    }
};

// this function gets called when the blackbox DOM and scripts are available
Blackbox.init = function() {
    
    Blackbox.fireEvent('init');

    // get querystring
    var queryObj = this._getQueryObject();

    // Initialize TDS data structures (messages)
    // BUG #60631: Required because DOM ready in tds.js sometimes does not fire
    TDS.init();

    // clear out contents div
    var contentsEl = document.getElementById('contents');
    if (contentsEl) {
        contentsEl.innerHTML = '';
    }

    // add empty accommodations
    var accommodations = new Accommodations();
    Accommodations.Manager.add(accommodations);

    // initialize content manager
    if (TDS.baseUrl && TDS.baseUrl.length > 0) {
        ContentManager.init(TDS.baseUrl);
        ContentManager.Frame.setBaseUrl(TDS.baseUrl, true);
    } else {
        ContentManager.init();
    }

    // let everyone know the blackbox is available
    Blackbox.fireEvent('available');

    // setup audio
    var flashPath = ContentManager.resolveBaseUrl('Scripts/Libraries/soundmanager2/swf/');
    TDS.Audio.Player.setup(flashPath);
    TDS.Audio.Recorder.initialize();

    // initialize TTS (why would this be initialized here instead of only in the module when included)
    //TTSManager.init();

    // get the blackbox config
    var config = Blackbox.getConfig();

    ContentManager.onPageEvent('loaded', function(page) {
        // get zoom
        var accProps = Accommodations.Manager.getCurrentProperties();
        var zoomLevel = accProps.getPrintSize();

        // set zoom
        var zoomObj = page.getZoom();
        if (zoomLevel) {
            zoomObj.setLevel(zoomLevel);
        } // use accommodation
        else {
            zoomObj.reset();
        } // use default

        // show a page when it is loaded
        if (config.preventShowOnLoad !== true) {
            page.show();
        }
    });

    ContentManager.onPageEvent('show', function(page) {
        TDS.Dialog.hideProgress();

        // start playing any audio that is set to auto play
        page.autoPlayQueue.start();
    });

    // get the specific client style path
    var clientStyle = Blackbox.getClientStylePath();

    // check if client style path was provided and if it wasn't then try the client name
    if (!YAHOO.lang.isString(clientStyle)) {
        clientStyle = Blackbox.getClient();
    }

    // if clientstyle or client was provided then tell the renderer
    if (YAHOO.lang.isString(clientStyle)) {
        ContentManager.Renderer.setClient(clientStyle);
    }

    // create and initialize the renderer
    var contentContainer = YUD.get('contents');

    if (contentContainer) {
        
        var contentRenderer = Blackbox.getRenderer();

        // log the type of renderer
        if (contentRenderer == ContentManager.Renderer.Direct) {
            console.log('Content Renderer: direct');
        } else if (contentRenderer == ContentManager.Renderer.SingleFrame) {
            console.log('Content Renderer: single frame');
        } else if (contentRenderer == ContentManager.Renderer.MultiFrame) {
            console.log('Content Renderer: multi frame');
        } else {
            console.log('Content Renderer: unknown');
        }

        ContentManager.Renderer.init(contentContainer, contentRenderer);
    }

    // check if enabling accessibility
    if (Blackbox.getShell() == 'accessibility') {
        ContentManager.enableAccessibility();
    }

    // set accommodations from url (NOTE: leave this after blackboxInitializing)
    Blackbox.setAccommodationsFromUrl();

    // hook up event handlers
    Blackbox.bindUIEvents();

    // if someone included item information in the shell URL then load the content
    // var urlContentRequest = Blackbox.getContentRequestFromUrl();

    Blackbox.fireEvent('ready');

    // load content in the url if any
    // if (urlContentRequest) Blackbox.loadContent(urlContentRequest);
};

// use this function to bind any events to the UI
Blackbox.bindUIEvents = function() {

    // subscribe to zoom
    YUE.on('btnZoomIn', 'click', function(ev) {
        var currentPage = ContentManager.getCurrentPage();
        if (currentPage) {
            currentPage.zoomIn();
        }
    });

    YUE.on('btnZoomOut', 'click', function(ev) {
        var currentPage = ContentManager.getCurrentPage();
        if (currentPage) {
            currentPage.zoomOut();
        }
    });

    // subscribe to measurement tools
    YUE.on('btnProtractor', 'click', function(ev) {
        var currentPage = ContentManager.getCurrentPage();
        if (currentPage && currentPage.MT_toggleProtractor) {
            currentPage.MT_toggleProtractor(null, currentPage);
        }
    });

    YUE.on('btnRuler', 'click', function(ev) {
        var currentPage = ContentManager.getCurrentPage();
        if (currentPage && currentPage.MT_toggleRuler) {
            currentPage.MT_toggleRuler(null, currentPage);
        }
    });
};

// this function is called to request content to get loaded
Blackbox.loadContent = function(contentRequest, forceReload) {
    // get the current page
    var currentPage = ContentManager.getCurrentPage();

    // check if the content request is already being shown
    if (currentPage) {
        if (!forceReload && currentPage.id == contentRequest.id) {
            return;
        }
        currentPage.hide();
    }

    if (!forceReload) {
        // check if there is an existing page
        var existingPage = ContentManager.getPage(contentRequest.id);

        if (existingPage) {
            existingPage.show();
            return; // no need to load anything..
        }
    }

    TDS.Dialog.showProgress();

    // update latest accommodations for this request
    var accommodations = Accommodations.Manager.getCurrent();
    contentRequest.accommodations = accommodations.getSelectedJson();

    var contentError = function(xhrData) {
        alert('Blackbox Error: ' + xhrData.statusText);
        TDS.Dialog.hideProgress();
    };

    // function for when the content returns from XHR
    var contentReady = function(xhrData) {
        // check if right status was returned
        if (xhrData.status != 200) {
            contentError(xhrData);
            return;
        }

        var contents = ContentManager.Xml.create(xhrData.responseXML);

        if (contents) {
            for (var i = 0; i < contents.length; i++) {
                var content = contents[i];

                // check if existing page already
                var page = ContentManager.getPage(content.id);

                // if there is an existing page remove it
                if (page && forceReload) {
                    ContentManager.removePage(page);
                    page = null;
                }

                if (page) {
                    page.show();
                } else {
                    // create and render new page
                    page = ContentManager.createPage(content);
                    page.render();
                }
            }
        }
    };

    var callback = {
        success: contentReady,
        failure: contentError,
        timeout: 120000
    };

    // CORS: https://developer.mozilla.org/en-US/docs/HTTP_access_control

    // NOTE: We need to use 'text/plain' or request will be preflighted
    // YAHOO.util.Connect.setDefaultPostHeader(false); // allow custom 'Content-Type'
    // YAHOO.util.Connect.initHeader('Content-Type', 'text/plain');

    // NOTE: Don't send custom headers ('X-Requested-With') or request will be preflighted
    YAHOO.util.Connect.setDefaultXhrHeader(false); // 

    // send xhr request
    var requestUrl = TDS.resolveBaseUrl('ContentRequest.axd/load');
    var postData = YAHOO.lang.JSON.stringify(contentRequest);
    YAHOO.util.Connect.asyncRequest('POST', requestUrl, callback, postData);

    Util.dir(contentRequest);
};

// returns a content object based on the values in the query string
// NOTE: Used by ITS
Blackbox.getContentRequestFromUrl = function() {

    console.warn('The function Blackbox.getContentRequestFromUrl() is deprecated and will be removed soon. Please load content manually using Blackbox.loadContent().');

    var queryObj = this._getQueryObject();

    // create request object
    var content = {
        token: queryObj.getValue('auth_token'),
        client: queryObj.getValue('client'),
        mode: queryObj.getValue('mode'),
        language: queryObj.getValue('language'),
        layoutFile: queryObj.getValue('layoutFile'),
        layoutName: queryObj.getValue('layoutName'),
        items: [],
        accommodations: this.getAccommodationsFromUrl()
    };

    // get passage from url
    var passagePath = queryObj.getValue('passage');
    content.passage = (passagePath != null) ? { file: passagePath } : null;

    // get items from url
    var items = queryObj.getValues('item');

    if (items != null) {
        // check if all items should be marked as disabled
        var disabled = queryObj.getValue('disabled');
        if (disabled == null) {
            disabled = false;
        }

        // create items
        for (var i = 0; i < items.length; i++) {
            var itemPath = items[i];
            content.items.push({ file: itemPath, disabled: disabled, response: null });
        }
    }

    if (content.passage == null && content.items.length == 0) {
        return null;
    }
    
    return content;
};

// an helper object with the querystring values which you can access with getValue(name) or getValues(name).
Blackbox._getQueryObject = function(win /* a browser window obj */) {
    var decode = function(s) {
        try {
            return decodeURIComponent(s).replace(/\r\n|\r|\n/g, "\r\n");
        } catch(e) {
            return "";
        }
    };

    win = win || window;
    var qs = win.location.search;
    var multimap = {};

    if (qs.length > 1) {
        qs = qs.substr(1);
        qs.replace(/([^=&]+)=([^&]*)/g, function(match, hfname, hfvalue) {
            var name = decode(hfname);
            var value = decode(hfvalue);

            if (name && name.length > 0) {
                name = name.toLowerCase();

                if (!multimap.hasOwnProperty(name)) {
                    multimap[name] = [];
                }

                multimap[name].push(value);
            }
        });
    }

    var getValues = function(name) {
        name = name.toLowerCase();
        var values = multimap[name];
        if (!YAHOO.util.Lang.isArray(values)) {
            return null;
        }
        return values;
    };

    var getValue = function(name) {
        var values = getValues(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    };

    return { getValue: getValue, getValues: getValues };
};

// parse accommodations from a string into a hash data structure
// e.x., Language:ENU;TTS:TDS_TTS_Item;TTS:TDS_TTS_Stim
// NOTE: Used by ITS
Blackbox.parseAccommodations = function(accommodationString) {
    
    // check if any accommodations
    if (accommodationString == null || accommodationString.length == 0) {
        return null;
    }

    var accPairs = accommodationString.split(';');

    // create hash table
    var accHash = {};

    for (var i = 0; i < accPairs.length; i++) {
        var accPair = accPairs[i];
        var accData = accPair.split(':');
        var type = accData[0];
        var value = accData[1];

        var values = accHash[type];

        if (values == null) {
            accHash[type] = [];
            values = accHash[type];
        }

        values.push(value);
    }

    // create json
    var accJson = [];

    Util.Array.each(Util.Object.keys(accHash), function(name) {
        accJson.push({ type: name, codes: accHash[name] });
    });

    return accJson;
};

// get the accommodation string in the url and parse it into json
Blackbox.getAccommodationsFromUrl = function() {
    // get accommodations from itempreview querystring
    var queryObj = this._getQueryObject();
    var accommodationString = queryObj.getValue('accommodations');
    var accommodations = this.parseAccommodations(accommodationString);
    return accommodations;
};

// get the accommodations in the url and add them to the accommodations manager
Blackbox.setAccommodationsFromUrl = function() {
    var accArray = this.getAccommodationsFromUrl();
    this.setAccommodations(accArray);
};

// add accommodation in json format into the accommodations manager and make them selected
Blackbox.setAccommodations = function(typeList) {
    
    if (typeList == null) {
        return false;
    }

    var accommodations = Accommodations.Manager.getCurrent();

    Util.Array.each(typeList, function(typeData) {
        var typeName = typeData.type;
        var typeCodes = typeData.codes;

        // load accommodation type/codes into accommodations manager if they don't alread exist
        var accType = accommodations.getType(typeName);

        if (accType == null) {
            accType = accommodations.createType(typeName, null, true, true, true, true);
        }

        Util.Array.each(typeCodes, function(typeCode) {
            var accValue = accommodations.getValue(typeCode);

            if (accValue == null) {
                accValue = accType.createValue(typeCode, typeCode, null, false, true, null);
            }
        });

        // set codes as selected
        accommodations.selectCodes(typeName, typeCodes);
    });

    return true;
};

// once you have added accommodations
Blackbox.changeAccommodations = function(callback) {
    
    if (!YAHOO.lang.isFunction(callback)) {
        throw new Error('Must pass in function that will change accommodations object.');
    }

    var accommodations = Accommodations.Manager.getCurrent();

    var pages = ContentManager.getPages();

    for (var i = 0; i < pages.length; i++) {
        var page = pages[i];
        var body = page.getBody();
        accommodations.removeCSS(body);
    }

    callback(accommodations);

    for (var i = 0; i < pages.length; i++) {
        var page = pages[i];
        var body = page.getBody();
        accommodations.applyCSS(body);
    }
};

// enable a button on the shell and assign functionality
Blackbox.showButton = function(name, handler, shouldEnable) {
    
    var button = document.getElementById(name);
    if (button == null) {
        return false;
    }

    YUD.setStyle(button.parentNode, 'display', '');

    if (YAHOO.lang.isFunction(handler)) {
        YUE.addListener(button, 'click', handler);
    }

    Blackbox.enableButton(name, shouldEnable);

    return true;
};

Blackbox.enableButton = function(name, shouldEnable) {
    
    var button = YUD.get(name);

    if (button == null) {
        return false;
    }

    if (shouldEnable) {
        YUD.removeClass(button.parentNode, 'inactive');
    } else {
        YUD.addClass(button.parentNode, 'inactive');
    }

    return true;
};

// function for creating a top bar button html
Blackbox._createButton = function(id, label, fn) {

    var liEl = document.createElement('li');

    var buttonEl = document.createElement('button');
    
    if (id) {
        buttonEl.id = id;
    }
    
    liEl.appendChild(buttonEl);

    var spanEl = document.createElement('span');
    spanEl.className = 'icon';
    
    if (label) {
        spanEl.innerHTML = label;
    }
    
    buttonEl.appendChild(spanEl);

    if (YAHOO.lang.isFunction(fn)) {
        YUE.addListener(buttonEl, 'click', fn);
    }

    return liEl;
};

// add a button to the top bar tool section 
Blackbox.createButtonTool = function(id, label, fn) {
    var liEl = Blackbox._createButton(id, label, fn);
    var topBarEl = document.getElementById('studentTools');
    if (topBarEl) {
        var ulEl = YAHOO.util.Dom.getFirstChild(topBarEl);
        if (ulEl) {
            ulEl.appendChild(liEl);
        }
    }
};

// add a button to the top bar controls section 
Blackbox.createButtonControl = function(id, label, fn) {
    var liEl = Blackbox._createButton(id, label, fn);
    var topBarEl = document.getElementById('studentControls');
    if (topBarEl) {
        var ulEl = YAHOO.util.Dom.getFirstChild(topBarEl);
        if (ulEl) {
            ulEl.appendChild(liEl);
        }
    }
};

// pop up a new window with the accessibility view
// NOTE: Used by ITS
Blackbox.openWindow = function(contentRequest) {
    
    // check for content to request
    if (!YAHOO.lang.isObject(contentRequest)) {
        return false;
    }

    // set defaults if they are not assigned
    var queryObj = this._getQueryObject(top);
    if (contentRequest.client == null) {
        contentRequest.client = queryObj.getValue('client');
    }
    if (contentRequest.language == null) {
        contentRequest.language = queryObj.getValue('language');
    }
    if (contentRequest.language == null) {
        contentRequest.language = "ENU";
    } // default if blank

    // logging
    dump(contentRequest);

    // create form
    var form = document.createElement("form");

    // set form properties
    form.enctype = form.encoding = "multipart/form-data";
    form.setAttribute('method', 'POST');

    // post form to new window
    // form.setAttribute('action', 'Accessibility.xhtml');
    form.setAttribute('action', TDS.resolveBaseUrl('Accessibility.xhtml'));
    form.setAttribute('target', '_blank');

    // add form to document (this is required on some browsers)
    document.body.appendChild(form);

    // parameters
    var params = { json: YAHOO.util.Lang.JSON.stringify(contentRequest) };

    // check if any parameters passed in and if so add them to form
    for (var key in params) {
        if (!params.hasOwnProperty(key)) {
            continue;
        }

        // create hidden field
        var hiddenField = document.createElement("input");
        hiddenField.setAttribute("type", "hidden");
        hiddenField.setAttribute("name", key);
        hiddenField.setAttribute("value", params[key]);
        form.appendChild(hiddenField);
    }

    // submit form
    form.submit();

    return true;
};

function log(message) {
    try {
        if (typeof top.console == 'object' && typeof top.console.log == 'function') {
            console.log(message);
        }
    } catch(e) {
    }
}

function dump(obj) {
    try {
        if (typeof top.console == 'object' && typeof top.console.dir == 'function') {
            console.dir(obj);
        }
    } catch(e) {
    }
}

// when the window for blackbox loads then run the init
// YUE.on(window, 'load', Blackbox.init, Blackbox, true);
