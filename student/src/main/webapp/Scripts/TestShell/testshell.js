"use strict";

var TestShell =
{
    name: null, // test shell name

    // these are variables:
    initializing: true,
    testLengthMet: false, // no more groups will be created
    testFinished: false, // all questions on the test have been responded to

    idleTimer: null, // idle timer object

    autoSaveTimer: null, // auto save timer object
    autoSaveInterval: 120, // # of seconds until we try to auto save

    enableKeyEvents: true,
    xhrManager: null,
    
    muted: false, // did we mute the volume for this test?
    
    allowUnloading: false  // indicates when to allow user to navigate away from test shell
};

// gets loaded from TestShellScripts.ascx code behind AddTestConfig()
// preset sane defaults
TestShell.Config = 
{
    urlBase: './', // the base url of the entire site
    reviewPage: 0, // this is used for when we come back to the shell from the review page
    hasAudio: false, // does this test have audio player/recorder
    testName: 'Unknown', // the name of the test displayed to the student
    testLength: 0, // The # of questions on the test.
    startPosition: 1, // What question to first show the student.
    contentLoadTimeout: 120, // The # of seconds until you abort trying to load content.
    interfaceTimeout: 20, // The # of minutes the student can be idle before logging them out.
    interfaceTimeoutDialog: 30, // The # of seconds the student has to respond to the logout dialog before logging them out.
    requestInterfaceTimeout: 40, // The # of minutes the student can be idle after making a print request before logging them out.
    oppRestartMins: 20, // The # of minutes until a pause will expire answered questions.
    autoSaveInterval: 120, // # of seconds until we try to auto save
    forbiddenAppsInterval: 60, // # of seconds until we check for forbidden apps
    environmentCheckInterval: 30,
    disableSaveWhenInactive: false,
    disableSaveWhenForbiddenApps: false,
    disableSaveWhenEnvironmentCompromised: false,
    allowSkipAudio: false,
    showSegmentLabels: false,
    audioTimeout: 180, // # of seconds (set 0 to turn off, default is 3 mins)
    enableLogging: false // enable logging in TestShell.Logging
};

TestShell.Events = new Util.EventManager(TestShell);

TestShell.SaveRequest =
{
    Manual: 0,
    Implicit: 1,
    Auto: 2
};

// call this function to load the test shell
TestShell.load = function()
{
    // check if we need to load audio applet for ELPA
    /*if (TestShell.Config.hasAudio) {
        TestShell.waitForAudio(shellInitWrapper);
    }*/

    try {
        TestShell.init();
    } catch(ex) {
        TDS.Diagnostics.report(ex);
    }
};

// call this function with a callback to wait for audio player to become ready
/*
TestShell.waitForAudio = function(fn) {

    // callback for when audio times out
    var audioFailed = function() {

        // do you want to try again?
        TDS.Dialog.showPrompt(Messages.get('TestShell.Label.AudioTimeout'),
            function() { // yes
                location.reload(); // reload test shell
                TestShell.UI.showLoading('');
            },
            function() { // no
                TestShell.redirectLogin(); // go to login page
            });

        // write audio debug log
        TestShell.Logging.sendAudioReport();
    };

    // check for audio timeout
    var audioTimer = null;
    
    if (TestShell.Config.audioTimeout > 0) {
        var audioTimeoutMillis = TestShell.Config.audioTimeout * 1000;
        audioTimer = YAHOO.lang.later(audioTimeoutMillis, this, audioFailed);
    }

    // show progress screen
    var msg = Messages.getAlt('TestShell.Label.WaitForAudio', 'Waiting for audio player');
    TestShell.UI.showLoading(msg);

    // wait for audio to get intialized
    AudioManager.onInit.subscribe(function() {
        if (audioTimer) {
            audioTimer.cancel();
        }
        fn();
    });
    
    // start setting up audio players
    AudioManager.setup();
};
*/

// call this function with a callback to wait for audio mixer to become ready
/*
TestShell.waitforMixer = function(fn) {

    var msg = Messages.getAlt('TestShell.Label.WaitForAudioPlayer', 'Waiting for audio mixer');
    TestShell.UI.showLoading(msg);

    // when mixer is ready mute test
    AudioMixer.onLoaded.subscribe(function() {
        // if we are unmuted then mute
        if (!AudioMixer.isMuted()) {
            TestShell.muted = true;
            AudioMixer.mute();
        }

        fn();
    });

    // when mixer fails
    AudioMixer.onFailed.subscribe(function() {
        var errorMsg = Messages.getAlt('TestShell.Label.FailedAudioMixer', 'Failed to load audio mixer');
        TDS.Dialog.showWarning(errorMsg, function() {
            TestShell.redirectLogin();
        });
    });

    // create mixer applet frame
    AudioMixer.init();
};
*/

