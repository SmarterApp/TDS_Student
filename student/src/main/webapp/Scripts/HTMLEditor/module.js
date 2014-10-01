/*
This the HTML editor module.
*/

ContentManager.onItemEvent('available', function(page, item)
{
    var editor = HTMLEditor.create(item);
    if (editor != null) editor.render();
});

ContentManager.onItemEvent('show', function(page, item)
{
    var editor = item.editor;
    // if (editor && editor.contentLoaded) editor.show();
});

// adds html editor menu options
ContentManager.onItemEvent('menushow', function(page, item, menu, evt)
{
    // check if there is an html editor
    if (!item.editor) return;

    // if the editor doesn't have focus then don't show editor contextmenu items
    if (!item.editor._focused)
    {
        // prevent showing goto editor menu with on writing prompt selection screen
        if (page.writing && !page.writing.isEditing()) return;

        /*
        menu.addMenuItem('entity', 'Go To Text Editor', function()
        {
            setTimeout(function() { ContentManager.focus(item.editor); }, 0);
        });
        */

        return;
    }

    var toolbar = item.editor.toolbar;
    var buttons = toolbar.getButtons();

    for (var i = 0; i < buttons.length; i++)
    {
        // need to scope the function so each for() loop is scoped properly
        (function()
        {
            var button = buttons[i];

            // get button label to use for menu
            var text = button.get('label');

            // is button selected
            var isSelected = toolbar.isSelected(button.get('value'));

            // make sure button isn't disabled
            var isDisabled = button.get('disabled');

            // menu click
            var onMenuItemClick = function()
            {
                // manually fire event as if user clicked on button
                button.__yui_events.mousedown.fire(evt);
                button.__yui_events.click.fire(evt);
                button.__yui_events.mouseup.fire(evt);
            };

            if (button.get('type') != 'menu') 
            {
                menu.addMenuItem('component', text, onMenuItemClick, isDisabled, isSelected);
            } 
            else 
	        {
                var menuItems = button.getMenu().itemData;

                for (var j = 0; j < menuItems.length; j++) 
		        {
                    menu.addMenuItem('component', menuItems[j].text, menuItems[j].onClick, isDisabled, menuItems[j].isSelected());
                }
            }

        })();
    }
});

// adds any audio links to the context menu
ContentManager.onItemEvent('menuhide', function(page, item, menu, evt)
{
    // check if there is an html editor
    if (!item.editor) return;

    // check if editor is current component
    var activeComponent = item.getActiveComponent();

    if (activeComponent instanceof YAHOO.widget.Editor)
    {
        // focus back on editor when menu closes
        Util.Dom.focus(activeComponent);
    }
});

var editorResponseGetter = function(item, response)
{
    // get editor
    var editor = item.editor;

    var editorBody = editor._getDoc().body;

    // get the text representation of the html and see if anything exists
    response.isValid = (editorBody.textContent.length > 0)
    response.isSelected = response.isValid;

    // save editors content to the textarea
    editor.saveHTML();
    response.value = response.isValid ? editor.get('textarea').value : '';
};

var editorResponseSetter = function(item, value)
{
    // get editor
    var editor = item.editor;
    editor.setEditorHTML(value);
};

// RESPONSE HANDLER: HTML EDITOR
ContentManager.registerResponseHandler('htmleditor', editorResponseGetter, editorResponseSetter);

/*************************************************************************************************************/

/*
This the HTML special characters module.
*/

ContentManager.onItemEvent('keyevent', function(page, item, evt)
{
    if (evt.type != 'keydown') return;
    if (Util.Event.hasModifier(evt)) return; // no modifiers
    
    if (HTMLEditor.SpecialCharacters2.isActiveComponent(item))
    {
        HTMLEditor.SpecialCharacters2._keyDown(evt, item.editor.specialCharacters);
    }
});

// itemComponentFocus
ContentManager.onComponentEvent('focus', function(page, entity, component)
{
    // make sure current component is special characters
    if (!HTMLEditor.SpecialCharacters2.isActiveComponent(entity)) return;

    var editor = entity.editor;

    setTimeout(function()
    {
        if (editor.specialCharacters.focused)
        {
            Util.Dom.focus(editor.specialCharacters.focused);
        }
        else
        {
            Util.Dom.focus(editor.specialCharacters._activeCategory.link);
        }
    }, 0);
});

/*************************************************************************************************************/

/*
This the HTML spell check module.
*/

ContentManager.onItemEvent('keyevent', function(page, item, evt)
{
    if (evt.type != 'keydown') return;
    if (Util.Event.hasModifier(evt)) return; // no modifiers

    // check if we are currently in editor component
    if (!HTMLEditor.isActiveComponent(item)) return;

    // check if spell check is enabled
    if (!item.spellCheck || !item.spellCheck.isEnabled()) return;

    // check if left/right
    if (evt.keyCode != 37 && evt.keyCode != 39) return;

    var wordElements = item.spellCheck._spellCheck.getWordElements();
    var wordIterator = Util.Iterator(wordElements);

    if (item.spellCheck.wordFocusID)
    {
        var editorDoc = item.editor._getDoc();
        var wordElement = editorDoc.getElementById(item.spellCheck.wordFocusID);
        wordIterator.jumpTo(wordElement);
    }

    if (evt.keyCode == 37) wordIterator.prev(); // left
    else if (evt.keyCode == 39) wordIterator.next(); // right

    // select default of nothing selected
    if (!wordIterator.valid()) wordIterator.reset();

    // focus on word element
    wordElement = wordIterator.current();
    Util.Dom.focus(wordElement);
});
