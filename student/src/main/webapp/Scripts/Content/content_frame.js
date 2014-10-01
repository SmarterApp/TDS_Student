ContentManager.Frame =
{
    _container: window.document.body, // default is current windows body
    _callbacks: new Util.Structs.Map(),
    _baseUrl: '',
    _overrideFrameUrl: false
};

ContentManager.Frame.setBaseUrl = function(baseUrl, overrideFrameUrl)
{
    this._baseUrl = baseUrl || '';
    this._overrideFrameUrl = overrideFrameUrl || false;
};

ContentManager.Frame.resolveUrl = function(url)
{
    // make sure this is not an absolute URL already
    if (Util.String.isHttpProtocol(url)) return url;
    
    // make sure url does not already contain base url    
    if (url.indexOf(this._baseUrl) != -1) return url; 

    return this._baseUrl + url;
};

// set the frame parent container
ContentManager.Frame.setContainer = function(element) { this._container = element; };

// get the doctype for a frame
ContentManager.Frame.getDocType = function()
{
    // chrome/safari/ipad needs HTML5 (or for older versions triggers quirks mode)
    if (YAHOO.env.ua.webkit > 0) return Util.Frame.DOCTYPE_HTML5;
    
    // IE 9 supports HTML5
    if (YAHOO.env.ua.ie >= 9) return Util.Frame.DOCTYPE_HTML5;

    // mozilla uses xhtml transitional
    return Util.Frame.DOCTYPE_XHTML1_TRANSITIONAL;
};

// set the renderer instance
ContentManager.Frame.getTemplate = function()
{
    var html = [];

    // add doc type
    var docType = ContentManager.Frame.getDocType();
    html.push(docType);
    
    // add html
    html.push('<html xmlns="http://www.w3.org/1999/xhtml">');
    html.push('<head>');
    html.push('<title>Content</title>');
    html.push('<meta http-equiv="X-UA-Compatible" content="IE=IE7,chrome=1" />');
    html.push('<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />');
    
    // set frames base uri
    var frameURI;
    
    if (this._overrideFrameUrl && this._baseUrl.length > 0)
    {
        // set custom base url
        frameURI = this._baseUrl;
    }
    else
    {
        // figure out base url based on current document
        var baseURI = document.baseURI || document.URL;
        frameURI = baseURI.replace(/[^/]*$/, '');
    }

    html.push('<base href="' + frameURI + '" />');
    
    // set frames scripts
    Util.Array.each(ContentManager.Renderer.getScripts(), function(script)
    {
        html.push('<script type="text/javascript" src="');
        html.push(ContentManager.Frame.resolveUrl(script));
        html.push('"></script>');
    });

    // add function for creating arrays from within the frame
    html.push('<script type="text/javascript">function createAnArray() { return []; }</script>');

    // set frames styles
    Util.Array.each(ContentManager.Renderer.getStyles(), function(style)
    {
        html.push('<link type="text/css" rel="stylesheet" href="');
        html.push(ContentManager.Frame.resolveUrl(style));
        html.push('" />');
    });

    html.push('</head>');
    html.push('<body onload="this.its = true; parent.ContentManager.Frame.onLoad(this);"><div id="main"><form id="contentForm" name="contentForm"></form></div></body>');
    html.push('</html>');

    return html.join('');
};

ContentManager.Frame.create = function(id, callback)
{
    // create empty frame
    var frame = Util.Frame.createBlank(); // 'width:100%;height:100%'
    frame.id = id;
    frame.className = 'contentFrame';
    this._container.appendChild(frame);

    // register frame onload callback
    if (YAHOO.lang.isFunction(callback))
    {
        this._callbacks.set(id, callback);
    }

    // get frame html template and write it out
    var htmlTemplate = this.getTemplate();
    Util.Frame.writeContent(frame, htmlTemplate);

    return frame;
};

ContentManager.Frame.remove = function(id)
{
    var frame = document.getElementById(id);
    Util.Dom.removeNode(frame);
};

// a iframe will call this when it is done loading
ContentManager.Frame.onLoad = function(frameWin)
{
    var frame = frameWin.frameElement;
    var callback = this._callbacks.get(frame.id);

    if (callback)
    {
        this._callbacks.remove(frame.id);
        callback(frame);
    }
};