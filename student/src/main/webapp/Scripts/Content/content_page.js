/* Init */

var ContentPage = function(content) {
    
    this.id = content.id;
    this.segmentID = content.segmentID;
    this.layout = content.layout; // layout name
    this.soundCue = content.soundCue;
    this.autoPlayQueue = TDS.Audio.Player.createQueue();

    this._html = content.html;
    this._container = null;
    this._doc = null;

    this._passage = null;
    this._items = [];
    this._itemHash = {};
    this._activeEntity = null; // the focused entity
    this._zoom = null; // zooming
    this._lastEntity = null; // last entity that was shown for this page

    // images
    this._imagesLoading = []; // images currently waiting to load
    this._imagesLoaded = []; // images that loaded successfully
    this._imagesFailed = []; // images that failed to load
    this._imagesAborted = []; // images that got aborted or timed out
    this._imagesTimer = null; // timer object for waiting on images
    this._imagesRetry = 0; // how many times a timeout occured and we retried

    // resource loader
    this._resourceCollection = new ResourceLoader.Collection();

    // status info
    this._status = ContentPage.State.Init;
    this._statusNames = {};
    this._statusNames[ContentPage.State.Init] = 'init';
    this._statusNames[ContentPage.State.Rendering] = 'rendering';
    this._statusNames[ContentPage.State.Rendered] = 'rendered';
    this._statusNames[ContentPage.State.Available] = 'available';
    this._statusNames[ContentPage.State.Loaded] = 'loaded';

    this._enableScroll = true;
};

// # of seconds until image times out
ContentPage.imageTimeout = 90;

// how many times we can retry loading images
ContentPage.imageMaxRetry = 2;

ContentPage.State = {
    Init: 0, // This is the default state. Object is created and the content HTML is ready to render.
    Rendering: 1, // Right before we write the content HTML to the DOM.
    Rendered: 2, // The content HTML has finished writing but the DOM might not be ready to access yet.
    Available: 3, // The content HTML is now ready in the DOM to be accessed.
    Loaded: 4 // All the resources in the HTML (e.x. images) have been loaded and content is ready to view.
};

ContentPage.prototype.getState = function() {
    return this._status;
};

ContentPage.prototype.setState = function(newState) {
    // check if valid page status
    if (!YAHOO.lang.isNumber(newState) ||
        newState < ContentPage.State.Init ||
        newState > ContentPage.State.Loaded) {
        throw 'Invalid page state';
    }

    // get current status
    var currentState = this.getState();

    // get the string names of the status's
    var currentStateName = this._statusNames[currentState];
    var newStateName = this._statusNames[newState];

    // check if page status string name was found
    if (newStateName == null) {
        throw 'No page state name found';
    }

    // check if this is a valid status transition
    var statusError = 'Invalid page state transition (' + currentStateName + ' --> ' + newStateName + ')';
    if (newState == ContentPage.State.Init && currentState >= ContentPage.State.Init) {
        throw statusError;
    } else if (newState == ContentPage.State.Rendering && 
               currentState != ContentPage.State.Init) {
        throw statusError;
    } else if (newState == ContentPage.State.Rendered && 
               currentState != ContentPage.State.Rendering) {
        throw statusError;
    } else if (newState == ContentPage.State.Available && 
               currentState != ContentPage.State.Rendered) {
        throw statusError;
    } else if (newState == ContentPage.State.Loaded && 
               currentState != ContentPage.State.Available) {
        throw statusError;
    }

    // set new status
    this._status = newState;

    this._log(newStateName);
    ContentManager.firePageEvent(newStateName, this, null, true);
};

// get the HTML used to render the page
ContentPage.prototype.getHtml = function() { return this._html; };

// set the HTML used to render the page
ContentPage.prototype.setHtml = function(html) {

    if (this.getState() <= ContentPage.State.Rendering) {
        this._html = html;
        return true;
    }

    return false;
};

// gets the container for the page, this is what we hide/show
ContentPage.prototype.getContainer = function() {
    return this._container;
};

// get pages document element
ContentPage.prototype.getDoc = function() {
    return this._doc;
};

