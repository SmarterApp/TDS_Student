/*
This adds tools functionality to the prototype of a content item.
*/

// get the container element for the tools
ContentItem.prototype.getToolsContainer = function()
{
    var itemEl = this.getElement();
    return Util.Dom.getElementByClassName('markComment', 'span', itemEl);
};

ContentItem.prototype.hasCommentLink = function()
{
    var doc = this._page.getDoc();
    var commentLink = doc.getElementById('Item_CommentLink_' + this.position);
    return ContentManager.isVisible(commentLink);
};

// fire comment button being clicked
ContentItem.prototype.toggleComment = function()
{
    ContentManager.fireEntityEvent('comment', this);
};

// get the mark for review link element
ContentItem.prototype.getMarkLink = function()
{
    var doc = this._page.getDoc();
    return doc.getElementById('Item_MarkLink_' + this.position);
};

// get the mark for review check box form element
ContentItem.prototype.getMarkCheckBox = function()
{
    var doc = this._page.getDoc();
    return doc.getElementById('Item_Mark_' + this.position);
};

// is mark for review visible?
ContentItem.prototype.hasMarkLink = function()
{
    var markLink = this.getMarkLink();
    return ContentManager.isVisible(markLink);
};

// is this item marked for review
ContentItem.prototype.isMarked = function()
{
    var checkMark = this.getMarkCheckBox();
    return (checkMark && checkMark.checked);
};

// toggle the mark for review on and off
ContentItem.prototype.toggleMark = function()
{
    var markCheck = this.getMarkCheckBox();
    if (markCheck == null) return;

    if (this.isMarked())
    {
        markCheck.checked = false;
    }
    else
    {
        markCheck.checked = true;
    }

    this.updateMarkLink();

    // TDS notification
    if (typeof (window.tdsUpdateItemMark) == 'function')
    {
        window.tdsUpdateItemMark(this.position, markCheck.checked);
    }
};

// update the html/css for mark for review link
ContentItem.prototype.updateMarkLink = function()
{
    var markLink = this.getMarkLink();
    if (markLink == null) return;

    if (this.isMarked())
    {
        YUD.removeClass(markLink, 'markReview');
        YUD.addClass(markLink, 'markReviewMarked');
        markLink.setAttribute('aria-checked', 'true');
    }
    else
    {
        YUD.removeClass(markLink, 'markReviewMarked');
        YUD.addClass(markLink, 'markReview');
        markLink.setAttribute('aria-checked', 'false');
    }
};

// get the print item link
ContentItem.prototype.getPrintLink = function()
{
    var doc = this._page.getDoc();
    return doc.getElementById('Item_PrintLink_' + this.position);
};

// is the print link showing
ContentItem.prototype.hasPrintLink = function()
{
    var printLink = this.getPrintLink();
    return ContentManager.isVisible(printLink);
};

// print item
ContentItem.prototype.print = function()
{
    // TDS notification
    if (typeof (window.tdsItemPrint) == 'function')
    {
        window.tdsItemPrint(this.position);
    }
};

// get the help item link
ContentItem.prototype.getHelpLink = function()
{
    var doc = this._page.getDoc();
    return doc.getElementById('Item_HelpLink_' + this.position);
};

// is the help link showing
ContentItem.prototype.hasHelpLink = function()
{
    var helpLink = this.getHelpLink();
    return ContentManager.isVisible(helpLink);
};

// open help
ContentItem.prototype.openHelp = function()
{
    // TDS notification
    if (typeof (window.tdsItemResource) == 'function')
    {
        window.tdsItemResource('help', this.tutorial.bankKey, this.tutorial.itemKey);
    }
};

// get the GTR item link
ContentItem.prototype.getGTRLink = function()
{
    var doc = this._page.getDoc();
    return doc.getElementById('Item_GTRLink_' + this.position);
};

// is the GTR link showing
ContentItem.prototype.hasGTRLink = function()
{
    var gtrLink = this.getGTRLink();
    return ContentManager.isVisible(gtrLink);
};

// open GTR
ContentItem.prototype.openGTR = function()
{
    // TDS notification
    if (typeof (window.tdsItemResource) == 'function')
    {
        window.tdsItemResource('gtr_' + this.position, this.gtr.bankKey, this.gtr.itemKey);
    }
};

ContentItem.prototype.getRemoveResponseLink = function()
{
    var itemDoc = this.getParentPage().getDoc();
    return itemDoc.getElementById('Item_RemoveResponse_' + this.position);
};

ContentItem.prototype.hasRemoveResponseLink = function()
{
    var removeResponseLink = this.getRemoveResponseLink();
    return ContentManager.isVisible(removeResponseLink);
};

// adds the remove item response button
ContentItem.prototype.addRemoveResponseLink = function()
{
    // check if link already exists
    var removeResponseLink = this.getRemoveResponseLink();
    if (removeResponseLink != null) return;

    // create link
    var itemDoc = this.getParentPage().getDoc();
    removeResponseLink = itemDoc.createElement('a');
    removeResponseLink.id = 'Item_RemoveResponse_' + this.position;
    YUD.addClass(removeResponseLink, 'removeResponse');
    Util.Dom.setTextContent(removeResponseLink, 'Remove Response');
    YUD.setStyle(removeResponseLink, 'display', 'block');
    
    // stop dom click event
    YUE.on(removeResponseLink, 'click', function (ev) {
        YUE.stopEvent(ev);
    });
    
    // execute method
    YUE.on(removeResponseLink, 'click', this.removeResponse, this, true);
    
    // add link
    var toolsEl = this.getToolsContainer();
    if (toolsEl) toolsEl.appendChild(removeResponseLink);
};

// this function will fire an event to remove the response for this item
ContentItem.prototype.removeResponse = function()
{
    // TDS notification
    if (typeof (window.tdsRemoveResponse) == 'function')
    {
        window.tdsRemoveResponse(this.position);
    }
};

/******************************************************************************************/

// this hooks up the item tool links when the page is available
ContentManager.onItemEvent('available', function(page, item)
{
    var doc = page.getDoc();

    // click event: comment
    var commentLink = doc.getElementById('Item_CommentLink_' + item.position);
    var commentCloseLink = doc.getElementById('Item_CommentCloseLink_' + item.position);

    YUE.on(commentLink, 'click', item.toggleComment, item, true);
    YUE.on(commentCloseLink, 'click', item.toggleComment, item, true);

    // click event: mark for review
    var markLink = item.getMarkLink();
    YUE.on(markLink, 'click', item.toggleMark, item, true);
    
    // update mark css link
    item.updateMarkLink();

    // click event: print item
    var printLink = item.getPrintLink();
    YUE.on(printLink, 'click', item.print, item, true);

    // click event: help
    var helpLink = item.getHelpLink();
    YUE.on(helpLink, 'click', item.openHelp, item, true);

    // click event: gtr
    var gtrLink = item.getGTRLink();
    YUE.on(gtrLink, 'click', item.openGTR, item, true);

    //add the remove response link.
    var accProps = page.getAccommodationProperties();
    if (TDS.isProxyLogin && accProps.hasResponseReset())
        item.addRemoveResponseLink();
});