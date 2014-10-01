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

