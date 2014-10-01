// entry point into the javascript from the review shell
function preinit()
{
    ReviewShell.init();
}

var ReviewShell =
{
    api: null, // xhr api
    workflow: null // section behavior management
};

// get the # of mins before a idle timeout should occur
ReviewShell.getTimeoutMins = function () {
    // get interface timeout from T_StartTestOpportunity
    var interfaceTimeout = YAHOO.util.Cookie.getSub('TDS-Student-Data', 'TC_IT');
    if (interfaceTimeout) {
        return interfaceTimeout * 1;
    } else {
        return TDS.timeout;
    }
};

ReviewShell.startTimeoutIdle = function() {
    var waitMins = ReviewShell.getTimeoutMins();
    if (waitMins > 0) {
        var respondSecs = 30;
        var idleTimer = new TimeoutIdle(waitMins, respondSecs, function() {
            // send notice to db
            TDS.Diagnostics.logServerError('Idle timeout on review shell.', null, function () {
                // redirect back to login shell (TODO: Do a proper pause here)
                TDS.logout();
            });
        });
        idleTimer.start();
    }
};

ReviewShell.init = function()
{
    // create xhr api with 30 sec timeout and 1 retry
    this.api = new Sections.XhrManager(ReviewShell);

    // create workflow
    this.workflow = ReviewShell.createWorkflow();
    ReviewShell.start();

    // start idle timer
    ReviewShell.startTimeoutIdle();
};

ReviewShell.start = function()
{
    if (TDS.showItemScores) this.workflow.start('sectionTestResults');
    else this.workflow.start('sectionTestReview');
};

ReviewShell.createWorkflow = function()
{
    var wf = Sections.createWorkflow();

    // logging
    wf.Events.subscribe('onRequest', function(activity) { Util.log('Section Request: ' + activity); });
    wf.Events.subscribe('onReady', function(activity) { Util.log('Section Ready: ' + activity); });
    wf.Events.subscribe('onLeave', function(activity) { Util.log('Section Hide: ' + activity); });
    wf.Events.subscribe('onEnter', function(activity) { Util.log('Section Show: ' + activity); });

    // create sections
    wf.addActivity(new Sections.TestReview());
    wf.addActivity(new Sections.TestResults());
    wf.addActivity(new Sections.Logout());

    /* create transitions */

    // "Congratulations, you have reached the end of the test!"
    wf.addTransition('sectionTestReview', 'back', 'sectionTestShell'); // "Review My Answers"
    wf.addTransition('sectionTestReview', 'next', 'sectionTestResults'); // "Submit Test"

    // "Your Results"
    
    return wf;
};