// this function to unload the test shell
TestShell.unload = function(event)
{
    // Check if the navigation away from this page is allowed. If not, give the user a warning
    if (!TestShell.allowUnloading) {
        return Messages.getAlt('TestShell.Label.leavingPageAlert', 'You are attempting to leave the test. If you select OK, the test will be paused. Select cancel to continue your test.');        
    }
        
    // always try and stop TTS before leaving a page
    TTS.Manager.stop();

    // check for valid exit.
    if (TDS.isProxyLogin)
    {
        TDS.CLS.LogoutComponent.PageUnloadEvent.fire(arguments);
    }
    
    // check if we muted the test during start and it is still muted
    if (TestShell.muted &&  Util.SecureBrowser.isMuted()) {
        Util.SecureBrowser.unmute();
    }
};

// this function gets called when the test shell scripts/html/css is ready
TestShell.init = function () {
    
    TestShell.UI.showLoading(Messages.getAlt('TestShell.Label.Initializing', 'Initializing'));

    // initialize xhr
    this.xhrManager = new TestShell.XhrManager();

    // initialize the TDS object and load global configs
    TDS.init();

    // initialize the UI
    TestShell.UI.init();

    // set url of iframe for resource dialogs
    ContentManager.Dialog.urlFrame = TestShell.Config.urlBase + 'Pages/DialogFrame.xhtml';

    // subscribe to button clicks
    this.subscribeDomEvents();

    // subscribe to keyboard events
    KeyManager.init();

    // check if any forbidden apps (also starts timer)
    if (TestShell.checkForbiddenApps()) return;

    // check if the environment is secure (also starts timer)
    if (TestShell.checkForEnvironmentSecurity()) return;

    // initialize content manager
    ContentManager.init(TDS.baseUrl);
    ContentManager.setReadOnly(TDS.isReadOnly);

    // set client for renderer
    ContentManager.Renderer.setClient(TDS.clientStylePath);

    // setup audio player
    var flashPath = TDS.resolveBaseUrl('Scripts/Libraries/soundmanager2/swf/');
    TDS.Audio.Player.setup(flashPath);

    // FIX: json parser for MS dates when serializing (only works with YUI 2.7.0 version of JSON)
    // NOTE FROM YUI for 2.8.0: Overriding YAHOO.lang.JSON.dateToString is no longer recommended because the default ISO 8601 
    // serialization is defined in the spec. If Dates must be formatted differently, either preprocess the data or 
    // set useNativeStringify to false after overriding dateToString.
    YAHOO.lang.JSON.dateToString = function (d) {
        // UTC milliseconds since Unix epoch (M$-AJAX serialized date format (MRSF))
        return '\/Date(' + d.getTime() + ')\/';
    };

    // NOTE: this needs to be after overriding YAHOO.lang.JSON.dateToString in 2.8.0
    // YAHOO.lang.JSON.useNativeStringify = false; // we want to overwrite dateToString so we can't use native JSON stringifier right now

    // create idle timer (we will start this once a item finishes loading)
    var idleTimeout = function () {
        // send pause notice to server and whether it fails or succeeds redirect to login
        TestShell._pauseInternal(true, 'timeout', TestShell.Config.disableSaveWhenInactive);
    };

    this.idleTimer = new TimeoutIdle(TestShell.Config.interfaceTimeout, TestShell.Config.interfaceTimeoutDialog, idleTimeout);
    this.idleTimer.start();

    // load segments
    TestShell.SegmentManager.init();

    // load tools
    TestShell.Tools.init();

    // get current responses
    TestShell.ResponseManager.ping();

    // hook up CLS listeners
    if (TDS.isProxyLogin) TDS.CLS.LogoutComponent.init();

    // let everyone know the test shell has been initialized
    TestShell.Events.fire('init');
};

