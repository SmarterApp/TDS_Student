﻿// namespace for creating sections
var Sections = {};

// base class for sections
Sections.Base = function(id)
{
    Sections.Base.superclass.constructor.call(this, id);

    var sectionHeader = this.getHeader();

    if (sectionHeader)
    {
        // the main sections h2's anchor has tabindex="-1" instead of tabindex="0" to keep it programmatically focussable but out of the TAB order
        sectionHeader.setAttribute('tabindex', -1);
    }

    this.hide();
};

YAHOO.lang.extend(Sections.Base, Util.Workflow.Activity);

// helper function for adding a dom onclick event
Sections.Base.prototype.addClick = function(id, callback)
{
    var target =  YUD.get(id);
    if (target == null) return false;

    YUE.on(target, 'click', function(evt)
    {
        // stop click event on links
        if (target.nodeName == 'A') YUE.stopEvent(evt);

        // NOTE: do not add this code in a timer it might cause double clicks
        callback.call(this, evt);

    }, this, true);

    return true;
};

Sections.Base.prototype.getContainer = function()
{
    return  YUD.get(this.getId());
};

Sections.Base.prototype.getHeader = function()
{
    return  YUD.get(this.getId() + 'Header');
};

Sections.Base.prototype.getHeaderText = function()
{
    var sectionHeader = this.getHeader();
    return Util.Dom.getTextContent(sectionHeader);
};

// this function gets called when showing a section
Sections.Base.prototype.show = function()
{
    var sectionContainer = this.getContainer();
    if (!sectionContainer) return false;

    YUD.setStyle(sectionContainer, 'display', 'block');
    YUD.setStyle(sectionContainer, 'visibility', 'visible');
    sectionContainer.setAttribute('aria-hidden', 'false');

    // focus on sections header
    var sectionHeader = this.getHeader();
    if (sectionHeader) sectionHeader.focus();

    // let user know page is ready
    TDS.ARIA.writeLog('Page is ready');

    return true;
};

// this function gets called when hiding a section
Sections.Base.prototype.hide = function()
{
    var sectionContainer = this.getContainer();
    if (!sectionContainer) return false;

    YUD.setStyle(sectionContainer, 'display', 'none');
    YUD.setStyle(sectionContainer, 'visibility', 'hidden');
    sectionContainer.setAttribute('aria-hidden', 'true');

    return true;
};

/***************************************************************************/

Sections.createWorkflow = function()
{
    var wf = new Util.Workflow();

    // show event
    wf.Events.subscribe('onEnter', function(section)
    {
        section.show();
    });

    // hide event
    wf.Events.subscribe('onLeave', function(section)
    {
        section.hide();
    });

    return wf;
}