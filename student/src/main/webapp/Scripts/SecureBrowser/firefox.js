// REQUIRES: YUI, IO.js, SecureBrowser.Base.js

/*
The Desktop version of the secure browser built on top of the firefox platform.
*/

TDS.SecureBrowser.Firefox = function () {
    TDS.SecureBrowser.Firefox.superclass.constructor.call(this);
    this.runtime = null;
    this._ignoringBreachEvents = null;   // Timer object in case we are temporarily not reacting to sb-breach-events.
};

YAHOO.lang.extend(TDS.SecureBrowser.Firefox, TDS.SecureBrowser.Base);

TDS.SecureBrowser.Firefox.prototype.initialize = function () {
    try {

        if (typeof (Runtime) == 'function') {
            this.runtime = new Runtime();
        } else {
            var success = Mozilla.execPrivileged(function () {
                var sbClass = Components.classes["@mozilla.org/securebrowser;1"];
                if (sbClass) {
                    this.runtime = sbClass.createInstance(Components.interfaces.mozISecureBrowser);
                }
            }.bind(this));
            if (!success) {
                console.log('SB runtime component failed to load');
            }
        }

        // check if this was a SB and we got the runtime
        if (!this.runtime) return;

        // dispose of runtime
        YUE.on(window, 'beforeunload', this.dispose.bind(this));
        
        // Listen for the security breach events
        this.enableSecurityBreachDetection();
    }
    catch (ex) { }
};

TDS.SecureBrowser.Firefox.prototype.dispose = function () {
    if (this.runtime != null) {
        delete this.runtime;
        this.runtime = null;
    }
    // remove all listeners so as not to cause a memory leak
    this.disableSecurityBreachDetection();
};

TDS.SecureBrowser.Firefox.prototype._hasAPI = function () {
    return (typeof (SecureBrowser) != 'undefined');
};

TDS.SecureBrowser.Firefox.prototype._hasRuntime = function () {
    return (this.runtime != null);
};

TDS.SecureBrowser.Firefox.prototype.clearCache = function () {
    try {
        if (this._hasAPI()) {
            SecureBrowser.clearCache();
            return true;
        }
    }
    catch (ex) { }

    return false;
};

TDS.SecureBrowser.Firefox.prototype.clearCookies = function () {
    try {
        if (this._hasAPI()) {
            SecureBrowser.clearCookies();
            return true;
        }
    }
    catch (ex) { }

    return false;
};
TDS.SecureBrowser.Firefox.prototype.emptyClipBoard = function () {
    try {
        if (this._hasAPI()) {
            SecureBrowser.emptyClipBoard();
            return true;
        }
    }
    catch (ex) { }

    return false;
};
TDS.SecureBrowser.Firefox.prototype.getMACAddress = function () {
    var mac = null;

    try {
        if (this._hasRuntime()) {
            mac = this.runtime.getMACAddress();
            mac.toUpperCase();
        }
    }
    catch (e) { }

    return mac;
};

TDS.SecureBrowser.Firefox.prototype. _getIPAddressList = function(bypassCache)
{
    // check if has SB runtime
    if (!this._hasRuntime()) return null;
    if (!YAHOO.util.Lang.isBoolean(bypassCache)) bypassCache = false;

    var dnsService = Components.classes["@mozilla.org/network/dns-service;1"].getService(Components.interfaces.nsIDNSService);
    var record = dnsService.resolve(dnsService.myHostName, bypassCache);

    var addressList = [];

    while (record.hasMore())
    {
        var address = record.getNextAddrAsString();
        addressList.push(address);
    }

    return addressList;
};

TDS.SecureBrowser.Firefox.prototype.getIPAddressList = function (bypassCache) {
    var addressList = null;

    try {
        addressList = this._getIPAddressList(bypassCache);
    }
    catch (ex) { }

    return addressList;
};

