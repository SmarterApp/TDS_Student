// This module is used for setting up the hottext interactions (e.x., selectables, draggables)
(function() {

    // used for getting the interaction response
    function responseGetterITS(item, response) {

        var isValid = true;

        // build response xml with all interactions
        var xml = [];
        xml.push('<interactions>');

        Util.Array.each(item.interactions, function(interaction) {
            
            xml.push(interaction.getResponse());

            // check if response is valid
            if (!interaction.validateResponse()) {
                isValid = false;
            }
        });

        xml.push('</interactions>');

        // set response object
        response.value = xml.join('');
        response.isValid = isValid;
        response.isSelected = isValid;
    }
    
    function responseGetterQTI(item, response) {

        var isValid = true;
        var xmlDoc = Util.Xml.parseFromString('<itemResponse></itemResponse>');

        Util.Array.each(item.interactions, function (interaction) {
            // Build response for each individual interaction
            interaction.createResponseQTI(xmlDoc);
            // check if response is valid
            if (!interaction.validateResponse()) {
                isValid = false;
            }
        });

        // set response object
        response.value = Util.Xml.serializeToString(xmlDoc);
        response.isValid = isValid;
        response.isSelected = isValid;
    }

    // used for setting the interaction response
    function responseSetterITS(item, value) {

        // create an xml document
        var responseXml = Util.Xml.parseFromString(value);
        var interactionsNode = responseXml.documentElement;

        // iterate over all the interaction nodes
        Util.Dom.queryTagsBatch('interaction', interactionsNode, function(interactionNode) {
            
            var interactionIdentifier = interactionNode.getAttribute('identifier');

            var interactionMatch = Util.Array.find(item.interactions, function(interaction) {
                return (interactionIdentifier == interaction.getResponseIdentifier());
            });

            if (interactionMatch) {
                var interactionXml = Util.Xml.serializeToString(interactionNode);
                interactionMatch.loadResponse(interactionXml);
            }
        });
    }
    
    function responseSetterQTI(item, value) {

        var responseXml = Util.Xml.parseFromString(value);
        var rootNode = responseXml.documentElement;

        // iterate over <response>'s
        Util.Dom.batchElementsByTagName('response', rootNode, function(responseNode) {

            // get id <response id="1">
            var responseId = responseNode.getAttribute('id');

            // find first interaction that has a group matching the response id
            var interactionMatch = Util.Array.find(item.interactions, function (interaction) {
                var groups = interaction.getGroups();
                return Util.Array.some(groups, function (group) {
                    var groupId = group.toString();
                    return (groupId && groupId == responseId);
                });
            });

            if (interactionMatch) {
                interactionMatch.loadResponseQTI(responseNode);
            }
        });
    }
    
    function responseGetter(item, response) {
        if (item.format.toUpperCase() == 'HT') {
            responseGetterITS(item, response);
        } else {
            responseGetterQTI(item, response);
        }
    }

    function responseSetter(item, value) {
        if (item.format.toUpperCase() == 'HT') {
            responseSetterITS(item, value);
        } else {
            responseSetterQTI(item, value);
        }
    }

    // load interactions for a qti item
    var loadQtiInteraction = function (page, item) {
        var itemStem = item.getStemElement();
        var idPostfix = '-interaction-' + item.getID();
        item.interactions = [];
        
        var qtiXml = (item.qti) ? item.qti.xml : null;
        if (qtiXml == null) {
            return;
        }

        // Hottext item QTI contains regular HTML markup. The hottext objects know what to do with
        // the markup, so just add it to the page.
        var hti = new HottextItem.Parse(item.position);
        hti.createFromXml(qtiXml, itemStem);
        
        // add the clickable spans
        var selectInteraction = new TDS.SelectInteraction('select' + idPostfix);
        selectInteraction.load(itemStem);
        
        // add KB shortcuts to clickable spans
        var componentArray = selectInteraction.getSelectElements();
        for (var i = 0; i < componentArray.length; ++i) {
            item.addComponent(componentArray[i]);
        }

        if (selectInteraction.getChoices().length > 0) {
            item.interactions.push(selectInteraction);
        }

        // load response xml if resuming a paused test
        if (item.value != null) {
            responseSetter(item, item.value);
        }
    };

    // map <constaints> from the item
    function mapSelectConstraints(selectInteraction, item) {

        var constraints = Util.Array.find(item.specs, function (spec) {
            return spec.nodeName == 'constraints';
        });

        if (constraints == null) return;

        Util.Dom.queryTagsBatch('cardinality', constraints, function(el) {
            var id = Util.Xml.getAttribute(el, 'target');
            var min = Util.Xml.getAttributeInt(el, 'min');
            var max = Util.Xml.getAttributeInt(el, 'max');
            selectInteraction.setCardinality(id, min, max);
        });
    }

    // load interactions for an item
    function loadInteraction(page, item) {

        var itemContainer = item.getElement();

        item.interactions = [];

        var id_postfix = '-interaction-' + item.getID();

        /* DragDrop Interaction */

        var ddInteraction = new TDS.DDInteraction('dd' + id_postfix);
        ddInteraction.load(itemContainer);
        
        // get draggables
        var draggables = ddInteraction.getDraggables();
        if (draggables.length > 0) {
            
            // add DD to item interactions
            item.interactions.push(ddInteraction);
            
            // tell each DD proxy what the scroll container is
            var pageElement = page.getElement();
            var scrollContainer = Util.Dom.getElementByClassName('theQuestions', 'div', pageElement);
            if (scrollContainer) {
                for (var i = 0; i < draggables.length; i++) {
                    var ddProxy = draggables[i].getProxy();
                    ddProxy.setScrollDirection(true, true);
                    ddProxy.setParentScroll(scrollContainer);
                }
            }
        }

        /* SELECT INTERACTION */

        var selectInteraction = new TDS.SelectInteraction('select' + id_postfix);
        selectInteraction.load(itemContainer);

        // map constraints
        mapSelectConstraints(selectInteraction, item);

        if (selectInteraction.getChoices().length > 0) {
            item.interactions.push(selectInteraction);
        }
        
        /* ORDER INTERACTION */
        var orderInteraction = new TDS.OrderInteraction('order' + id_postfix);
        orderInteraction.load(itemContainer);

        if (orderInteraction.getGroups().length > 0) {
            item.interactions.push(orderInteraction);
        }

        /* RESPONSE */

        // load response xml
        if (item.value != null) {
            responseSetter(item, item.value);
        }

        // add KB shortcuts to clickable spans
        var componentArray = selectInteraction.getSelectElements();
        for (var i = 0; i < componentArray.length; ++i) {
            item.addComponent(componentArray[i]);
        }

    }

    // listen for when the item content is available
    ContentManager.onItemEvent('available', function(page, item) {
        if (item.isResponseType('HotText')) {
            loadInteraction(page, item);
        }
        if (item.isResponseType('QTI-Hottext')) {
            loadQtiInteraction(page, item);
        }
    });

    // listen for key events
    ContentManager.onItemEvent('keyevent', function (page, item, evt) {
        // check if mi
        if (!item.interactions) return;
        if (evt.type != 'keydown') return;
        if (evt.ctrlKey || evt.altKey) return; // no modifiers

        if ((evt.key == 'Enter') && (
                (item.isResponseType('HotText')) ||
                (item.isResponseType('QTI-Hottext'))
            )) {
            // ignore key events if in read-only mode
            if (ContentManager.isReadOnly()) return;

            evt.stopPropagation();

            // Trigger click event on active component
            var activeComp = item.getActiveComponent();
            if (activeComp && typeof activeComp.click == "function")
                activeComp.click();
        }
    });

    // register response getter/setter for hottext
    ContentManager.registerResponseHandler('HotText', responseGetter, responseSetter);

    // register response getter/setter for hottext
    ContentManager.registerResponseHandler('QTI-HotText', responseGetter, responseSetter);

})();