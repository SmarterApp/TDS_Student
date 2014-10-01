// a class used for wrapping an editor with spell check functionality
HTMLEditor.SpellCheck = function(editor)
{
    this._editor = editor; // YUI editor
    this._spellCheck = null; // spell check engine
    this._enabled = false; // in spell check mode?
    this.wordFocusID = null; // the last word ID that got focus
}

HTMLEditor.SpellCheck.register = function(item, editor)
{
    SpellCheckManager.setLanguage(ContentManager.getLanguage());

    var editorSpellCheck = new HTMLEditor.SpellCheck(editor);
    editorSpellCheck._init();

    // check for when iframe is ready
    editor.on('editorContentLoaded', function()
    {
        var editorDoc = editor._getDoc();
        
        YUE.onFocus(editorDoc, function(ev)
        {
            var target = YUE.getTarget(ev);
            
            // check if word
            if (target.id && YUD.hasClass(target, editorSpellCheck._spellCheck.wordClassName))
            {
                editorSpellCheck.wordFocusID = target.id;
            }
        });
    });

    return editorSpellCheck;
};

HTMLEditor.SpellCheck.prototype._init = function()
{
    var editor = this._editor;

    // when the toolbar loads
    editor.on('toolbarLoaded', function() 
    {

        // Add the spell check button
        var spellCheckButton = { type: 'push', label: 'Spell Check', value: 'spellcheck' };
        editor.toolbar.addButtonToGroup(spellCheckButton, 'textstyle');

        // Add the spell check language selector in case the language is Spanish
        if (ContentManager.getLanguage() == 'ESN') {
            var languagesMenu = [];            
            languagesMenu.push({ text: 'Spanish Dictionary', value: 'ESN', checked: SpellCheckManager.getLanguage() == 'ESN', isSelected: function() { return SpellCheckManager.getLanguage() == 'ESN'; }, onClick: function() { SpellCheckManager.setLanguage('ESN'); editor.toolbar.getButtonByValue('langselect').checkValue('ESN'); } });
            languagesMenu.push({ text: 'English Dictionary', value: 'ENU', checked: SpellCheckManager.getLanguage() == 'ENU', isSelected: function() { return SpellCheckManager.getLanguage() == 'ENU'; }, onClick: function() { SpellCheckManager.setLanguage('ENU'); editor.toolbar.getButtonByValue('langselect').checkValue('ENU'); } });

            var langSelectButton = { type: 'select', label: 'Language', value: 'langselect', menu: languagesMenu };
            editor.toolbar.addButtonToGroup(langSelectButton, 'textstyle');

            editor.toolbar.on('langselectClick', function(ev) {
                if (ev.button.value != null) {
                    SpellCheckManager.setLanguage(ev.button.value);
                }
                return false;
            });
        }

        // Add a seperator for this group
        editor.toolbar.addSeparator();


        // click on spell check button
        editor.toolbar.on('spellcheckClick', function()
        {
            if (this._enabled)
            {
                this.disable();
            }
            else
            {
                this.enable();
            }

            return false;

        }, this, true);

    }, this, true);

    editor.on('editorClick', function(args)
    {
        // make sure spell check is enabled
        if (!this._enabled) return;

        // stop event
        YUE.stopEvent(args.ev);

        // only listen for left click
        if (args.ev.button == 0)
        {
            var el = YUE.getTarget(args.ev);

            // check if what we clicked on was a misspelled word
            if (YUD.hasClass(el, this._spellCheck.wordClassName))
            {
                // get word and process
                var word = Util.Dom.getTextContent(el);
                this._clickedWord(args.ev, el, word);
            }
        }

        return false;

    }, this, true);

    // when the content loads
    editor.on('editorContentLoaded', function()
    {
        var win = editor._getWindow();
        var doc = editor._getDoc();

        var rootNode = doc.body;
        this._spellCheck = new SpellCheck(SpellCheckManager, rootNode);

    }, this, true);
}

// is spell check mode enabled
HTMLEditor.SpellCheck.prototype.isEnabled = function() { return this._enabled; };

// this is event handler for when spell check is requested
HTMLEditor.SpellCheck.prototype.enable = function()
{
    if (this._enabled) return;

    // make html read only (need to switch this off, on and off to fix FF 2.0 bug)
    this._editor._setDesignMode('off');
    this._editor._setDesignMode('on');
    this._editor._setDesignMode('off');

    // disable toolbar
    this._editor.toolbar.set('disabled', true);
    this._editor.toolbar.getButtonByValue('spellcheck').set('disabled', false);
    this._editor.toolbar.selectButton('spellcheck');

    // enable spell check
    this._spellCheck.check();

    this._enabled = true;
};

// this is event handler for when spell check is disabled
HTMLEditor.SpellCheck.prototype.disable = function()
{
    if (!this._enabled) return;

    this._enabled = false;

    // disable spell check
    this._spellCheck.done();

    // make html writable (need to switch this on, off and on to fix FF 2.0 bug)
    this._editor._setDesignMode('on');
    this._editor._setDesignMode('off');
    this._editor._setDesignMode('on');

    // re-enable toolbar
    this._editor.toolbar.set('disabled', false);

    // reset undo/redo
    this._editor._undoCache = [];
    this._editor._undoLevel = 0;

    // reset toolbar buttons state
    this._editor.nodeChange();
};

// this is event handler for when clicking on a word in the YUI editor
HTMLEditor.SpellCheck.prototype._clickedWord = function(ev, node, word)
{
    var suggestions = SpellCheckManager.getSuggestions(word);

    var menuItems = [];

    // check if there are any word suggestions
    if (suggestions.length > 0)
    {
        for (var i = 0; i < suggestions.length; i++)
        {
            var suggestion = suggestions[i];

            var obj = { node: node, word: word, replacement: suggestion };

            // create menu item
            menuItems.push(
            {
                text: suggestion,
                onclick: { fn: this._replaceWord, obj: obj, scope: this }
            });
        }
    }
    else
    {
        // create empty menu item
        menuItems.push(
        {
            text: 'No suggestions'
            // disabled: true // BUG #16817: ESC does not close Suggestion list menu if it has 'No Suggestion'
        });
    }

    // get element XY
    var menuXY = YUD.getXY(node);

    // include XY of iframe
    menuXY = ContentManager.getEventXY(ev, menuXY);

    // add height of word offset 
    var region = YAHOO.util.Region.getRegion(node);
    menuXY[1] += region.height;

    ContentManager.Menu.show(ev, menuItems, menuXY);
};

// this is event handler for when clicking on a replacement word in the context menu
HTMLEditor.SpellCheck.prototype._replaceWord = function(type, args, obj)
{
    this._spellCheck.replaceWord(obj.node, obj.word, obj.replacement);
};
