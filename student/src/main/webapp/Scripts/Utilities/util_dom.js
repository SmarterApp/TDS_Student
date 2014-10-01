// REQUIRES: NONE

Util.Dom =
{
    // get the window for a document
    getWindow: function(doc) {
        if (doc) {
            return (doc.parentWindow || doc.defaultView);
        }
        return null;
    },

    // Cross-browser function for getting the document element of a frame or iframe.
    getFrameContentDocument: function(frame) {
        if (frame) {
            if (YAHOO.env.ua.webkit) {
                return (frame.document || frame.contentWindow.document);
            } else {
                return (frame.contentDocument || frame.contentWindow.document);
            }
        }
        return null;
    },

    // Cross-browser function for getting the window of a frame or iframe.
    getFrameContentWindow: function(frame) {
        if (frame) {
            return (frame.contentWindow || this.getWindow(this.getFrameContentDocument(frame)));
        }
        return null;
    },

    // copy the document body css to frame body
    copyCSSFrame: function(frameID)
    {
        var frame = YUD.get(frameID);
        if (frame == null || frame.src == '') return false; // ignore empty frame
        if (frame.contentDocument == null || frame.contentDocument.body == null) return false; // ignore empty content
        frame.contentDocument.body.className = document.body.className; // update CSS
        return true;
    },

    // check if this element is current showing (this does not take into account if it is visible on the screen due to scroll though)
    isVisible: function(node)
    {
        // get node
        node = YUD.get(node);

        // check if valid object
        if (!node) return false;

        /*
        try
        {
        var displayed = (YUD.getStyle(node, 'display') != 'none');
        } 
        catch (ex)
        {
        Util.log('error getting display style');
        }
        */

        // keep looping through parent nodes to make sure none are hidden until we reach the body
        while (node && (node.nodeName.toLowerCase() != 'body') && (YUD.getStyle(node, 'display') != 'none') && (YUD.getStyle(node, 'visibility') != 'hidden'))
        {
            node = node.parentNode;
        }

        // if we made it to the body then everything was visible
        if (node && node.nodeName.toLowerCase() == 'body') return true;
        else return false;
    },

    // get all focusable elements in an element
    getFocusableElements: function(root)
    {
        root = root || this.innerElement;

        var focusableTags = [
            "a",
            "button",
            "select",
            "textarea",
            "input",
            "iframe"
        ];

        var focusable = {};

        for (var i = 0; i < focusableTags.length; i++)
        {
            focusable[focusableTags[i]] = true;
        }

        function isFocusable(el)
        {
            if (el.focus && el.type !== "hidden" && !el.disabled && focusable[el.tagName.toLowerCase()])
            {
                return true;
            }

            return false;
        }

        // Not looking by Tag, since we want elements in DOM order
        return YUD.getElementsBy(isFocusable, null, root);
    },

    // returns true if element is a form element
    isFormElement: function(el)
    {
        if (el.nodeType == 3 /*TEXT_NODE*/)
        {
            el = el.parentNode;
        }

        return " button textarea input select option ".indexOf(" " + el.tagName.toLowerCase() + " ") >= 0;
    },

    // Stops all events for this element.
    stopAllEvents: function(el, type)
    {
        YUE.on(el, type, YUE.stopEvent, YUE, true);
    },

    // stop drag and drop (https://developer.mozilla.org/en/Drag_and_Drop)
    stopDragEvents: function(el)
    {
        el = el || window; // default to current window
        Util.Dom.stopAllEvents(el, 'dragstart'); // FF >= 3.5
        Util.Dom.stopAllEvents(el, 'draggesture'); // FF <= 3.0
    },

    stopDropEvents: function(el)
    {
        el = el || window; // default to current window
        Util.Dom.stopAllEvents(el, 'drop');
    }
};

/******************************************************************************************/

Util.Dom.focus = function(obj)
{
    if (!obj || typeof (obj.focus) != 'function') return false;

    try { obj.focus(); }
    catch (ex) { return false; } // IE throws error if frame is hidden

    return true;
};

Util.Dom.blur = function(obj)
{
    if (!obj || typeof (obj.blur) != 'function') return false;

    try { obj.blur(); }
    catch (ex) { return false; }

    return true;
};

