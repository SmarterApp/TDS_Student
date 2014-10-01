/*
Copyright (c) 2014, American Institutes for Research. All rights reserved.
GENERATED: 7/25/2014 2:03:39 PM
MACHINE: DC1KHANMOLT
*/

// FILE: section.js (9da9eb63) 7/22/2014 5:26:22 PM

var Sections = {};
Sections.Base = function(id)
{
Sections.Base.superclass.constructor.call(this, id);
var sectionHeader = this.getHeader();
if (sectionHeader)
{
sectionHeader.setAttribute('tabindex', -1);
}
this.hide();
};
YAHOO.lang.extend(Sections.Base, Util.Workflow.Activity);
Sections.Base.prototype.addClick = function(id, callback)
{
var target =  YUD.get(id);
if (target == null) return false;
YUE.on(target, 'click', function(evt)
{
if (target.nodeName == 'A') YUE.stopEvent(evt);
callback.call(this, evt);
}, this, true);
return true;
};
Sections.Base.prototype.getContainer = function()
{
return  YUD.get(this.getId());
};
Sections.Base.prototype.getHeader = function()
{
return  YUD.get(this.getId() + 'Header');
};
Sections.Base.prototype.getHeaderText = function()
{
var sectionHeader = this.getHeader();
return Util.Dom.getTextContent(sectionHeader);
};
Sections.Base.prototype.show = function()
{
var sectionContainer = this.getContainer();
if (!sectionContainer) return false;
YUD.setStyle(sectionContainer, 'display', 'block');
YUD.setStyle(sectionContainer, 'visibility', 'visible');
sectionContainer.setAttribute('aria-hidden', 'false');
var sectionHeader = this.getHeader();
if (sectionHeader) sectionHeader.focus();
TDS.ARIA.writeLog('Page is ready');
return true;
};
Sections.Base.prototype.hide = function()
{
var sectionContainer = this.getContainer();
if (!sectionContainer) return false;
YUD.setStyle(sectionContainer, 'display', 'none');
YUD.setStyle(sectionContainer, 'visibility', 'hidden');
sectionContainer.setAttribute('aria-hidden', 'true');
return true;
};
Sections.createWorkflow = function()
{
var wf = new Util.Workflow();
wf.Events.subscribe('onEnter', function(section)
{
section.show();
});
wf.Events.subscribe('onLeave', function(section)
{
section.hide();
});
return wf;
}

// FILE: section_xhr.js (21ac023b) 7/22/2014 5:26:22 PM

