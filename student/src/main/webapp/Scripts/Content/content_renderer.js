ContentManager.Renderer =
{
    _instance: null,
    _client: null,
    _customScripts: [], // custom list of javascript 
    _customStyles: [], // custom list of CSS
    _customFormatter: null // custom formatter func for css/js
};

// check if we are using direct renderer (no iframes)
ContentManager.Renderer.isDirect = function() {
    return this._instance instanceof ContentManager.Renderer.Direct;
};

ContentManager.Renderer.getClient = function() { return this._client; };
ContentManager.Renderer.setClient = function(client) { this._client = client; };

// get the custom formatter for use elsewhere (e.x., sim)
ContentManager.Renderer.getCustomFormatter = function() { return this._customFormatter; };

// set a custom function for modifying css/js url's before they are added to iframe (e.x., manifest)
ContentManager.Renderer.setCustomFormatter = function(formatterFunc) { this._customFormatter = formatterFunc; };

// modify a list of resource url's (css/js) using the custom formatter 
ContentManager.Renderer.processResources = function(resources)
{
    if (YAHOO.lang.isFunction(this._customFormatter))
    {
        for (var i = 0; i < resources.length; i++)
        {
            resources[i] = this._customFormatter(resources[i]);
        }
    }
};

ContentManager.Renderer.clearCustomScripts = function() { this._customScripts = []; };
ContentManager.Renderer.getCustomScripts = function() { return this._customScripts; };
ContentManager.Renderer.addCustomScript = function(path) { this._customScripts.push(path); };

ContentManager.Renderer.getScripts = function()
{
    var scripts =
    [
        'Scripts/Libraries/YUI/yahoo-dom-event/yahoo-dom-event.js',
        'Scripts/Libraries/YUI/dragdrop/dragdrop-min.js',
        'Scripts/Libraries/jwplayer/jwplayer.js'
    ];
    
    // add custom js
    for (var i = 0; i < this._customScripts.length; i++)
    {
        scripts.push(this._customScripts[i]);
    }
    
    // call custom formatter
    ContentManager.Renderer.processResources(scripts);

    return scripts;
};

ContentManager.Renderer.clearCustomStyles = function() { this._customStyles = []; };
ContentManager.Renderer.getCustomStyles = function() { return this._customStyles; };
ContentManager.Renderer.addCustomStyle = function(path) { this._customStyles.push(path); };

ContentManager.Renderer.getStyles = function()
{
    // add default css
    var styles =
    [
        'Scripts/Libraries/YUI/menu/assets/skins/sam/menu.css',
        'Scripts/Libraries/YUI/button/assets/skins/sam/button.css',
        'Scripts/Libraries/YUI/container/assets/skins/sam/container.css',
        'Scripts/Libraries/YUI/editor/assets/skins/sam/editor.css',

        'Shared/CSS/items.css',
        'Shared/CSS/elpa.css',
        'Shared/CSS/accommodations.css',
        'Shared/CSS/frame.css',
        'Scripts/Simulator2/Renderer/CSS/simulator.css',
        'Scripts/Simulator2/Renderer/CSS/slider.css'
    ];

    // add client specific css
    if (this._client)
    {
        styles.push('Projects/' + this._client + '/css/items.css');
        styles.push('Projects/' + this._client + '/css/elpa.css');
    }
    
    // add custom css
    for (var i = 0; i < this._customStyles.length; i++)
    {
        styles.push(this._customStyles[i]);
    }

    // add browser specific css
    if (YAHOO.env.ua.webkit)
    {
        styles.push('Shared/CSS/Browsers/webkit.css');
    }
    else if (YAHOO.env.ua.ie)
    {
        styles.push('Shared/CSS/Browsers/ie.css');
    }
    
    // call custom formatter
    ContentManager.Renderer.processResources(styles);

    return styles;
};

ContentManager.Renderer.applyStyles = function(doc, client)
{
    // get styles to apply
    var styles = this.getStyles(client);
    
    // get current styles
    var links = document.getElementsByTagName('link');

    // filter out styles that are already loaded on the page
    styles = Util.Array.reject(styles, function(style) {
        return Util.Array.find(links, function(link) {
            return (link.href.toLowerCase().indexOf(style.toLowerCase()) != -1);
        });
    });

    // resolve url's
    for(var i = 0; i < styles.length; i++)
    {
        styles[i] = ContentManager.resolveBaseUrl(styles[i]);
    }

    Util.dir(styles);

    // apply css to the doc
    var win = (doc.parentWindow || doc.defaultView);
    YAHOO.util.Get.css(styles, { win: win });
};