// a helper function for focusing on the current window
Util.Dom.focusWindow = function(delay)
{
    if (YAHOO.lang.isNumber(delay)) setTimeout(function() { Util.Dom.focus(window); }, delay);
    else Util.Dom.focus(window);
};

// check if an element (needle) is a child of an array of DOM elements (haystacks)
Util.Dom.getAncestor = function(needle, haystacks)
{
    if (needle == null || haystacks == null) return false;

    var isMatch = function(element)
    {
        for (var i = 0; i < haystacks.length; i++)
        {
            var haystack = haystacks[i];
            if (haystack == element) return true;
        }

        return false;
    };

    if (isMatch(needle)) return needle;

    var element = null;

    try
    {
        element = YUD.getAncestorBy(needle, function(ancestor)
        {
            return isMatch(ancestor);
        });
    }
    catch (ex) { } // Permission denied to access property 'parentNode' from a non-chrome context

    return element;
};

// check if object is a DOM element
Util.Dom.isElement = function(o)
{
    try
    {
        return (
            typeof HTMLElement === "object" ? o instanceof HTMLElement : //DOM2
            typeof o === "object" && o.nodeType && typeof o.nodeName === "string"
        );
    }
    catch (ex) { return false; }
};

// input types that represent text input 
// http://www.w3schools.com/tags/att_input_type.asp
Util.Dom._TEXT_TYPES = {
    'text': 1,
    'password': 1
};

// is this element a text area (<input>, <textarea>, "html editor")?
Util.Dom.isTextInput = function(el) {

    // check valid element
    if (!el) return false;

    // check if textarea
    if (el.tagName && el.tagName.toLowerCase() == 'textarea') {
        return true;
    }

    // check if input text type
    if (el.tagName && el.tagName.toLowerCase() == 'input' && Util.Dom._TEXT_TYPES[el.type]) {
        return true;
    }

    // check if element is content editable (old way)
    if (el.contentEditable == '' ||
        el.contentEditable == 'true') {
        return true;
    }

    // check if element is content editable (new way)
    if (el.isContentEditable) {
        return true;
    }

    var doc = Util.Dom.getOwnerDocument(el);

    if (doc) {

        // check if contenteditable
        if (doc.designMode == 'on') {
            return true;
        }

        // check if explicitly set as allowing text input
        if (YUD.getAttribute(doc.body, 'data-tds-textinput') == 'true') {
            return true;
        }
    }

    return false;
};

// check if an image is loaded
Util.Dom.isImgLoaded = function(img)
{
    if (!img.complete)
    {
        return false;
    }
    
    if (typeof img.naturalWidth != 'undefined' && img.naturalWidth == 0)
    {
        return false;
    }
    
    return true;
};

/*************************/

Util.Dom.NodeType =
{
    ELEMENT: 1,
    ATTRIBUTE: 2,
    TEXT: 3,
    CDATA_SECTION: 4,
    ENTITY_REFERENCE: 5,
    ENTITY: 6,
    PROCESSING_INSTRUCTION: 7,
    COMMENT: 8,
    DOCUMENT: 9,
    DOCUMENT_TYPE: 10,
    DOCUMENT_FRAGMENT: 11,
    NOTATION: 12
};

/**
* Removes a node from its parent.
* @param {Node} node The node to remove.
* @return {Node} The node removed if removed; else, null.
*/
Util.Dom.removeNode = function(node)
{
    return node && node.parentNode ? node.parentNode.removeChild(node) : null;
};

/**
* Removes all the child nodes on a DOM node.
* @param {Node} node Node to remove children from.
*/
Util.Dom.removeChildren = function(node)
{
    // Note: Iterations over live collections can be slow, this is the fastest
    // we could find. The double parenthesis are used to prevent JsCompiler and
    // strict warnings.
    var child;
    while ((child = node.firstChild))
    {
        node.removeChild(child);
    }
};

/**
* Sets multiple properties on a node.
* @param {Element} element DOM node to set properties on.
* @param {Object} properties Hash of property:value pairs.
*/
Util.Dom.setProperties = function(element, properties)
{
    for (key in properties)
    {
        var val = properties[key];

        if (key == 'style')
        {
            element.style.cssText = val;
        }
        else if (key == 'class')
        {
            element.className = val;
        }
        else if (key == 'for')
        {
            element.htmlFor = val;
        }
        else if (key.lastIndexOf('aria-', 0) == 0) // startsWith
        {
            element.setAttribute(key, val);
        }
        else if (key in Util.Dom._DIRECT_ATTRIBUTE_MAP)
        {
            element.setAttribute(Util.Dom._DIRECT_ATTRIBUTE_MAP[key], val);
        }
        else
        {
            element[key] = val;
        }
    }
};

