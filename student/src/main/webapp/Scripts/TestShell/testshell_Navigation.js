TestShell.Navigation = {};

TestShell.Navigation.State = 
{
    Initializing: 'init', // default state when starting test
    Change: 'change', // used when directly going to a page from the dropdown
    Back: 'back', // used when going back
    Next: 'next', // used when gong next
    Viewing: 'view' // viewing a page and not in a state transition
};

TestShell.Navigation._currentState = TestShell.Navigation.State.Initializing;

// get the current state
TestShell.Navigation.getState = function() { return this._currentState; };

// set the current state
TestShell.Navigation.setState = function(state) { this._currentState = state; };

// get the validation workflow used for navigation
TestShell.Navigation.createWorkflow = function(fromGroup, toGroup)
{
    var taskWorkflow = new Util.TaskWorkflow();
    taskWorkflow.add(TestShell.Validation.checkAudioPlaying);
    taskWorkflow.add(TestShell.Validation.checkGrid);
    taskWorkflow.add(TestShell.Validation.checkDirtyResponses);
    taskWorkflow.add(TestShell.Validation.checkAudioRecording);
    taskWorkflow.add(TestShell.Validation.checkRecorderQuality);
    taskWorkflow.add(TestShell.Validation.checkIfPromptSelected);
    taskWorkflow.add(TestShell.Validation.checkSimulatorPlaying, 'SimulatorPlayingWhileNavigating');

    // segment transition
    var transition = { from: fromGroup, to: toGroup };
    taskWorkflow.add(TestShell.Validation.checkSegment, transition);
    
    // add proxy login specific validations 
    if (TDS.isProxyLogin)
    {
        // before checking optional unanswered commit any unsaved work (BUG #59138)
        taskWorkflow.add(TestShell.save, TestShell, true);
        taskWorkflow.add(TestShell.Validation.checkOptionalUnanswered);
    }

    return taskWorkflow;
};

// moves to the currently selected page in the dropdown
TestShell.Navigation.change = function()
{
    var currentGroup = TestShell.PageManager.getCurrent();
    var selectedGroup = TestShell.PageManager.get(TestShell.UI.Nodes.ddlNavigation.value);

    var taskWorkflow = TestShell.Navigation.createWorkflow(currentGroup, selectedGroup);
    taskWorkflow.start(this._changeInternal, this);
};

// call this function when a change is made to the page dropdown
TestShell.Navigation._changeInternal = function()
{
    // save current page
    TestShell.save();

    var currentGroup = TestShell.PageManager.getCurrent();

    // get selected group
    var groupID = TestShell.UI.Nodes.ddlNavigation.value;
    var selectedGroup = TestShell.PageManager.get(groupID);

    // make sure the group were going to is visible
    if (!selectedGroup.isVisible())
    {
        this.update();
        this.reset();

        TestShell.UI.showWarning(ErrorCodes.get('NextUnanswered'));
        Util.log('TestShell.changePage (FAIL: not visible): ' + selectedGroup.id);
        return;
    }
    else
    {
        Util.log('TestShell.changePage: ' + selectedGroup.id);
    }

    TestShell.Navigation.setState(TestShell.Navigation.State.Change);
    
    // hide current group
    if (currentGroup) currentGroup.hide();
    
    // set the selected group to be current and request it
    TestShell.PageManager.setCurrent(selectedGroup);
    this.requestPage();
};

// moves to the previous page
TestShell.Navigation.back = function()
{
    var currentGroup = TestShell.PageManager.getCurrent();
    var previousGroup = TestShell.PageManager.getPrevious();

    var taskWorkflow = TestShell.Navigation.createWorkflow(currentGroup, previousGroup);
    taskWorkflow.start(this._backInternal, this);
};

// moves to the previous page
TestShell.Navigation._backInternal = function()
{
    // cannot go back we are on first page
    if (TestShell.PageManager.isFirst())
    {
        Util.log('TestShell.backPage: You are currently on the first page cannot move back.');
        return;
    }

    // save current page
    TestShell.save();

    // get the group before the current one
    var currentGroup = TestShell.PageManager.getCurrent();
    var previousGroup = TestShell.PageManager.getPrevious();

    Util.log('TestShell.backPage: ' + (previousGroup ? previousGroup.id : 'none') + ' (current: ' + (currentGroup ? currentGroup.id : 'none') + ')');

    TestShell.Navigation.setState(TestShell.Navigation.State.Back);

    // hide current group
    if (currentGroup) currentGroup.hide();

    // set the selected group to be current and request it
    TestShell.PageManager.setCurrent(previousGroup);
    this.requestPage();
};

// moves to the next page
TestShell.Navigation.next = function()
{
    var currentGroup = TestShell.PageManager.getCurrent();

    // HACK: mark segment review page as confirmed when moving next
    if (currentGroup instanceof TestShell.PageReview) currentGroup.setConfirmed();

    var nextGroup = TestShell.PageManager.getNext();

    var taskWorkflow = TestShell.Navigation.createWorkflow(currentGroup, nextGroup);
    taskWorkflow.start(this._nextInternal, this);
};

