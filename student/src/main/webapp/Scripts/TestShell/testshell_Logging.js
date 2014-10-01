﻿/*
This code is used for sending test shell logging to the server for debug purposes.
*/

(function(TestShell) {

    function formatTime(d) {
        var hour = d.getHours() % 12;
        hour = Util.Date.xPad(hour === 0 ? 12 : hour, 0);
        var time = hour + ':' +
            Util.Date.xPad(d.getUTCMinutes(), 0) + ':' +
            Util.Date.xPad(d.getUTCSeconds(), 0) + '.' +
            Util.Date.xPad(d.getUTCMilliseconds(), 00);
        return time;
    }

    function formatDate(dt) {
        var formattedTime = formatTime(dt);
        return dt.getUTCMonth() + '/' + dt.getUTCDate() + '/' + dt.getUTCFullYear()+ ' ' + formattedTime;
    }

    function getString(obj) {
        if (obj == null) return '';
        if (obj instanceof Date) return formatDate(obj);
        return obj.toString();
    }

    // takes an array of object literals and turns it into a text report
    function createTable(objArr) {

        // check if array
        if (!YAHOO.lang.isArray(objArr)) return null;
        if (objArr.length == 0) return null;

        var columns = [];
        var columnWidths = [];

        // figure out columns
        for (var name in objArr[0]) {
            columns.push(name);
            columnWidths.push(name.length);
        }

        // figure out column widths
        for (var i = 0; i < objArr.length; i++) {
            var obj = objArr[i];
            for (var j = 0; j < columns.length; j++) {
                var columnName = columns[j];
                var objValue = obj[columnName];
                var objString = getString(objValue);
                if (objString.length > columnWidths[j]) {
                    columnWidths[j] = objString.length;
                }
            }
        }

        var padding = 3;

        var sb = [];
    
        // write out columns
        for (var i = 0; i < columns.length; i++) {
            var columnName = columns[i];
            var columnSize = columnName.length;
            sb.push(columnName);
            var columnWidthRemaining = (columnWidths[i] - columnSize) + padding;
            for (var j = 0; j < columnWidthRemaining; j++) {
                sb.push(' ');
            }
        }
    
        sb.push('\n');

        // write out underlines
        for (var i = 0; i < columns.length; i++) {
            var columnName = columns[i];
            var columnSize = columnName.length;
            for (var j = 0; j < columnWidths[i]; j++) {
                sb.push('-');
            }
            for (var j = 0; j < padding; j++) {
                sb.push(' ');
            }
        }
    
        sb.push('\n');

        // write out rows
        for (var i = 0; i < objArr.length; i++) {
            var obj = objArr[i];
            for (var j = 0; j < columns.length; j++) {
                var columnName = columns[j];
                var objValue = obj[columnName];
                var objString = getString(objValue);
                var columnSize = objString.length;

                sb.push(objString);
                var columnWidthRemaining = (columnWidths[j] - columnSize) + padding;
                for (var k = 0; k < columnWidthRemaining; k++) {
                    sb.push(' ');
                }
            }
        
            sb.push('\n');
        }

        return sb.join('');
    }

    function createReport(name, objArr) {
        var sb = [];
        sb.push('@' + name.toUpperCase() + ':\n\n');
        var tableReport = createTable(objArr);
        sb.push(tableReport);
        sb.push('\n');
        return sb.join('');
    }

    /***************************************************/

    var consoleLog = console.log;
    var consoleHistory = [];
    var consoleMax = 20;

    // call this to enable console history
    function enableConsoleHistory() {

        // http://paulirish.com/2009/log-a-lightweight-wrapper-for-consolelog/
        // http://stackoverflow.com/questions/7942323/pass-arguments-to-console-log-as-first-class-arguments-via-proxy-function
        console.log = function() {

            // record arguments
            consoleHistory.push(arguments);
            
            // check if over max
            if (consoleHistory.length > consoleMax) {
                consoleHistory.slice(); // remove first entry
            }
            
            consoleLog(Array.prototype.slice.call(arguments));
        };

    }
    
    /***************************************************/

    var responsesSent = [];

    // call this to track responses being sent
    function trackResponsesSent(responses) {

        // check if array
        if (!YAHOO.lang.isArray(responses)) return;

        var now = formatDate(new Date());

        for (var i = 0; i < responses.length; i++) {

            var response = responses[i];

            responsesSent.push({
                date: now,
                position: response.position,
                sequence: response.sequence,
                selected: response.isSelected,
                valid: response.isValid,
                length: (response.value != null) ? response.value.length : -1,
                hash: (response.value != null) ? Util.String.hashCode(response.value) : -1
            });
        }
    }

    // subscribe to when responses are sent
    function enableResponseTracking() {
        TestShell.ResponseManager.Events.onSend.subscribe(function(outgoingResponses) {
            setTimeout(function() {
                try {
                    trackResponsesSent(outgoingResponses);
                } catch(ex) {
                    console.error(ex);
                }
            }, 0);
        });
    }

    var contentReceived = [];

    function trackItemReceived(itsItem)
    {
        var now = formatDate(new Date());

        var debugItem = {
            date: now,
            id: itsItem.bankKey + '-' + itsItem.itemKey,
            length: (itsItem.value != null) ? itsItem.value.length : -1,
            hash: (itsItem.value != null) ? Util.String.hashCode(itsItem.value) : -1
        };

        contentReceived.push(debugItem);
    }

    function enableContentTracking() {
        ContentManager.onItemEvent('init', function(page, item, itsItem) {
            trackItemReceived(itsItem);
        });
    }

    // get all the pages info
    function getPages() {

        var debugPages = [];

        var pages = TestShell.PageManager.getPages();
    
        for (var i = 0; i < pages.length; i++) {

            var page = pages[i];
        
            // get based page info
            var debugPage = {
                current: (page === TestShell.PageManager.getCurrent()),
                page: page.pageNum,
                id: page.id,
                segment: null,
                enabled: page.isEnabled(),
                visible: page.isVisible(),
                visited: page.isVisited(),
                completed: page.isCompleted(),
                
                // group
                items: null,
                itemsAnswered: null,
                itemsRequired: null,
                groupRequired: null,
                
                // content
                loaded: page.hasContent(),
                layout: null,
                passage: false,
                coverPage: null
            };

            // get segment info
            if (typeof page.getSegment == 'function') {
                var segment = page.getSegment();
                if (segment) {
                    debugPage.segment = segment.getId();
                }
            }
            
            // page group
            if (page instanceof TestShell.PageGroup) {

                var groupInfo = page.getInfo();
                debugPage.items = groupInfo.itemCount;
                debugPage.itemsAnswered = groupInfo.itemsAnswered;
                debugPage.itemsRequired = groupInfo.itemsRequired;
                debugPage.groupRequired = groupInfo.groupRequired;

                var coverPageInfo = page.getCoverPageInfo();
                if (coverPageInfo) {
                    debugPage.coverPage = coverPageInfo.id;
                }
            }

            // content info
            if (page instanceof TestShell.PageContent) {
                
                var contentPage = page.getContentPage();
                if (contentPage) {
                    debugPage.layout = contentPage.layout;
                    debugPage.items = contentPage.getItems().length;
                    debugPage.passage = contentPage.getPassage() != null;
                }
            }

            debugPages.push(debugPage);
        }

        return debugPages;
    };
    
    function getItems() {

        var itemsDebug = [];
        
        var pageGroups = TestShell.PageManager.getGroups();

        for (var i = 0; i < pageGroups.length; i++) {
            var page = pageGroups[i];
            var responses = page.responses;

            for (var j = 0; j < responses.length; j++) {
                var response = responses[j];
                
                var debugItem = {
                    page: page.pageNum,
                    position: response.position,
                    id: response.id,
                    prefetched: response.prefetched,
                    required: response.isRequired,
                    selected: response.isSelected,
                    valid: response.isValid,
                    sequence: response.sequence,
                    length: (response.value != null) ? response.value.length : -1,
                    hash: (response.value != null) ? Util.String.hashCode(response.value) : -1,
                    format: null,
                    responseType: null
                };
                
                var contentItem = response.getItem();

                if (contentItem) {
                    debugItem.format = contentItem.format;
                    debugItem.responseType = contentItem.responseType;
                }

                itemsDebug.push(debugItem);
            }
            
        }

        return itemsDebug;
    }
    
    function getPlugins() {

        var pluginsInfo = [];
        
        for (var i = 0; i < navigator.plugins.length; i++) {
            var plugin = navigator.plugins[i];
            pluginsInfo.push({
                name: plugin.name,
                version: plugin.version,
                filename: plugin.filename
            });
        }

        return pluginsInfo;
    }
    
    /*function getAudioPlayers() {
        // write out player info
        var players = AudioManager.getPlayers();
        var playersInfo = [];

        for (var i = 0; i < players.length; i++) {
            var player = players[i];
            playersInfo.push({
                name: player.getName(),
                setup: player.isSetup(),
                ready: player.isReady()
            });
        }
        return playersInfo;
    }*/

    function createDebugReport() {

        var sb = [];

        // segments
        var debugSegments = TestShell.SegmentManager.getDebug();
        if (debugSegments.length > 0) {
            sb.push(createReport('Segments', debugSegments));
        }

        // pages
        var debugPages = getPages();
        if (debugPages.length > 0) {
            sb.push(createReport('Pages', debugPages));
        }

        // items
        var debugItems = getItems();
        if (debugItems.length > 0) {
            sb.push(createReport('Items', debugItems));
        }

        // responses
        if (responsesSent.length > 0) {
            sb.push(createReport('Responses Sent', responsesSent));
        }

        // content items received
        if (contentReceived.length > 0) {
            sb.push(createReport('Content Received', contentReceived));
        }

        // audit
        var auditRecords = TestShell.Audit.getList();
        if (auditRecords.length > 0) {
            sb.push(createReport('Pages Audit', auditRecords));
        }
        
        // console
        if (consoleHistory.length > 0) {
            sb.push('@CONSOLE: \n\n');
            
            for (var i = 0; i < consoleHistory.length; i++) {
                sb.push(consoleHistory[i].toString() + '\n');
            }
        }

        return sb.join('');
    }
    
    /*function createAudioReport() {

        var sb = [];

        // plugins
        sb.push(createReport('Plugins', getPlugins()));

        // audio players
        sb.push(createReport('Audio Players', getAudioPlayers()));

        // write out java applet debug logs
        sb.push('@AUDIO APPLET: \n');
        var javaPlayer = AudioManager.getPlayer('java');
        // check if player is registered
        if (javaPlayer) {
            var audioApplet = javaPlayer._getApplet();
            if (audioApplet) {
                // if we get here the applet is in the DOM
                sb.push('DOM: true \n');
                var isLoaded = (typeof audioApplet.getCurrentState == 'function');
                sb.push('Loaded: ' + isLoaded + '\n');
                sb.push('Process Fail: ' + javaPlayer._processFailed + '\n');
                // if this is true then applet has external functions available
                if (isLoaded) {
                    // check the applet state
                    sb.push('State: ' + audioApplet.getCurrentState() + ' \n');
                    sb.push('Debug: \n');
                    sb.push(audioApplet.getDebugLog());
                }
            } else {
                // if we get here applet was not found in the DOM
                sb.push('DOM: false \n');
            }
        }

        return sb.join('');
    }*/

    var Logging = {};

    Logging.writeDebugReport = function() {
        consoleLog(createDebugReport());
    };

    // look for debug shortcut
    if (TestShell.Config.enableLogging) {

    }

    /*Logging.writeAudioReport = function() {
        consoleLog(createAudioReport());
    };
    
    Logging.sendAudioReport = function() {
        var details = createAudioReport();
        TDS.Diagnostics.logServerError('Audio initialization timed out', details);
    };*/

    // set namespace
    TestShell.Logging = Logging;
    
    /***************************************/
    
    // call this to subscribe to shortcut for sending logs
    function enableLogShortcut() {
        
        KeyManager.onKeyEvent.subscribe(function(keyObj) {

            // look for ctrl-alt-shift-D
            if (keyObj && 
                keyObj.ctrlKey && 
                keyObj.altKey && 
                keyObj.shiftKey && 
                keyObj.type == 'keyup' && 
                keyObj.keyCode == 68) {
            
                // send logs and show dialog with reference id
                var details = createDebugReport();
                TDS.Diagnostics.logServerError('TEST SHELL LOGS', details, true);
            }
        });
    }

    // listen for test shell to be ready
    TestShell.Events.subscribe('init', function() {

        // check if logging is enabled
        if (TestShell.Config.enableLogging) {

            console.info('TestShell logging is enabled');
            
            // enableConsoleHistory();
            enableLogShortcut();
            enableContentTracking();
            enableResponseTracking();
        }

    });

})(TestShell);



