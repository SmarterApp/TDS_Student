// define global namespace for all TDS global variables and components
if (typeof TDS == 'undefined') {
    var TDS = {};
}

TDS._initialized = false;
TDS.buildNumber = 0;
TDS.baseUrl = '';
TDS.messages = null; // messageSystem instance
TDS.globalAccommodations = null;
TDS.testeeCheckin = null;
TDS.clientStylePath = null;

// app flags
TDS.isProxyLogin = false;
TDS.isDataEntry = false;
TDS.isReadOnly = false;
TDS.isSIRVE = false;
TDS.inPTMode = false;
TDS.showItemScores = false;
TDS.isMonolith = false;

// global app settings 
TDS.Settings = {};

// global debug settings
TDS.Debug = {
    ignoreForbiddenApps: false
};

TDS.init = function () {

    if (TDS._initialized) {
        return;
    }

    // loads configs
    TDS.Config.load();

    if (TDS.messages) {
        // set message system language callback
        TDS.messages._getLanguage = TDS.getLanguage;

        // update i18n messages on the page
        TDS.Messages.Template.processLanguage();
    }

    TDS._initialized = true;
    Util.log('TDS INIT');
};

// get current accommodations 
// NOTE: one stop shop to get accommodations no matter where we are
TDS.getAccommodations = function () {

    // check if login shell
    if (typeof LoginShell == 'object' && LoginShell.segmentsAccommodations != null) {
        return LoginShell.segmentsAccommodations[0];
    }

    // check if test shell
    if (typeof ContentManager == 'object') {
        var page = ContentManager.getCurrentPage();
        if (page) {
            return page.getAccommodations();
        }
    }

    // check accommodations manager
    var accommodations = Accommodations.Manager.getCurrent();
    if (accommodations) {
        return accommodations;
    }

    // finally try global accommodations
    return TDS.globalAccommodations;
};

TDS.getAccs = TDS.getAccommodations;

// get current accommodation properties
TDS.getAccommodationProperties = function() {
    var accommodations = TDS.getAccommodations();
    return new Accommodations.Properties(accommodations);
};

TDS.getAccProps = TDS.getAccommodationProperties;

// get all the languages for this client
TDS.getLanguages = function() {
    var accGlobalProps = new Accommodations.Properties(TDS.globalAccommodations);
    return accGlobalProps.getLanguages();
};

// get the currently selected language
TDS.getLanguage = function() {
    var accProps = TDS.getAccommodationProperties();
    return accProps.getLanguage();
};

// resolve a path to the base of the site
TDS.resolveBaseUrl = function(path) {
    var url = TDS.baseUrl + (path || '');
    return Util.Browser.resolveUrl(url);
};

// call this function to redirect to another url
TDS.redirect = function(url) {
    if (TDS.Dialog) {
        TDS.Dialog.showProgress();
    }

    // if this is score entry app before redirecting we need to disable confirm exit popup
    if (TDS.isProxyLogin && typeof TDS.CLS.LogoutComponent == 'object') {
        TDS.CLS.LogoutComponent.PageUnloadEvent.unsubscribeAll();
    }

    // if raw is to true then don't include base url
    var raw = Util.String.isHttpProtocol(url);
    if (raw !== true) {
        url = this.baseUrl + url;
    }

    setTimeout(function() {
        top.window.location.href = url;
    }, 1);
};

TDS.redirectError = function(key, header, context) {
    var url = TDS.baseUrl + 'Pages/Notification.xhtml';

    if (YAHOO.lang.isString(key)) {
        var message = Messages.get(key);
        url += '?messageKey=' + encodeURIComponent(message);
    }

    if (YAHOO.lang.isString(header)) {
        //add header key if one has been passed.
        url = url + "&header=" + encodeURIComponent(header);
    }

    if (YAHOO.lang.isString(context)) {
        //add header key context. 
        url = url + "&context=" + encodeURIComponent(context);
    }

    top.location.href = url;
};

// redirects to the test shell
TDS.redirectTestShell = function(page) {
    var redirectUrl;
    var accProps = TDS.getAccommodationProperties();

    // figure out url
    if (accProps.hasBraille() || accProps.isTestShellAccessibility()) {
        redirectUrl = 'Pages/TestShellAccessibility.xhtml';
    } else if (accProps.isTestShellClassic()) {
        redirectUrl = 'Pages/TestShell.xhtml';
    } else {
        redirectUrl = 'Pages/TestShell.xhtml';
    }

    // add optional page 
    if (page) {
        redirectUrl += '?page=' + page;
    }

    TDS.redirect(redirectUrl);
};

