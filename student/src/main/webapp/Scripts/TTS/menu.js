/**
 *  TTS.Menu  This class attempts to make building a set of standard menu options easier.
 *
 *  Note:  Unfortunately our content and the variation in our content (ESPECIALLY Multiple CHOICE) makes
 *  this really hard to do in a clean faction.   The CFG you can see below is the standard calls we
 *  have available, if the function should appear in a menu then a cb will be defined for that play
 *  instance.
 *
 *  Also because of the way we do language information in the content
 *  ie: <div> First lang </div> <div class="languagedivider"> </div> <div> Second lang </div>
 *
 *  ^ We have to PARSE based on the 'order' of the user languages.  You do not know for certain
 *  which language is in a container, they are _NOT_ marked till the parser comes through and 
 *  tries to tag them.  So if a person chooses spanish, it will com fist in the dom, and after
 *  a language divider it is 'english'.  Make sense?  Hell no!  But we didn't tag content
 *  intelligently so you get to live with it.
 *
 *  For each major section we basically check:
 *    -> Is something already playing?  IN that case we can only pause or stop
 *    -> what is in the dom they pass in (array of cruft in MC case)
 *    -> does the user have a selection?
 *    
 *    Then in the module_tts.js because of the way we wrote MC, we have to do all sorts of
 *    nasty hacks around the menu config, levels for the menu etc.   That is what the SEC_OP is 
 *    for, secondary options, primary options, selection etc.
 *
 *  The ORDER is the current iteration order that is present in the blackbox module_tts.js.
 */
