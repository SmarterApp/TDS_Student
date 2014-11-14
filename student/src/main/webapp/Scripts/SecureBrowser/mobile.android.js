﻿// REQUIRES: SecureBrowser.Base.js, Summit/air_mobile.js

TDS.SecureBrowser.Mobile = TDS.SecureBrowser.Mobile || {};

/*
The mobile Android version of the secure browser built on top of the Summit application.
*/
TDS.SecureBrowser.Mobile.Android = function () {
    TDS.SecureBrowser.Mobile.Android.superclass.constructor.call(this);
    this._config = { pausedSinceLaunch: false, keyboardChanged: false, clipboardChanged: false };
    this._airMobile = (new Summit.SecureBrowser.Mobile()).getNativeBrowser();
    this._airMobile.initialize();
};

YAHOO.lang.extend(TDS.SecureBrowser.Mobile.Android, TDS.SecureBrowser.Base);

TDS.SecureBrowser.Mobile.Android.prototype.initialize = function () {
    var sb = this._airMobile;
    var config = this._config;

    // wait for SB to be ready
    sb.listen(sb.EVENT_DEVICE_READY, window.document, function () {

        // Any time the app returns from background, we treat that as a breach of security
        sb.listen(sb.EVENT_RETURN_FROM_BACKGROUND, window.document, function () {
            config.pausedSinceLaunch = true;
        });

        // check if a differnt keyboard other than the default soft keyboard is being used, and if so,
        // retreat as a breach of security
        if (sb.device.keyboard != 'com.air.mobilebrowser/.softkeyboard.SoftKeyboard') {
            config.keyboardChanged = true;
        }

        // detect any change in keyboard, and if there is change, treat as a breach of security
        sb.listen(sb.EVENT_KEYBOARD_CHANGED, window.document, function () {
            config.keyboardChanged = true;
        });

        // detect if the content of the clipboard has been changed, and if there is change, treat as a breach of security
        sb.listen(sb.EVENT_CLIPBOARD_CHANGED, window.document, function () {
            config.clipboardChanged = true;
        });

        // This is the code to capture the event that will be fired when mini apps are running
        // (Samsung Galaxy Tab 2 only)
        sb.listen(sb.EVENT_MINI_APP_DETECTED, window.document, function () {
            config.pausedSinceLaunch = true;
        });
    });

};

TDS.SecureBrowser.Mobile.Android.prototype.isEnvironmentSecure = function () {
    return (!this._config.pausedSinceLaunch && !this._config.keyboardChanged && !this._config.clipboardChanged);
};


// Returns a handle to the native browser engine.
TDS.SecureBrowser.Mobile.Android.prototype.getRunTime = function () {
    return this._airMobile;
}