TDS.SecureBrowser.Firefox.prototype.getProcessList = function (allowDups) {
    var processList = [];

    if (this._hasRuntime()) {
        try {
            var processString = this.runtime.getRunningProcessList();
            processList = processString.split(',');
        }
        catch (e) { }
    }

    // clean any leading or trailing whitespace
    for (var i = 0; i < processList.length; i++) {
        processList[i] = YAHOO.lang.trim(processList[i]).toLowerCase();
    }

    // remove any duplicates
    processList = (allowDups === true) ? processList : Util.Array.unique(processList);

    return processList;
};

TDS.SecureBrowser.Firefox.prototype.fixFocus = function () {
    
    // We need to temporarily suspend listening for security breach events because the act of opening up hidden windows will trigger that
    // We will resume shortly afterwards
    var stopIgnoringBreachEvents = function () {
        this._ignoringBreachEvents = null;
        Util.log("Resuming breach detection");
    };
    if (this._ignoringBreachEvents) {
        clearTimeout(this._ignoringBreachEvents);
    }
    this._ignoringBreachEvents = setTimeout(stopIgnoringBreachEvents.bind(this), 1000);
    Util.log("Suspending breach detection");

    try {        
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
        var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"].getService(Components.interfaces.nsIWindowMediator);
        var w = wm.getMostRecentWindow("navigator:browser");
        var hiddenWindowRef = w.openDialog("chrome://bmakiosk/content/hiddenWindow.xul", "hidden", "alwaysLowered=yes,titlebar=no");
        setTimeout(function () {
            if (hiddenWindowRef != null) {
                hiddenWindowRef.close();
            }

            hiddenWindowRef = null;
        }, 1);

        Util.log('SecureBrowser: Fixed focus success');
        return true;
    }
    catch (ex) {
        Util.log('SecureBrowser: Fixed focus failed - ' + ex);
    }

    return false;
};

TDS.SecureBrowser.Firefox.prototype.close = function () {
    try {
        this.javaTearDown();
    } catch(ex) {

    }

    // if the browser is Mac OS 10.8 or higher, re-enable screenshots
    if (Util.Browser.isMac() && (Util.Browser.getOSXVersion() >= 10.8) && (Util.Browser.getSecureVersion() <= 6.2)) {
        var screenshotsEnabled = Mozilla.enableScreenshots();

        // check if disabling screenshot is successful
        if (screenshotsEnabled) Util.log('Screenshots are enabled.');
        else Util.log('Screenshots are not enabled.');
    }

    try { SecureBrowser.CloseWindow(); } catch (ex) { }

    try {
        this.focus(); self.opener = this; window.open('', '_parent', ''); window.close();
    }
    catch (ex) { }
};

// Get the start time of when the app was launched
TDS.SecureBrowser.Firefox.prototype.getAppStartTime = function () {
    // New SBs have a pref that is set at app launch
    try {
        var startTimePref = Mozilla.getPreference("bmakiosk.startup.timestamp");
        if (startTimePref != null) return Date.parse(startTimePref);
    } catch (e) {
    }

    // Plan B - see if set a session storage appStartTime item      
    if (window.sessionStorage.getItem("appStartTime") != null) {
        return Date.parse(window.sessionStorage.getItem("appStartTime"));
    }

    // OK - we don't know anymore
    return null;
};

TDS.SecureBrowser.Firefox.prototype.isSpacesEnabled = function() {
    var prefValue = Mozilla.getPreference('bmakiosk.spaces.enabled');
    return (YAHOO.lang.isBoolean(prefValue)) ? prefValue : false;
};

