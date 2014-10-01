// These are common functions that are part of YUI3.

// fix so YUI3 stuff works ok
var Y = YAHOO;
Y.Lang = Y.lang; 
Y.UA = YAHOO.env.ua;

// Classes/mixins: 
// \yui\build\yui-base\
(function() {

var L = Y.Lang, 
DELIMITER = '__',
// FROZEN = {
//     'prototype': 1,
//     '_yuid': 1
// },

/*
 * IE will not enumerate native functions in a derived object even if the
 * function was overridden.  This is a workaround for specific functions 
 * we care about on the Object prototype. 
 * @property _iefix
 * @for YUI
 * @param {Function} r  the object to receive the augmentation
 * @param {Function} s  the object that supplies the properties to augment
 * @private
 */
_iefix = function(r, s) {
    var fn = s.toString;
    if (L.isFunction(fn) && fn != Object.prototype.toString) {
        r.toString = fn;
    }
};


/**
 * Returns a new object containing all of the properties of
 * all the supplied objects.  The properties from later objects
 * will overwrite those in earlier objects.  Passing in a
 * single object will create a shallow copy of it.  For a deep
 * copy, use clone.
 * @method merge
 * @for YUI
 * @param arguments {Object*} the objects to merge
 * @return {object} the new merged object
 */
Y.merge = function() {
    var a = arguments, o = {}, i, l = a.length;
    for (i=0; i<l; i=i+1) {
        Y.mix(o, a[i], true);
    }
    return o;
};
   
/**
 * Applies the supplier's properties to the receiver.  By default
 * all prototype and static propertes on the supplier are applied
 * to the corresponding spot on the receiver.  By default all
 * properties are applied, and a property that is already on the
 * reciever will not be overwritten.  The default behavior can
 * be modified by supplying the appropriate parameters.
 *
 * @TODO add constants for the modes
 *
 * @method mix
 * @param {Function} r  the object to receive the augmentation
 * @param {Function} s  the object that supplies the properties to augment
 * @param ov {boolean} if true, properties already on the receiver
 * will be overwritten if found on the supplier.
 * @param wl {string[]} a whitelist.  If supplied, only properties in 
 * this list will be applied to the receiver.
 * @param {int} mode what should be copies, and to where
 *        default(0): object to object
 *        1: prototype to prototype (old augment)
 *        2: prototype to prototype and object props (new augment)
 *        3: prototype to object
 *        4: object to prototype
 * @param merge {boolean} merge objects instead of overwriting/ignoring
 * Used by Y.aggregate
 * @return {object} the augmented object
 */
Y.mix = function(r, s, ov, wl, mode, merge) {

    if (!s||!r) {
        return r || Y;
    }

    if (mode) {
        switch (mode) {
            case 1: // proto to proto
                return Y.mix(r.prototype, s.prototype);
            case 2: // object to object and proto to proto
                Y.mix(r.prototype, s.prototype);
                break; // pass through 
            case 3: // proto to static
                return Y.mix(r, s.prototype);
            case 4: // static to proto
                return Y.mix(r.prototype, s);
            default:  // object to object is what happens below
        }
    }

    // Maybe don't even need this wl && wl.length check anymore??
    var arr = merge && L.isArray(r), i, l, p;

    if (wl && wl.length) {
        for (i = 0, l = wl.length; i < l; ++i) {
            p = wl[i];
            if (p in s) {
                if (merge && L.isObject(r[p], true)) {
                    Y.mix(r[p], s[p]);
                } else if (!arr && (ov || !(p in r))) {
                    r[p] = s[p];
                } else if (arr) {
                    r.push(s[p]);
                }
            }
        }
    } else {
        for (i in s) { 
            // if (s.hasOwnProperty(i) && !(i in FROZEN)) {
                // check white list if it was supplied
                // if the receiver has this property, it is an object,
                // and merge is specified, merge the two objects.
                if (merge && L.isObject(r[i], true)) {
                    Y.mix(r[i], s[i]); // recursive
                // otherwise apply the property only if overwrite
                // is specified or the receiver doesn't have one.
                } else if (!arr && (ov || !(i in r))) {
                    r[i] = s[i];
                // if merge is specified and the receiver is an array,
                // append the array item
                } else if (arr) {
                    r.push(s[i]);
                }
            // }
        }
    
        if (Y.UA.ie) {
            _iefix(r, s);
        }
    }

    return r;
};

/**
 * Returns a wrapper for a function which caches the
 * return value of that function, keyed off of the combined 
 * argument values.
 * @function cached
 * @param source {function} the function to memoize
 * @param cache an optional cache seed
 * @return {Function} the wrapped function
 */
Y.cached = function(source, cache){
    cache = cache || {};

    // I want the profiler to show me separate entries for each
    // cached function.  Is this too much to ask?
    
    // return function cached_sourceFunction
    // return this['cached_' + source.name] = function
    // var a = function(){}; a.name = 'foo'; return a;

    return function cached(arg1, arg2) {

        // (?)()   51  5.76%   0.571ms 1.01ms  0.02ms  0.001ms 0.041ms
        // A() 76  6.58%   0.652ms 0.652ms 0.009ms 0.005ms 0.03ms
        // var key = (arg2 !== undefined) ? Y.Array(arguments, 0, true).join(DELIMITER) : arg1;

        // (?)()   51  8.57%   0.837ms 0.838ms 0.016ms 0.013ms 0.024ms
        // var key = (arguments.length > 1) ? Array.prototype.join.call(arguments, DELIMITER) : arg1;

        // (?)()   51  8.06%  0.761ms 0.762ms 0.015ms 0.002ms 0.025ms
        // var key = (arg2 !== undefined) ? Array.prototype.join.call(arguments, DELIMITER) : arg1;
        
        // (?)()   51  7.87%   0.749ms 0.751ms 0.015ms 0.001ms 0.027ms
        // A() 30  2.23%   0.214ms 0.214ms 0.007ms 0.005ms 0.009ms
        var key = (arg2) ? Array.prototype.join.call(arguments, DELIMITER) : arg1;

        if (!(key in cache)) {
            cache[key] = source.apply(source, arguments);
        }

        return cache[key];
    };

};

})();

