Sections.TestApproval = function()
{
    Sections.TestApproval.superclass.constructor.call(this, 'sectionTestApproval');

    this._timer = null;

    this.Controls =
    {
        btnCancelApproval: YUD.get('btnCancelApproval')
    };

    this.addClick(this.Controls.btnCancelApproval, this.cancel);
};

YAHOO.lang.extend(Sections.TestApproval, Sections.Base);

Sections.TestApproval.Status =
{
    waiting: 0,
    approved: 1,
    denied: 2,
    logout: 3
};

Sections.TestApproval.prototype.setMessage = function(header, message)
{
    YUD.get('lblApprovalHeader').innerHTML = header;
    YUD.get('lblApprovalMessage').innerHTML = message;
};

Sections.TestApproval.prototype.cancel = function()
{
    // cancel any xhr requests
    LoginShell.api.abort('checkApproval');

    // stop any polling
    if (this._timer)
    {
        this._timer.cancel();
        this._timer = null;
    }
    
    // logout user and skip verify dialog
    this.request('logout', true);
};

Sections.TestApproval.prototype.load = function ()
{
    /*
    var header = 'Waiting for TA approval...';
    var message = 'Please wait while the Test Administrator edits the settings for your test session. This may take a few minutes...';
    this.setMessage(header, message);
    */
};

Sections.TestApproval.prototype.enter = function()
{
    var self = this;

    // default polling duration 
    var pollDuration = 5000;

    // check for approval after a specific duration 
    var pollForApproval = function(duration)
    {
        self.timer = YAHOO.lang.later(duration, this, checkForApproval);
    };

    // check the server for approval
    var checkForApproval = function()
    {
        LoginShell.api.checkApproval(null, function(approval)
        {
            if (approval)
            {
                // check if approved/denied
                switch (approval.status)
                {
                    case Sections.TestApproval.Status.approved: self.approved(approval); return;
                    case Sections.TestApproval.Status.denied: self.denied(approval); return;
                    case Sections.TestApproval.Status.logout: self.logout(); return;
                }
                
                // continue polling for approval
                pollForApproval(pollDuration);
            }
        });
    };

    // begin polling for approval
    pollForApproval(1);
};

Sections.TestApproval.prototype.approved = function(approval)
{
    LoginShell.setTestAccommodations(approval.segmentsAccommodations);
    
    // go to verify 
    this.request('next', approval);
};

Sections.TestApproval.prototype.denied = function(approval)
{
    var self = this;

    // build denied message
    var deniedMessage = Messages.get('Approval.Label.TADenied');
    
    if (approval.comment != null && approval.comment.length > 0) {
        deniedMessage += ": " + approval.comment;
    }

    // show message
    TDS.Dialog.showAlert(deniedMessage, function() {
        self.logout();
    });
};

Sections.TestApproval.prototype.logout = function()
{
    // logout user and skip verify dialog
    this.request('logout', true);
};

