// a class used for wrapping an editor with special characters functionality
// (NOTE: you need to do this before rendering editor)
HTMLEditor.SpecialCharacters2 = {};

HTMLEditor.SpecialCharacters2.register = function(item, editor)
{
    editor.on('toolbarLoaded', function()
    {
        HTMLEditor.SpecialCharacters2._init(item, editor);
    });

    var specCharsBuilder = HTMLEditor.SpecialCharacters2._createUI(item, editor);

    if (specCharsBuilder)
    {
        item.addComponent(specCharsBuilder._divContainer);
    }
};

HTMLEditor.SpecialCharacters2._init = function(item, editor)
{
    // add special chars button
    var configSpecialChar = { type: 'push', label: 'Special Characters', value: 'specialchar' };
    editor.toolbar.addButtonToGroup(configSpecialChar, 'textstyle');

    editor.toolbar.on('specialcharClick', function(ev)
    {
        HTMLEditor.SpecialCharacters2._clickButton(editor);
        return false;
    });
};

// create special characters div
HTMLEditor.SpecialCharacters2._createUI = function(item, editor)
{
    var itemContainer = item.getElement();
    var writingBlock = Util.Dom.getElementByClassName('writingBlock', 'div', itemContainer);

    if (writingBlock == null) return null;

    // get special characters menu array
    var categories = HTMLEditor.SpecialCharacters2._getData();

    // create HTML
    var specCharsBuilder = new SpecialCharactersBuilder(writingBlock);
    editor.specialCharacters = specCharsBuilder;

    specCharsBuilder.onClickCharacter.subscribe(function(character)
    {
        editor.execCommand('inserthtml', character.html);
    });

    specCharsBuilder.create(categories);

    // setup shortcuts
    YUE.onFocus(specCharsBuilder._divWrapper, function(ev)
    {
        var target = YUE.getTarget(ev);

        if (target.nodeName == 'A')
        {
            specCharsBuilder.focused = target;
        }
    });

    return specCharsBuilder;
};

HTMLEditor.SpecialCharacters2._keyDown = function(ev, specCharsBuilder)
{
    // check if left/up/right/down
    if (!(ev.keyCode >= 37 && ev.keyCode <= 40)) return;

    // check if focused entity
    if (!specCharsBuilder.focused) return;

    // get current row (div)
    var divRow = specCharsBuilder.focused.parentNode.parentNode.parentNode;

    // up/down
    if (ev.keyCode == 38 || ev.keyCode == 40)
    {
        // get all the rows
        var divRows = [];
        divRows.push(specCharsBuilder._divMenu);

        YUD.batch(specCharsBuilder._activeCategory.div.getElementsByTagName('div'), function(div)
        {
            divRows.push(div);
        });
        
        // create iterator and select row
        var rows = Util.Iterator(divRows);
        rows.jumpTo(divRow);

        // up
        if (ev.keyCode == 38)
        {
            rows.prev();
        }
        // down
        else if (ev.keyCode == 40)
        {
            rows.next();
        }

        divRow = rows.current();
    }

    var links = divRow.getElementsByTagName('a');

    // change dom container into array
    links = Util.Array.map(links, function(link) { return link; });

    // create iterator and select column
    var columns = Util.Iterator(links);
    columns.jumpTo(specCharsBuilder.focused);

    // left
    if (ev.keyCode == 37)
    {
        columns.prev();
    }
    // right
    else if (ev.keyCode == 39)
    {
        columns.next();
    }
    
    // if there is no column selected then reset to first one
    if (!columns.valid()) columns.reset();
    
    // focus on element
    var currentColumn = columns.current();
    Util.Dom.focus(currentColumn);
}

HTMLEditor.SpecialCharacters2._clickButton = function(editor)
{
    if (editor.specialCharacters)
    {
        editor.specialCharacters.toggle();
    }
};

HTMLEditor.SpecialCharacters2._getData = function()
{
    var categories = [];
    var categoryLookup = {}, groupLookup = {}, charLookup = {};

    var addChar = function(categoryName, groupName, characterHtml)
    {
        // category
        var category = categoryLookup[categoryName];

        if (category == null)
        {
            category = { label: categoryName, groups: [] };
            categories.push(category);
            categoryLookup[categoryName] = category;
        }

        // group
        var group = groupLookup[groupName];

        if (group == null)
        {
            group = { label: groupName, characters: [] }
            category.groups.push(group);
            groupLookup[groupName] = group;
        }

        // character
        var character = charLookup[characterHtml];

        if (character == null)
        {
            var character = { html: characterHtml };
            group.characters.push(character);
            charLookup[characterHtml] = character;
        }
    };

    // MATH
    var addOperators = Util.Function.bind(addChar, null, 'Math', 'Operators');
    var addGreek = Util.Function.bind(addChar, null, 'Math', 'Greek');
    var addFractions = Util.Function.bind(addChar, null, 'Math', 'Fractions');

    Util.Array.map(['&#247;', '&#215;', '&#8800;', '&#60;', '&#62;', '&#177;'], addOperators);
    Util.Array.map(['&#8734;', '&#945;', '&#946;', '&#948;', '&#952;', '&#931;'], addGreek);
    Util.Array.map(['&#188;', '&#189;', '&#190;', '&#8531;', '&#8532;', '&#8540;', '&#8541;', '&#8542;'], addFractions);

    // SPANISH
    var addSpanishSymbols = Util.Function.bind(addChar, null, 'Spanish', 'Symbols');
    var addSpanishLower = Util.Function.bind(addChar, null, 'Spanish', 'Lower Case');
    var addSpanishUpper = Util.Function.bind(addChar, null, 'Spanish', 'Upper Case');

    Util.Array.map(['&#191;', '&#161;'], addSpanishSymbols);
    Util.Array.map(['&#224;', '&#232;', '&#236;', '&#242;', '&#249;', '&#225;', '&#233;', '&#237;', '&#243;', '&#250;', '&#253;', '&#227;', '&#241;', '&#245;'], addSpanishLower);
    Util.Array.map(['&#192;', '&#200;', '&#204;', '&#210;', '&#217;', '&#193;', '&#201;', '&#205;', '&#211;', '&#218;', '&#221;', '&#195;', '&#209;', '&#213;'], addSpanishUpper);

    return categories;
};

