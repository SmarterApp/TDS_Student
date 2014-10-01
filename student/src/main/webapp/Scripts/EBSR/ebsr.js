// Collection of MC/MS items
var EBSR = function (xmlString, item) {
    this._xmlString = xmlString;
    var ebsrItem = this;
    this._position = item.position;
    

    // Parse choiceInteraction XML tags into array
    var nodes = Util.Xml.parseFromString(xmlString);
    var docElement = nodes.documentElement;
    this.interactionsXml = docElement.getElementsByTagName('choiceInteraction');
    this._interactions = [];
    this._interactionHash = {};

    $.each(this.interactionsXml, function (i, interactionXml) {
        // Set up the Option processing class based on interaction type
        var contentOptionClass = [];
        contentOptionClass["MS"] = EBSR.MSOption;
        contentOptionClass["MC"] = EBSR.MCOption;

        // parse each interaction
        var interaction = new EBSR.QTI(interactionXml, item.position, contentOptionClass, i + 1);
        // overload the getItem method, returning the EBSR item
        interaction.options.getItem = function () {
            return item;
        };

        // add the processed MC/MS interaction to the EBSR item
        ebsrItem.addInteraction(interaction);
    });

};

// Add a choiceInteraction (either an MC or MS sub-item) to the EBSR item
EBSR.prototype.addInteraction = function(interaction) {
    // store interaction
    this._interactions.push(interaction);
    this._interactionHash[interaction.responseId] = interaction;
};

// get all choiceInteractions for this EBSR item
EBSR.prototype.getInteractions = function() {
    return this._interactions;
};

// get prompt nodes for all interactions in this EBSR item
EBSR.prototype.getPrompts = function() {
    var interactions = this.getInteractions();
    var prompts = [];
    Util.Array.each(interactions, function(interaction) {
        var prompt = interaction.getPrompt();
        if (prompt && prompt.length == 1) {
            prompts.push(prompt[0]);
        }
    });
    return prompts;
};

// Get an interaction based on it's key
EBSR.prototype.getInteractionByKey = function (key) {
    var interactions = this.getInteractions();
    var targetInteraction = {};
    Util.Array.each(interactions, function(interaction) {
        if (interaction.key == key) {
           targetInteraction = interaction;
        }
    });
    return targetInteraction;
};

// Get an interaction based on it's responseId
EBSR.prototype.getInteraction = function (key) {
    return this._interactionHash[key];
};

// Traverse all interactions to find the option that matches the focusedComponent
EBSR.prototype.getFocusedOption = function (focusedComponent) {
    for (var i = 0; i< this._interactions.length; i++) {
        var options = this._interactions[i].getOptions();
        for (var j = 0; j < options.length; j++) {
            var option = options[j];
            if (option.getElement() == focusedComponent) return option;
        }
    }
};


// Remove all <choiceInteraction> tags, and place remaining content into the Stem
// NOTE: At time of writing, ITS had no plans for any structures outside of the <choiceInteraction> tags
EBSR.prototype.populateStem = function () {

    // Get the contents from XML
    if (this._xmlString == null) return;
    var xmlDoc = Util.Xml.parseFromString(this._xmlString);
    var docEl = xmlDoc.documentElement;

    // Remove the interactions, leaving the contents of the stem
    var stemContent = $(docEl).children().not('choiceInteraction');

    // Populate the stem with contents
    $("#Stem_" + this._position).html(stemContent.contents());
};

// Generate the HTML DOM structure for the EBSR item, and all sub-item interactions/options
EBSR.prototype.generateHTML = function (answerContainer) {
    var interactions = this.getInteractions();

    // parse each interaction into HTML
    Util.Array.each(interactions, function (interaction) {
        interaction.generateInteractionHTML(answerContainer);
    });
};

//  Compile the list of components for all sub-items for keyboard navigation
EBSR.prototype.getAllComponentLists = function () {
    var componentList = [];
    var interactions = this.getInteractions();

    // fetch componentList for each interaction
    Util.Array.each(interactions, function (interaction) {
        var interactionComponentList = interaction.getComponentList();
        componentList = componentList.concat(interactionComponentList);
    });

    return componentList;
};

// Compile the list of radio buttons for all sub-items
EBSR.prototype.getAllRadioButtons = function () {
    var radioButtons = [];
    var interactions = this.getInteractions();

    // fetch componentList for each interaction
    Util.Array.each(interactions, function (interaction) {
        var interactionRadioButtons = interaction.getRadioButtons();
        radioButtons = radioButtons.concat(interactionRadioButtons);
    });

    return radioButtons;
};


// Preset the answer to item provided in 'value'
EBSR.prototype.setValue = function (value) {

    // make sure valid response
    if (typeof value != 'string' || 
        value.indexOf('<itemResponse>') == -1) {
        return false;
    }

    var ebsr = this;

    // Parse choiceInteraction XML tags into array
    var nodes = Util.Xml.parseFromString(value);
    var docElement = nodes.documentElement;

    // iterate over <response> nodes
    $('response', docElement).each(function (responseIdx, responseNode) {
        var responseId = responseNode.getAttribute('id');
        var interaction = ebsr.getInteraction(responseId);
        if (interaction && interaction.options) {
            interaction.options.clear(); // Bug 112020 - clear selections before loading new values
            // iterate over <value> nodes in a <response>
            $('value', responseNode).each(function (valueIdx, valueNode) {
                var optionId = $(valueNode).text();
                if (optionId) {
                    // select the option key
                    var option = interaction.options.getOption(optionId);
                    if (option) {
                        option.select(true);
                    }
                }
            });
        }
    });

    return true;
};

