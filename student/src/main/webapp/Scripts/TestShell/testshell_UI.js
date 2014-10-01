var NotificationType =
{
    none: 0,
    success: 1,
    notice: 2,
    error: 3
};

TestShell.UI =
{
    zoom: null, // test shell zooming

    // default CSS on the test shell
    defaultBodyCSS: null,

    CSS: {
        dialogShowing: 'showingDialog', // use on current iframe when dialog is shown
        popupShowing: 'showingPopup', // use on current iframe when help or tool is used
        loading: 'showingLoading' // used on shell for loading screen
    },

    Nodes: {},
    Events: {}
};

TestShell.UI.Notification =
{
    None: 0,
    Success: 1,
    Notice: 2,
    Error: 3
};

// helper function for adding a dom onclick event
TestShell.UI.addClick = function(id, callback)
{
    var target = YUD.get(id);
    if (target == null) return false;

    // disable targets ability to get focus
    if (ContentManager.enableARIA === false)
    {
        target.setAttribute('tabindex', '-1');

        // BUG #63493: If you stop mousedown on <select> in Chrome/Firefox16+ then it won't open
        if (target.tagName != 'SELECT')
        {
            // disable links from getting focus when clicked
            YUE.on(target, 'mousedown', function(evt) { YUE.stopEvent(evt); });
            YUE.on(target, 'mouseup', function(evt) { YUE.stopEvent(evt); });
        }
    }
    
    YUE.on(target, 'click', function(evt)
    {
        // stop click event on links
        if (target.nodeName == 'A' || target.nodeName == 'SELECT')
        {
            YUE.stopEvent(evt);
        }

        // if the button is not disabled then execute callback
        if (YUD.getAttribute(target, 'disabled') != 'disabled')
        {
            callback.call(this, evt);
        }

    }, this, true);

    return true;
};

// helper function for adding a custom YUI event
TestShell.UI.createEvent = function(name)
{
    this.Events[name] = new YAHOO.util.CustomEvent(name, this, false, YAHOO.util.CustomEvent.FLAT);
};

TestShell.UI.init = function()
{
    // save original CSS to be used for new frames
    this.defaultBodyCSS = document.body.className;

    // create test shell zooming class (also used for tools)
    this.zoom = new ContentZoom(document);
    this.zoom.contentImages = false; // change all images

    // get dom nodes and create events
    this.loadDomNodes();
    this.createDomEvents();

    // hook up to context menu button
    this.enableContextMenuButton();

    // enable the redraw fix if someone clicks the header
    this.enableRedrawFix();

    // add button events
    TDS.Button.init();

    // add aria log
    TDS.ARIA.createLog();
    
    // subscribe to modal dialogs
    /*
    simpleDialog.showEvent.subscribe(function()
    {
        YUD.addClass(TestShell.Frame.getBody(), TestShell.UI.CSS.dialogShowing);
    });

    simpleDialog.hideEvent.subscribe(function()
    {
        YUD.removeClass(TestShell.Frame.getBody(), TestShell.UI.CSS.dialogShowing);
    });
    */
};

TestShell.UI.loadDomNodes = function()
{
    // get header elements
    this.Nodes.ddlNavigation = YUD.get('ddlNavigation');
    this.Nodes.btnDebug = YUD.get('btnDebug');
    this.Nodes.btnHelp = YUD.get('btnHelp');

    // get student tool elements
    this.Nodes.btnZoomIn = YUD.get('btnZoomIn');
    this.Nodes.btnZoomOut = YUD.get('btnZoomOut');
    this.Nodes.btnLineReader = YUD.get('btnLineReader');
    this.Nodes.btnCalculator = YUD.get('btnCalculator');
    this.Nodes.btnFormula = YUD.get('btnFormula');
    this.Nodes.btnPeriodic = YUD.get('btnPeriodic');
    this.Nodes.btnPrint = YUD.get('btnPrint');
    this.Nodes.btnPrintPractice = YUD.get('btnPrintPractice');
    this.Nodes.btnGlobalNotes = YUD.get('btnGlobalNotes');

    // get student control elements
    this.Nodes.lblStatus = YUD.get('lblStatus');
    this.Nodes.btnSave = YUD.get('btnSave');
    this.Nodes.btnPause = YUD.get('btnPause');
    this.Nodes.btnBack = YUD.get('btnBack');
    this.Nodes.btnNext = YUD.get('btnNext');
    this.Nodes.btnEnd = YUD.get('btnEnd');
    this.Nodes.btnResults = YUD.get('btnResults');

    this.Nodes.testName = YUD.get('lblTestName');
    this.Nodes.tools = YUD.get('studentTools');
    this.Nodes.controls = YUD.get('studentControls');
};

