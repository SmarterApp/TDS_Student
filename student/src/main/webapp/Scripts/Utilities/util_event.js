// REQUIRES: util.js

Util.Event = { };

Util.Event.preventDefault = function(e)
{
    e = e || window.event;

    // fix for destroying ctrl key combinations for IE (this will allow 'keypress' when events are cancelled)
    if (YAHOO.env.ua.ie && (e.ctrlKey || e.altKey)) {
        try { e.keyCode = 0; } catch (ex) { }
    }

    e.cancelBubble = true;
    e.returnValue = false;

    // stopPropagation works in Firefox.
    if (e.stopPropagation)
    {
        e.stopPropagation();
        e.preventDefault();
    }

    return false;
};

Util.Event.allowDefault = function(e)
{
    if (!e) e = window.event;

    if (e.cancelBubble)
    {
        e.cancelBubble = false;
    }

    if (e.returnValue)
    {
        e.returnValue = true;
    }

    return true;
};

/* Dispatch a click event into the document tree, returns false if the event is cancelled
*
* Note: I would have called this function fireEvent, or
*       dispatchEvent, however, this would have resulted in the
*       browser-supplied functions (former in IE, latter in DOM-
*       compliant browsers) being called. Be sure to avoid that.
*/
Util.Event.doEventDispatch = function(elm)
{
    var evt = null;

    if (document.createEvent)
    {
        evt = document.createEvent('MouseEvents');
    }

    if (elm && elm.dispatchEvent && evt && evt.initMouseEvent)
    {
        var tmp1 = evt.initMouseEvent(
            'click',
            true,     // Click events bubble
            true,     // and they can be cancelled
            document.defaultView,  // Use the default view
            1,        // Just a single click
            0,        // Don't bother with co-ordinates
            0,
            0,
            0,
            false,    // Don't apply any key modifiers
            false,
            false,
            false,
            0,        // 0 - left, 1 - middle, 2 - right
            null);    // Click events don't have any targets other than
        // the recipient of the click
        return elm.dispatchEvent(evt);
    }

    return true;
};

Util.Event.selectLink = function(id, clickOnly)
{
    var link = YUD.get(id);
    if (link == null || link.getAttribute("disabled") == "disabled") return;

    // check for normal onlick assignment
    if (link.onclick)
    {
        link.onclick();
    }
    // try to simulate click
    else
    {
        // execute onclick handlers
        var clickSuccess = this.doEventDispatch(link, 'click');
        if (!clickSuccess) return; // if there was a click and it was aborted, we leave here before navigating to link

        // HACK: This is for mozilla 1.3, if you simulate click above it actually works.. 
        // firefox it does not, so we need to use location.href as well
        if (YAHOO.env.ua.gecko == 1.3 && clickOnly == true) return;

        // go to url
        location.href = link.href;
    }

    /*
    // check if this was assigned onlick through YUI
    var listeners = YAHOO.util.Event.getListeners(link, 'click');

    if (listeners != null)
    {
    for (var i = 0; i < listeners.length; i++)
    {
    var listener = listeners[i];

    if (listener != null && listener.fn != null && listener.obj != null)
    {
    listener.fn(null, listener.obj);
    }
    }
    }
    */
};

// change an asp.net link into a regular html link and assign it a function instead
Util.Event.linkFunction = function(id, func)
{
    var el = document.getElementById(id);
    var js = el.href.replace('javascript:', '');
    el.href = '#';
    YAHOO.util.Event.addListener(el, "click", func, js);
};

// ASP.NET: blocks a linkbutton from being clicked twice
// condition = pass in a function to check if a condition is true before allowing to click on link
Util.Event.blockLink = function(id, blockClass, condition)
{
    // this is for hiding buttons
    var button = YUD.get(id);
    if (button == null) return;

    // keep original href in case it gets removed
    var href = button.href;

    button.execute = function()
    {
        location.href = href;
    };

    var interceptLink = function(e)
    {
        // if button has been canceled somewhere else then return
        if (e != null && e.cancelBubble) return;

        // stop real click event
        if (e != null) YUE.stopEvent(e);

        // if button has been clicked already then return
        //if (button.clicked) return;
        if (YAHOO.util.Dom.hasClass(button, blockClass)) return;

        // validate page before clicking
        if (typeof (Page_ClientValidate) == "function")
        {
            if (!Page_ClientValidate("")) return; // page did not validate
        }

        // check condition before allowing to click, if it fails then abort
        if (typeof (condition) == 'function')
        {
            if (!condition()) return;
        }

        // disable button
        //button.clicked = true;
        YAHOO.util.Dom.addClass(button, blockClass); // add style for button to look disabled

        // run JS or go to URL
        button.execute();

        // enable button after 30 second delay
        setTimeout(function()
        {
            //button.clicked = false;
            YAHOO.util.Dom.removeClass(button, blockClass);
        }, 30000);
    };

    YAHOO.util.Event.addListener(button, "click", interceptLink);
};

