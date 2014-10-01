/**
 *   All the important code that is utilized in this site has been moved into a stand alone 
 *   TTS.Config.UI that allows us to modify the config settings.  I suppose it could just
 *   be namespaced as TTS.UI, but that gives the wrong impression about what does highlights...
 */
Sections.TTSCheck = function(){
    Sections.TTSCheck.superclass.constructor.call(this, 'sectionTTSCheck');
};
YAHOO.lang.extend(Sections.TTSCheck, Sections.Base);

Sections.TTSCheck.prototype.init = function(){

    //Initialize TTS after the page has loaded (ensures any custom config can be pulled in)
    YAHOO.util.Event.onDOMReady(function(){
        this.ui  = new TTS.Config.UI();
        this.ui.next = this.request.bind(this, 'next');
        this.ui.back = this.request.bind(this, 'back');

        TTS.getInstance().runOnInit(function(){
            TTS.Config.User.reset(); //Resets the TTS user configuration before displaying
            this.ui.setCfg(this.getSettings());
            this.ui.init();
        }.bind(this));
    }.bind(this));
};

Sections.TTSCheck.prototype.load = function(){

};

/**
 *  Check if the accommodations actually support a language.
 */
Sections.TTSCheck.prototype.hasLang = function(lang){
    var properties = TDS.getAccommodationProperties();
    if(window.LoginShell && (LoginShell.testSelection == null)){
        properties = new Accommodations.Properties(TDS.globalAccommodations);
    }
    var langs = properties.getLanguages() || [];
    return (langs.indexOf(lang) != -1);
};


//Settings we need to configure
Sections.TTSCheck.prototype.getSettings = function(){
    //Set if the test / seection supports this language at all.
    TTS.Config.Lang.ESN.Enabled = this.hasLang('ESN');

    //Do more settings config.
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
