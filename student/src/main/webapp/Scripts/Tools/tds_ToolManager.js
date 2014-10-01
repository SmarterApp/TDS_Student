if (typeof TDS == 'undefined') TDS = {};

TDS.ToolManager =
{
    _timeout: 120, // seconds
    _overlayManager: new YAHOO.widget.OverlayManager(),
    _container: null, // contains all the panel HTML
    Events: new Util.EventManager() // onCreating, onCreated, onLoaded, onShow, onHide
};

TDS.ToolManager.init = function()
{
    // check for existing tools container
    this._container = YUD.get('tools');
    
    // create tools container if it doesn't exist
    if (this._container == null) 
    {
        this._container = HTML.DIV({ id: 'tools' });
        document.body.appendChild(this._container);
    }
};

TDS.ToolManager.createPanel = function(id, name, header, footer, key, resize)
{
    // create a panel instance
    var panel = new YAHOO.widget.Dialog(id, {
        draggable: true,
        constraintoviewport: true,
        close: true,
        //width: '600px',
        //height: '600px',
        underlay: 'none',
        //fixedcenter: true,
        visible: false,
        zindex: 998
    });

    // create html
    var htmlBody = [];
    htmlBody.push('<iframe id="frame-' + id + '" frameborder="0" marginheight="0" marginwidth="0" height="100%" width="100%"></iframe>'); // scrolling="yes"
    htmlBody.push('<div class="yui-panel-mask"></div>');
    

    // add html
    if (header) panel.setHeader(header);
    panel.setBody(htmlBody.join(''));
    if (footer) panel.setFooter(footer);

    // add function for getting frame
    panel.getFrame = function()
    {
        return this.element.getElementsByTagName('iframe')[0];
    };

    panel.getMask = function()
    {
        return YUD.getElementsByClassName('yui-panel-mask', 'div', panel.element)[0];
    };
    

    

    /*panel.beforeRenderEvent.subscribe(function()
    {
    var lastX = 0;
    var lastY = 0;

        var dd = new TDS.ToolManager.DragDrop(panel);

        dd.on('b4DragEvent', function(e)
    {
    });

        dd.on('dragEvent', function(e)
    {
    });

        // show panel
    dd.on('endDragEvent', function()
    {
    panel.show();
    });

    }, panel, true);*/

    panel.isShowing = function()
    {
        return TDS.ToolManager.isShowing(id);
    };

    // load frame url
    panel.load = function() 
    {
        TDS.ToolManager.loadKey(panel, key);
    };

    // gets fired after the HTML has rendered
    panel.renderEvent.subscribe(function()
    {
        panel.dd.useShim = true; // prevents mouseover dragging from being interfered with

        YUD.addClass(panel.innerElement, 'tool');

        // create class name for this tool
        // var className = 'tool-' + header.replace(/\s+/g, '').toLowerCase();
        var className = 'tool-' + name;
        YUD.addClass(panel.innerElement, className);

        // create parent class name
        if (panel.innerElement && panel.innerElement.parentNode) {
            YUD.addClass(panel.innerElement.parentNode, className + '-container');
        }
        
        // load
        panel.load();

    }, panel, true);

    // set width/height from css
    panel.refresh = function()
    {
        panel.cfg.setProperty('width', YUD.getStyle(panel.innerElement, 'width'));
        panel.cfg.setProperty('height', YUD.getStyle(panel.innerElement, 'height'));
    };

    TDS.ToolManager.Events.fire('onCreating', panel);

    panel.render(this._container);
    TDS.ToolManager.bottomLeft(panel);
    // panel.center();

    // check if we are requesting resizing
    if (resize) TDS.ToolManager.makeResize(panel);

    // fire specific panel events
    panel.beforeShowEvent.subscribe(function()
    {
        panel.refresh();
    });

    // fire specific panel events
    panel.showEvent.subscribe(function()
    {
        TDS.ToolManager.Events.fire('onShow', panel);

        // focus on panel
        setTimeout(function() { TDS.ToolManager._overlayManager.focus(panel); }, 0);
    });

    panel.beforeHideEvent.subscribe(function()
    {
        // BUG: When selecting text in dialog frame select boxes can't get focused
        // STEPS: Open help, select text, leave text selected and close help.. no select boxes work (grades or global accs)
        if (Util.Browser.isSecure() && !Util.Browser.isMac())
        {
            // NOTE: Don't do this on mac or it causes focus bug #30808
            Util.SecureBrowser.fixFocus();
        }
    });

    panel.hideEvent.subscribe(function()
    {
        TDS.ToolManager.Events.fire('onHide', panel);
        Util.Dom.focusWindow(2); // (BUG #26524)
    });

    // add panel to overlay manager and return
    this._overlayManager.register(panel);

    // FIX 
    // TDS.ToolManager._overlayManager.overlays[0].dd

    TDS.ToolManager.Events.fire('onCreated', panel);

    return panel;
};