// get the pages current active components document
ContentPage.prototype.getActiveDoc = function() {
    
    // get current entity (item or passage)
    var activeEntity = this.getActiveEntity();

    if (activeEntity != null) {
        // get current component of entity
        var activeComponent = activeEntity.getActiveComponent();

        // check if component has a getDoc() function
        if (activeComponent != null && YAHOO.lang.isFunction(activeComponent.getDoc)) {
            return activeComponent.getDoc();
        }
    }

    return this.getDoc();
};

// get pages body element
ContentPage.prototype.getBody = function() {
    var doc = this.getDoc();
    return doc ? doc.body : null;
};

// get pages form
ContentPage.prototype.getForm = function() {
    var doc = this.getDoc();
    return doc ? doc.forms['contentForm'] : null;
};

// get the pages current window
ContentPage.prototype.getWin = function() {
    var doc = this.getDoc();
    return doc ? (doc.parentWindow || doc.defaultView) : null;
};

// get the pages current active components window
ContentPage.prototype.getActiveWin = function() {
    // get current entity (item or passage)
    var activeEntity = this.getActiveEntity();

    if (activeEntity != null) {
        // get current component of entity
        var activeComponent = activeEntity.getActiveComponent();

        // check if component has a getWin() function
        if (activeComponent != null && YAHOO.lang.isFunction(activeComponent.getWin)) {
            return activeComponent.getWin();
        }
    }

    // return pages window
    return this.getWin();
};

// get the pages iframe container (<iframe> element) if there is one
ContentPage.prototype.getFrameElement = function() {
    var win = this.getWin();
    return win ? win.frameElement : null;
};

// get the container element for this page
ContentPage.prototype.getElement = function() {
    var doc = this.getDoc();
    return doc ? doc.getElementById('Page_' + this.id) : null;
};

// get the compound element container
ContentPage.prototype.getCompoundElement = function() {

    var pageDoc = this.getDoc();
    var items = this.getItems();

    // we check if there is a compound container by looking for a div with 'compound_{firstPos}'
    if (pageDoc && items.length > 0) {
        var firstItem = items[0];
        return pageDoc.getElementById('compound_' + firstItem.position);
    }

    return null;
};

// get the first page header
ContentPage.prototype.getHeader = function() {
    var pageElement = this.getElement();
    var headers = pageElement.getElementsByTagName('h3');

    if (headers.length > 0) {
        return headers[0];
    }
    return null;
};

ContentPage.prototype.getAccommodations = function() {
    return Accommodations.Manager.get(this.segmentID);
};

ContentPage.prototype.getAccs = ContentPage.prototype.getAccommodations;

ContentPage.prototype.getAccommodationProperties = function() {
    return Accommodations.Manager.getProperties(this.segmentID);
};

ContentPage.prototype.getAccProps = ContentPage.prototype.getAccommodationProperties;

ContentPage.prototype.zoomIn = function() {
    return this._zoom.zoomIn();
};

ContentPage.prototype.zoomOut = function() {
    return this._zoom.zoomOut();
};

ContentPage.prototype.getZoom = function() {
    return this._zoom;
};

ContentPage.prototype.getZoomFactor = function() {
    var zoomInfo = this.getZoom();
    return (zoomInfo == null) ? 1 : zoomInfo.levels[zoomInfo.currentLevel].factor;
};

// set the passage from the frames its.passage object
ContentPage.prototype.setPassage = function(passage) {
    this._passage = passage;
};

// get the passage
ContentPage.prototype.getPassage = function(visible) {
    // check if passage is visible
    if (visible && this._passage && !this._passage.isVisible()) {
        return null;
    }
    return this._passage;
};

// add a item from the frames its.items[] object
ContentPage.prototype.addItem = function(item) {
    this._items.push(item);
    this._itemHash[item.position] = item;
};

// get all the items on this page
ContentPage.prototype.getItems = function(visible) {
    // create a copy of the item array
    var items = this._items.slice(0);

    // filter for visible entities only
    if (visible) {
        items = Util.Array.filter(items, function(item) {
            return item.isVisible();
        });
    }

    return items;
};

// get an item that matches this position
ContentPage.prototype.getItem = function(position) {
    return this._itemHash[position];
};

// get all items and passage in a single array
ContentPage.prototype.getEntities = function(visible) {
    // get all items and passage in array
    var entities = this.getItems(visible);

    var passage = this.getPassage(visible);
    if (passage) {
        entities.push(passage);
    }

    return entities;
};

