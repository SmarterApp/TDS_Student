// validation tasks for Util.TaskWorkflow class
TestShell.Validation = {};

// check if grid has loaded
TestShell.Validation.checkGrid = function(taskWorkflow)
{
    var currentGroup = TestShell.PageManager.getCurrent();

    if (currentGroup && currentGroup.responses)
    {
        // loop through each response to see if there are any grid items
        for (var i = 0; i < currentGroup.responses.length; i++)
        {
            var response = currentGroup.responses[i];
            var item = response.getItem();

            // check if response is grid
            if (item && item.grid && 
                item.responseType.toLowerCase() == 'grid')
            {
                // make sure grid is either done loading or an error occured
                if (item.grid.getState() != GridState.Loaded && 
                    item.grid.getState() != GridState.Error)
                {
                    TestShell.Navigation.reset();
                    TestShell.UI.showWarning(Messages.get('TDSShellUIJS.Label.WaitForGridToLoad'));
                    taskWorkflow.cancel();
                }
            }
        }

        // delay the student from hitting next/back a bunch of times
        if (currentGroup.iframe)
        {
            var visitTime = (new Date() - currentGroup.iframe.timestamps.showDate);

            if (visitTime < 500) // delay for half second
            {
                TestShell.Navigation.reset();
                taskWorkflow.cancel();
            }
        }
    }
};

TestShell.Validation.checkAudioPlaying = function(taskWorkflow) {
    if (TDS.Audio.isActive()) {
        if (TestShell.Config.allowSkipAudio) {
            TDS.Audio.stopAll();
        } else {
            TestShell.Navigation.reset();
            TestShell.UI.showWarning(ErrorCodes.get('NavigateAudioPlaying'));
            taskWorkflow.cancel();
        }
    }
};

TestShell.Validation.checkAudioRecording = function(taskWorkflow) {
    if (TDS.Audio.Recorder.isCapturing()) {
        TestShell.UI.showWarning(ErrorCodes.get('SaveRecordingInProgress'));
        taskWorkflow.cancel();
    }
};

// makes sure there are no dirty responses that cannot be implictly saved before performing task
TestShell.Validation.checkDirtyResponses = function(taskWorkflow)
{
    var currentGroup = TestShell.PageManager.getCurrent();
    if (currentGroup == null || currentGroup.responses == null) return;

    for (var i = 0; i < currentGroup.responses.length; i++)
    {
        var response = currentGroup.responses[i];
        var item = response.getItem();

        // if this item doesn't allow implicit save and it is dirty then prompt the user they have unsaved work
        if (item && !item.saveOptions.implicit && response.isDirty())
        {
            TestShell.UI.showWarningPrompt('NavigateUnsaved',
            {
                yes: function() { response.undo(); taskWorkflow.resume(); },
                no: function() { TestShell.Navigation.reset(); taskWorkflow.cancel(); }
            });

            taskWorkflow.wait();
        }
    }
};

TestShell.Validation.checkRecorderQuality = function(taskWorkflow) {
    
    var currentGroup = TestShell.PageManager.getCurrent();
    if (currentGroup == null || currentGroup.responses == null) return;

    // TODO: Why do I get the first index? Review this.
    var response = currentGroup.responses[0];
    var item = response.getItem();

    // if this item has a recorder and is dirty then check the quality of the response
    if (item == null || item.recorder == null || response.isDirty() == false) return;

    var quality = TDS.Audio.Recorder.retrieveQuality(item.recorder /*id*/);

    // check quality
    if (YAHOO.lang.isString(quality) && quality.toUpperCase() != 'GOOD') {
        TestShell.UI.showWarningPrompt('SaveRecordingQuality', {
            yes: function() {
                taskWorkflow.resume();
            },
            yesLabel: 'Global.Prompt.Button.KeepIt',
            no: function() {
                TestShell.Navigation.reset();
                taskWorkflow.cancel();
            },
            noLabel: 'Global.Prompt.Button.TryAgain'
        });
        taskWorkflow.wait();
    }
};

// check segment transition
TestShell.Validation.checkSegment = function(taskWorkflow, result, transition)
{
    var fromGroup = transition.from;
    var toGroup = transition.to;

    // check if we are transitioning from one group to another
    if (fromGroup == null || toGroup == null) return;

    var fromSegment = fromGroup.getSegment();
    var toSegment = toGroup.getSegment();

    // check if we are moving from one segment to another
    if (fromSegment == toSegment) return;

    // if segment is not permeable and not finished then user cannot leave it
    if (fromSegment && !fromSegment.isPermeable() && !fromSegment.isFinished())
    {
        TestShell.Navigation.reset();
        taskWorkflow.cancel();

        var msgImpermeableIncomplete = 'Segment is not permeable so all the questions must be completed before going to another segment.';
        msgImpermeableIncomplete = Messages.getAlt('TestShell.Segment.ImpermeableIncomplete', msgImpermeableIncomplete);
        TestShell.UI.showWarning(msgImpermeableIncomplete);
    }
    // check if we should show a warning about leaving the segment
    else if ((fromSegment && !fromSegment.isPermeable()) || 
             (fromSegment && fromSegment.requireExitApproval()) || 
             (toSegment && toSegment.requireEntryApproval()))
    {
        var msgLeaveWarning = 'You are leaving the current segment, are you sure you want to do this?';
        msgLeaveWarning = Messages.getAlt('TestShell.Segment.LeaveWarning', msgLeaveWarning);

        TestShell.UI.showWarningPrompt(msgLeaveWarning,
        {
            yes: function() { taskWorkflow.resume(); },
            yesLabel: 'Yes',
            no: function() { TestShell.Navigation.reset(); taskWorkflow.cancel(); },
            noLabel: 'No'
        });

        taskWorkflow.wait();
    }

};