/********************************************************************/

// a class for building a special characters interface within a div
var SpecialCharactersBuilder = function(divWrapper)
{
    this.onClickCharacter = new YAHOO.util.CustomEvent('onClickCharacter', this, false, YAHOO.util.CustomEvent.FLAT),

    this._doc = Util.Dom.getOwnerDocument(divWrapper);
    this._divWrapper = divWrapper;
    this._divContainer = null;
    this._divMenu = null;
    this._divCharacters = null;

    this._category = null; // yui menu category obj
    this._group = null; // yui menu group obj
    this._character = null; // yui menu character obj

    this._isShowing = false; // is special characters div visible
    this._activeCategory = null; // current category obj

    this._init();
};

SpecialCharactersBuilder.prototype.toggle = function()
{
    if (this._isShowing) this.hide();
    else this.show();
}

SpecialCharactersBuilder.prototype.hide = function()
{
    YUD.setStyle(this._divContainer, 'display', 'none');
    this._isShowing = false;
};

SpecialCharactersBuilder.prototype.show = function()
{
    YUD.setStyle(this._divContainer, 'display', 'block');
    this._isShowing = true;
};

SpecialCharactersBuilder.prototype._init = function()
{
    // create div containers
    this._divContainer = HTML.DIV({ 'class': 'specialCharacters', 'style': 'display:none;' });
    this._divWrapper.insertBefore(this._divContainer, this._divWrapper.firstChild);

    this._divMenu = HTML.DIV({ 'class': 'menu' });
    this._divContainer.appendChild(this._divMenu);

    this._divCharacters = HTML.DIV({ 'class': 'characters' });
    this._divContainer.appendChild(this._divCharacters);

    // create menu list
    this._divMenu.appendChild(this._doc.createElement('ul'));
};

// create html based on YUI menu array
SpecialCharactersBuilder.prototype.create = function(categories)
{
    /* CATEGORY */
    for (var i = 0; i < categories.length; i++)
    {
        var category = categories[i];

        // create a list item with an anchor tag
        var liCategory = HTML.LI(null, HTML.A({ 'href': '#' }, category.label));
        this._divMenu.firstChild.appendChild(liCategory);

        var divCategory = HTML.DIV({ 'class': 'category' });
        this._divCharacters.appendChild(divCategory);

        // assign event to category
        var linkCategory = YUD.getLastChild(liCategory);

        category.link = linkCategory;
        category.div = divCategory;

        // set default active category
        if (this._activeCategory == null)
        {
            this._activeCategory = category;
        }

        YUE.on(linkCategory, 'click', function(evt, category)
        {
            YUE.stopEvent(evt);
            this._setActiveCategory(category);
        }, category, this);

        /* GROUP */
        for (var j = 0; j < category.groups.length; j++)
        {
            var group = category.groups[j];

            // create a div with a span and empty unordered list
            var divGroup = HTML.DIV({ 'class': 'group' },
                HTML.SPAN(null, group.label),
                HTML.UL({ 'class': 'characters' })
            );

            group.div = divGroup;

            divCategory.appendChild(divGroup);

            /* CHARACTER */
            for (var k = 0; k < group.characters.length; k++)
            {
                var character = group.characters[k];

                // create list item
                var ulGroup = YUD.getLastChild(divGroup);
                var liChar = HTML.LI(null, HTML.A({ 'class': 'character', 'href': '#' }));
                ulGroup.appendChild(liChar);

                // set character text
                var linkChar = YUD.getLastChild(liChar);
                linkChar.innerHTML = character.html;

                character.link = linkChar;

                // assign event to character
                YUE.on(linkChar, 'click', function(evt, character)
                {
                    YUE.stopEvent(evt);
                    this.onClickCharacter.fire(character);
                }, character, this);

            }
        }
    }

    this._setActiveCategory(this._activeCategory);
};

SpecialCharactersBuilder.prototype._setActiveCategory = function(category)
{
    // make category link active
    var linksCategories = this._divMenu.getElementsByTagName('a');

    for (var i = 0; i < linksCategories.length; i++)
    {
        YUD.removeClass(linksCategories[i], 'active');
    }

    YUD.addClass(category.link, 'active');

    // show category 
    var divCategories = YUD.getElementsByClassName('category', 'div', this._divCharacters);

    for (var i = 0; i < divCategories.length; i++)
    {
        YUD.setStyle(divCategories[i], 'display', 'none');
    }

    YUD.setStyle(category.div, 'display', 'block');

    this._activeCategory = category;
};

// check if an items active component is the special characters component
HTMLEditor.SpecialCharacters2.isActiveComponent = function(item)
{
    if (!item.editor) return; // no editor
    if (!item.editor.specialCharacters) return; // no special characters

    var currentComponent = item.getActiveComponent();
    return (currentComponent && Util.Dom.isElement(currentComponent) && YUD.hasClass(currentComponent, 'specialCharacters'));
};
