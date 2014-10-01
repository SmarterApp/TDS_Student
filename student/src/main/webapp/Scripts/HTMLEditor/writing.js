/*
This contains code that binds HTML editor to the writing widget. 
It is primarily used for the word count functionality. 
*/

HTMLEditor.Writing = {};

HTMLEditor.Writing.register = function(item, editor)
{
    // check if this items prompt is showing
    var isEditorShowing = function()
    {
        var items = item.getPage().getItems();

        for (var i = 0; i < items.length; i++)
        {
            var pageItem = items[i];
            if (pageItem.unit && pageItem.unit.isSelected() && pageItem.editor == editor) return true;
        }

        return false;
    };

    var pageElement = item.getPage().getBody();
    var itemElement = item.getElement();

    var wordsAllowed = 850;
    var globalWordCount = YUD.getElementsByClassName('writeCounter', 'span', pageElement)[0];
    var promptWordCount = YUD.getElementsByClassName('inlineCount', 'span', itemElement)[0];

    // check if this key event can add to the word count
    var canKeyAddWordCount = function(event)
    {
        if (event.ctrlKey || event.altKey || event.metaKey) return false;
        if ((event.keyCode >= 33 && event.keyCode <= 40) || event.keyCode == 8 || event.keyCode == 46) return false;
        return true;
    };

    var updateWordCount = function(warning)
    {
        var wordsCount = editor.countWords();

        // update prompts word count
        promptWordCount.innerHTML = (wordsCount == 0) ? '' : wordsCount + ' words';

        // check if this editor is showing and update global word count
        if (isEditorShowing())
        {
            globalWordCount.innerHTML = wordsCount + ' out of ' + wordsAllowed + ' words used.';
        }

        // check if we should show an alert dialog
        if (warning && wordsCount > wordsAllowed)
        {
            var message = 'Your response cannot exceed ' + wordsAllowed + ' words.';

            if (typeof (showAlert) == 'function')
            {
                showAlert('Writing', message, function()
                {
                    ContentManager.focus(editor);
                });
            }
        }

    };

    // content load event
    editor.on('editorContentLoaded', function()
    {
        updateWordCount();

        if (isEditorShowing())
        {
            // NOTE: We shouldn't need this anymore since component selection is what gives this focus now
            // editor.show();
        }

        // when the prompt is opened update word count
        item.unit.onOpenEdit.subscribe(function()
        {
            updateWordCount();
        });

        // when the prompt is closed
        item.unit.onCloseEdit.subscribe(function()
        {
            item.resetComponent();

            if (item.spellCheck)
            {
                item.spellCheck.disable();
            }
        });
    });

    var keyPressInvalid = false;

    editor.on('editorKeyPress', function(data)
    {
        var event = data.ev, target = data.target;
        var wordsCount = editor.countWords();

        // prevent the keypress if we are over the count and this is not a destructive key press
        if (canKeyAddWordCount(event) && wordsCount > wordsAllowed)
        {
            YUE.stopEvent(event);
            keyPressInvalid = true;
        }
        else
        {
            keyPressInvalid = false;
        }
    });

    editor.on('editorKeyUp', function(data)
    {
        var event = data.ev, target = data.target;
        updateWordCount(canKeyAddWordCount(event));
    });

};
