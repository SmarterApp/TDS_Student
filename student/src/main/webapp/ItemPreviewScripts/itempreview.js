//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
/*
 * This code is used to support the development page that hosts the blackbox
 */

var ItemPreview = { };

// collection of all the loaded configs
ItemPreview.configs = { };

// this function is called from the blackbox API as its state changes
function blackboxEvent(blackbox, name)
{
    if (name == 'available') ItemPreview.blackboxAvailable();
    else if (name == 'ready') ItemPreview.blackboxReady();
}

// this function is called when the blackbox is available
ItemPreview.blackboxAvailable = function()
{
    // if the blackbox is loaded into a frame then set global aliases 
    if (typeof Blackbox == 'undefined')
    {
        BlackboxWin = BlackboxLoader.getWin();
        BlackboxDoc = BlackboxLoader.getDoc();
        Blackbox = BlackboxWin.Blackbox;
        Accommodations = BlackboxWin.Accommodations;
        Util = BlackboxWin.Util;
        ContentManager = BlackboxWin.ContentManager;
        YAHOO = BlackboxWin.YAHOO; 
        YUI = BlackboxWin.YUI;
        TDS = BlackboxWin.TDS;
    } 
    else
    {
        BlackboxWin = window;
        BlackboxDoc = document;
    }

    // show nav buttons
    Blackbox.showButton('btnSave', save);
    Blackbox.showButton('btnBack');
    Blackbox.showButton('btnNext');

    // setup accommodations and dialog
    createAccommodations();

    // setup scoring engine
    ItemPreview.initItemScoringEngine();
    
    // setup dialog handler url
    if (BlackboxWin.DialogContent) {
        BlackboxWin.DialogContent.urlFrame = Util.Browser.resolveUrl('Pages/API/DialogFrame.axd/load', document);
    }

    // setup word list handler url
    if (BlackboxWin.WordListPanel) {
        BlackboxWin.WordListPanel.xhrUrl = Util.Browser.resolveUrl('Pages/API/WordList.axd', document);
    }
};

// this function gets called when the blackbox is completely ready and everything is initialized
ItemPreview.blackboxReady = function()
{
    // add events to next/back buttons
    ItemPreview.Navigation.bindEvents();

    // setup item preview shell DOM events
    var ddlNavigation = document.getElementById('ddlNavigation');
    if (ddlNavigation != null) ddlNavigation.disabled = false;

    // reload button
    var btnReloadPage = document.getElementById('btnReloadPage');
    YAHOO.util.Event.on(btnReloadPage, 'click', function(evt)
    {
        YAHOO.util.Event.stopEvent(evt);
        // loadSelectedContent(true);
    });
    
    // accessibility button
    var btnAccessibility = document.getElementById('btnAccessibility');
    YAHOO.util.Event.on(btnAccessibility, 'click', function(evt)
    {
        YAHOO.util.Event.stopEvent(evt);
        ItemPreview.openAccessibilityWindow();
    });

    // score button
    var btnItemScore = document.getElementById('btnItemScore');
    YAHOO.util.Event.on(btnItemScore, 'click', function(evt)
    {
        YAHOO.util.Event.stopEvent(evt);
        ItemPreview.score();
    });

    var ddlLayouts = document.getElementById('ddlLayouts');
    if (ddlLayouts != null) ddlLayouts.disabled = false;

    if (IsBlackBoxReadOnly) {
        ContentManager.setReadOnly(true);
    }

    // check for errors
    ContentManager.onPageEvent('show', function(page) {
        var entities = page.getEntities();
        var errors = Util.Array.mapFlatten(entities, function(entity) {
            return entity.getErrors();
        });
        if (errors.length > 0) {
            var errorMsg = 'There were exceptions when loading the page. Please review the console logs for more information. <br/></br>';
            errorMsg += '<b>Exception: </b>' + errors[0].ex;
            TDS.Dialog.showAlert(errorMsg);
        }
    });

    // hide grid settings on item preview
    ContentManager.onPageEvent('hide', function(page)
    {
        YAHOO.util.Dom.setStyle('gridTools', 'display', 'none');
    });
    
    // load anything that is in querystring
    var querystring = Util.QueryString.parse();

    if (querystring.config)
    {
        // json file
        ItemPreview.requestConfig('data', querystring.config);
    }
    else if (querystring.content)
    {
        // folder of xml
        ItemPreview.requestConfig('content', querystring.content);
    }
    else 
    {
        ItemPreview.requestConfig('content', 'ItemPreviewContent');
    }
    
    // check if dev tools should be enabled
    if (querystring.dev == 'true') {
        
        Blackbox.createButtonTool('btnReloadPage', 'Reload', function() {
            ItemPreview.Navigation.reloadPage();
        });

        Blackbox.createButtonTool('btnResponseDialog', 'Response', function() {
            openResponseDialog();
        });

        Blackbox.createButtonTool('btnAccsDialog', 'Accommodations', function() {
            showAccommodations();
        });

        Blackbox.createButtonTool('btnTTSComponent', 'TTS Component', function() {
            showSelectedTTS();
        });

        Blackbox.createButtonTool('btnTTSSelection', 'TTS Selection', function() {
            showSelectedTTS(true);
        });

        Blackbox.createButtonTool('btnLineReader', 'Line Reader', function() {
            TDS.LineReaderControl.toggle();
        });

        Blackbox.createButtonTool('btnMasking', 'Masking', function() {
            TDS.Mask.toggle();
        });

    }

};

