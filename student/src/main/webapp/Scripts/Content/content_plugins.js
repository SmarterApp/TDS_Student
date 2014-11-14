/*
Plugins api entity and page object.
*/

ContentManager.Plugins = (function () {

    function Plugins() {
        this._lookup = new Util.Structs.Map();
        Util.Event.Emitter(this);
    }

    // remove a plugin
    Plugins.prototype.remove = function(plugin) { // removePlugins()
        return Util.Array.remove(this._lookup, plugin);
    };

    // add a plugin
    Plugins.prototype.add = function(name, plugin) { // addPlugin
        this._lookup.set(name, plugin);
        this.fire('add', plugin);
    };

    // get all the plugin names
    Plugins.prototype.getNames = function() { // getPluginNames
        return this._lookup.getKeys();
    };

    // get all the plugins
    Plugins.prototype.getAll = function () { // getPlugins
        return this._lookup.getValues();
    };

    Plugins.prototype.has = function(name) {
        return this._lookup.containsKey(name);
    };

    Plugins.prototype.get = function(name) { // getPlugin
        return this._lookup.get(name);
    };

    Plugins.prototype.remove = function (name) { // removePlugin
        var removed = this._lookup.remove(name);
        if (removed) {
            this.fire('remove', plugin);
        }
    };

    Plugins.prototype.clear = function() {
        this._lookup.clear();
    };

    return Plugins;

})();