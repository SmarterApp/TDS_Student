/** **************************************************************************
* @class Whiteboard
* @superclass none
* @param none
* @return Whiteboard instance
* Creates a new Whiteboard class.
* Note: This is a singleton class
*****************************************************************************
*/
Simulator.Whiteboard = function (sim) {

    var categories = [];

    var source = 'Whiteboard';

    var dbg = function () {return sim.getDebug();};

    var key = Math.random()*1000;
    
    this.addCategory = function(catName) {
        if(catName in categories) return false;
        else {
            categories[catName] = [];
            return true;
        }
    };
    
    this.getCategory = function(name) {
        if(name in categories) return categories[name];
        else return null;
    };
    
    this.getCategoryAsString = function(name, separator) {
        var buff = [];
        var num = 0;
        if(name in categories) {
            var cat = categories[name];
            for(var item in cat) {
                if(cat.hasOwnProperty(item)) {
                    if(num > 0) buff.push(Simulator.Constants.PAIR_DELIMITTER + ' ');
                    buff.push(item); 
                    if(separator) buff.push(Simulator.Constants.KEY_VALUE_DELIMITTER + ' '); 
                    //if(separator) buff.push(Simulator.Constants.KEY_VALUE_DELIMITTER + ' ['); 
                    else buff.push(Simulator.Constants.KEY_VALUE_DELIMITTER);
                    //else buff.push(Simulator.Constants.KEY_VALUE_DELIMITTER + '[');
                    buff.push(this.getItem(name, item)); //buff.push(']');
                    num++;
                }
            }
            return buff.join('');
        }
        else return null;
    };
    
    this.categoryExists = function(categoryName) {
        return category in categories;
    };
    
    this.itemExists = function(categoryName, itemName) {
        if(categoryName in categories) {
            var category = categories[categoryName];
            return (itemName in category);
        }
        else return false;
    };
    
    this.addItem = function(category, item) {
        if(category in categories) {
            var cat= categories[category];
            if(item in cat) return null;
            else {
                cat[item] = null;
                return key;
            }
        } else return null;
    };
    
    this.setItem = function(category, item, value, itemKey) {
        if(category in categories) {
            if(key == itemKey) {
                var cat = categories[category];
                cat[item] = value;
                return true;
            }
            else return false;
        }
        else return false;
    };
    
    this.getItem = function(category, theItem) {
        if(category in categories) {
            cat = categories[category];
            if(cat[theItem]) return cat[theItem];
            else return null;
        }
        else return null;
    };
    
    this.clearCategory = function(category) {
        if(categories[category]) categories[category] = [];
    };
    
    this.clearWhiteboard = function() {
        for(var p in categories) {
            categories[p] = [];
        }
    };
    
    this.inspect = function(embedded, force) {
        var buff = [];
        var sep = '\n\n';
        buff.push('Inspecting Whiteboard:'); buff.push(sep);
        for ( var i in categories) {
            var aCat = categories[i];
            buff.push('Inspecting '); buff.push(i); buff.push(' category:'); buff.push(sep);
            for(var k in aCat) {
                buff.push(i); buff.push('['); buff.push(k); buff.push('] = '); buff.push(aCat[k]); buff.push(sep);
            }
        }
        if(!embedded) (force) ? dbg().debugf(buff.join(''), null, true) : dbg().debug(buff.join(''), null, true);
        return buff.join('');
    };


};