TestShell.UI.createDomEvents = function()
{
    // add events for each element
    Util.Array.each(Util.Object.keys(this.Nodes), function(key)
    {
        var element = TestShell.UI.Nodes[key];

        // create event
        TestShell.UI.createEvent(key);

        // add event trigger
        TestShell.UI.addClick(element, function()
        {
            TestShell.UI.Events[key].fire(element);
        });
    });
};

TestShell.UI.isLoading = function()
{
    return YUD.hasClass(document.body, TestShell.UI.CSS.loading);
};

TestShell.UI.showLoading = function(message)
{
    if (YAHOO.lang.isString(message))
    {
        if (YUD.get('loadingMessage'))
        {
            YUD.get('loadingMessage').innerHTML = message;
        }
    }
    else
    {
        // YUD.get('loadingMessage').innerHTML = '';
    }

    if (this.isLoading()) return false;
    return YUD.addClass(document.body, TestShell.UI.CSS.loading);
};

TestShell.UI.hideLoading = function()
{
    if (!this.isLoading()) return false;
    return YUD.removeClass(document.body, TestShell.UI.CSS.loading);
};

TestShell.UI.enableControl = function(id, enabled)
{
    // get control
    var control = YUD.get(id);
    if (control == null || control.parentNode == null) return;

    // enable/disable button?
    if (enabled)
    {
        YUD.addClass(control.parentNode, 'active');
        YUD.removeClass(control.parentNode, 'inactive');
        control.removeAttribute('disabled');
    }
    else
    {
        YUD.removeClass(control.parentNode, 'active');
        YUD.addClass(control.parentNode, 'inactive');
        control.setAttribute('disabled', 'disabled');
    }
};

// refresh the controls on the page to the latest state
TestShell.UI.updateControls = function()
{
    TestShell.UI.enableControl('btnPause', true);

    var currentGroup = TestShell.PageManager.getCurrent();

    if (currentGroup != null)
    {
        // show back button as long as we are not on the first page
        TestShell.UI.enableControl('btnBack', !TestShell.PageManager.isFirst(currentGroup));
        TestShell.UI.enableControl('btnNext', true);
    }
    else
    {
        TestShell.UI.enableControl('btnBack', false);
        TestShell.UI.enableControl('btnNext', false);
    }

    TestShell.UI.enableControl('btnEnd', TestShell.isTestCompleted());

    // show message on notification bar when we receive response from the server we are finished with the test
    if (TestShell.testFinished)
    {
        TestShell.UI.showNotification(TestShell.UI.Notification.Success, Messages.get('TestCompleted'));
    }

    // check if we are showing item scores
    if (TDS.showItemScores)
    {
        TestShell.UI.enableControl('btnPause', false);
        TestShell.UI.enableControl('btnEnd', false);
        TestShell.UI.enableControl('btnResults', true);
        TestShell.UI.showNotification(TestShell.UI.Notification.Success, Messages.get('TestItemScores'));
    }

    // update dropdown with anything that has changed
    TestShell.Navigation.update();

    // figure out how many responses the user has seen
    var responsesSoFar = 0;
    
    // OLD:
    /*var lastVisibleGroup = TestShell.PageManager.getLastGroup(true);
    if (lastVisibleGroup != null) responsesSoFar = lastVisibleGroup.responses[lastVisibleGroup.responses.length - 1].position;*/

    // assume all responses before the first position were answered
    var firstGroup = TestShell.PageManager.getFirstGroup();
    if (firstGroup && firstGroup.responses) responsesSoFar = firstGroup.responses[0].position - 1;

    // count num responses answered for all visible groups
    var allGroups = TestShell.PageManager.getGroups();
    Util.Array.each(allGroups, function(group)
    {
        responsesSoFar += group.getNumAnswered();
    });

    // Display test name info
    // BUG: in IE 7 (6, 8?) this makes the questions flicker for reading test
    var testLabel = TestShell.Config.testName + ' (' + responsesSoFar + ' ' + Messages.get('TDSShellUIJS.Label.OutOf') + ' ' + TestShell.Config.testLength + ')';
    TestShell.UI.Nodes.testName.innerHTML = testLabel;
};