(function(){
var YUD = YAHOO.util.Dom;

//Also need to actually parse all the horrible options etc.
var CFG = {
  PRI:{
    Label: 'TDSTTS.Label.Speak',
    cb: false
  },
  SEC:{
    Label: 'TDSTTS.Label.Speak',
    cb: false
  },
  PRI_OP: {
    Label: 'TDSTTS.Label.SpeakStemAndOptionsPrimary',
    cb: false
  },
  SEC_OP: {
    Label: 'TDSTTS.Label.SpeakStemAndOptionsSecondary',
    cb: false
  },
  SEL_PRI:{
    Label: 'TDSTTS.Label.SpeakTextPrimary', //Why no selection option...
    cb: false
  },
  SEL_SEC:{
    Label: 'TDSTTS.Label.SpeakTextSecondary',
    cb:  false
  },
  STOP:{
    Label: 'TDSTTS.Label.StopSpeaking',
    css: 'stopspeaking',
    //allowDisabled: true, //Enable if you want the button to show up, even if func not avail
    cb:  false
  },
  PAUSE:{
    Label: 'TDSTTS.Label.PauseSpeaking',
    css: 'pausespeaking',
    cb: false 
  },
  RESUME:{
    Label: 'TDSTTS.Label.ResumeSpeaking',
    css: 'resumespeaking',
    cb: false
  },
  ORDER: [ //Order in which they should be added to the menu, / happy iterator support.
    'PRI', 'SEC', 'PRI_OP', 'SEC_OP', 'SEL_PRI', 'SEL_SEC', 'STOP', 'PAUSE', 'RESUME'
  ]
};

//Menu class for determining what exactly can be spoken at any moment.
TTS.Menu = function(langs){
    langs = langs || ['ENU', 'ESN'];

    var ctrl = TTS.getInstance();

    this.langs = langs;

    //Our method of determining what language something is written in is sketchy...
    this.pri = langs[0];
    this.sec = langs[1];
    /* (You can HAVE spanish in the test, but not actually have the ability to play it)
      this.pri = ctrl.isLanguageSupported(langs[0]) ? langs[0] : null;
      this.sec = ctrl.isLanguageSupported(langs[1]) ? langs[1] : null;
    */

    //Set  this to enable pause support, based on browser.
    this.hasPauseSupport = !Util.Browser.isChrome() && // Pause is not working on chrome books
        !Util.Browser.isIOS() && // Bug 120767 - Balaji recommended disabling TTS pause due to iOS bug
        !(Util.Browser.getOSXVersion() >= 10.9 && Util.Browser.getSecureVersion() < 6.5) && //OSX 10.9 requires SB6.5 or above as we fixed a bug in the SB that prevents pause from working  
        !(Util.Browser.isAndroid() && Util.Browser.getSecureVersion() < 2);  // FB120100. On Android, only SB 2.0 and above support pause

    this.reset();
};

//Reset all the defaults in between play operations.
TTS.Menu.prototype.reset = function(){
    this.cfg = JSON.parse(JSON.stringify(CFG));
    return this.cfg;
};

//This is used to determine what is available when a user right clicks.
TTS.Menu.prototype.getMenuCfg = function(dom, selection, win, text, page, entity){
  var cfg = this.reset();
  var ctrl = TTS.getInstance();

  if(!ctrl.isPlaying()){

    //Ensure that even if the speech engine doesn't support the language, we do walk the dom
    //and assign proper language tags.
    if(!text){
      this.checkDom(cfg, dom);
    }else{
      this.checkAlt(cfg, text);
    }

    //Check the language settings to see if we should bother with ESN
    this.checkSelection(cfg, dom, win || window);
    
    //Check the selection options to see if there is a selection with options
  }

  //Check the service status (not in the isPlaying check, have to support resume or pause?
  this.checkServiceStates(cfg);

  //Parse the dom for each language type.
  return cfg;
};


/**
 *  Alt text only allows primary text.
 */
TTS.Menu.prototype.checkAlt = function(cfg, text){
  var ctrl = TTS.getInstance();
  cfg.PRI.cb = ctrl.getPlayFunction(text, TTS.Parser.Types.HTML, this.pri);
};



//Global menu vs the right click.
TTS.Menu.prototype.getGlobalMenuCfg = function(dom, selection){
  //Can only currently be accessed in student, here be BUGS galor (probably)
};

//Check if a range is present, and if so setup the play functions
//The play function will default to playing the selection if one is present.
TTS.Menu.prototype.checkSelection = function(cfg, dom, win){
  var ctrl = TTS.getInstance(); //TODO: Handle all the stupid iFrame crap.
  var win  = win && win.document ? win.document : win;
  if (!dom || !ctrl.getSelection(win)){
    return;
  }

  var contain = new TTS.Parser.Container(win, TTS.Parser.Types.Selection, this.pri, this.sec);

  //Parse the range and attempt to get a play function (the parser fraks this part up, need to fix)
  cfg.SEL_PRI.cb = this.pri ? ctrl.getPlayFunction(contain, null, this.pri) : false;
  cfg.SEL_SEC.cb = this.sec ? ctrl.getPlayFunction(contain, null, this.sec) : false;
};

//Check the dom element for spanish and english text support.
TTS.Menu.prototype.checkDom = function(cfg, dom){
  //Check the dom root to see if we have ENU and ESN
  if(!dom){return;}

  //Parse the dom and see if we have a valid block of text to speak
  var ctrl = TTS.getInstance();
  if(ctrl.isPlaying()){return;}

  //Remember the getPlayFunction will restrict parsing to only supported langs by default,
  //forcing the container to accept that ESN content is required since you can have a spanish
  //test but only ENU voice packs
  var contain = new TTS.Parser.Container(dom, TTS.Parser.Types.DOM, this.pri, this.sec);
  cfg.PRI.cb = this.pri ? ctrl.getPlayFunction(contain, null, this.pri) : false; 
  cfg.PRI.Label = this.getDomTitle(dom, cfg.PRI.Label, this.pri);
  cfg.PRI.contain = contain;

  cfg.SEC.cb = this.sec ? ctrl.getPlayFunction(contain, null, this.sec) : false; 
  cfg.SEC.Label = this.getDomTitle(dom, cfg.SEC.Label, this.sec);
  cfg.SEC.contain = contain;
};


//Requires additional hack code in order to actually support option by option support...
TTS.Menu.prototype.addOptions = function(cfg, dom){
  cfg = cfg || this.cfg; //This is hard since MC items have all sorts of crap on them.

  var ctrl = TTS.getInstance();
  if(ctrl.isPlaying()){return;}

  //Remember the getPlayFunction will restrict parsing to only supported langs by default,
  //forcing the container to accept that ESN content is required since you can have a spanish
  //test but only ENU voice packs
  var contain = new TTS.Parser.Container(dom, TTS.Parser.Types.DOM, this.pri, this.sec);
  cfg.PRI_OP.cb = this.pri ? ctrl.getPlayFunction(contain, null, this.pri) : false;
  cfg.PRI_OP.contain = contain;
  cfg.SEC_OP.cb = this.sec ? ctrl.getPlayFunction(contain, null, this.sec) : false;
  cfg.SEC_OP.contain = contain;
};


TTS.Menu.prototype.addFocusedOption = function(cfg, dom){
  if(!dom){return;}

  cfg = cfg || this.cfg; //This is hard since MC items have all sorts of crap on them.

  var ctrl = TTS.getInstance();
  if(ctrl.isPlaying()){return;}
  

  //Remember the getPlayFunction will restrict parsing to only supported langs by default,
  //forcing the container to accept that ESN content is required since you can have a spanish
  //test but only ENU voice packs
  var contain = new TTS.Parser.Container(dom, TTS.Parser.Types.DOM, this.pri, this.sec);

  cfg.PRI.cb    = this.pri ? ctrl.getPlayFunction(contain, null, this.pri) : false;
  cfg.PRI.Label = this.getDomTitle(dom, cfg.PRI.Label, this.pri);
  cfg.PRI.contain = contain;

  cfg.SEC.cb    = this.sec ? ctrl.getPlayFunction(contain, null, this.sec) : false;
  cfg.SEC.Label = this.getDomTitle(dom, cfg.SEC.Label, this.sec);
  cfg.SEC.contain = contain;
};



//Check to see if the service is playing already or not.
TTS.Menu.prototype.checkServiceStates = function(cfg){
  var ctrl = TTS.getInstance();
  if(ctrl.isPlaying()){
    cfg.STOP.cb  =  function(){TTS.getInstance().stop();};
  }
  //Pause and resume support
  if(this.hasPauseSupport){
    if(ctrl.isPlaying()){
      cfg.PAUSE.cb =  function(){TTS.getInstance().pause();};
    }else if(ctrl.getStatus() == TTS.Status.Paused){
      cfg.RESUME.cb = function(){TTS.getInstance().resume();};
    }
  }
};

// we need to create a key to lookup the label of the menu
TTS.Menu.prototype.getDomTitle = function(dom, label, lang){
  if(!dom){ return label; }

  //Sometimes an array, in that case use the first element.
  if(dom && dom.length && dom[0]){
    dom = dom[0]; 
  }

  var key = 'TDSTTS.Label.Speak';
  var mod = this.pri == lang ? 'Primary' : 'Secondary';
  if (dom.title){  // remove whitespace from title (e.x., "Option A" => "OptionA")
      key += dom.title.replace(/\s+/g, '');
  }else{
      key += 'Question';
  }
  key += mod;
  return key;
};

})();
