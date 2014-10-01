TestShell.Tools = {};

TestShell.Tools.init = function()
{
    TDS.ToolManager.init();
};

TestShell.Tools.toggleHelp = function()
{
    // get the tools path
    var key = 'Global.Path.Help';
    var id = 'tool-' + key;

    var panel = TDS.ToolManager.get(id);

    if (panel == null)
    {
        var headerText = window.Messages.getAlt('TestShell.Label.HelpGuide', 'Help');
        panel = TDS.ToolManager.createPanel(id, 'helpguide', headerText, null, key);
    }
    
    TDS.ToolManager.toggle(panel);
};

TestShell.Tools.toggleFormula = function()
{
    var contentPage = ContentManager.getCurrentPage();
    var accProps = contentPage.getAccommodationProperties();

    // check if we have the tool
    if (!accProps.hasFormula()) return;

    // get tool code
    var key = accProps.getFormula();
    var id = 'tool-' + key;

    var panel = TDS.ToolManager.get(id);

    if (panel == null)
    {
        var headerText = window.Messages.getAlt('TestShell.Label.Formulas', 'Formula');
        panel = TDS.ToolManager.createPanel(id, 'formula', headerText, null, key);
    }
    
    TDS.ToolManager.toggle(panel);
};

TestShell.Tools.togglePeriodicTable = function()
{
    var contentPage = ContentManager.getCurrentPage();
    var accProps = contentPage.getAccommodationProperties();

    // check if we have the tool
    if (!accProps.hasPeriodicTable()) return;

    // get tool code
    var key = accProps.getPeriodicTable();
    var id = 'tool-' + key;

    var panel = TDS.ToolManager.get(id);

    if (panel == null)
    {
        var headerText = window.Messages.getAlt('TestShell.Label.PeriodicTable', 'Periodic Table');
        panel = TDS.ToolManager.createPanel(id, 'periodictable', headerText, null, key);
    }
    
    TDS.ToolManager.toggle(panel);
};

// update class on frame
TestShell.Tools.updateShowing = function()
{
    if (this._count) YUD.addClass(TestShell.Frame.getBody(), TestShell.UI.CSS.popupShowing);
    else YUD.removeClass(TestShell.Frame.getBody(), TestShell.UI.CSS.popupShowing);
};

// add zooming to tools
TDS.ToolManager.Events.subscribe('onLoaded', function(panel)
{
    // set zoom css
    var frame = panel.getFrame();
    var frameDoc = Util.Dom.getFrameContentDocument(frame);

    // add tool window to zoomable docs and refresh (make sure frame has no zoom styles or images won't zoom)
    TestShell.UI.zoom.addDocument(frameDoc, true);
    TestShell.UI.zoom.refresh();
});

// add accommodations to help guide
TDS.ToolManager.Events.subscribe('onShow', function(panel) {
    YUD.addClass(document.body, 'showingTools');
});

// add accommodations to help guide
TDS.ToolManager.Events.subscribe('onHide', function(panel) {
    YUD.removeClass(document.body, 'showingTools');
});