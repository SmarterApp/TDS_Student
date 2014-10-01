/**********************/
/* MC OPTION GROUP    */
/**********************/

// Collection of MC options
var ContentMCGroup = function(item)
{
    this._item = item;
    this._options = [];
    this._optionHash = {};
};

// get item that belongs to these options
ContentMCGroup.prototype.getItem = function() { return this._item; };

// add new option
ContentMCGroup.prototype.addOption = function(option)
{
    // store option
    this._options.push(option);
    this._optionHash[option.key] = option;
};

// get all options
ContentMCGroup.prototype.getOptions = function() { return this._options; };

// get option by key or order number
ContentMCGroup.prototype.getOption = function(optionKey)
{
    var option = null;

    if (YLang.isString(optionKey))
    {
        optionKey = optionKey.toUpperCase();
        option = this._optionHash[optionKey] || null;
    }
    else if (YLang.isNumber(optionKey))
    {
       option = this._options[optionKey - 1] || null; // e.x., 1 = 'A', 2 = 'B'
    }
    
    return option;
};

// get the selected option, will return null if nothing is selected
ContentMCGroup.prototype.getSelected = function()
{
    for (var i = 0; i < this._options.length; i++)
    {
        if (this._options[i].isSelected()) return this._options[i];
    }

    return null;
};

// remove all selections
ContentMCGroup.prototype.clear = function() {
    var selected = this.getSelected();
    if (selected != null) selected.deselect();
};

// get the selected options value, will return null if nothing is selected
ContentMCGroup.prototype.getValue = function()
{
    var option = this.getSelected();
    return (option) ? option.key : null;
};

// set the value of the option group which will select the option
ContentMCGroup.prototype.setValue = function(optionKey)
{
    var option = this.getOption(optionKey);
    if (!option) return false; // no option with this key

    // option.getRadioButton().click();
    option.select();
    return true;
};

// get the focused option
ContentMCGroup.prototype.getFocusedOption = function()
{
    var focusedComponent = this.getItem().getActiveComponent();

    for (var i = 0; i < this._options.length; i++)
    {
        var option = this._options[i];
        if (option.getElement() == focusedComponent) return option;
    }

    return null;
};
