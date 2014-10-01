/**********************/
/* MS OPTION          */
/**********************/

var ContentMSOption = function(options, key)
{
    ContentMSOption.superclass.constructor.call(this, options, key);
};

YAHOO.lang.extend(ContentMSOption, ContentMCOption);

// check if the selection can be made
ContentMSOption.prototype._validateSelection = function() {

    var maxChoices = this._options.getMaxChoices();
    var selected = this._options.getSelected();
    var selectedCount = selected.length;

    // check if max choices was met
    if (maxChoices == 1 && selectedCount > 0) {
        selected[0].deselect(); // deselect existing option as a convenience
    } else if (maxChoices > 0 && maxChoices <= selectedCount) {
        return false; // max has been met
    }

    // selection is allowed
    return true;
};

ContentMSOption.prototype.select = function()
{
    // toggle checkbox
    var checkbox = this.getRadioButton();

    if (checkbox.checked) {
        this.deselect();
    } else {

        // check if we can select this choice
        if (!this._validateSelection()) {
            return false;
        }

        // select checkbox input
        checkbox.checked = true;

        // add selected css
        YUD.addClass(this.getElement(), 'optionSelected');

        // show feedback
        var page = this._options._item.getPage();
        var pageAccProps = page.getAccommodationProperties();
        if (pageAccProps != null && pageAccProps.showFeedback()) this.showFeedback();
    }

    // TDS notification
    /*if (typeof (window.tdsUpdateItemResponse) == 'function')
    {
        // get current options position and notify TDS
        var position = this._options._item.position;
        window.tdsUpdateItemResponse(position, this.key);
    }*/

    return true;
};