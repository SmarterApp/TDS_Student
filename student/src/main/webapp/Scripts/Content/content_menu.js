ContentManager.Menu =
{
    _instance: null
};

ContentManager.Menu.getInstance = function() { return this._instance; };

// create an internal instance of the context menu
ContentManager.Menu.init = function()
{
    // create context menu for this frame
    var menuId = "menu_" + Math.random().toString().split('.')[1];
    var contextMenu = new YAHOO.widget.ContextMenu(menuId, {
        zindex: 1000, // NOTE: the highest z-index in elpa.css is 4
        iframe: false,
        shadow: false
    });

    // save context menu instance
    ContentManager.Menu._instance = contextMenu;

    // event handler for when menu is shown
    contextMenu.showEvent.subscribe(function() {
        var body = ContentManager.getCurrentPage().getDoc().body;
        YUD.addClass(body, 'contextMenuShowing');
    });

    // event handler for before menu is hidden
    contextMenu.beforeHideEvent.subscribe(function() {

        // When hiding menu clearActiveItem() won't blur() <a> because menu is hidden
        if (contextMenu.activeItem && 
            contextMenu.activeItem._oAnchor) {
            Util.Dom.blur(contextMenu.activeItem._oAnchor);
        }
        
        // The above code doesn't always work so if the focused element is the menu then blur() it this way
        if (document.activeElement &&
            YUD.hasClass(document.activeElement, 'yuimenuitemlabel')) {
            Util.Dom.blur(document.activeElement);
        }

        // BUG: #12357 and maybe #12285 
        // Clear all menu items once menu is hiding so keyboard shortcuts don't work on them (some YUI bug??)
        contextMenu.clearActiveItem();
    });

    // event handler for menu is hidden
    contextMenu.hideEvent.subscribe(function() {

        // BUG #16879: When scroll bars appear and you try and clear them right when menu closes there is a problem
        // contextMenu.clearContent();
        var page = ContentManager.getCurrentPage();

        // BUG #15288: Hitting Back/Next gives an error, while the context/Global menu is open.
        // TODO: Review why this might be null.
        if (page == null) return;

        var body = page.getDoc().body;
        if (body) YUD.removeClass(body, 'contextMenuShowing');

        var entity = page.getActiveEntity();
        if (entity == null) return;

        /*
        var contextTarget = contextMenu.contextEventTarget;

        if (contextTarget && YAHOO.lang.isFunction(contextTarget.focus)) {
            // target of the menu (entity component)
            ContentManager.focus(contextTarget);
        }
        */

        // set focus back on iframe (NOTE: helps when in caret mode)
        var pageWin = page.getWin();
        ContentManager.focus(pageWin);

        // fire close event
        try {
            ContentManager.fireEntityEvent('menuhide', entity);
        }
        catch (ex) { }
    });

    // apply text select fix
    ContentManager.Menu.applyTextSelectionFix(contextMenu);

};

// is the context menu showing
ContentManager.Menu.isShowing = function()
{
    return (ContentManager.Menu._instance && 
            ContentManager.Menu._instance.cfg.getProperty('visible'));
};

// call this function when an event (mouse or key) should trigger the context menu to open
ContentManager.Menu.show = function(evt, menuItems /*opt*/, menuXY /*opt*/)
{
    // check if there is an instance of YUI menu created
    if (this._instance == null) return;

    // hide context menu
    if (ContentManager.Menu.hide()) return;

    // if we are in read only mode then hide the 
    // menu if no explicit menu items were passed in
    if (menuItems == null && ContentManager.isReadOnly()) return;

    // get current page and entity
    var page = ContentManager.getCurrentPage();
    if (page == null) return;

    var entity = page.getActiveEntity();
    if (entity == null) return;

    var activeComponent = entity.getActiveComponent();
    if (activeComponent == null) return;
    
    // get menu location
    if (!menuXY) {

        try {

            // component can return the XY directly
            if (YAHOO.lang.isFunction(activeComponent.getXY)) {
                menuXY = activeComponent.getXY(evt);
            }
            // key event returned the XY
            else if (evt == null || evt.keyCode) {
                menuXY = YUD.getXY(activeComponent); // will return false if not a DOM element
            }
            // mouse click returned the XY
            else {
                menuXY = ContentManager.getEventXY(evt);
            }
        }
        catch (ex) {
            Util.log('showContextMenu: error getting XY - \'' + ex + '\'');
        }
    }

    // set default if none assigned
    if (!menuXY) menuXY = [0, 0];

    // if no menu items were passed in then fire event to collect them from the widgets
    if (menuItems == null)
    {
        // create new context menu collection
        var contentMenu = new ContentMenu();

        // get current text selection
        var pageActiveDoc = page.getActiveDoc();
        var pageSelection = ContentManager.getSelection(pageActiveDoc);

        // fire event
        ContentManager.fireEntityEvent('menushow', entity, [contentMenu, evt, pageSelection]);

        // check if someone cancelled showing the menu
        if (contentMenu.cancel) return;

        menuItems = contentMenu.getMenuItems();
    }

    if (menuItems.length == 0) return; // no menu items nothing left to do

    // clear current menu
    ContentManager.Menu._instance.clearContent();

    // set context target of menu
    ContentManager.Menu._instance.contextEventTarget = activeComponent;

    // set position of context menu
    var frameXY = [0, 0];

    // if this page is in a frame we need to adjust menu position to compensate
    var frameEl = page.getFrameElement();
    if (frameEl) frameXY = YUD.getXY(frameEl);

    var menuPosition = [menuXY[0] + frameXY[0], menuXY[1] + frameXY[1]];

    ContentManager.Menu._instance.cfg.setProperty('xy', menuPosition);

    // add the new menu items
    ContentManager.Menu._instance.addItems(menuItems);

    Util.Dom.focusWindow(); // (BUG #26524)

    // render and show menu
    ContentManager.Menu._instance.render(document.body);
    ContentManager.Menu._instance.show();
};

