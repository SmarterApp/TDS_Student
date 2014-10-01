/*********************************************
 * CONTENT MANAGER EVENTS 
 *********************************************/

// add all the selected accommodations to the page body
ContentManager.onPageEvent('beforeShow', function(page)
{
    var pageAccs = page.getAccommodations();

    if (pageAccs != null) {
        Accommodations.Manager.updateCSS(page.getBody(), pageAccs.getId());
    }
});

// HACK: If there is no real passage but someone selected multi item layout with a passage section
// in it we will create a fake passage object for the items illustration if it has one.
ContentManager.onPageEvent('available', function(page)
{
    if (page.getPassage() != null) return;

    // check if this layout has a passage section
    var pageElement = page.getElement();
    var passageElement = Util.Dom.getElementByClassName('thePassage', 'div', pageElement);

    // check if there is a passage element and assume it is the illustration
    if (passageElement == null) return;

    var firstItem = page.getItems()[0];

    // HACK: create fake passage
    var passage = ContentManager._createPassage(page, { bankKey: firstItem.bankKey, itemKey: 0 });
    page.setPassage(passage);
});

// add focusable passage components
ContentManager.onPassageEvent('available', function(page, passage)
{
    var passageElement = passage.getElement();
    if (passageElement != null) passage.addComponent(passageElement);
});

// add expandable passage
ContentManager.onPassageEvent('available', function(page, passage)
{
    // check if enabled
    var accProps = page.getAccommodationProperties();
    if (!accProps || !accProps.showExpandablePassages()) return;

    var css_collapsed = 'passage-collapsed'; // passage is normal size
    var css_expanded = 'passage-expanded'; // passage has filled up screen

    // get the element to set collapse
    var expandEl = ContentManager.Renderer.isDirect() ? 
        page.getElement() : // <-- only modern shell supports this
        page.getBody(); // <-- this is for classic shell

    // start off as collapsed
    YUD.addClass(expandEl, css_collapsed);

    // create new expand/collapse link
    var expandLink = page.getDoc().createElement('a');
    YUD.setAttribute(expandLink, 'href', '#');
    YUD.addClass(expandLink, 'expand-collapse-passage');

    // add event handler to toggle classes
    YUE.on(expandLink, 'click', function(clickEv)
    {
        // stop dom event
        YUE.stopEvent(clickEv);
        
        // check for class
        if (YUD.hasClass(expandEl, css_expanded))
        {
            // make passage collapsed
            YUD.removeClass(expandEl, css_expanded);
            YUD.addClass(expandEl, css_collapsed);
        }
        else if (YUD.hasClass(expandEl, css_collapsed))
        {
            // make passage expanded
            YUD.removeClass(expandEl, css_collapsed);
            YUD.addClass(expandEl, css_expanded);
        }
    });

    /*
    // add link to passage
    var passageEl = passage.getElement();
    if (passageEl == null) return; // no passage element

    // try and find passage title
    var passageHeaders = passageEl.getElementsByTagName("h2");
    
    // add expand link before the passage title
    if (passageHeaders && passageHeaders.length > 0)
    {
        var passageHeader = passageHeaders[0];
        YUD.insertBefore(expandLink, passageHeader);
        return;
    }
    */
    
    // get passage element
    var passageEl = passage.getElement();
    if (passageEl == null) return; // no passage element

    // add expand link as the first child of passage padding
    var paddingEl = Util.Dom.getElementByClassName('padding', 'div', passageEl);
    
    if (paddingEl)
    {
        var paddingChildEl = YUD.getFirstChild(paddingEl);
        if (paddingChildEl) YUD.insertBefore(expandLink, paddingChildEl);
        else paddingEl.appendChild(expandLink);
    }
});

