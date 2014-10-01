var Proofreading = { };

// equation editor
ContentManager.onItemEvent('available', function (page, item) {

    if (!item.isResponseType('Proofreading')) return;

    var editor = HTMLEditor.create(item);

    item.editor = editor;

    if (editor != null) editor.render();

    // NOTE: Disabled tab key because this is used for navigation now
    // var tabKey = new YAHOO.util.KeyListener(textArea, { keys: [9] }, { fn: tabFunc }, 'keypress');
    // tabKey.enable();

    // check for read-only
    var readOnlyFunc = function (evt) {
        if (item.isReadOnly()) {
            YUE.stopEvent(evt);
        }
    };

    //YUE.on(equationContainer, 'keypress', readOnlyFunc);
    //YUE.on(equationContainer, 'mousedown', readOnlyFunc);
});

ContentManager.onItemEvent('ready', function (page, item) {

    if (!item.isResponseType('Proofreading')) return;

    // get the editor by scanning DOM
    var editorDoc = page.getDoc().getElementById(item.editor.id + '_editor').contentDocument;

    // if student's response is null copy the stem content and fill in the editor area
    if (item.value == null) Proofreading._promptContentLoaded(item, editorDoc);

    var editor = item.editor;

    var group = { group: 'function', label: '',
        buttons: [
                    { type: 'separator' },
                    { label: 'Reset', value: 'reset', disabled: false },
                    { type: 'separator' }
                 ]
    };


    editor.toolbar.addButtonGroup(group);
    editor.toolbar.on('resetClick', function (o) {
        //var reset = confirm('Reset Editor Content?');
        //if (reset == true) 
        Proofreading._promptContentLoaded(item, editorDoc);
        //else return;
    }, editor, true);
});

Proofreading._promptContentLoaded = function (item, doc) {

    if (item.response) { doc.body.innerHTML = item.response; }
    else {
        var stemNode = item.getPage().getDoc().getElementById('Stem_' + item.position).children[0].innerHTML;
        doc.body.innerHTML = stemNode + "<br>";
    }
};

var editorResponseGetter = function (item, response) {
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

var editorResponseSetter = function (item, value) {
    // get editor
    var editor = item.editor;
    editor.setEditorHTML(value);
}

// RESPONSE HANDLER: HTML EDITOR
ContentManager.registerResponseHandler('htmleditor', editorResponseGetter, editorResponseSetter);