// attach DOM events to the test shell
TestShell.subscribeDomEvents = function()
{
    // TOOLS:
    TestShell.UI.Events.btnZoomIn.subscribe(function() { TestShell.UI.zoomIn(); });
    TestShell.UI.Events.btnZoomOut.subscribe(function() { TestShell.UI.zoomOut(); });
    TestShell.UI.Events.btnLineReader.subscribe(function() { TestShell.UI.toggleLineReader(); });
    TestShell.UI.Events.btnFormula.subscribe(function() { TestShell.Tools.toggleFormula(); });
    TestShell.UI.Events.btnPeriodic.subscribe(function() { TestShell.Tools.togglePeriodicTable(); });
    TestShell.UI.Events.btnCalculator.subscribe(function() { TestShell.Calculator.toggle(); });
    TestShell.UI.Events.btnPrint.subscribe(function() { TestShell.Print.passage(); });
    
    // NAVIGATION:
    TestShell.UI.Events.btnSave.subscribe(function() { TestShell.save(TestShell.SaveRequest.Manual); });
    TestShell.UI.Events.btnPause.subscribe(function() { TestShell.pause(); });
    TestShell.UI.Events.btnBack.subscribe(function() { TestShell.Navigation.back(); });
    TestShell.UI.Events.btnNext.subscribe(function() { TestShell.Navigation.next(); });
    TestShell.UI.Events.btnEnd.subscribe(function() { TestShell.complete(); });
    TestShell.UI.Events.btnResults.subscribe(function() { TestShell.testResults(); });
    TestShell.UI.Events.btnHelp.subscribe(function() { TestShell.Tools.toggleHelp(); });

    YUE.on(TestShell.UI.Nodes.ddlNavigation, 'change', function() { TestShell.Navigation.change(); });
};

TestShell.getHandlersUrl = function(handler)
{
    var urlBuilder = [];
    urlBuilder.push(TDS.baseUrl);
    urlBuilder.push('Pages/API/');
    if (handler) urlBuilder.push(handler);
    return urlBuilder.join('');
};

// if this is true then the test has been completed and we can leave the test shell
TestShell.isTestCompleted = function()
{
    // check if the test length has been met and we have pages
    if (TestShell.testLengthMet && TestShell.PageManager.hasPages())
    {
        // check if all the pages have been completed
        return TestShell.PageManager.isCompleted();
    }

    return false;
};

// runs some validations and then pauses the test
TestShell.pause = function()
{
    var taskWorkflow = new Util.TaskWorkflow();

    // WARNING: It is possible the applet could be recording when you try and record.
    // (e.x., try recording something and save, then record something else, pause and say yes.. it will call undo)
    taskWorkflow.add(TestShell.Validation.checkDirtyResponses);
    taskWorkflow.add(TestShell.Validation.checkIfPromptSelected);
    taskWorkflow.add(TestShell.Validation.checkSimulatorPlaying, 'SimulatorPlayingWhileNavigating');

    // check block pausing
    taskWorkflow.add(TestShell.save, this, true);
    taskWorkflow.add(TestShell.Validation.checkBlockPausing);
 
    taskWorkflow.start(this._pauseInternal, this);
};

// internal function for pausing test
TestShell._pauseInternal = function(silent, reason, disableSave)
{
    // before we redirect to a different page, disable all exit check scripts.
    if (TDS.isProxyLogin) TDS.CLS.LogoutComponent.PageUnloadEvent.unsubscribeAll();
 
    // Save all items that can be implicit saved.
    // NOTE: If this was called from public function then there should be nothing to save.
    if (!disableSave) this.save();

    // send pause request to server
    var sendPause = function()
    {
        // check for a reason and set default if none
        if (!YAHOO.lang.isString(reason)) reason = 'manual';
        
        // send pause notice to server and whether it fails or succeeds redirect to login
        TestShell.xhrManager.queueAction('pause', { reason: reason }, function()
        {
            if (TDS.isProxyLogin) {
                TestShell.redirectProxyLogout();
            } else {
                TestShell.redirectLogin();
            }
        });
    };

    if (silent === true)
    {
        sendPause();
    }
    else
    {
        // HACK: writing gets different pause message
        var pauseMessage = (TestShell.Frame.getWriting()) ? ErrorCodes.get('WritingPause') : ErrorCodes.get('Pause', [TestShell.Config.oppRestartMins]);

        TestShell.UI.showWarningPrompt(pauseMessage,
        {
            yes: sendPause
        });
    }
};

