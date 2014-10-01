/*
This is used to add simple options to the context menu. If you have a very complex
menu option then please create a seperate module (e.x., TTS) to deal with this.
*/

// context menu logic for caret mode
ContentManager.onEntityEvent('menushow', function(page, entity, menu, evt)
{
    // don't enable for mouse clicks
    if (!evt.keyCode) return false;

    // get entity info
    var isPassage = (entity instanceof ContentPassage);
    var isStem = (entity instanceof ContentItem) && (entity.getStemElement() == entity.getActiveComponent());

    // only allow passage/stem to get caret mode
    if (!isPassage && !isStem) return false;

    // check if there is an accommodation that requires caret mode
    var allowedAccommodation = false;

    var accProps = page.getAccommodationProperties();
    if (accProps.hasHighlighting()) allowedAccommodation = true;
    else if (isPassage && accProps.hasTTSStimulus()) allowedAccommodation = true;
    else if (isStem && accProps.hasTTSItem()) allowedAccommodation = true;

    if (!allowedAccommodation) return false;

    // check if this browser supports heightened privileges
    if (!Mozilla.execPrivileged()) return;

    if (Mozilla.inCaretMode())
    {
        var menuFunc = function()
        {
            // disable caret mode
            ContentManager.enableCaretMode(false);
        };

        var menuItem =
        {
            text: Messages.get('TDSContentEventsJS.Link.DisableTextSelection'),
            classname: 'caretMode',
            onclick: { fn: menuFunc }
        };

        menu.addMenuItem('entity', menuItem);
    }
    else
    {
        var menuFunc = function()
        {
            // BUG: Need to refocus component to reset caret
            var focusedComponent = entity.getActiveComponent();

            // make sure DOM element
            if (focusedComponent && focusedComponent.focus)
            {
                entity.clearComponent();
                entity.setActiveComponent(focusedComponent);

                // enable caret mode
                ContentManager.enableCaretMode(true);
            }
        };

        var menuItem =
        {
            text: Messages.get('TDSContentEventsJS.Link.EnableTextSelection'),
            classname: 'caretMode',
            onclick: { fn: menuFunc }
        };

        menu.addMenuItem('entity', menuItem);
    }

});

// context menu for passage printing
ContentManager.onPassageEvent('menushow', function(page, passage, menu, evt)
{
    // a helper function for adding simple menu items
    var addMenu = function(messageKey, menuClass, menuFunc)
    {
        var menuItem = {
            text: Messages.get(messageKey),
            classname: menuClass,
            onclick: { fn: menuFunc }
        };

        menu.addMenuItem('entity', menuItem);
    };

    if (typeof (tdsPassagePrint) != 'function') return;
    if (YUD.hasClass(document.body, 'unproctored')) return;
    if (!ContentManager.isVisible('btnPrint')) return; // BUG #15619

    // PRINT PASSAGE
    addMenu('TDSContentEventsJS.Label.PrintPassage', 'printPassage', function()
    {
        // TDS notification
        tdsPassagePrint();
    });
});

// context menu for items
ContentManager.onItemEvent('menushow', function(page, item, menu, evt)
{
    var activeComp = item.getActiveComponent();
    
    // Check to see if active component is an EBSR prompt, and treat it like a stem
    var isEbsrPrompt = function () {
        if (!item.EBSR) {
            return false;
        }
        var prompts = item.EBSR.getPrompts();
        if (prompts.indexOf(activeComp) < 0) {
            return false;
        } else {
            return true;
        }
    };
    var isActiveStem = activeComp == item.getStemElement();
    if (item == null || (!isActiveStem && !isEbsrPrompt())) {
        return;
    }
    // a helper function for adding simple menu items
    var addMenu = function(messageKey, menuClass, menuFunc)
    {
        var menuItem = {
            text: Messages.get(messageKey),
            classname: menuClass,
            onclick: { fn: menuFunc }
        };

        menu.addMenuItem('entity', menuItem);
    };

    // MENU: Mark for review
    if (item.hasMarkLink())
    {
        var messageKey = item.isMarked() ? 'TDSContentEventsJS.Label.UnmarkForReview' : 'TDSContentEventsJS.Label.MarkForReview';

        addMenu(messageKey, 'markReview', function()
        {
            item.toggleMark();
        });
    }

    // MENU: Comments
    if (item.hasCommentLink())
    {
        var messageLabel = ContentManager.getCommentLabel();

        addMenu(messageLabel, 'comment', function()
        {
            item.toggleComment();
        });
    }

    // MENU: Print Item
    if (item.hasPrintLink())
    {
        addMenu('TDSContentEventsJS.Label.PrintItem', 'printItem', function()
        {
            item.print();
        });
    }

    // MENU: GTR
    if (item.hasGTRLink())
    {
        var menuName = Messages.getAlt('TDSContentEventsJS.Label.GTRItem', 'Guide to Revision');

        addMenu(menuName, 'gtrItem', function()
        {
            item.openGTR();
        });
    }

    // MENU: Help/Tutorial
    if (item.hasHelpLink())
    {
        addMenu('TDSContentEventsJS.Label.HelpItem', 'helpItem', function()
        {
            item.openHelp();
        });
    }

    //Turn off Tools that should be disabled on right clicks
    if(TDS.LineReaderControl){ 
        TDS.LineReaderControl.off();
    } 
});

// subscribe to when context menu is hidden for passage/items
/*
ContentManager.subscribe('entityMenuHide', function(evt)
{
// BUG #15486: Error message shows up by hitting Ctl+M/Ctl+G and then Ctl+Right Arrow key
if (!evt.entity) return;

// set focus back on iframe (NOTE: helps when in caret mode)
var page = evt.entity.getPage();
var frame = page.getFrame();
ContentManager.focus(frame);
});
*/
