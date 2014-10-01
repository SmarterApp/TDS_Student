﻿// REQUIRES: SecureBrowser.Base.js, Summit/air_mobile.js


TDS.SecureBrowser.Mobile = TDS.SecureBrowser.Mobile || {};

/*
The mobile iOS version of the secure browser built on top of the Summit application.
*/

TDS.SecureBrowser.Mobile.iOS = function () {

    TDS.SecureBrowser.Mobile.iOS.superclass.constructor.call(this);

    // initialize the guided access mode as 'unknown'
    this._guidedAccessMode = 'unknown';
    this._processes = [];
    this._backgroundThreshold = 5;  // default value is five seconds

    // get browser object and initialize
    this._airMobile = (new Summit.SecureBrowser.Mobile()).getNativeBrowser();
    this._airMobile.initialize();
};

YAHOO.lang.extend(TDS.SecureBrowser.Mobile.iOS, TDS.SecureBrowser.Base);

TDS.SecureBrowser.Mobile.iOS.prototype.loadProcessList = function () {
    this.setProcessList();
};

TDS.SecureBrowser.Mobile.iOS.prototype.getProcessList = function () {
	this.setProcessList();
    return this._processes;
};

TDS.SecureBrowser.Mobile.iOS.prototype.setBackgroundThreshold = function (value) {
    this._backgroundThreshold = value;
};

TDS.SecureBrowser.Mobile.iOS.prototype.initialize = function () {

    var secBrowser = this._airMobile;
    var guidedAccessMode = this._guidedAccessMode;
    var backgroundThreshold = this._backgroundThreshold;
    var startTimeBackground = null;
    var hasBeenBackgrounded = false;
    this.getGuidedAccessMode = function () { return guidedAccessMode; };
    this.getHasBeenBackgrounded = function () { return hasBeenBackgrounded; };
    this.setProcessList = function () { this._processes = secBrowser.device.runningProcesses; };
    var isLockedDown = true; // indicate if a student test session is going on
    this.setLockDown = function (lockdown) {
        isLockedDown = lockdown;
        if (!lockdown) {
            hasBeenBackgrounded = false;
        }
    };

    // check the guided access mode from the API
    secBrowser.checkGuidedAccessStatus(null, function (results) {
        if (results.enabled) {
            guidedAccessMode = 'true';
        } else {
            guidedAccessMode = 'false';
        }
        Util.log("access mode recorded is .. " + guidedAccessMode);
    });

    // listen and update for guided access changes
    secBrowser.listen(secBrowser.EVENT_GUIDED_ACCESS_CHANGED, document, function () {
        if (secBrowser.device.guidedAccessEnabled) {
            guidedAccessMode = 'true';
        } else {
            guidedAccessMode = 'false';
        }
        Util.log("access mode now is changed to ... " + guidedAccessMode);
    });

    // listen and check if the browser has been pushed to the background
    secBrowser.listen(secBrowser.EVENT_ENTER_BACKGROUND, document, function () {
        // record the time when the browser enters background during a test session
        if (isLockedDown) {
            startTimeBackground = (new Date()).getTime();
        }
        Util.log("the browser has been pushed to the background");
    });

    // listen and check if the browser has returned to the background
    secBrowser.listen(secBrowser.EVENT_RETURN_FROM_BACKGROUND, document, function () {
        // check if it is currently in a test session
        if (isLockedDown) {
            // check if the browser has been pushed to the background previously
            if (startTimeBackground != null) {
                // calculate the duration in which the browser has been running in the background
                var endTimeBackground = (new Date()).getTime();
                if ((endTimeBackground - startTimeBackground) > (backgroundThreshold * 1000)) {
                    // issue a warning only when the duration is longer than a threshold
                    hasBeenBackgrounded = true;
                }
                startTimeBackground = null;
            }
        }
        Util.log("the browser has been put to the background");
    });
};

TDS.SecureBrowser.Mobile.iOS.prototype.enableLockDown = function (lockDown) {
    this.setLockDown(lockDown);
};

TDS.SecureBrowser.Mobile.iOS.prototype.isEnvironmentSecure = function () {
    return ((this.getGuidedAccessMode() == 'true') && (!this.getHasBeenBackgrounded()));
};

// Returns a handle to the native browser engine.
TDS.SecureBrowser.Mobile.iOS.prototype.getRunTime = function () {
    return this._airMobile;
};


