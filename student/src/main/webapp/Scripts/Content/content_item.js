var ContentItem = function(page, bankKey, itemKey, filePath, format, responseType, grade, subject, position) {
    
    this.format = format;
    this.responseType = responseType;
    this.grade = grade;
    this.subject = subject;
    this.position = position;
    this.isDirty = false;
    this.value = null;

    // TTS
    this.stemTTS = null;
    this.illustrationTTS = null;

    // content
    this.gridAnswerSpace = null;
    this.tutorial = null;
    this.gtr = null;

    // save options
    this.saveOptions =
    {
        auto: false,
        explicit: false,
        implicit: false
    };

    ContentItem.superclass.constructor.call(this, page, bankKey, itemKey, filePath);
};

YAHOO.lang.extend(ContentItem, ContentEntity);

ContentItem.prototype.getParentPage = function() { return this._page; };

ContentItem.prototype.getID = function() {
    return 'I-' + this.bankKey + '-' + this.itemKey;
};

// this function gets called when a new instance of content item is created
ContentItem.prototype.init = function() {
    
    var format = this.format.toLowerCase();
    var type = this.responseType.toLowerCase();

    // configure save options
    // set saving options
    // allowImplicitSave: allow navigate away and save automatically
    // allowExplicitSave: allow save button to show
    if (type == 'grid' || type == 'simulator' || type == 'scratchpad') {
        this.saveOptions.auto = false;
        this.saveOptions.implicit = true;
        this.saveOptions.explicit = true;
    } else if (type == 'microphone') {
        this.saveOptions.auto = false;
        this.saveOptions.implicit = true;
        this.saveOptions.explicit = true;
    } else if (format == 'mc' || format == 'si' /* scoring entry */) {
        this.saveOptions.auto = false;
        this.saveOptions.implicit = false;
        this.saveOptions.explicit = false;
    } else if (format == 'eq' /* equation editor */) {
        this.saveOptions.auto = false;
        this.saveOptions.implicit = true;
        this.saveOptions.explicit = true;
    } else if (format == 'asi' /* scaffolding */) {
        this.saveOptions.auto = false;
        this.saveOptions.implicit = true;
        this.saveOptions.explicit = false;
    } else if (type == 'na') { /* no response type specified so nothing to save */
        this.saveOptions.auto = false;
        this.saveOptions.implicit = false;
        this.saveOptions.explicit = false;
    } else {
        this.saveOptions.auto = true;
        this.saveOptions.implicit = true;
        this.saveOptions.explicit = true;
    }
};

// check if read-only is enabled for this item
ContentItem.prototype.isReadOnly = function() {
    return ContentManager.isReadOnly();
};

ContentItem.prototype.isResponseType = function(type) {
    return type.toLowerCase() == this.responseType.toLowerCase();
};

// get item container element
ContentItem.prototype.getElement = function() {

    // if we used compound layout we want to get the wrapper used on stem/items
    var compoundEl = this._page.getCompoundElement();
    if (compoundEl) {
        return compoundEl;
    } else { // return the item container div
        var doc = this._page.getDoc();
        if (doc) {
            return doc.getElementById('Item_' + this.position);
        }
    }

    return null;
};

// get item illustration element
ContentItem.prototype.getIllustrationElement = function() {
    var doc = this._page.getDoc();
    return doc ? doc.getElementById('Illustration_' + this.position) : null;
};

// get item stem element
ContentItem.prototype.getStemElement = function() {
    var doc = this._page.getDoc();
    return doc ? doc.getElementById('Stem_' + this.position) : null;
};

// get the response container
// Bug 113503 return an array of elements instead of container
ContentItem.prototype.getResponseArea = function() {

    var doc = this._page.getDoc();
    if (!doc) return null;

    // Commenting this out for now since this is interfering with the tags that the taggers have provided. 
    // They have added tagged descriptions for these to the end of the stem so TTS attempting to read these new elements causes problems
    /*
    // check for response types we know about
    var responseType = this.responseType.toLowerCase();
    switch (responseType) {
        case 'tableinput':
            return doc.getElementById('TableContainer_' + this.position);
        case 'tablematch':
            return doc.getElementById('MatchContainer_' + this.position);
        case 'simulator':
            return doc.getElementById('SimContainer_' + this.position);
    }
    */

    var pageEl = this._page.getElement();

    // check for mc parent
    if (this.MC) {
        var options = this.MC.getOptions();
        var optionElements = [];
        if (options && options.length > 0) {
            for (var i = 0; i < options.length; i++) {
                var optionEl = options[i].getElement();
                if (optionEl) {
                    optionElements.push(optionEl);
                }
            }
        }
        return optionElements;
    }

    return null;
};

// get this items response, this will call into the widget
ContentItem.prototype.getResponse = function() {
    
    // get response types handler (needs to be registered by a widget)
    var responseHandler = ContentManager.getResponseHandler(this.responseType);

    // if there is no handler then just return the empty response
    if (responseHandler == null || responseHandler.getter == null) {
        return null;
    }

    // create a response object that the widget will use to set
    var response =
    {
        value: null, // response value from student
        isSelected: false, // is this selected
        isValid: false, // is this considered a valid response
        isAvailable: false // is the item in a valid state to get its response
    };

    // call response getter
    responseHandler.getter(this, response);

    return response;
};

// set this items response, this will call into the widget
ContentItem.prototype.setResponse = function(value) {
    
    // get response types handler (needs to be registered by a widget)
    var responseHandler = ContentManager.getResponseHandler(this.responseType);

    // if there is no handler then just return the empty response
    if (responseHandler == null || responseHandler.setter == null) {
        return false;
    }

    // call response setter
    return responseHandler.setter(this, value);
};

// check if this item is supported on this browser/platform
ContentItem.prototype.isSupported = function() {

    var supportedHandler = ContentManager.getSupportHandler(this.responseType);

    // check for handler    
    if (typeof supportedHandler == 'function') {
        return supportedHandler(this);
    }
    
    // if there is no handler then assume we are supported
    return true;
};

// call this function when setting removing focus on an item
ContentItem.prototype.clearActive = function() {
    var itemElement = this.getElement();

    // remove css
    YUD.removeClass(itemElement, 'activeItem');

    ContentItem.superclass.clearActive.call(this);
};

// this gets called when making an item active
ContentItem.prototype.setActive = function(domEvent, force) { // parameters optional
    
    var activated = ContentItem.superclass.setActive.call(this, domEvent, force);

    // add css
    if (activated) {
        var element = this.getElement();
        YUD.addClass(element, 'activeItem');
    }

    // set focus to question element container
    // if (activated) ContentManager.focus(element);

    // only scroll the question into view if using keyboard
    // if (typeof (domEvent) == 'undefined') element.scrollIntoView(true);
};

ContentItem.prototype.showFeedback = function() {
    var itemEl = this.getElement();
    if (itemEl) {
        YUD.addClass(itemEl, 'showFeedback');
    }
};

ContentItem.prototype.hideFeedback = function() {
    var itemEl = this.getElement();
    if (itemEl) {
        YUD.removeClass(itemEl, 'showFeedback');
    }
};

ContentItem.prototype._log = function(message) {
    if (ContentManager._debug) {
        ContentManager.log('ITEM I-' + this.bankKey + '-' + this.itemKey + ' (' + this.position + '): ' + message);
    }
};

ContentItem.prototype.toString = function() {
    return 'Item I-' + this.bankKey + '-' + this.itemKey;
};