// add focusable item components
ContentManager.onItemEvent('available', function(page, item)
{
    var layoutElements = [];

    // add stem element
    var stemElement = item.getStemElement();
    layoutElements.push(stemElement);

    // get illustration element
    var illustrationElement = item.getIllustrationElement();

    // figure out if we should add the illustration as a component
    if (illustrationElement)
    {
        YUD.addClass(illustrationElement, 'illustrationContainer');
        
        // illustration is part of layout/OO and can be empty so make sure it has content
        var illustrationText = Util.Dom.getTextContent(illustrationElement);
        illustrationText = YAHOO.lang.trim(illustrationText);

        // add illustration element if it has text (illustration.innerHTML != '<p>&nbsp;</p>')
        if (illustrationText.length > 0)
        {
            layoutElements.push(illustrationElement);
        }
    }

    // sort nodes by order in DOM
    Util.Array.sort(layoutElements, Util.Dom.compareNodeOrder);
    
    // add all the layout elements
    Util.Array.each(layoutElements, function(layoutElement)
    {
        item.addComponent(layoutElement);
    });
});

ContentManager.onEntityEvent('focus', function(page, entity)
{
    // when focusing on an item reset the component to be the first one 
    entity.resetComponent();
});

ContentManager.onEntityEvent('blur', function(page, entity)
{
    entity.clearComponent();

    // BUG #63088 fix: Clear any selected text
    page.collapseSelection();
});

ContentManager.onComponentEvent('focus', function(page, entity, component)
{
    if (ContentManager.isElement(component))
    {
        YUD.addClass(component, 'contextAreaFocus');
    }
});

ContentManager.onComponentEvent('blur', function(page, entity, component)
{
    // NOTE: You might see a double blur when selecting something other than stem. This
    // is because when you first focus on an entity the first component gets focused.
    // Then your click is registered and the stem gets blurred and whatever other component
    // you selected (e.x., MC option) will get focus. 
    if (ContentManager.isElement(component))
    {
        YUD.removeClass(component, 'contextAreaFocus');
    }

    ContentManager.enableCaretMode(false);
});

// force the iframe to repaint/redraw it's contents (example css: forceRedraw{padding-bottom:1px;})
ContentManager.onPageEvent('show', function(page)
{
    var pageContainer = page.getContainer();
    if (pageContainer == null || pageContainer.nodeName != 'IFRAME') return;

    var pageBody = page.getBody();
    if (pageBody == null) return;

    YUD.addClass(pageBody, 'forceRedraw');

    setTimeout(function()
    {
        YUD.removeClass(pageBody, 'forceRedraw');
    }, 1);
});

// When an item is available subscribe to its mouse events.
// NOTE: This was moved from content_entity.js. 
// TODO: Should this be in an event? Seems important enough to be in main code.
ContentManager.onEntityEvent('available', function(page, item)
{
    var element = item.getElement();
    ContentManager.addMouseEvents(item, element);
});

// check if item is unsupported
ContentManager.onItemEvent('unsupported', function(page, item) {
    var itemEl = item.getElement();
    YUD.addClass(itemEl, 'unsupported');
});

// check for elements that we want TTS to skip
ContentManager.onItemEvent('available', function (page, item) {
    
    var itemEl = item.getElement();
    if (itemEl) {
        // check for item tools container
        var markCommentEl = Util.Dom.getElementByClassName('markComment', 'span', itemEl);
        if (markCommentEl) {
            markCommentEl.setAttribute('data-tts-skip', 'true');
        }
        // check for item position header
        var posEl = Util.Dom.queryTag('h2', itemEl);
        if (posEl && Util.Dom.getTextContent(posEl) == item.position) {
            posEl.setAttribute('data-tts-skip', 'true');
        }
    }

    var pageDoc = page.getDoc();
    if (pageDoc) {
        // check for comment box
        var commentBoxEl = pageDoc.getElementById('Item_CommentBox_' + item.position);
        if (commentBoxEl) {
            commentBoxEl.setAttribute('data-tts-skip', 'true');
        }
    }

});