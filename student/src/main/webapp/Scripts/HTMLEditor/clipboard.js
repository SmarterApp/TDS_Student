//a class for editor copy-cut-paste functionality.
HTMLEditor.ClipBoardTools = function(editor)
{
    //YUI editor
    this._editor = editor; 
    //cliboard
    this._clipboard = null;     
}

HTMLEditor.ClipBoardTools.register = function(item, editor)
{   
    var editorClipboardTool = new HTMLEditor.ClipBoardTools(editor);
    editorClipboardTool._init();
    return editorClipboardTool;
}

/* We do not add these buttons here. Instead, we add them in htmleditor.js.
//These are the buttons we will be adding to the tool bar which pertain to copy-cut-paste implementation.
HTMLEditor.ClipBoardTools.EditButtons = [{ type: 'push', label: 'Copy', text: 'Copy', value: 'copy', disabled: false, title: 'Copy', id: 'copyButton' },  { type: 'push', label: 'Cut', text: 'Cut', value: 'cut', disabled: false, title: 'Cut', id: 'cutButton' }, { type: 'push', label: 'Paste', text: 'Paste', value: 'paste', disabled: false, title: 'Paste', id: 'pasteButton' }];
*/

HTMLEditor.ClipBoardTools.prototype._init = function()
{
    var myEditor = this._editor;
    var that = this;
    myEditor.on('toolbarLoaded', function() {
        /*
         * We do this instead in htmleditor.js
        //Add the custom buttons 
        myEditor.toolbar.addButtonGroup({ group: 'cutcopypaste', label: 'Copy Cut Paste', buttons: HTMLEditor.ClipBoardTools.EditButtons});
        */
        //and attach event handlers
        myEditor.toolbar.on('copyClick', that._onCopyClickHandler, null, that);
        myEditor.toolbar.on('cutClick', that._onCutClickHandler, null, that);
        myEditor.toolbar.on('pasteClick', that._onPasteClickHandler, null, that);
    }, myEditor, true);
     
}

HTMLEditor.ClipBoardTools.prototype._clipBoardNullOrEmpty = function() {
    if (this._clipboard == null)
        return true;
    else {
        var text = this._clipboard.textContent;
        if (text == "")
            return true;
    }
}

// Copy button click handler.
HTMLEditor.ClipBoardTools.prototype._onCopyClickHandler = function() 
{
    var editorWindow = this._editor._getWindow();
    var selected = this._getSelectedDocumentFragment(editorWindow, false);
    this._clipboard = selected[0];
    return false;
}

//Paste button click handler.
HTMLEditor.ClipBoardTools.prototype._onPasteClickHandler = function() {
    if (!this._clipBoardNullOrEmpty()) {
        var iframeWindow =  this._editor._getWindow();
        /*
         * https://bugz.airws.org/default.asp?32236#181366
         * We are only allowing copy from within the editor window we do not need to clean up HTML.
         */
        //var cleanHTML = this._editor.cleanHTML(this._clipboard.innerHTML);
        this._editor.execCommand('inserthtml', this._clipboard.innerHTML);
    }
    return false;
}

//Cut button click handler.
HTMLEditor.ClipBoardTools.prototype._onCutClickHandler = function() {
    var iframeWindow = this._editor._getWindow();

    var selected = this._getSelectedDocumentFragment(iframeWindow, true);
    //remember selected[0] is the positive selection and selected[1] is the negative selection.
    this._clipboard = selected[0];

    if (!this._clipBoardNullOrEmpty()) {
        /*
         * https://bugz.airws.org/default.asp?32236#181366
         * We are only allowing copy from within the editor window we do not need to clean up HTML.
         */
        //var newContent = this._editor.cleanHTML(selected[1].innerHTML);
        this._editor.setEditorHTML(selected[1].innerHTML);        
    }
    return false;
}

