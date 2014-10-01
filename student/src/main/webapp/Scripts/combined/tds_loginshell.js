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

// FILE: loginshell.js (7b0e0397) 7/22/2014 5:26:22 PM

function init()
{
LoginShell.init();
}
var LoginShell =
{
Events: new Util.EventManager(),
defaultBodyCSS: null,
api: null,
workflow: null,
session: null,
testee: null,
testeeForms: null,
testSelection: null,
testForms: null,
testApproved: false,
formSelection: null,
segmentsAccommodations: null
};
LoginShell.Settings = {};
LoginShell.init = function()
{
try
{
if (TDS.Cache.validate && Util.Browser.isSecure())
{
TDS.Cache.checkObsolete();
}
}
catch(ex) {}
if (LoginShell.checkBrowserForSpaces()) return;
if (Util.Browser.isSecure()) Util.SecureBrowser.setAppStartTime((new Date()).toUTCString(), false);
this.Events.fire('onInit');
this.defaultBodyCSS = document.body.className;
this.api = new Sections.XhrManager(LoginShell);
this.workflow = LoginShell.createWorkflow();
this.start();
};
LoginShell.start = function()
{
this.Events.fire('onStart');
var startSection = 'sectionLogin';
var querystring = Util.QueryString.parse();
if (querystring.section)
{
startSection = querystring.section;
}
this.workflow.start(startSection);
};
LoginShell.createWorkflow = function()
{
return LoginShell.createApprovedWorkflow();
};
LoginShell.createApprovedWorkflow = function()
{
var wf = Sections.createWorkflow();
wf.Events.subscribe('onRequest', function (activity) { Util.log('Section Request: ' + activity); });
wf.Events.subscribe('onReady', function (activity) { Util.log('Section Ready: ' + activity); });
wf.Events.subscribe('onLeave', function (activity) { Util.log('Section Hide: ' + activity); });
wf.Events.subscribe('onEnter', function(activity){
Util.log('Section Show: ' + activity);
window.scrollTo(0, 0);
});
wf.addActivity(new Sections.Diagnostics());
wf.addActivity(new Sections.Login());
wf.addActivity(new Sections.LoginProctor());
wf.addActivity(new Sections.LoginVerify());
wf.addActivity(new Sections.TestSelection());
wf.addActivity(new Sections.TestApproval());
wf.addActivity(new Sections.Accommodations());
wf.addActivity(new Sections.TestVerify());
wf.addActivity(new Sections.Instructions());
wf.addActivity(new Sections.SoundCheck());
wf.addActivity(new Sections.TTSCheck());
wf.addActivity(new Sections.Logout());
if (TDS.isProxyLogin)
{
wf.addActivity(new Sections.TestSelectionProxyReenter());
wf.addTransition('sectionTestSelectionProxyReenter', 'next', 'sectionTestSelection');
}
wf.addTransition('sectionDiagnostics', 'next', 'sectionLogout');
wf.addTransition('sectionDiagnostics', 'elpa', 'sectionSoundCheck');
wf.addTransition('sectionDiagnostics', 'tts', 'sectionTTSCheck');
wf.addTransition('sectionLoginProctor', 'next', 'sectionLogin');
wf.addTransition('sectionLogin', 'diag', 'sectionDiagnostics');
wf.addTransition('sectionLogin', 'next', 'sectionLoginVerify');
wf.addTransition('sectionLoginVerify', 'back', 'sectionLogout');
wf.addTransition('sectionLoginVerify', 'next', 'sectionTestSelection');
wf.addTransition('sectionTestSelection', 'back', 'sectionLogout');
wf.addTransition('sectionTestSelection', 'acc', 'sectionAccommodations');
wf.addTransition('sectionTestSelection', 'next', 'sectionTestApproval');
wf.addTransition('sectionAccommodations', 'next', 'sectionTestApproval');
wf.addTransition('sectionAccommodations', 'back', 'sectionTestSelection');
wf.addTransition('sectionTestApproval', 'logout', 'sectionLogout');
wf.addTransition('sectionTestApproval', 'next', 'sectionTestVerify');
wf.addTransition('sectionTestVerify', 'back', function()
{
if (LoginShell.session.isProctorless || TDS.isProxyLogin) return 'sectionTestSelection';
return 'sectionTestApproval';
});
wf.addTransition('sectionTestVerify', 'next', function()
{
var accProps = TDS.getAccommodationProperties();
if (accProps.hasSoundCheck())
{
return 'sectionSoundCheck';
}
if (!TDS.isDataEntry && accProps && accProps.hasTTS())
{
return 'sectionTTSCheck';
}
return 'sectionInstructions';
});
wf.addTransition('sectionInstructions', 'back', 'sectionLogout');
var canShowGlobalAccs = function()
{
if (LoginShell.testSelection != null) return false;
if (LoginShell.segmentsAccommodations != null) return false;
if (TDS.globalAccommodations == null) return false;
var accTypes = TDS.globalAccommodations.getTypes();
return (Util.Array.find(accTypes, function(accType) { return accType.isVisible(); }) != null);
};
wf.Events.subscribe('onEnter', function(section)
{
if (canShowGlobalAccs())
{
YUD.setStyle('btnAccGlobal', 'display', 'inline');
}
else
{
YUD.setStyle('btnAccGlobal', 'display', 'none');
}
});
wf.addTransition('sectionSoundCheck', 'back', function()
{
if (LoginShell.testSelection == null) return 'sectionDiagnostics';
else return 'sectionLogout';
});
wf.addTransition('sectionSoundCheck', 'next', function()
{
if (LoginShell.testSelection == null) return 'sectionDiagnostics';
else return 'sectionInstructions';
});
wf.addTransition('sectionTTSCheck', 'back', function()
{
if (LoginShell.testSelection == null) return 'sectionDiagnostics';
else return 'sectionLogout';
});
wf.addTransition('sectionTTSCheck', 'next', function()
{
if (LoginShell.testSelection == null) return 'sectionDiagnostics';
else return 'sectionInstructions';
});
return wf;
};
LoginShell.clear = function()
{
LoginShell.resetCSS(true);
LoginShell.clearLoginInfo();
LoginShell.clearTestSelection();
LoginShell.clearBrowser();
};
LoginShell.resetCSS = function(useDefaults)
{
if (LoginShell.segmentsAccommodations && LoginShell.segmentsAccommodations.length > 0)
{
LoginShell.segmentsAccommodations[0].removeCSS(document.body);
}
if (useDefaults)
{
TDS.globalAccommodations.removeCSS(document.body);
TDS.globalAccommodations.selectDefaults();
}
TDS.globalAccommodations.applyCSS(document.body);
};
LoginShell.clearLoginInfo = function()
{
this.session = null;
this.testee = null;
LoginShell.setSessionLabel('');
LoginShell.setNameLabel('');
};
LoginShell.clearTestSelection = function()
{
this.testSelection = null;
this.testForms = null;
this.testeeForms = null;
this.segmentsAccommodations = null;
};
LoginShell.clearTestAccommodations = function()
{
var langGlobal = TDS.getLanguage();
LoginShell.segmentsAccommodations = null;
var langTest = TDS.getLanguage();
if (langGlobal != langTest)
{
TDS.Messages.Template.processLanguage();
}
};
LoginShell.setLoginInfo = function(loginInfo)
{
this.session = loginInfo.session;
this.testee = loginInfo.testee;
this.testee.name = this.testee.lastName + ', ' + this.testee.firstName;
LoginShell.setSessionLabel(this.session.id);
var ssidLabel = Messages.get('Global.Label.SSID');
LoginShell.setNameLabel(this.testee.name + ' (' + ssidLabel + ': ' + this.testee.id + ')');
};
LoginShell.setSessionLabel = function(sessionID)
{
var spanSession = Util.Dom.getElementByClassName('sessionID', 'span', 'ot-topBar');
spanSession.innerHTML = sessionID;
};
LoginShell.setNameLabel = function(name)
{
var spanName = YUD.get('ot-studentInfo');
spanName.innerHTML = name;
};
LoginShell.setTestSelection = function(testSelection)
{
this.testSelection = testSelection;
};
LoginShell.setOppInfo = function(oppInfo)
{
this.testForms = oppInfo.testForms;
this.testeeForms = oppInfo.testeeForms;
};
LoginShell.setTestAccommodations = function(segmentsAccommodations)
{
var langGlobal = TDS.getLanguage();
LoginShell.segmentsAccommodations = segmentsAccommodations;
var langTest = TDS.getLanguage();
if (langGlobal != langTest)
{
TDS.Messages.Template.processLanguage();
}
};
LoginShell.clearBrowser = function()
{
Util.SecureBrowser.emptyClipBoard();
var querystring = Util.QueryString.parse();
if (querystring.logout || querystring.section == null)
{
Util.Browser.eraseCookie('TDS-Student-Auth');
Util.Browser.eraseCookie('TDS-Student-Data');
}
Util.Browser.eraseCookie('TDS-Student-Accs');
if (Util.Browser.isSecure() && !TDS.isProxyLogin)
{
var clientKey = 'TDS-Student-Client';
var clientValue = YAHOO.util.Cookie.get(clientKey);
Util.SecureBrowser.clearCookies();
if (clientValue) {
YAHOO.util.Cookie.set(clientKey, clientValue, { path: TDS.cookiePath });
}
}
try { LoginShell.saveBrowserInfo(); } catch (ex) { }
};
LoginShell.checkBrowserForSpaces = function()
{
if (Util.SecureBrowser.isSpacesEnabled())
{
TDS.redirectError('Browser.Denied.SpacesEnabled', 'LoginDenied.Header', 'Default.xhtml');
return true;
}
return false;
};
LoginShell.closeAllOtherBrowserWindows = function () {
var cnt = 0;
try {
var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"].getService(Components.interfaces.nsIWindowMediator);
var en = wm.getEnumerator("navigator:browser");
while (en.hasMoreElements()) {
var win = en.getNext();
if (win != wm.getMostRecentWindow("navigator:browser")) {
win.close();
cnt++;
}
}
} catch (e) {
}
Util.log("Closed "+ cnt + " backgrounded windows");
};
LoginShell.setMozillaPreferences = function () {
var success = Mozilla.execPrivileged(function () {
Mozilla.setPreference('accessibility.tabfocus', 7);
Mozilla.setPreference('security.OCSP.enabled', 0);
Mozilla.setPreference('mousewheel.horizscroll.withaltkey.action', 0);
Mozilla.setPreference('mousewheel.horizscroll.withaltkey.numlines', 1);
Mozilla.setPreference('mousewheel.horizscroll.withaltkey.sysnumlines', true);
Mozilla.setPreference('mousewheel.withcontrolkey.action', 0);
Mozilla.setPreference('mousewheel.withcontrolkey.numlines', 1);
Mozilla.setPreference('mousewheel.withcontrolkey.sysnumlines', false);
Mozilla.setPreference('mousewheel.withshiftkey.action', 0);
Mozilla.setPreference('mousewheel.withshiftkey.numlines', 1);
Mozilla.setPreference('mousewheel.withshiftkey.sysnumlines', false);
Mozilla.setPreference('browser.safebrowsing.enabled', false);
Mozilla.setPreference('browser.safebrowsing.malware.enabled', false);
Mozilla.setPreference('extensions.blocklist.enabled', false);
Mozilla.setPreference('extensions.update.enabled', false);
Mozilla.setPreference('browser.search.update', false);
Mozilla.setPreference('browser.microsummary.enabled', false);
Mozilla.setPreference('browser.microsummary.updateGenerators', false);
Mozilla.setPreference('network.prefetch-next', true);
Mozilla.setPreference('dom.max_script_run_time', 0);
Mozilla.setPreference('layout.spellcheckDefault', 0);
Mozilla.setPreference('browser.sessionhistory.max_entries', 0);
var offlineEnablePref = 'browser.cache.offline.enable';
if (!Mozilla.getPreference(offlineEnablePref)) Mozilla.setPreference(offlineEnablePref, true);
var offlineAllowPref = 'offline-apps.allow_by_default';
if (!Mozilla.getPreference(offlineAllowPref)) Mozilla.setPreference(offlineAllowPref, true);
if (navigator.userAgent.indexOf('OS X 10.6') != -1) {
Mozilla.setPreference('layers.acceleration.disabled', true);
}
Util.SecureBrowser.fixUserAgent();
LoginShell.javaSetup();
if (Util.Browser.isWindows() || Util.Browser.isLinux()) {
var fullscreenFix = Util.Storage.get('tds.fullscreenFix');
if (!fullscreenFix) {
Mozilla.fullscreen();
Util.Storage.set('tds.fullscreenFix', true);
}
}
if (Util.Browser.isMac() && (Util.Browser.getOSXVersion() >= 10.8) && (Util.Browser.getSecureVersion() <= 6.2)) {
var screenshotsDisabled = Mozilla.disableScreenshots();
if (screenshotsDisabled) Util.log('Screenshots are disabled.');
else Util.log('Screenshots are not disabled.');
}
Util.SecureBrowser.enablePermissiveMode(false);
LoginShell.closeAllOtherBrowserWindows();
});
if (success) Util.log('Mozilla preferences successfully set.');
else Util.log('Mozilla preferences failed to set.');
};
LoginShell.javaSetup = function() {
Util.SecureBrowser.javaSetup();
};
LoginShell.saveBrowserInfo = function()
{
BrowserInfoCookie.clear();
BrowserInfoCookie.setClientInfo('screen', screen.width + 'x' + screen.height);
var macAddress = Util.SecureBrowser.getMACAddress();
if (macAddress != null)
{
BrowserInfoCookie.setClientInfo('mac', macAddress);
}
var ipAddress = Util.SecureBrowser.getIPAddress();
if (ipAddress != null)
{
BrowserInfoCookie.setClientInfo('ip', ipAddress);
}
};
LoginShell.Events.subscribe('onInit', function()
{
if (Util.Browser.isSecure() && !Util.Browser.isMac())
{
Util.SecureBrowser.fixFocus();
}
if (Util.Browser.isSecure())
{
setTimeout(LoginShell.setMozillaPreferences, 0);
}
if (window.browserUnsupported)
{
TDS.Dialog.showWarning(Messages.get('BrowserUnsupported'));
}
});
var BrowserInfoCookie =
{
_name: 'TDS-Student-Browser',
clear: function()
{
YAHOO.util.Cookie.remove(this._name, {
path: TDS.cookiePath
});
},
setClientInfo: function(key, value)
{
YAHOO.util.Cookie.setSub(this._name, key, value, {
path: TDS.cookiePath
});
},
getClientInfo: function(key)
{
YAHOO.util.Cookie.getSub(this._name, key);
}
};

