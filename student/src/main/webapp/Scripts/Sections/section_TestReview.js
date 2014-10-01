Sections.TestReview = function()
{
    Sections.TestReview.superclass.constructor.call(this, 'sectionTestReview');

    this.addClick('btnReviewTest', this.viewGroup);
    this.addClick('btnCompleteTest', this.score);
};

YAHOO.lang.extend(Sections.TestReview, Sections.Base);

Sections.TestReview.prototype.load = function ()
{
    // show marked warning text
    this.setMarked(window.groups);
    
    // fill select box
    this.setGroups(window.groups);
};

Sections.TestReview.prototype.setMarked = function(groups)
{
    var markedWarning = YUD.get('markedWarning');
    var marked = false;

    for (var i = 0; i < groups.length; i++)
    {
        var group = groups[i];

        if (group.marked)
        {
            marked = true;
            break;
        }
    }

    if (marked)
    {
        YUD.setStyle(markedWarning, 'display', 'block');
    }
    else
    {
        YUD.setStyle(markedWarning, 'display', 'none');
    }
};

Sections.TestReview.prototype.setGroups = function(groups)
{
    var ddlNavigation = YUD.get('ddlNavigation');
    ddlNavigation.options.length = 0; // clear selectbox

    for (var i = 0; i < groups.length; i++)
    {
        var group = groups[i];

        // create label
        var label = "";
        
        var defaultAccProps = Accommodations.Manager.getDefaultProperties();
        
        // check if the nav acc says to use tasks for labels
        if (defaultAccProps && defaultAccProps.getNavigationDropdown() == 'TDS_NavTk')
        {
            //if the task accommodation is set we would need to say something like "task".
            //the task number is simply the group number starting with 1.
            label = Messages.getAlt('TDSShellObjectsJS.Label.TaskLabel', 'Task ') + group.page;
        }
        else
        {
            label = group.firstPos;
            if (group.firstPos != group.lastPos) label += ' - ' + group.lastPos;
        }
        
        if (group.marked) label += ' (' + Messages.get('TDSShellObjectsJS.Label.Marked') + ')';

        // add selectbox option
        ddlNavigation[i] = new Option(label, group.page);
    }
};

Sections.TestReview.prototype.viewGroup = function(group)
{
    var ddlNavigation = YUD.get('ddlNavigation');

    if (ddlNavigation.value == '')
    {
        //we need to show a warning e.g. "Please select a page first". however, depending
        //on the accommodation of the navigation drop down we may need to replace the "page" with something else e.g. "task".
        var label = Messages.get('TDSShellObjectsJS.Label.PageLabel').toLowerCase();
        // check if the nav acc says to use tasks for labels
        var defaultAccProps = Accommodations.Manager.getDefaultProperties();
        if (defaultAccProps && defaultAccProps.getNavigationDropdown() == 'TDS_NavTk')
        {
            //overwrite the label value.
            label = Messages.get('TDSShellObjectsJS.Label.TaskLabel').toLowerCase();
        }   
        var pageFirstMessage = Messages.get('ReviewShell.Message.PageFirst', [label]);
        TDS.Dialog.showAlert(pageFirstMessage);

        return;
    }

    // get test shell url
    TDS.redirectTestShell(ddlNavigation.value);
};

Sections.TestReview.prototype.score = function()
{
    var self = this;

    // check if we can complete test
    if (window.canCompleteTest === false)
    {
        var error = Messages.getAlt('ReviewShell.Message.CannotCompleteTest', 'Cannot complete the test.');
        TDS.Dialog.showAlert(error);
        return;
    }

    // show confirm dialog
    var message = Messages.getAlt('ReviewShell.Message.SubmitTest', 'Are you sure you want to submit the test?');
    
    TDS.Dialog.showPrompt(message, function()
    {
        ReviewShell.api.scoreTest(function(summary)
        {
            if (summary) self.request('next', summary);
        });
    });
}