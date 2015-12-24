//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
﻿/*
 * This code is used to support the development page that hosts the blackbox
 */

(function(IP, CM) {
    
    var IPNav = {};

    var pagesIter = null;
    var qs = Util.QueryString.parse();
    
    // you must call this before using Navigation
    IPNav.init = function (pages) {
        pagesIter = new Util.Iterator(pages, { limits: true });
    };

    // gets the current page
    IPNav.getCurrentPage = function () {
        return pagesIter.current();
    };

    // call this function when you want to show the current config page set in the iterator
    IPNav.showCurrentPage = function (reload) {

        var configPage = this.getCurrentPage();
        if (configPage == null) return false;

        // show/hide buttons
        Blackbox.enableButton('btnBack', !this.isFirstPage());
        Blackbox.enableButton('btnNext', !this.isLastPage());

        // set dropdown
        var ddlNavigation = document.getElementById('ddlNavigation');
        if (ddlNavigation != null) {
            ddlNavigation.value = configPage.id;
        }

        // set item configs
        if (configPage.items) {
            var printable = (qs.printable == 'true');
            configPage.items.forEach(function (item) {
                item.printable = printable;
            });
        }

        // set page options
        if (!configPage.layoutName) {
            configPage.layoutName = IP.getCustomLayoutName();
        }

        // load page
        IPNav.savePageId(configPage);
        Blackbox.loadContent(configPage, reload).done(function (page) {
            page.show();
        });

        return true;
    };

    IPNav.reloadPage = function () {
        return IPNav.showCurrentPage(true);
    };

    // get a config page by id string
    IPNav.getPageById = function (pageId) {
        var config = IP.getConfig();
        for (var i = 0; i < config.pages.length; i++) {
            var configPage = config.pages[i];
            if (configPage.id == pageId) {
                return configPage;
            }
        }
        return null;
    };

    // find the first config page that matches the bank and item key
    IPNav.getPageByKeys = function(bankKey, itemKey) {
        var config = IP.getConfig();
        for (var i = 0; i < config.pages.length; i++) {
            var page = config.pages[i];
            if (page.items) {
                for (var j = 0; j < page.items.length; j++) {
                    var item = page.items[j];
                    if (bankKey == item.bankKey &&
                        itemKey == item.itemKey) {
                        return page;
                    }
                }
            }
        }
        return null;
    };

    // go to a specific config page object
    IPNav.goToPage = function (configPage) {
        // check if already viewing page
        if (configPage == null) return false;
        if (configPage == this.getCurrentPage()) return false;
        // jump to page
        if (pagesIter.jumpTo(configPage)) {
            return this.showCurrentPage();
        }
        return false;
    };

    // go to a specific config page id
    IPNav.goToPageId = function (pageId) {
        return this.goToPage(this.getPageById(pageId));
    };

    // try and go to the saved page in the cookie
    IPNav.goToSavedPage = function () {
        var savedPageId = IPNav.getSavedPageId();
        return (savedPageId && IPNav.goToPageId(savedPageId));
    };

    IPNav.goToFirstPage = function () {
        if (pagesIter.reset()) {
            return this.showCurrentPage();
        }
        return false;
    };

    IPNav.goToLastPage = function () {
        if (pagesIter.end()) {
            this.showCurrentPage();
        }
        return false;
    };

    IPNav.goToNextPage = function () {
        if (!CM.fire('requestNextPage', CM.getCurrentPage())) {
            return false;
        }
        if (pagesIter.next()) {
            this.showCurrentPage();
        }
        return false;
    };

    IPNav.goToPreviousPage = function () {
        if (!CM.fire('requestPreviousPage', CM.getCurrentPage())) {
            return false;
        }
        if (pagesIter.prev()) {
            this.showCurrentPage();
        }
        return false;
    };

    IPNav.isFirstPage = function () {
        var currentConfig = IP.getConfig();
        return (currentConfig.pages[0] == this.getCurrentPage());
    };

    IPNav.isLastPage = function () {
        var currentConfig = IP.getConfig();
        return (currentConfig.pages[currentConfig.pages.length - 1] == this.getCurrentPage());
    };

    IPNav.isPagesLoadComplete = function () {
        var config = IP.getConfig();
        if (config && config.pages && config.pages.length > 0)
            return true;
        else {
            return false;
        }
    };

    /*******************************************************/

    IPNav.savePageId = function (configPage) {
        YAHOO.util.Cookie.set('page', configPage.id);
    };

    IPNav.getSavedPageId = function () {
        return YAHOO.util.Cookie.get('page');
    };

    IPNav.bindEvents = function () {
        var btnNextEl = BlackboxDoc.getElementById('btnNext');
        YAHOO.util.Event.on(btnNextEl, 'click', IPNav.goToNextPage, IPNav, true);

        var btnPrevEl = BlackboxDoc.getElementById('btnBack');
        YAHOO.util.Event.on(btnPrevEl, 'click', IPNav.goToPreviousPage, IPNav, true);

        var ddlNavigation = document.getElementById('ddlNavigation');
        YAHOO.util.Event.on(ddlNavigation, 'change', function (evt) {
            var configPage = IPNav.getPageById(ddlNavigation.value);
            IPNav.goToPage(configPage);
        });

        // Bind context menu button (only visible in mobile)
        var btnContext = BlackboxDoc.getElementById('btnContext');
        if (btnContext !== null) {

            // list for menu button
            YAHOO.util.Event.on(btnContext, Util.Event.Mouse.start, function (ev) {

                // prevent click through
                YAHOO.util.Event.stopEvent(ev);

                // open menu right below button
                var btnRegion = YAHOO.util.Dom.getRegion(btnContext);
                ContentManager.Menu.show(ev, null, [btnRegion.left, btnRegion.bottom]);
            });
        }
    };

    IP.Navigation = IPNav;

})(window.ItemPreview, window.ContentManager);

