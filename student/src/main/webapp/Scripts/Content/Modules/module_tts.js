 /**
  * This module handles the hacks for TTS based around Multiple choice answers, AND the 
  * right click events for selection etc.  This is also required to initialize the TTS system
  * within the test and turn on or off TTS highlight selection based on accommodations.
  *
  * DEBUG:  In your firebug console you can set TTS.Config.Debug = true in order to see a much
  * better view of what the TTS system is actually doing.
  *
  * Makes heavy use of TTS.Menu (not terribly efficient but better than the older code by a lot).
  *
  *  TODO: Consider adding in logic in the initial page load to add language tags (per Balaji)
  *
  */
(function(){
    //Is there a test I can do to disable everything if the accomodation properties do now allow
    //any tts for this test?  The accommodation properties seem to be set on the page level
    TTS.MenuSystem = { //Convert a {TTS.Menu}.getMenuCfg(obj) into the TDS menu implementation.
        Last: null,    //The most recently created TTS menu info
        addMenuSetup: function(menu, playCfg){
          try{
            if(!menu || !playCfg || !playCfg.ORDER){
                return;
            }
            //Debug for Console debug of the TTS menu
            TTS.MenuSystem.Last = playCfg; 

            var order = playCfg.ORDER;
            for(var i=0; i<order.length; ++i){
                var entryName = order[i];
                var entry = playCfg[entryName];

                if(entry && (typeof entry.cb == 'function' || entry.allowDisabled)){
                  menu.addMenuItem(entry.level || 'entity', {
                    text: Messages.get(entry.Label || 'TTS'),
                    onclick: {fn: entry.cb},
                    disabled:  typeof entry.cb == 'function' ? false : entry.allowDisabled,
                    classname: entry.css || 'speaksection'
                  });
                }
            }
          }catch(e){
            console.error("Failed to create menu item.", e);
          }
        }
    };


    // Override the check for whether tracking is enabled or not based to look for the accommodation.
    TTS.Config.isTrackingEnabled = function () {
        return window.ContentManager != null && window.ContentManager.getAccommodationProperties().isTTSTrackingEnabled();
    };
    
    //Initialize TTS after the page has loaded (ensures any custom config can be pulled in)
    //Delay initialization till the page is actually loaded and all configuration is present,
    //shell loads in configuration after all the scripts have been loaded. 
    YAHOO.util.Event.onDOMReady(function () {
        // TODO: Some CM init event where accs are also ready        
        setTimeout(function () { //Item preview hack, initial setup requires delay...                            
            TTS.getInstance();
        }, 500);
    });


    // listen for when the item is ready
    ContentManager.onPassageEvent('menushow', function(page, passage, menu, evt, selection){
        // we only support Firefox
        if (!TTS.Manager.isAvailable()) return;

        //first does the stimulus has TTS enabled in accommodations?
        var accProps = page.getAccommodationProperties();
        if (!accProps.hasTTSStimulus()) return;
        
        // Bug 114478 - do not populate TTS menu items if other audio is playing
        if (TDS.Audio.isActive()) return;

        //Create the text to speech menu defaults.

        var languages  = ContentManager.getLanguage() != 'ENU' ? ['ESN', 'ENU'] : ['ENU'];
        var ttsMenu    = new TTS.Menu(languages);

        var menuCfg = ttsMenu.getMenuCfg(
          passage.getElement(), //Passage element
          selection,            //The selection that was on the page.
          page.getActiveWin(), //Active window
          passage.stemTTS,      //If defined will overwrite all passage html
          page,
          passage
        );
        TTS.Config.Debug && console.log("Passage menu show.", menuCfg);
        TTS.MenuSystem.addMenuSetup(menu, menuCfg);
    });


    // listen for when the item is ready and Check if we have tts enabled and available.
    ContentManager.onItemEvent('menushow', function(page, item, menu, evt, selection){
        var ctrl = TTS.getInstance();
        if (!ctrl.isAvailable()) return;
        
        var accProps = page.getAccommodationProperties();
        if (!accProps.hasTTSItem()) return;

        // Bug 114478 - do not populate TTS menu items if other audio is playing
        if (TDS.Audio.isActive()) return;

        // EBSR handles TTS menushow in module_ebsr.js
        if (item.EBSR) return;

        //If the active element is the stem or illustration, we can popup this menu.
        if (item.getActiveComponent() != item.getStemElement() &&
            item.getActiveComponent() != item.getIllustrationElement() && !item.MC){
          return;
        }

        //Create the text to speech menu defaults.
        var pageWin    = page.getActiveWin(); //for determining user highlight selection
        var languages  = ContentManager.getLanguage() != 'ENU' ? ['ESN', 'ENU'] : ['ENU'];
        var domToParse = [];

        //Add in major dom elements that will make up TTS parsing / play info
        var stem = item.getStemElement();
        if(stem){
          domToParse.push(stem);
        }

        var illustration = item.getIllustrationElement(); 
        if(illustration){
          domToParse.push(illustration);
        }

        //Actually load up the menu configuration plus hard coded stemTTS hacks.
        var ttsMenu    = new TTS.Menu(languages);
        var menuCfg   = ttsMenu.getMenuCfg(domToParse, selection, pageWin, item.stemTTS, page, item);

        if (item.MC){ //HACKS specific to only multiple choice objects. (LEGACY CODE)
            var optionGroup = item.MC;
            
            if (TTS.version < 2.0) {   // Retaining for backwards compatability
                for (var i = 0; i < optionGroup.getOptions().length; ++i){
                    domToParse.push((optionGroup.getOptions())[i].getElement());
                }
                //Play question and options selection.
                ttsMenu.addOptions(menuCfg, domToParse);
            }

            //If we focused only the option, speak only the option (note this stomps other menu options)
            var focusedOption = optionGroup.getFocusedOption();
            if (focusedOption && focusedOption.key){ //If options are focused then we disable selections
                menuCfg.SEL_PRI = false; //Disables the two selection options if focused
                menuCfg.SEL_SEC = false;

                //Hacky, but if a component level menu shows up, it stomps anything except component level.
                //Not a fan of this model....  You have no idea if something else has altered the 'level'
                //of the menu that is passed in so we have to hack it based on types that do.
                //WHY ISN"T IT IN A LOOP or config setting Justin?  Because only _some_ of these need to
                //show up in the MC hacks, this is that list.
                menuCfg.PRI.level    = 'component';
                menuCfg.SEC.level    = 'component';
                menuCfg.STOP.level   = 'component';
                menuCfg.PAUSE.level  = 'component';
                menuCfg.RESUME.level = 'component';

                ttsMenu.addFocusedOption(menuCfg, item.getActiveComponent());
            }
        }
        TTS.MenuSystem.addMenuSetup(menu, menuCfg);
        TTS.Config.Debug && console.log("TTS On Item Menushow config", menuCfg);
    });

    function ttsNotSpeaking () {
        var ttsInstance = TTS.getInstance();
        if (ttsInstance && ttsInstance.isPlaying()) {
            return false;
        } else {
            return true;
        }
    }

    TDS.Audio.Player.onBeforePlay.subscribe(function() {
        return ttsNotSpeaking();
    });
    
    TDS.Audio.Player.onBeforeResume.subscribe(function () {
        return ttsNotSpeaking();
    });

    //Always try to stop TTS if it is running.
    ContentManager.onPageEvent('hide', function(page){
        TTS.getInstance().stop();
    });
})();

