Sections.Logout = function()
{
    Sections.Logout.superclass.constructor.call(this, 'sectionLogout');
};

YAHOO.lang.extend(Sections.Logout, Sections.Base);

Sections.Logout.prototype.requestApproval = function(skipCheck)
{
    if (skipCheck === true) return Util.Workflow.Approval.Approved;

    var section = this;

    // open dialog and verify
    TDS.Dialog.showPrompt(Messages.get('Global.Label.LogoutVerify'), function() 
    {
        section.requestApproved();
    });
    
    return Util.Workflow.Approval.Pending;
};

Sections.Logout.prototype.load = function ()
{
    // if a test is selected then pause
    if (LoginShell.testSelection != null)
    {
        // pause test
        var self = this;
        LoginShell.api.pauseTest(function() { self.ready(); });
        
        return true; // wait for pause
    }

    return false; // don't wait
};

Sections.Logout.prototype.enter = function()
{
    // redirect to login page
    TDS.logout();
};