// move the dialog in the bottom left
TDS.ToolManager.bottomLeft = function(dialog)
{
    // set latest width/height
    dialog.refresh();
    
    var nViewportOffset = 10, /* VIEWPORT_OFFSET*/
        elementWidth = dialog.element.offsetWidth,
        elementHeight = dialog.element.offsetHeight,
        viewPortWidth = YUD.getViewportWidth(),
        viewPortHeight = YUD.getViewportHeight(),
        x,
        y;

    // set left
    x = nViewportOffset + YUD.getDocumentScrollLeft();

    // get footer adjustment
    var footerOffset = (YUD.get('foot') ? YUD.get('foot').offsetHeight : 0);
    
    // set bottom
    if ((elementHeight + footerOffset) < viewPortHeight)
    {
        y = (viewPortHeight - elementHeight - footerOffset) + YUD.getDocumentScrollTop();
    } 
    else 
    {
        // just set to top the dialog is to big
        y = nViewportOffset + YUD.getDocumentScrollTop();
    }

    dialog.cfg.setProperty("xy", [parseInt(x, 10), parseInt(y, 10)]);
    dialog.cfg.refireEvent("iframe");

    if (YAHOO.UA.webkit) {
        dialog.forceContainerRedraw();
    }
};

// load url into a iframe
TDS.ToolManager.loadFrameUrl = function(container, frame, url, success, abort)
{
    var timer = null;

    // function handler for when frame loads
    var onFrameLoaded = function()
    {
        Util.log('tool load: ' + url);
        YUD.removeClass(container, 'yui-panel-loading');

        // cancel timeout
        if (timer) timer.cancel();

        // remove frame load listener
        YUE.removeListener(frame, 'load', onFrameLoaded);

        var frameDoc = Util.Dom.getFrameContentDocument(frame);

        // check for access to the frame (if external site we don't have privileges)
        var allowAccess = true;
        try { var test = frameDoc.URL; } catch (ex) { allowAccess = false; }

        if (allowAccess)
        {
            // check if iframe loaded properly
            if (frame.src == '' || frameDoc.URL == 'about:blank') return;

            // assign keyboard handler
            KeyManager.attachListener(frameDoc);
            
            Util.Dom.copyCSSFrame(frame);
        }

        // call function indicating we are loaded
        if (YAHOO.lang.isFunction(success)) success(frameDoc, allowAccess);
    };

    // function handler for when frame times out
    var onFrameAborted = function()
    {
        Util.log('tool abort: ' + url);
        YUD.removeClass(container, 'yui-panel-loading');

        // remove frame load listener
        YUE.removeListener(frame, 'load', onFrameLoaded);

        // stop loading
        var frameWin = Util.Dom.getFrameContentWindow(frame);
        try { frameWin.stop(); }
        catch (ex) { }

        // call function indicating we failed
        if (YAHOO.lang.isFunction(abort)) abort();

        // write out error
        var errorMessage = 'Timeout occured loading content.';
        var errorLink = '<a href="#" id=\"reload\">Click here to reload</a>';
        
        // set src to blank and write out error
        frame.src = Util.Frame.BLANK_SOURCE;
        Util.Frame.writeContent(frame, errorMessage + ' ' + errorLink);

        // add reload listener
        var frameDoc = Util.Dom.getFrameContentDocument(frame);

        YUE.addListener(frameDoc.getElementById('reload'), 'click', function(ev)
        {
            YUE.stopEvent(ev);
            loadFrame();
        });
    };

    // function for loading frame
    var loadFrame = function()
    {
        YUD.addClass(container, 'yui-panel-loading');

        // add frame load listener
        YUE.addListener(frame, 'load', onFrameLoaded);

        // abort timer
        timer = YAHOO.lang.later((TDS.ToolManager._timeout * 1000), this, onFrameAborted);

        // go to url
        frame.src = url;
    };

    loadFrame();
};

