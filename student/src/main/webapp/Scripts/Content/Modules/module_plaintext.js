// set focus to text area
ContentManager.onItemEvent('focus', function(page, item)
{
    if (!item.isResponseType('plaintext')) return;

    var doc = page.getDoc();

    // get editor
    var textArea = doc.getElementById('Item_Response_' + item.position);
    
    // if this is not a touch device then set focus on text area
    if (!Util.Browser.isTouchDevice()) {
        ContentManager.focus(textArea);
    }
});

// remove focus from text area
ContentManager.onItemEvent('blur', function(page, item)
{
    if (!item.isResponseType('plaintext')) return;

    var doc = page.getDoc();

    // get editor
    var textArea = doc.getElementById('Item_Response_' + item.position);
    textArea.blur();
});

// plaintext
ContentManager.onItemEvent('available', function(page, item)
{
    if (!item.isResponseType('plaintext')) return;

    var doc = page.getDoc();

    // get editor
    var textArea = doc.getElementById('Item_Response_' + item.position);
    
    // Bug 87179: allow user to tab to text box.
    if ((textArea) && (ContentManager.isAccessibilityEnabled())) {
        textArea.setAttribute('tabindex', '0');
    }

    var tabFunc = function(type, args)
    {
        var event = args[1];

        var oS = textArea.scrollTop;
        var sS = textArea.selectionStart;
        var sE = textArea.selectionEnd;

        textArea.value = textArea.value.substring(0, sS) + '\t' + textArea.value.substr(sE);
        textArea.setSelectionRange(sS + 1, sS + 1);
        ContentManager.focus(textArea);
        textArea.scrollTop = oS;

        YUE.stopEvent(event);
    };

    // check for read-only
    ContentManager.setReadOnlyKeyEvent(item, textArea);
});

// RESPONSE HANDLER: PLAIN TEXT
(function()
{
    var getter = function(item, response)
    {
        // get textarea
        var pageDoc = item.getPage().getDoc();
        var textArea = pageDoc.getElementById('Item_Response_' + item.position);

        response.value = textArea.value;
        response.isValid = (response.value.length > 0);
        response.isSelected = response.isValid;

        if (response.value)
        {
            // escape closing tag for CDATA (bug #15742)
            response.value = response.value.replace(/]]>/g, ']]&gt;');
        }
    };

    var setter = function(item, value)
    {
        // get textarea
        var pageDoc = item.getPage().getDoc();
        var textArea = pageDoc.getElementById('Item_Response_' + item.position);

        textArea.value = value;
    };

    ContentManager.registerResponseHandler('plaintext', getter, setter);

})();