TDS.SecureBrowser.Firefox.prototype.javaSetup = function () {
    // grant applet privileges in the user's java.policy file
    var hostURL = window.location.hostname;
    var protocol = window.location.protocol;
    var serverURL = protocol + "//" + hostURL;
    this._grantAppletFromURLFullPrivileges(serverURL);

    // check and make sure we are running from a clean java installation
    var appStartTime = Util.SecureBrowser.getAppStartTime();
    var javaMarkerFile = ".securebrowser_" + (appStartTime == null ? "0" : appStartTime);
    var folderDeleteStatus = this._deleteSunFolder(false, true, javaMarkerFile);

    // Copy over the trusted.cert file if we had to delete the sun folder in this invokation
    if (!folderDeleteStatus.hadError && folderDeleteStatus.folderdeleted) {
        var certFileURL = TDS.resolveBaseUrl('Shared/Applets/trusted.certs.bin');
        var trustedCertsFiles = this._getTrustedCertStores();
        if (!trustedCertsFiles.hadError) {
            for (var i = 0; i < trustedCertsFiles.returnValue.length; i++) {
                var certStore = trustedCertsFiles.returnValue[i];
                this._downloadFile(certFileURL, certStore);
            }
        }
    }
    
    // Copy over the deployment.properties file if we had to delete the sun folder in this invokation
    if (!folderDeleteStatus.hadError && folderDeleteStatus.folderdeleted) {
        var deploymentPropertiesFileUrl = TDS.resolveBaseUrl('Shared/Applets/deployment.properties.bin');
        var deploymentPropertiesFiles = this._getDeploymentPropertiesFile();
        if (!deploymentPropertiesFiles.hadError) {
            for (var i = 0; i < deploymentPropertiesFiles.returnValue.length; i++) {
                var deploymentPropertiesFile = deploymentPropertiesFiles.returnValue[i];
                this._downloadFile(deploymentPropertiesFileUrl, deploymentPropertiesFile);
            }
        }
    }
};

TDS.SecureBrowser.Firefox.prototype.javaTearDown = function () {
    this._deleteSunFolder(true, false, null);
};

// Returns an array of folders where java user profiles are stored
// This is platform specific and java version specific.
// In some platforms, this could be a variety of different locations which is 
// why this function returns a collection instead of a single value
TDS.SecureBrowser.Firefox.prototype._getSunFolderList = function () {
    try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    } catch (e) {
        return { hadError: true, errorString: "UniversalXPConnect denied. Exception = " + e.message };
    }

    var sunDir = [];
    try {
        var platform = navigator.platform.toLowerCase();
        if (platform.indexOf('win') >= 0) {
            var appDataCurrent = DirIO.get('AppData');
            if (appDataCurrent.path.toLowerCase().indexOf('appdata') < 0) {
                //not windows 7 system.
                sunDir[0] = appDataCurrent.path + DirIO.sep + "Sun";
            } else {
                //windows 7 system. need to look into AppData/LocalLow
                sunDir[0] = appDataCurrent.parent.path + DirIO.sep + "LocalLow" + DirIO.sep + "Sun";
            }
        } else if (platform.indexOf('linux') >= 0) {
            var homeDir = DirIO.get("Home");
            sunDir[0] = homeDir.path + DirIO.sep + ".java";
        } else if (platform.indexOf('mac') >= 0) {
            var homeDir = DirIO.get("Home");
            sunDir[0] = homeDir.path + DirIO.sep + "Library" + DirIO.sep + "Caches" + DirIO.sep + "Java Applets"; // Java 1.4 and before
            sunDir[1] = homeDir.path + DirIO.sep + "Library" + DirIO.sep + "Caches" + DirIO.sep + "Java"; // Java 1.5 and above  
            sunDir[2] = homeDir.path + DirIO.sep + "Library/Application\ Support/Oracle/Java/Deployment"; // Java 1.5 and above  
        }
    } catch (e) {
        return { hadError: true, errorString: "Could not determine Sun folder. Exception = " + e.message };
    }

    return { hadError: false, returnValue: sunDir };
};

// Returns an array of folders where java user profiles are stored
// This is platform specific and java version specific.
// In some platforms, this could be a variety of different locations which is 
// why this function returns a collection instead of a single value
TDS.SecureBrowser.Firefox.prototype._getTrustedCertStores = function () {
    var sunFolderListHolder = this._getSunFolderList();
    if (sunFolderListHolder.hadError) return { hadError: true, errorString: "Could not determine Sun folder." };

    var certStoreList = [];
    var platform = navigator.platform.toLowerCase();
    for (var i = 0; i < sunFolderListHolder.returnValue.length; i++) {
        if (platform.indexOf('win') >= 0) {
            certStoreList.push(sunFolderListHolder.returnValue[i] + DirIO.sep + 'Java' + DirIO.sep + 'Deployment' + DirIO.sep + 'security' + DirIO.sep + 'trusted.certs');
        } else if (platform.indexOf('linux') >= 0) {
            certStoreList.push(sunFolderListHolder.returnValue[i] + DirIO.sep + 'deployment' + DirIO.sep + 'security' + DirIO.sep + 'trusted.certs');
        } else if (platform.indexOf('mac') >= 0) {
            certStoreList.push(sunFolderListHolder.returnValue[i] + DirIO.sep + 'security' + DirIO.sep + 'trusted.certs');
        }
    }

    return { hadError: false, returnValue: certStoreList };
};

