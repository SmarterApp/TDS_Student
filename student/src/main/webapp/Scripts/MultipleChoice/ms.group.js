/**********************/
/* MS OPTION GROUP */
/**********************/

var ContentMSGroup = function(item)
{
    this._minChoices = 0;
    this._maxChoices = 0;
    ContentMSGroup.superclass.constructor.call(this, item);
};

YAHOO.lang.extend(ContentMSGroup, ContentMCGroup);

ContentMSGroup.prototype.setMinChoices = function(num) {
    this._minChoices = num;
};

ContentMSGroup.prototype.getMinChoices = function () {
    return this._minChoices;
};

ContentMSGroup.prototype.setMaxChoices = function (num) {
    this._maxChoices = num;
};

ContentMSGroup.prototype.getMaxChoices = function () {
    return this._maxChoices;
};

// get the selected option, will return null if nothing is selected
ContentMSGroup.prototype.getSelected = function() 
{
    var selectedOptions = [];

    for (var i = 0; i < this._options.length; i++)
    {
        if (this._options[i].isSelected()) {
            selectedOptions.push(this._options[i]);
        }
    }

    return selectedOptions;
};

// remove all selections
ContentMSGroup.prototype.clear = function() {
    var selected = this.getSelected();
    for (var i = 0; i < selected.length; i++) {
        selected[i].deselect();
    }
};

// get the selected options value, will return null if nothing is selected
ContentMSGroup.prototype.getValue = function()
{
    var options = this.getSelected();
    return options.join(',');
};

// set the value of the option group which will select the option
ContentMSGroup.prototype.setValue = function(value) {

    this.clear();

    if (value == null) {
        return false;
    }
    
    var optionKeys = value.split(',');

    for (var i = 0; i < optionKeys.length; i++) {
        var optionKey = optionKeys[i];
        var option = this.getOption(optionKey);
        if (option != null) option.select();
    }

    return true;
};
