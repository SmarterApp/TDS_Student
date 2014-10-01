/*
This code is used for setting up the writing widget with ckeditor.
*/

(function(CKEDITOR) {

    var wordsAllowed = 850;

    // get the word count for some text
    function countHTMLWords(html) {

        // This method for counting words from an HTMl document comes from the CKEditor word count plugin
        var text = html.
            replace(/(\r\n|\n|\r)/gm, " "). // Replace end of line chars with spaces
            replace(/^\s+|\s+$/g, ""). // Remove whitespace from start/end of lines
            replace("&nbsp;", " "); // Replace &nbsp with spaces

        // Strip all HTML
        var tmp = document.createElement("div");
        tmp.innerHTML = text;

        text = tmp.textContent || tmp.innerText;

        var words = text.split(/\s+/); // Split on whitespace

        for (var wordIndex = words.length - 1; wordIndex >= 0; wordIndex--) {
            if (words[wordIndex].match(/^([\s\t\r\n]*)$/)) {
                words.splice(wordIndex, 1);
            }
        }

        return words.length;
    };
    
    // This builds an ckeditor instance for an items writing prompt.
    // NOTE: We don't use module_htmleditor2.js right now because 
    // there are some differences in how we set this up.
    function createEditor(writing, item) {
        
        var itemDiv = YUD.get('Item_' + item.position);
        var editorDiv = itemDiv.getElementsByClassName('writingBlock')[0];
        var editor = HTMLEditor.create(editorDiv, item.position, item.responseType);
        item.editor = editor;
        editor.parentItem = item;

        // check if existing response
        if (item.value) {
            // we use private variable because in editor.js (line 867) 
            // it will use it when loading but won't fire events
            editor._.data = item.value;
        }
                        
        // Chrome needs this...
        // TODO: I commented this out in code rewrite.. do we still need it?
        // editor.contentDom.focus();

        // #bug 104821 add item component, fix ctrl+tab  change focus
        item.addComponent(editor);

        // update the word count for a ckeditor instance
        function updateWordCount(warning) {

            var wordsCount = countHTMLWords(editor.getData());

            // update the main word count
            var wordCountString = (wordsCount == 0) ? '' : wordsCount + ' out of ' + wordsAllowed + ' words used.';
            writing.wordCountEl.innerHTML = wordCountString;

            // update the unit word count
            if (wordsCount > 0) {
                var promptWordCount = YUD.getElementsByClassName('inlineCount', 'span', YUD.get('Item_' + item.position))[0];
                promptWordCount.innerHTML = wordsCount + ' words';
            }

            // check if we should show an alert dialog
            if (warning && wordsCount > wordsAllowed) {
                var message = 'Your response cannot exceed ' + wordsAllowed + ' words.';
                TDS.Dialog.showAlert(message, function () {
                    ContentManager.focus(editor);
                });
            }
        };

        // listen for when a key is pressed so we can update word count
        editor.on('change', function() {
            updateWordCount(true);
        });

        editor.on('instanceReady', function () {
            updateWordCount(false);
        });

    }

    // sets up a writing item unit
    function processItem(writing, item) {
        
        // create writing prompt
        var unit = new WritingUnit(item.getElement());
        unit.item = item; // assign item to unit
        item.unit = unit; // assign unit to item
        writing.units.push(unit); // add to unit list
        
        // check if a unit is already selected (if selected on server side) 
        if (unit.isSelected()) {
            unit.select(); // we need to call select do the CSS class gets applied
            YUD.removeClass(writing.linkStart, 'inactive'); // set start writing mode to active
            item.setActive();
        }

        // add onlick for when clicking on prompt
        YUE.addListener(unit.linkSelect, 'click', function () {
            if (ContentManager.Menu.isShowing()) return;
            writing.selectUnit(unit);
        });

        // add onlick for expanding prompt
        YUE.addListener(unit.linkExpandPrompt, 'click', unit.toggleExpand, unit, true);

        item.isVisible = function () {
            return Util.Dom.isVisible(unit.divPrompt);
        };
        
        // Update on show...due to issues with CKE editor and Chrome, we need to
        // create the editor instance only after the parent div is visible.
        item.unit.onOpenEdit.subscribe(function () {
            if (!item.editor) {
                createEditor(writing, item);
            }
        });
    }

    // sets up the writing page widget
    function processPage(page) {
        
        var pageWin = page.getWin();

        // create writing widget
        var divWriting = page.getElement();
        var writing = new WritingWidget(divWriting, pageWin);
        page.writing = writing;

        // create word count bar and add it to the dom
        var wordCountDiv = divWriting.getElementsByClassName('writeNav2')[0];
        YUD.addClass(wordCountDiv, 'writeNav2');
        var parentDiv = divWriting.getElementsByClassName('bigTable')[0];
        parentDiv.appendChild(wordCountDiv);
        page.writing.wordCountEl = wordCountDiv;
        
        // process each item (prompt) on the page
        var items = page.getItems();
        Util.Array.each(items, function(item) {
            processItem(writing, item);
        });

        // hack for one prompt
        if (writing.units.length == 1) {
            writing.units[0].select();
            writing.enableEditing();
        }
        else {
            writing.toggleEdit();
        }
    }
    
    // check if page is using writing layout
    ContentManager.onPageEvent('available', function(page) {
        if (page.layout == '12') {
            processPage(page);
        }
    });

})(window.CKEDITOR);

// listen for key events
ContentManager.onItemEvent('keyevent', function(page, item, evt) {
    
    // make sure writing
    if (!item.unit) return;

    // make sure keydown event and no modifiers
    if (evt.type != 'keydown' || evt.ctrlKey || evt.altKey) return;

    // get writing objects
    var writing = page.writing;
    var unit = item.unit;

    // if we are on the prompt selection screen and someone hits enter then perform an action on the prompt
    if (evt.key == 'Enter' && !writing.isEditing())
    {
        if (unit.isSelected()) {
            writing.enableEditing();
        } else {
            writing.selectUnit(unit);
        }
    }
});

ContentManager.onItemEvent('menushow', function(page, item, menu) {
    
    // make sure writing
    if (!item.unit) return;
    
    // check if stem
    if (item.getActiveComponent() != item.getStemElement()) return;

    // get writing objects
    var writing = page.writing;

    // add change prompt if editing 
    if (writing.isEditing()) {
        menu._entity.push({
            text: 'Change Prompt',
            classname: 'changePrompt',
            onclick: {
                fn: writing.toggleEdit,
                scope: writing
            }
        });
    }
});

// RESPONSE HANDLER: WRITING PROMPTS
(function()
{
    var getter = function(item, response) { // editorResponseGetter
        if (!item.editor) {
            response.isSelected = false;
            return;
        }
        response.value = item.editor.getData();
        response.isValid = (response.value.length > 0);
        response.isSelected = item.unit.isSelected();
    };

    var setter = function(item, value) { // editorResponseSetter
        item.editor.setData(value);
    };

    ContentManager.registerResponseHandler('prompt selection', getter, setter);
})();

