Sections.TestResults = function()
{
    Sections.TestResults.superclass.constructor.call(this, 'sectionTestResults');
    this.addClick('btnScoreLogout', this.logout);
    
    this._pollAttempts = 5; // how many times we check for display scores
    this._pollDelay = 60000; // the delay between each display score check

    this.addClick('btnEnterMoreScores', this.redirectToTestSelectionSection);
};

YAHOO.lang.extend(Sections.TestResults, Sections.Base);

Sections.TestResults.prototype.load = function (summary)
{
    // get user info (cookies have escaping problem, there is + sign in them)
    /*var testeeId = YAHOO.util.Cookie.getSub('TDS-Student-Data', 'T_ID');
    var testeeFirstName = YAHOO.util.Cookie.getSub('TDS-Student-Data', 'T_FN');
    var testeeLastName = YAHOO.util.Cookie.getSub('TDS-Student-Data', 'T_LN');
    var testeeFullname = testeeLastName + ', ' + testeeFirstName;
    */

    // set test info from global variables
    YUD.get('lblName').innerHTML = window.tdsTestee.lastName + ', ' + window.tdsTestee.firstName;
    YUD.get('lblSSID').innerHTML = window.tdsTestee.id;
    YUD.get('lblTestName').innerHTML = window.tdsTestProps.displayName;

    // if there was a summary passed in then render it otherwise go load it
    if (summary != null) this.renderSummary(summary);
    else this.loadSummary();
};

Sections.TestResults.prototype.renderSummary = function(testSummary)
{
    Util.dir(testSummary);

    var resultsContainer = this.getContainer();

    // check if we are supposed to poll score scores
    if (testSummary.pollForScores)
    {
        if (this._pollAttempts > 0)
        {
            // schedule polling
            YUD.addClass(resultsContainer, 'scoreWaiting');
            this.pollSummary();
        }
        else
        {
            // stop polling
            YUD.addClass(resultsContainer, 'scoreTimedOut');
        }
    }
    else
    {
        // check what kind of scores we have to show
        var hasTestScores = (testSummary.testScores && testSummary.testScores.length > 0);
        var hasItemScores = (testSummary.itemScores && testSummary.itemScores.length > 0);

        // render scores if available
        if (hasTestScores) this.renderTestScores(testSummary.testScores);
        if (hasItemScores) this.renderItemScores(testSummary.itemScores, testSummary.viewResponses);
                
        // if there are no scores available then show explanation
        if (!hasTestScores && !hasItemScores)
        {
            // if this is PT then add class indicating score isn't available
            YUD.addClass(resultsContainer, TDS.inPTMode ? 'scoreUnavailableInPT' : 'scoreUnavailable');
        }
    }
};

// this gets called when there are display scores to write out to the screen
Sections.TestResults.prototype.renderTestScores = function(testScores)
{
    var resultsContainer = this.getContainer();

    // hide polling message
    YUD.removeClass(resultsContainer, 'scoreWaiting');
    
    // show score panel
    YUD.addClass(resultsContainer, 'scoreAvailable');

    // create score html
    var html = [];

    Util.Array.each(testScores, function(testScore) 
    {
        var scoreHtml = '<li><span class="scoreLabel">{label}</span><span class="scoreValue">{value}</span></li>';
        scoreHtml = YAHOO.lang.substitute(scoreHtml, testScore);
        html.push(scoreHtml);
    }, this);

    // write score
    var testScoresListEl = YUD.get('scoreList'); // <ul>
    testScoresListEl.innerHTML = html.join(' ');
};

Sections.TestResults.prototype.renderItemScores = function(itemScores, viewResponses)
{
    var resultsContainer = this.getContainer();

    // hide polling message
    YUD.removeClass(resultsContainer, 'scoreWaiting');
    
    // show score panel
    YUD.addClass(resultsContainer, 'scoreAvailable');

    // get table body
    var scoresTblEl = YUD.get('itemScores'); // <table>
    var scoresBodyEl = scoresTblEl.getElementsByTagName('tbody')[0];

    // create table rows
    Util.Array.each(itemScores, function(itemScore) 
    {
        var scoreRowEl = HTML.TR();

        // create position
        var scorePosEl;
        
        // check if we allow going back into the test to view responses
        if (viewResponses)
        {
            // create link back to test shell
            scorePosEl = HTML.A({ href: '#' }, itemScore.position);

            this.addClick(scorePosEl, function(ev) 
            {
                TDS.redirectTestShell(itemScore.page);
            });
        }
        else
        {
            // just show text
            scorePosEl = itemScore.position;
        }

        // Column "Question Number"
        scoreRowEl.appendChild(HTML.TD(null, scorePosEl));
        
        // Column "Your Answer"
        var responseText;
        
        if (itemScore.format != 'MC')
        {
            responseText = Messages.getAlt('ItemScores.Row.Format.' + itemScore.format, itemScore.response);
        }
        else
        {
            responseText = itemScore.response;
        }

        scoreRowEl.appendChild(HTML.TD(null, responseText));

        // Column "Correct Answer"
        var answerText;
        
        if (itemScore.format != 'MC')
        {
            answerText = Messages.getAlt('ItemScores.Row.Format.' + itemScore.format, itemScore.scoreRationale);
        }
        else
        {
            answerText = itemScore.scoreRationale;
        }
        
        scoreRowEl.appendChild(HTML.TD(null, answerText));
        
        // Column "Score"
        if (itemScore.score >= 0)
        {
            scoreRowEl.appendChild(HTML.TD(null, itemScore.score + '/' + itemScore.scoreMax));
        }
        else
        {
            scoreRowEl.appendChild(HTML.TD(null, Messages.getAlt('ItemScores.Row.NoScore', 'N/A')));
        }
        
        scoresBodyEl.appendChild(scoreRowEl);
    }, this);
    
    // show table
    YUD.setStyle(scoresTblEl, 'display', 'block');
};

// call this to wait and then try and load test summary
Sections.TestResults.prototype.pollSummary = function()
{
    this._pollAttempts--;
    YAHOO.lang.later(this._pollDelay, this, this.loadSummary);
};

// load test summary
Sections.TestResults.prototype.loadSummary = function()
{
    var self = this;
    
    ReviewShell.api.getDisplayScores(function(summary)
    {
        if (summary) self.renderSummary(summary);
    });
};

Sections.TestResults.prototype.logout = function()
{
    TDS.Dialog.showProgress();
    TDS.logout();
};

Sections.TestResults.prototype.redirectToTestSelectionSection = function()
{
    if (TDS.isProxyLogin)
    {
        var firstName = window.tdsTestee.firstName;
        var lastName = window.tdsTestee.lastName;
        var testeeID = window.tdsTestee.id;

        var message = Messages.get('TestResults.Link.EnterMoreScoresConfirm', [lastName, firstName, testeeID]);
        TDS.Dialog.showPrompt(message, function() 
        {
            //this logic may be useful also for SIRVE and that is why the check above is for isProxyLogin rather than isDataEntry.
            TDS.redirect('Pages/LoginShell.xhtml?section=sectionTestSelectionProxyReenter');
        });
    }
    else
    {
        /*
        * this button should never show up on a student page. but if for whatever reason it does show up we will set the default action to logout.
        */
        this.logout();
    }
};
