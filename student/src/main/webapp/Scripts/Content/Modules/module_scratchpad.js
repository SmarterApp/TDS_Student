/*
 *  This is a copy of the TestShell.Comments code, will try and register it and 
 * make it work for ItemPreview.
 */
(function() {
    
    //Ensure that the javascript is loaded / enabled
    if (!window.ScratchPad) {
        return;
    } 

    //Setup some configuration that is based purely on ItemPreview / Blackbox
    ScratchPad.Config = ScratchPad.Config || {};
    ScratchPad.Config.DefaultLabel = ContentManager.getCommentLabel();
    ScratchPad.Config.CommentCode = 'ScratchPad';

    // find all SP images and add them to page load
    ContentManager.onItemEvent('rendered', function (page, item) {

        if (!item.isResponseType(ScratchPad.Config.CommentCode)) {
            return;
        }

        try {
            ScratchPad.Utils.loadImageFiles(page, item.rendererSpec);
        } catch (ex) {
            // NOTE: preloading images was done mid-year so we need to ignore any errors
            TDS.Diagnostics.report(ex);
        }
    });

    //Set this only in the demo branch, this should not override in production.
    // I'm not sure what the comment above ^^ means, but check for notes, not all BB apps use it.
    //if (TDS.Notes) {
    //    TDS.Notes.getFactory().cfg.DefaultClass = TDS.Notes.Types.ScratchPad;
    //}

    //Initialize the factory in the TDS app, set the the CommentCode
    //An actual item (it would be silly to also allow a scratchpad coment)
    ContentManager.onItemEvent('available', function(page, item) {
        if (!item.isResponseType(ScratchPad.Config.CommentCode)) {
            return;
        }
        console.log("Create a new Scratchpad Item", item.position);

        //Accessibility for a scratchpad == ????  Text area?  Should not show up?
        var id = 'ScratchPad_' + item.position;
        console.log("Module:ScratchPad: Trying to add into:", id, document.getElementById(id));
        
        //Build or retrieve the scratchpad
        var spXml = item.rendererSpec;
        var sp = ScratchPad.Factory.getInstance().createScratchPad(id, spXml);
        console.log("What is item.ScratchPad.", item.ScratchPad);

        //Define a format for the Zwibbler item, it is Going to be XML because AIR hates using modern practices.
        setTimeout(function() {
            console.log("Resize timeout event.", item.ScratchPad);
            sp.app.resizeCanvas();
            sp.app.view.draw();
            if (item.value) {
                sp.setResponseXml(item.value);
            }
        }, 100); //Have to delay the size check when dealing with IE

        //For equation editing using our notes equation item.
        ScratchPad.Equation.init(sp);

        item.ScratchPad = sp;

        sp.isReadOnly = item.isReadOnly;

    });


    ContentManager.onItemEvent('zoom', function (page, item) {
        if (item.ScratchPad == null) return;

        // get current zoom factor
        var zoomInfo = page.getZoom();
        var zoomFactor = (zoomInfo == null) ? 1 : zoomInfo.levels[zoomInfo.currentLevel].factor;

        item.ScratchPad.app.view.scale = zoomFactor;
        item.ScratchPad.app.view.update(true);
    });

    //Provide a get and set operation for the scratchpad save
    ContentManager.registerResponseHandler('ScratchPad',
        function(item, response) { //getResponse
            try {
                var sp = item && item.ScratchPad ? item.ScratchPad : null;
                if (sp) {                    
                    response.value = sp.getResponseXml();
                    response.isValid = sp.isValid();
                    response.isSelected = response.isValid;
                }
            } catch(e) {
                console.error("Module:ScratchPad Could not get a response for item, e", item, e);
            }
        },
        function(item, value) { //setResponse
            try {
                var sp = item && item.ScratchPad ? item.ScratchPad : null;
                if (value && sp && typeof value == 'string') {                               
                    sp.setResponseXml(value);                    
                }
            } catch (e) {                
                console.error("Module:ScratchPad Failed to set (value, e)", value, e);
            }
        }
    );
    
    //Factory handler for eq item type events.
    ScratchPad.Equation = {              
      init: function(sp){
          //bind to node edit 
          sp.on('math.edit', ScratchPad.Equation.editListener.bind(sp));
          
          sp.on('convert-dom-request', ScratchPad.Equation.updateDomListener.bind(sp));
      },
      editListener: function(nodeId, node){ 

        //Need to get the data that may already exist in this data and ensure we can open a valid
        //notes object (unserialize)
        console.log("what is this?", this, nodeId);

        var isNew = (typeof node.properties.data === 'object') ? false : true;
        var wnd = SPEquations.init(this.id, node);
        wnd.show();

        //save handler
        wnd.preSave = function (args, eqWnd) {
            console.log("$$$$equation item save", this);

            //get the data out of the notes comments.
            var data = args.data ? args.data.comment : {};
            data.isNew = isNew; //to flag new requests

            //Fires off the updateDomListener / convert-dom-request event.
            this.setItemProperty(nodeId, 'data', data);
            
            return false;//Prevent ajax save in the notes framework
        }.bind(this);

        //cancel handler
        wnd.preClose = function () {
            console.log("$$$$equation item cancel", this);

            if (isNew)
                this.app.view.eventSource.emit("menu.undo", {});

            return false;
        }.bind(this);
      },
      updateDomListener: function(data, nodeId) {
          console.log("Update dom listener?", this, data, nodeId);
          
          data = data || {};
          data.navigation = false;
          data.tabs = false;

          var node = this.app.view.doc.getNode(nodeId);

          var me = new MathJax.Editor.Widget(data);
          var div = me.getContainerDom();
          div.parentNode.removeChild(div); //Remove from the current page, add to canvas

          //delete old widget ref if exists
          if (typeof node.widgetId !== 'undefined')
              MathJax.Editor.Store.remove(node.widgetId);
          node.widgetId = me.id; //widget id wrt to store, to clear unused objs


          //Hmm, this is a little scary
          this.setDomElement(nodeId, div);

      }
    };

    if (typeof (ScratchPad.Utils) == 'undefined') ScratchPad.Utils = {};

    // parse all the image file names out of SP answer space xml
    ScratchPad.Utils.parseImageFiles = function (questionXml) {
        var eachNode = function (nodeList, func) {
            var nodes = [];

            for (var i = 0; i < nodeList.length; i++) {
                nodes.push(nodeList[i] || nodeList.item(i));
            }

            YAHOO.util.Dom.batch(nodes, func);
        };

        var getTextContent = function (node) {
            if (node && node.childNodes && node.childNodes.length)
                return node.childNodes[0].nodeValue;
            return null;
        };

        var imageFiles = [];
        var xmlDoc = Util.Xml.parseFromString(questionXml);

        // get all <backgroundImage> elements
        var imageNodes = xmlDoc.getElementsByTagName('backgroundImage');

        eachNode(imageNodes, function (imageNode) {
            var imageFile = getTextContent(imageNode);
            if (imageFiles.indexOf(imageFile) == -1) imageFiles.push(imageFile);
        });

        return imageFiles;
    };

    // load image files from the SP into the page collection
    ScratchPad.Utils.loadImageFiles = function (page, questionXml) {
        // get all the image file names out of the SP answer space xml
        var imageFiles = ScratchPad.Utils.parseImageFiles(questionXml);

        for (var i = 0; i < imageFiles.length; i++) {
            var imageFile = imageFiles[i];

            // we need to resolve the url in the same we do for
            // SP so we use same file name and it gets cached
            var imageUrl = ScratchPad.Utils.resolveUrl(imageFile);
            // create image and add it to the pages collection
            var img = new Image();
            page.addImage(img);
            img.src = imageUrl;
        }
    };

    ScratchPad.Utils.resolveUrl = function (url) {
        // change any html ampersand entities into the ampersand character
        url = url.replace(/&amp;/g, '&');

        // escape url
        url = url.split('&').join('&amp;').split('<').join('&lt;').split('"').join('&quot;');

        var el = document.createElement('div');
        el.innerHTML = '<a href="' + url + '">x</a>';
        return el.firstChild.href;
    };
})();