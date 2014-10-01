// REQUIRE: tds_xhr.js

Sections.XhrManager = function(shell)
{
    var timeout = (90 * 1000); // 90 second timeout
    Sections.XhrManager.superclass.constructor.call(this, timeout, 1);

    // save ref to shell
    this._shell = shell;

    this.Events.subscribe('onShowProgress', function()
    {
        TDS.ARIA.writeLog('Please wait.');
        TDS.Dialog.showProgress();
    });

    this.Events.subscribe('onHideProgress', function()
    {
        TDS.Dialog.hideProgress();
    });

    this.Events.subscribe('onError', this.onError);
};

YAHOO.extend(Sections.XhrManager, TDS.XhrManager);

Sections.XhrManager.prototype.getUrl = function(action)
{
    return TDS.baseUrl + 'Pages/API/MasterShell.axd/' + action;
};

Sections.XhrManager.prototype.onError = function(request, errorMessage, retriable, logout)
{
    var xhr = this;
    var shell = this._shell;

    if (retriable)
    {
        errorMessage += ' ' + Messages.getAlt('Messages.Label.XHRError', 'Select Yes to try again or No to logout.');

        TDS.Dialog.showPrompt(errorMessage,
        function()
        {
            // yes (resubmit xhr)
            xhr.sendRequest(request);
        },
        function()
        {
            // no (back to login page)
            if (logout)
            {
                TDS.logout();
            }
        });
    }
    else
    {
        TDS.Dialog.showWarning(errorMessage, function() {
            
            // Note: some login fail cases don't redirect the user and don't
            // reload the login page.  In the case we are a satellite, we want to force
            // the user to log out for any failure in login section.
            if (TDS.testeeCheckin != null) {
                logout = true;
            }
            
            // ok (back to login page)
            if (logout)
            {
                TDS.logout();
            }
        });
    }
};

/****************************************************************************************/   

// login proctor (user name, password)
Sections.XhrManager.prototype.loginProctor = function(data, callback)
{
    return this.sendAction('loginProctor', data, callback, { forceLogout: false });
};

// login (sessionID, ssid, firstName)
Sections.XhrManager.prototype.loginStudent = function(data, callback)
{
    return this.sendAction('loginStudent', data, callback, { forceLogout: false });
};

// get tests (grade)
Sections.XhrManager.prototype.getTests = function(data, callback)
{
    return this.sendAction('getTests', data, function(testSelections) {

        for (var i = 0; i < testSelections.length; i++)
        {
            var testSelection = testSelections[i];
            
            // translate reason key
            if (!Util.String.isNullOrEmpty(testSelection.reasonKey)) {
                testSelection.reasonText = Messages.get(testSelection.reasonKey);
            }

            // translate warning key
            if (!Util.String.isNullOrEmpty(testSelection.warningKey)) {
                testSelection.warningText = Messages.get(testSelection.warningKey);
            }
        }

        callback(testSelections);
    });
};

// get accommodations for a test
Sections.XhrManager.prototype.getSegmentsAccommodations = function(data, callback)
{
    var fixSegments = function(segmentsAccommodations)
    {
        if (segmentsAccommodations)
        {
            for (var i = 0; i < segmentsAccommodations.length; i++)
            {
                // create real accommodations object out of the segment accommodations json
                var accommodations = Accommodations.create(segmentsAccommodations[i]);
                accommodations.selectDefaults();
                segmentsAccommodations[i] = accommodations;
            }
        }

        Util.dir(segmentsAccommodations);
        callback(segmentsAccommodations);
    };

    return this.sendAction('getSegmentsAccommodations', data, fixSegments);
};

// open test (testKey, accommodations)
Sections.XhrManager.prototype.openTest = function(data, callback)
{
    return this.sendAction('openTest', data, callback);
};

Sections.XhrManager.prototype.pauseTest = function(callback)
{
    return this.sendAction('pauseTest', null, callback);
};

Sections.XhrManager.prototype.checkApproval = function(data, callback)
{
    var fixSegments = function(approval)
    {
        if (approval && approval.segmentsAccommodations)
        {
            var segmentsAccommodations = approval.segmentsAccommodations;

            for (var i = 0; i < segmentsAccommodations.length; i++)
            {
                // create real accommodations object out of the segment accommodations json
                var accommodations = Accommodations.create(segmentsAccommodations[i]);
                accommodations.selectAll(); // we only have acc's that we use, so select them all
                segmentsAccommodations[i] = accommodations;
            }
        }

        Util.dir(approval);
        callback(approval);
    };

    return this.sendAction('checkApproval', data, fixSegments, { showProgress: false });
};

Sections.XhrManager.prototype.denyApproval = function(callback)
{
    return this.sendAction('denyApproval', null, callback);
};

// start test
Sections.XhrManager.prototype.startTest = function(data, callback)
{
    return this.sendAction('startTest', data, callback);
};

// score test
Sections.XhrManager.prototype.scoreTest = function(callback)
{
    return this.sendAction('scoreTest', null, callback);
};

// get test scores
Sections.XhrManager.prototype.getDisplayScores = function(callback)
{
    return this.sendAction('getDisplayScores', null, callback, { showProgress: false, showDialog: false });
};

