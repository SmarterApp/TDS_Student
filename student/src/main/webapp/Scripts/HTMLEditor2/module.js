/*
This the HTML editor module. This should have 
all code dealing with hooking up to blackbox and ContentManager.
*/

(function (HTMLEditor, CM) {

    HTMLEditor.resolveBaseUrl = CM.resolveBaseUrl.bind(CM);
    HTMLEditor.getLanguage = CM.getLanguage.bind(CM);

    // create ckeditor in the response container
    function process(page, item) {

        // get the editor container and clear it out
        var containerEl = page.getDoc().getElementById('editor_' + item.position);
        var editor = HTMLEditor.create(containerEl, item.responseType);
        item.editor = editor;
        editor.parentItem = item;

        // check if existing response
        if (item.value) {
            // we use private variable because in editor.js (line 867) 
            // it will use it when loading but won't fire events
            editor._.data = item.value;
        }
    }
   
    // listen for when an htmleditor item is available 
    CM.onItemEvent('available', function (page, item) {
        // if not writing then check if item uses ckeditor
        if (page.layout != '12' && (
            item.isResponseType('HTMLEditor') ||
            item.isResponseType('HTMLEditorTable') ||
            item.isResponseType('PlainTextSpell') ||
            item.isResponseType('Prompt Selection'))) {
                process(page, item);
        }
    });

    // when an instance of CKEditor is ready apply fixes
    CKEDITOR.on('instanceReady', function (ev) {

        var editor = ev.editor;
        var item = editor.parentItem;
        if (!editor || !item) return;
        
        // add a entity component compatible blur function 
        editor.blur = function () {
            var editable = editor.editable();
            if (editable) {
                editable.$.blur();
            }
        };

        // check when editor is focused
        editor.on('focus', function (ev) {
            item.setActiveComponent(editor);
        });

        // add item component
        item.addComponent(editor);

        // if the editor has the same document object as
        // main document then don't apply iframe fixes
        var doc = editor.document.$;
        var win = editor.document.getWindow().$;
        if (doc == document) return;

        // add menu fixes to the editors iframe
        ContentManager.Menu.applyDocFix(win);

        // add content manager events into editor
        ContentManager.addMouseEvents(item, doc);
        ContentManager.addKeyEvents(doc);

        // check if this is mobile device
        if (Util.Browser.isTouchDevice()) {
            ContentManager.listenForFocus(doc);
        }

        var page = item.getPage();

        // add accommodations to iframe 
        // TODO: add this code for event 'dataReady' when more time to test
        if (doc.body) {
            var pageAccommodations = page.getAccommodations();
            pageAccommodations.applyCSS(doc.body);
        }

        // add zoom
        var zoom = page.getZoom();
        zoom.addDocument(doc);
        zoom.refresh();
    });

    // prevent the content manager menu from showing and use ckeditor's
    CM.onEntityEvent('menushow', function (page, entity, contentMenu, evt) {

        if (!entity.editor) return;
        var editor = entity.editor;
            
        // check if editor has focus
        if (editor.focusManager &&
            editor.focusManager.hasFocus) {
            contentMenu.cancel = true; // cancel showing menu
        } else if (editor.element) {
            var editorEl = editor.element.$;
            var menuTargetEl = YUE.getTarget(evt);
            // was context menu right clicked on editor?
            if (editorEl == menuTargetEl || 
                YUD.isAncestor(editorEl, menuTargetEl)) { 
                contentMenu.cancel = true; // cancel showing menu
            }
        }

    });

    // when hiding turn off spellcheck
    CM.onItemEvent('hide', function (page, item) {

        var editor = item.editor;

        // check if html editor has spell check enabled
        if (editor &&
            editor.commands &&
            editor.commands.spellchecker &&
            editor.commands.spellchecker.enabled) {
            editor.commands.spellchecker.exec();
        }
    });

})(HTMLEditor, ContentManager);

// set response handlers
(function(CM) {

    function getter(item, response) {
        if (item.editor) {
            response.isReady = item.editor.isReady;
            response.value = item.editor.getData();
            response.isValid = (response.value.length > 0);
            response.isSelected = response.isValid;
        } else {
            response.isReady = false;
        }
    };

    function plainTextGetter(item, response) {
        if (item.editor) {
            var value = item.editor.element.$;
            response.isReady = item.editor.isReady;
            response.value = Util.Dom.getTextContent(value);
            response.isValid = (response.value.length > 0);
            response.isSelected = response.isValid;
        } else {
            response.isReady = false;
        }
    };

    function setter(item, value) {
        if (item.editor) {
            item.editor.setData(value);
        }
    };

    // RESPONSE HANDLER: HTML EDITOR
    CM.registerResponseHandler('htmleditor', getter, setter);

    // RESPONSE HANDLER: PlainTextSpell
    CM.registerResponseHandler('PlainTextSpell', plainTextGetter, setter);
    
    // RESPONSE HANDLER: HTMLEditorTable
    CM.registerResponseHandler('HTMLEditorTable', getter, setter);

})(ContentManager);

