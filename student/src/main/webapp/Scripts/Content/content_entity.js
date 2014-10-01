var ContentEntity = function(page, bankKey, itemKey, filePath) {
    this._page = page;
    this.bankKey = bankKey;
    this.itemKey = itemKey;
    this.filePath = filePath;
    this.audioLinks = [];
    this.printed = false;

    // TODO: replace with ID's
    this._components = []; // collection of DOM elements that make up the components of the entity
    this._activeComponent = null; // the currently focused DOM element

    this.init();
};

ContentEntity.prototype.init = function() {};

ContentEntity.prototype.getElement = function() {
    return null;
};

// get the elements plain text (no html)
ContentEntity.prototype.getText = function() {
    var el = this.getElement();
    if (el) {
        var text = Util.Dom.getTextContent(el);
        text = YAHOO.lang.trim(text);
        return text;
    }
    return '';
};

// does this entity have any content
ContentEntity.prototype.isEmpty = function() {
    return (this.getText().length == 0);
};

// get this entity parent page
ContentEntity.prototype.getPage = function() {
    return this._page;
};

// get a resource types bank/item keys
ContentEntity.prototype.getResource = function(type) {
    return (this.resources) ? this.resources[type] : null;
};

// is this entity visible in the DOM
ContentEntity.prototype.isVisible = function() {
    var element = this.getElement();
    return Util.Dom.isVisible(element);
};

// check if this entity is active (focused)
ContentEntity.prototype.isActive = function() {
    return this == this._page._activeEntity;
};

// remove focus
ContentEntity.prototype.clearActive = function() {
    var activeEntity = this._page.getActiveEntity();

    // check if the page we are blurring really is focused
    if (activeEntity && activeEntity == this) {
        this._log('blur entity');

        var element = activeEntity.getElement();
        ContentManager.blur(element);

        this._page._activeEntity = null;
        ContentManager.fireEntityEvent('blur', this);
        return true;
    }

    return false;
};

// make this entity active 
// (set force to true to force the focus regardless if it already focused)
ContentEntity.prototype.setActive = function(domEvent, force) // parameters optional
{
    // check if this entity is already active
    var activeEntity = this._page.getActiveEntity() || null;
    if (!force && this == activeEntity) {
        return false;
    }

    // clear active entity
    this._page.clearEntity();

    this._log('focus entity');

    // set new entity
    this._page._activeEntity = this;
    this._page._lastEntity = this;

    // focus
    this.focus();

    // scroll (only if key shortcut was used)
    if (typeof(domEvent) == 'undefined') {
        this.scrollTo();
    }

    // fire event
    ContentManager.fireEntityEvent('focus', this /* current */, activeEntity /* previous */);
    return true;
};

// this sets focus where we can use up/down
ContentEntity.prototype.focus = function() {
    // then try and set focus on the element (probably won't work)
    var entityElement = this.getElement();
    ContentManager.focus(entityElement);
};

// this scrolls to the item/passage so it is in view
ContentEntity.prototype.scrollTo = function() {
    var element = this.getElement();
    // var container = this._page.getScrollableElement();

    // TODO: Switch to the new scroll into view function which bases scrolling on a container:
    // Util.Style.scrollIntoContainerView(element, container);
    element.scrollIntoView(false);
};

// add a component
ContentEntity.prototype.addComponent = function(element) {
    if (element == null) {
        return false;
    }
    this._components.push(element); // BUG: When reloading page a user got "this._components is null"
    return true;
};

// get all the components
ContentEntity.prototype.getComponents = function(visible) {
    var components = this._components;

    // filter for visible components only
    if (visible) {
        components = Util.Array.filter(components, function(component) {
            if (YAHOO.lang.isFunction(component.isVisible)) {
                return component.isVisible();
            } else if (Util.Dom.isElement(component)) {
                return Util.Dom.isVisible(component);
            }

            return true;
        });
    }

    return components;
};

// look to see if an element is a child of one of the components of this entity
ContentEntity.prototype.findComponent = function(element) {
    return ContentManager.getAncestor(element, this._components);
};

// get the currently focused component
ContentEntity.prototype.getActiveComponent = function() {
    return this._activeComponent;
};

