Sections.TestSelection = function() {
    Sections.TestSelection.superclass.constructor.call(this, 'sectionTestSelection');
    this.testSelections = null;
};

YAHOO.lang.extend(Sections.TestSelection, Sections.Base);

Sections.TestSelection.Status = {
    disabled: 0,
    hidden: 1,
    start: 2,
    resume: 3
};

Sections.TestSelection.prototype.init = function() {
    this.addClick('btnLogout', function() {
        this.request('back');
    });
};

// load the tests
Sections.TestSelection.prototype.load = function(grade) {

    // erase any test previous selections
    LoginShell.testSelection = null;

    // HACK: if the grade is null and there are previously loaded tests then just show them
    if (grade == null && this.testSelections != null) {
        return false;
    }

    var self = this;

    // call xhr
    LoginShell.api.getTests({ grade: grade }, function(testSelections) {
        if (testSelections) {
            // render tests
            self.testSelections = testSelections;
            self.ready(testSelections);
        }
    });

    return true; // tell workflow to wait for xhr to finish
};

// render the test selections
Sections.TestSelection.prototype.enter = function() {
    
    // debug info
    Util.dir(this.testSelections);

    var testSelectionsEl = YUD.get('testSelections');

    // clear html
    if (testSelectionsEl.innerHTML.length > 0) {
        testSelectionsEl.innerHTML = '';
    }

    // add test properties
    if (this.testSelections && this.testSelections.length > 0) {
        for (var i = 0; i < this.testSelections.length; i++) {
            var testSelection = this.testSelections[i];
            var tableRow = this._createButton(testSelection, i + 1);
            if (tableRow != null) {
                testSelectionsEl.appendChild(tableRow);
            }
        }
    } else {
        testSelectionsEl.innerHTML = '<span id="testSelectionsEmpty" i18n-content="Opportunity.Label.NoTests"></span>';
    }

    // apply message translations
    TDS.Messages.Template.processLanguage(testSelectionsEl);
};

// this function creates a single test selection div
Sections.TestSelection.prototype._createButton = function(testSelection, idx) {
    
    /*
    Login.TS.HeaderStart = 'Start {0} Test';
    Login.TS.HeaderResume = 'Resume {0} Test';
    Login.TS.HeaderInactive = 'You\'ve used all your opportunities';
    Login.TS.DescActive = 'This is <em class="">opportunity {0} of {1}</em>';
    Login.TS.DescInactive = 'You\'ve used all your opportunities';
    */

    // figure out all the labels
    var testName = testSelection.displayName;

    // if score entry app then add mode
    if (TDS.isDataEntry) {
        testName += ' (' + testSelection.mode + ')';
    }

    var testActive = false; // can we begin test
    var testClass = ''; // start, resume or inactive
    var testHeader = '';
    var testDesc = '';
    var testOpp = '';

    if (testSelection.status == Sections.TestSelection.Status.start) {
        testActive = true;
        testClass = 'start';
        testHeader = '<span class="testAction">Start</span> ' + testName;
        testDesc = 'This is ';
    } else if (testSelection.status == Sections.TestSelection.Status.resume) {
        testActive = true;
        testClass = 'resume';
        testHeader = '<span class="testAction">Resume</span> ' + testName;
        testDesc = 'This is ';
    } else if (testSelection.status == Sections.TestSelection.Status.disabled) {
        testClass = 'inactive';
        testHeader = '<span class="testAction">Inactive</span> ' + testName;
        testDesc = testSelection.reasonText;
    } else {
        // hidden
        return null;
    }

    testOpp = Messages.get('Login.TS.DescActive', [testSelection.opportunity, testSelection.maxOpportunities]);

    // render the html:

    // create test selection button
    var testButtonEl = HTML.DIV();
    YUD.addClass(testButtonEl, 'testSelection');
    YUD.addClass(testButtonEl, testClass);
    YUD.addClass(testButtonEl, (idx % 2 == 0) ? 'even' : 'odd');
    YUD.setAttribute(testButtonEl, 'role', 'button');
    YUD.setAttribute(testButtonEl, 'tabindex', 0);

    // create test header
    var testHeaderEl = HTML.H3();
    testHeaderEl.innerHTML = testHeader;
    testButtonEl.appendChild(testHeaderEl);

    // create test description
    var testDescEl = HTML.P();
    testDescEl.innerHTML = testDesc;

    if (testActive) {
        testDescEl.appendChild(HTML.SPAN(null, testOpp));
    }

    testButtonEl.appendChild(testDescEl);

    if (testActive) {
    // add mouse click
    this.addClick(testButtonEl, function(evt) {
            this.select(testSelection);
    });

    // add enter key
        YUE.on(testButtonEl, 'keypress', function(ev) {
            var charCode = YUE.getCharCode(ev);
            if (charCode == 13) {
            this.select(testSelection);
        }
    }, this, true);
    }

    return testButtonEl;
};