// Returns an array of file paths to deployment.properties where java user profiles are stored
// This is platform specific and java version specific.
// In some platforms, this could be a variety of different locations which is 
// why this function returns a collection instead of a single value
TDS.SecureBrowser.Firefox.prototype._getDeploymentPropertiesFile = function () {
    var sunFolderListHolder = this._getSunFolderList();
    if (sunFolderListHolder.hadError) return { hadError: true, errorString: "Could not determine Sun folder." };

    var depPropFilesList = [];
    var platform = navigator.platform.toLowerCase();
    for (var i = 0; i < sunFolderListHolder.returnValue.length; i++) {
        if (platform.indexOf('win') >= 0) {
            depPropFilesList.push(sunFolderListHolder.returnValue[i] + DirIO.sep + 'Java' + DirIO.sep + 'Deployment' + DirIO.sep + 'deployment.properties');
        } else if (platform.indexOf('linux') >= 0) {
            depPropFilesList.push(sunFolderListHolder.returnValue[i] + DirIO.sep + 'deployment' + DirIO.sep + 'deployment.properties');
        } else if (platform.indexOf('mac') >= 0) {
            depPropFilesList.push(sunFolderListHolder.returnValue[i] + DirIO.sep + 'deployment.properties');
        }
    }

    return { hadError: false, returnValue: depPropFilesList };
};

// This function deletes the Sun folder that is found in 
// users home dir (for Linux/Mac) or App data folder on Windows
// This is important when running the SB since it comes bundled with 
// its own Java (in windows and Linux) and the presence of this folder 
// can cause conflicts that could impact the Java in the browser. Additionally
// this takes care of clearing the Java cache on all OSes

// boolean param forceDelete - Delete the folder without checking to see if the 
//								SB originally created the Sun folder

// boolean param recreateandMarkFolder - Recreate the Sun folder with a dummy file in it 
//								(to indicate that SB created the Sun folder)

// string param markerName - optional name for the marker file. If not specified, browser
//                           name and version are used

