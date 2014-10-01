// zoom class, pass in a document and optional zoom css class name (to be the default)
function ContentZoom(defaultDoc, defaultZoomCSS, page) {
    
    this.page = page;

    // the zoom CSS and factor for each level
    this.levels = 
    [
        { css: 'TDS_PS_L0', factor: 1 }, // TDS_PS_Normal
        { css: 'TDS_PS_L1', factor: 1.25 }, // TDS_PS_Larger
        { css: 'TDS_PS_L2', factor: 1.5 }, // TDS_PS_Largest
        { css: 'TDS_PS_L3', factor: 1.75 },
        { css: 'TDS_PS_L4', factor: 2 }
    ];

    // get level for a css name
    this.getLevel = function(zoomCSS) {
        if (zoomCSS != null) {
            for (var i = 0; i < this.levels.length; i++) {
                if (this.levels[i].css == zoomCSS) {
                    return i;
                }
            }
        }

        return 0; // return 0 as default
    };

    // set default level
    this.defaultLevel = this.getLevel(defaultZoomCSS);

    this.previousLevel = -1;

    // set current level
    this.currentLevel = this.defaultLevel;

    // get the current css
    this.getCSS = function() {
        return this.levels[this.currentLevel].css;
    };

    // documents to perform zooming on
    this._documents = [];

    this.addDocument = function(doc, cleanCSS /*bool*/) {
        // make sure document being added doesn't have zoom current zoom class since we did not 
        // add it from zoom class and if it is already added then refresh() will not zoom images
        var currLevelObj = this.levels[this.currentLevel];
        if (currLevelObj && cleanCSS) {
            YUD.removeClass(doc.body, currLevelObj.css);
        }

        this._documents.push(doc);
    };

    // add default document passed in
    this.addDocument(defaultDoc);

    // when this true only content images get changed
    this.contentImages = true;

    this._isContentImage = function(image) {
        return YUD.hasClass(image, 'Image');
    };

    // set zoom level on a specific document
    this._setDocumentLevel = function(doc, newLevel, forceChange) {
        var currLevelObj = this.levels[this.currentLevel];
        var newLevelObj = this.levels[newLevel];

        // if this doc already has the zoom CSS set, then skip it
        if (!forceChange && this.currentLevel == newLevel && YUD.hasClass(doc.body, newLevelObj.css)) {
            return false;
        }

        // makes sure the dimensions are saved before performing any zooming
        this._saveDimensions(doc);

        // remove current zoom css
        YUD.removeClass(doc.body, currLevelObj.css);

        // add new zoom class
        YUD.addClass(doc.body, newLevelObj.css);

        // zoom images
        this._zoomImages(doc, newLevelObj.factor);

        // zoom flash
        this._zoomFlash(doc, newLevelObj.factor);

        return true;
    };

    /**
    * Used to set a specific zoom level.
    *
    * @method zoom
    * @param  {int}  newLevel  One of the zoom levels in the levels array
    */
    this.setLevel = function (newLevel, forceChange) {

        var levelChange = false;

        if (newLevel == null) {
            return levelChange;
        }

        if (typeof(newLevel) == 'string') {
            newLevel = this.getLevel(newLevel);
        }

        // fire content manager beforezoom
        if (this.page != null) {
            ContentManager.firePageEvent('beforeZoom', this.page, null, true);
        }

        if (this._documents.length > 0) {
            for (var i = 0; i < this._documents.length; i++) {
                if (this._setDocumentLevel(this._documents[i], newLevel, forceChange)) {
                    levelChange = true;
                }
            }
        } else {
            levelChange = true;
        }

        this.previousLevel = this.currentLevel;
        this.currentLevel = newLevel;

        // fire content manager zoom only if zoom changed on one of the documents
        if (levelChange && this.page != null) {
            ContentManager.firePageEvent('zoom', this.page, null, true);
        }

        return levelChange;
    };

    // Used to zoom in a level.
    this.zoomIn = function() {
        if (this.currentLevel < this.levels.length - 1) {
            return this.setLevel(this.currentLevel + 1);
        } else {
            return false;
        }
    };

    // Used to zoom out a level. 
    this.zoomOut = function() {
        if (this.currentLevel > 0) {
            return this.setLevel(this.currentLevel - 1);
        } else {
            return false;
        }
    };

    this.reset = function (forceChange) {
        this.setLevel(this.defaultLevel, forceChange);
    };

    /**
    * Used to make sure all the documents in the collection are the right css class. This can
    * be helpful for when you add a new iframe to the collection. 
    *
    * @method refresh
    */
    this.refresh = function (forceChange) {
        this.setLevel(this.currentLevel, forceChange);
    };

    /**
    * Used to zoom the images.
    *
    * @method	zoomImages
    * @param	{Object}	doc		The current HTML document.
    * @param	{int}		factor	The numeric option represents a magnifying factor. 
    * 			A zoom factor of 1 means a normal size. A zoom factor of 2, for example, means 
    * 			an element sized by 2x. A zoom factor of 0.5 will make an element half its original size.
    */
    this._zoomImages = function (doc, factor) {
        
        if (doc == null || doc.images == null) {
            return;
        }

        for (var i = 0; i < doc.images.length; i++) {

            var image = doc.images[i];

            // check if this is an image in the content
            if (this.contentImages && !this._isContentImage(image)) {
                continue;
            }

            // if we don't know the original width/height don't perform zooming
            if (!this._hasAttribute(image, 'originalWidth')) {
                continue;
            }

            // perform zoom
            image.width = image.getAttribute('originalWidth') * factor;
            image.height = image.getAttribute('originalHeight') * factor;
        }

    };

    this._zoomFlash = function(doc, factor) {
        if (doc == null) {
            return;
        }

        // <embed>
        if (doc.embeds != null) {
            for (var i = 0; i < doc.embeds.length; i++) {
                var embed = doc.embeds[i];

                // ignore anything other than flash
                if (embed.type != 'application/x-shockwave-flash') {
                    continue;
                }

                // get the flash parent container
                var flashContainer = embed.parentNode;
                this._zoomElement(flashContainer, factor);
            }
        }

        // <object> (TODO: Do we need this anymore since we are ignoring jwplayer?)
        var objectTags = doc.getElementsByTagName('object');

        if (objectTags != null) {
            for (var i = 0; i < objectTags.length; i++) {
                var objectTag = objectTags[i];

                // ignore anything other than flash
                if (objectTag.type != 'application/x-shockwave-flash') {
                    continue;
                }

                // hack: ignore jwplayer objects
                if (objectTag.data != null && objectTag.data.indexOf('jwplayer') != -1) {
                    continue;
                }

                // perform zooming
                this._zoomElement(objectTag, factor);
            }
            ;
        }
    };

    this._zoomElement = function(el, factor) {
        // if we don't know the original width/height don't perform zooming
        if (!this._hasAttribute(el, 'originalWidth')) {
            return;
        }

        // get zoomed dimensions for container
        var width = (el.getAttribute('originalWidth') * factor);
        var height = (el.getAttribute('originalHeight') * factor);

        // set zoomed dimensions on container (flash itself should already be 100% and fill container)
        YUD.setStyle(el, 'width', width + 'px');
        YUD.setStyle(el, 'height', height + 'px');
    };

    /**
    * Used to prepare zooming. This will get run on all documents everytime zooming is 
    * but will not do anything if height is already set.
    *
    * @method saveHeight
    * @param  {Object}  doc  The current HTML document.
    */
    this._saveDimensions = function(doc) {
        // check if this doc is null or doesn't not have image array 
        if (doc == null) {
            return;
        }

        // loops through each image and saves its original dimensions
        if (doc.images != null) {
            for (var i = 0; i < doc.images.length; i++) {
                var image = doc.images[i];
                this._saveImageDimensions(image);
            }
        }

        // loops through each flash embed and saves its original dimensions
        if (doc.embeds != null) {
            for (var i = 0; i < doc.embeds.length; i++) {
                var embed = doc.embeds[i];

                if (embed.type == 'application/x-shockwave-flash') {
                    var flashContainer = embed.parentNode;
                    this._saveElementDimensions(flashContainer);
                }
            }
        }

        // loops through each flash object and saves its original dimensions
        var objectTags = doc.getElementsByTagName('object');

        if (objectTags != null) {
            for (var i = 0; i < objectTags.length; i++) {
                var objectTag = objectTags[i];

                if (objectTag.type == 'application/x-shockwave-flash') {
                    this._saveElementDimensions(objectTag);
                }
            }
        }
    };

    this._saveImageDimensions = function (image) {
        
        if (image == null) {
            return;
        }

        // check if this is an image in the content
        if (this.contentImages && !this._isContentImage(image)) {
            return;
        }

        // if we already know height skip this image
        if (this._hasAttribute(image, "originalHeight")) {
            return;
        }

        // save original image dimensions only if they are greater than 0
        if (image.width > 0 && image.height > 0) {
            image.setAttribute('originalWidth', image.width);
            image.setAttribute('originalHeight', image.height);
        }
    };

    this._saveElementDimensions = function (el) {
        
        if (el == null) {
            return;
        }

        // if we already know height skip this image
        if (this._hasAttribute(el, 'originalHeight')) {
            return;
        }

        var width = 0, height = 0;

        // check if element explictly defines width/height
        if (this._hasAttribute(el, 'width') &&
            this._hasAttribute(el, 'height')) {
            width = (YUD.getAttribute(el, 'width') * 1);
            height = (YUD.getAttribute(el, 'height') * 1);
        } else {
            // get elements region info
            var region = YUD.getRegion(el);

            if (region != null) {
                width = region.width;
                height = region.height;
            }
        }

        // save original element dimensions only if they are greater than 0
        if (width > 0 && height > 0) {
            el.setAttribute('originalWidth', width);
            el.setAttribute('originalHeight', height);
        }
    };

    // used to tell if an element has a specific attribute 
    // (since this DOM2 function and IE does not support, use this method instead)
    this._hasAttribute = function(ele, attr) {
        if (ele.hasAttribute) {
            return ele.hasAttribute(attr);
        } else {
            return ele.getAttribute(attr);
        }
    };
}