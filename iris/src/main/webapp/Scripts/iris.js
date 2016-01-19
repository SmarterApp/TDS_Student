/*
This code implements the XDM API for use within item preview app.
*/

(function (XDM, CM) {

    // set read only
    CM.setReadOnly(true);

    // we load one page in advance, but we don't want that to cause a cascade of page show/load
    Blackbox.getConfig().preventShowOnLoad = true;

    // setup cross domain api 
    XDM.init(window);

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

    var loadedDefaultAccommodations = false;

    function parseAccommodations(segmentId, position, label, segmentEl) {
        var types = [];

        $(segmentEl).find('accommodation').each(function () {
            var $this = $(this);

            types.push({
                name: $this.attr('type'),

                values: [{
                    name: $this.attr('name'),
                    code: $this.attr('code'),
                    selected: $this.attr('selected') === 'true',
                    isDefault: true
                }]
            });
        });

        // clone default accommodations
        var accs = Accommodations.Manager.getDefault().clone();

        // overwrite with segment-specific accommodations
        accs.importJson({
            id: segmentId,
            position: position,
            label: label,
            types: types
        });

        // se the first segment's accommodations as default
        if (!loadedDefaultAccommodations) {
            loadedDefaultAccommodations = true;
            Accommodations.Manager.setDefault(segmentId);
        }

        return accs;
    }

    function loadGroupedContent(xmlDoc) {

        if (CM.getPages().length > 0) {
            throw new Error("content has already been loaded; cannot load grouped content");
        }

        if (typeof xmlDoc == 'string') {
            xmlDoc = Util.Xml.parseFromString(xmlDoc);
        }

        $(xmlDoc).find('segment').each(function () {

            var segmentId = $(this).attr('id');

            // parse accommodations
            var accommodations = parseAccommodations(segmentId, 'position', 'label', this);
            Accommodations.Manager.add(accommodations);

            // parse content
            var contents = CM.Xml.create(this);
            for (var i = 0; i < contents.length; ++i) {
                var content = contents[i];
                var page = CM.createPage(content);

                // when a page is shown, we want to begin rendering the following page
                page.once('show', function () {
                    var pages = CM.getPages(),
                        nextPageIndex = pages.indexOf(this) + 1,
                        nextPage = pages[nextPageIndex];

                    if (nextPage) {
                        nextPage.render();
                    }
                });
            }
        });

        // render the first page, and notify the caller when it is ready
        var pages = CM.getPages(),
            pageCount = pages.length,
            firstPage = pages[0],
            deferred = $.Deferred();

        firstPage.once('loaded', function () {
            deferred.resolve();

            TDS.Dialog.hideProgress();
            firstPage.show();

            var navigability = getNavigability();
            sendNavUpdate(navigability);
        });

        firstPage.render();

        return deferred.promise();
    }

    function loadToken(vendorId, token) {
        TDS.Dialog.showProgress();
        var url = location.href + '/Pages/API/content/load?id=' + vendorId;
        return $.post(url, token, null, 'text').then(function (data) {
            return loadContent(data);
        });
    }

    function loadGroupedContentToken(vendorId, token) {
        TDS.Dialog.showProgress();
        var url = location.href + '/Pages/API/content/loadContent?id=' + vendorId;
        return $.post(url, token, null, 'text').then(function (data) {
            return loadGroupedContent(data);
        });
    }

    function setItemResponse(item, response) {
        if (item && item instanceof ContentItem) {
            item.setResponse(response);
        } else {
            throw new Error('invalid item; could not set response');
        }
    }

    function setItemLabel(item, label) {
        if (item && item instanceof ContentItem) {
            item.setQuestionLabel(label);
        } else {
            throw new Error('invalid item; could not set label');
        }
    }

    function setResponse(value) {
        var entity = CM.getCurrentPage().getActiveEntity();
        //Begin Hack: 1327 remove <p> from response with prev/next button
        while(value.search("&amp;")!=-1) // '&' comes as '&amp;amp;' in response
        	value = value.replace("&amp;", "&"); 
		value = $('<div/>').html(value).text();
		while(value.search("<p>")!=-1)
			value = value.replace ("<p>", "");
		while(value.search("</p>")!=-1)
			value = value.replace ("</p>", "</br>");
		//End Hack
        setItemResponse(entity, value);
    }

    function setResponses(itemResponses) {
        var items = CM.getCurrentPage().getItems();
        itemResponses.forEach(function (itemResponse) {
            var itemFromPosition, itemFromId;
            if (typeof itemResponse.position === 'number') {
                itemFromPosition = items[itemResponse.position - 1];
            }

            if (itemResponse.id) {
                itemFromId = items.filter(function (item) {
                    var itemId = getItemId(item);
                    return itemId === itemResponse.id;
                })[0];
            }

            if (itemFromPosition && itemFromId && itemFromPosition !== itemFromId) {
                throw new Error('item position and id do not match');
            }

            setItemResponse(itemFromPosition || itemFromId, itemResponse.response);

            if (itemResponse.label) {
                setItemLabel(itemFromPosition || itemFromId, itemResponse.label);
            }
        });
    }

    function getResponse() {
        var entity = CM.getCurrentPage().getActiveEntity();
        if (entity instanceof ContentItem) {
            return entity.getResponse().value;
        }
        return null;
    }

    function getIndexOfCurrentPage() {
        var currentPage = CM.getCurrentPage(),
            pages = CM.getPages(),
            index = pages.indexOf(currentPage);
    }

    function getNavigability() {
        var n = {
            pages: null,
            index: 0,

            haveNextPage: false,
            haveNextPaginatedItem: false,

            havePrevPage: false,
            havePrevPaginatedItem: false,

            update: function () {
                this.haveNextPage = false;
                this.haveNextPaginatedItem = false;
                this.havePrevPage = false;
                this.havePrevPaginatedItem = false;

                var currentPage = CM.getCurrentPage();

                this.pages = this.pages || CM.getPages(),
                this.index = this.pages.indexOf(currentPage);

                this.haveNextPage = this.index < this.pages.length - 1;
                this.havePrevPage = this.index > 0;

                var pagination = currentPage.plugins.get('pagination');

                if (pagination) {
                    this.haveNextPaginatedItem = pagination.haveNext();
                    this.havePrevPaginatedItem = pagination.havePrev();
                }
            }
        };

        n.update();

        return n;
    }

    function go(direction) {
        var n = getNavigability();

        // too easy to take wrong branch if we use terse logic here,
        // so we'll just use the clear version

        if (direction === 'next' && n.haveNextPaginatedItem) {
            CM.requestNextPage()
        }
        else if (direction === 'prev' && n.havePrevPaginatedItem) {
            CM.requestPreviousPage();
        }
        else if (direction === 'next' && n.haveNextPage) {
            n.pages[++n.index].show();
        }
        else if (direction === 'prev' && n.havePrevPage) {
            n.pages[--n.index].show();
        }

        n.update();
        sendNavUpdate(n);
    }

    function showNext() {
        go('next');
    }

    function showPrev() {
        go('prev');
    }

    function sendNavUpdate(navigability) {
        var n = navigability;
        XDM(window.parent).post('IRiS:navUpdate',
            n.havePrevPage || n.havePrevPaginatedItem,
            n.haveNextPage || n.haveNextPaginatedItem
        );
    }

    XDM.addListener('IRiS:loadToken', loadToken);
    XDM.addListener('IRiS:loadContent', loadGroupedContentToken);
    XDM.addListener('IRiS:getResponse', getResponse);
    XDM.addListener('IRiS:setResponse', setResponse);
    XDM.addListener('IRiS:setResponses', setResponses);

    XDM.addListener('IRiS:showNext', showNext);
    XDM.addListener('IRiS:showPrev', showPrev);

    /* Following is throwing error, not a blocker though
     * Blackbox.events.on('ready', function () {
        XDM(window.parent).post('IRiS:ready');
    });*/

})(window.Util.XDM, window.ContentManager);