// FILE: section_Diagnostics.js (c667ab01) 7/22/2014 5:26:22 PM

Sections.Diagnostics = function()
{
Sections.Diagnostics.superclass.constructor.call(this, 'sectionDiagnostics');
this.addClick('btnDiagLogin', function()
{
this.request('next', true);
});
this.addClick('btnDiagELPA', function() {
if (this.isSpeedTestRunning())
return;
this.request('elpa');
});
this.addClick('btnDiagTTS', function() {
if (this.isSpeedTestRunning())
return;
this.request('tts');
});
this.addClick('lblDiagBrowserLabel', function() {
YUD.setStyle('diagnosticsDebug', 'display', 'block');
});
this._userInput = null;
};
YAHOO.lang.extend(Sections.Diagnostics, Sections.Base);
Sections.Diagnostics.prototype.init = function () {
var message = Messages.getAlt('Diagnostics.Label.Initializing', "Initializing...");
this.setDiagnosticsTestButtonText(message);
if (this.hasUserInputFields()) {
this.setUserInputFields();
}
this.setBrowserInfo();
this.processQueryString();
this.setFlash();
this.setDiagSpeedTestHandler(null);
};
Sections.Diagnostics.prototype.load = function ()
{
YUD.setStyle('downloadResultsLabel', 'display', 'none');
YUD.setStyle('uploadResultsLabel', 'display', 'none');
YUD.setStyle('bandwidthSummaryLabel', 'display', 'none');
document.getElementById('downloadResults').innerHTML = '';
document.getElementById('uploadResults').innerHTML = '';
document.getElementById('bandwidthSummary').innerHTML = '';
document.getElementById('bandwidthSummary').className = '';
var availableTests = YAHOO.util.Dom.get('availableTests');
availableTests.selectedIndex = 0;
YAHOO.util.Dom.get('numStudentsTesting').value = "";
};
Sections.Diagnostics.prototype.setDiagnosticsTestButtonText = function(text)
{
var buttonSpan = YAHOO.util.Dom.get('btnDiagSpeedTest');
var buttonTarget = buttonSpan.firstChild.firstChild;
if (buttonTarget != null)
buttonTarget.innerHTML = text;
};
Sections.Diagnostics.prototype.setUserInputFields = function()
{
if (!this.hasUserInputFields()) return;
YArray.each(TDS.Config.NetworkDiagnosticsTestInfo, function(subjectInfo)
{
var option = HTML.OPTION({ 'value': subjectInfo.subject }, subjectInfo.subject);
YAHOO.util.Dom.get('availableTests').appendChild(option);
});
YAHOO.util.Dom.setAttribute('diagnosticsTestInput', 'style', 'display:block');
};
Sections.Diagnostics.prototype.processQueryString = function() {
var querystring = Util.QueryString.parse();
if (querystring.section == 'sectionDiagnostics') {
YUD.setStyle('btnDiagLogin', 'display', 'none');
}
};
Sections.Diagnostics.prototype.setFlash = function()
{
var flashVer = Util.Browser.getFlashVersion();
YUD.setStyle('flashversionloading', 'display', 'none');
if (flashVer > 0)
{
document.getElementById('flashversion').innerHTML = flashVer;
}
else
{
document.getElementById('flashversion').innerHTML = Messages.getAlt('Diagnostics.Label.NoFlashFound','No Flash Plugin Detected');
}
};
Sections.Diagnostics.prototype.setDiagSpeedTestHandler = function(version) {
if (version && version.plugin >= 1.5) {
Util.Network.AppletBandWidthTester.init(this);
} else {
this.addClick('btnDiagSpeedTest', function() {
YAHOO.util.Dom.setAttribute('btnDiagSpeedTest', 'disabled', 'true');
this.setConnectionSpeed();
});
this.setDiagnosticsTestButtonText(Messages.getAlt('Diagnostics.Label.RunNetDiagTest','Run Network Diagnostics Tests'));
}
};
Sections.Diagnostics.prototype.isSpeedTestRunning = function() {
var disabledAttrib = YAHOO.util.Dom.get('btnDiagSpeedTest').getAttribute('disabled');
if (disabledAttrib != null && "true" == disabledAttrib)
return true;
return false;
};
Sections.Diagnostics.prototype.setAppletSpeedTestClickHandler = function() {
YAHOO.util.Dom.get('btnDiagSpeedTest').removeAttribute('disabled');
this.addClick('btnDiagSpeedTest', function() {
this.appletTestConnectionSpeed();
});
this.setDiagnosticsTestButtonText(Messages.getAlt('Diagnostics.Label.RunNetDiagTest', 'Run Network Diagnostics Tests'));
};
Sections.Diagnostics.prototype.appletTestConnectionSpeed = function() {
if (this.isSpeedTestRunning())
return;
YAHOO.util.Dom.setAttribute('btnDiagSpeedTest', 'disabled', 'true');
if (this.hasUserInputFields() && !this.validateUserInput())
{
YAHOO.util.Dom.get('btnDiagSpeedTest').removeAttribute('disabled');
return;
}
document.getElementById('downloadResults').innerHTML = '';
document.getElementById('uploadResults').innerHTML = '';
document.getElementById('bandwidthSummary').innerHTML = '';
document.getElementById('bandwidthSummary').className = '';
document.getElementById('downloadResults').innerHTML = 'Testing Connection Speed <span class="loadingAnimation"></span>';
var successCallback = function(downloadMeasurements, uploadMeasurements) {
var runStats = [];
var convertFrom_bps_To_KBps = function(bpsValue) {
return bpsValue / (1000 * 8);
};
runStats['GET'] = { aveLineSpeed : convertFrom_bps_To_KBps(downloadMeasurements)};
runStats['POST'] = { aveLineSpeed : convertFrom_bps_To_KBps(uploadMeasurements)};
this.NetworkDiagnosticsTestCallbackHdlr(runStats);
};
var failureCallback = function(failureCode, failureDetails) {
var runStats = [];
runStats['GET'] = { aveLineSpeed : -1};
runStats['POST'] = { maxLineSpeed : -1};
this.NetworkDiagnosticsTestCallbackHdlr(runStats);
};
var speedTestConfig = {applicationURL: TDS.baseUrl,
debugMode: true, bufferSize: 4096,
numberOfSimultaneousConnections: 2,
owner: this,
onSuccessCallbackFn: successCallback,
onFailureCallbackFn: failureCallback,
transactionId: 1                                                                    };
var speedTesterApplet = Util.Network.AppletBandWidthTester.run(speedTestConfig);
};
Sections.Diagnostics.prototype.setConnectionSpeed = function()
{
YUD.setStyle('downloadResultsLabel', 'display', 'none');
YUD.setStyle('uploadResultsLabel', 'display', 'none');
YUD.setStyle('bandwidthSummaryLabel', 'display', 'none');
document.getElementById('downloadResults').innerHTML = '';
document.getElementById('uploadResults').innerHTML = '';
document.getElementById('bandwidthSummary').innerHTML = '';
document.getElementById('bandwidthSummary').className = '';
this._userInput = null;
if (this.hasUserInputFields() && !this.validateUserInput())
{
YAHOO.util.Dom.get('btnDiagSpeedTest').removeAttribute('disabled');
return;
}
document.getElementById('downloadResults').innerHTML = 'Testing Connection Speed <span class="loadingAnimation"></span>';
var downloadFiles = [];
downloadFiles.push({ url: 'StaticResources/SpeedTest/256k.jpg', size: 262144 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/512k.jpg', size: 524288 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/1M.jpg', size: 1048576 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/2M.jpg', size: 2097152 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/4M.jpg', size: 4194304 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/8M.jpg', size: 8388608 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/16M.jpg', size: 16777216 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/32M.jpg', size: 33554432 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/64M.jpg', size: 67108864 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/128M.jpg', size: 134217728 });
downloadFiles.push({ url: 'StaticResources/SpeedTest/256M.jpg', size: 268435456 });
var callBackHndlr = this.NetworkDiagnosticsTestCallbackHdlr;
var scopeObj = this;
var fileIndex = 0;
var pingConfig = { numIterations: 1, downloadurl: TDS.resolveBaseUrl(downloadFiles[fileIndex].url), alternateGETPOST: false, callBack:
function (pingStats) {
if ((pingStats['GET'].aveTransactionTime < 4000) && (fileIndex < (downloadFiles.length - 1))) {
if (fileIndex < (downloadFiles.length - 1)) fileIndex++;
pingConfig.downloadurl = TDS.resolveBaseUrl(downloadFiles[fileIndex].url);
(new Util.Network.BandWidthTester(pingConfig)).run();
} else {
if (fileIndex < (downloadFiles.length - 1)) fileIndex++;
var bwConfig = { numIterations: 1,
requestSizeinKiloBytes: downloadFiles[fileIndex].size / 1024,
downloadurl: TDS.resolveBaseUrl(downloadFiles[fileIndex].url),
generateUploadData: true,
alternateGETPOST: true,
latencyOffsetinms: 0,
callBack: callBackHndlr,
callBackScope: scopeObj
};
(new Util.Network.BandWidthTester(bwConfig)).run();
}
}
};
(new Util.Network.BandWidthTester(pingConfig)).run();
};
Sections.Diagnostics.prototype.hasUserInputFields = function()
{
return (typeof TDS == 'object' && typeof TDS.Config == 'object' && typeof TDS.Config.NetworkDiagnosticsTestInfo == 'object' && TDS.Config.NetworkDiagnosticsTestInfo.length > 0);
};
Sections.Diagnostics.prototype.validateUserInput = function()
{
YAHOO.util.Dom.setAttribute('chooseTestError', 'style', 'display:none');
YAHOO.util.Dom.setAttribute('chooseStudentCountError', 'style', 'display:none');
this._userInput = null;
var isValid = true;
var selectedTest = YAHOO.util.Dom.get('availableTests').value;
if (selectedTest == "")
{
YAHOO.util.Dom.setAttribute('chooseTestError', 'style', 'display:block');
isValid = false;
}
var numStudents = YAHOO.util.Dom.get('numStudentsTesting').value;
if (!YAHOO.lang.isNumber(parseInt(numStudents)) || parseInt(numStudents) <= 0)
{
YAHOO.util.Dom.setAttribute('chooseStudentCountError', 'style', 'display:block');
isValid = false;
}
if (isValid)
{
var testInfo = YArray.find(TDS.Config.NetworkDiagnosticsTestInfo, function(subjectInfo)
{
return subjectInfo.subject == selectedTest;
});
this._userInput = { 'selectedTest': selectedTest, 'numStudentsToTest': parseInt(numStudents), 'testInfo' : testInfo };
}
return isValid;
};
Sections.Diagnostics.prototype.NetworkDiagnosticsTestCallbackHdlr = function(runStats)
{
var defaultDisclaimer = ' (Please note: The throughput estimates include the encryption/decryption overhead for data transfer. Throughput estimates change as the network conditions change and can vary from run to run.)';
var disclaimer = Messages.getAlt('Diagnostics.Label.Disclaimer', defaultDisclaimer);
var downloadResults = runStats['GET'];
if (downloadResults.aveLineSpeed > 0)
{
var downloadLineSpeedinMbps = ((downloadResults.aveLineSpeed * 8) / 1000).toFixed(3);
YUD.setStyle('downloadResultsLabel', 'display', 'block');
document.getElementById('downloadResults').innerHTML = downloadLineSpeedinMbps + ' Mbps download.';
}
else
{
document.getElementById('downloadResults').innerHTML = 'Error running throughput tests...';
YAHOO.util.Dom.get('btnDiagSpeedTest').removeAttribute('disabled');
return;
}
var uploadResults = runStats['POST'];
if (uploadResults.aveLineSpeed > 0)
{
var uploadLineSpeedinMbps = ((uploadResults.aveLineSpeed * 8) / 1000).toFixed(3);
YUD.setStyle('uploadResultsLabel', 'display', 'block');
document.getElementById('uploadResults').innerHTML = uploadLineSpeedinMbps + ' Mbps upload.';
}
else
{
document.getElementById('uploadResults').innerHTML = 'Error running upload tests...';
}
var downloadLineSpeedinKbps = downloadResults.aveLineSpeed * 8;
var message = '';
var messageClass = '';
if (this._userInput != null && downloadResults.aveLineSpeed > 1)
{
var testingCapacityCalculator = new Util.Network.CapacityCalculator();
var itemSize = (this._userInput.testInfo != null) ? this._userInput.testInfo.itemsize : 80;
var responseTime = (this._userInput.testInfo != null) ? this._userInput.testInfo.responsetime : 30;
var probability = testingCapacityCalculator.ProbabilityWithinCapacity(downloadLineSpeedinKbps / 1000, itemSize, this._userInput.numStudentsToTest, responseTime);
var risk = testingCapacityCalculator.GetRisk();
var recommendedCount = testingCapacityCalculator.GetRecommendedStudentCount();
message = this._getMessage(risk, recommendedCount);
if (risk == 'High' || risk == 'NearCertain')
{
messageClass = "speedBad";
}
else if (risk == 'Moderate')
{
messageClass = "speedOK";
}
else if (risk == 'VeryLow')
{
messageClass = "speedGreat";
}
YUD.setStyle('bandwidthSummaryLabel', 'display', 'block');
document.getElementById('bandwidthSummary').innerHTML = message + disclaimer;
document.getElementById('bandwidthSummary').className = messageClass;
}
YAHOO.util.Dom.get('btnDiagSpeedTest').removeAttribute('disabled');
};
Sections.Diagnostics.prototype._getMessage = function(risk, recommendedCount)
{
recommendedCount = [recommendedCount];
switch (risk)
{
case 'Unknown':
return Messages.get('Diagnostics.Label.RiskUnknown', recommendedCount);
case 'NearCertain':
return Messages.get('Diagnostics.Label.RiskNearCertain', recommendedCount);
case 'High':
return Messages.get('Diagnostics.Label.RiskHigh', recommendedCount);
case 'Moderate':
return Messages.get('Diagnostics.Label.RiskModerate', recommendedCount);
case 'VeryLow':
return Messages.get('Diagnostics.Label.RiskVeryLow', recommendedCount);
}
return "";
};
Sections.Diagnostics.prototype.showPreferences = function()
{
var preferenceDiv = YUD.getElementsByClassName('browserPreferences')[0];
var preferenceRows = [];
var addPreference = function(name)
{
var value = Mozilla.getPreference(name);
var preferenceRow = '<p>' + name + ': <strong>' + value + '</strong></p>';
preferenceRows.push(preferenceRow);
};
var privileges = Mozilla.execPrivileged(function()
{
addPreference('signed.applets.codebase_principal_support');
addPreference('accessibility.tabfocus');
addPreference('accessibility.browsewithcaret');
addPreference('network.http.max-connections');
addPreference('network.http.max-connections-per-server');
addPreference('network.http.max-persistent-connections-per-server');
addPreference('network.http.pipelining');
addPreference('network.http.pipelining.maxrequests');
addPreference('network.dns.disableIPv6');
addPreference('network.dns.CacheEntries');
addPreference('network.dns.CacheExpiration');
});
if (privileges)
{
preferenceDiv.innerHTML = '<span class="nugget">' + preferenceRows.join('') + '</span>';
}
else
{
preferenceDiv.innerHTML = '<p><span class="nugget">Error getting preferences: <strong>' + Mozilla._lastException + '</strong></span></p>';
}
preferenceDiv.style.display = 'block';
};
Sections.Diagnostics.prototype.setBrowserInfo = function()
{
if (TDS.BrowserInfo == null) return;
YUD.get('lblDiagOSLabel').innerHTML = TDS.BrowserInfo.osLabel;
YUD.get('lblDiagBrowserLabel').innerHTML = TDS.BrowserInfo.label;
YUD.get('lblDiagBrowserSecure').innerHTML = TDS.BrowserInfo.isSecure;
YUD.get('lblDiagUAEl').innerHTML = TDS.BrowserInfo.userAgent;
YUD.get('lblDiagOSName').innerHTML = TDS.BrowserInfo.osName;
YUD.get('lblDiagOSVer').innerHTML = TDS.BrowserInfo.osVersion;
YUD.get('lblDiagArch').innerHTML = TDS.BrowserInfo.architecture;
YUD.get('lblDiagBrowserName').innerHTML = TDS.BrowserInfo.name;
YUD.get('lblDiagBrowserVer').innerHTML = TDS.BrowserInfo.version;
};

// FILE: section_Login.js (2097f873) 7/22/2014 5:26:22 PM

Sections.Login = function()
{
Sections.Login.superclass.constructor.call(this, 'sectionLogin');
this.Controls =
{
txtLoginSessionID: YUD.get('loginSessionID'),
cbUser: YUD.get('cbUser'),
cbSession: YUD.get('cbSession'),
btnLogin: YUD.get('btnLogin')
};
this.addClick(this.Controls.cbUser, function()
{
this.disableUserInput(this.Controls.cbUser.checked);
});
this.addClick(this.Controls.cbSession, function()
{
this.disableSessionInput(this.Controls.cbSession.checked);
});
this.addClick('btnDiagnostic', function()
{
this.request('diag');
});
};
YAHOO.lang.extend(Sections.Login, Sections.Base);
Sections.Login._loginInputPrefix = 'loginRow_';
Sections.Login._loginErrorPrefix = 'loginErr_';
Sections.Login.prototype.getLoginInput = function(id)
{
return YUD.get(Sections.Login._loginInputPrefix + id);
};
Sections.Login.prototype.setLoginInput = function(id, value)
{
var input = YUD.get(Sections.Login._loginInputPrefix + id);
if (input) input.value = value;
};
Sections.Login.prototype.getSessionID = function() { return this.Controls.txtLoginSessionID.value; };
Sections.Login.prototype.setSessionID = function(value) { this.Controls.txtLoginSessionID.value = value; };
Sections.Login.prototype.load = function ()
{
var self = this;
TDS.globalAccommodations.selectDefaults();
LoginShell.clear();
if (Util.Browser.isSecure() && Util.Browser.isChrome()) {
Util.SecureBrowser.enableLockDown(false);
}
var loginForm = YUD.get('loginForm');
loginForm.onsubmit = function()
{
self.validate();
return false;
};
this.render();
this.setControls();
this.checkForRedirect(loginForm);
};
Sections.Login.prototype.render = function() {
this.setBrowserInfo();
var loginContainer = YUD.get('loginContainer');
if (loginContainer.innerHTML != '') loginContainer.innerHTML = '';
Util.Array.each(TDS.Config.loginRequirements, this.renderRequirement, this);
};
Sections.Login.prototype.renderRequirement = function(loginReq)
{
var loginContainer = YUD.get('loginContainer');
var idReq = Sections.Login._loginInputPrefix + loginReq.id;
var idErr = Sections.Login._loginErrorPrefix + loginReq.id;
var i18n = 'User.Label.' + loginReq.id;
var loginRow = HTML.DIV({ 'className': 'loginRow' });
loginContainer.appendChild(loginRow);
var loginReqLabel = HTML.LABEL({ 'for': idReq });
if (Messages.get(i18n) != i18n)
{
Messages.setHTMLContent(loginReqLabel, 'User.Label.' + loginReq.id);
}
else
{
Util.Dom.setTextContent(loginReqLabel, loginReq.label + ':');
}
loginRow.appendChild(loginReqLabel);
var loginReqInput = HTML.INPUT(
{
'type': 'text',
'id': idReq,
'name': idReq,
'size': 26,
'maxlength': 100,
'tabindex': 0
});
loginRow.appendChild(loginReqInput);
var loginReqError = HTML.SPAN({ 'id': idErr, 'className': 'validation' });
loginReqError.innerHTML = '&nbsp;';
loginRow.appendChild(loginReqError);
};
Sections.Login.prototype.setControls = function()
{
if (TDS.inPTMode)
{
this.disableUserInput(true);
this.disableSessionInput(true);
this.Controls.cbUser.checked = true;
this.Controls.cbSession.checked = true;
}
else
{
this.disableUserInput(false);
this.disableSessionInput(false);
this.Controls.cbUser.checked = false;
this.Controls.cbSession.checked = false;
}
if (TDS.isDataEntry || TDS.isReadOnly)
{
YUD.setStyle('loginForm2', 'display', 'none');
}
};
Sections.Login.prototype.disableUserInput = function(disabled)
{
this.Controls.cbUser.checked = disabled;
Util.Array.each(TDS.Config.loginRequirements, function(loginReq)
{
var input = this.getLoginInput(loginReq.id);
if (disabled)
{
input.disabled = true;
input.value = 'GUEST';
}
else
{
input.disabled = false;
input.value = '';
}
}, this);
};
Sections.Login.prototype.disableSessionInput = function(disabled)
{
this.Controls.cbSession.checked = disabled;
this.Controls.txtLoginSessionID.disabled = disabled;
if (disabled)
{
this.setSessionID('GUEST Session');
}
else
{
this.setSessionID('');
}
};
Sections.Login.prototype.validate = function ()
{
var self = this;
if (Util.Browser.isSecure()) {
if (!Util.SecureBrowser.isEnvironmentSecure()) {
var defaultError = 'Environment is not secure. Please notify your proctor';
if (Util.Browser.isIOS()) {
defaultError = 'Guided Access is not turned on. Please notify your proctor. (Before turning on Guided Access, check the volume on your iPad to make sure you can hear the audio.)';
TDS.Dialog.showWarning(Messages.getAlt('LoginShell.Alert.EnvironmentInsecureiOSVolumeControl', defaultError));
} else {
TDS.Dialog.showWarning(Messages.getAlt('LoginShell.Alert.EnvironmentInsecure', defaultError));
}
return;
} else if (Util.Browser.isIOS()) {
var defaultVolumeWarning = 'Warning: You cannot adjust the volume of your iPad during the test. If you need to adjust the volume, please turn off Guided Access. Adjust the volume using the volume control buttons on the iPad, and then activate Guided Access.  If you need help, please ask your proctor.';
TDS.Dialog.showWarning(Messages.getAlt('LoginShell.Alert.EnvironmentSecureiOSVolumeControl', defaultVolumeWarning));
}
}
var forbiddenApps = Util.SecureBrowser.getForbiddenApps();
var forbiddenAppsFlat = '';
for (var i = 0; i < forbiddenApps.length; i++)
{
if (forbiddenAppsFlat.length > 0) forbiddenAppsFlat += '|';
forbiddenAppsFlat += forbiddenApps[i].desc;
}
if (Util.Browser.isSecure()) {
var processList = Util.SecureBrowser.getProcessList(true);
var browserInstances = [];
Util.Array.each(processList, function (processName) {
if (processName.toLowerCase().indexOf('securebrowser') > -1) {
browserInstances.push(processName);
}
});
if (browserInstances.length > 1) {
TDS.Dialog.showWarning(Messages.getAlt('LoginShell.Alert.MultipleBrowserInstances', 'More than 1 secure browser instance detected. Please close all instances and try again'));
return;
}
}
var keyValues = [];
Util.Array.each(TDS.Config.loginRequirements, function (loginReq) {
var input = this.getLoginInput(loginReq.id);
var value = YAHOO.lang.trim(input.value);
var keyValue = loginReq.id + ':' + value;
keyValues.push(keyValue);
}, this);
var loginRequest =
{
keyValues: keyValues.join(';'),
sessionID: this.getSessionID(),
forbiddenApps: forbiddenAppsFlat
};
LoginShell.api.loginStudent(loginRequest, function (loginInfo) {
if (loginInfo) {
LoginShell.setLoginInfo(loginInfo);
self.request('next', loginInfo);
if (Util.Browser.isSecure() && Util.Browser.isChrome()) {
Util.SecureBrowser.enableLockDown(true);
}
}
});
};
function loginForm()
{
LoginShell.workflow.getActivity('sectionLogin').validate();
return false;
}
Sections.Login.prototype.checkForRedirect = function (loginForm) {
var oname = 'globalRedirectSettings';
var appString = "accommodationStringP";
var self = this;
if (typeof (window[oname]) == "object") {
Util.Array.each(TDS.Config.loginRequirements, function (loginReq) {
self.setLoginInput(loginReq.id, window[oname][Sections.Login._loginInputPrefix + loginReq.id]);
});
this.setSessionID(window[oname]['loginSessionID']);
var astr = decodeURIComponent(window[oname][appString]);
astr = YAHOO.lang.JSON.parse(astr);
TDS.globalAccommodations.importJson(astr);
TDS.globalAccommodations.applyCSS(document.body);
TDS.Messages.Template.processLanguage();
loginForm.onsubmit();
}
};
Sections.Login.prototype.setBrowserInfo = function()
{
if (TDS.BrowserInfo == null) return;
var lblVerEl = document.getElementById('lblLoginBrowserVer');
lblVerEl.innerHTML = TDS.BrowserInfo.label;
};

// FILE: section_LoginProctor.js (a38a0943) 7/22/2014 5:26:22 PM

Sections.LoginProctor = function()
{
Sections.LoginProctor.superclass.constructor.call(this, 'sectionLoginProctor');
};
YAHOO.lang.extend(Sections.LoginProctor, Sections.Base);
Sections.LoginProctor.prototype.load = function ()
{
var self = this;
var loginForm = YUD.get('loginProctorForm');
loginForm.onsubmit = function()
{
self.validate();
return false;
};
};
Sections.LoginProctor.prototype.validate = function()
{
var self = this;
var loginRequest =
{
userName: YUD.get('txtProctorUserName').value,
password: YUD.get('txtProctorPassword').value
};
LoginShell.api.loginProctor(loginRequest, function(loginResponse)
{
if (loginResponse)
{
self.request('next', loginResponse);
}
});
};

// FILE: section_LoginVerify.js (86669f82) 7/22/2014 5:26:22 PM

Sections.LoginVerify = function()
{
Sections.LoginVerify.superclass.constructor.call(this, 'sectionLoginVerify');
this.Controls =
{
btnVerifyDeny: YUD.get('btnVerifyDeny'),
btnVerifyApprove: YUD.get('btnVerifyApprove'),
verifyName: YUD.get('verifyName'),
verifyGrade: YUD.get('verifyGrade'),
verifyBirthday: YUD.get('verifyBirthday'),
verifySchool: YUD.get('verifySchool'),
verifySSID: YUD.get('verifySSID'),
ddlGrades: YUD.get('verifyGradeSelector').getElementsByTagName('select')[0]
};
this.addClick(this.Controls.btnVerifyDeny, function()
{
this.request('back');
});
this.addClick(this.Controls.btnVerifyApprove, this.approve);
};
YAHOO.lang.extend(Sections.LoginVerify, Sections.Base);
Sections.LoginVerify.prototype.load = function (loginInfo)
{
var session = loginInfo.session, testee = loginInfo.testee;
this.reset();
this.Controls.verifyName.innerHTML = testee.name;
this.Controls.verifyGrade.innerHTML = testee.grade;
this.Controls.verifyBirthday.innerHTML = testee.birthday;
this.Controls.verifySchool.innerHTML = testee.schoolName;
this.Controls.verifySSID.innerHTML = testee.id;
if (testee.isReal)
{
YUD.setStyle('verifyGradeViewer', 'display', 'block');
YUD.setStyle('verifyGradeSelector', 'display', 'none');
}
else
{
YUD.setStyle('verifyGradeViewer', 'display', 'none');
YUD.setStyle('verifyGradeSelector', 'display', 'block');
}
this.setGrades(window.grades);
};
Sections.LoginVerify.prototype.reset = function()
{
this.Controls.verifyName.innerHTML = '';
this.Controls.verifyGrade.innerHTML = '';
this.Controls.verifyBirthday.innerHTML = '';
this.Controls.verifySchool.innerHTML = '';
this.Controls.verifySSID.innerHTML = '';
this.Controls.ddlGrades.selectedIndex = 0;
};
Sections.LoginVerify.prototype.setGrades = function(grades)
{
var label = Messages.get('User.List.Identified.SelectGradeList');
this.Controls.ddlGrades[0] = new Option(label, '');
for (var i = 0; i < TDS.Config.grades.length; i++)
{
var grade = TDS.Config.grades[i];
this.Controls.ddlGrades[i + 1] = new Option(grade, grade);
}
};
Sections.LoginVerify.prototype.getGrade = function()
{
return this.Controls.ddlGrades.value;
};
Sections.LoginVerify.prototype.approve = function()
{
var grade = null;
if (LoginShell.testee.isReal)
{
grade = LoginShell.testee.grade;
}
else
{
grade = this.getGrade();
if (grade == null || grade.length == 0)
{
var label = Messages.get('User.List.Identified.MustSelectGrade');
label = label.replace('<-- ', '');
TDS.Dialog.showWarning(label);
return;
}
}
this.request('next', grade);
};

// FILE: section_TestSelection.js (5c8cac32) 7/22/2014 5:26:22 PM

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
Sections.TestSelection.prototype.load = function(grade) {
LoginShell.testSelection = null;
if (grade == null && this.testSelections != null) {
return false;
}
var self = this;
LoginShell.api.getTests({ grade: grade }, function(testSelections) {
if (testSelections) {
self.testSelections = testSelections;
self.ready(testSelections);
}
});
return true;
};
Sections.TestSelection.prototype.enter = function() {
Util.dir(this.testSelections);
var testSelectionsEl = YUD.get('testSelections');
if (testSelectionsEl.innerHTML.length > 0) {
testSelectionsEl.innerHTML = '';
}
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
TDS.Messages.Template.processLanguage(testSelectionsEl);
};
Sections.TestSelection.prototype._createButton = function(testSelection, idx) {
var testName = testSelection.displayName;
if (TDS.isDataEntry) {
testName += ' (' + testSelection.mode + ')';
}
var testActive = false;
var testClass = '';
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
return null;
}
testOpp = Messages.get('Login.TS.DescActive', [testSelection.opportunity, testSelection.maxOpportunities]);
var testButtonEl = HTML.DIV();
YUD.addClass(testButtonEl, 'testSelection');
YUD.addClass(testButtonEl, testClass);
YUD.addClass(testButtonEl, (idx % 2 == 0) ? 'even' : 'odd');
YUD.setAttribute(testButtonEl, 'role', 'button');
YUD.setAttribute(testButtonEl, 'tabindex', 0);
var testHeaderEl = HTML.H3();
testHeaderEl.innerHTML = testHeader;
testButtonEl.appendChild(testHeaderEl);
var testDescEl = HTML.P();
testDescEl.innerHTML = testDesc;
if (testActive) {
testDescEl.appendChild(HTML.SPAN(null, testOpp));
}
testButtonEl.appendChild(testDescEl);
if (testActive) {
this.addClick(testButtonEl, function(evt) {
this.select(testSelection);
});
YUE.on(testButtonEl, 'keypress', function(ev) {
var charCode = YUE.getCharCode(ev);
if (charCode == 13) {
this.select(testSelection);
}
}, this, true);
}
return testButtonEl;
};
Sections.TestSelection.prototype.select = function(testSelection, skipWarning) {
var self = this;
if (!skipWarning && !Util.String.isNullOrEmpty(testSelection.warningText)) {
TDS.Dialog.showPrompt(testSelection.warningText, function() {
self.select(testSelection, true);
});
return false;
}
LoginShell.setTestSelection(testSelection);
TDS.Dialog.showProgress();
if (testSelection.requirements.flashVersion > 0) {
if (Util.Browser.getFlashVersion() < testSelection.requirements.flashVersion) {
var message = ErrorCodes.get('Opportunity.Javascript.NoFlash', [testSelection.requirements.flashVersion]);
TDS.Dialog.showWarning(message);
return false;
}
}
self.open(testSelection);
return true;
};
Sections.TestSelection.prototype.open = function(testSelection) {
var self = this;
var request = {
testKey: testSelection.key,
testID: testSelection.id,
oppKey: testSelection.oppKey,
subject: testSelection.subject,
grade: testSelection.grade
};
if (LoginShell.session.isProctorless && testSelection.status == 2) {
LoginShell.api.getSegmentsAccommodations(request, function(accommodations) {
if (accommodations) {
self.request('acc', accommodations);
}
});
} else {
LoginShell.api.openTest(request, function(oppInfo) {
if (oppInfo) {
LoginShell.setOppInfo(oppInfo);
self.request('next');
}
});
}
};

// FILE: section_TestApproval.js (757c938f) 7/22/2014 5:26:22 PM

Sections.TestApproval = function()
{
Sections.TestApproval.superclass.constructor.call(this, 'sectionTestApproval');
this._timer = null;
this.Controls =
{
btnCancelApproval: YUD.get('btnCancelApproval')
};
this.addClick(this.Controls.btnCancelApproval, this.cancel);
};
YAHOO.lang.extend(Sections.TestApproval, Sections.Base);
Sections.TestApproval.Status =
{
waiting: 0,
approved: 1,
denied: 2,
logout: 3
};
Sections.TestApproval.prototype.setMessage = function(header, message)
{
YUD.get('lblApprovalHeader').innerHTML = header;
YUD.get('lblApprovalMessage').innerHTML = message;
};
Sections.TestApproval.prototype.cancel = function()
{
LoginShell.api.abort('checkApproval');
if (this._timer)
{
this._timer.cancel();
this._timer = null;
}
this.request('logout', true);
};
Sections.TestApproval.prototype.load = function ()
{
};
Sections.TestApproval.prototype.enter = function()
{
var self = this;
var pollDuration = 5000;
var pollForApproval = function(duration)
{
self.timer = YAHOO.lang.later(duration, this, checkForApproval);
};
var checkForApproval = function()
{
LoginShell.api.checkApproval(null, function(approval)
{
if (approval)
{
switch (approval.status)
{
case Sections.TestApproval.Status.approved: self.approved(approval); return;
case Sections.TestApproval.Status.denied: self.denied(approval); return;
case Sections.TestApproval.Status.logout: self.logout(); return;
}
pollForApproval(pollDuration);
}
});
};
pollForApproval(1);
};
Sections.TestApproval.prototype.approved = function(approval)
{
LoginShell.setTestAccommodations(approval.segmentsAccommodations);
this.request('next', approval);
};
Sections.TestApproval.prototype.denied = function(approval)
{
var self = this;
var deniedMessage = Messages.get('Approval.Label.TADenied');
if (approval.comment != null && approval.comment.length > 0) {
deniedMessage += ": " + approval.comment;
}
TDS.Dialog.showAlert(deniedMessage, function() {
self.logout();
});
};
Sections.TestApproval.prototype.logout = function()
{
this.request('logout', true);
};

// FILE: section_Accommodations.js (d354ce8c) 7/22/2014 5:26:22 PM

Sections.Accommodations = function()
{
Sections.Accommodations.superclass.constructor.call(this, 'sectionAccommodations');
this.addClick('btnAccBack', function()
{
this.request('back');
});
this.addClick('btnAccSelect', this.submit);
this._segmentsAccommodations = null;
this._rendererCollection = null;
};
YAHOO.lang.extend(Sections.Accommodations, Sections.Base);
Sections.Accommodations.prototype.init = function()
{
var querystring = Util.QueryString.parse();
if (querystring.showInvisibleAccs) Accommodations.Renderer.hideInvisible = false;
if (querystring.showUnselectableAccs) Accommodations.Renderer.hideUnselectable = false;
};
Sections.Accommodations.prototype.load = function (segmentsAccommodations)
{
this._segmentsAccommodations = segmentsAccommodations;
this._rendererCollection = [];
var segmentsContainer = YUD.get('segments');
segmentsContainer.innerHTML = '';
Util.Array.each(segmentsAccommodations, function(segmentAccommodations)
{
if (!segmentAccommodations.isAnyVisible()) return;
var segmentContainer = HTML.DIV({ id: 'segment-' + segmentAccommodations.getId(), 'className': 'segment' });
segmentsContainer.appendChild(segmentContainer);
var segmentHeader = HTML.H3(null, segmentAccommodations.getLabel());
segmentContainer.appendChild(segmentHeader);
var renderer = new Accommodations.Renderer(segmentAccommodations, segmentContainer);
renderer.bind();
renderer.render();
this._rendererCollection.push(renderer);
}, this);
};
Sections.Accommodations.prototype.submit = function()
{
var self = this;
var formValues = [];
formValues.push('testKey=' + LoginShell.testSelection.key);
formValues.push('testID=' + LoginShell.testSelection.id);
formValues.push('subject=' + LoginShell.testSelection.subject);
formValues.push('grade=' + LoginShell.testSelection.grade);
Util.Array.each(this._segmentsAccommodations, function(segmentAccommodations)
{
var segmentPos = segmentAccommodations.getPosition();
var codes = segmentAccommodations.getSelectedDelimited(true, ',');
formValues.push('segment=' + segmentPos + '#' + codes);
});
Util.dir(formValues);
LoginShell.api.openTest(formValues.join('&'), function(oppInfo)
{
if (oppInfo)
{
LoginShell.setOppInfo(oppInfo);
self.request('next');
}
});
};

// FILE: section_TestVerify.js (eb5d0989) 7/22/2014 5:26:22 PM

Sections.TestVerify = function()
{
Sections.TestVerify.superclass.constructor.call(this, 'sectionTestVerify');
this.addClick('btnApproveAccommodations', this.approve);
this.addClick('btnWrongAccommodations', this.deny);
};
YAHOO.lang.extend(Sections.TestVerify, Sections.Base);
Sections.TestVerify.prototype.load = function(approval)
{
var segmentsAccommodations = approval.segmentsAccommodations;
var segmentsContainer = YUD.get('summary-segments');
segmentsContainer.innerHTML = '';
YUD.get('lblVerifySessionID').innerHTML = LoginShell.session.id;
this.renderForms(LoginShell.testForms);
TDS.globalAccommodations.removeCSS(document.body);
segmentsAccommodations[0].applyCSS(document.body);
Util.Array.each(segmentsAccommodations, this.renderAccommodations, this);
};
Sections.TestVerify.prototype.renderForms = function(testForms)
{
var verifyForm = YUD.get('verifyTestForm');
if (verifyForm == null) return;
var verifyFormSelector = verifyForm.getElementsByTagName('select')[0];
if (verifyFormSelector == null) return;
verifyFormSelector.options.length = 0;
if (testForms == null || testForms.length == 0)
{
YUD.setStyle(verifyForm, 'display', 'none');
return;
}
verifyFormSelector[0] = new Option('Select a form', '');
for (var i = 0; i < testForms.length; i++)
{
var testForm = testForms[i];
verifyFormSelector[i + 1] = new Option(testForm.id, testForm.key);
}
YUD.setStyle(verifyForm, 'display', 'block');
};
Sections.TestVerify.prototype.renderAccommodations = function(segmentAccommodations)
{
if (!segmentAccommodations.isAnyVisible()) return;
var segmentsContainer = YUD.get('summary-segments');
var segmentContainer = HTML.DIV({ 'className': 'summary-segment' });
segmentsContainer.appendChild(segmentContainer);
var segmentHeader = HTML.H3(null, segmentAccommodations.getLabel());
segmentContainer.appendChild(segmentHeader);
var accTypes = segmentAccommodations.getTypes();
Util.Array.each(accTypes, function(accType)
{
if (accType.isVisible())
{
this.renderAccType(segmentContainer, accType);
}
}, this);
};
Sections.TestVerify.prototype.renderAccType = function(segmentContainer, accType)
{
var typeLabel = accType.getLabel() + ':';
var valuesLabel = this.getTypeValuesLabel(accType);
var spanTypeLabel = HTML.SPAN({ 'class': 'summary-segment-type-label' }, typeLabel);
var spanValuesLabel = HTML.SPAN({ 'class': 'summary-segment-type-values' }, valuesLabel);
var row = HTML.DIV({ 'className': 'summary-segment-type' }, spanTypeLabel, spanValuesLabel);
segmentContainer.appendChild(row);
};
Sections.TestVerify.prototype.getTypeValuesLabel = function(accType)
{
var valueNames = [];
var accValues = accType.getValues();
for (var i = 0; i < accValues.length; i++)
{
var accValue = accValues[i];
valueNames.push(accValue.getLabel());
}
return valueNames.join(', ');
};
Sections.TestVerify.prototype.getSelectedTestForm = function()
{
var ddlTestForms = YUD.get('ddlTestForms');
return (ddlTestForms.options.length > 0) ? ddlTestForms.value : null;
};
Sections.TestVerify.prototype.approve = function()
{
var selectedFormKey = this.getSelectedTestForm();
if (YAHOO.lang.isString(selectedFormKey))
{
if (selectedFormKey == '')
{
var defaultError = 'Must select a test form';
TDS.Dialog.showWarning(Messages.getAlt('LoginShell.Alert.MustSelectForm', defaultError));
return;
}
var formKeys = LoginShell.testeeForms;
if (YAHOO.lang.isArray(formKeys) && (formKeys.length > 0) && formKeys.indexOf(selectedFormKey) == -1)
{
var defaultError = 'The selected test form does not match what is allowed for the student';
TDS.Dialog.showWarning(Messages.getAlt('LoginShell.Alert.FormSelectionInvalid', defaultError));
return;
}
LoginShell.formSelection = selectedFormKey;
}
this.applyAccommodations();
this.request('next');
};
Sections.TestVerify.prototype.applyAccommodations = function() {
var accProps = TDS.getAccommodationProperties();
if (accProps && accProps.isPermissiveModeEnabled()) {
Util.SecureBrowser.enablePermissiveMode(true);
}
};
Sections.TestVerify.prototype.deny = function()
{
LoginShell.resetCSS();
LoginShell.clearTestAccommodations();
var self = this;
LoginShell.api.denyApproval(function()
{
self.request('back');
});
};

// FILE: section_Instructions.js (d85b3a98) 7/22/2014 5:26:22 PM

Sections.Instructions = function()
{
Sections.Instructions.superclass.constructor.call(this, 'sectionInstructions');
this.addClick('btnCancelTest', function()
{
this.request('back');
});
this.addClick('btnStartTest', this.start);
};
YAHOO.lang.extend(Sections.Instructions, Sections.Base);
Sections.Instructions.prototype.init = function ()
{
var container = YUD.get('quickQuide');
var frame = YUD.get('helpFrame');
var url = TDS.baseUrl + 'Pages/';
url += Messages.get('Global.Path.Help');
TDS.ToolManager.loadFrameUrl(container, frame, url, function(frameDoc, allowAccess)
{
YUD.addClass(frameDoc.body, 'embedded');
onHelpLoad(frame, 'startSpeakingButton', 'stopSpeakingButton', 'ttsHelpMessage', 'noTTSHelpMessage');
});
};
Sections.Instructions.prototype.start = function()
{
var startData =
{
formKey: LoginShell.formSelection
};
LoginShell.api.startTest(startData, function(testConfig)
{
if (testConfig) TDS.redirectTestShell();
});
};

// FILE: section_SoundCheck.js (45aaa52f) 7/22/2014 5:26:22 PM

Sections.SoundCheck = function()
{
Sections.SoundCheck.superclass.constructor.call(this, 'sectionSoundCheck');
};
YAHOO.lang.extend(Sections.SoundCheck, Sections.Base);
Sections.SoundCheck.prototype.init = function () {
var Audio = TDS.Audio;
var Player = TDS.Audio.Player;
var Recorder = TDS.Audio.Recorder;
var soundCheck = this;
var standalone = true;
var cssDisabled = 'disabled';
var accProps = TDS.getAccommodationProperties();
var hasAudio = accProps.hasSoundPlayCheck();
var hasRecorder = accProps.hasRecorderCheck();
var audioPlayer = null;
var audioRecorder = null;
var CheckState =
{
Audio: 0,
Recorder: 1,
Error: -1
};
var checkState;
function gotoInstructions()
{
soundCheck.request('next');
}
function gotoLogin()
{
soundCheck.request('back');
}
function showSoundCheck()
{
checkState = CheckState.Audio;
YUD.removeClass('audioPlayer', 'playing_fail');
YUD.setStyle('checkSound', 'display', '');
YUD.setStyle('checkRecorder', 'display', 'none');
YUD.setStyle('checkError', 'display', 'none');
}
function showRecorderCheck()
{
checkState = CheckState.Recorder;
if (audioRecorder && YUD.hasClass(audioRecorder, 'recording_fail'))
{
audioRecorder.classNameEvent = 'recording_ready';
audioRecorder.className = 'elicitedwrap recording_ready';
}
YUD.setStyle('checkSound', 'display', 'none');
YUD.setStyle('checkRecorder', 'display', '');
YUD.setStyle('checkError', 'display', 'none');
}
function showError()
{
checkState = CheckState.Error;
YUD.setStyle('checkSound', 'display', 'none');
YUD.setStyle('checkRecorder', 'display', 'none');
YUD.setStyle('checkError', 'display', '');
}
function soundYes()
{
if (YUD.hasClass('btnSoundYes', cssDisabled)) return;
if (hasRecorder)
{
showRecorderCheck();
}
else
{
gotoInstructions();
}
}
function soundNo()
{
Audio.stopAll();
var soundRetry = function()
{
YUE.removeListener('btnErrorRetry', 'click', soundRetry);
showSoundCheck();
};
YUE.addListener('btnErrorRetry', 'click', soundRetry);
showError();
}
function recorderYes()
{
if (YUD.hasClass('btnRecorderYes', cssDisabled)) return;
gotoInstructions();
}
function recorderProblem()
{
Audio.stopAll();
var recorderRetry = function()
{
YUE.removeListener('btnErrorRetry', 'click', recorderRetry);
showRecorderCheck();
};
YUE.addListener('btnErrorRetry', 'click', recorderRetry);
showError();
}
TDS.Audio.Player.setup();
TDS.Audio.Recorder.initialize();
audioPlayer = YUD.get('audioPlayer');
if( !Util.Browser.supportsAudioOGG() ) {
var str = YUD.getAttribute(audioPlayer, 'href');
str = str.replace('.ogg', '.m4a');
YUD.setAttribute(audioPlayer, 'href', str);
str = YUD.getAttribute(audioPlayer, 'type');
str = str.replace('ogg', 'm4a');
YUD.setAttribute(audioPlayer, 'type', str);
}
TDS.Audio.Widget.createPlayer(audioPlayer);
audioRecorder = YUD.get('audioRecorder');
TDS.Audio.Widget.createRecorder(audioRecorder);
YUE.addListener('btnSoundYes', 'click', soundYes);
YUE.addListener('btnSoundNo', 'click', soundNo);
YUE.addListener('btnRecorderYes', 'click', recorderYes);
YUE.addListener('btnRecorderProblem', 'click', recorderProblem);
YUD.batch(YUD.getElementsByClassName('soundCheckLogout', 'span'), function(logoutEl) {
YUE.on(logoutEl, 'click', gotoLogin);
});
Player.onIdle.subscribe(function() {
YUD.removeClass('btnSoundYes', cssDisabled);
});
Recorder.onPlayStop.subscribe(function() {
YUD.removeClass('btnRecorderYes', cssDisabled);
});
this.load = function ()
{
YUD.setStyle('flashError', 'display', 'none');
YUD.setStyle('javaError', 'display', 'none');
YUD.setStyle('checkSound', 'display', 'none');
YUD.setStyle('checkRecorder', 'display', 'none');
if (hasRecorder && TDS.SecureBrowser.getRecorder() == null) {
YUD.setStyle('soundCheckRecorderError', 'display', 'block');
return;
}
if (hasAudio)
{
showSoundCheck();
}
else if (hasRecorder)
{
showRecorderCheck();
}
};
};

// FILE: section_TTSCheck.js (bc35e089) 7/22/2014 5:26:22 PM

Sections.TTSCheck = function(){
Sections.TTSCheck.superclass.constructor.call(this, 'sectionTTSCheck');
};
YAHOO.lang.extend(Sections.TTSCheck, Sections.Base);
Sections.TTSCheck.prototype.init = function(){
YAHOO.util.Event.onDOMReady(function(){
this.ui  = new TTS.Config.UI();
this.ui.next = this.request.bind(this, 'next');
this.ui.back = this.request.bind(this, 'back');
TTS.getInstance().runOnInit(function(){
TTS.Config.User.reset();
this.ui.setCfg(this.getSettings());
this.ui.init();
}.bind(this));
}.bind(this));
};
Sections.TTSCheck.prototype.load = function(){
};
Sections.TTSCheck.prototype.hasLang = function(lang){
var properties = TDS.getAccommodationProperties();
if(window.LoginShell && (LoginShell.testSelection == null)){
properties = new Accommodations.Properties(TDS.globalAccommodations);
}
var langs = properties.getLanguages() || [];
return (langs.indexOf(lang) != -1);
};
Sections.TTSCheck.prototype.getSettings = function(){
TTS.Config.Lang.ESN.Enabled = this.hasLang('ESN');
var accProps = TDS.getAccommodationProperties();
var isNotMobile = !Util.Browser.isMobile();
if(isNotMobile===false) {
$('#TTS_Set_Defaults').parent().hide();
$('#TTS_Adjustments .instructions').hide();
}
var config = {
ShowVoicePacks: accProps.showVoicePackControl(),
ShowVolume:     isNotMobile && accProps.showVolumeControl(),
ShowPitch:      isNotMobile && accProps.showPitchControl() && !Util.Browser.isLinux(),
ShowRate:       isNotMobile && accProps.showRateControl()
};
return config;
};

// FILE: section_TestSelectionProxyReenter.js (306cd116) 7/22/2014 5:26:22 PM

Sections.TestSelectionProxyReenter = function()
{
Sections.LoginVerify.superclass.constructor.call(this, 'sectionTestSelectionProxyReenter');
};
YAHOO.lang.extend(Sections.TestSelectionProxyReenter, Sections.Base);
Sections.TestSelectionProxyReenter.prototype.load = function () {
YAHOO.lang.later(0, this, function () {
if (LoginShell.session == null) {
var sessionId = YAHOO.util.Cookie.getSub('TDS-Student-Data', 'S_ID');
LoginShell.session = { id: sessionId };
}
LoginShell.session.isProctorless = false;
if (LoginShell.testee == null)
LoginShell.testee = {};
this.request('next');
});
};