// load url into a iframe
TDS.ToolManager.loadUrl = function(panel, url, fn)
{
    var frame = panel.getFrame();

    TDS.ToolManager.loadFrameUrl(panel.element, frame, url, function()
    {
        TDS.ToolManager.Events.fire('onLoaded', panel);
        if (YAHOO.lang.isFunction(fn)) fn();
    });
};

// load url into a iframe from a message key
TDS.ToolManager.loadFrameKey = function(container, frame, messageKey, fn)
{
    // get url in messages
    if (!messageKey) return;

    var url;
    if (Util.String.isHttpProtocol(messageKey)) {
        url = messageKey;
    } else if (Messages.has(messageKey)) {
        url = Messages.get(messageKey);
        url = TDS.baseUrl + 'Pages/' + url; // urls start in the /Pages/ (seems hacky)
    } else {
        return; // no url found
    }
    
    this.loadFrameUrl(container, frame, url, fn);
};

// load url into a iframe from a message key
TDS.ToolManager.loadKey = function(panel, messageKey)
{
    var frame = panel.getFrame();

    var onPanelLoaded = function()
    {
        TDS.ToolManager.Events.fire('onLoaded', panel);
    };

    var onPanelFailed = function()
    {
        TDS.ToolManager.Events.fire('onFailed', panel);
    };

    TDS.ToolManager.loadFrameKey(panel.element, frame, messageKey, onPanelLoaded, onPanelFailed);
};

TDS.ToolManager.get = function(id)
{
    return this._overlayManager.find(id);
};

TDS.ToolManager.getAll = function() {
    return this._overlayManager.overlays;
};

TDS.ToolManager.isShowing = function(id)
{
    var panel = this.get(id);
    return (panel) ? panel.cfg.getProperty("visible") : false;
};

TDS.ToolManager.hide = function(id)
{
    var panel = this.get(id);
    if (panel) panel.hide();
};

TDS.ToolManager.hideAll = function()
{
    this._overlayManager.hideAll();
};

TDS.ToolManager.show = function(id)
{
    var panel = this.get(id);

    if (panel)
    {
        this.hideAll();
        panel.show();
    }
};

TDS.ToolManager.toggle = function(id)
{
    var panel = this.get(id);
    if (panel == null) return;

    if (this.isShowing(panel)) 
    {
        this.hide(panel);
    }
    else
    {
        this.show(panel);
        /*Bug 16594: Puts focus back on iframe when panel is shown*/
        setTimeout(function () { panel.getFrame().focus(); }, 0);
    }
};

/*****************************************************************************************/

