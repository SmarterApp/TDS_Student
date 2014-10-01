Util.Object = function() {};

// return all the keys that belong to the instance of this object
Util.Object.keys = function(object) {
    var keys = [];
    for (var key in object) {
        if (object.hasOwnProperty(key)) {
            keys.push(key);
        }
    }
    return keys;
};

// return all this objects values
Util.Object.values = function(object) {
    var values = [];
    for (var key in object) {
        if (object.hasOwnProperty(key)) {
            values.push(object[key]);
        }
    }

    return values;
};

// check if object has no keys
Util.Object.isEmpty = function(object) {
    for (var key in object) {
        if (object.hasOwnProperty(key)) {
            return false;
        }
    }

    return true;
};

// Removes a key-value pair based on the key.
Util.Object.remove = function(obj, key) {
    var rv;
    if ((rv = key in obj)) {
        delete obj[key];
    }
    return rv;
};

// Safe way to clone native objects.
// NOTE: Something like http://stackoverflow.com/a/12593036 DOES NOT WORK in ipad/fx
// NOTE: Check out implementing: https://github.com/bestiejs/lodash/blob/master/lodash.js#L959
// NOTE: You can also use JSON.parse(JSON.stringify(obj)) for cloning.
Util.Object.clone = function(obj) {

    if (obj == null || typeof(obj) != 'object') {
        return obj;
    }
    var newObj = {};

    for (var prop in obj) {

        (function(prop) {

            if (typeof obj[prop] == 'function') {
                // must call native function directly or FX throws WrappedNative error
                newObj[prop] = function() {
                    obj[prop]();
                };
            } else {
                // set value
                newObj[prop] = obj[prop];
            }

        })(prop);
    }

    return newObj;
};

/******************/

/**
* Returns the values of the object/map/hash.
*
* @param {Object} obj The object from which to get the values.
* @return {!Array} The values in the object/map/hash.
*/
Util.Object.getValues = function(obj) {
    var res = [];
    var i = 0;
    for (var key in obj) {
        res[i++] = obj[key];
    }
    return res;
};

/**
* Returns the keys of the object/map/hash.
*
* @param {Object} obj The object from which to get the keys.
* @return {!Array.<string>} Array of property keys.
*/
Util.Object.getKeys = function(obj) {
    var res = [];
    var i = 0;
    for (var key in obj) {
        res[i++] = key;
    }
    return res;
};

// json filler
if (typeof JSON != 'object') {
    JSON = {};
    JSON.stringify = YAHOO.lang.JSON.stringify;
    JSON.parse = YAHOO.lang.JSON.parse;
}