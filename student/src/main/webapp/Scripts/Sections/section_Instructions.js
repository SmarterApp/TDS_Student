Sections.Instructions = function()
{
    Sections.Instructions.superclass.constructor.call(this, 'sectionInstructions');

    this.addClick('btnCancelTest', function()
    {
        this.request('back');
    });

    this.addClick('btnStartTest', this.start);

};

YAHOO.lang.extend(Sections.Instructions, Sections.Base);

Sections.Instructions.prototype.init = function ()
{
    var container = YUD.get('quickQuide');
    var frame = YUD.get('helpFrame');

    var url = TDS.baseUrl + 'Pages/';
    url += Messages.get('Global.Path.Help');

    // load instructions
    TDS.ToolManager.loadFrameUrl(container, frame, url, function(frameDoc, allowAccess) // loadFrameKey
    {
        // set embedded class so the help styles itself for being embedded in a page
        YUD.addClass(frameDoc.body, 'embedded');

        // init help tts
        onHelpLoad(frame, 'startSpeakingButton', 'stopSpeakingButton', 'ttsHelpMessage', 'noTTSHelpMessage');
    });
};

Sections.Instructions.prototype.start = function()
{
    var startData = 
    {
        formKey: LoginShell.formSelection
    };
    
    LoginShell.api.startTest(startData, function(testConfig)
    {
        // make sure there is a config
        if (testConfig) TDS.redirectTestShell();
    });
};