// check if there are any items that require saving for the group, if so show the save button
TestShell.UI.showSave = function(group)
{
    var enable = false;

    if (group && group.responses)
    {
        // check if any item allows explicit save
        enable = Util.Array.find(group.responses, function(response)
        {
            return (response.getItem().saveOptions.explicit);
        });
    }

    // add enabled class
    if (enable) YUD.addClass('btnSave', 'enable');
    else YUD.removeClass('btnSave', 'enable');

    // add inactive class
    TestShell.UI.enableControl('btnSave', enable);

    return enable;
};

// Shows a message to the user at the top of the screen
// notification: notification object
TestShell.UI.showNotification = function(type, message)
{
    // check if notification container exists
    var notificationsContainer = YUD.getElementsByClassName('notificationsContainer', 'div');
    if (notificationsContainer == null || notificationsContainer.length == 0) return false;

    if (type == null || type == 0)
    {
        YUD.get('pnlNotifySuccess').style.display = 'none';
        YUD.get('pnlNotifyNotice').style.display = 'none';
        YUD.get('pnlNotifyError').style.display = 'none';
    }
    else if (type == 1)
    {
        YUD.get('pnlNotifySuccess').style.display = '';
        YUD.get('pnlNotifySuccess').innerHTML = message;
    }
    else if (type == 2)
    {
        YUD.get('pnlNotifyNotice').style.display = '';
        YUD.get('pnlNotifyNotice').innerHTML = message;
    }
    else if (type == 3)
    {
        YUD.get('pnlNotifyError').style.display = '';
        YUD.get('pnlNotifyError').innerHTML = message;
    }

    return true;
};

TestShell.UI.reload = function()
{
    TestShell.UI.showLoading();
    var currentGroup = TestShell.PageManager.getCurrent();
    if (currentGroup) currentGroup.requestContent(true);
};

TestShell.UI.showContentError = function()
{
    TestShell.UI.hideLoading();

    TestShell.UI.showErrorPrompt('ContentTimeout',
    {
        yes: function()
        {
            // reload frame
            TestShell.UI.reload();

            // log
            var group = TestShell.PageManager.getCurrent();

            if (group)
            {
                TDS.Diagnostics.logServerError('CONTENT ' + group.id + ': Reload');
            }
        },
        no: function()
        {
            // force pause
            TestShell._pauseInternal(true);
        }
    });
};

TestShell.UI.zoomIn = function()
{
    var currentPage = ContentManager.getCurrentPage();
    if (currentPage) currentPage.zoomIn();
};

TestShell.UI.zoomOut = function()
{
    var currentPage = ContentManager.getCurrentPage();
    if (currentPage) currentPage.zoomOut();
};

TestShell.UI.toggleLineReader = function()
{
    TDS.LineReaderControl.toggle();
};

TestShell.UI.clearScreen = function()
{
    while (true)
    {
        try { document.body.removeChild(document.body.firstChild); }
        catch(ex) { break; }
    }
};

