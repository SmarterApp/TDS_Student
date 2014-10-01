// A generic event manager where you can fire/subscribe to events that might not exist.
// NOTE: Uses YUI event provider internally.
Util.EventManager = function(context)
{
    this._globalContext = context || null;
    this._events = new YAHOO.util.EventProvider();
};

Util.EventManager.prototype.subscribe = function(type, callback, overrideContext, cancellable)
{
    if (!YAHOO.lang.isString(type)) return false;
    if (!YAHOO.lang.isFunction(callback)) return false;

    type = type.toLowerCase();

    // lazily create the actual event if it does not exist
    if (!this._events.hasEvent(type))
    {
        // set a default context to event if it exists
        if (this._globalContext) this._events.createEvent(type, { scope: this._globalContext });
        else this._events.createEvent(type);
    }

    // YUI event will fire with an array of args
    var callbackEvent = function(args /*array*/)
    {
        // map array of args as function arguments instead of an array
        // WARNING: If event subscriber returns false from here then it cancels all other subscribers
        var ret;
        if (args) ret = callback.apply(this, args);
        else ret = callback.call(this);

        // if this event is cancellable then return the callback value
        if (cancellable === true) return ret;
        else return true;
    };

    // subscribe to event
    this._events.subscribe(type, callbackEvent, null, overrideContext || this._globalContext);
    return true;
};

// fire event using array of objects
Util.EventManager.prototype.notify = function(type, args /*array*/)
{
    if (!YAHOO.lang.isString(type)) return false;
    type = type.toLowerCase();

    // fire event
    return this._events.fireEvent(type, args);
};

// fire event using params of objects
Util.EventManager.prototype.fire = function(type /* ... */)
{
    // get arguments as an array but skip the 'type' argument
    // var args = [].splice.call(arguments, 1);
    var args = Array.prototype.slice.call(arguments, 1);

    // fire event
    return this.notify(type, args);
};