TDS.SecureBrowser.Firefox.prototype._deleteSunFolder = function (forceDelete, recreateandMarkFolder, markerName) {
    try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    } catch (e) {
        return { hadError: true, errorString: "UniversalXPConnect denied. Exception = " + e.message };
    }

    try {
        var sunDir = [];


        var platform = navigator.platform.toLowerCase();
        if (platform.indexOf('win') >= 0) {
            var appDataCurrent = DirIO.get('AppData');
            if (appDataCurrent.path.toLowerCase().indexOf('appdata') < 0) {
                //not windows 7 system.
                sunDir[0] = appDataCurrent.path + DirIO.sep + "Sun";
            } else {
                //windows 7 system. need to look into AppData/LocalLow
                sunDir[0] = appDataCurrent.parent.path + DirIO.sep + "LocalLow" + DirIO.sep + "Sun";
            }
        } else if (platform.indexOf('linux') >= 0) {
            var homeDir = DirIO.get("Home");
            sunDir[0] = homeDir.path + DirIO.sep + ".java";
        } else if (platform.indexOf('mac') >= 0) {
            var homeDir = DirIO.get("Home");
            sunDir[0] = homeDir.path + DirIO.sep + "Library" + DirIO.sep + "Caches" + DirIO.sep + "Java Applets"; // Java 1.4 and before
            sunDir[1] = homeDir.path + DirIO.sep + "Library" + DirIO.sep + "Caches" + DirIO.sep + "Java"; // Java 1.5 and above      
            sunDir[2] = homeDir.path + DirIO.sep + "Library/Application\ Support/Oracle/Java/Deployment"; // Java 1.7 and above  
        }

        // check to See if this Sun folder was created by the SB at some point
        var folderCreatedBySB = true;
        var markerFileName = (navigator.userAgent.indexOf('AIRSecureBrowser') != -1) ? '.SecureBrowser' : '.' + navigator.appCodeName;

        if (markerName != null) markerFileName = markerName;

        for (var counter1 = 0; counter1 < sunDir.length; counter1++) {
            var fileIn = FileIO.open(sunDir[counter1] + DirIO.sep + markerFileName);
            if (!fileIn.exists()) {
                // Our special marker file is not in the Sun folder. We did not create it
                folderCreatedBySB = false;
            }
        }

        // If SB created the Sun Folder and we are not asked to forceDelete, then no need to delete this folder
        if (folderCreatedBySB && !forceDelete) {
            return { hadError: false, errorString: "No need to delete the Sun folder. This browser created it" };
        }

        // Before deleting the folder, we need recurse and check to make sure that there are no read-only
        // files. This is mainly to take care of 2009's approach of setting deployment.certs or trusted.certs
        // to be read-only to prevent deletion of the certificate needed for ELPA.
        for (var counter1 = 0; counter1 < sunDir.length; counter1++) {
            var directory = DirIO.open(sunDir[counter1]);
            if (directory && directory.exists()) {
                var filesInDir = DirIO.read(directory, true);
                for (var counter2 = 0; filesInDir && counter2 < filesInDir.length; counter2++) {
                    var tmpFile = FileIO.open(filesInDir[counter2]);
                    if (tmpFile.isFile())
                        tmpFile.permissions = 438;
                }
                directory.remove(true);
            }
        }

        if (recreateandMarkFolder) {
            for (var counter1 = 0; counter1 < sunDir.length; counter1++) {
                var directory = DirIO.open(sunDir[counter1]);
                DirIO.create(directory);
                var fileIn = FileIO.open(sunDir[counter1] + DirIO.sep + markerFileName);
                FileIO.create(fileIn);
            }
        }

    } catch (e) {
        return { hadError: true, errorString: "UniversalXPConnect granted but still could not drop Sun folder. Exception = " + e.message };
    }
    if (recreateandMarkFolder)
        return { hadError: false, errorString: "Successfully dropped and recreated the Sun folder(s)", folderdeleted: true };
    else
        return { hadError: false, errorString: "Successfully dropped the Sun folder(s)", folderdeleted: true };
};

// This installs the necessary privileges needed
// to run a signed applet from the specified URL
// with full privileges without causing the annoying 
// popup from Java showing up.
// BE CAREFUL -- by doing this, you are allowing applets from 
// this URL (signed or unsigned) to have FULL privileges to do 
// anything that Java allows.
TDS.SecureBrowser.Firefox.prototype._grantAppletFromURLFullPrivileges = function (siteURL) {
    try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    } catch (e) {
        return { hadError: true, errorString: "UniversalXPConnect denied. Exception = " + e.message };
    }

    var homeDir, javaPolicyFile;

    try {
        homeDir = DirIO.get("Home");
        javaPolicyFile = FileIO.open(homeDir.path + DirIO.sep + ".java.policy");
        if (!javaPolicyFile || !javaPolicyFile.exists()) {
            FileIO.create(javaPolicyFile); // creates a file with rw-rw-rw- permissions			
            FileIO.write(javaPolicyFile, "// Created by SB for ELPA \n");
        }
    } catch (e) {
        return { hadError: true, errorString: "UniversalXPConnect obtained but still could not create/open .java.policy file. Exception = " + e.message };
    }

    var fileUpdated = false;
    try {
        var contents = FileIO.read(javaPolicyFile);

        // @TODO : wrap the next 
        if (!siteURL.match("/$")) {
            siteURL += "/"; // Make sure the URL ends with a /
        }
        var policyString = 'grant codebase "' + siteURL + '-" { permission java.security.AllPermission; };';
        if (contents.indexOf(policyString) == -1) {
            // OK - we need to add this URL to the file. IT does not contain it already
            contents += policyString;
            contents += "\n";
            fileUpdated = true;
        }

        if (fileUpdated) {
            FileIO.write(javaPolicyFile, contents);
        }
    } catch (e) {
        return { hadError: true, errorString: "Could not update .java.policy file. Exception = " + e.message };
    }

    return { hadError: false, errorString: ".java.policy file successfully updated" };

};

