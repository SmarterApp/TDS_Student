HottextItem = (typeof (HottextItem) == "undefined") ? {} : HottextItem;

///////////////////////
// Parser.  Parse the hottextItem xml text and create a js object that we pass to the renderer
////
//   A Hottext item contains valid html markup in its xml, with specific tags
//   interspersed that contain the qti item rendering content.
//   We need to go through and store the qti information, then remove the non-html tags.
HottextItem.Parse = function(id) {
    // Unique ID for things in this item
    this.mid = id;

    // To convert xml into html, we need to traverse the DOM and preserve only the html parts.
    // The hottext xml specifications are removed and replaced within a span, to be used during rendering.
    this._fixXmlTasks={};

};

// Student app sends us xml doc in one big string.  Parse it.
HottextItem.Parse.prototype.createFromXml = function(xmlString, container) {
    var xmlDoc = Util.Xml.parseFromString(xmlString);

    // Remove the outer tag, replace it with html-friendly 'div', 
    // place the whole node inside the stem.
    this.parseItemBody(xmlDoc);

    var xmlNodes = xmlDoc.documentElement;

    // Extract the qti logic from the XML dom.
    this.parseHottextInteractions(xmlDoc, xmlNodes);

    // Remove the qti parts from the html parts, now that we've extracted them.
    this.fixXml(xmlDoc, xmlNodes);

    // Now we have only html part left.  Put that into a div for later rendering.
    var aDiv = document.createElement('div');

    // Convert from XHTML to HTML in cross-browser way.
    aDiv.innerHTML = Util.Xml.serializeToString(xmlNodes);
    container.innerHTML = aDiv.childNodes[0].innerHTML;
};

// The whole item is embedded in 'item body' tag.  Remove that since it's not valid HTML.
HottextItem.Parse.prototype.parseItemBody = function(doc) {
    var itemBody = doc.getElementsByTagName('itemBody');
    if (itemBody && itemBody.length) {
        // There should just be one, but just in case...
        if (itemBody && itemBody.length == 1) {
            var itemElement = itemBody[0];
            var itemDiv = doc.createElement('div');
            var test = itemElement.parentNode.nodeName;
            YUD.setAttribute(itemDiv, 'id', test);
            var itemChildren = YUD.getChildren(itemElement);
            for (var j = 0; j < itemChildren.length; ++j) {
                var childNode = itemChildren[j];
                var childClone = childNode.cloneNode(true);
                itemDiv.appendChild(childClone);
            }
            itemElement.parentNode.replaceChild(itemDiv, itemElement);
        }
    }
};

// Parse hottextInteractions xml elements, convert them into HTML 'div'.
HottextItem.Parse.prototype.parseHottextInteractions = function(doc, xmlElements) {
    var interactionElements = xmlElements.getElementsByTagName('hottextInteraction');
    if (interactionElements && interactionElements.length) {
        for (var i = 0; i <= interactionElements.length; ++i) {
            i = 0;
            var interactionElement = interactionElements[i];

            // Create a div element for the hottextInteraction
            var interactionDiv = doc.createElement('div');

            // Store responseId and maxChoices in the div attributes
            var responseIdentifier = YUD.getAttribute(interactionElement, 'responseIdentifier');
            //var maxChoices = YUD.getAttribute(interactionElement, 'maxChoices');

            var divId = this.createDivId(interactionDiv.nodeName, 'ht-' + this.mid + '-' + responseIdentifier);
            YUD.setAttribute(interactionDiv, 'id', divId);
            //YUD.setAttribute(interactionDiv, 'maxChoices', maxChoices);

            // Save children of the xml element to the new div
            var interactionChildren = YUD.getChildren(interactionElement);
            for (var j = 0; j < interactionChildren.length; ++j) {
                var childNode = interactionChildren[j];
                var childClone = childNode.cloneNode(true);
                interactionDiv.appendChild(childClone);
            }
            interactionElement.parentNode.replaceChild(interactionDiv, interactionElement);
        }
    }
    this.parseHottexts(doc, xmlElements);
};

// Create shell-unique identifier for this interaction block.
HottextItem.Parse.prototype.createDivId = function (nodeType, identifier) {
    return this.mid + '-' + nodeType + '-' + identifier;
};

// Parse hottext xml elements and convert them into HTML 'span'.
HottextItem.Parse.prototype.parseHottexts = function(doc, xmlElements) {
    var hottextElements = xmlElements.getElementsByTagName('hottext');
    if (hottextElements && hottextElements.length) {
        for (var i = 0; i <= hottextElements.length; ++i) {
            i = 0;
            var hottextElement = hottextElements[i];
            // Create a span element for the hottext
            var hottextSpan = doc.createElement('span');

            // Specify class to this span
            YUD.addClass(hottextSpan, 'interaction selectable');
            // Store identifier and create group in the span attributes
            //var identifier = YUD.getAttribute(hottextElement, 'identifier');
            //var group = '1';
            //YUD.setAttribute(hottextSpan, 'data-its-identifier', identifier);
            //YUD.setAttribute(hottextSpan, 'data-its-group', group);

            // Save children of the hottext element to the new span
            for (var j = 0; j < hottextElement.childNodes.length; ++j) {
                var childNode = hottextElement.childNodes[j];
                var childClone = childNode.cloneNode(true);
                hottextSpan.appendChild(childClone);
            }
            hottextElement.parentNode.replaceChild(hottextSpan, hottextElement);
        }
    }
};

// Identify and remove xml tags that are not valid html.
HottextItem.Parse.prototype.fixXml = function(xmlDoc, parentNode) {
    this._fixXmlTasks = [];
    this.fixXmlRecurse(parentNode);
    // Now remove them.
    for (var i = 0; i < this._fixXmlTasks.length; ++i) {
        var parent = this._fixXmlTasks[i].parent;
        var child = this._fixXmlTasks[i].child;
        parent.removeChild(child);
    }
};

// First recurse through DOM and identify any nodes that are to be removed.
HottextItem.Parse.prototype.fixXmlRecurse = function(parentNode) {
    for (var i = 0; i < parentNode.childNodes.length; ++i) {
        var childNode = parentNode.childNodes[i];
        if (childNode.nodeName == 'hottextInteraction') {
            var obj = {
                parent: parentNode,
                child: childNode
            };
            this._fixXmlTasks.push(obj);
        } else {
            this.fixXmlRecurse(childNode);
        }
    }
};