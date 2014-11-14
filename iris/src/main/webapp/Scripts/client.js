var IRiS = (function (XDM) {

    if (XDM) {
        XDM.init(window);
    }

    var frame = null;
    var api = {};

    api.setFrame = function (frameWindow) {
        frame = frameWindow;
    };

    api.loadItem = function (bankKey, itemKey) {
        return XDM(frame).post('IRiS:loadItem', bankKey, itemKey);
    };

    api.loadToken = function (vendorId, token) {
        return XDM(frame).post('IRiS:loadToken', vendorId, token);
    };

    api.setResponse = function (value) {
        return XDM(frame).post('IRiS:setResponse', value);
    };

    api.getResponse = function () {
        return XDM(frame).post('IRiS:getResponse');
    };

    // expose api
    return api;

})(window.Util && window.Util.XDM);