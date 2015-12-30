//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
/*
This code implements the XDM API for use within item preview app.
*/

(function (XDM, CM, IPNav) {

    XDM.init(window);

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

    // response that is waiting to get loaded
    var waitingResponse = null;
    
    // load an item and then when it is ready the response
    function loadItem(bankKey, itemKey, response) {

        var loadComplete = IPNav.isPagesLoadComplete();
        if (!loadComplete) {
            throw new Error('Content Loading ... Please Wait...');
        } 

        var configPage = IPNav.getPageByKeys(bankKey, itemKey);
        if (!configPage) {
            throw new Error('Could not find the item.');
        }

        var id = configPage.id;

        // set response to get loaded
        if (response && configPage.items) {

            // set the response on the config item object which will get set if the item is loading
            for (var i = 0; i < configPage.items.length; i++) {
                var configItem = configPage.items[i];
                if (configItem.itemKey == itemKey) {
                    configItem.response = response;
                    break;
                }
            }

            // set this so we can manually load response (this isn't really needed anymore but I left it)
            waitingResponse = {
                id: id,
                value: response
            };
        }

        // go to the page
        IPNav.goToPageId(id);

        // if page is already showing then load response
        var contentPage = CM.getCurrentPage();
        if (contentPage && contentPage.id == id) {
            loadResponse(contentPage);
        }
    }

    // call this to load a waiting response
    function loadResponse(contentPage) {

        // check if the page that loaded matches the waiting response
        if (waitingResponse && waitingResponse.id == contentPage.id) {
            var items = contentPage.getItems();
            if (items && items.length > 0) {
                var firstItem = items[0]; // we only know the page...
                firstItem.setResponse(waitingResponse.value);
            }
        }

        // clear response
        waitingResponse = null;
    }

    XDM.addListener('BB:loadItem', loadItem);
    XDM.addListener('BB:setResponse', setResponse);
    XDM.addListener('BB:getResponse', getResponse);

})(Util.XDM, ContentManager, ItemPreview.Navigation);