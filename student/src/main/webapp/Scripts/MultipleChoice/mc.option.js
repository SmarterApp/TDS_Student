/**********************/
/* MC OPTION          */
/**********************/

// Single MC option
var ContentMCOption = function(options, key)
{
    this._options = options;
    this.key = key;
    this.audioLink = null;
    this.tts = null;
    this.feedback = null; // LPN feedback html
};

ContentMCOption.prototype.toString = function() { return this.key; };

// get the container div around the option
ContentMCOption.prototype.getElement = function()
{
    var item = this._options.getItem();
    var doc = item.getPage().getDoc();
    return doc.getElementById('Item_OptionContainer_Response_MC_' + item.position + '_' + this.key);
};

// get form radio group
ContentMCOption.prototype.getRadioGroup = function()
{
    var item = this._options.getItem();
    var form = item.getPage().getForm();
    return form['Item_Response_MC_' + item.position];
};

// get form radio button element
ContentMCOption.prototype.getRadioButton = function()
{
    var item = this._options.getItem();
    var form = item.getPage().getForm();
    return form['Item_Response_MC_' + item.position + '_' + this.key];
};

// get sound anchor tag element
ContentMCOption.prototype.getSoundLink = function()
{
    var item = this._options.getItem();
    var doc = item.getPage().getDoc();

    var soundDIV = doc.getElementById('Item_OptionSound_Response_MC_' + item.position + '_' + this.key);
    if (soundDIV == null) return null;

    var soundLink = soundDIV.getElementsByTagName('a')[0];
    return soundLink;
};

// get the content element
ContentMCOption.prototype.getContentElement = function()
{
    var optionElement = this.getElement();
    return YUD.getElementsByClassName('optionContent', 'div', optionElement);
};

// get the feedback element
ContentMCOption.prototype.getFeedbackElement = function()
{
    var optionElement = this.getElement();
    return Util.Dom.getElementByClassName('optionFeedback', 'div', optionElement);
};

// show feedback for this option
ContentMCOption.prototype.showFeedback = function()
{
    var feedbackElement = this.getFeedbackElement();

    if (feedbackElement != null)
    {
        YUD.addClass(feedbackElement, 'showing');
    }
};

// hide feedback for this option
ContentMCOption.prototype.hideFeedback = function()
{
    var feedbackElement = this.getFeedbackElement();

    if (feedbackElement != null)
    {
        YUD.removeClass(feedbackElement, 'showing');
    }
};

ContentMCOption.prototype.select = function(force)
{
    // get the current selected MC option
    var currentSelection = this._options.getSelected();

    // if this is already selected then rturn
    if (currentSelection == this && force !== true) return false;

    // clear current selection css
    if (force)
    {
        var options = this._options.getOptions();

        Util.Array.each(options, function(option)
        {
            option.deselect();
        });
    }
    else if (currentSelection)
    {
        currentSelection.deselect();
    }

    // mark radio button as checked (this also removes the current selection)
    var radioButton = this.getRadioButton();

    if (!radioButton.checked)
    {
        radioButton.checked = true;
    }

    // add selected css
    YUD.addClass(this.getElement(), 'optionSelected');

    // show feedback
    var page = this._options._item.getPage();
    var pageAccProps = page.getAccommodationProperties();
    if (pageAccProps != null && pageAccProps.showFeedback()) this.showFeedback();

    // TDS notification
    if (typeof (window.tdsUpdateItemResponse) == 'function')
    {
        // get current options position and notify TDS
        var position = this._options._item.position;
        window.tdsUpdateItemResponse(position, this.key);
    }

    return true;
};

ContentMCOption.prototype.deselect = function()
{
    // remove class
    var optionElement = this.getElement();
    YUD.removeClass(optionElement, 'optionSelected');

    // remove selection
    var radioButton = this.getRadioButton();
    radioButton.checked = false;

    // hide feedback
    this.hideFeedback();
};

// is this option selected
ContentMCOption.prototype.isSelected = function()
{
    var radioButton = this.getRadioButton();
    return (radioButton && radioButton.checked === true);
};

ContentMCOption.prototype.hasStrikethrough = function()
{
    var element = this.getElement();
    return (element && YUD.hasClass(element, 'strikethrough'));
};

ContentMCOption.prototype.toggleStrikethrough = function()
{
    var element = this.getElement();
    if (element) YUD.toggleClass(element, 'strikethrough');
};

ContentMCOption.prototype.toString = function() { return this.key; };