// ARRAY: 
// \yui\build\yui-base\
(function() {

var L = Y.Lang, Native = Array.prototype

/**
 * Adds the following array utilities to the YUI instance.  Additional
 * array helpers can be found in the collection component.
 * @class Array
 */

/** 
 * Y.Array(o) returns an array:
 * - Arrays are return unmodified unless the start position is specified.
 * - "Array-like" collections (@see Array.test) are converted to arrays
 * - For everything else, a new array is created with the input as the sole item
 * - The start position is used if the input is or is like an array to return
 *   a subset of the collection.
 *
 *   @TODO this will not automatically convert elements that are also collections
 *   such as forms and selects.  Passing true as the third param will
 *   force a conversion.
 *
 * @method ()
 * @static
 *   @param o the item to arrayify
 *   @param i {int} if an array or array-like, this is the start index
 *   @param al {boolean} if true, it forces the array-like fork.  This
 *   can be used to avoid multiple array.test calls.
 *   @return {Array} the resulting array
 */
YArray = function(o, startIdx, al) {
    var t = (al) ? 2 : Y.Array.test(o), i, l, a;

    // switch (t) {
    //     case 1:
    //         // return (startIdx) ? o.slice(startIdx) : o;
    //     case 2:
    //         return Native.slice.call(o, startIdx || 0);
    //     default:
    //         return [o];
    // }

    if (t) {
        try {
            return Native.slice.call(o, startIdx || 0);
        // IE errors when trying to slice element collections
        } catch(e) {
            a=[];
            for (i=0, l=o.length; i<l; i=i+1) {
                a.push(o[i]);
            }
            return a;
        }
    } else {
        return [o];
    }

};

YAHOO.Array = YArray;

/** 
 * Evaluates the input to determine if it is an array, array-like, or 
 * something else.  This is used to handle the arguments collection 
 * available within functions, and HTMLElement collections
 *
 * @method test
 * @static
 *
 * @todo current implementation (intenionally) will not implicitly 
 * handle html elements that are array-like (forms, selects, etc).  
 *
 * @return {int} a number indicating the results:
 * 0: Not an array or an array-like collection
 * 1: A real array. 
 * 2: array-like collection.
 */
YArray.test = function(o) {
    var r = 0;
    if (L.isObject(o)) {
        if (L.isArray(o)) {
            r = 1; 
        } else {
            try {
                // indexed, but no tagName (element) or alert (window)
                if ("length" in o && !("tagName" in o) && !("alert" in o) && 
                    (!Y.Lang.isFunction(o.size) || o.size() > 1)) {
                    r = 2;
                }
                    
            } catch(e) {}
        }
    }
    return r;
};

/**
 * Executes the supplied function on each item in the array.
 * @method each
 * @param a {Array} the array to iterate
 * @param f {Function} the function to execute on each item
 * @param o Optional context object
 * @static
 * @return {YUI} the YUI instance
 */
YArray.each = (Native.forEach) ?
    function (a, f, o) { 
        Native.forEach.call(a || [], f, o || Y);
        return Y;
    } :
    function (a, f, o) { 
        var l = (a && a.length) || 0, i;
        for (i = 0; i < l; i=i+1) {
            f.call(o || Y, a[i], i, a);
        }
        return Y;
    };

/**
 * Returns an object using the first array as keys, and
 * the second as values.  If the second array is not
 * provided the value is set to true for each.
 * @method hash
 * @static
 * @param k {Array} keyset
 * @param v {Array} optional valueset
 * @return {object} the hash
 */
YArray.hash = function(k, v) {
    var o = {}, l = k.length, vl = v && v.length, i;
    for (i=0; i<l; i=i+1) {
        o[k[i]] = (vl && vl > i) ? v[i] : true;
    }

    return o;
};

/**
 * Returns the index of the first item in the array
 * that contains the specified value, -1 if the
 * value isn't found.
 * @method indexOf
 * @static
 * @param a {Array} the array to search
 * @param val the value to search for
 * @return {int} the index of the item that contains the value or -1
 */
YArray.indexOf = (Native.indexOf) ?
    function(a, val) {
        return a.indexOf(val);
    } :
    function(a, val) {
        for (var i=0; i<a.length; i=i+1) {
            if (a[i] === val) {
                return i;
            }
        }

        return -1;
    };

/**
 * Numeric sort convenience function.
 * Y.ArrayAssert.itemsAreEqual([1, 2, 3], [3, 1, 2].sort(Y.Array.numericSort));
 * @method numericSort
 */
YArray.numericSort = function(a, b) { 
    return (a - b); 
};

/**
 * Executes the supplied function on each item in the array.
 * Returning true from the processing function will stop the 
 * processing of the remaining
 * items.
 * @method some
 * @param a {Array} the array to iterate
 * @param f {Function} the function to execute on each item
 * @param o Optional context object
 * @static
 * @return {boolean} true if the function returns true on
 * any of the items in the array
 */
 YArray.some = (Native.some) ?
    function (a, f, o) { 
        return Native.some.call(a, f, o);
    } :
    function (a, f, o) {
        var l = a.length, i;
        for (i=0; i<l; i=i+1) {
            if (f.call(o, a[i], i, a)) {
                return true;
            }
        }
        return false;
    };

})();