// hide context menu
ContentManager.Menu.hide = function()
{
    if (ContentManager.Menu._instance && ContentManager.Menu.isShowing())
    {
        ContentManager.Menu._instance.hide();
        return true;
    }

    return false;
};

// Fixes issues with YUI menu by assigning menu events to iframe
ContentManager.Menu.applyDocFix = function(contentDoc)
{
    // make sure the content doc is not the same as the host doc
    if (document == contentDoc) return;

    // must load YUI menu at least once for the below code to work (it is lazy loaded)
    new YAHOO.widget.Menu('preload');

    // get all current documents listeners (it has YUI events)
    var docListeners = YAHOO.util.Event.getListeners(document);

    // BUG: In IE this returns null
    if (docListeners == null) return;

    var focusBlurAssigned = false;

    // look for all listeners assigned to the menu manager
    for (var i = 0; i < docListeners.length; i++)
    {
        var listener = docListeners[i];

        if (listener.obj == YAHOO.widget.MenuManager)
        {
            // assign all the same events as the main document gets
            YUE.on(contentDoc, listener.type, listener.fn, YAHOO.widget.MenuManager, true);

            // must assign focus/blur with specific function, just do this once
            if (!focusBlurAssigned)
            {
                YUE.onFocus(contentDoc, listener.fn, YAHOO.widget.MenuManager, true);
                YUE.onBlur(contentDoc, listener.fn, YAHOO.widget.MenuManager, true);
                focusBlurAssigned = true;
            }
        }
    }
};

// this function takes a YUI context menu and fixes issue 
// where any selected text gets deselected when opening it
ContentManager.Menu.applyTextSelectionFix = function(contextMenu) {

    // this fix doesn't support IE but that browser doesn't need it
    if (YAHOO.env.ua.ie) return;

    var makeSelection = function (rangeData) {

        var range = document.createRange();

        if (range) {
            
            // create selection range
            range.setStart(rangeData.anchorNode, rangeData.anchorOffset);
            range.setEnd(rangeData.focusNode, rangeData.focusOffset);

            // get selection
            var selection = window.getSelection();

            // check if selection exists
            if (selection) {

                // clear current selection
                if (selection.removeAllRanges) selection.removeAllRanges(); // Mozilla
                else if (selection.empty) selection.empty(); // IE, Safari

                // add range to current selection
                selection.addRange(range);
            }
        }
    };

    var onFocus = function(type, args, rangeData) {

        this.unsubscribe('focus', onFocus);

        setTimeout(function() {
            try { makeSelection(rangeData); } 
            catch(ex) {}
        }, 0);
    };

    var onBeforeShow = function() {

        // only apply this fix in direct mode
        if (!ContentManager.Renderer.isDirect()) return;

        var selection = window.getSelection(),
            rangeData;

        // check if there is a selection object and it is not collapsed
        if (selection && !selection.isCollapsed) {

            rangeData = {
                anchorNode: selection.anchorNode,
                anchorOffset: selection.anchorOffset,
                focusNode: selection.focusNode,
                focusOffset: selection.focusOffset						
            };

            this.subscribe('focus', onFocus, rangeData);
        }
    };

    contextMenu.subscribe('beforeShow', onBeforeShow);                
};

/********************************************************************/

// this class is created once per context menu request
var ContentMenu = function()
{
    this._global = []; // global menu 
    this._entity = []; // item/passage menu
    this._component = []; // item/passage component menu (e.x., MC option)
    
    // set this to true to cancel a menu show from within a menu event
    this.cancel = false;
};

ContentMenu.prototype.getMenuItems = function()
{
    if (this._component.length > 0) return this._component;
    else if (this._entity.length > 0) return this._entity;
    else if (this._global.length > 0) return this._global;
    return [];
};

// helper function for adding new menu items to YUI menu structure
// @level The menu level (global, entity, component)
// @label The menu text
// @fn The function triggered when clicking on the menu
ContentMenu.prototype.addMenuItem = function(level, label, fn, disabled, checked, insert)
{
    var menuItem;

    if (YAHOO.lang.isString(label))
    {
        menuItem = { text: label };
    }
    else if (YAHOO.lang.isObject(label))
    {
        // label is actually a pre-ready YUI json structure in this case
        menuItem = label;
    }
    else return;

    if (YAHOO.lang.isFunction(fn))
    {
        if (YAHOO.env.ua.gecko)
        {
            // HACK: Balaji said this code is required for fixing SB TTS issues.
            // NOTE: Don't try this on anything other than Firefox.
            var scheduledFunc = function()
            {
                YAHOO.lang.later(0, this, fn);
            };

            menuItem.onclick = { fn: scheduledFunc };
        }
        else
        {
            menuItem.onclick = { fn: fn };
        }
    }

    if (YAHOO.lang.isBoolean(disabled)) menuItem.disabled = disabled;
    if (YAHOO.lang.isBoolean(checked)) menuItem.checked = checked;

    var collection = this['_' + level];

    if (collection)
    {
        if (insert) collection.unshift(menuItem);
        else collection.push(menuItem);
    }
};

ContentMenu.prototype.insertMenuItem = function(level, label, fn, disabled, checked)
{
    this.addMenuItem(level, label, fn, disabled, checked, true);
};
