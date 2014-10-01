/*
This contains general content manager keyboard subscriptions. 
*/

// attach event listeners to a documentframe for key events
ContentManager.addKeyEvents = function(keyEventObj) 
{
    if (!this.enableKeyEvents) return false;
    if (keyEventObj == null) return false;
    if (keyEventObj.__tds_keyEventsEnabled === true) return false;

    YUE.addListener(keyEventObj, "keydown", this.onKeyEvent, this, true);
    YUE.addListener(keyEventObj, "keypress", this.onKeyEvent, this, true);
    YUE.addListener(keyEventObj, "keyup", this.onKeyEvent, this, true);

    keyEventObj.__tds_keyEventsEnabled = true;
    return true;
};

// call this function when any keyboard event gets fired
ContentManager.onKeyEvent = function(evt)
{
    // BUG #15461: Able to open the context menu using Ctl M in the background while warning dialog box is open
    if (ContentManager.isDialogShowing()) return;

    // CTRL-M (this check must be before isMenuShowing())
    if (evt.ctrlKey && evt.keyCode == 77)
    {
        YUE.stopEvent(evt);
        if (evt.type == 'keydown') {
            ContentManager.Menu.show(evt);
        }
    };

    // if the menu is showing then don't process key events (menu deals with its own key events)
    if (ContentManager.Menu.isShowing()) return;
    if (ContentManager.isDialogShowing()) return;

    // need to stop tab or it will tab to different elements (inputs, links)
    if (evt.keyCode == 9) YUE.stopEvent(evt);

    var currentPage = ContentManager.getCurrentPage();
    if (currentPage == null) return;

    var currentEntity = currentPage.getActiveEntity();
    if (currentEntity == null) return;

    // assign some DOM3 key names to the event if they don't exist
    if (!evt.key) {
        switch (evt.keyCode) {
            case 9:  evt.key = 'Tab'; break;
            case 13: evt.key = 'Enter'; break;
            case 27: evt.key = 'Esc'; break;
            case 32: evt.key = 'Space'; break;
            case 37: evt.key = 'Left'; break;
            case 38: evt.key = 'Up'; break;
            case 39: evt.key = 'Right'; break;
            case 40: evt.key = 'Down'; break;
        }
    }

    // fire events
    this.firePageEvent('keyevent', currentPage, [evt], false);
    this.fireEntityEvent('keyevent', currentEntity, [evt], false);
};

// listen for page key events
ContentManager.onPageEvent('keyevent', function(page, evt)
{
    if (evt.type != 'keydown') return;

    // entity focus (items and passage)
    if (evt.key == 'Tab' && !evt.ctrlKey && !evt.altKey)
    {
        // shift-tab
        if (evt.shiftKey) page.prevEntity();
        // tab
        else page.nextEntity();
    }
});

// listen for up/down keys
// listen for item/passage key events
ContentManager.onEntityEvent('keyevent', function(page, entity, evt)
{
    var keyCode = evt.keyCode;

    // make sure an arrow key
    var arrowKeys = (keyCode >= 37 && keyCode <= 40);
    if (!arrowKeys) return;

    // make sure no other keys are held down
    if (Util.Event.hasModifier(evt)) return;

    // make sure not in caret mode
    if (Mozilla.inCaretMode()) return;

    var target = YAHOO.util.Event.getTarget(evt);

    // if we are in an text area then lets leave here
    if (Util.Event.inTextInput(evt)) return;

    // make sure not on a form element
    if (Util.Dom.isFormElement(target)) return;

    // make sure not in grid
    if (target.tagName == 'svg') return;

    // stop event
    YUE.stopEvent(evt);

    // only scroll on ctrl and keydown
    if (evt.type != 'keydown') return; // !evt.domEvent.ctrlKey

    // scroll page
    page.scroll(evt.key); // up/down/left/right
});

// listen for tab keys
ContentManager.onEntityEvent('keyevent', function(page, entity, evt)
{
    if (evt.type != 'keydown') return;

    // check if in caret mode
    if (Mozilla.inCaretMode())
    {
        // if esc is held down then exit from caret mode
        if (evt.key == 'Esc')
        {
            ContentManager.enableCaretMode(false);
        }

        // leave here and ignore up/down while in caret mode
        return;
    }

    // component focus within an item/passage (ctrl-tab/ctrl-shift-tab)
    if (evt.key == 'Tab' && evt.ctrlKey && !evt.altKey)
    {
        if (evt.shiftKey) entity.prevComponent();
        else entity.nextComponent();

        YUE.stopEvent(evt);
        return;
    }
});

// this function is for fixing for when the cursor goes out of a selected components container
(function()
{
    var lastRange = null;

    ContentManager.onEntityEvent('keyevent', function(page, entity, evt)
    {
        // ignore up/down while in caret mode
        if (!Mozilla.inCaretMode()) return;

        var caretMoveKey = (evt.key == 'Up' || evt.key == 'Down' || evt.key == 'Left' || evt.key == 'Right');
        if (!caretMoveKey) return;

        // get selection range elements
        var pageWin = entity.getPage().getWin();
        var selection = pageWin.getSelection();
        var range;

        try
        {
            range = selection.getRangeAt(0);
        }
        catch (ex) { return; } // "Component returned failure code: 0x80070057 (NS_ERROR_ILLEGAL_VALUE) [nsISelection.getRangeAt]" nsresult: "0x80070057 (NS_ERROR_ILLEGAL_VALUE)"

        // make sure we got a range
        if (!range) return;

        // check if the start/end elements of this range are out of bounds
        var activeElement = entity.getActiveComponent();
        var startElement = range.startContainer;
        var endElement = range.endContainer;

        var outOfBounds = (ContentManager.getAncestor(startElement, [activeElement]) == null);
        if (!outOfBounds) outOfBounds = (ContentManager.getAncestor(endElement, [activeElement]) == null);

        // if the cursor is out of bounds and we have a last know good range then select that
        if (outOfBounds)
        {
            // make sure we have a last known in bounds range
            if (!lastRange) return;

            try
            {
                // reselect last known in bounds range
                selection.collapseToStart();
                selection.removeAllRanges();
                selection.addRange(lastRange);
            }
            catch (ex) { return; } // possible component returned exception
        }
        else
        {
            lastRange = range.cloneRange();
        }
    });

})();

// HACK: This fixes an issue on the old Mac OS X securebrowser where you cannot select text
(function()
{
    // check if this browser requires the fix
    var needsSelectionFix = (Util.Browser.isMac() && Util.Browser.isSecure() && Util.Browser.getFirefoxVersion() == 2);
    if (!needsSelectionFix) return;

    // function for applying fix
    var applyfix = function(el)
    {
        YUD.addClass(el, 'selectionFix');
        YUE.on(el, 'focus', function() { top.focus(); });
        YUE.on(el, 'mouseover', function() { top.focus(); });
    };

    // apply fix to elements that can be selected
    ContentManager.onPassageEvent('available', function(page, passage)
    {
        applyfix(passage.getElement());
    });

    ContentManager.onItemEvent('available', function(page, item)
    {
        applyfix(item.getStemElement());
    });

})();


// listen for up/down keys
// listen for item/passage key events
ContentManager.onEntityEvent('keyevent', function(page, entity, evt)
{
    if (evt.type != 'keyup') return;

    if (evt.key == 'Esc')
    {
        setTimeout(function()
        {
            // entity.setActive(null, true);

            var activeComponent = entity.getActiveComponent();

            if (activeComponent)
            {
                entity.setActiveComponent(activeComponent, true);
            }
        }, 0);
    }

});