// COLLECTION
// \yui\build\collection\

(function() {

/**
 * Collection utilities beyond what is provided in the YUI core
 * @module collection
 */

var L = Y.Lang, Native = Array.prototype, A = Y.Array;

/**
 * Adds the following array utilities to the YUI instance
 * (Y.Array).  This is in addition to the methods provided
 * in the core.
 * @class YUI~array~extras
 */

/**
 * Returns the index of the last item in the array
 * that contains the specified value, -1 if the
 * value isn't found.
 * method Array.lastIndexOf
 * @static
 * @param a {Array} the array to search
 * @param val the value to search for
 * @return {int} the index of hte item that contains the value or -1
 */
A.lastIndexOf = (Native.lastIndexOf) ?
    function(a ,val) {
        return a.lastIndexOf(val);    
    } :
    function(a, val) {
        for (var i=a.length-1; i>=0; i=i-1) {
            if (a[i] === val) {
                break;
            }
        }
        return i;
    };

/**
 * Returns a copy of the array with the duplicate entries removed
 * @method Array.unique
 * @static
 * @param a {Array} the array to find the subset of uniques for
 * @param sort {bool} flag to denote if the array is sorted or not. Defaults to false, the more general operation
 * @return {Array} a copy of the array with duplicate entries removed
 */
A.unique = function(a, sort) {
    var s = L.isValue(sort) ? sort : false,
        b = a.slice(), i = 0, n = -1, item = null;
    if (s) {
        while (i < b.length) {
            if (b[i] === item) {
                n = (n == -1 ? i : n);
                i += 1;
            } else if (n !== -1) {
                b.splice(n, i-n);
                i = n;                
                n = -1;
            } else {
                item = b[i];
                i += 1;
            }
        }
        return b;
    } else {
        while (i < b.length) {
            item = b[i];
            while ((n = b.lastIndexOf(item)) !== i) {
                b.splice(n, 1);
            }
            i += 1;
        }
        return b;
    }
};

/**
* Executes the supplied function on each item in the array.
* Returns a new array containing the items that the supplied
* function returned true for.
* @method Array.filter
* @param a {Array} the array to iterate
* @param f {Function} the function to execute on each item
* @param o Optional context object
* @static
* @return {Array} The items on which the supplied function
* returned true. If no items matched an empty array is 
* returned.
*/
A.filter = (Native.filter) ?
    function(a, f, o) {
        return Native.filter.call(a, f, o);
    } :
    function(a, f, o) {
        var results = [];
        A.each(a, function(item, i, a) {
            if (f.call(o, item, i, a)) {
                results.push(item);
            }
        });

        return results;
    };

/**
* The inverse of filter. Executes the supplied function on each item. 
* Returns a new array containing the items that the supplied
* function returned *false* for.
* @method Array.reject
* @param a {Array} the array to iterate
* @param f {Function} the function to execute on each item
* @param o Optional context object
* @static
* @return {Array} The items on which the supplied function
* returned false.
*/
A.reject = function(a, f, o) {
    return A.filter(a, function(item, i, a) {
        return !f.call(o, item, i, a);
    });
};

/**
* Executes the supplied function on each item in the array.
* @method Array.every
* @param a {Array} the array to iterate
* @param f {Function} the function to execute on each item
* @param o Optional context object
* @static
* @return {boolean} true if every item in the array returns true
* from the supplied function.
*/
A.every = (Native.every) ?
    function(a, f, o) {
        return Native.every.call(a,f,o);
    } :
    function(a, f, o) {
        var l = a.length;
        for (var i = 0; i < l; i=i+1) {
            if (!f.call(o, a[i], i, a)) {
                return false;
            }
        }

        return true;
    };

/**
* Executes the supplied function on each item in the array.
* @method Array.map
* @param a {Array} the array to iterate
* @param f {Function} the function to execute on each item
* @param o Optional context object
* @static
* @return {Array} A new array containing the return value
* of the supplied function for each item in the original
* array.
*/
A.map = (Native.map) ? 
    function(a, f, o) {
        return Native.map.call(a, f, o);
    } :
    function(a, f, o) {
        var results = [];
        A.each(a, function(item, i, a) {
            results.push(f.call(o, item, i, a));
        });
        return results;
    };


/**
* Executes the supplied function on each item in the array.
* Reduce "folds" the array into a single value.
* @method Array.reduce
* @param a {Array} the array to iterate
* @param init The initial value to start from
* @param f {Function} the function to execute on each item. It
* is responsible for returning the updated value of the
* computation.
* @param o Optional context object
* @static
* @return A value that results from iteratively applying the
* supplied function to each element in the array.
*/
A.reduce = (Native.reduce) ?
    function(a, init, f, o) {
        //Firefox's Array.reduce does not allow inclusion of a
        //  thisObject, so we need to implement it manually
        return Native.reduce.call(a, function(init, item, i, a) {
            return f.call(o, init, item, i, a);
        }, init);
    } :
    function(a, init, f, o) {
        var r = init;
        A.each(a, function (item, i, a) {
            r = f.call(o, r, item, i, a);
        });
        return r;
    };


/**
* Executes the supplied function on each item in the array,
* searching for the first item that matches the supplied
* function.
* @method Array.find
* @param a {Array} the array to search
* @param f {Function} the function to execute on each item. 
* Iteration is stopped as soon as this function returns true
* on an item.
* @param o Optional context object
* @static
* @return {object} the first item that the supplied function
* returns true for, or null if it never returns true
*/
A.find = function(a, f, o) {
    var l = a.length;
    for(var i=0; i < l; i++) {
        if (f.call(o, a[i], i, a)) {
            return a[i];
        }
    }
    return null;
};

/**
* Iterates over an array, returning a new array of all the elements
* that match the supplied regular expression
* @method Array.grep
* @param a {Array} a collection to iterate over
* @param pattern {RegExp} The regular expression to test against 
* each item
* @static
* @return {Array} All the items in the collection that 
* produce a match against the supplied regular expression. 
* If no items match, an empty array is returned.
*/
A.grep = function (a, pattern) {
    return A.filter(a, function (item, index) {
        return pattern.test(item);
    });
};


/**
* Partitions an array into two new arrays, one with the items
* that match the supplied function, and one with the items that
* do not.
* @method Array.partition
* @param a {Array} a collection to iterate over
* @paran f {Function} a function that will receive each item 
* in the collection and its index.
* @param o Optional execution context of f.
* @static
* @return An object with two members, 'matches' and 'rejects',
* that are arrays containing the items that were selected or 
* rejected by the test function (or an empty array).
*/
A.partition = function (a, f, o) {
    var results = {matches: [], rejects: []};
    A.each(a, function (item, index) {
        var set = f.call(o, item, index, a) ? results.matches : results.rejects;
        set.push(item);
    });
    return results;
};

/**
* Creates an array of arrays by pairing the corresponding
* elements of two arrays together into a new array.
* @method Array.zip
* @param a {Array} a collection to iterate over
* @param a2 {Array} another collection whose members will be 
* paired with members of the first parameter
* @static
* @return An array of arrays formed by pairing each element 
* of the first collection with an item in the second collection 
* having the corresponding index.
*/
A.zip = function (a, a2) {
    var results = [];
    A.each(a, function (item, index) {
        results.push([item, a2[index]]);
    });
    return results;
};

})();
