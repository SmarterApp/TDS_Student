//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
if (typeof Blackbox != 'object') {
    Blackbox = {};
}

/*
Include this file on external web sites that host a blackbox app cross-domain.
The site that is hosting the blackbox app will need to implement the XDM listeners.
Requires: Util_XDM.js
*/

(function (BB, XDM) {

    XDM.init(window);

    var frame = null;
    var API = {};

    API.setFrame = function (frameWindow) {
        frame = frameWindow;
    };

    API.loadItemId = function (bank_itemKey, response) {
        var bankKey = '', itemKey = '';
        if (bank_itemKey) {
            var keys = bank_itemKey.split('-');
            if (keys) {
                bankKey = keys[0];
                itemKey = keys[1];
            }
        }
        return this.loadItem(bankKey, itemKey, response);
    };

    API.loadItem = function(bankKey, itemKey, response) {
        return XDM(frame).post('BB:loadItem', bankKey, itemKey, response);
    };

    API.setResponse = function (value) {
        return XDM(frame).post('BB:setResponse', value);
    };

    API.getResponse = function () {
        return XDM(frame).post('BB:getResponse');
    };

    // expose api
    BB.XDM = API;

})(Blackbox, Util.XDM);