// called when someone clicks on start/resume on a test
Sections.TestSelection.prototype.select = function(testSelection, skipWarning) {

    var self = this;
    
    // check if there is a warning
    if (!skipWarning && !Util.String.isNullOrEmpty(testSelection.warningText)) {
        TDS.Dialog.showPrompt(testSelection.warningText, function() {
            self.select(testSelection, true);
        });
        return false;
    }

    // save test selection
    LoginShell.setTestSelection(testSelection);

    // show progress since we might be detecting java
    TDS.Dialog.showProgress();

    // FLASH: check if the version of flash installed meets the minimum requirements
    if (testSelection.requirements.flashVersion > 0) {
        if (Util.Browser.getFlashVersion() < testSelection.requirements.flashVersion) {
            var message = ErrorCodes.get('Opportunity.Javascript.NoFlash', [testSelection.requirements.flashVersion]);
            TDS.Dialog.showWarning(message);
            return false;
        }
    }

    // JAVA: check if this test requires java and we are not skipping the check
    /*if (testSelection.requirements.javaVersion > 0)
    {
        // setProgressMessage('<%= Message("Opportunity.Javascript.DetectingJava") %>');

        // call this if java failed to be detected
        var javaFailure = function()
        {
            Util.log('getJREVersionAsync: failure');

            var message = ErrorCodes.get('Opportunity.Javascript.NoJava', [testSelection.requirements.javaVersion]);
            TDS.Dialog.showWarning(message);
        };

        // call this if java was detected
        var javaSuccess = function(version)
        {
            Util.log('getJREVersionAsync: success');

            // check if the version of java installed meets the minimum requirements
            if (version.applet < testSelection.requirements.javaVersion)
            {
                javaFailure();
            }
            else
            {
                self.open(testSelection);
            }
        };

        var callback =
        {
            success: javaSuccess,
            failure: javaFailure,
            codebase: window.javaFolder + '/Utilities/'
        };

        // begin java detction
        Util.log('getJREVersionAsync: begin');
        Util.Browser.getJREVersionAsync(callback, window.javaFolder + '/Utilities/');
        return false;
    }*/

    self.open(testSelection);
    return true;
};

// call the server to open test
Sections.TestSelection.prototype.open = function(testSelection) {

    var self = this;

    // Create the form values to send to the server.
    var request = {
        testKey: testSelection.key,
        testID: testSelection.id,
        oppKey: testSelection.oppKey, // required for SIRVE purposes.
        subject: testSelection.subject,
        grade: testSelection.grade
    };

    // if this is a proctorless session and the test has never been started then show accommodations selection
    if (LoginShell.session.isProctorless && testSelection.status == 2) {
        // get accommodations for test
        LoginShell.api.getSegmentsAccommodations(request, function(accommodations) {
            if (accommodations) {
                self.request('acc', accommodations);
            }
        });
    } else {
        // submit test for approval
        LoginShell.api.openTest(request, function(oppInfo) {
            if (oppInfo) {
                LoginShell.setOppInfo(oppInfo);
                self.request('next');
            }
        });
    }
};