// get all items and passge in an iterator
ContentPage.prototype.getEntitiesIterator = function(visible) {
    
    var entities = this.getEntities(visible);

    // create iterator and jump to current entity
    var iter = Util.Iterator(entities);
    iter.jumpTo(this.getActiveEntity());
    return iter;
};

// get the current focused entity
ContentPage.prototype.getActiveEntity = function() {
    return this._activeEntity;
};

// clears the current active entity
ContentPage.prototype.clearEntity = function() {
    
    var activeEntity = this.getActiveEntity();
    if (activeEntity == null) {
        return false;
    } // nothing active

    // clear active entity
    activeEntity.clearActive();
    return true;
};

// move to the previous entity
ContentPage.prototype.prevEntity = function() {
    var items = this.getItems(true);
    var currentEntity = this.getActiveEntity();

    // if there is nothing focused on or the focus is on the passage then return the first item
    if (currentEntity == null || currentEntity instanceof ContentPassage) {
        currentEntity = items[items.length - 1];
    } else {
        for (var i = 0; i < items.length; i++) {
            var item = items[i];

            if (currentEntity == item) {
                currentEntity = items[i - 1] || this.getPassage(true) || items[items.length - 1];
                break;
            }
        }
    }

    if (currentEntity) {
        currentEntity.setActive();
        return currentEntity;
    } else {
        return null;
    }
};

// move to the next entity
ContentPage.prototype.nextEntity = function() {
    var items = this.getItems(true);
    var currentEntity = this.getActiveEntity();

    // if there is nothing focused on or the focus is on the passage then return the first item
    if (currentEntity == null || currentEntity instanceof ContentPassage) {
        currentEntity = items[0];
    } else {
        for (var i = 0; i < items.length; i++) {
            var item = items[i];

            if (currentEntity == item) {
                currentEntity = items[i + 1] || this.getPassage(true) || items[0];
                break;
            }
        }
    }

    if (currentEntity) {
        currentEntity.setActive();
        return currentEntity;
    } else {
        return null;
    }
};

/*
// move to the next entity
ContentPage.prototype.nextEntity = function()
{
    var iter = this.getIterator(true);
    iter.next().setActive();
};

ContentPage.prototype.prevEntity = function()
{
    var iter = this.getIterator(true);
    iter.prev().setActive();
};
*/

ContentPage.prototype.isShowing = function() {
    return (this == ContentManager.getCurrentPage());
};

// Collapses the selected text to the start of the selection.
// http://stackoverflow.com/questions/8513368/collapse-selection-to-start-of-selection-not-div
ContentPage.prototype.collapseSelection = function() {
    var pageDoc = this.getActiveDoc();
    var pageSelection = ContentManager.getSelection(pageDoc);    
    if (pageSelection && pageSelection.rangeCount > 0) {
        pageSelection.collapseToStart();
    }
};

// get all the contents images
ContentPage.prototype.getImages = function() {
    var frameDoc = this.getDoc();

    var images = [];

    for (var i = 0; i < frameDoc.images.length; i++) {
        var image = frameDoc.images[i];

        if (image.className == 'Image') {
            images.push(image);
        }
    }

    return images;
};

// get the element that should be used for scrolling this page up/down
ContentPage.prototype.getScrollableElement = function() {
    var pageElement = this.getElement();

    // make sure there is a page element
    if (pageElement != null) {
        var entity = this.getActiveEntity();

        // writing (bug # 31730)
        if (this.writing) {
            return Util.Dom.getElementByClassName('writeWrap', 'div', pageElement);
        }
            // passage
        else if (entity instanceof ContentPassage) {
            return entity.getElement();
        }
            // item
        else if (entity instanceof ContentItem) {
            return Util.Dom.getElementByClassName('theQuestions', 'div', pageElement);
        }
    }

    return null;
};


ContentPage.prototype.enableScroll = function(){
    this._enableScroll = true;
};
ContentPage.prototype.disableScroll = function(){
    this._enableScroll = false;
};

// scroll the page up/down/left/right
ContentPage.prototype.scroll = function(direction) {
    var el = this.getScrollableElement();
    if (el == null || !this._enableScroll) {
        return;
    }

    switch (direction) {
    case 'Up':
        el.scrollTop -= 50;
        break;
    case 'Down':
        el.scrollTop += 50;
        break;
    case 'Left':
        el.scrollLeft -= 100;
        break;
    case 'Right':
        el.scrollLeft += 100;
        break;
    }
};