Sections.XhrManager = function(shell)
{
var timeout = (90 * 1000);
Sections.XhrManager.superclass.constructor.call(this, timeout, 1);
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
xhr.sendRequest(request);
},
function()
{
if (logout)
{
TDS.logout();
}
});
}
else
{
TDS.Dialog.showWarning(errorMessage, function() {
if (TDS.testeeCheckin != null) {
logout = true;
}
if (logout)
{
TDS.logout();
}
});
}
};
Sections.XhrManager.prototype.loginProctor = function(data, callback)
{
return this.sendAction('loginProctor', data, callback, { forceLogout: false });
};
Sections.XhrManager.prototype.loginStudent = function(data, callback)
{
return this.sendAction('loginStudent', data, callback, { forceLogout: false });
};
Sections.XhrManager.prototype.getTests = function(data, callback)
{
return this.sendAction('getTests', data, function(testSelections) {
for (var i = 0; i < testSelections.length; i++)
{
var testSelection = testSelections[i];
if (!Util.String.isNullOrEmpty(testSelection.reasonKey)) {
testSelection.reasonText = Messages.get(testSelection.reasonKey);
}
if (!Util.String.isNullOrEmpty(testSelection.warningKey)) {
testSelection.warningText = Messages.get(testSelection.warningKey);
}
}
callback(testSelections);
});
};
Sections.XhrManager.prototype.getSegmentsAccommodations = function(data, callback)
{
var fixSegments = function(segmentsAccommodations)
{
if (segmentsAccommodations)
{
for (var i = 0; i < segmentsAccommodations.length; i++)
{
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
var accommodations = Accommodations.create(segmentsAccommodations[i]);
accommodations.selectAll();
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
Sections.XhrManager.prototype.startTest = function(data, callback)
{
return this.sendAction('startTest', data, callback);
};
Sections.XhrManager.prototype.scoreTest = function(callback)
{
return this.sendAction('scoreTest', null, callback);
};
Sections.XhrManager.prototype.getDisplayScores = function(callback)
{
return this.sendAction('getDisplayScores', null, callback, { showProgress: false, showDialog: false });
};

// FILE: section_Logout.js (4e39781e) 7/22/2014 5:26:22 PM

Sections.Logout = function()
{
Sections.Logout.superclass.constructor.call(this, 'sectionLogout');
};
YAHOO.lang.extend(Sections.Logout, Sections.Base);
Sections.Logout.prototype.requestApproval = function(skipCheck)
{
if (skipCheck === true) return Util.Workflow.Approval.Approved;
var section = this;
TDS.Dialog.showPrompt(Messages.get('Global.Label.LogoutVerify'), function()
{
section.requestApproved();
});
return Util.Workflow.Approval.Pending;
};
Sections.Logout.prototype.load = function ()
{
if (LoginShell.testSelection != null)
{
var self = this;
LoginShell.api.pauseTest(function() { self.ready(); });
return true;
}
return false;
};
Sections.Logout.prototype.enter = function()
{
TDS.logout();
};

// FILE: mastershell.js (e300bcb7) 7/22/2014 5:26:22 PM

YUE.onDOMReady(function()
{
window.focus();
if (typeof (preinit) == 'function')
{
try
{
preinit();
}
catch (ex)
{
TDS.Diagnostics.report(ex);
}
}
KeyManager.init();
KeyManager.onKeyEvent.subscribe(function(obj)
{
if (obj.type == 'keydown' && obj.keyCode == 27)
{
TDS.ToolManager.hideAll();
}
});
YUE.on('btnHelp', 'click', function(evt)
{
YUE.stopEvent(evt);
});
YUE.on('btnHelp', 'mouseup', function(evt)
{
var key = 'Global.Path.Help';
var lang = TDS.getLanguage();
var id = 'tool-' + key + '-' + lang;
var panel = TDS.ToolManager.get(id);
if (panel == null)
{
var headerText = window.Messages.getAlt('StudentMaster.Label.HelpGuider', 'Help');
panel = TDS.ToolManager.createPanel(id, 'helpguide', headerText, null, key);
}
TDS.ToolManager.toggle(panel);
});
TDS.Button.init();
TDS.ARIA.createLog();
setupAccommodations();
if (TDS.isProxyLogin)
{
TDS.CLS.LogoutComponent.init();
var currentPage = (location.href).toLowerCase();
if (currentPage.indexOf('login') == -1)
{
var idleTimer = new TimeoutIdle(TDS.timeout, 30, function() { TDS.logoutProctor(false); });
idleTimer.start();
}
}
if (typeof (init) == 'function')
{
setTimeout(function()
{
try
{
init();
}
catch (ex)
{
TDS.Diagnostics.report(ex);
}
}, 0);
}
});
window.onbeforeunload = function()
{
TTS.Manager.stop();
if (TDS.isProxyLogin)
{
TDS.CLS.LogoutComponent.PageUnloadEvent.fire(arguments);
}
};
function closeWindow()
{
if (TDS.isProxyLogin)
{
TDS.redirect(TDS.CLS.logoutPage + "?exl=false", true) ;
}
else
{
if (TDS.Cache.isAvailable())
{
TDS.Cache.stop();
YAHOO.lang.later(60000, this, function()
{
Util.SecureBrowser.close();
});
}
else
{
Util.SecureBrowser.close();
}
}
}
TDS.Cache.Events.subscribe('onStop', function()
{
TDS.Dialog.showProgress();
});
TDS.Cache.Events.subscribe('onShutdown', function()
{
Util.SecureBrowser.close();
});
function setupAccommodations()
{
var testAccommodations = Accommodations.Manager.getDefault();
if (testAccommodations != null)
{
testAccommodations.applyCSS(document.body);
}
else
{
TDS.globalAccommodations.applyCSS(document.body);
}
window.globalAccDialog = new Accommodations.Dialog(TDS.globalAccommodations, 'globalAccDialog');
window.globalAccDialog.onBeforeSave.subscribe(function(accommodations)
{
accommodations.removeCSS(document.body);
});
var currentGlobalAccs = null;
var currentGlobalLang = 'ENU';
YUE.on('btnAccGlobal', 'click', function(evt)
{
TDS.ToolManager.hideAll();
currentGlobalAccs = TDS.globalAccommodations.clone();
window.globalAccDialog.show();
});
window.globalAccDialog.onSave.subscribe(function(accommodations)
{
accommodations.applyCSS(document.body);
var accProps = new Accommodations.Properties(accommodations);
var newGlobalLang = accProps.getLanguage();
if (currentGlobalLang != newGlobalLang)
{
currentGlobalLang = newGlobalLang;
TDS.Messages.Template.processLanguage();
}
var globalString = accommodations.getSelectedDelimited();
if (globalString != null)
{
}
});
window.globalAccDialog.onCancel.subscribe(function()
{
var selectedAccs = currentGlobalAccs.getSelectedJson();
Util.Array.each(selectedAccs, function(selectedAcc)
{
TDS.globalAccommodations.selectCodes(selectedAcc.type, selectedAcc.codes);
});
});
}
TDS.ToolManager.Events.subscribe('onShow', function(panel)
{
var frame = panel.getFrame();
Util.Dom.copyCSSFrame(frame);
});

// FILE: reviewshell.js (ab6124fa) 7/22/2014 5:26:22 PM

function preinit()
{
ReviewShell.init();
}
var ReviewShell =
{
api: null,
workflow: null
};
ReviewShell.getTimeoutMins = function () {
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
TDS.Diagnostics.logServerError('Idle timeout on review shell.', null, function () {
TDS.logout();
});
});
idleTimer.start();
}
};
ReviewShell.init = function()
{
this.api = new Sections.XhrManager(ReviewShell);
this.workflow = ReviewShell.createWorkflow();
ReviewShell.start();
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
wf.Events.subscribe('onRequest', function(activity) { Util.log('Section Request: ' + activity); });
wf.Events.subscribe('onReady', function(activity) { Util.log('Section Ready: ' + activity); });
wf.Events.subscribe('onLeave', function(activity) { Util.log('Section Hide: ' + activity); });
wf.Events.subscribe('onEnter', function(activity) { Util.log('Section Show: ' + activity); });
wf.addActivity(new Sections.TestReview());
wf.addActivity(new Sections.TestResults());
wf.addActivity(new Sections.Logout());
wf.addTransition('sectionTestReview', 'back', 'sectionTestShell');
wf.addTransition('sectionTestReview', 'next', 'sectionTestResults');
return wf;
};

// FILE: section_TestReview.js (78f4e3cb) 7/22/2014 5:26:22 PM

Sections.TestReview = function()
{
Sections.TestReview.superclass.constructor.call(this, 'sectionTestReview');
this.addClick('btnReviewTest', this.viewGroup);
this.addClick('btnCompleteTest', this.score);
};
YAHOO.lang.extend(Sections.TestReview, Sections.Base);
Sections.TestReview.prototype.load = function ()
{
this.setMarked(window.groups);
this.setGroups(window.groups);
};
Sections.TestReview.prototype.setMarked = function(groups)
{
var markedWarning = YUD.get('markedWarning');
var marked = false;
for (var i = 0; i < groups.length; i++)
{
var group = groups[i];
if (group.marked)
{
marked = true;
break;
}
}
if (marked)
{
YUD.setStyle(markedWarning, 'display', 'block');
}
else
{
YUD.setStyle(markedWarning, 'display', 'none');
}
};
Sections.TestReview.prototype.setGroups = function(groups)
{
var ddlNavigation = YUD.get('ddlNavigation');
ddlNavigation.options.length = 0;
for (var i = 0; i < groups.length; i++)
{
var group = groups[i];
var label = "";
var defaultAccProps = Accommodations.Manager.getDefaultProperties();
if (defaultAccProps && defaultAccProps.getNavigationDropdown() == 'TDS_NavTk')
{
label = Messages.getAlt('TDSShellObjectsJS.Label.TaskLabel', 'Task ') + group.page;
}
else
{
label = group.firstPos;
if (group.firstPos != group.lastPos) label += ' - ' + group.lastPos;
}
if (group.marked) label += ' (' + Messages.get('TDSShellObjectsJS.Label.Marked') + ')';
ddlNavigation[i] = new Option(label, group.page);
}
};
Sections.TestReview.prototype.viewGroup = function(group)
{
var ddlNavigation = YUD.get('ddlNavigation');
if (ddlNavigation.value == '')
{
var label = Messages.get('TDSShellObjectsJS.Label.PageLabel').toLowerCase();
var defaultAccProps = Accommodations.Manager.getDefaultProperties();
if (defaultAccProps && defaultAccProps.getNavigationDropdown() == 'TDS_NavTk')
{
label = Messages.get('TDSShellObjectsJS.Label.TaskLabel').toLowerCase();
}
var pageFirstMessage = Messages.get('ReviewShell.Message.PageFirst', [label]);
TDS.Dialog.showAlert(pageFirstMessage);
return;
}
TDS.redirectTestShell(ddlNavigation.value);
};
Sections.TestReview.prototype.score = function()
{
var self = this;
if (window.canCompleteTest === false)
{
var error = Messages.getAlt('ReviewShell.Message.CannotCompleteTest', 'Cannot complete the test.');
TDS.Dialog.showAlert(error);
return;
}
var message = Messages.getAlt('ReviewShell.Message.SubmitTest', 'Are you sure you want to submit the test?');
TDS.Dialog.showPrompt(message, function()
{
ReviewShell.api.scoreTest(function(summary)
{
if (summary) self.request('next', summary);
});
});
}

// FILE: section_TestResults.js (96b54191) 7/22/2014 5:26:22 PM

Sections.TestResults = function()
{
Sections.TestResults.superclass.constructor.call(this, 'sectionTestResults');
this.addClick('btnScoreLogout', this.logout);
this._pollAttempts = 5;
this._pollDelay = 60000;
this.addClick('btnEnterMoreScores', this.redirectToTestSelectionSection);
};
YAHOO.lang.extend(Sections.TestResults, Sections.Base);
Sections.TestResults.prototype.load = function (summary)
{
YUD.get('lblName').innerHTML = window.tdsTestee.lastName + ', ' + window.tdsTestee.firstName;
YUD.get('lblSSID').innerHTML = window.tdsTestee.id;
YUD.get('lblTestName').innerHTML = window.tdsTestProps.displayName;
if (summary != null) this.renderSummary(summary);
else this.loadSummary();
};
Sections.TestResults.prototype.renderSummary = function(testSummary)
{
Util.dir(testSummary);
var resultsContainer = this.getContainer();
if (testSummary.pollForScores)
{
if (this._pollAttempts > 0)
{
YUD.addClass(resultsContainer, 'scoreWaiting');
this.pollSummary();
}
else
{
YUD.addClass(resultsContainer, 'scoreTimedOut');
}
}
else
{
var hasTestScores = (testSummary.testScores && testSummary.testScores.length > 0);
var hasItemScores = (testSummary.itemScores && testSummary.itemScores.length > 0);
if (hasTestScores) this.renderTestScores(testSummary.testScores);
if (hasItemScores) this.renderItemScores(testSummary.itemScores, testSummary.viewResponses);
if (!hasTestScores && !hasItemScores)
{
YUD.addClass(resultsContainer, TDS.inPTMode ? 'scoreUnavailableInPT' : 'scoreUnavailable');
}
}
};
Sections.TestResults.prototype.renderTestScores = function(testScores)
{
var resultsContainer = this.getContainer();
YUD.removeClass(resultsContainer, 'scoreWaiting');
YUD.addClass(resultsContainer, 'scoreAvailable');
var html = [];
Util.Array.each(testScores, function(testScore)
{
var scoreHtml = '<li><span class="scoreLabel">{label}</span><span class="scoreValue">{value}</span></li>';
scoreHtml = YAHOO.lang.substitute(scoreHtml, testScore);
html.push(scoreHtml);
}, this);
var testScoresListEl = YUD.get('scoreList');
testScoresListEl.innerHTML = html.join(' ');
};
Sections.TestResults.prototype.renderItemScores = function(itemScores, viewResponses)
{
var resultsContainer = this.getContainer();
YUD.removeClass(resultsContainer, 'scoreWaiting');
YUD.addClass(resultsContainer, 'scoreAvailable');
var scoresTblEl = YUD.get('itemScores');
var scoresBodyEl = scoresTblEl.getElementsByTagName('tbody')[0];
Util.Array.each(itemScores, function(itemScore)
{
var scoreRowEl = HTML.TR();
var scorePosEl;
if (viewResponses)
{
scorePosEl = HTML.A({ href: '#' }, itemScore.position);
this.addClick(scorePosEl, function(ev)
{
TDS.redirectTestShell(itemScore.page);
});
}
else
{
scorePosEl = itemScore.position;
}
scoreRowEl.appendChild(HTML.TD(null, scorePosEl));
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
YUD.setStyle(scoresTblEl, 'display', 'block');
};
Sections.TestResults.prototype.pollSummary = function()
{
this._pollAttempts--;
YAHOO.lang.later(this._pollDelay, this, this.loadSummary);
};
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
TDS.redirect('Pages/LoginShell.xhtml?section=sectionTestSelectionProxyReenter');
});
}
else
{
this.logout();
}
};

