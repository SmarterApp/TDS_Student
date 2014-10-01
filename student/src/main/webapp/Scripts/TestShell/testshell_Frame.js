// Some common content iframe operations
// NOTE: This is more for compatability with older functions that relied on global functions with the same names.
// TODO: Remove this when possible!
TestShell.Frame =
{
    // WRITING: current layout object if any
    getWriting: function()
    {
        var contentPage = ContentManager.getCurrentPage();
        return (contentPage && contentPage.writing) ? contentPage.writing : null;
    },

    // get the document body for the content frame
    getBody: function()
    {
        var contentPage = ContentManager.getCurrentPage();
        return (contentPage) ? contentPage.getBody() : null;
    }
};