TDS.getLoginUrl = function() {

    var url;

    // check for return url
    if (typeof TDS.Student == 'object') {
        url = TDS.Student.Storage.getReturnUrl();
    }

    // if there is no return url use base url
    if (url == null) {
        url = 'Pages/LoginShell.xhtml?logout=true';
    }

    return url;
};

TDS.logout = function() {
    var url = TDS.getLoginUrl();
    TDS.redirect(url);
};

TDS.logoutProctor = function(exl) {
    TDS.redirect('Pages/Proxy/logout.xhtml?exl=' + exl, false);
};

/************************************************************
* NOTE: All the code below is used to load the TDS variables (from GlobalXHR.axd?GetConfigs) into the right data structures
*/

// configs holds the raw json data that comes from the server
if (typeof TDS.Config == 'undefined') {
    TDS.Config = {};
}

TDS.Config.load = function() {
    // load config
    TDS.Config._setStyles();

    // load accommodations
    TDS.Config._loadGlobalAccs();
    TDS.Config._loadTestAccs();

    // loads messages
    TDS.Config._loadMessages();

    // load resource manifest if available
    TDS.Config._loadManifest();
};

// append a cacheid and chksum to css links if any
TDS.Config._loadManifest = function() {

    if (typeof TDS.Config.resourceManifest != 'object') {
        return;
    }
    if (typeof ContentManager != 'object' ||
        typeof ContentManager.Renderer != 'object') {
        return;
    }

    // override content_renderer.js to return the file paths with query strings
    var manifestFormatter = function(resourceKey) {
        var resourceFile = resourceKey;

        // Add the cache Id flag
        if (YAHOO.lang.isString(TDS.Cache.id)) {
            resourceFile += "?cid=" + TDS.Cache.id;
        } else {
            resourceFile += "?cid=1";
        }

        // Add the checksum
        Util.Array.each(TDS.Config.resourceManifest, function(manifestEntry) {
            if (manifestEntry.name == resourceKey) {
                resourceFile += "&chksum=" + manifestEntry.chksum;
            }
        });

        return resourceFile;
    };

    ContentManager.Renderer.setCustomFormatter(manifestFormatter);
};

// set CSS styles
TDS.Config._setStyles = function() {
    if (this.styles) {
        Util.Array.each(this.styles, function (style) {
            YUD.addClass(document.body, style);
        });
    }
};

// load global accommodations
TDS.Config._loadGlobalAccs = function() {

    // check if accommodations objects exists
    if (typeof Accommodations != 'function') {
        return;
    }

    TDS.globalAccommodations = new Accommodations('global');

    if (this.accs_global) {
        TDS.globalAccommodations.importJson(this.accs_global);
        TDS.globalAccommodations.selectDefaults();
    }
};

// load test and segment accommodations
TDS.Config._loadTestAccs = function() {
    // check if accommodations objects exists
    if (typeof Accommodations != 'function') {
        return;
    }

    // check for newer namespace which uses sessionStorage
    if (typeof TDS.Student == 'object') {
        this.accs_segments = TDS.Student.Storage.getAccJson();
    }

    // check if config exists
    if (this.accs_segments == null) {
        return;
    }

    for (var i = 0; i < this.accs_segments.length; i++) {
        var accommodations = Accommodations.create(this.accs_segments[i]);
        accommodations.selectAll();
        Accommodations.Manager.add(accommodations);
    };
};

// load new messages system
TDS.Config._loadMessages = function() {
    // check if messages lib is available
    if (typeof TDS.Messages != 'object') {
        return;
    }

    // load messages json
    var messageLoader = new TDS.Messages.MessageLoader();
    if (TDS.Config.messages) {
        messageLoader.load(TDS.Config.messages);
    }

    // save message system and process template replacements
    // messageLoader.buildIndex();
    TDS.messages = messageLoader.getMessageSystem();
};

// event stuff
(function() {

    // run init when all scripts are done loading
    YAHOO.util.Event.onDOMReady(TDS.init);

    // force YUI to throw exceptions in custom events (default is off)
    YAHOO.util.Event.throwErrors = true;

    // stop drag/scroll DOM events
    if (typeof Util == 'object') {
        Util.Dom.stopDragEvents();
    }

})();

/************************************************************/

// Preferences API:

TDS.getPref = function(name, defaultValue /*[boolean|number|string]*/) {
    if (typeof TDS.Config.preferences == 'object') {
        var value = TDS.Config.preferences[name];
        if (value !== undefined) {
            return value;
        }
    }

    return defaultValue;
};