// set an entities component as being active
ContentEntity.prototype.setActiveComponent = function(component, force) {
    // check if component is already focused
    if (!force && this._activeComponent == component) {
        return;
    }

    // remove current component
    if (this._activeComponent != null) {
        this._log('blur component - \'' + this._activeComponent.id + '\'');

        // remove focus from the active component (blur)
        ContentManager.blur(this._activeComponent);

        // fire component focus event
        ContentManager.fireComponentEvent('blur', this, this._activeComponent);

        this._activeComponent = null;
    }
    
    // check if there is any element
    if (component == null) {
        return;
    }

    // make sure this entity has focus
    if (!this.isActive()) {
        throw new Error('The entity must have focus before setting a component.');
    }

    // make sure this component belongs to this entity
    var components = this.getComponents();

    var componentFound = false;

    for (var i = 0; i < components.length; i++) {
        if (component == components[i]) {
            componentFound = true;
            break;
        }
    }

    if (!componentFound) {
        throw new Error('Component not found for this item.');
    }

    // set element to active
    this._activeComponent = component;

    this._log('focus component - \'' + this._activeComponent.id + '\'');

    // set focus on the page frame (removes focus from test shell)
    // BUG #16928: On Linux applet steals focus and if this code is called it cannot be returned
    if (TDS.Audio.Java.isReady()) {
        // focus on content window as long as there is no audio applet
        ContentManager.focus(this.getPage().getWin());
    }

    // try to set focus on the component
    ContentManager.focus(this._activeComponent);

    // fire component focus event
    ContentManager.fireComponentEvent('focus', this, this._activeComponent);
};

// set the focus to the previous component
ContentEntity.prototype.prevComponent = function() {
    var components = this.getComponents(true);
    var focusedComponent = this.getActiveComponent();

    if (focusedComponent == null) {
        focusedComponent = components[components.length - 1];
    } else {
        for (var i = 0; i < components.length; i++) {
            var component = components[i];

            if (component == focusedComponent) {
                focusedComponent = components[i - 1] || components[components.length - 1];
                break;
            }
        }
    }

    this.setActiveComponent(focusedComponent);
    return focusedComponent;
};

// set the focus to the next component
ContentEntity.prototype.nextComponent = function() {
    var components = this.getComponents(true);
    var focusedComponent = this.getActiveComponent();

    if (focusedComponent == null) {
        focusedComponent = components[0];
    } else {
        for (var i = 0; i < components.length; i++) {
            var component = components[i];

            if (component == focusedComponent) {
                focusedComponent = components[i + 1] || components[0];
                break;
            }
        }
    }

    this.setActiveComponent(focusedComponent);
    return focusedComponent;
};

// clear the active component
ContentEntity.prototype.clearComponent = function() {
    this.setActiveComponent(null);
};

// set the active component to the first one
ContentEntity.prototype.resetComponent = function() {
    this.setActiveComponent(this._components[0]);
};

// set the caret position on the active components first text node
ContentEntity.prototype.resetCaretPosition = function() {
    var page = this.getPage();
    var pageWin = page.getWin();
    var pageDoc = page.getDoc();
    var element = this.getActiveComponent();

    // make sure the component is an element
    if (!ContentManager.isElement(element)) {
        return false;
    }

    // make sure the element is in this pages document
    if (!YUD.inDocument(element, pageDoc)) {
        return false;
    }

    // set focus on the page window and then the element
    ContentManager.focus(pageWin);
    // ContentManager.focus(element);

    // get all text nodes of this element
    var xPathResult = pageDoc.evaluate(".//text()", element, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);

    var textNode = null;

    // iterate on all the text nodes and find one that has some text
    for (var i = 0, l = xPathResult.snapshotLength; i < l; i++) {
        var node = xPathResult.snapshotItem(i);
        var text = node.textContent;

        // remove whitespace
        text = text.replace(/\s+/g, ' '); // replace
        text = text.replace(/^\s+|\s+$/g, ''); // trim

        textNode = node;

        // if text was found then stop here
        if (text.length > 0) {
            break;
        }
    }

    // make sure there was a textnode found
    if (!textNode) {
        return false;
    }

    // set cursor on first text node

    // HELPFUL:
    // * http://stackoverflow.com/questions/1181700/set-cursor-position-on-contenteditable-div
    // * http://www.timdown.co.uk/code/selections/selectionutil.js
    var selection = pageWin.getSelection();
    selection.removeAllRanges();

    // set the range on the text node
    var range = pageDoc.createRange();
    //range.selectNode(textNode);
    range.setStart(textNode, 0);
    range.setEnd(textNode, 0);

    // make browser selection of text range
    selection.addRange(range);

    // collapse range so there is no selection and caret is at the beginning of the text
    selection.collapseToStart();

    return true;
};

ContentEntity.prototype.dispose = function() {
    this._activeComponent = null;

    if (this._components) {
        for (var i = 0; i < this._components.length; i++) {
            if (this._components[i].dispose) {
                this._components[i].dispose();
            }

            delete this._components[i];
        }
    }

    this._components = null;

    this._page = null;
    this.bankKey = null;
    this.itemKey = null;
    this.audioLinks = null; // TODO: dispose these?
};