// call this to request a config
// type = 'file' (json),  'folder' (directory of xml's) or 'http' (url of a json file)
ItemPreview.requestConfig = function(type, path)
{
    var configKey = type + '-' + path;
    
    // check if there is already a config
    if (ItemPreview.configs[configKey])
    {
        ItemPreview.showConfig(ItemPreview.configs[configKey]);
        return;
    }

    var callback =
    {
        success: ItemPreview.onLoadConfigSuccess,
        failure: ItemPreview.onLoadConfigFailure,
        timeout: 60000,
        argument: configKey
    };

    // request config from server
    if (type == 'http')
    {
        // load the config file directly
        YAHOO.util.Connect.asyncRequest('GET', path, callback);
    }
    else
    {
        // request the config from api
        var requestUrl = window.ItemPreviewUrl + 'Pages/API/API.axd/config?' + type + '=' + path;

        if (ItemPreviewConfigCacheId) {
            requestUrl += '&cache=' + ItemPreviewConfigCacheId;
        }

        // options
        var qs = ItemPreview.parseQueryString();
        if (qs.format) {
            requestUrl += '&format=' + qs.format;
        }
        if (qs.responseType) {
            requestUrl += '&responseType=' + qs.responseType;
        }

        YAHOO.util.Connect.asyncRequest('GET', requestUrl, callback);
    }
};

// this is called when a config is retieved from the server
ItemPreview.onLoadConfigSuccess = function(xhrObj)
{
    // check if text/html which mean we got logged out
    if (Util.String.startsWith(xhrObj.getResponseHeader['Content-Type'], 'text/html'))
    {
        ItemPreview.logout();
        return;
    }

    var divContentLoading = document.getElementById('contentLoading');
    if (divContentLoading != null) {
        divContentLoading.setAttribute('style', 'display:none');
    }

    // parse config
    var config = YAHOO.lang.JSON.parse(xhrObj.responseText);
    
    // process config
    ItemPreview.processConfig(config);

    // save config
    var configKey = xhrObj.argument;
    ItemPreview.configs[configKey] = config;
    
    // show config
    ItemPreview.showConfig(config);
};

// this is called when a config has failed to be retieved from the server
ItemPreview.onLoadConfigFailure = function(xhrObj)
{
    ItemPreview.showAlert('Error', 'There was a problem when loading the config.');
};

