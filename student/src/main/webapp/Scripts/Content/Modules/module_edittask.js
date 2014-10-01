/*
This module is used for loading editText or editChoice interaction items (QTI).
*/

(function() {

    function isEditTaskText(item) {
        return item.isResponseType('EditTask');
    }

    function isEditTaskChoice(item) {
        return item.isResponseType('EditTaskChoice');
    }

    function isEditTask(item) {
        return isEditTaskText(item) || isEditTaskChoice(item);
    }

    // listen for when the item is first initialized (nothing is renderer yet)
    function init(page, item) {

        // change the stem to be the response container
        item.getStemElement = function() {
            var compoundEl = page.getCompoundElement();
            if (compoundEl) {
                return compoundEl;
            } else {
                return item.getElement(); // we need stem and response area
            }
        };
    }

    // process a edit task item and render html
    function process(page, item) {
        
        ContentManager.log("EditItem: processing item " + item.getID());

        // Look for the parent (container) div
        var pageDoc = page.getDoc();
        var container = pageDoc.getElementById('EditContainer_' + item.position);
        if (container == null) {
            console.warn('EditItem: Could not find the container for item ' + item.getID());
            return;
        }

        // Parse the xml file.  It comes from qti field.
        var qtiXml = (item.qti) ? item.qti.xml : null;
        if (qtiXml == null) {
            console.warn('EditItem: Could not find QTI for item ' + item.getID());
            return;
        }

        // Edit item QTI contains regular HTML markup.  The edit objects know what do do with
        // the markup, so just add it to the page.
        var ei = new EditItem.Parse(item.position);
        ei.createFromXml(qtiXml,container);
        
        YUD.removeClass(container, 'loading');
        YUD.addClass(container, 'edit-container');

        // Per requirements, all edit interactions in an item are either
        // choice or text, not mixed.
        var editChoiceInstance = ei.choiceInteractions;
        var editTextInstance = ei.textInteractions;

        var editing = {
            editText: editTextInstance,
            editChoice: editChoiceInstance
        };

        item.editing = editing;

        // TODO: Use YUE.on() and not use onresize directly
        var onresize = window.onresize;
        window.onresize = function () {
            if (onresize != null)
                onresize();
            if (item.editing && item.editing.editText)
                item.editing.editText.setDbPosition();
        };

        editChoiceInstance.showItem();
        editTextInstance.showItem();

        // Add components to keyboard navigation using ctrl-TAB
        var componentArray = editTextInstance.getComponentArray();
        componentArray.push.apply(componentArray, editChoiceInstance.getComponentArray());
        for (var i = 0; i < componentArray.length; ++i) {
            item.addComponent(componentArray[i]);
        }

        // check if there is an existing response
        if (item.value != null) {
            if (isEditTaskText(item)) {
                editTextInstance.setXmlResponse(item.value);
            }
            else if (isEditTaskChoice(item)) {
                editChoiceInstance.setXmlResponse(item.value);
            }
        }
        
    }
    
    function processKey(page, item, evt) {
        // check if mi
        if (item == null || item.editing == null) return;
        if (evt.type != 'keydown') return;
        if (evt.ctrlKey || evt.altKey) return; // no modifiers

        // ignore key events if in read-only mode
        if (ContentManager.isReadOnly()) return;

        // get the current edit task span
        var componentEl = item.getActiveComponent(evt);
        
        // if the component is the stem then let's leave
        if (componentEl == null) {
            // Component logic sometimes gives us the wrong component.  Since the dialog
            //box that controls this is modal, we will always assume key elements are for the active
            // db.  If no db is up then we check for that and exit.
            //||  componentEl == item.getStemElement()) {
            return;
        }

        var edits = null;
        
        if (isEditTaskText(item)) {
            edits = item.editing.editText;
        }
        else if (isEditTaskChoice(item)) {
            edits = item.editing.editChoice;
        }

        // Notify widget of key event and fetch handler based on component ID
        if (edits) {
            edits.handleKeyEvent.call(edits, componentEl, evt);
        }
    }

    // this event checks when page is ready and checks item format and qti content
    ContentManager.onItemEvent('init', function (page, item) {
        if (isEditTask(item)) {
            init(page, item);
        }
    });
    
    // this event checks when page is ready and checks item format and qti content
    ContentManager.onItemEvent('available', function (page, item) {
        if (isEditTask(item)) {
            process(page, item);
        }
    });

    // listen for key events
    ContentManager.onItemEvent('keyevent', processKey);

    // If we zoom,the dialog gets displaced.  Undisplace it.
    ContentManager.onItemEvent('zoom', function (page, item) {
        if (item.editing && item.editing.editText) {
            item.editing.editText.setDbPosition();
        }
    });
    
    // Handle the case where an item is being shown again.
    ContentManager.onItemEvent('hide', function (page, item) {

        if (item.editing) {

            var activeInstance = EditItem.Html._activeDbInstance;

            if (activeInstance.panelWidget) {
                activeInstance.panelWidget.hide();
                activeInstance.interaction = null;

                if (activeInstance.activeSpan) {
                    YUD.removeClass(activeInstance.activeSpan, 'TDS_EDIT_SPAN_HOVER');
                    activeInstance.activeSpan = null;
                }
            }
        }

    });

})();

// register response getter and setter for Edit questions
(function () {
    // response handler for EditTask questions (choice not here yet)
    var getter = function (item, response) {
        var value = '';
        if (item && item.editing) {
            var editType = (item.responseType == 'EditTask') ? item.editing.editText : item.editing.editChoice;
            value = editType.getXmlResponse();
        }

        response.value = value;
        response.isAvailable = true;

        var validResponse = (value && value.length > 0) ? true : false;
        response.isSelected = validResponse;
        response.isValid = validResponse;
    };

    // response handler for EditTask questions (choice not here yet)
    var setter = function (item, value) {
        if (item && item.editing) {
            var editType = (item.responseType == 'EditTask') ? item.editing.editText : item.editing.editChoice;
            editType.setXmlResponse(value);
        }
    };

    ContentManager.registerResponseHandler('EditTask', getter, setter);
    ContentManager.registerResponseHandler('EditTaskChoice', getter, setter);
})();