// this hooks up the context menu button in the toolbar
TestShell.UI.enableContextMenuButton = function() {

    var btnContext = YUD.get('btnContext');
    if (btnContext == null) return;

    // list for menu button
    YUE.on(btnContext, Util.Event.Mouse.start, function(ev) {
        
        // prevent click through
        YUE.stopEvent(ev);

        // open menu right below button
        var btnRegion = YUD.getRegion(btnContext);
        ContentManager.Menu.show(ev, null, [btnRegion.left, btnRegion.bottom]);
    });
};

// show global shortcuts menu
TestShell.UI.showGlobalContextMenu = function(ev)
{
    // check if we can open menu?
    // TODO: check for if dialog is open 
    if (TestShell.Comments.isShowing()) return;

    // addMenuItem = function(level, label, fn, disabled, checked, insert)
    var contentMenu = new ContentMenu();

    // get elements text
    var getText = function(id)
    {
        var el = YUD.get(id);
        return el.innerText ? el.innerText : el.textContent;
    };

    // add a link as a menu item
    var addLinkToMenu = function(link, menuClass, alternateText)
    {
        link = YUD.get(link);

        if (link == null) return;
        if (YUD.getStyle(link, 'display') == 'none') return; // don't show hidden links
        if (YUD.hasClass(link, 'excludeMenu')) return; // skip links with this class

        var menuFunc = function()
        {
            setTimeout(function() { Util.Event.selectLink(link); }, 0);
        };

        var menuItem =
        {
            text: YAHOO.lang.isString(alternateText) ? alternateText : getText(link.id),
            classname: menuClass,
            onclick: { fn: menuFunc }
        };

        contentMenu.addMenuItem('global', menuItem);
    };

    // add a collection of links as menu items
    var addLinksToMenu = function(parentID)
    {
        var parent = YUD.get(parentID);
        var links = parent.getElementsByTagName('a');

        YUD.batch(links, function(link)
        {
            addLinkToMenu(link, link.className);
        });
    };

    // add a link for the sound cue in the global context menu if available
    var currentPage = ContentManager.getCurrentPage();

    if (currentPage && currentPage.soundCue)
    {
        var linkEl = YUD.get(currentPage.soundCue.id);

        if (linkEl != null && !TDS.Audio.isActive()) {
            addLinkToMenu(linkEl, 'sound_instructions', Messages.getAlt('TDSAudioJS.Label.AddMenuPlayInstruction', 'Play Instructions'));
        }
    }

    // add menu items
    addLinksToMenu(this.Nodes.controls);
    addLinksToMenu(this.Nodes.tools);
    addLinkToMenu(this.Nodes.btnHelp, 'help');

    // show global shortcuts menu
    ContentManager.Menu.show(ev, contentMenu.getMenuItems());
};

TestShell.UI.enableRedrawFix = function() {

    var navigationEl = YUD.get('navigation');
    if (navigationEl == null) return;

    YUE.on(navigationEl, Util.Event.Mouse.start, function(ev) {

        var targetEl = YUE.getTarget(ev);
        
        // check if the target is only the navigation bar
        if (navigationEl == targetEl) {
            ContentManager.applyRedrawFix();
        }
    });
};