// check if there are any optional unanswered questions and check with the user before leaving the page
TestShell.Validation.checkOptionalUnanswered = function(taskWorkflow)
{
    // this doesn't apply to SIRVE
    if (TDS.isSIRVE) return;
    
    var currentGroup = TestShell.PageManager.getCurrent();
    if (currentGroup == null || currentGroup.responses == null) return;

    // only check optional items if the # required is 0 (otherwise we would get warnings with writing)
    if (currentGroup.numRequired > 0) return;

    var optionalUnansweredResponses = [];

    for (var i = 0; i < currentGroup.responses.length; i++)
    {
        var response = currentGroup.responses[i];

        // check if answered
        if (response.isAnswered()) continue;

        // check if required
        if (!response.isRequired) optionalUnansweredResponses.push(response);
    }

    if (optionalUnansweredResponses.length > 0)
    {
        var messageKey = 'OptionalUnansweredResponses';
        var messageParam = optionalUnansweredResponses[0].position;
        var messageText = ErrorCodes.get(messageKey, messageParam);

        TestShell.UI.showErrorPrompt(messageText,
        {
            yes: function() { taskWorkflow.resume(); },
            yesLabel: Messages.get('Global.Label.Yes'),
            no: function() { TestShell.Navigation.reset(); taskWorkflow.cancel(); },
            noLabel: Messages.get('Global.Label.No')
        });

        taskWorkflow.wait();
    }

};

// simulator
TestShell.Validation.checkSimulatorPlaying = function(taskWorkflow, result, errorMessage)
{
    var currentGroup = TestShell.PageManager.getCurrent();
    if (currentGroup == null || currentGroup.responses == null) return;

    // get simulator item (always first)
    var response = currentGroup.responses[0];
    var item = response.getItem();

    // make sure item has a simulator
    if (item == null || item.simulator == null) return;

    if (item.simulator.isPlaying())
    {
        errorMessage = errorMessage || 'SimulatorPlaying';
        TestShell.UI.showWarning(ErrorCodes.get(errorMessage));
        taskWorkflow.cancel();
    }
};

// check if responses have all been responded to before pausing
TestShell.Validation.checkBlockPausing = function(taskWorkflow)
{
    // get current page and check if it is 
    var page = TestShell.PageManager.getCurrent();
    if (page == null) return;

    // if block pausing is disabled then allow pausing
    var accs = TestShell.PageManager.getAccommodations(page);
    if (accs == null || !accs.createProps().blockPausing()) return;

    // if the pages the student has visited are completed then allow pausing
    if (TestShell.PageManager.isCompleted(true, true)) return;

    var validateStatus = function(status)
    {
        // check for status's that allow us to continue without warnings
        if (status == 5 /*paused*/) 
        {
            taskWorkflow.resume();
        } 
        else
        {
            // stop the user here they need to answer all visible questions
            TestShell.UI.showWarning(ErrorCodes.get('TestShell.Label.BlockPausing'));
            taskWorkflow.cancel();
        }
    };

    // check if the server is paused
    TestShell.xhrManager.getPauseStatus(validateStatus);
    taskWorkflow.wait();
};


// Check if this is a writing item and the item has not been checked.
TestShell.Validation.checkIfPromptSelected = function(taskWorkflow) {
    var page = TestShell.PageManager.getCurrent();

    if (page == null) return;

    // If the item is complete, then it must be checked.
    if (page.isCompleted()) {
        return;
    }

    var responses = page.responses;

    if (responses == null || responses.length < 1) {
        return;
    }

    // Each response has a pointer to its own item
    var item = responses[0].getItem();
    if ((item == null) ||  (item.responseType != "Prompt selection")) {
        return;
    }
        
    // If any are checked, return for now.
    for (var i = 0; i < responses.length; ++i) {
        item = responses[i].getItem();
        if (item.unit != null && item.unit.isSelected() == true) {
            // Open issue: is there a different error message for checked and not complete?
            return;
        }
    }
    // stop the user here they need to answer all visible questions
    TestShell.UI.showWarning(Messages.get('WritingNoPromptSelected'));
    taskWorkflow.cancel();
};