TestShell.complete = function () {
    var taskWorkflow = new Util.TaskWorkflow();
    taskWorkflow.add(TestShell.Validation.checkAudioPlaying);
    taskWorkflow.add(TestShell.Validation.checkDirtyResponses);
    taskWorkflow.add(TestShell.Validation.checkAudioRecording);
    taskWorkflow.add(TestShell.Validation.checkRecorderQuality);
    taskWorkflow.add(TestShell.Validation.checkIfPromptSelected);
    taskWorkflow.add(TestShell.Validation.checkSimulatorPlaying, 'SimulatorPlayingWhileNavigating');

    if (TDS.isProxyLogin) {
        // before we redirect to a different page, disable all exit check scripts.
        TDS.CLS.LogoutComponent.PageUnloadEvent.unsubscribeAll();

        if (TDS.isSIRVE)
	    {
	        //if SIRVE do not do any validation and just redirect to login shell.
            TestShell.redirectLogin();
	        return;
	    }
    }

    taskWorkflow.start(this._completeInternal, this);
};

// This redirects to the review shell. Used when in item score review mode.
TestShell.testResults = function()
{
    TDS.redirect('Pages/ReviewShell.xhtml');
};

TestShell._completeInternal = function()
{
    // check if button is visible, if not then don't allow this function to work (prevents shortcut from still working)
    var btn = YUD.getStyle('btnEnd', 'display');
    if (btn == 'none') return;

    // save current page
    this.save();

    var hasUnanswered = false;

    // make sure each group has a valid response
    Util.Array.each(TestShell.PageManager.getGroups(), function(group)
    {
        if (!group.isCompleted())
        {
            TestShell.UI.showWarning(ErrorCodes.get('EndUnanswered'));
            hasUnanswered = true;
        }
    });

    if (hasUnanswered) return;

    // show warning
    TestShell.UI.showWarningPrompt('Complete', 
    {
        yes: function()
        {
            // TestShell.Navigation.hideCurrentPage(); // try and hide the current frame
            
            // send complete notice to server and redirect to review screen if succeeds
            TestShell.xhrManager.queueAction('complete', null, function()
            {
                TestShell.redirectReview();
            });
        }
    });
};

// save the responses on the page 
// (NOTE: if savetype is not specified then it is considered implicit)
TestShell.save = function (saveRequest) {
    // Bug 93008: save() was blocking page navigation in SIRVE - it shouldn't have been applied to SIRVE after all
    if (TDS.isSIRVE) return;
    
    if (saveRequest == null) saveRequest = TestShell.SaveRequest.Implicit;

    if (saveRequest == TestShell.SaveRequest.Manual)
    {
        var taskWorkflow = new Util.TaskWorkflow();
        taskWorkflow.add(TestShell.Validation.checkAudioRecording);
        taskWorkflow.add(TestShell.Validation.checkRecorderQuality);
        taskWorkflow.add(TestShell.Validation.checkSimulatorPlaying, 'SimulatorPlayingWhileSaving');
        taskWorkflow.add(TestShell.Validation.checkIfPromptSelected);
        taskWorkflow.start(function () { this._saveInternal(saveRequest); }, this);
    }
    else
    {
        this._saveInternal(saveRequest);
    }
};

// Sends any unsaved responses to the server (if autosave is true then this save required was made by the timer)
TestShell._saveInternal = function(saveRequest)
{
    // get current group
    var currentGroup = TestShell.PageManager.getCurrent();
    if (currentGroup == null) return;

    // ignore performing auto save when an action is being performed
    if (saveRequest == TestShell.SaveRequest.Auto && TestShell.xhrManager.hasAction()) return;

    var saveResponses = [];

    Util.Array.each(currentGroup.responses, function(response)
    {
        // make sure item has been loaded
        var item = response.getItem();
        if (item == null) return;

        // check if this item supports the current save request
        if (saveRequest == TestShell.SaveRequest.Manual && !item.saveOptions.explicit) return;
        if (saveRequest == TestShell.SaveRequest.Auto && !item.saveOptions.auto) return;
        if (saveRequest == TestShell.SaveRequest.Implicit && !item.saveOptions.implicit) return;

        // check if in spell check mode
        if (item.spellCheck && item.spellCheck.isEnabled())
        {
            // ignore auto save when spell checking
            if (saveRequest == TestShell.SaveRequest.Auto) return;

            // disable spell check so we can save
            item.spellCheck.disable();
        }

        // check if response is dirty (this will try and get the items response, so if doing so is unsafe then be careful)
        if (!response.isDirty()) return;

        // HACK: finalize grid
        if (item.grid)
        {
            item.grid.canvas.stopAction();
            item.grid.canvas.clearFocused();
        }

        var itemResponse = item.getResponse();

        if (!itemResponse)
        {
            Util.log('There is no item response.');
            return;
        }

        // HACK: set recorder to not dirty
        if (item.recorder) {
            var recorderObj = TDS.Audio.Widget.getRecorder(item.recorder);
            if (recorderObj) {
                recorderObj.dirty = false;
            }
        }

        // response.sequence++;
        response.value = itemResponse.value;
        response.isSelected = itemResponse.isSelected;
        response.isValid = itemResponse.isValid;

        saveResponses.push(response);
    });

    // check if there are any responses to save
    // BUG #57542: we must check for this because if we send empty responses 
    // and an action is assigned to xhrmanager it will get triggered
    if (saveResponses.length > 0)
    {
        TestShell.ResponseManager.sendResponses(saveResponses);
    }

    TestShell.autoSaveStart();
};