// Create the response XML document
// TODO: handle shuffle.  ITS doesn't currently support shuffle
EBSR.prototype.getResponse = function() {
    // build response xml with all interactions
    var responseEl = ['<itemResponse>'];

    var interactions = this.getInteractions();
    var createSelectedNode = function (selected) {
        if (selected) {
            responseEl.push('<value>');
            responseEl.push(selected);
            responseEl.push('</value>');
        }
    };

    Util.Array.each(interactions, function(interaction) {
        // build group node
        responseEl.push('<response id="' + interaction.responseId + '">');

        // build response node
        var selected = interaction.options.getSelected();

        if (interaction.type.toUpperCase() == "MS") {
            for (var i = 0; i < selected.length; i++) {
                createSelectedNode(selected[i]);
            }
        } else {
            createSelectedNode(selected);
        }
        responseEl.push('</response>');
    });

    responseEl.push('</itemResponse>');
    return responseEl.join('');
};

// Has the EBSR item been answered?  All sub-items must have a option selected
// TODO: validate min/maxChoice constraints
EBSR.prototype.isValid = function() {
    var interactions = this.getInteractions();
    var response = true;
    Util.Array.each(interactions, function (interaction) {
        var selected = interaction.options.getSelected();
        // Bug 112670 MS interactions return selected = [], MC return selected = null, check both
        if (!selected || selected.length == 0) {
            response = false;
        }
    });
    return response;
};


/**********************/
/*EBSR MC OPTION      */
/**********************/

// Single EBSR MC option
EBSR.MCOption = function (options, key, interactionKey) {
    EBSR.MCOption.superclass.constructor.call(this, options, key);
    this._interactionKey = interactionKey;
};

YAHOO.lang.extend(EBSR.MCOption, ContentMCOption);

// get the container div around the option
EBSR.MCOption.prototype.getElement = function () {
    var item = this._options.getItem();
    var doc = item.getPage().getDoc();
    return doc.getElementById('Item_OptionContainer_Response_EBSR_' +
        item.position + '_Part' + this._interactionKey + '_' + this.key);
};

// get form radio group
EBSR.MCOption.prototype.getRadioGroup = function () {
    var item = this._options.getItem();
    var form = item.getPage().getForm();
    return form['Item_Response_EBSR_' + item.position + '_Part' + this._interactionKey];
};

// get form radio button element
EBSR.MCOption.prototype.getRadioButton = function () {
    var item = this._options.getItem();
    var form = item.getPage().getForm();
    return form['Item_Response_EBSR_' + item.position + '_Part' + this._interactionKey +
        '_' + this.key];
};

// get sound anchor tag element
EBSR.MCOption.prototype.getSoundLink = function () {
    var item = this._options.getItem();
    var doc = item.getPage().getDoc();

    var soundDIV = doc.getElementById('Item_OptionSound_Response_EBSR_' + item.position +
        '_Part' + this._interactionKey + '_' + this.key);
    if (soundDIV == null) return null;

    var soundLink = soundDIV.getElementsByTagName('a')[0];
    return soundLink;
};

EBSR.MCOption.prototype.select = function (force) {
    // get the current selected MC option
    var currentSelection = this._options.getSelected();

    // if this is already selected then return
    if (currentSelection == this && force !== true) {
        return false;
    }

    // clear current selection css
    if (force) {
        var options = this._options.getOptions();
        Util.Array.each(options, function (option) {
            option.deselect();
        });
    }
    else if (currentSelection) {
        currentSelection.deselect();
    }

    // mark radio button as checked (this also removes the current selection)
    var radioButton = this.getRadioButton();

    if (!radioButton.checked) {
        radioButton.checked = true;
    }

    // add selected css
    $(this.getElement()).addClass('optionSelected');
    //YUD.addClass(this.getElement(), 'optionSelected');

    // show feedback
    var page = this._options.getItem().getPage();
    var pageAccProps = page.getAccommodationProperties();
    if (pageAccProps != null && pageAccProps.showFeedback()) this.showFeedback();

    return true;
};

/**********************/
/* EBSR MS OPTION     */
/**********************/

EBSR.MSOption = function (options, key, interactionKey) {
    EBSR.MSOption.superclass.constructor.call(this, options, key, interactionKey);
};

YAHOO.lang.extend(EBSR.MSOption, EBSR.MCOption);

EBSR.MSOption.prototype.select = function (force) {
    // toggle checkbox
    var checkbox = this.getRadioButton();

    //Bug 112020 - Toggle option selection if not forced to select
    if (checkbox.checked && !force) {
        // deselect
        this.deselect();
    } else {
        // select
        checkbox.checked = true;

        // add selected css
        $(this.getElement()).addClass('optionSelected');
        //YUD.addClass(this.getElement(), 'optionSelected');

        // show feedback
        var page = this._options.getItem().getPage();
        var pageAccProps = page.getAccommodationProperties();
        if (pageAccProps != null && pageAccProps.showFeedback()) this.showFeedback();
    }

    return true;
};
