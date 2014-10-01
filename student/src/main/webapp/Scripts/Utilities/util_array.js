// REQUIRES: none

// for browsers that don't support indexOf (basically Mozilla 1.3)
// http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array:indexOf
if (!Array.prototype.indexOf)
{
    Array.prototype.indexOf = function(elt /*, from*/)
    {
        var len = this.length;

        var from = Number(arguments[1]) || 0;
        from = (from < 0)
         ? Math.ceil(from)
         : Math.floor(from);
        if (from < 0)
            from += len;

        for (; from < len; from++)
        {
            if (from in this &&
          this[from] === elt)
                return from;
        }
        return -1;
    };
}

/*************************/

if (typeof (YAHOO.Array) != 'undefined')
{
    Util.Array = YAHOO.Array;
    Util.Array.uniqueList = YAHOO.Array.unique;
}
else
{
    Util.Array = {};
}

Util.Array._ARRAY_PROTOTYPE = Array.prototype;

Util.Array.contains = function(arr, obj)
{
    return arr.indexOf(obj) >= 0;
};

Util.Array.concat = function(var_args)
{
    return Util.Array._ARRAY_PROTOTYPE.concat.apply(Util.Array._ARRAY_PROTOTYPE, arguments);
};

/**
 * Adds or removes elements from an array. This is a generic version of Array
 * splice. This means that it might work on other objects similar to arrays,
 * such as the arguments object.
 *
 * @param {goog.array.ArrayLike} arr The array to modify.
 * @param {number|undefined} index The index at which to start changing the
 *     array. If not defined, treated as 0.
 * @param {number} howMany How many elements to remove (0 means no removal. A
 *     value below 0 is treated as zero and so is any other non number. Numbers
 *     are floored).
 * @param {...*} var_args Optional, additional elements to insert into the
 *     array.
 * @return {!Array} the removed elements.
 */
Util.Array.splice = function(arr, index, howMany, var_args) 
{
    Util.Asserts.assert(arr.length != null);
    return Util.Array._ARRAY_PROTOTYPE.splice.apply(arr, Util.Array.slice(arguments, 1));
};

/**
 * Returns a new array from a segment of an array. This is a generic version of
 * Array slice. This means that it might work on other objects similar to
 * arrays, such as the arguments object.
 *
 * @param {goog.array.ArrayLike} arr The array from which to copy a segment.
 * @param {number} start The index of the first element to copy.
 * @param {number=} opt_end The index after the last element to copy.
 * @return {!Array} A new array containing the specified segment of the original
 *     array.
 */
Util.Array.slice = function(arr, start, opt_end) 
{
    Util.Asserts.assert(arr.length != null);

    // passing 1 arg to slice is not the same as passing 2 where the second is
    // null or undefined (in that case the second argument is treated as 0).
    // we could use slice on the arguments object and then use apply instead of
    // testing the length
    if (arguments.length <= 2) 
    {
        return Util.Array._ARRAY_PROTOTYPE.slice.call(arr, start);
    }
    else 
    {
        return Util.Array._ARRAY_PROTOTYPE.slice.call(arr, start, opt_end);
    }
};

Util.Array.removeAt = function(arr, i)
{
    // use generic form of splice
    // splice returns the removed items and if successful the length of that
    // will be 1
    return Array.prototype.splice.call(arr, i, 1).length == 1;
};

/**
* Removes the first occurrence of a particular value from an array.
* @param {goog.array.ArrayLike} arr Array from which to remove value.
* @param {*} obj Object to remove.
* @return {boolean} True if an element was removed.
*/
Util.Array.remove = function(arr, obj)
{
    var i = arr.indexOf(obj);
    var rv;
    if ((rv = i >= 0))
    {
        Util.Array.removeAt(arr, i);
    }
    return rv;
};

Util.Array.clear = function(arr)
{
    // For non real arrays we don't have the magic length so we delete the indices.
    if (!YAHOO.lang.isArray(arr))
    {
        for (var i = arr.length - 1; i >= 0; i--)
        {
            delete arr[i];
        }
    }
    
    arr.length = 0;
};

// Whether the array is empty.
Util.Array.isEmpty = function(arr)
{
    return arr.length == 0;
};

/**
 * Inserts an object at the given index of the array.
 * @param {goog.array.ArrayLike} arr The array to modify.
 * @param {*} obj The object to insert.
 * @param {number=} opt_i The index at which to insert the object. If omitted,
 *      treated as 0. A negative index is counted from the end of the array.
 */
Util.Array.insertAt = function (arr, obj, opt_i) {
    Util.Array.splice(arr, opt_i, 0, obj);
};

// insert the target before the obj
Util.Array.insertBefore = function(arr, obj, target) {
    var idx = arr.indexOf(obj);
    if (idx == -1) {
        return false;
    } else {
        if (idx == 0) {
            arr.unshift(target);
        } else {
            Util.Array.insertAt(arr, target, idx);
        }
        return true;
    }
};