/**
* Map of attributes that should be set using
* element.setAttribute(key, val) instead of element[key] = val.  Used
* by Util.Dom.setProperties.
*
* @type {Object}
* @private
*/
Util.Dom._DIRECT_ATTRIBUTE_MAP =
{
    'cellpadding': 'cellPadding',
    'cellspacing': 'cellSpacing',
    'colspan': 'colSpan',
    'rowspan': 'rowSpan',
    'valign': 'vAlign',
    'height': 'height',
    'width': 'width',
    'usemap': 'useMap',
    'frameborder': 'frameBorder',
    'maxlength': 'maxLength',
    'type': 'type'
};

/**
* Returns the owner document for a node.
* @param {Node|Window} node The node to get the document for.
* @return {!Document} The document owning the node.
*/
Util.Dom.getOwnerDocument = function(node)
{
    return (
      node.nodeType == Util.Dom.NodeType.DOCUMENT ? node :
      node.ownerDocument || node.document);
};

/**
* Cross-browser function for setting the text content of an element.
* @param {Element} element The element to change the text content of.
* @param {string} text The string that should replace the current element
*     content.
*/
Util.Dom.setTextContent = function(element, text)
{
    if ('textContent' in element)
    {
        element.textContent = text;
    }
    else if (element.firstChild && element.firstChild.nodeType == Util.Dom.NodeType.TEXT)
    {
        // If the first child is a text node we just change its data and remove the
        // rest of the children.
        while (element.lastChild != element.firstChild)
        {
            element.removeChild(element.lastChild);
        }
        element.firstChild.data = text;
    }
    else
    {
        Util.Dom.removeChildren(element);
        var doc = Util.Dom.getOwnerDocument(element);
        element.appendChild(doc.createTextNode(text));
    }
};

/**
* Map of tags whose content to ignore when calculating text length.
* @type {Object}
* @private
*/
Util.Dom._TAGS_TO_IGNORE =
{
    'SCRIPT': 1,
    'STYLE': 1,
    'HEAD': 1,
    'IFRAME': 1,
    'OBJECT': 1
};

/**
* Map of tags which have predefined values with regard to whitespace.
* @type {Object}
* @private
*/
Util.Dom._PREDEFINED_TAG_VALUES = { 'IMG': ' ', 'BR': '\n' };

/**
* Returns the text content of the current node, without markup and invisible
* symbols. New lines are stripped and whitespace is collapsed,
* such that each character would be visible.
*
* In browsers that support it, innerText is used.  Other browsers attempt to
* simulate it via node traversal.  Line breaks are canonicalized in IE.
*
* @param {Node} node The node from which we are getting content.
* @return {string} The text content.
*/
Util.Dom.getTextContent = function(node)
{
    var textContent;

    // Note(user): IE9, Opera, and Safara 3 support innerText but they include
    // text nodes in script tags. So we revert to use a user agent test here.
    var CAN_USE_INNER_TEXT = (YAHOO.env.ua.ie > 0 && YAHOO.env.ua.ie < 9);

    if (CAN_USE_INNER_TEXT && ('innerText' in node))
    {
        // canonicalize new lines
        textContent = node.innerText.replace(/(\r\n|\r|\n)/g, '\n');
        // Unfortunately .innerText() returns text with &shy; symbols
        // We need to filter it out and then remove duplicate whitespaces
    }
    else
    {
        var buf = [];
        Util.Dom._getTextContent(node, buf, true);
        textContent = buf.join('');
    }

    // Strip &shy; entities.
    textContent = textContent.replace(/ \xAD /g, ' ').replace(/\xAD/g, '');

    // Skip this replacement on IE, which automatically turns &nbsp; into ' '
    // and / +/ into ' ' when reading innerText.
    if (!YAHOO.env.ua.ie)
    {
        textContent = textContent.replace(/ +/g, ' ');
    }
    if (textContent != ' ')
    {
        textContent = textContent.replace(/^\s*/, '');
    }

    return textContent;
};