//This is our solution to preventing pop-ups from Java when running signed applets.
//The .java.policy file appears not to be read by the next-gen plugin so what we are doing here 
//instead is downloading and installing a trusted.certs into the security folder in teh users
//Java profile.

// Downloads a file from the specified URL to the local folder system using the path specified
TDS.SecureBrowser.Firefox.prototype._downloadFile = function (sourceURL, localDestPath) {
    try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    } catch (e) {
        return { hadError: true, errorString: "UniversalXPConnect denied. Exception = " + e.message };
    }

    try {
        //new obj_URI object
        var obj_URI = Components.classes["@mozilla.org/network/io-service;1"].getService(Components.interfaces.nsIIOService).newURI(sourceURL, null, null);

        var objTargetFile = FileIO.open(localDestPath);
        if (!objTargetFile.exists()) {
            FileIO.create(objTargetFile);
        }

        //new persitence object
        var obj_Persist = Components.classes["@mozilla.org/embedding/browser/nsWebBrowserPersist;1"].createInstance(Components.interfaces.nsIWebBrowserPersist);

        // with persist flags if desired
        var nsIWBP = Components.interfaces.nsIWebBrowserPersist;
        var flags = nsIWBP.PERSIST_FLAGS_REPLACE_EXISTING_FILES;
        obj_Persist.persistFlags = flags | nsIWBP.PERSIST_FLAGS_FROM_CACHE;

        //obj_Persist.persistFlags = PERSIST_FLAGS_BYPASS_CACHE;

        //save file to target
        obj_Persist.saveURI(obj_URI, null, null, null, null, objTargetFile);
    } catch (e) {
        return { hadError: true, errorString: "UniversalXPConnect denied. Exception = " + e.message };
    }
    return { hadError: false, errorString: sourceURL + "successfully saved to " + localDestPath };
};

// check if native system mute capabilities is supported on the system
TDS.SecureBrowser.Firefox.prototype._hasNativeMute = function () {
    var winVer = Util.Browser.getWindowsNTVersion();
    return (winVer >= 6.0 && typeof this.runtime.systemMute == 'boolean');
};

// mute system audio
TDS.SecureBrowser.Firefox.prototype.mute = function () {
    try {
        if (this._hasRuntime()) {
            if (this._hasNativeMute()) {
                console.log('mute native');
                this.runtime.systemMute = true;
                return true;
            } else if (this.runtime.systemVolume != null) {
                console.log('mute volume');
                Util.Storage.set('tds-mutedVolume', this.runtime.systemVolume); // store the system volume, will be restored after unmute
                this.runtime.systemVolume = 0;
                return true;
            }
        }
    } catch (ex) {
        console.error(ex);
    }
    return false;
};

// unmute system audio
TDS.SecureBrowser.Firefox.prototype.unmute = function () {
    try {
        if (this._hasRuntime()) {
            if (this._hasNativeMute()) { // muted natively?
                console.log('unmute native');
                this.runtime.systemMute = false;
                return true;
            } else if (this.runtime.systemVolume === 0) { // muted volume?
                var mutedVolume = Util.Storage.get('tds-mutedVolume');
                if (mutedVolume) { // restore previous volume
                    console.log('unmute volume: ' + mutedVolume);
                    this.runtime.systemVolume = mutedVolume;
                    return true;
                }
            }
        }
    } catch (ex) {
        console.error(ex);
    }
    return false;
};

