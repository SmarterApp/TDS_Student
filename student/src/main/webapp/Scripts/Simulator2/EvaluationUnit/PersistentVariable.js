﻿/**
 * *****************************************************************************
 * @class PersistentVariable 
 * @superclass Variable
 * @param none
 * @return - instance of PersistentVariable
 * 
 *******************************************************************************
 */
SimParser.PersistentVariable = function () {

    // Inherit instance variables
    SimParser.Variable.call(this);
    
    // used to identify the name of the stored variable
    var store;
    
    // setters and getters
    this.getStore = function () {
        return store;
    }
    
    this.setStore = function (s) {
        store = s;
        return this;
    }

    // function for setting variable attributes
    SimParser.PersistentVariable.prototype.setAttributes = function (attr, node) {
        
        // call inherited method
        SimParser.Variable.prototype.setAttributes.call(this, attr, node);
        
        // set the 'store' attribute
        if (attr.store !== undefined) {
            this.setStore(attr.store);
        }
        // set the 'cumOp' attribute
        if (attr.comOp !== undefined) {
            this.setStore(attr.cumOP);
        }
    }

    this.setEname('PersistentVariable');

};

SimParser.PersistentVariable.prototype = new SimParser.PersistentVariable();
SimParser.PersistentVariable.prototype.constructor = SimParser.Variable;