/**
* Recursive support function for text content retrieval.
*
* @param {Node} node The node from which we are getting content.
* @param {Array} buf string buffer.
* @param {boolean} normalizeWhitespace Whether to normalize whitespace.
* @private
*/
Util.Dom._getTextContent = function(node, buf, normalizeWhitespace)
{
    if (node.nodeName in Util.Dom._TAGS_TO_IGNORE)
    {
        // ignore certain tags
    }
    else if (node.nodeType == Util.Dom.NodeType.TEXT)
    {
        if (normalizeWhitespace)
        {
            buf.push(String(node.nodeValue).replace(/(\r\n|\r|\n)/g, ''));
        }
        else
        {
            buf.push(node.nodeValue);
        }
    }
    else if (node.nodeName in Util.Dom._PREDEFINED_TAG_VALUES)
    {
        buf.push(Util.Dom._PREDEFINED_TAG_VALUES[node.nodeName]);
    }
    else
    {
        var child = node.firstChild;

        while (child)
        {
            Util.Dom._getTextContent(child, buf, normalizeWhitespace);
            child = child.nextSibling;
        }
    }
};

/**
 * Whether a node contains another node.
 * @param {Node} parent The node that should contain the other node.
 * @param {Node} descendant The node to test presence of.
 * @return {boolean} Whether the parent node contains the descendent node.
 */
Util.Dom.contains = function(parent, descendant) 
{
    // We use browser specific methods for this if available since it is faster
    // that way.

    // IE DOM
    if (parent.contains && descendant.nodeType == Util.Dom.NodeType.ELEMENT) {
        return parent == descendant || parent.contains(descendant);
    }

    // W3C DOM Level 3
    if (typeof parent.compareDocumentPosition != 'undefined') {
        return parent == descendant || Boolean(parent.compareDocumentPosition(descendant) & 16);
    }

    // W3C DOM Level 1
    while (descendant && parent != descendant) {
        descendant = descendant.parentNode;
    }
    
    return descendant == parent;
};

/**
 * Compares the document order of two nodes, returning 0 if they are the same
 * node, a negative number if node1 is before node2, and a positive number if
 * node2 is before node1.  Note that we compare the order the tags appear in the
 * document so in the tree <b><i>text</i></b> the B node is considered to be
 * before the I node.
 *
 * @param {Node} node1 The first node to compare.
 * @param {Node} node2 The second node to compare.
 * @return {number} 0 if the nodes are the same node, a negative number if node1
 *     is before node2, and a positive number if node2 is before node1.
 */
Util.Dom.compareNodeOrder = function(node1, node2) 
{
    // Fall out quickly for equality.
    if (node1 == node2) {
        return 0;
    }

    // Use compareDocumentPosition where available
    if (node1.compareDocumentPosition) {
        // 4 is the bitmask for FOLLOWS.
        return node1.compareDocumentPosition(node2) & 2 ? 1 : -1;
    }

    // Process in IE using sourceIndex - we check to see if the first node has
    // a source index or if its parent has one.
    if ('sourceIndex' in node1 || (node1.parentNode && 'sourceIndex' in node1.parentNode)) 
    {
        var isElement1 = node1.nodeType == Util.Dom.NodeType.ELEMENT;
        var isElement2 = node2.nodeType == Util.Dom.NodeType.ELEMENT;

        if (isElement1 && isElement2) 
        {
            return node1.sourceIndex - node2.sourceIndex;
        }
        else 
        {
            var parent1 = node1.parentNode;
            var parent2 = node2.parentNode;

            if (parent1 == parent2) {
                return Util.Dom._compareSiblingOrder(node1, node2);
            }

            if (!isElement1 && Util.Dom.contains(parent1, node2)) {
                return -1 * Util.Dom._compareParentsDescendantNodeIE(node1, node2);
            }

            if (!isElement2 && Util.Dom.contains(parent2, node1)) {
                return Util.Dom._compareParentsDescendantNodeIE(node2, node1);
            }

            return (isElement1 ? node1.sourceIndex : parent1.sourceIndex) - (isElement2 ? node2.sourceIndex : parent2.sourceIndex);
        }
    }

    // For Safari, we compare ranges.
    var doc = Util.Dom.getOwnerDocument(node1);

    var range1, range2;
    range1 = doc.createRange();
    range1.selectNode(node1);
    range1.collapse(true);

    range2 = doc.createRange();
    range2.selectNode(node2);
    range2.collapse(true);

    return range1.compareBoundaryPoints(window.Range.START_TO_END, range2);
};