// check if system audio is muted
TDS.SecureBrowser.Firefox.prototype.isMuted = function () {
    try {
        if (this._hasRuntime()) {
            if (this._hasNativeMute()) { 
                return (this.runtime.systemMute === true); // muted natively?
            } else {
                return (this.runtime.systemVolume === 0); // muted volume?
            }
        }
    } catch (ex) {
        console.error(ex);
    }
    return false;
};

// get system volume
TDS.SecureBrowser.Firefox.prototype.getVolume = function (checkMute) {
    try {
        if (this._hasRuntime()) {
            if (checkMute && this.isMuted()) {
                return 0; // muted
            } else {
                return this.runtime.systemVolume * 10; // 0-10.. make into percentage
            }
        }
    } catch (ex) {
        console.error(ex);
    }
    return -1;
};

// set system volume
TDS.SecureBrowser.Firefox.prototype.setVolume = function (percent, checkMute) {
    try {
        if (this._hasRuntime()) {
            // convert percentage to raw value and set
            var level = Math.round(percent / 10);
            console.log('set volume: ' + level);
            this.runtime.systemVolume = level;

            // if we are setting the volume then unmute
            if (checkMute && percent > 0 && this.isMuted()) {
                this.unmute();
            }

            return true;
        }
    } catch (ex) {
        console.error(ex);
    }
    return false;
};

TDS.SecureBrowser.Firefox.prototype.enablePermissiveMode = function (enabled) {

    var changed = false;

    try {
        if (this._hasRuntime() &&
            typeof this.runtime.permissive == 'boolean') {
            this.runtime.permissive = enabled;
            changed = true;
        }
    }
    catch (ex) { }

    // check if something was changed
    if (changed && Util.Browser.isMac()) {

        var availWidth = screen.availWidth;
        var availHeight = screen.availHeight;

        /* 
        NOTE: On Mac when we turn on permissive mode the bar at the top appears.
        When this bar shows up it covers the top of the browser. If we get the 
        available height then it will take into account the bar. So we just need 
        to resize the window to have it fit properly. */
        Mozilla.resizeWindowTo(availWidth, availHeight);
    }

    return changed;
};

TDS.SecureBrowser.Firefox.prototype.enableChromeMode = function (enabled) {
    try {
        if (typeof SecureBrowser == 'object' &&
            typeof SecureBrowser.showChrome == 'function') {
            SecureBrowser.showChrome(enabled);
            return true;
        }
    }
    catch (ex) {}
    return false;
};

/*
Register for SB events that indicate that some other app has forced itself on top of the SB. 
This is treated as a security breach
*/
TDS.SecureBrowser.Firefox.prototype.enableSecurityBreachDetection = function () {
    var self = this;    
    var observer =
    {
        QueryInterface: function(iid) {
            if (iid.equals(Components.interfaces.nsISupports) ||
                iid.equals(Components.interfaces.nsIObserver))
                return this;

            throw Components.results.NS_ERROR_NO_INTERFACE;
        },

        observe: function(subject, topic, data) {
            if (topic == "sb-security-breach" && !self._ignoringBreachEvents) {
                Util.log('Security breach detected!');
                var notification = { type: 'sb-security-breach', message: 'Event from SB' };
                self.Events.onSecurityBreach.fire(notification);
            }
        }
    };

    try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
        var os = Components.classes["@mozilla.org/observer-service;1"].getService(Components.interfaces.nsIObserverService);
        os.addObserver(observer, "sb-security-breach", false);
        Util.log('Listening for sb-security-breach events');
    } catch(e) {
        Util.log(e.message);
    } 
};

/*
Unscribe all listeners that may have hooked into the SB for security breach events. 
This is required to make sure that we dont leak memory
*/
TDS.SecureBrowser.Firefox.prototype.disableSecurityBreachDetection = function () {
    try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
        var os = Components.classes["@mozilla.org/observer-service;1"].getService(Components.interfaces.nsIObserverService);
        var observers = os.enumerateObservers('sb-security-breach');
        while (observers.hasMoreElements()) {
            os.removeObserver(observers.getNext(), 'sb-security-breach');
        }
        Util.log('Removing all observers for sb-security-breach events');
    } catch (e) {
        Util.log(e.message);
    }
};
