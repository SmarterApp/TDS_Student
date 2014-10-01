/*
This code is used to add pagination to item groups.
*/

(function (CM) {

    var CSS_WIDGET = 'multi-page'; // widget <ul> container
    var CSS_ITEMACTIVE = 'page-active'; // widget <li> when active
    var CSS_ITEMANSWERED = 'page-answered'; // widget <li> when answered
    var CSS_ITEMSHOW = 'page-item-show'; // item <div class="itemContainer"> when showing/active 
    var CSS_ITEMHIDE = 'page-item-hidden'; // item <div class="itemContainer"> when hidden 

    // collection of pages we have paginated
    var paginatedPages = [];

    // get tab element for an item
    function getTabEl(item) {
        return document.getElementById('itemTab-' + item.position);
    }

    // create a single tab html for the widget
    function createTabEl(item) {
        var itemTabEl = document.createElement('li');
        itemTabEl.setAttribute('id', 'itemTab-' + item.position);
        var itemLinkEl = document.createElement('a');
        itemLinkEl.setAttribute('href', '#');
        itemLinkEl.setAttribute('tabindex', '0');
        var tabData = { item: item };
        $(itemLinkEl).on('click', tabData, onTabClick);
        $(itemLinkEl).text(item.position);
        itemTabEl.appendChild(itemLinkEl);
        return itemTabEl;
    }

    // create the widget html
    function createWidgetEl(items) {
        var containerEl = document.createElement('ul');
        containerEl.className = CSS_WIDGET;
        items.forEach(function(item) {
            var itemTabEl = createTabEl(item);
            containerEl.appendChild(itemTabEl);
        });
        return containerEl;
    }

    // this is fired when someone clicks on a tab
    function onTabClick(evt) {
        var item = evt.data.item;
        item.setActive();
    }

    // this is called when we want to update a tab to show if response has been updated
    function updateItemAnswered(item) {
        var itemEl = item.getElement();
        var tabEl = getTabEl(item);
        var response = item.getResponse();
        if (response && response.isValid) {
            YUD.addClass(tabEl, CSS_ITEMANSWERED);
        } else {
            YUD.removeClass(tabEl, CSS_ITEMANSWERED);
        }
    }

    // check if a page can be paginated
    function allowPagination(page) {

        // check if accessibility shell
        if (ContentManager.isAccessibilityEnabled()) {
            return false;
        }

        // ignore compound layouts
        var compoundEl = page.getCompoundElement();
        if (compoundEl) {
            return false;
        }

        // make sure we have a passage and more than one item
        var passage = page.getPassage();
        var items = page.getItems();
        if (passage == null || items.length < 2) {
            return false;
        }

        // make sure pagination acc is enabled
        var accProps = page.getAccommodationProperties();
        if (accProps == null || !accProps.isPaginatedItemGroupsEnabled()) {
            return false;
        }

        return true;
    }

    // check if a page was already paginated
    function isPaginated(page) {
        return (paginatedPages.length > 0 &&
                paginatedPages.indexOf(page) != -1);
    }

    // get the items that should be placed in the tab
    function getItems(page) {

        var items = page.getItems();

        // HACK: if we are using simulator layout then get all items but the simulator
        // NOTE: PageLayout.cs RenderSingleMultiItems() for more info
        if (page.layout == '28' ||
            page.layout == '29') {
            items = items.filter(function(item) {
                return item.format.toUpperCase() != 'SIM';
            });
        }

        return items;
    }

    // setup pagination on a page 
    function processPage(page) {

        // add page to our lookup
        if (!isPaginated(page)) {
            paginatedPages.push(page);
        }

        // process items
        var items = getItems(page);

        // BUG: item.EBSR does not exist (Utah_PT: Reading 6-8)
        /*items.forEach(function (item) {
            updateItemAnswered(item);
        });*/

        // check if any items
        if (items.length == 0) return;
        var firstItem = items[0];

        // create widget
        var widgetEl = createWidgetEl(items);

        // add widget right before the first item <div class="itemContainer">
        $(firstItem.getElement()).before(widgetEl);

        // focus on the first item we want to show
        processItemFocus(page, firstItem);
    }

    // figure out what item is showing or hiding based on the focus event
    function processItemFocus(page, currentItem) {

        // check if page is paginated
        if (!isPaginated(page)) return;

        // check if focused item one that is paginated
        var items = getItems(page);
        if (items.indexOf(currentItem) == -1) return;

        // show/hide items
        items.forEach(function (item) {
            if (item == currentItem) {
                onItemShow(page, item);
            } else {
                onItemHide(page, item);
            }
        });
    }

    // this is called when an item shows in a group
    function onItemShow(page, item) {
        var itemEl = item.getElement();
        var tabEl = getTabEl(item);
        YUD.removeClass(itemEl, CSS_ITEMHIDE);
        YUD.addClass(itemEl, CSS_ITEMSHOW);
        YUD.addClass(tabEl, CSS_ITEMACTIVE);

        // scroll back to top
        var scrollEl = page.getScrollableElement();
        if (scrollEl) {
            scrollEl.scrollTop = 0;
            scrollEl.scrollLeft = 0;
        }
    }

    // this is called when an item is hidden in the group
    function onItemHide(page, item) {
        var itemEl = item.getElement();
        var tabEl = getTabEl(item);
        YUD.addClass(itemEl, CSS_ITEMHIDE);
        YUD.removeClass(itemEl, CSS_ITEMSHOW);
        YUD.removeClass(tabEl, CSS_ITEMACTIVE);
        updateItemAnswered(item);
    }
    
    // check if we shuld process this page for pagination
    CM.onPageEvent('available', function (page) {
        if (allowPagination(page)) {
            processPage(page);
        }
    });

    CM.onItemEvent('focus', processItemFocus);

})(ContentManager);