/**
 * Utility function to compare the position of two nodes, when
 * {@code textNode}'s parent is an ancestor of {@code node}.  If this entry
 * condition is not met, this function will attempt to reference a null object.
 * @param {Node} textNode The textNode to compare.
 * @param {Node} node The node to compare.
 * @return {number} -1 if node is before textNode, +1 otherwise.
 * @private
 */
Util.Dom._compareParentsDescendantNodeIE = function(textNode, node) 
{
    var parent = textNode.parentNode;
    if (parent == node) {
        // If textNode is a child of node, then node comes first.
        return -1;
    }

    var sibling = node;
    
    while (sibling.parentNode != parent) {
        sibling = sibling.parentNode;
    }
    
    return Util.Dom._compareSiblingOrder(sibling, textNode);
};


/**
 * Utility function to compare the position of two nodes known to be non-equal
 * siblings.
 * @param {Node} node1 The first node to compare.
 * @param {Node} node2 The second node to compare.
 * @return {number} -1 if node1 is before node2, +1 otherwise.
 * @private
 */
Util.Dom._compareSiblingOrder = function(node1, node2) 
{
    var s = node2;
    while ((s = s.previousSibling)) 
    {
        if (s == node1) {
            // We just found node1 before node2.
            return -1;
        }
    }

    // Since we didn't find it, node1 must be after node2.
    return 1;
};

/******************************************************************************************/

// YUI DOM HELPER METHOD: If the className exists on the node it is removed, if it doesn't exist it is added.
Util.Dom.toggleClass = function(node, className, force)
{
    var add = (force !== undefined) ? force : !(YUD.hasClass(node, className));
    if (add) YUD.addClass(node, className);
    else YUD.removeClass(node, className);
};

// a helper method for looking up an element by class name and only getting first result
Util.Dom.getElementByClassName = function(className, tag, root, apply, o, overrides)
{
    var elements = YUD.getElementsByClassName(className, tag, root, apply, o, overrides);
    if (elements.length > 0) return elements[0];
    return null;
};

// get a selection range safely from a window object
Util.Dom.getSelectionRange = function(win)
{
    win = win || window;

    // get selection object
    var selection = win.getSelection();
    var range = null;

    // get range object
    // NOTE: Check rangeCount first if it is zero then don't call getRangeAt()
    if (selection && selection.rangeCount > 0)
    {
        try { range = selection.getRangeAt(0); }
        catch (ex) { }
    }

    return range;
};

/******************************************************************************************/

// get all the regions for this element
Util.Dom.getElementRegions = function(el)
{
    var regions = [];

    // check if element supports client rects
    if (el.getClientRects)
    {
        // get bounding rectangles (https://developer.mozilla.org/en-US/docs/DOM/element.getClientRects)
        var clientRects = el.getClientRects();

        for (var i = 0, ii = clientRects.length; i < ii; i++)
        {
            var clientRect = clientRects[i];
            var region = new YAHOO.util.Region(clientRect.top, clientRect.right, clientRect.bottom, clientRect.left);
            regions.push(region);
        }
    } 
    else
    {
        // get DOM position
        var targetPos = YAHOO.util.Dom.getXY(el);

        var top = targetPos[1],
            left = targetPos[0],
            bottom = top + el.offsetHeight,
            right = left + el.offsetWidth;

        var region = new YAHOO.util.Region(top, right, bottom, left);
        regions.push(region);
    }

    return regions;
};

Util.Dom.createElementFromHtml = function(html) {
    var div = document.createElement('div');
    div.innerHTML = html;
    return YUD.getFirstChild(div);
};

Util.Dom.batch = function(nodes, method, scope) {
    var count = nodes.length;
    for (var i = 0; i < count; i++) {
        var node = nodes[i];
        if (typeof scope == 'object') {
            method.call(scope, node);
        } else {
            method(node);
        }
    }
};