// code for creating test shell buttons
(function(UI) {

    function createButton(parentId, id, label, className, fn) {

        // get the parent
        var topBarEl = YUD.get(parentId);
        if (topBarEl == null) return null;

        // get the list
        var ulEl = YUD.getFirstChild(topBarEl);
        if (ulEl == null) return null;

        var liEl = document.createElement('li');

        var linkEl = document.createElement('a');
        if (id) {
            linkEl.id = id;
        }
        YUD.setAttribute(linkEl, 'href', '#');
        YUD.setAttribute(linkEl, 'tabindex', '-1');
        if (className) {
            YUD.addClass(linkEl, className);    // adding className for generating global menu accordingly
        }
        liEl.appendChild(linkEl);

        var spanEl = document.createElement('span');
        spanEl.className = 'icon';
        if (label) {
            spanEl.innerHTML = label;
        }
        linkEl.appendChild(spanEl);

        if (YAHOO.lang.isFunction(fn)) {
            UI.addClick(linkEl, fn);
        }
        
        // add button to list
        ulEl.appendChild(liEl);

        return liEl;
    };

    // add a button to the top bar tool section 
    UI.addButtonTool = function(id, label, className, fn) {
        return createButton('studentTools', id, label, className, fn);
    };

    // add a button to the top bar controls section 
    UI.addButtonControl = function (id, label, className, fn) {
        return createButton('studentControls', id, label, className, fn);
    };

})(TestShell.UI);

/****************************************************************************************/   

// Dialogs
(function(UI) {

    function showAlert(textHeader, textMessage, funcOk) {
        
        // close menu and hide loading screen
        ContentManager.Menu.hide();
        UI.hideLoading();

        var handleOk = function() {
            this.hide();
            top.focus();
            if (funcOk) {
                funcOk();
            }
        };

        // Ok
        var buttons = [
            { text: Messages.get('Global.Label.OK'), handler: handleOk }
        ];

        TDS.Dialog.show(textHeader, textMessage, buttons);
    }

    UI.showAlert = showAlert; // showAlert

    UI.showWarning = function(textMessage, funcOk) { // showWarningAlert
        var textHeader = Messages.get('TDSShellUIJS.Label.Warning');
        showAlert(textHeader, textMessage, funcOk);
    };

    UI.showError = function(textMessage, funcOk) { // showWarningError
        var textHeader = Messages.get('TDSShellUIJS.Label.Error');
        showAlert(textHeader, textMessage, funcOk);
    };

    // TDS shell prompt
    function showPrompt(textHeader, textMessage, obj)
    {
        // hide any context menus
        ContentManager.Menu.hide();

        // prepare labels
        obj.noLabel = Messages.get(obj.noLabel ? obj.noLabel : 'Global.Label.No');
        obj.yesLabel = Messages.get(obj.yesLabel ? obj.yesLabel : 'Global.Label.Yes');
    
        // prepare functions
        var yesHandler = function()
        {
            this.hide();
            top.focus();

            if (obj.yes)
            {
                if (obj.scope) obj.yes.call(obj.scope);
                else obj.yes();
            }
        };

        var noHandler = function()
        {
            this.hide();
            top.focus();

            if (obj.no)
            {
                if (obj.scope) obj.no.call(obj.scope);
                else obj.no();
            }
        };

        // No, Yes
        var buttons = [
            { text: obj.noLabel, handler: noHandler, isDefault: true }, // NOTE: If isDefault is set on yes then tabbing seems to have problems
            { text: obj.yesLabel, handler: yesHandler }
        ];

        // Logout
        if (obj.logout)
        {
            var logoutHandler = function()
            {
                this.hide();
                UI.clearScreen();
                TestShell.redirectLogin();
            };

            var logoutLabel = Messages.getAlt('Global.Label.Logout', 'Logout');
            buttons.push({ text: logoutLabel, handler: logoutHandler });
        }
    
        TDS.Dialog.show(textHeader, textMessage, buttons);
    }

    // show warning prompt
    UI.showWarningPrompt = function(textMessage, obj) { // showWarningPrompt
        var textHeader = Messages.get('TDSShellUIJS.Label.Warning');
        textMessage = ErrorCodes.get(textMessage);
        showPrompt(textHeader, textMessage, obj);
    };

    // show error prompt
    UI.showErrorPrompt = function(textMessage, obj) { // showErrorPrompt
        var textHeader = Messages.get('TDSShellObjectsJS.Label.Error');
        textMessage = ErrorCodes.get(textMessage);
        showPrompt(textHeader, textMessage, obj);
    };

})(TestShell.UI);

