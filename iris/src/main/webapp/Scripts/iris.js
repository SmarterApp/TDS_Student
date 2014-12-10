/*
This code implements the XDM API for use within item preview app.
*/

(function (XDM, CM) {

    // set read only
    CM.setReadOnly(true);

    // setup cross domain api 
    XDM.init(window);

    function loadContent(xmlDoc) {
        if (typeof xmlDoc == 'string') {
            xmlDoc = Util.Xml.parseFromString(xmlDoc);
        }
        // create array of content json from the xml
        var deferred = $.Deferred();
        var contents = CM.Xml.create(xmlDoc);
        var content = contents[0];
        page = CM.createPage(content);
        page.render();
        page.once('loaded', function () {
            TDS.Dialog.hideProgress();
            page.show();
            deferred.resolve();
        });
        return deferred.promise();
    }
    
    function loadToken(vendorId, token) {
        TDS.Dialog.showProgress();
        var url = location.href + '/Pages/API/content/load?id=' + vendorId;
        return $.post(url, token, null, 'text').then(function (data) {
            return loadContent(data);
        });
    }

    function setResponse(value) {
        var entity = CM.getCurrentPage().getActiveEntity();
        if (entity instanceof ContentItem) {
            entity.setResponse(value);
        }
    }

    function getResponse() {
        var entity = CM.getCurrentPage().getActiveEntity();
        if (entity instanceof ContentItem) {
            return entity.getResponse().value;
        }
        return null;
    }

    XDM.addListener('IRiS:loadToken', loadToken);
    XDM.addListener('IRiS:getResponse', getResponse);
    XDM.addListener('IRiS:setResponse', setResponse);

})(window.Util.XDM, window.ContentManager);