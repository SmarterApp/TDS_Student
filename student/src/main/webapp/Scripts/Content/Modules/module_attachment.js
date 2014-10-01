// this event checks when page is ready and then goes through all the links to 
// resources like PDF and RTF and makes sure that they are opened in a different tab
ContentManager.onPageEvent('available', function(page) 
{
    var pageDoc = page.getDoc();
    var pageWin = page.getWin();

    var pageLinks = pageDoc.getElementsByTagName('a');

    for (var i = 0; i < pageLinks.length; i++) 
    {
        var pageLink = pageLinks[i];

        // check if PDF or RTF
        if (pageLink.href.toLowerCase().indexOf('.pdf') != -1  ||
            pageLink.href.toLowerCase().indexOf('.rtf') != -1) 
        {
            pageLink.target = '_blank';
        }        
    }
});