// call this function on a config when it is first loaded
ItemPreview.processConfig = function(config)
{
    var configHash = new Util.HashCombiner();

    for (var i = 0; i < config.pages.length; i++)
    {
        var configPage = config.pages[i];
        
        var pageHash = new Util.HashCombiner();

        // add passage hash
        if (configPage.passage != null)
        {
            pageHash.add(configPage.passage.file);
        }

        // add items hash
        if (configPage.items != null)
        {
            for (var j = 0; j < configPage.items.length; j++)
            {
                var configItem = configPage.items[j];
                pageHash.add(configItem.position);
                pageHash.add(configItem.file);
            }
        }

        // set page id
        configPage.id = 'page-' + pageHash.get();
        configHash.add(configPage.id);
    }

    // set config id
    config.id = 'config-' + configHash.get();
};

// takes a config and build the selectbox
ItemPreview.buildDropdown = function(config)
{
    if (config == null) return;

    var ddlNavigation = document.getElementById('ddlNavigation');
    if (ddlNavigation == null) return;
    
    // clear navigation
    ddlNavigation.length = 0;

    // build sections
    if (config.sections != null)
    {
        for (var i = 0; i < config.sections.length; i++)
        {
            var configSection = config.sections[i];
            var newGroupSection = document.createElement('optgroup');
            newGroupSection.setAttribute('id', 'section-' + configSection.id);
            newGroupSection.setAttribute('label', configSection.label);
            ddlNavigation.appendChild(newGroupSection);
        }
    }

    // build pages
    if (config.pages != null)
    {
        for (var i = 0; i < config.pages.length; i++)
        {
            var configPage = config.pages[i];

            //var optionPage = new Option(page.label, page.id);
            var optionPage = document.createElement('option');

            if (configPage.sectionID)
            {
                var groupSection = document.getElementById('section-' + configPage.sectionID);
                groupSection.appendChild(optionPage);
            } 
            else
            {
                ddlNavigation.appendChild(optionPage);
            }

            // NOTE: for IE you have to add option first then you can change text/value (otherwise nothing shows)
            optionPage.text = configPage.label;
            optionPage.value = configPage.id;
        }
    }
};

// call this to show a new config
ItemPreview.showConfig = function(config)
{
    // build selectbox menu
    ItemPreview.buildDropdown(config);

    // set the current config
    ItemPreview.setConfig(config);

    var IPNav = ItemPreview.Navigation;

    // check if keys provided in querystring
    var qs = ItemPreview.parseQueryString();
    if (qs.bankKey >= 0 && qs.itemKey >= 0) {
        var configPage = IPNav.getPageByKeys(qs.bankKey, qs.itemKey);
        if (configPage) {
            IPNav.goToPage(configPage);
        }
        return;
    }

    // go to a saved page id or if it is not available just the first page
    if (!IPNav.goToSavedPage()) {
        IPNav.goToFirstPage();
    }
};

ItemPreview._currentConfig = null;

ItemPreview.setConfig = function(config) 
{
    ItemPreview._currentConfig = config;
    ItemPreview.Navigation.init(config.pages);
};

ItemPreview.getConfig = function() {
    return this._currentConfig;
};

/*****************************************************************************/

ItemPreview.parseQueryString = function () {
    return Util.QueryString.parse(location.search.substring(1));
};

ItemPreview.getCustomLayoutName = function () {
    // get layout (if selected in dropdown)
    var layoutName = "";
    var qs = ItemPreview.parseQueryString();
    if (qs.layout != null) {
        layoutName = qs.layout + '';
    } else if (document.getElementById('ddlLayouts') != null) {
        layoutName = document.getElementById('ddlLayouts').value;
    }

    return (layoutName.length > 0) ? layoutName : null;
};

ItemPreview.openAccessibilityWindow = function()
{
    var configPage = ItemPreview.Navigation.getCurrentPage();
    if (configPage) Blackbox.openWindow(configPage);
};

// this "logs out" which just reloads the page
ItemPreview.logout = function()
{
    TDS.Dialog.showWarning('You have been logged out.', function () 
    {                       
        // reload page
        top.location.href = top.location.href;  
    });
};

ItemPreview.showAlert = function(header, text) {
    
    // stop video from playing (on ipad video blocks dialog from closing)
    var currentPage = ContentManager.getCurrentPage();
    if (currentPage) BlackboxWin.VideoManager.stop(currentPage);

    // show dialog
    TDS.Dialog.show(header, text);
};
