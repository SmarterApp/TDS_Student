(function(YUIEditor) {

    var HTMLEditor = {};

    var rootPElement = '<p><br></p>';

    // get the data structure for creating the YUI toolbar
    function getToolbar() {

        // Toolbar
        var editorToolbar = {
            buttons:
            [
                {
                    group: 'textstyle',
                    label: '',
                    buttons: [
                        { type: 'push', label: 'Bold', value: 'bold' },
                        { type: 'push', label: 'Italic', value: 'italic' },
                        { type: 'push', label: 'Underline', value: 'underline' },
                        { type: 'separator' },
                        { type: 'push', label: 'Indent', value: 'indent', disabled: true },
                        { type: 'push', label: 'Outdent', value: 'outdent', disabled: true },
                        { type: 'separator' }
                    ]
                }
            ]
        };

        function addSeparator() {
            editorToolbar.buttons[0].buttons.push({
                type: 'separator'
            });
        };

        function addButton(label, value, disabled) {
            editorToolbar.buttons[0].buttons.push({
                type: 'push',
                label: label,
                value: value,
                disabled: disabled
            });
        };

        // add undo/redo
        addButton('Undo', 'undo', true);
        addButton('Redo', 'redo', true);

        addSeparator();

        //add copy-cut-paste editing buttons
        addButton('Copy', 'copy', false);
        addButton('Cut', 'cut', false);
        addButton('Paste', 'paste', false);

        addSeparator();

        return editorToolbar;
    };

    // Does this browser require the Mac OS X secure browser selection hack
    function requiresSelectionFix() {
        // check if mac and SB is less than 4.0
        return (Util.Browser.isMac() && Util.Browser.getSecureVersion() < 4.0);
    };

    // this gets called when someone clicks on the editor
    function onMouseDown(item, editor) {

        // set editor component as active
        item.setActive();
        item.setActiveComponent(editor);

        // because 'contextmenu' is disabled you always need to set focus to the editor on any mouse click
        ContentManager.focus(editor);
    };

    // this gets called when the YUI editor frame is ready
    function onContentLoaded(item, editor) {

        var win = editor._getWindow();
        var doc = editor._getDoc();

        // set editors intial response value
        if (item.value) {
            editor.setEditorHTML(item.value);
        } else {
            editor.setEditorHTML(rootPElement);
        }

        editor.contentLoaded = true; // previous response content loading has occured

        // disable browsers built in spell checker
        YUD.setAttribute(doc.body, 'spellcheck', false);

        // add menu fixes to the editors iframe
        ContentManager.Menu.applyDocFix(win);

        // add fixes for focus/blur (in fx2 these events need to be assigned to the document, but the editor just assigns them to the window)
        YUE.on(doc, 'focus', editor._handleFocus, editor, true);
        YUE.on(doc, 'blur', editor._handleBlur, editor, true);

        // add content manager events into editor
        ContentManager.addMouseEvents(item, doc);
        ContentManager.addKeyEvents(doc);

        // stop right click on regular browsers
        Util.Dom.stopAllEvents(doc, 'contextmenu');

        // check for when someone clicks on editor
        editor.on('editorMouseDown', function() {
            onMouseDown(item, editor);
        });

        // BUG #12516: Mac OS X secure browser selection hack
        if (requiresSelectionFix()) {
            YUE.addListener(doc, 'mousedown', function(e) {
                ContentManager.focus(top);
            });

            YUE.addListener(doc, 'mouseup', function(e) {
                ContentManager.focus(win);
            });
        }

        var page = item.getPage();

        // add accommodations
        var pageAccommodations = page.getAccommodations();
        pageAccommodations.applyCSS(doc.body);

        // add zoom
        var zoom = page.getZoom();
        zoom.addDocument(doc);
        zoom.refresh();

        // tell everyone the content is ready to go
        setTimeout(function() {
            ContentManager.fireEntityEvent('ready', item);
        }, 0);
    };

    // create an YUI editor from an item
    function create(item) {

        var itemElement = item.getElement();

        // check if this has a html editor
        var textarea;
        if (item.isResponseType('htmleditor')) {
            textarea = itemElement.getElementsByTagName('textarea')[0];
        } else if (item.isResponseType('prompt selection')) {
            textarea = itemElement.getElementsByTagName('textarea')[1];
        } else {
            return null; // no editor
        }

        var editorCSS = [];
        editorCSS.push('@import url(' + ContentManager.resolveBaseUrl('Shared/CSS/accommodations.css') + ');');
        editorCSS.push('@import url(' + ContentManager.resolveBaseUrl('Scripts/HTMLEditor/base.css') + ');');

        // BUG #12516: Mac OS X secure browser selection hack
        if (requiresSelectionFix()) {
            // this css is required for the browser to never give the frame focus, we need to manually do it
            editorCSS.push('* { -moz-user-focus: ignore !important; }');
        }

        var editorToolbar = getToolbar();

        var config = {
            dompath: false,
            focusAtStart: false,
            animate: false,
            buttonType: 'basic',
            extracss: editorCSS.join(' '),
            toolbar: editorToolbar,
            ptags: true
        };

        // create editor
        var editor = new YUIEditor(textarea, config);

        // set doctype
        editor._docType = '<!DOCTYPE html>';

        // keep ref to editor
        item.editor = editor;

        // BUG #12562: remove all editor shortcuts
        for (var name in editor._keyMap) {
            if (YAHOO.lang.hasOwnProperty(editor._keyMap, name)) {
                editor._keyMap[name].key = -999;
            }
        }

        // check for when iframe is ready
        editor.on('editorContentLoaded', function() {
            onContentLoaded(item, editor);
        });

        // stop tab key when ctrl is held down
        function stopTab(obj) {
            var event = obj.ev;
            if (event.ctrlKey && event.keyCode == 9) {
                return false;
            }
        };

        editor.on('beforeEditorKeyDown', stopTab);

        /* REGISTER EDITOR EXTENSIONS HERE */

        // spell check
        item.spellCheck = HTMLEditor.SpellCheck.register(item, editor);

        // special characters
        HTMLEditor.SpecialCharacters2.register(item, editor);

        // writing
        if (item.getPage().layout == '12') {
            HTMLEditor.Writing.register(item, editor);
        }

        // copy-cut-paste tools.
        HTMLEditor.ClipBoardTools.register(item, editor);

        // add editor component
        editor.id = editor.get('id');

        editor.isVisible = function() {
            var editorContainer = editor.get('element_cont').get('element');
            return Util.Dom.isVisible(editorContainer);
        };

        editor.blur = function() {
            // BUG #31730 & #34121: focus stays on editor
            setTimeout(function() {
                // if when bluring away the focus is still on the editor then focus on something else
                if (editor._focused && item.unit) {
                    var focusableElement = item.unit.linkExpandPrompt; // get some random element
                    Util.Dom.focus(focusableElement); // adding focus here forces focus away from YUI editor
                    Util.Dom.blur(focusableElement); // remove focus so you can't hit enter on it
                }
            }, 0);
        };

        item.addComponent(editor);

        return editor;
    };

    function isActiveComponent(item) {
        if (!item.editor) {
            return false;
        } // no editor
        var currentComponent = item.getActiveComponent();
        return (currentComponent && currentComponent instanceof YUIEditor);
    };

    /*************************************************************************************************************/

    // get the editors plain text
    YUIEditor.prototype.getText = function() {
        var editorBody = this._getDoc().body;
        var html = editorBody.innerHTML;
        var stripHTML = /<\S[^><]*>/g;
        return html.replace(/&nbsp;/gi, ' ').replace(/<br>/gi, '\n').replace(stripHTML, '');
    };

    // counts the # of words in the text passed in
    YUIEditor.prototype.countWords = function() {

        var text = this.getText();
        var fullStr = text + ' ';
        var initial_whitespace_rExp = /^[^A-Za-z0-9]+/gi;
        var left_trimmedStr = fullStr.replace(initial_whitespace_rExp, '');

        // filter out anything that is not: A-Z, a-z, 0-9, ', `, -
        var non_alphanumerics_rExp = /[^A-Za-z0-9\'-`]+/gi;

        var cleanedStr = left_trimmedStr.replace(non_alphanumerics_rExp, ' ');
        var splitString = cleanedStr.split(' ');
        var word_count = splitString.length - 1;

        if (fullStr.length < 2) {
            word_count = 0;
        }

        return word_count;
    };

    // public API
    window.HTMLEditor = HTMLEditor;
    HTMLEditor.create = create;
    HTMLEditor.isActiveComponent = isActiveComponent;

})(YAHOO.widget.Editor);