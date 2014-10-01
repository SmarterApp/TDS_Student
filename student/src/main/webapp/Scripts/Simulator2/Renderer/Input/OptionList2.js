/**
 * *****************************************************************************
 * @class OptionList 
 * @superclass GroupList
 * @param sim - The Simulator instance
 * @param node -
 * @param panel - 
 * @return - instance of a OptionList
 *******************************************************************************
 */
Simulator.Input.OptionList = function(sim, node, panel, theSection) {

    Simulator.Input.GroupList.call(this, sim); // Inherit Instance variables
    
    var that = this;
    
    var source = 'OptionList';

    var dbg = function() { return sim.getDebug(); };
    var utils = function() { return sim.getUtils(); };
    var scoringTable = function () { return sim.getScoringTable(); }; // get scoring table instance
    var keyboardInput = function () { return sim.getKeyboardInput(); };  // get keyboard input instance
    var simMgr = function() { sim.getSimulationManager(); };
    var simDocument = function() { return sim.getSimDocument(); };

    if(sim) {
        this.setPanel(panel);
        this.setSection(theSection);
    }

    // Instance variables are all inherited
    this.getData = function(inputScope) {
        var data = [];
        var proxy = null;
        var items = this.getItems();
        for(var i = 0;  i < items.length; i++) {
            var HTMLItem = simDocument().getElementById(items[i].lookup('itemID'));
            var labelFor = simDocument().getElementById(items[i].lookup('labelForID'));
            if(inputScope == 'dataInput') proxy = this.getDataProxy(i);
            else if(inputScope == 'evaluationInput') proxy = this.getEvaluationProxy(i);
            else if(inputScope == 'animationInput') proxy = this.getAnimationProxy(i);
            if(HTMLItem.checked) {
                if(proxy != undefined && proxy != null) data[0] = proxy;
                else data[0] = items[i].lookup('val');
                this.setData(data[0]);
                labelFor.setAttribute('class', 'inputChecked');
                items[i].setValue('checked','true');
                selectedItem = data[0];  // selectedItem defined in the superclass
            }
            else {
                labelFor.removeAttribute('class');
                items[i].setValue('checked', 'false');
            }
        }
        if(data.length == 0) this.setData('');  // If we get here, there is no item found to be checked
        return data;
    };    
    
    this.handleEvent = function(event) {
        switch (event.type) {
        case 'inputReq':
            this.recordInput(this);
            break;        
        case 'info':
            switch(event.context) {
            case 'simulatorStateChange':
                    
                break;
            }
            break;
        default:
            dbg().logWarning(source, 'Unhandled event type received: ' + event.toString());
            return;
        }
        
    };
    
    this.setElementSelectState = function(state, contents) {
        var items = this.getItems();
        var proxy = null;
        var value = null;
        for(var i = 0; i < items.length; i++) {
            proxy = items[i].lookup('dataProxy');
            if(proxy) value = proxy;
            else value = items[i].lookup('val');
            var itemID = items[i].lookup('itemID');
            var labelFor = simDocument().getElementById('labelFor' + itemID);
            var htmlElement = simDocument().getElementById(itemID);
            if(value == contents) {
                items[i].setValue('checked','true');
                selectedItem = value;  // selectedItem defined in the superclass
                htmlElement.checked = 'checked';
                labelFor.setAttribute('class', 'inputChecked');
                this.recordInput(this, true);
            }
            else if(items[i].lookup('checked')) {
                items[i].setValue('checked', 'false');
                htmlElement.removeAttribute('checked');
                labelFor.removeAttribute('class');
            }
        }
    };
    
    this.keyboardNavigateTo = function(elementID, itemID, index) {
        var element = simDocument().getElementById(elementID);
        var nodes = element.getElementsByTagName('li');
        var item = nodes[index];
        if(element) element.selectedIndex = index;            
        if(item) item.setAttribute('class', 'simAreaFocus');
    };
    
    this.receivedSpeechFocus = function() {
        debug(this.getName() + ' received speech focus');
        var element = simDocument().getElementById(this.getNodeID());
        var nodes = element.getElementsByTagName('li');
        var item = nodes[0];
        if(element) element.selectedIndex = 0;            
        element.style.border='thin solid #ff0000';
        if(item) item.setAttribute('class', 'simAreaFocus');
    };
    
    this.keyboardNavigateAwayFrom = function(elementID, itemID, index) {
        var element = simDocument().getElementById(elementID);
        var nodes = element.getElementsByTagName('li');
        var item = nodes[index];
        item.removeAttribute('class');
    };
    
    this.removeSpeechFocus = function() {
        debug(this.getName() + ' lost speech focus');
        var element = simDocument().getElementById(this.getNodeID());
        var nodes = element.getElementsByTagName('li');
        var item = nodes[0];
        item.removeAttribute('class');
        element.style.border='none';
    };
    
    this.disableInput = function() {
        this.disableItems();
    };
    
    this.enableInput = function() {
        this.enableItems();
    };
    
    this.speechActivated = function(value) {        
        this.setSelectStateViaSpeech(this.getNodeID(), value);
        this.onChange(this.getNodeID());
    };

    this.setAttributes = function(attr, node) {
        Simulator.Input.OptionList.prototype.setAttributes.call(this, attr, node);
        if(this.getScoreable()) scoringTable().addElement(this.getName(), 'input');
        this.setItems(node);
    };
    
    this.doOnClick = function(id) {
    	this.onChange(id);
    };

    this.render = function() {
        var nodeID = this.getNodeID();
        var itemID = null;
        var items = this.getItems();
        var nextNum = utils().getNextSequenceNumber();
        var image = null;
        var panelHtml = panel.getHTMLElement();
        
        this.setFocusable(true, true); // Don't set the element itself to accept keyboard input
/*        if(simMgr().getSpeechEnabled()) {
            var label = this.getSpeechLabel();  // if there is a speechLabel defined in the xml use that
            if(label == '') {
                label = this.getLabel();
                if(label == '')  {
                    label = Section.GetCurrentSectionLabel();
                    if(label == '') {
                        dbg().logFatalError(source, 'Item is speech enabled but no speechLabel, element label, or enclosing section label is specified for ' + this.getName(), true);
                        return;
                    }
                }
            }
            buff.push(Simulator.Constants.SPEECH_LABEL_PREFIX + label); buff.push('<br>');
        }
        else 
*/            
        
        if(this.getLabel()) {
        	var textLabelEl = simDocument().createTextNode(this.getLabel());
        	panelHtml.appendChild(textLabelEl);
        	var brElement = simDocument().createElement('br');
        	panelHtml.appendChild(brElement);
        }
        
        
        var ulElement = null;
        for ( var x = 0; x < items.length; x++) {
            var itemIDReset = x == 0 ? true : false;
            itemID = this.createItemID(itemIDReset);
            items[x]['itemID'] = itemID;
            image = (items[x]).lookup('image');
            if(x == 0) {
                if (image != undefined && image != null) {
                	ulElement = simDocument().createElement('ul');
                	ulElement.id = nodeID;
                	ulElement.setAttribute('class', 'singleSelect withImages');
                } else {
                	ulElement = simDocument().createElement('ul');
                	ulElement.id = nodeID;
                	ulElement.setAttribute('class', 'singleSelect');
                }
                panelHtml.appendChild(ulElement);
            }
            if ((items[x]).lookup('val') != undefined) {
            	var listElement = simDocument().createElement('li');
            	var inputEl = simDocument().createElement('input');
            	inputEl.id = itemID;
            	inputEl.setAttribute('type', 'radio');
            	inputEl.setAttribute('name', 'RadioButtonGroup' + nodeID + nextNum);
            	inputEl.setAttribute('value', (items[x]).lookup('val'));
            	if (this.getSaveOnChange()) {
                    utils().bindEvent(inputEl, 'click', function () {
                        that.doOnClick(nodeID);
                    });
            	}
            	if(items[x].lookup('default') == 'yes') {
            		inputEl.setAttribute('checked', '');
            	}
            	listElement.appendChild(inputEl);
            	var labelForID = 'labelFor' + itemID;
            	items[x].setValue('labelForID', labelForID);
            	var elementLabel = simDocument().createElement('label');
            	elementLabel.id = labelForID;
            	elementLabel.setAttribute('for', itemID);
            	if(items[x].lookup('default') == 'yes') {
            	    elementLabel.setAttribute('class', 'inputChecked');
            	}
            	if (image != undefined && image != null) {
            		var imageSpanEl = simDocument().createElement('span');
            		imageSpanEl.setAttribute('class', 'holderImage');
            		var imageEl = simDocument().createElement('img');
            		imageEl.setAttribute('src', image);
            		imageEl.setAttribute('alt', (items[x]).lookup('val'));
            		imageSpanEl.appendChild(imageEl);
            		elementLabel.appendChild(imageSpanEl);
            	}
            	var listLabelSpanEl = simDocument().createElement('span');
            	listLabelSpanEl.setAttribute('class', 'listLabel');
            	listLabelSpanEl.innerHTML = (items[x]).lookup('val');
            	elementLabel.appendChild(listLabelSpanEl);
            	listElement.appendChild(elementLabel);
            	ulElement.appendChild(listElement);
            }

            if(this.isFocusable()) {
            	// Register the element items for keyboard input
            	keyboardInput().addFocusableElementItem(this, nodeID, itemID);
            }
        }
        
        this.setDefaultSelections();
        this.mapHTML2JS(ulElement);
    };
    
    this.getSourceName = function() {
        return source;
    };


    this.inspect = function(embedded, forced) {
        var buff = [];
        var sep = '\n\n';
        if(!embedded) {
            buff.push('Inspecting ');
            buff.push(this.getName());
            buff.push(sep);
        }
        for ( var i in this) {
            if (i == 'items') {
                buff.push(this.parent.inspect());
            } else {
                if (i.substr(0, 3) == 'get') {
                    buff.push(i);
                    buff.push(' = ');
                    buff.push(eval('this.' + i + '()'));
                    buff.push(sep);
                }
            }
        }
        if(!embedded) forced === true ? dbg().debug(buff.join('')) : dbg().debug(buff.join(''));
        else return buff.join('');
    };

    // Convenience function for the most frequently used Debug methods
    var debug = function(str1, str2, trace) {
        dbg().debug(source, str1, str2, trace);
    };

    var debugf = function(str1, str2, trace) {
        dbg().debugf(source, str1, str2, trace);
    };

};

// Inherit methods and class variables
Simulator.Input.OptionList.prototype = new Simulator.Input.GroupList();
Simulator.Input.OptionList.parent = Simulator.Input.GroupList;
Simulator.Input.OptionList.prototype.constructor = Simulator.Input.OptionList; // Reset the prototype to point to the current class