ContentPage.prototype._log = function(message) {
    if (!ContentManager._debug) {
        return;
    }
    ContentManager.log('PAGE ' + this.id + ': ' + message);
};

ContentPage.prototype.toString = function() { return 'Page ' + this.id; };

/******************************************************************************************/

// call this function to render the pages HTML
ContentPage.prototype.render = function() {
    
    // check if we have already started to render
    if (this.getState() > ContentPage.State.Init) {
        return false;
    }

    // schedule rendering
    YAHOO.lang.later(1, this, function() {
        // set status/fire event
        this.setState(ContentPage.State.Rendering);

        // render content HTML
        ContentManager.Renderer.writePage(this);
    });

    return true;
};

// this function gets called right after the renderer has written the HTML to the DOM
ContentPage.prototype.onRendered = function(pageDoc, pageContainer) {
    // add YUI class to enable widgets
    YUD.addClass(pageDoc.body, 'yui-skin-sam');

    // add touch class
    YUD.addClass(pageDoc.body, Util.Browser.isTouchDevice() ? 'touch' : 'no-touch');

    this._doc = pageDoc;
    this._container = pageContainer;

    // get the element containing the pages content
    var pageElement = this.getElement();

    // this will occur if something got rendered but there was no page found
    if (pageElement == null) {
        throw new Error('The page element could not be found.');
    }

    // prevent the form from being submitted
    var pageForm = this.getForm();
    if (pageForm != null) {
        YUE.on(pageForm, 'submit', function(ev) {
            YUE.stopEvent(ev);
            return false;
        });
    }

    // get all images and check for them to be loaded
    YUD.batch(pageElement.getElementsByTagName('img'), this.addImage, this, true);

    // set status/fire event
    this.setState(ContentPage.State.Rendered);

    // let DOM refresh
    YAHOO.lang.later(1, this, this.onAvailable);
    
    // check if any items are unsupported
    var items = this.getItems();
    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        if (!item.isSupported()) {
            ContentManager.fireEntityEvent('unsupported', item);
        }
    }
};

// this function gets called after writing the HTML to the DOM is done and DOM has been refreshed
ContentPage.prototype.onAvailable = function() {
    // process doc
    ContentManager._processPage(this);

    // set zoom object
    this._zoom = new ContentZoom(this.getDoc(), null, this);

    // get the element containing the pages content
    var pageElement = this.getElement();

    // set status/fire event
    this.setState(ContentPage.State.Available);

    // stop clicks on all page links (help, print, comment, mark review)
    YUD.batch(pageElement.getElementsByTagName('a'), function(link) {
        // disable tab index so it cannot be tabbed to
        if (ContentManager.enableARIA === false) {
            link.setAttribute('tabindex', '-1');

            // disable links from getting focus when clicked
            YUE.on(link, 'mousedown', function(ev) { YUE.stopEvent(ev); });
            YUE.on(link, 'mouseup', function(ev) { YUE.stopEvent(ev); });
        }

        // disable link click so you cannot go the url
        if (!ContentManager.isLinkClickable(link)) {
            YUE.on(link, 'click', function(ev) { YUE.stopEvent(ev); });
        }
    });

    // check if resources that were added
    if (this._resourceCollection.hasLoaders()) {
        // subscribe to collection events and start loading
        this._resourceCollection.subscribe(ResourceLoader.Status.COMPLETE, this.checkLoaded, this);
        this._resourceCollection.subscribe(ResourceLoader.Status.ERROR, this.checkLoaded, this);
        this._resourceCollection.subscribe(ResourceLoader.Status.ABORT, this.checkLoaded, this);
        this._resourceCollection.load();
    }

    // check if page is done loading
    YAHOO.lang.later(1, this, this.checkLoaded);
};

// hide the page
ContentPage.prototype.hide = function() {
    // check if page is already hidden
    if (!this.isShowing()) {
        return false;
    }

    this._log('hide');

    // clear focused page entity
    this.clearEntity();

    // clear current page object
    var currentPage = ContentManager.getCurrentPage();
    if (currentPage == this) {
        ContentManager.setCurrentPage(null);
    }

    // hide container element
    var pageContainer = this.getContainer();

    if (pageContainer) {
        ContentManager.firePageEvent('beforeHide', this, null, true);
        ContentManager.Renderer.hide(pageContainer);
    } else {
        return false;
    }

    // fire hide event
    ContentManager.firePageEvent('hide', this, null, true);

    return true;
};

