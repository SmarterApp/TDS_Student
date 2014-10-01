(function(rangy) {
    
    // check for rangy
    if (!rangy) return;

    if(rangy.config){
        rangy.config.alertOnFail = false;
        rangy.config.alertOnWarn = false;
    }

    // this variable will get lazy loaded on the first call
    var globalClassApplier;

    function getSelfOrAncestorWithClass(node, cssClass) {
        while (node) {
            if (YUD.hasClass(node, cssClass)) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    };
    // Checks for MathML structures with children
    function checkMathML(node) {
        var mathTypes = /mfrac|msup|mfenced|msqrt|msubsup|msub|mover|munder|msubsup|mlongdiv|munderover|mroot/;
        if (mathTypes.test(node)) {
            return true;
        }
        return false;
    };

    function createCSSClassApplier() {
        var cssClassApplier = rangy.createCssClassApplier('highlight', {
            ignoreWhiteSpace: true,
            useExistingElements: false,
            removeEmptyElements: false,
            tagNames: ['span']
        });   
        
        // modify this function for any elements you don't want to perform highlighting on
        cssClassApplier.isModifiable = function(textNode) {
            // ignore any text nodes that have a ancestor with the class 'no-highlight'
            // Bug 121028 ignores any node that is a MathML parent structure with children, this does not affect display
            if (getSelfOrAncestorWithClass(textNode.parentNode, 'no-highlight') != null || checkMathML(textNode.parentNode.nodeName)){
                return false;
            }
            return true;
        };

        return cssClassApplier;
    }

    function getCSSClassApplier() {
        if (!globalClassApplier) {
            globalClassApplier = createCSSClassApplier();
        }
        return globalClassApplier;
    }

    // highlight selected text
    function setHighlight(selection) {
        if (selection.rangeCount > 0) {
            var cssClassApplier = getCSSClassApplier();
            cssClassApplier.applyToRanges(selection.getAllRanges());
            selection.collapseToStart();
        }
    }
    
    // clear highlighting for an element
    function clearHighlighting(el) {
        
        // create a range for the element
        var range = rangy.createRange();
        range.selectNode(el);
        
        // undo the highlighting for the element range
        var cssClassApplier = getCSSClassApplier();
        cssClassApplier.undoToRange(range);
    }

    // does a selection object have any text selected
    function hasSelection(selection) {
        var text = selection.toString();
        text = YAHOO.lang.trim(text);
        return (text.length > 0);
    }

    // this function is executed when a menu is shown
    function onMenuShow(page, entity, menu, evt, selection) {

        // check if highlighting accommodation is enabled
        var accProps = page.getAccommodationProperties();
        if (!accProps.hasHighlighting()) return;

        var hasSelectedText = hasSelection(selection);

        if (hasSelectedText) {
            var lblHighlightText = ContentManager.getLabel('HIGHLIGHT_TEXT');
            menu.addMenuItem('entity', lblHighlightText, function() {
                setHighlight(selection);
                // BUG #16185: The Flashing cursor is not seen after highlighting some text
                ContentManager.enableCaretMode(false);
            });
        }

        // check if there is previously highlighted text on the page
        // BUG: you can't pass in <span> as the tag because in IE we use <font>
        var activeDoc = page.getActiveDoc();
        var highlightedElement = entity.getElement();

        // if the highlighted element's document is different than the
        // active document then we should use the active document and 
        // check for highlighting in that instead (fixes sim)
        if (highlightedElement && highlightedElement.ownerDocument != activeDoc) {
            highlightedElement = activeDoc.body;
        }

        var highlightedNodes = YAHOO.util.Dom.getElementsByClassName(ContentManager.CSS.HIGHLIGHT, null, highlightedElement);
        var hasHighlightedText = (highlightedNodes.length > 0);

        if (hasHighlightedText) {
            var lblHighlightClr = ContentManager.getLabel('HIGHLIGHT_CLEAR');
            menu.addMenuItem('entity', lblHighlightClr, function() {
                clearHighlighting(highlightedElement);
            }, !hasHighlightedText);
        }
    }

    // subscribe to when context menu is shown for passage/items
    ContentManager.onEntityEvent('menushow', onMenuShow);

})(window.rangy);
