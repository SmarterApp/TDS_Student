// fires when the DOM is loaded (but not all the images)
YUE.onDOMReady(function()
{
    // Sets focus to the current window
    // BUG: #13529 - Focus is lost on 'Is this your test?' page
    window.focus();

    // fire preinit function if it exists
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

    // setup keyboard listener
    KeyManager.init();

    // global key events
    KeyManager.onKeyEvent.subscribe(function(obj)
    {
        // check for escape
        if (obj.type == 'keydown' && obj.keyCode == 27)
        {
            // hide help
            TDS.ToolManager.hideAll();
        }
    });

    // setup help:
    YUE.on('btnHelp', 'click', function(evt)
    {
        // stop mouse click
        YUE.stopEvent(evt);
    });

    YUE.on('btnHelp', 'mouseup', function(evt)
    {
        // get the tools path
        var key = 'Global.Path.Help';
        var lang = TDS.getLanguage(); // this is because we need the key to unique if someone switches global language
        var id = 'tool-' + key + '-' + lang;

        var panel = TDS.ToolManager.get(id);

        if (panel == null)
        {
            var headerText = window.Messages.getAlt('StudentMaster.Label.HelpGuider', 'Help');
            panel = TDS.ToolManager.createPanel(id, 'helpguide', headerText, null, key);
        }

        TDS.ToolManager.toggle(panel);
    });

    // attach buttons events
    TDS.Button.init();

    // write out aria log div
    TDS.ARIA.createLog();

    // setup accommodations
    setupAccommodations();

    // perform some proxy specific code
    if (TDS.isProxyLogin)
    {
        TDS.CLS.LogoutComponent.init();
        
        //initialize a timeout handler. in the proxy app we want to log the proctor out if he is idle on any page.
        //testshell has a separate timer. the timer here handles idling in student login and review shells.
        //do not do this on the proctor login pages i.e. clslogin.xhtml or replogin.xhtml.
        var currentPage = (location.href).toLowerCase();
        if (currentPage.indexOf('login') == -1)
        {
            //the time the user gets to response is hardcoded to 30 seconds right now.
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

// fires before the page gets unloaded
window.onbeforeunload = function()
{
    // always try and stop TTS before leaving a page
    TTS.Manager.stop();

    // destroy audio iframe
    /*if (typeof AudioManager == 'object')
    {
        AudioManager.dispose();
    }*/
    
    //check for valid exit.
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
        // check if offline cache is available for this browser
        if (TDS.Cache.isAvailable())
        {
            // stop the cache and wait for it to be shutdown
            TDS.Cache.stop();
            
            // failsafe timer for shutting down (wait 1 min)
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

// this gets triggered when someone tries to stop the cache
TDS.Cache.Events.subscribe('onStop', function()
{
    TDS.Dialog.showProgress();
});

// this gets triggered when the cache has been forced to shutdown
TDS.Cache.Events.subscribe('onShutdown', function()
{
    Util.SecureBrowser.close();
});

function setupAccommodations() 
{
    // get the test accommodations
    var testAccommodations = Accommodations.Manager.getDefault();
    
    // check if we should apply the global or test accommodations to the body
    if (testAccommodations != null)
    {
        testAccommodations.applyCSS(document.body);
    }
    else
    {
        TDS.globalAccommodations.applyCSS(document.body);
    }

    // create global accommodations selector dialog
    window.globalAccDialog = new Accommodations.Dialog(TDS.globalAccommodations, 'globalAccDialog');
    
    window.globalAccDialog.onBeforeSave.subscribe(function(accommodations)
    {
        accommodations.removeCSS(document.body);
    });

    var currentGlobalAccs = null; // copy of global accs
    var currentGlobalLang = 'ENU';

    // open global accommodations
    YUE.on('btnAccGlobal', 'click', function(evt)
    {
        // hide help
        TDS.ToolManager.hideAll();

        currentGlobalAccs = TDS.globalAccommodations.clone(); // make a copy of accs in case we cancel
        window.globalAccDialog.show();
    });
    
    // save global accommodations
    window.globalAccDialog.onSave.subscribe(function(accommodations)
    {
        // update css
        accommodations.applyCSS(document.body);

        // check if language changed
        var accProps = new Accommodations.Properties(accommodations);
        var newGlobalLang = accProps.getLanguage();

        if (currentGlobalLang != newGlobalLang)
        {
            // update i18n messages
            currentGlobalLang = newGlobalLang;
            TDS.Messages.Template.processLanguage();
        }

        // persist in cookie
        var globalString = accommodations.getSelectedDelimited();

        if (globalString != null)
        {
            // Disabled saving global accs cookie since it was never used
            // globalString = globalString.replace(/;/g, ','); // HACK: cookie is expected to be comma delimitted
            // YAHOO.util.Cookie.setSub("TDS-Student-Accs", "global", globalString, { path: '/' });
        }
    });
    
    // reset global accommodations
    window.globalAccDialog.onCancel.subscribe(function()
    {
        var selectedAccs = currentGlobalAccs.getSelectedJson();

        Util.Array.each(selectedAccs, function(selectedAcc)
        {
            TDS.globalAccommodations.selectCodes(selectedAcc.type, selectedAcc.codes);
        });
    });
}

// add accommodations to help guide
TDS.ToolManager.Events.subscribe('onShow', function(panel)
{
    var frame = panel.getFrame();
    Util.Dom.copyCSSFrame(frame);
});