// get all elements by tag name 
// (DEPRECATED: use Util.Dom.getElementsByTagName)
Util.Dom.queryTags = function(name, context) {
    context = context || document;
    return context.getElementsByTagName(name);
};

// get the first element found by tag name
// (DEPRECATED: use Util.Dom.getElementByTagName)
Util.Dom.queryTag = function (name, context) {
    var results = Util.Dom.queryTags(name, context);
    return (results.length > 0) ? results[0] : null;
};

// get all elements by tag name and run a function against them
// (DEPRECATED: use Util.Dom.batchElementByTagName)
Util.Dom.queryTagsBatch = function (name, context, method, scope) {
    var results = Util.Dom.queryTags(name, context);
    return Util.Dom.batch(results, method, scope);
};

// get all elements by tag name in an array
Util.Dom.getElementsByTagName = function (name, context) {
    context = context || document;
    var collection = context.getElementsByTagName(name);
    return Util.Array.slice(collection);
};

// get the first element found by tag name
Util.Dom.getElementByTagName = function (name, context) {
    var list = Util.Dom.getElementsByTagName(name, context);
    return (list.length > 0) ? list[0] : null;
};

// get all elements by tag name and run a function against them
Util.Dom.batchElementsByTagName = function (name, context, method, scope) {
    var list = Util.Dom.getElementsByTagName(name, context);
    return Util.Dom.batch(list, method, scope);
};

Util.Dom.querySelectorAll = function(selector, context) {
    context = context || document;
    var nodeList = context.querySelectorAll(selector);
    return Util.Array.slice(nodeList); // convert into array
};

Util.Dom.querySelector = function(selector, context) {
    context = context || document;
    return context.querySelector(selector);
};

// find all matching selectors and run a function against them
Util.Dom.batchQuerySelector = function (selector, context, method, scope) {
    var results = Util.Dom.querySelectorAll(selector, context);
    return Util.Dom.batch(results, method, scope);
};

// find all matching selectors and run a function against them
// (DEPRECATED: use Util.Dom.batchQuerySelector)
Util.Dom.querySelectorBatch = Util.Dom.batchQuerySelector;

/******************************************************************************************/

