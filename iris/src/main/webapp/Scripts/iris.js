/*
This code implements the XDM API for use within item preview app.
*/

(function (XDM, CM) {

    // set read only
    CM.setReadOnly(true);

    // setup cross domain api 
    XDM.init(window);

  /*Start SB-1279; following code is from Balaji*/
  function getItemId(item) {
        return "I-" + item.bankKey + "-" + item.itemKey;
    }

    function getItemMap(requestedItems) {
        var distinctItemCount = 0;

        var itemMap = requestedItems.reduce(function (map, item) {
            ++distinctItemCount;
            map[getItemId(item)] = item;
            return map;
        }, {});

        if (requestedItems.length !== distinctItemCount) {
            throw new Error('One or more of the requested items appears multiple times in this request.');
        }

        return itemMap;
    }

    function getExistingPage(requestedItems) {

        var requestedItemCount = Object.keys(requestedItems).length,
            partialMatches = false,
            matchedPage = null,
            matchedItems = null;

        // go through each page to try matching items
        CM.getPages().forEach(function (page) {
            var items = page.getItems(),
                matches = [];

            // check this page for items which are in the current content request
            items.forEach(function (item) {
                var itemId = getItemId(item),
                    matchedItem = requestedItems[itemId];

                if (matchedItem) {
                    matches.push({
                        loaded: item,
                        requested: matchedItem
                    });
                }
            });

            if (matches.length === items.length && items.length === requestedItemCount) {
                // exact match, save the page and items
                matchedPage = page;
                matchedItems = matches;
            } else if (matches.length) {
                // only some items matched
                partialMatches = true;
            }
        });

        if (partialMatches) {
            throw new Error('One or more of the items requested have already been loaded. Make sure the content request is the same as the orginal (e.g. it can\'t contain different response or label values).');
        }

        return {
            page: matchedPage,
            itemPairs: matchedItems
        };
    }

    function loadContent(xmlDoc) {
        if (typeof xmlDoc == 'string') {
            xmlDoc = Util.Xml.parseFromString(xmlDoc);
        }

        // create array of content json from the xml
        var deferred = $.Deferred();
        var contents = CM.Xml.create(xmlDoc);
        var content = contents[0];

        var itemMap = getItemMap(content.items);
        var result = getExistingPage(itemMap);

        if (result.page) {
            // show the page
            TDS.Dialog.hideProgress();
            result.page.show();

            // set the responses for the items
            result.itemPairs.forEach(function (pair) {
                var loaded = pair.loaded,
                    requested = pair.requested;

                loaded.setResponse(requested.value);
                requested.label && loaded.setQuestionLabel(requested.label);
            });

            // nothing left to do, just resolve the deferred now
            deferred.resolve();
        } else {
            page = CM.createPage(content);
            page.render();
            page.once('loaded', function () {
                TDS.Dialog.hideProgress();
                page.show();
                deferred.resolve();
            });
        }

        return deferred.promise();
    }
    /*End Sb-1279*/
    
    
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