TestShell.autoSaveStart = function()
{
    if (this.autoSaveTimer != null) this.autoSaveTimer.cancel();

    // check if there is an auto save interval
    if (TestShell.Config.autoSaveInterval == 0) return;

    var autoSaveMillis = (TestShell.Config.autoSaveInterval * 1000);

    this.autoSaveTimer = YAHOO.lang.later(autoSaveMillis, this, function()
    {
        this.save(TestShell.SaveRequest.Auto);
    });
};

TestShell.redirectProxyLogout = function () {
    TestShell.allowUnloading = true;
    TDS.logoutProctor(false);
};

TestShell.redirectLogin = function()
{
    TestShell.allowUnloading = true;
    TestShell.UI.showLoading('');
    var url = TDS.baseUrl + 'Pages/LoginShell.xhtml?logout=true';
    top.location.href = url;
};

TestShell.redirectReview = function()
{
    TestShell.allowUnloading = true;
    TestShell.UI.showLoading('');
    var url = TDS.baseUrl + 'Pages/ReviewShell.xhtml';
    top.location.href = url;
};

// redirect to the error page
TestShell.redirectError = function(text)
{
    TestShell.allowUnloading = true;
    var url = TDS.baseUrl + 'Pages/Notification.xhtml';

    if (YAHOO.util.Lang.isString(text))
    {
        url += '?message=' + encodeURIComponent(text);
    }

    top.location.href = url;
};

// set timer for forbidden apps
TestShell.checkForbiddenApps = function()
{
    // check if forbidden apps checking is disabled
    if (TDS.Debug.ignoreForbiddenApps) return false;
    if (!(TestShell.Config.forbiddenAppsInterval > 0)) return false;

    // check if this school is excluded
    if (Util.Browser.readCookie('TDS-Student-ExcludeSchool') == 'True') return false;

    var forbiddenApps = Util.SecureBrowser.getForbiddenApps();

    // if there are any forbidden apps then alert user and log them out
    if (forbiddenApps.length > 0)
    {
        var message = Messages.get('ForbiddenApps') + forbiddenApps[0].desc;

        TestShell.UI.showAlert('Error', message, function()
        {
            TestShell._pauseInternal(true, 'forbiddenApps', TestShell.Config.disableSaveWhenForbiddenApps);
        });

        return true;
    }

    // check 30 seconds from now again
    var forbiddenAppsMillis = (TestShell.Config.forbiddenAppsInterval * 1000);
    YAHOO.lang.later(forbiddenAppsMillis, this, TestShell.checkForbiddenApps);
    return false;
};

// set timer for checking that the environment is still secure
TestShell.checkForEnvironmentSecurity = function () {
    
    // if the environment security has been breached, alert user and log them out
    if (!Util.SecureBrowser.isEnvironmentSecure()) {
        var error = Messages.getAlt('TestShell.Alert.EnvironmentInsecure', 'Environment is not secure. Your test will be paused.');
        TestShell.UI.showAlert('Error', error, function () {
            TestShell._pauseInternal(true, 'Environment Security', TestShell.Config.disableSaveWhenEnvironmentCompromised);
        });

        return true;
    }

    // check 30 seconds from now again
    var timerMillis = (TestShell.Config.environmentCheckInterval * 1000);
    YAHOO.lang.later(timerMillis, this, TestShell.checkForEnvironmentSecurity);
    return false;
};


// this function gets called when an iframe gets logged out and redirected back to the login page
function onFrameLogout()
{
    if (top._frameLoggedOut) return;
    top._frameLoggedOut = true;

    var logoutError = Messages.get('TDSShellJS.Label.FrameLogout');
    TestShell.redirectError(logoutError);
}
