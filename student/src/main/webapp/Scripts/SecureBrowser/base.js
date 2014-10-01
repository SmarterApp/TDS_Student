// REQUIRES: None

TDS = window.TDS || {};
TDS.SecureBrowser = TDS.SecureBrowser || {};

/*
The base class for all secure browsers. Any new secure browser must extend from this and override 
the methods to provide custom behavior if required
*/
TDS.SecureBrowser.Base = function () {
	this.Events = {
        // Event to fire if security breach is detected. Subscribers can decide what to do with the breach
        onSecurityBreach: new YAHOO.util.CustomEvent('onSecurityBreach', this, false, YAHOO.util.CustomEvent.FLAT),
    };
};

// Any SB initialization code. This is called upon instantiation and only once
TDS.SecureBrowser.Base.prototype.initialize = function () { };

// Check if the environment is secure for testing
TDS.SecureBrowser.Base.prototype.isEnvironmentSecure = function () { return true; };

// Clear all cached resources
TDS.SecureBrowser.Base.prototype.clearCache = function () { return false; };

// Clear all cookies
TDS.SecureBrowser.Base.prototype.clearCookies = function () { return false; };

// Clear any history
TDS.SecureBrowser.Base.prototype.clearHistory = function () {
    this.clearCache();
    this.clearCookies();
};

TDS.SecureBrowser.Base.prototype.emptyClipBoard = function () { return false; };

TDS.SecureBrowser.Base.prototype.getMACAddress = function() { return null; };

// Get all IP Addresses associated with this end point
TDS.SecureBrowser.Base.prototype.getIPAddressList = function (bypassCache) { return []; };

// Get primary IP 
TDS.SecureBrowser.Base.prototype.getIPAddress = function (bypassCache) {
    var addressList = this.getIPAddressList();
    if (addressList != null && addressList.length > 0) return addressList[0];
    return null;
};

// Get list of all running processes
TDS.SecureBrowser.Base.prototype.loadProcessList = function () { return false; };
TDS.SecureBrowser.Base.prototype.getProcessList = function () { return []; };

// get list of blacklisted processes
TDS.SecureBrowser.Base.prototype.getForbiddenApps = function () {
    var currentForbiddenApps = [];

    // make sure forbidden apps list exists
    if (typeof (TDS) != 'object' ||
        typeof (TDS.Config) != 'object' ||
        typeof (TDS.Config.forbiddenApps) != 'object' ||
        (TDS.Config.forbiddenApps == null)) {
        return currentForbiddenApps;
    }

    // get currently running processes
    var processList = this.getProcessList(); // []<string>

    if (processList) {
        Util.Array.each(processList, function (processName) {
            // check if the process name is a forbidden app
            var matchingApp = Util.Array.find(TDS.Config.forbiddenApps, function (forbiddenApp) {
                return Util.String.equals(forbiddenApp.name, processName, {
                    trim: true,
                    ignoreCase: true
                });
            });
            if (matchingApp != null) {
                currentForbiddenApps.push(matchingApp);
            }
        });
    }

    return currentForbiddenApps;
};

// call this function to fix an issue with the SB not getting proper focus and arrow keys not working
TDS.SecureBrowser.Base.prototype.fixFocus = function () {
    return false;
};

// force close the secure browser
TDS.SecureBrowser.Base.prototype.close = function () {
    if (typeof TDS.logout == 'function') TDS.logout();
    return false;
};

// Get the start time of when the app was launched
TDS.SecureBrowser.Base.prototype.getAppStartTime = function () {
    // set a session storage appStartTime item      
    if (window.sessionStorage.getItem("appStartTime") != null) {
        return Date.parse(window.sessionStorage.getItem("appStartTime"));
    }
    // OK - we don't know anymore
    return null;
};

// Method called at startup to see if we need to set the appStartTime incase we dont have a pref to report this
TDS.SecureBrowser.Base.prototype.setAppStartTime = function (timestamp, forceSet) {
    if (forceSet || this.getAppStartTime() == null) {
        window.sessionStorage.setItem("appStartTime", timestamp);
    }
};

// check if Mac OS X 10.7 spaces functionality is enabled
TDS.SecureBrowser.Base.prototype.isSpacesEnabled = function () {
    return false;
};

// Any java setup works goes here. This applies mostly to desktop for now but is part of the base class for now
TDS.SecureBrowser.Base.prototype.javaSetup = function() {
    return false;
};

// Returns a handle to the native browser engine.
TDS.SecureBrowser.Base.prototype.getRunTime = function () {
    return null;
};

// Do any security steps here to lock down the browser
TDS.SecureBrowser.Base.prototype.enableLockDown = function(lockDown) {
    return false;
};

// Mute the system volume
TDS.SecureBrowser.Base.prototype.mute = function() {
    return false;
};

// Unmute the system volume
TDS.SecureBrowser.Base.prototype.unmute = function() {
    return false;
};

// Check if the system is muted
TDS.SecureBrowser.Base.prototype.isMuted = function() {
    return false;
};

TDS.SecureBrowser.Base.prototype.enablePermissiveMode = function (enabled) { };

TDS.SecureBrowser.Base.prototype.enableChromeMode = function (enabled) {
};

// get the volume.. if this returns -1 then not supported
TDS.SecureBrowser.Base.prototype.getVolume = function () {
    return -1;
};

// set the volume.. if this returns false then not supported or invalid percentage
TDS.SecureBrowser.Base.prototype.setVolume = function (percent) {
    return false;
};