// moves to the next page
TestShell.Navigation._nextInternal = function()
{
    // save current page
    TestShell.save();

    var currentGroup = TestShell.PageManager.getCurrent();

    // cannot go to next page if the current page has not been completed
    if (currentGroup && !currentGroup.isCompleted())
    {
        TestShell.UI.showWarning(ErrorCodes.get('NextUnanswered'));
        return;
    }

    // cannot go to next page if we are on the last page and it has not been completed
    if (TestShell.PageManager.isLast() && TestShell.testLengthMet)
    {
        // if we are not show item scores then show warning
        if (!TDS.showItemScores) TestShell.UI.showWarning(ErrorCodes.get('NextTestFinished'));
        return;
    }

    // get the next group (could be null if we haven't got it from the server yet)
    var nextGroup = TestShell.PageManager.getNext();

    Util.log('TestShell.nextPage: ' + (nextGroup ? nextGroup.id : 'waiting') + ' (current: ' + (currentGroup ? currentGroup.id : 'none') + ')');

    TestShell.Navigation.setState(TestShell.Navigation.State.Next);

    // hide current group
    if (currentGroup) currentGroup.hide();

    // <------ BUG: sometimes another thread executes right here if java is doing something...

    // set the selected group to be current and request it
    TestShell.PageManager.setCurrent(nextGroup);
    this.requestPage();
};

// the current page we are requesting
TestShell.Navigation._lastRequestedGroup = null;

// Try and show the current group. If the current group is NULL then it will wait for it to be loaded from XHR request.
TestShell.Navigation.requestPage = function()
{
    var group = TestShell.PageManager.getCurrent();

    // if the current group is NULL then assume we are still waiting on XHR to return it
    if (group == null)
    {
        Util.log('TestShell.UI.requestPage (no group)');

        TestShell.UI.showLoading(Messages.getAlt('TestShell.UI.WaitNextPage', 'Waiting for the next page.'));
        return;
    }

    // check if new page is being requested and write statistics
    if (TestShell.Navigation._lastRequestedGroup != group)
    {
        TestShell.Navigation._lastRequestedGroup = group;

        // write statistics
        TestShell.Audit.add(group.id, 'page-requested');
    }

    // make sure the page you are trying to show has content
    if (!group.hasContent())
    {
        Util.log('TestShell.UI.requestPage: ' + group.id + ' (no iframe)');

        TestShell.UI.showLoading(Messages.getAlt('TestShell.UI.LoadingContent', 'Loading the page content.'));
        group.requestContent();
        return;
    }

    // if we are already displaying this page then nothing left to do
    if (group.isShowing())
    {
        Util.log('TestShell.UI.requestPage: ' + group.id + ' (already viewing)');
        return;
    }

    // get page segment
    var currentSegment = group.getSegment();

    // request entry into the page segment
    if (!TestShell.SegmentManager.transition(currentSegment))
    {
        // if transition() returns false then we need to wait for segment approval
        TestShell.UI.showLoading(Messages.getAlt('TestShell.UI.WaitSegmentApproval', 'Waiting for segment approval.'));
        return;
    }

    // show group content frame 
    // NOTE: if someone returns false from beforeShow event then they can cancel this
    if (group.show() === false)
    {
        Util.log('TestShell.UI.requestPage: ' + group.id + ' (cancelled)');
        return;
    }

    Util.log('TestShell.UI.requestPage: ' + group.id + ' (viewing)');

    // if the show was successful then set the state to viewing
    TestShell.Navigation.setState(TestShell.Navigation.State.Viewing);

    // mark page as being visited
    group.setVisited();

    // update buttons
    TestShell.UI.updateControls();

    // select this groups dropdown entry
    if (group.navOption != null) group.navOption.selected = true;

    // if the load screen is showing then hide it
    TestShell.UI.hideLoading();
};

// reset whatever is currently clicked on the navigation to what the current group is
TestShell.Navigation.reset = function()
{
    var currentGroup = TestShell.PageManager.getCurrent();
    if (currentGroup != null && currentGroup.navOption != null) currentGroup.navOption.selected = true;
};

// update dropdown with anything that has changed
TestShell.Navigation.update = function()
{
    Util.Array.each(TestShell.PageManager.getPages(), function(page)
    {
        page.updateNavigationLabel();
    });
};

// goes back to the previous page you visited in history
/*TestShell.Navigation.previous = function()
{
    // get previously visited page from history
    var previousPage = TestShell.PageManager.History.pop();

    if (previousPage)
    {
        TestShell.UI.Nodes.ddlNavigation.value = previousPage.id;
        this._changeInternal();
    }
};*/