Sections.TestSelectionProxyReenter = function()
{
    Sections.LoginVerify.superclass.constructor.call(this, 'sectionTestSelectionProxyReenter');
};

YAHOO.lang.extend(Sections.TestSelectionProxyReenter, Sections.Base);

// verify the login info received from the server
Sections.TestSelectionProxyReenter.prototype.load = function () {
    YAHOO.lang.later(0, this, function () {
        if (LoginShell.session == null) {
            //!!hack. there is no way right now to pass the session id back. 
            //so we will retrieve it from the student context cookie: TDS-Student-Data
            //if we come into this section then we must have a valid cookie so far.
            var sessionId = YAHOO.util.Cookie.getSub('TDS-Student-Data', 'S_ID');
            LoginShell.session = { id: sessionId };
        }
        LoginShell.session.isProctorless = false;

        if (LoginShell.testee == null)
            LoginShell.testee = {};

        this.request('next');
    });
};


