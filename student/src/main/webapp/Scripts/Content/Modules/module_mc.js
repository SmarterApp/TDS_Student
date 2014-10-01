/*************/
/* MC EVENTS */
/*************/

// listen for when the item is created and build MC options
ContentManager.onItemEvent('init', function(page, item, itsItem) {

    // HACK: The scaffolding item has options property. 
    if (item.isResponseType('Scaffolding')) return;
    
    // check to see if this is an MC or MS item format
    var format = item.format.toUpperCase();
    if ((format != 'MS') && (format != 'MC') && (format != 'SI')) return;

    // check if this item has MC options
    if (itsItem.options == null) return;

    var options;
    if (item.format != null && item.format.toUpperCase() == 'MS') {
        options = new ContentMSGroup(item);
        options.setMinChoices(itsItem.optionsConfig.minChoices);
        options.setMaxChoices(itsItem.optionsConfig.maxChoices);
    } else {
        options = new ContentMCGroup(item);
    }

    Util.Array.each(itsItem.options, function(itsOption)
    {
        // get option key
        var optionKey = itsOption.key.toUpperCase();

        // create option class
        var option;
        if (item.format != null && item.format.toUpperCase() == 'MS') {
            option = new ContentMSOption(options, optionKey);
        } else {
            option = new ContentMCOption(options, optionKey);
        }

        options.addOption(option);

        // get any extra option data
        option.tts = itsOption.tts;
        option.feedback = itsOption.feedback; 
    });

    // attach option group to the item
    item.MC = options;
});

// listen for when the item DOM is available
ContentManager.onItemEvent('available', function(page, item)
{
    // check if this item has MC options
    if (item.MC == null) return;

    // Bug 97306, 118785 - put format_ms on MS items in Wai layout because 
    // page wrapper doesn't.
    if (page.layout == 'wai' && item.format == 'MS') {
        var itemDiv = item.getElement();
        YUD.addClass(itemDiv, 'multipleChoiceItem format_ms');
    }

    var options = item.MC.getOptions();
    
    Util.Array.each(options, function(option)
    {
        // get option elements
        var optionContainer = option.getElement();
        var radioButton = option.getRadioButton();
        var radioButtonGroup = option.getRadioGroup();

        // add aria for screen reader
        if (ContentManager.enableARIA)
        {
            radioButton.setAttribute('aria-label', 'Option ' + option.key);
        }

        // add context area
        item.addComponent(optionContainer);

        // add feedback
        if (option.feedback != null)
        {
            // check if feedback is enabled before adding it
            var pageAccProps = page.getAccommodationProperties();
            if (pageAccProps != null && pageAccProps.showFeedback())
            {
                // create feedback element
                var doc = item.getPage().getDoc();

                var feedbackElement = doc.createElement('div');
                YUD.addClass(feedbackElement, 'optionFeedback');
                feedbackElement.innerHTML = option.feedback;

                // add feedback into DOM
                optionContainer.appendChild(feedbackElement);

                // check if we should show the feedback right now
                if (option.isSelected()) option.showFeedback();
                else option.hideFeedback();
            }
        }

        // add click event directly to radio button
        YUE.on(radioButton, 'click', function(ev)
        {
            option.select(true); // HACK: force selection
        });

        YUE.on(radioButton, 'focus', function(ev)
        {
            YUD.setStyle(optionContainer, 'background-color', 'orange');
        });

        YUE.on(radioButton, 'blur', function(ev)
        {
            YUD.setStyle(optionContainer, 'background-color', '');
        });

        // NOTE: Safari 2.0 onclick does not always work, so we replace it with mousedown which does work.
        var clickType = (YAHOO.env.ua.webkit > 0 && YAHOO.env.ua.webkit <= 419.3) ? 'mousedown' : 'click';

        // add click event to option container
        YUE.on(optionContainer, clickType, function(clickEvent)
        {
            // ignore dom events if in read-only mode
            if (item.isReadOnly()) return;

            // ignore click if alt key was being held down
            if (clickEvent.altKey) return;

            // TODO: RENABLE THIS? ---> if (ContentManager.Menu.isShowing()) return;

            // select option
            option.select();
        });

    });

});

// listen for key events
ContentManager.onItemEvent('keyevent', function(page, item, evt)
{
    // check if MC
    if (!item.MC) return;
    if (evt.type != 'keydown') return;
    if (evt.ctrlKey || evt.altKey) return; // no modifiers

    var options = item.MC;

    if (evt.key == 'Enter')
    {
        // ignore key events if in read-only mode
        if (ContentManager.isReadOnly()) return;

        var option = options.getFocusedOption();
        if (option) option.select();
    }
});

// listen for context menu
ContentManager.onItemEvent('menushow', function(page, item, menu, evt)
{
    // check if MC
    if (!item.MC) return;

    var optionGroup = item.MC;

    // check if there is a focused option
    var focusedOption = optionGroup.getFocusedOption();
    if (!focusedOption) return;

    // MENU ITEM: Strikethrough
    var accProps = page.getAccommodationProperties();
    
    if (accProps.hasStrikethrough())
    {
        var strikedthrough = YUD.hasClass(focusedOption.getElement(), 'strikethrough');

        var menuLabel = { text: (focusedOption.hasStrikethrough() ? Messages.get('TDSMC.MenuLabel.UndoStrikethrough') : Messages.get('TDSMC.MenuLabel.Strikethrough')), classname: 'strikethrough' };

        menu.addMenuItem('component', menuLabel, function()
        {
            focusedOption.toggleStrikethrough();
        });
    }
});

// register response getter and setter for MC questions
(function()
{
    // response handler for MC questions
    var getter = function(item, response)
    {
        var value = item.MC.getValue();
        if (!value) return; // nothing selected

        response.value = value;
        response.isAvailable = true;
        response.isSelected = true;
        response.isValid = true;
    };

    // response handler for MC questions
    var setter = function(item, value)
    {
        item.MC.setValue(value);
    };

    ContentManager.registerResponseHandler('vertical', getter, setter);
    ContentManager.registerResponseHandler('vertical MS', getter, setter);
    ContentManager.registerResponseHandler('horizontal', getter, setter);
    ContentManager.registerResponseHandler('stacked', getter, setter);
    ContentManager.registerResponseHandler('stackedB', getter, setter);
})();

