Sections.LoginProctor = function()
{
    Sections.LoginProctor.superclass.constructor.call(this, 'sectionLoginProctor');
};

YAHOO.lang.extend(Sections.LoginProctor, Sections.Base);

Sections.LoginProctor.prototype.load = function ()
{
    var self = this;

    var loginForm = YUD.get('loginProctorForm');

    loginForm.onsubmit = function()
    {
        self.validate();
        return false; // cancels form submission
    };
};

Sections.LoginProctor.prototype.validate = function()
{
    var self = this;

    var loginRequest =
    {
        userName: YUD.get('txtProctorUserName').value,
        password: YUD.get('txtProctorPassword').value
    };
    
    LoginShell.api.loginProctor(loginRequest, function(loginResponse)
    {
        if (loginResponse)
        {
            self.request('next', loginResponse);
        }
    });
};