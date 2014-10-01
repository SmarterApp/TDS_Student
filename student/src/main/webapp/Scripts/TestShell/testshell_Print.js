TestShell.Print = {};

TestShell.Print._getCallback = function()
{
    var callback =
    {
        success: function(xhrObj)
        {
            TestShell.idleTimer.waitMins = TestShell.Config.requestInterfaceTimeout;
            TestShell.idleTimer.reset();

            // NOTE: The alert is commented out because I now notify the student right away when a print is submitted.
            // TestShell.UI.showAlert(Messages.get('TDSShellUIJS.Label.Notice'), ErrorCodes.get('Print'));
        },

        failure: function(xhrObj)
        {
            TestShell.UI.showError(Messages.get('TDSShellUIJS.Label.PrintRequestFailed'));
        },

        timeout: 30000
    };

    return callback;
};

// get accommodations used for printing passage/item
TestShell.Print._getAccommodations = function()
{
    var page = ContentManager.getCurrentPage();
    var accProps = page.getAccommodationProperties();

    // get acc codes
    var printSize = page.getZoom().getCSS(); // print size
    var fontType = accProps.getFontType();
    var fontSize = accProps.getFontSize();

    var parameters = [];
    parameters.push('Print Size:' + printSize);
    parameters.push('Font Type:' + fontType);
    parameters.push('Font Size:' + fontSize);
    return parameters.join(';');
};

TestShell.Print.getXhrUrl = function()
{
    var urlBuilder = [];
    urlBuilder.push(TDS.baseUrl);
    urlBuilder.push('Pages/API/TestShell.axd/');
    return urlBuilder.join('');
};

// print the current passage
TestShell.Print.passage = function()
{
    // create base url
    var url = this.getXhrUrl() + 'print?';

    // get current page group
    var group = TestShell.PageManager.getCurrent();

    // create url parameters
    var urlParameters = [];
    urlParameters.push('type=passage');
    urlParameters.push('page=' + group.pageNum);
    urlParameters.push('accommodations=' + this._getAccommodations());

    url += urlParameters.join('&');

    YUC.asyncRequest('POST', url, this._getCallback());
    TestShell.UI.showAlert(Messages.get('TDSShellUIJS.Label.Notice'), Messages.get('Print'));

    this.passageComplete(group);
};

// print item position
TestShell.Print.item = function(position)
{
    // create base url
    var url = this.getXhrUrl() + 'print?';

    // get current page group
    var group = TestShell.PageManager.getCurrent();

    // get response
    var response = group.getResponse(position);

    // create url parameters
    var urlParameters = [];
    urlParameters.push('type=item');
    urlParameters.push('page=' + group.pageNum);
    urlParameters.push('position=' + position);
    urlParameters.push('accommodations=' + this._getAccommodations());

    url += urlParameters.join('&');

    YUC.asyncRequest('POST', url, this._getCallback());
    TestShell.UI.showAlert(Messages.get('TDSShellUIJS.Label.Notice'), Messages.get('Print'));

    if (response)
    {
        this.itemComplete(group, response);
    }
};

// call when passage printing has completed
TestShell.Print.passageComplete = function(group)
{
    // make sure this is the accessible layout
    var contentPage = group.getContentPage();
    if (contentPage.layout != 'WAI') return;

    var pagePassage = contentPage.getPassage();
    var passageElement = pagePassage.getElement();

    // try and get item status
    var status = Util.Dom.getElementByClassName('status', 'div', passageElement);

    // create item status if it doesn't exist
    if (status == null)
    {
        // create status div and unordered list
        status = HTML.DIV({ className: 'status' });

        // add aria
        status.setAttribute('role', 'status');
        status.setAttribute('aria-atomic', 'false');
        status.setAttribute('aria-relevant', 'additions text');
        status.setAttribute('aria-live', 'assertive');

        // add status div right after the passage element div
        YUD.insertBefore(status, YUD.getFirstChild(passageElement));
    }

    // add status message to list
    // status.appendChild(HTML.DIV(null, 'Print request submitted'));
    status.innerHTML = 'Print request submitted';
};

// call when item printing has completed
TestShell.Print.itemComplete = function(group, response)
{
    // make sure this is the accessible layout
    var contentPage = group.getContentPage();
    if (contentPage.layout != 'WAI') return;

    // get item element
    var item = response.getItem();
    if (item == null) return;

    var printLink = item.getPrintLink();
    YUD.addClass(printLink, 'printSubmitted');

    if (printLink)
    {
        // set delay on writing label so this doesn't get spoken when clicking on link
        setTimeout(function()
        {
            printLink.setAttribute('aria-label', 'This question has been submitted for printing.');
        }, 0);
    }
};