// create a new renderer
// @element Container for the renderer
// @renderer The renderer class
ContentManager.Renderer.init = function(container, rendererClass)
{
    // get element that will be the container for the pages
    container = YUD.get(container);
    
    /*
    if (ContentManager.enableARIA)
    {
        container.setAttribute('role', 'main');

        // NOTE: By putting aria-live on the container for all the pages it 
        // will speak the page each time you just hide or show it automatically
        container.setAttribute('aria-live', 'polite');
        container.setAttribute('aria-relevant', 'additions text');
    }
    */

    // create renderer
    var rendererInstance = new rendererClass(container);

    // save renderer and initialize it
    this._instance = rendererInstance;
    this._instance.init();
};

// a helper method for writing html into the innerHTML of a node
ContentManager.Renderer.writeHtml = function(parentNode, html)
{
    // create page html container
    // NOTE: Can't appendChild to a node created from another frame so get parent node owner
    var parentDoc = Util.Dom.getOwnerDocument(parentNode);
    var pageWrapper = parentDoc.createElement('div');
    YUD.addClass(pageWrapper, 'pageWrapper');
    
    // check if html exists
    if (html) {

        // if the html is a xml document then get its element
        if (html.documentElement) {
            html = html.documentElement;
        }
    
        // check for the type of html
        if (html.nodeType > 0) {
            pageWrapper.appendChild(html);
        } else {
            pageWrapper.innerHTML = html;
        }
    }
    
    // set page hidden from JAWS until it is ready
    pageWrapper.setAttribute('aria-hidden', 'true'); 

    // add container to frame
    parentNode.appendChild(pageWrapper);

    return pageWrapper;
};

// a helper method for showing a page container
ContentManager.Renderer.show = function(pageContainer) {
    pageContainer.setAttribute('aria-hidden', 'false');
    YUD.removeClass(pageContainer, 'hiding');
    YUD.addClass(pageContainer, 'showing');
};

// a helper method for hiding a page container
ContentManager.Renderer.hide = function(pageContainer) {
    pageContainer.setAttribute('aria-hidden', 'true');
    YUD.removeClass(pageContainer, 'showing');
    YUD.addClass(pageContainer, 'hiding');
};


// write out the HTML for a page object
ContentManager.Renderer.writePage = function(page)
{
    return this._instance.write(page);
};

// remove the HTML for a page object
ContentManager.Renderer.removePage = function(page)
{
    return this._instance.remove(page);
};

/************************************************************/

// RENDERER: Used to render each page in a seperate frame
ContentManager.Renderer.MultiFrame = function(rootContainer)
{
    this.init = function()
    {
        ContentManager.Frame.setContainer(rootContainer);
    };

    this.write = function(page)
    {
        // first create a frame to hold HTML
        var frame = ContentManager.Frame.create(page.id, function(frame)
        {
            // get the frames element we will write to
            var frameDoc = Util.Dom.getFrameContentDocument(frame);
            var frameForm = frameDoc.forms['contentForm'];

            // write html to element in frame
            ContentManager.Renderer.writeHtml(frameForm, page.getHtml());

            // tell page we are done rendering
            page.onRendered(frameDoc /*document*/, frame /*container*/);
        });

        // hide page container
        ContentManager.Renderer.hide(frame);
    };

    this.remove = function(page)
    {
        ContentManager.Frame.remove(page.id);
    };
};

// RENDERER: Used to render all the pages in one frame
ContentManager.Renderer.SingleFrame = function(rootContainer)
{
    this._frame = null;

    this.init = function()
    {
        ContentManager.Frame.setContainer(rootContainer);
        ContentManager.Frame.create('contentFrame');
    };

    this.write = function(page)
    {
        // write html to shell container
        var frame = document.getElementById('contentFrame');
        var frameDoc = Util.Dom.getFrameContentDocument(frame);
        var frameForm = frameDoc.forms['contentForm'];
        var pageContainer = ContentManager.Renderer.writeHtml(frameForm, page.getHtml());

        // hide page container
        ContentManager.Renderer.hide(pageContainer);

        // tell page we are done rendering
        page.onRendered(frameDoc, pageContainer);
    };

    this.remove = function(page)
    {
        var pageContainer = page.getContainer();
        Util.Dom.removeNode(pageContainer);
    };
};

// RENDERER: Used to render all the pages in the shell within an element (no frames are used)
ContentManager.Renderer.Direct = function(rootContainer)
{
    this.init = function()
    {
        // ContentManager.Renderer.applyStyles(document);
        var contentForm = HTML.FORM({ id: 'contentForm', name: 'contentForm' });
        rootContainer.appendChild(contentForm);
        // container.innerHTML = '<form id="contentForm" name="contentForm"></form>';
    };

    this.write = function(page)
    {
        // write html to shell container
        var form = document.forms['contentForm'];
        var pageContainer = ContentManager.Renderer.writeHtml(form, page.getHtml());

        // hide page container
        ContentManager.Renderer.hide(pageContainer);

        // tell page we are done rendering
        page.onRendered(document, pageContainer);
    };

    this.remove = function(page)
    {
        var pageContainer = page.getContainer();
        Util.Dom.removeNode(pageContainer);
    };
};

/************************************************************/