// make a YUI panel resizable
TDS.ToolManager.makeResize = function(panel)
{
    // Setup constants
    var mask = panel.getMask();
    var frame = panel.getFrame();

    // QUIRKS FLAG, FOR BOX MODEL
    var IE_QUIRKS = (YAHOO.env.ua.ie && document.compatMode == "BackCompat");

    // UNDERLAY/IFRAME SYNC REQUIRED
    var IE_SYNC = (YAHOO.env.ua.ie == 6 || (YAHOO.env.ua.ie == 7 && IE_QUIRKS));

    // PADDING USED FOR BODY ELEMENT (Hardcoded for example)
    var PANEL_BODY_PADDING = (10 * 2); // 10px top/bottom padding applied to Panel body element. The top/bottom border width is 0

    // Create Resize instance, binding it to the 'resizablepanel' DIV 
    var resize = new YAHOO.util.Resize(panel.id, {
        autoRatio: false,
        minWidth: 300,
        minHeight: 100,
        status: false,
        proxy: true,
        hover: true
    });

    //This fixes a position issue with IE6 and overflow hidden
    if (YAHOO.env.ua.ie == 6)
    {
        YUD.setStyle(resize._handles.r, 'height', (panel.element.offsetHeight - 5) + 'px');

        resize.on('startResize', function()
        {
            YUD.setStyle(mask, 'height', panel.body.offsetHeight + 'px');
            YUD.setStyle(mask, 'width', panel.body.offsetWidth + 'px');
        });
    }

    // Setup resize handler to update the size of the Panel's body element
    // whenever the size of the 'resizablepanel2' DIV changes
    resize.on('resize', function(args)
    {
        var panelHeight = args.height;
        var panelWidth = args.width;

        if (!panelHeight)
        {
            panelHeight = resize._cache.height;
        }

        if (!panelWidth)
        {
            panelWidth = resize._cache.width;
        }

        var headerHeight = (this.header) ? this.header.offsetHeight : 0; // Content + Padding + Border
        var footerHeight = (this.footer) ? this.footer.offsetHeight : 0; // Content + Padding + Border

        var bodyHeight = (panelHeight - headerHeight - footerHeight);
        var bodyContentHeight = (IE_QUIRKS) ? bodyHeight : bodyHeight - PANEL_BODY_PADDING;
        var bodyContentWidth = (IE_QUIRKS) ? panelWidth : panelWidth - PANEL_BODY_PADDING;

        YUD.setStyle(this.body, 'height', bodyContentHeight + 'px');
        YUD.setStyle(this.body, 'width', bodyContentWidth + 'px');

        if (YAHOO.env.ua.ie == 6)
        {
            YUD.setStyle(resize._handles.r, 'height', (this.element.offsetHeight - 5) + 'px');
            YUD.setStyle(mask, 'height', bodyHeight + 'px');
            YUD.setStyle(mask, 'width', panelWidth + 'px');

            YUD.setStyle(frame, 'height', bodyContentHeight + 'px');
            YUD.setStyle(frame, 'width', bodyContentWidth + 'px');
        }

        if (IE_SYNC)
        {
            // Keep the underlay and iframe size in sync.

            // You could also set the width property, to achieve the 
            // same results, if you wanted to keep the panel's internal
            // width property in sync with the DOM width. 

            this.sizeUnderlay();

            // Syncing the iframe can be expensive. Disable iframe if you
            // don't need it.

            this.syncIframe();
        }

    }, panel, true);

}; 

/*****************************************************************************************/

// drag drop for tools
// http://new.davglass.com/files/yui/panel9/
// http://new.davglass.com/files/yui/dd10/
// http://developer.yahoo.com/yui/examples/dragdrop/dd-shim.html
TDS.ToolManager.DragDrop = function(panel)
{
    this._panel = panel;

    // call DDProxy constructor code
    this.init(panel.element, null, { scroll: false });
    this.initFrame();

    // set drag handle on panel header
    this.setHandleElId(panel.header);
    
    // use shim for going over iframes
    this.useShim = true;
};

YAHOO.extend(TDS.ToolManager.DragDrop, YAHOO.util.DDProxy,
{
    /*setDragElPos: function(iPageX, iPageY)
    {
        var constrainedXY = this._panel.getConstrainedXY(iPageX, iPageY);
        
        var el = this.getDragEl();
        this.alignElWithMouse(el, constrainedXY[0], constrainedXY[1]);
    }*/
});

/*****************************************************************************************/

// BUG #22906: Clicking on links in Help Guide scrolls down the pages outside the test
TDS.ToolManager.Events.subscribe('onLoaded', function(panel)
{
    var panelFrame = panel.getFrame();
    var panelDoc = Util.Dom.getFrameContentDocument(panelFrame);

    YUD.batch(panelDoc.getElementsByTagName('a'), function(link)
    {
        // check if valid link
        if (link.href.length == 0) return;

        // get links anchor target
        var targetID = link.hash.substring(1);
        if (targetID.length == 0) return;

        // get targets element
        var target = panelDoc.getElementById(targetID);
        if (target == null) return;

        YUE.on(link, 'click', function(evt)
        {
            // stop normal anchor scroll
            YUE.stopEvent(evt);

            // scroll to target
            target.scrollIntoView();

            // scroll parent to the top of screen since iframe will try to scroll parent towards bottom
            window.scrollTo(0, 0);
        });
    });
});