/*
 * Returns an array of size at the most two: the first element is what was selected in this window - positive selection - and the second element 
 * is what was left outside of the selection - negative selection.
 * Both elements are of type Element. This method always returns the positive selection. The negative selection is returned only if shouldCut is true.
 * 
 * @param thiswindow: The window of concern.
 * @param shouldCut: true/false valued input. A true value indicates that the caller is interested in negative selection as well.
 * @return: Returns the positive and negative selection in an array of size two. the negative selection is only returned if shouldCut is true. 
 */

HTMLEditor.ClipBoardTools.prototype._getSelectedDocumentFragment = function(thiswindow, shouldCut) {
    //a clipboard to hold the document fragment.
    var clipboard = null;
    var notSelectedClipboard = null;
    var selection = thiswindow.getSelection();
    var thisdocument = thiswindow.document;
    if (selection.rangeCount > 0) {
        var selectedRange = selection.getRangeAt(0);

        /*
         * We are not going to use this for the time being.
         */
        var setAncestorForTextNode = function(element)
        {
            if (element == null || element.nodeName == 'BODY')
                return element;
            //else we will go up the ancestor chain as long as we keep finding
            //parents of type b, i, u 
            var parent = element.parentNode;
            /*
            //recurse a few levels up - will limit it to 5 at the max.
            //i do not want to use a while(true) loop.
            while(true)
            */
            var found = false;
            if (parent != null && parent.nodeName != 'BODY')
            {
                for (var counter1 = 0; counter1 < 5; ++counter1)
                {
                    /*
                    //lets forget about this condition and get upto last 5 parents.
                    if (parent.nodeName == 'B' || parent.nodeName == 'I' || parent.nodeName == 'U')
                    {
                    */
                        found = true;
                        if (parent.parentNode != null && parent.parentNode.nodeName != 'BODY')
                        {
                            parent = parent.parentNode;
                            continue;
                        }
                        else
                            break;                                
                    /* }
                    else
                        break;
                     */  
                }
            }
            return (found)? parent : element;
        }

        var clearAllChildNodes = function(nd) {
            if (nd.childNodes != null) {
                var numberOfNodes = nd.childNodes.length;
                for (var counter1 = 1; counter1 <= numberOfNodes; ++counter1) {
                    nd.removeChild(nd.childNodes[0]);
                }
            }
        }

        //if shouldCut is set then notSelectedWouldBeParent will be the one to which we will add not selected nodes.
        var addNodesToDocFrag = function(selection, wouldBeParent, currentNode, shouldCut, notSelectedWouldBeParent) {
            //if this is a "body" node then we have a special case to take care of.
            //rather than simple cloning we need to create a new div element and still in all cloned 
            //children inside this new div element.
            var nodeToAdd = null;
            var notSelectedNodeToAdd = null;
            if (currentNode.nodeType == Node.ELEMENT_NODE && currentNode.nodeName.toLowerCase() == 'body') {
                nodeToAdd = document.createElement('span');
                notSelectedNodeToAdd = document.createElement('span');
            }
            else {
                notSelectedNodeToAdd = currentNode.cloneNode(false);
                nodeToAdd = currentNode.cloneNode(false);
            }

            clearAllChildNodes(nodeToAdd);
            clearAllChildNodes(notSelectedNodeToAdd);

            if (selection.containsNode(currentNode, true)) {
                var appendNewChild = false;
                var text = "";
                var remainingText = "";
                //is the node partially in there?
                //only startcontainer and endcountainers would be partially in the selection.
                if (selectedRange.endContainer.nodeValue && selectedRange.endContainer == currentNode) {
                    //if start and end nodes are the same then we have just to extract subtext.
                    if (selectedRange.endContainer == selectedRange.startContainer) {
                        text = selectedRange.endContainer.nodeValue.substring(selectedRange.startOffset, selectedRange.endOffset);
                        if (selectedRange.startOffset > 0)
                            remainingText = selectedRange.endContainer.nodeValue.substring(0, selectedRange.startOffset);

                    }
                    else {
                        text = selectedRange.endContainer.nodeValue.substring(0, selectedRange.endOffset);
                    }
                    if (selectedRange.endOffset < selectedRange.endContainer.nodeValue.length)
                        remainingText = remainingText + selectedRange.endContainer.nodeValue.substring(selectedRange.endOffset);

                    appendNewChild = true;
                }
                else if (selectedRange.startContainer.nodeValue && selectedRange.startContainer == currentNode) {
                    //todo: we may need to recurse up in this case to get the styling information but the question is how far do we recurse?
                    text = selectedRange.startContainer.nodeValue.substring(selectedRange.startOffset, selectedRange.startContainer.nodeValue.length);
                    if (selectedRange.startOffset > 0)
                        remainingText = selectedRange.startContainer.nodeValue.substring(0, selectedRange.startOffset);
                    appendNewChild = true;
                }
                /*
                else {
                //the case where node is completely inside the selection. handled later.
                }
                */

                if (appendNewChild) {
                    var newdiv = thisdocument.createElement('span');
                    newdiv.innerHTML = text;
                    wouldBeParent.appendChild(newdiv);

                    if (shouldCut) {
                        newdiv = thisdocument.createElement('span');
                        newdiv.innerHTML = remainingText;
                        wouldBeParent.appendChild(newdiv);
                        notSelectedWouldBeParent.appendChild(newdiv);
                    }
                }
                else {
                    //node is completely inside.
                    //in this case even if "shouldCut" is true we won't have anything to 
                    //add to the "not selected" tree from the current node. We however may have to add a dummy child to hold
                    //possible future exclusions e.g. in the case of "body" i.e. if the current node is an element node.
                    wouldBeParent.appendChild(nodeToAdd);
                    if (shouldCut && currentNode.nodeType == Node.ELEMENT_NODE) {
                        notSelectedWouldBeParent.appendChild(notSelectedNodeToAdd);
                    }
                }
            }
            else {
                if (shouldCut)
                    notSelectedWouldBeParent.appendChild(notSelectedNodeToAdd);
            }

            //now recurse through its children.
            for (var counter1 = 0; counter1 < currentNode.childNodes.length; ++counter1) {
                addNodesToDocFrag(selection, nodeToAdd, currentNode.childNodes[counter1], shouldCut, notSelectedNodeToAdd);
            }
        }

        var commonAncestor = null;
        //if we are being asked to cut from DOM, then we need to also return not only what was 
        //selected but also what got left behind.
        /*
        //start fix for https://bugz.airws.org/default.asp?22387
        //we now recurse upto body regardless of wether this is cut or copy click.
        //the only pitfall of this may be that it will be slow and also that it may copy unwanted 
        //styling e.g. indentation.
        if (shouldCut) {
            notSelectedClipboard = thisdocument.createElement('span');
            commonAncestor = (thisdocument.getElementsByTagName("body"))[0];
        }
        else
        {
            commonAncestor = selectedRange.commonAncestorContainer;
            //
            //we may have an example like this. if just the "in" is selected we want to 
            //simultaneously get the style information.
            //<span class="padding" _moz_dirty="">Tell <b _moz_dirty="">what's <i _moz_dirty="">happening<u _moz_dirty=""> in </u>this </i>picture</b>.</span>
            //
            commonAncestor = setAncestorForTextNode(commonAncestor);
        }
        */
        notSelectedClipboard = thisdocument.createElement('span');
        commonAncestor = (thisdocument.getElementsByTagName("body"))[0];
        //end fix for https://bugz.airws.org/default.asp?22387
        
        /*
        * documentFragment does not have innerHTML
        clipboard = thisdocument.createDocumentFragment(); 
        */
        clipboard = thisdocument.createElement('span');
        addNodesToDocFrag(selection, clipboard, commonAncestor, shouldCut, notSelectedClipboard);
    }
    return [clipboard, notSelectedClipboard];
}
