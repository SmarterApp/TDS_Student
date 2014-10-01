// REQUIRES: util_event.js, YUI

/*
Parser help:
https://github.com/tobie/ua-parser/blob/master/regexes.yaml
https://github.com/faisalman/ua-parser-js/
*/

Util.Browser = {};

Util.Browser.isWindows = function() {
    return (navigator.platform.indexOf("Windows") != -1 ||
            navigator.platform.indexOf("Win32") != -1 ||
            navigator.platform.indexOf("Win64") != -1);
};

Util.Browser.isLinux = function() {
    return (navigator.platform.indexOf("X11") != -1 ||
            navigator.platform.indexOf("Linux") != -1 ||
            navigator.platform.indexOf("BSD") != -1);
};

Util.Browser.isMac = function() {
    return (navigator.platform.indexOf("Macintosh") != -1 ||
            navigator.platform.indexOf("MacPPC") != -1 ||
            navigator.platform.indexOf("MacIntel") != -1);
};

// Get the windows nt version
Util.Browser.getWindowsNTVersion = function() {
    var matches = navigator.userAgent.match(/Windows NT (\d+\.\d+)/);
    var value;
    if (matches && matches[1]) {
        value = parseFloat(matches[1]);
    }
    return value || 0;
};

// Get the mac os x version (does not work reliably on older versions 10.3-10.4)
Util.Browser.getOSXVersion = function () {
    var matches = navigator.userAgent.match(/Mac OS X (\d+\.\d+)/);
    var value;
    if (matches && matches[1]) {
        value = parseFloat(matches[1]);
    }
    return value || 0;
};

// Is this Mac based on the PPC architecture
Util.Browser.isMacPPC = function() {
    return navigator.userAgent.indexOf('PPC Mac') != -1;
};

// get the firefox version (http://en.wikipedia.org/wiki/Gecko_%28layout_engine%29)
Util.Browser.getFirefoxVersion = function() {
    
    if (YAHOO.env.ua.gecko > 0) {
        switch (YAHOO.env.ua.gecko) {
            case 1.7: return 1.0;
            case 1.8: return 1.5;
            case 1.81: return 2.0;
            case 1.9: return 3.0;
            case 1.91: return 3.5;
            case 1.92: return 3.6;
            case 1.93: return 3.7;
            case 2.0: return 4.0;
            default: return YAHOO.env.ua.gecko; // NOTE: after 4.0 the gecko version matched firefox version
        }
    }

    return 0;
};

Util.Browser.isFirefox = function() {
    return (Util.Browser.getFirefoxVersion() > 0);
};

Util.Browser.isIE = function() {
    return (YAHOO.env.ua.ie > 0);
};

// get the IE document mode (0 means browser does not support this)
Util.Browser.getIEDocMode = function() {
    return (document.documentMode ? document.documentMode : 0);
};

Util.Browser.isChrome = function() {
    return (YAHOO.env.ua.chrome > 0);
};

Util.Browser.isChromeOS = function() {
    return (window.navigator.userAgent.indexOf('CrOS') > -1);
};

Util.Browser.getChromeVersion = function() {
    return YAHOO.env.ua.chrome;
};

Util.Browser.isMobile = function() {
    return (YAHOO.env.ua.mobile != null);
};

Util.Browser.isIOS = function() {
    return (Util.Browser.getIOSVersion() > 0);
};

Util.Browser.getIOSVersion = function() {
    return YAHOO.env.ua.ios;
};

Util.Browser.isAndroid = function() {
    return (YAHOO.env.ua.android > 0);
};

// check if this browser supports touch events
Util.Browser.isTouchDevice = function() {
    return 'ontouchstart' in window;
};

Util.Browser.supportsSVG = function() {
    return document.implementation.hasFeature('http://www.w3.org/TR/SVG11/feature#BasicStructure', '1.1');
};


Util.Browser.supportsAudioOGG = function() {
    if (Util.Browser.isIOS() || Util.Browser.isAndroid()) {
        return false;
    } else if (Util.Browser.isChrome() || Util.Browser.isFirefox()) {
        return true;
    } else {
        return false;
    }
};

Util.Browser.isSecure = function() {
    var clientSideCheck = (navigator.userAgent.indexOf('AIRSecureBrowser') != -1 || 
                           navigator.userAgent.indexOf('AIRMobile') != -1); // Summit's browser

    var serverSideCheck = (typeof TDS == 'object' && typeof TDS.BrowserInfo == 'object') ? TDS.BrowserInfo.isSecure : false;

    var extensionCheck = window.AIRSecureBrowserExtension ? true : false;

    var chromeAppCheck = YUD.hasClass(document.body, 'browser_airsecurebrowser');
    
    return clientSideCheck || serverSideCheck || extensionCheck || chromeAppCheck;
};

Util.Browser.getSecureVersion = function() {

    var numberfy = function(s) {
        var c = 0;
        return parseFloat(s.replace(/\./g, function() {
            return (c++ == 1) ? '' : '.';
        }));
    };

    var ua = navigator.userAgent;

    // Check for desktop version
    var ma = ua.match(/AIRSecureBrowser\/([^\s\)]*)/);
    if (ma && ma[1]) {
        return numberfy(ma[1]);
    }

    // Check for mobile version (Summit)
    ma = ua.match(/AIRMobile[SecureBrowser]*\/([^\s\)]*)/);
    if (ma && ma[1]) {
        return numberfy(ma[1]);
    }

    return 0;
};

Util.Browser.getFlashVersion = function() {
    return parseFloat(FlashDetect.major + '.' + FlashDetect.minor);
};

/*********************************************************************************************/

/* Utils */

// resolve a url to the current base
Util.Browser.resolveUrl = function(url, root) {
    
    // set default if empty
    url = url || '';

    // set root document for resolving
    root = root || document;

    // change any html ampersand entities into the ampersand character
    url = url.replace(/&amp;/g, '&');

    // escape url
    url = url.split('&').join('&amp;').split('<').join('&lt;').split('"').join('&quot;');

    // cross browser compatible (even in IE 6) way of qualifying a url
    // http://stackoverflow.com/questions/470832/getting-an-absolute-url-from-a-relative-one-ie6-issue
    var el = root.createElement('div');
    el.innerHTML = '<a href="' + url + '">x</a>';
    return el.firstChild.href;
};

// enable full screen mode on Chrome Book
Util.Browser.requestFullScreen = function(elem) {
    
    if (!elem) {
        elem = document.body;
    }

    // webkit browser
    if (elem.webkitRequestFullScreen) {
        // apply className fullScreenMode to fix the size of document.body element in full-screen mode
        YUD.addClass(elem, 'fullScreenMode');
        elem.webkitRequestFullScreen();
        elem.addEventListener('webkitfullscreenchange', function() {
            if (!document.webkitIsFullScreen) {
                YUD.removeClass(elem, 'fullScreenMode');
                removeEventListener(this);
            }
        });
    }
    //else if (elem.mozRequestFullScreen) elem.mozRequestFullScreen();    // firefox
    //else if (elem.requestFullScreen) elem.requestFullScreen();          // others
    else {
        return;
    }
};