// show the page (hides current page if one exists)
ContentPage.prototype.show = function() {
    // check if we are already showing this page
    if (this.isShowing()) {
        return false;
    }

    this._log('show');

    // hide current page
    var currentPage = ContentManager.getCurrentPage();
    if (currentPage && currentPage != this) {
        currentPage.hide();
    }

    // show container element
    var pageContainer = this.getContainer();
    if (pageContainer == null) {
        return false;
    }

    // fire before show event
    var cancelShow = ContentManager.firePageEvent('beforeShow', this, null, true);
    if (cancelShow === false) {
        return false;
    } // check if show is cancelled

    // show page html
    ContentManager.Renderer.show(pageContainer);

    // set new page as current
    ContentManager.setCurrentPage(this);

    // see what is good candidate to be first active entity
    var activeEntity = null;

    // last entity viewed
    if (this._lastEntity != null) {
        activeEntity = this._lastEntity;
    } else {
        // get pages entities
        var items = this.getItems(true);
        var passage = this.getPassage(true);

        if (items.length > 0) {
            activeEntity = items[0];
        } // first item
        else if (passage) {
            activeEntity = passage;
        } // passage
    }

    // set entity as active (fires entity focus event)
    if (activeEntity) {
        activeEntity.setActive(null, true);
    }

    // fire show event
    YAHOO.lang.later(1, this, function() {
        ContentManager.firePageEvent('show', this, null, true);
    });

    return true;
};

// call this function when you think the page might be loaded
ContentPage.prototype.checkLoaded = function() {
    // check if there are any images we are still waiting on
    if (this._imagesLoading.length > 0) {
        return;
    }

    // cancel existing timeout
    if (this._imagesTimer != null) {
        this._imagesTimer.cancel();
        this._imagesTimer = null;
    }

    // check if any resources we are still waiting on
    if (this._resourceCollection.hasLoaders() &&
        this._resourceCollection.getStatus() == ResourceLoader.Status.LOADING) {
        return;
    }

    // fire loaded event
    if (this.getState() == ContentPage.State.Available) {
        // set status/fire event
        this.setState(ContentPage.State.Loaded);
    }
};

/****************************************************************************************/

// get resource loaders
ContentPage.prototype.getResourceLoaders = function() { return this._resourceCollection; };

// add a resource to load
ContentPage.prototype.addResourceLoader = function(resource) {
    this._resourceCollection.add(resource);
};

ContentPage.prototype.getImagesLoading = function() { return this._imagesLoading; };
ContentPage.prototype.getImagesLoaded = function() { return this._imagesLoaded; };
ContentPage.prototype.getImagesFailed = function() { return this._imagesFailed; };
ContentPage.prototype.getImagesAborted = function() { return this._imagesAborted; };

// add an image that we will be waiting for
// PRELOAD? http://www.phpied.com/preload-cssjavascript-without-execution/
ContentPage.prototype.addImage = function(image) {
    // generate an ID for an image if it doesn't have one
    if (image.id == '') {
        image.id = YUD.generateId();
    }

    this._imagesLoading.push(image);

    // Internet Explorer doesn't reliably raise LOAD events on images, so we must use READY_STATE_CHANGE.
    // If the image is cached locally, IE won't fire the LOAD event while the
    // onreadystate event is fired always. On the other hand, the ERROR event
    // is always fired whenever the image is not loaded successfully no matter
    // whether it's cached or not.

    // add dom events
    var loadEvent = YAHOO.env.ua.ie ? 'readystatechange' : 'load';
    YUE.addListener(image, loadEvent, this._onImageEvent, image, this);
    YUE.addListener(image, 'abort', this._onImageEvent, image, this);
    YUE.addListener(image, 'error', this._onImageEvent, image, this);

    // set timestamp
    image.dateRequested = Util.Date.now();

    // cancel existing timeout and start new one
    this._startImageTimer();
};

// cancel existing timeout and start new one
ContentPage.prototype._startImageTimer = function() {
    if (this._imagesTimer != null) {
        this._imagesTimer.cancel();
    }
    this._imagesTimer = YAHOO.lang.later((ContentPage.imageTimeout * 1000), this, this._onImagesTimeout);
};