// insert the target after the obj
Util.Array.insertAfter = function (arr, obj, target) {
    var idx = arr.indexOf(obj);
    if (idx == -1) {
        return false;
    } else {
        if (idx == arr.length - 1) {
            arr.push(target);
        } else {
            Util.Array.insertAt(arr, target, idx + 1);
        }
        return true;
    }
};

// get unique values in an array fast
Util.Array.unique = function(list)
{
    // create variables
    var bucket = {}, i, l = list.length, uniqueList = [];

    // create hash bucket of unique values
    for (i = 0; i < l; i++)
    {
        if (list[i].length == 0) continue; // skip empty
        bucket[list[i]] = null; // .toLowerCase()
    }

    // add hash keys to unique list array
    for (key in bucket)
    {
        uniqueList.push(key);
    }

    return uniqueList;
};

// Returns the first defined value of the array passed in, or null.
Util.Array.pick = function(values)
{
    for (var i = 0, l = values.length; i < l; i++)
    {
        if (values[i] != null) return values[i];
    }
    
    return null;
};

/**
 * Returns the last element in an array without removing it.
 * @param {goog.array.ArrayLike} array The array.
 * @return {*} Last item in array.
 */
Util.Array.peek = function(array) {
  return array[array.length - 1];
};


/**
* Calls f for each element of an array. If any call returns true, some()
* returns true (without checking the remaining elements). If all calls
* return false, some() returns false.
*/
Util.Array.some = Util.Array._ARRAY_PROTOTYPE.some ?
    function(arr, f, opt_obj)
    {
        return Util.Array._ARRAY_PROTOTYPE.some.call(arr, f, opt_obj);
    } :
    function(arr, f, opt_obj)
    {
        var l = arr.length;  // must be fixed during loop... see docs
        var arr2 = YAHOO.lang.isString(arr) ? arr.split('') : arr;
        for (var i = 0; i < l; i++)
        {
            if (i in arr2 && f.call(opt_obj, arr2[i], i, arr))
            {
                return true;
            }
        }
        return false;
    };


/**
 * Returns an array consisting of every argument with all arrays
 * expanded in-place recursively.
 *
 * @param {...*} var_args The values to flatten.
 * @return {!Array.<*>} An array containing the flattened values.
 */
Util.Array.flatten = function(var_args) 
{
    var result = [];
    for (var i = 0; i < arguments.length; i++) 
    {
        var element = arguments[i];
        if (YAHOO.lang.isArray(element)) 
        {
            result.push.apply(result, Util.Array.flatten.apply(null, element));
        }
        else 
        {
            result.push(element);
        }
    }
    return result;
};

/**
 * Compares its two arguments for order, using the built in < and >
 * operators.
 * @param {*} a The first object to be compared.
 * @param {*} b The second object to be compared.
 * @return {number} A negative number, zero, or a positive number as the first
 *     argument is less than, equal to, or greater than the second.
 */
Util.Array.defaultCompare = function(a, b) {
  return a > b ? 1 : a < b ? -1 : 0;
};

/**
 * Sorts the specified array into ascending order.  If no opt_compareFn is
 * specified, elements are compared using
 * <code>Util.Array.defaultCompare</code>, which compares the elements using
 * the built in < and > operators.  This will produce the expected behavior
 * for homogeneous arrays of String(s) and Number(s), unlike the native sort,
 * but will give unpredictable results for heterogenous lists of strings and
 * numbers with different numbers of digits.
 *
 * This sort is not guaranteed to be stable.
 *
 * Runtime: Same as <code>Array.prototype.sort</code>
 *
 * @param {Array} arr The array to be sorted.
 * @param {Function=} opt_compareFn Optional comparison function by which the
 *     array is to be ordered. Should take 2 arguments to compare, and return a
 *     negative number, zero, or a positive number depending on whether the
 *     first argument is less than, equal to, or greater than the second.
 */
Util.Array.sort = function(arr, opt_compareFn)
{
    if (arr.length == null) return;
    Array.prototype.sort.call(arr, opt_compareFn || Util.Array.defaultCompare);
};

// http://stackoverflow.com/questions/966225/how-to-create-a-two-dimensional-array-in-javascript
// http://stackoverflow.com/questions/6053108/javascript-4d-arrays/6053332#6053332
// TODO: fix this function it does not work (see message_system.js line 158)
Util.Array.createMultiDimArray = function() // multiDimensionalArray
{
    var args = Array.prototype.slice.call(arguments);

    function helper(arr)
    {
        if (arr.length <= 0) return null;
        if (arr.length == 1) return new Array(arr[0]);

        var currArray = new Array(arr[0]);
        var newArgs = arr.slice(1, arr.length);

        for (var i = 0; i < currArray.length; i++)
        {
            currArray[i] = helper(newArgs);
        }

        return currArray;
    }

    return helper(args);
};