// A Mochikit style Dombuilder for YUI:
// DESCRIPTION: http://jeethurao.com/blog/?p=156
// EXAMPLE: http://mochi.github.com/mochikit/doc/html/MochiKit/DOM.html
// ORIGINAL SOURCE: http://bitbucket.org/woadwarrior/dombuilder/src/tip/dombuilder.js
(function()
{
    Util.Dom.Builder = {};
    var lang = YAHOO.lang;
    var HTML = Util.Dom.Builder;
    var Element = YAHOO.util.Element;

    function processChild(child)
    {
        if (lang.isNull(child) || lang.isUndefined(child))
        {
            return null;
        }
        else if (lang.isArray(child))
        {
            var r, fn = arguments.callee, l = [];

            for (var i = 0; i < child.length; i++)
            {
                r = fn.call(this, child[i]);
                if (lang.isArray(r))
                {
                    for (var j = 0; j < r.length; j++)
                    {
                        l.push(r[j]);
                    }
                }
                else
                {
                    l.push(r);
                }
            }
            return l;
        }
        else if (lang.isString(child) || lang.isNumber(child))
        {
            return document.createTextNode(child);
        }
        else if (lang.isFunction(child))
        {
            return arguments.callee.call(this, child.call(this));
        }
        else if (Element && child instanceof Element)
        {
            var el = child.get('element');
            if (el)
            {
                return el;
            }
        }
        else
        {
            return child;
        }

        return null;    // Fallback
    }

    function __replaceWord(w)
    {
        return w.substr(1, 2).toUpperCase();
    }

    function processStyleName(k)
    {
        return k.replace(/\-\w/, __replaceWord);
    }

    function processAttr(k, v)
    {
        if (k == 'style' && lang.isObject(v))
        {
            var d = {};

            for (var sk in v)
            {
                if (lang.isValue(v[sk]))
                {
                    d[processStyleName(sk)] = v[sk];
                }
            }

            return d;
        }
        else if (!lang.isString(v) && lang.isValue(v))
        {
            return v.toString();
        }
        else if (lang.isFunction(v))
        {
            return arguments.callee.call(this, k, v());
        }
        return v;
    }

    function createDom(tag, attrs /* children */)
    {
        // IE does not allow setting of 'type' attribute on 'input' or 'button'.
        // http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/name_2.asp
        if ((YAHOO.env.ua.ie > 0 && YAHOO.env.ua.ie < 9) && lang.isObject(attrs) && (attrs.name || attrs.type))
        {
            var tagNameArr = ['<', tag];

            if (attrs.name)
            {
                tagNameArr.push(' name="', attrs.name, '"');
            }

            if (attrs.type)
            {
                tagNameArr.push(' type="', attrs.type, '"');

                // Clone attributes map to remove 'type' without mutating the input.
                attrs = YAHOO.lang.merge(attrs);
                delete attrs.type;
            }

            tagNameArr.push('>');
            tag = tagNameArr.join('');
        }

        var el = document.createElement(tag);

        if (lang.isObject(attrs))
        {
            for (var key in attrs)
            {
                if (lang.hasOwnProperty(attrs, key))
                {
                    var attr = processAttr.call(this, key, attrs[key]);

                    if (key === 'style')
                    {
                        if (lang.isObject(attr))
                        {
                            for (var sk in attr)
                            {
                                if (lang.hasOwnProperty(attr, sk))
                                {
                                    YUD.setStyle(el, sk, attr[sk]);
                                }
                            }
                        }
                        else
                        {
                            el.style.cssText = attr;
                        }
                    }
                    else if (key === 'class')
                    {
                        el.className = attr;
                    }
                    else
                    {
                        YUD.setAttribute(el, key, attr);
                    }
                }
            }
        }

        var children = [];

        for (var i = 2; i < arguments.length; i++)
        {
            children.push(arguments[i]);
        }

        for (var i = 0; i < children.length; i++)
        {
            var child = processChild.call(this, children[i]);

            if (!lang.isNull(child))
            {
                if (lang.isArray(child))
                {
                    for (var j = 0; j < child.length; j++)
                    {
                        el.appendChild(child[j]);
                    }
                }
                else
                {
                    el.appendChild(child);
                }
            }
        }

        return el;
    }

    HTML.createDom = createDom;

    var tag_cache = {};

    function makeTag(t)
    {
        var tag_name = t.toUpperCase();

        if (tag_cache.hasOwnProperty(tag_name))
        {
            return tag_cache[tag_name];
        }

        return Util.Function.bind(createDom, null, t);
    }

    HTML.makeTag = makeTag;
    var tag, tags = ['a', 'button', 'br', 'canvas',
        'dd', 'div', 'dl', 'dt', 'em', 'fieldset', 'form',
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'hr', 'img', 'input',
        'label', 'legend', 'li', 'ol', 'optgroup', 'option', 'p', 'pre',
        'select', 'span', 'strong', 'table', 'tbody', 'td', 'textarea',
        'tfoot', 'th', 'thead', 'tr', 'tt', 'ul'];

    // add each tag function to dom builder
    for (var i = 0, i_max = tags.length; i < i_max; i++)
    {
        tag = tags[i];
        HTML[tag.toUpperCase()] = makeTag(tag);
    }
})();

var HTML = Util.Dom.Builder;

/******************************************************************************************/

// Sets the specified outer HTML on a element
// https://github.com/tinymce/tinymce/blob/master/jscripts/tiny_mce/classes/dom/DOMUtils.js
Util.Dom.setOuterHTML = function(el, html, doc)
{
    function setHTML(el, html, doc)
    {
        var n, tp;

        tp = doc.createElement("body");
        tp.innerHTML = html;

        n = tp.lastChild;

        while (n)
        {
            YUD.insertAfter(n.cloneNode(true), el);
            n = n.previousSibling;
        }

        Util.Dom.removeNode(el);
    };

    el = YAHOO.util.Dom.get(el);

    // Only set HTML on elements
    if (!el || el.nodeType != 1) return null;

    doc = doc || el.ownerDocument || document;

    if (YAHOO.env.ua.ie)
    {
        try
        {
            // try outerHTML for IE it sometimes produces an "unknown runtime error"
            el.outerHTML = html;
        }
        catch (ex)
        {
            // fix for "unknown runtime error"
            setHTML(el, html, doc);
        }
    }
    else
    {
        setHTML(el, html, doc);
    }

    return el;
};