// this function is called when an time has a timeout
ContentPage.prototype._onImagesTimeout = function() {
    Util.log('image timeout');

    // check if we can retry
    if (this._imagesRetry < ContentPage.imageMaxRetry) {
        this._imagesRetry++;

        // attempt to reload each image
        Util.Array.each(this._imagesLoading, function(image) {
            image.src = image.src;

            // set timestamp
            image.dateRequested = Util.Date.now();
        });

        this._startImageTimer();
    } else {
        // simulate abort event
        Util.Array.each(this._imagesLoading, function(image) {
            var evt = { type: 'abort' };
            this._onImageEvent(evt, image);
        }, this);
    }
};

// handles image events (READY_STATE_CHANGE, LOAD, ABORT, and ERROR)
ContentPage.prototype._onImageEvent = function(evt, image) {
    // make sure we are waiting on this event
    if (!Util.Array.contains(this._imagesLoading, image)) {
        return;
    }

    if (evt.type == 'readystatechange') {
        // This implies that the user agent is IE
        if (image.readyState == 'complete') {
            // This is the IE equivalent of a LOAD event.
            evt = { type: 'load' }; // (NOTE: can't change evt.type property so we fake it)
        } else {
            // This may imply that the load failed.
            // Note that the image has only the following states:
            //   * uninitialized
            //   * loading
            //   * complete
            // When the ERROR or the ABORT event is fired, the readyState
            // will be either uninitialized or loading and we'd ignore those states
            // since they will be handled separately (eg: evt.type = 'ERROR').

            // Notes from MSDN : The states through which an object passes are
            // determined by that object. An object can skip certain states
            // (for example, interactive) if the state does not apply to that object.
            // see http://msdn.microsoft.com/en-us/library/ms534359(VS.85).xhtml

            // The image is not loaded, ignore.
            return;
        }
    }

    // Add natural width/height properties for non-Gecko browsers.
    if (typeof image.naturalWidth == 'undefined') {
        if (evt.type == 'load') {
            image.naturalWidth = image.width;
            image.naturalHeight = image.height;
        } else {
            // This implies that the image fails to be loaded.
            image.naturalWidth = 0;
            image.naturalHeight = 0;
        }
    }

    // image load
    // this._log('image ' + evt.type + ' - ' + image.src);

    // remove dom events
    var loadEvent = YAHOO.env.ua.ie ? 'readystatechange' : 'load';
    YUE.removeListener(image, loadEvent, this._onImageEvent);
    YUE.removeListener(image, 'abort', this._onImageEvent);
    YUE.removeListener(image, 'error', this._onImageEvent);

    // remove the image from waiting queue
    Util.Array.remove(this._imagesLoading, image);

    // move image into final queue
    if (evt.type == 'load') {
        this._imagesLoaded.push(image);
    } else if (evt.type == 'abort') {
        this._imagesAborted.push(image);
    } else {
        this._imagesFailed.push(image);
    } // error

    // set timestamp
    image.dateCompleted = Util.Date.now();

    // check if page is done loading
    this.checkLoaded();
};

/******************************************************************************************/

// fire an event for the page and each passage/items
ContentPage.prototype.fireEvent = function(name, args, fireEntityEvents) {
    // fire event for page
    this._events.notify(name, args);

    if (fireEntityEvents) {
        // fire event for passage if any
        if (page.passage) {
            this.fireEntityEvent(name, page.passage, args);
        }

        // fire event for each item
        var items = page.getItems();

        for (var i = 0; i < items.length; i++) {
            this.fireEntityEvent(name, items[i], args);
        }
    }
};

ContentPage.prototype.dispose = function() {
    if (this._passage) {
        this._passage.dispose();
        delete this._passage;
    }

    if (this._items) {
        for (var i = 0; i < this._items.length; i++) {
            this._items[i].dispose();
            delete this._items[i];
        }
    }

    this._items = null;
    this._itemHash = null;

    this.id = null;
    this.segmentID = null;
    this.layout = null;
    this.soundCue = null;
    this._html = null;
    this._container = null;
    this._doc = null;

    this._activeEntity = null;
    this._zoom = null;
    this._lastEntity = null;

    this._imagesLoading = null;
    this._imagesLoaded = null;
    this._imagesFailed = null;
    this._imagesAborted = null;
    this._imagesTimer = null;
    this._imagesRetry = null;
};