// ASP.NET: clicks on a link that had blockLink() applied to it
Util.Event.selectBlockedLink = function(id)
{
    var button = YAHOO.lang.isString(id) ? YUD.get(id) : id;
    if (button == null) return;

    var listeners = YAHOO.util.Event.getListeners(button, "click");

    if (listeners == null) return;

    for (var i = 0; i < listeners.length; ++i)
    {
        var listener = listeners[i];
        listener.fn.call(button);
    }
};

/*****************************************************************************/

Util.Event.hasModifier = function(ev)
{
    return (ev.ctrlKey || ev.altKey || ev.metaKey);
};

// did this key event occur in a text area
Util.Event.inTextInput = function(ev)
{
    var target = YAHOO.util.Event.getTarget(ev);
    return Util.Dom.isTextInput(target);
};

// Inserts a tab character when tab is pressed.
// Modified version of: http://pallieter.org/Projects/insertTab/
Util.Event.insertTab = function(e)
{
    var o = YUE.getTarget(e);
    var kC = e.keyCode ? e.keyCode : e.charCode ? e.charCode : e.which;

    Util.log('textarea: check tab');

    if (kC == 9 && !e.shiftKey && !e.ctrlKey && !e.altKey)
    {
        // original: keydown
        if (e.type == 'keypress') // NOTE: MAC OS 9 1.3 should be keyup // (YAHOO.env.ua.gecko != 1.3) ? 'keydown' : 'keyup'
        {
            Util.log('textarea: insert tab');
            this.insertChar(o, '\t');
        }

        YUE.stopEvent(e);
        return false;
    }

    return true;
};

// o = textarea,textbox
// c = char to insert (e.x., '\t' (tab), '\b' (backspace), 'C')
Util.Event.insertChar = function(o, c)
{
    var oS = o.scrollTop;

    if (o.setSelectionRange)
    {
        var sS = o.selectionStart;
        var sE = o.selectionEnd;
        o.value = o.value.substring(0, sS) + c + o.value.substr(sE);
        o.setSelectionRange(sS + 1, sS + 1);
        o.focus();
    }
    else if (o.createTextRange)
    {
        document.selection.createRange().text = c;
    }

    o.scrollTop = oS;
};

/*****************************************************************************/

// normalized mouse events for desktop/mobile
Util.Event.Mouse = (function()
{
	if ('ontouchstart' in window) {
		return {
			start: 'touchstart',
			end: 'touchend',
			move: 'touchmove',
			click: 'click',
			enter: 'touchenter',
			leave: 'touchleave',
			touchScreen: true
		};
	} else {
		return {
			start: 'mousedown',
			end: 'mouseup',
			move: 'mousemove',
			click: 'click',
			enter: 'mouseenter',
			leave: 'mouseleave',
			touchScreen: false
		};
	}
})();

// normalize a touch event into mouse event
Util.Event.normalize = function(evt) {
    
	// check if touch screen
	if ('ontouchstart' in window && evt.changedTouches) {
	    
		var touches = evt.changedTouches;

		// find touch event that matches dom event
		for (var i = 0, ii = touches.length; i < ii; i++) {
			if (touches[i].target == evt.target) {
				// save original event
				var oldevt = evt; 
				
				// replace mouse event with touch event
				evt = touches[i];
				evt.preventDefault = function() { return oldevt.preventDefault(); };
				evt.stopPropagation = function() { return oldevt.stopPropagation(); };
				break;
			}
		}
	}
	
	return evt;
};


/****************************************/

Util.Event.Custom = function(context) {
    Util.Event.Custom.superclass.constructor.call(this, '', context, true, YAHOO.util.CustomEvent.FLAT, false);
};

YAHOO.lang.extend(Util.Event.Custom, YAHOO.util.CustomEvent);

// simplify the notify function (fire() calls this)
Util.Event.Custom.prototype.notify = function(s, args) {
    
    // get scope and call event function
    var scope = s.getScope(this.scope);
    var ret = s.fn.apply(scope, args);
    
    // if this is a listen once subscription then remove it now
    if (s.listenOnce) {
        this.unsubscribe(s.fn, s.obj);
    }

    return ret;
};

Util.Event.Custom.prototype.subscribeOnce = function(fn, obj, overrideContext) {

    if (!fn) {
        throw new Error("Invalid callback for subscriber to '" + this.type + "'");
    }

    var s = new YAHOO.util.Subscriber(fn, obj, overrideContext);
    s.listenOnce = true; // tells notify() to remove this once called

    // if this event was already fired then
    if (this.fired) {
        this.notify(s, this.firedWith);
    } else {
        this.subscribers.push(s);
    }
    
};
