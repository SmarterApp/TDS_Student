/**
 *  This control is an attempt to provide a simple clean interface to using the TTS library.
 *    See README for basic code structure of the TTS application at large.
 *
 *  The control is a class that simply tries to remove some of the suffering around the playing
 *  of dom elements, selection etc.  You shouldn't have to know the content that is most likely
 *  what you want to play, the library should figure that out.  
 *
 *  You can play in the following ways:
 *
 *  var cntrl = TTS.getInstance() || new TTS.Control();
 *      cntrl.play(); //Play the body, unless the user has done range selection
 *      cntrl.play(document.getElementById('id')); //Play the dom in id
 *      cntrl.play('Some Text'); //Speak 'some Text
 *
 *  You can also look up an existing instance in TTS.Store.Instances
 */
(function() {
    var TDS = window.TDS || {}; //This really should be a variable called "TTS.Config.Voice.."
    TTS = window.TTS || {};
    TTS.version = 1.0;

    //Contains references so you can more easily debug in a page that is loaded.
    TTS.Store = TTS.Store || {
        count: 0,
        Instances: {}
    };

    //Provide an easy way of accessing a single instance of the control.
    TTS.Singleton = null;
    TTS.getInstance = function(NoInit) {
        if (!TTS.Singleton) {
            var cfg = JSON.parse(JSON.stringify(TTS.Config));
            cfg.NoInit = NoInit;
            TTS.Singleton = new TTS.Control(cfg);
        }
        return TTS.Singleton;
    };

    /**
     *  Initialize with your own config, or it makes a copy per instance.  Sadly the TTS.Manager
     *  is a somewhat ugly global namespace so we can only do so much.
     */
    TTS.Control = function(cfg) {
        this.cfg = cfg || JSON.parse(JSON.stringify(TTS.Config));
        //Manager Reference
        //Default Parser
        if (!this.cfg.NoInit) {
            this.init(this.cfg.cb);
        }
        //Keep a reference to all the instances for debugging
        TTS.Store.Instances[TTS.Store.count++] = this;
    };


    /**
     *  Initialize various event subscribers and try to setup text to speech if available.
     */
    TTS.Control.prototype.init = function(cb) {
        //Listen to all the different events that prove that it is up and running
        this.eventSubscribe(cb);

        //Deal with all the random strange bits of suffering.
        this.OSHacks();
        this.BrowserHacks();

        //Setup the voices that are used by tts
        this.registerVoices(TTS.Config.getVoices());

        //Init the TTS.Manager
        this.getManager().init();
    };

    /**
     *  Helper method for running on initialization
     */
    TTS.Control.prototype.runOnInit = function(cb) {
        if (typeof cb != 'function') {
            return;
        }

        var state = TTS.Manager.getStatus();
        if (TTS.Manager._initialized || state == TTS.Status.NotSupported) {
            cb(state);
        } else {
            this.getEventManager().onInitSuccess.subscribe(cb);
        }
    };


    /**
     *   Checking if something like ESN (espanol) is supported
     */
    TTS.Control.prototype.isLanguageSupported = function(langCode) {
        return TTS.Manager.isLanguageSupported(langCode);
    };


    /**
     *   getVoices for Language returns an array of config information along with
     *   helpful friendly names and exact names.  getVoice and getVoices return only
     *   the actual OS level key (aka non-user facing)
     */
    TTS.Control.prototype.getVoicesForLanguage = function(lang) {
        lang = lang || TTS.Config.Lang.ENU.Code;
        return TTS.Manager.getVoicesForLanguage(lang);
    };

    TTS.Control.prototype.getVoiceForLanguage = function(lang) {
        lang = lang || TTS.Config.Lang.ENU.Code;
        return TTS.Manager.getVoiceForLanguage(lang);
    };

    TTS.Control.prototype.getStatus = function() {
        return TTS.Manager.getStatus();
    };
    /**
     *  This will get the active voices that are supported on the OS, this is not a 
     *  user friendly key.  See this.getVoicesForLanguage() ^
     */
    TTS.Control.prototype.getVoice = function() { //currently active voice
        return TTS.Manager.getVoice();
    };
    TTS.Control.prototype.getVoices = function() { //Service call level
        return TTS.Manager.getVoices();
    };


    /**
     * If you call this method after initialization, you MUST then call this.getManager().init()
     */
    TTS.Control.prototype.registerVoices = function(voices) {
        voices = voices || TTS.Config.getVoices();
        if (!voices || !voices.length) {
            return;
        }

        TTS.Manager.clearVoicePacks();
        for (var i = 0; i < voices.length; i++) {
            var voicePack = voices[i];
            TTS.Manager.registerVoicePack(voicePack.name, voicePack.priority, voicePack.language);
        }

        //Step by step...
        if (TTS.Manager._initialized) {
            TTS.Manager._initializeAvailableVoices();
        }
    };


    /**
     * Going to remove this highlighter and the events associated with it.
     */
    TTS.Control.prototype.removeHighlighter = function() {
        var eM = eM || this.getEventManager();
        if (eM && eM.onWord && this.highObj) {
            if (this.highCb) {
                eM.onWord.unsubscribe(this.highCb);
            }
            if (this.stopCb) {
                eM.onStatusChange.unsubscribe(this.stopCb);
            }
            delete this.highObj;
        }
    };

    /**
     *  Sets up the text highlighter, the highlighter is somewhat mangled Summit code that now
     *  will attempt to use Rangy to do the highlighting.  Why we are using a visual cue for a
     *  feature made to help with people who cannot really use vision is beyond me... "feature"
     */
    TTS.Control.prototype.setupHighlighter = function(toSpeak, debugStr) {
        if (!TTS.Config.isTrackingEnabled()) {
            return;
        }
        try {
            TTS.Config.Debug && console.log("Create new highlighter with toSpeak, debugStr", toSpeak, debugStr);
            //Note the highlight object getWords function is setup in the BrowserHacks
            //function in this class.
            this.removeHighlighter();
            var highObj = new TTS.Highlighter(toSpeak.textInfo, debugStr);

            //For function unsubscribe and bindings, you need to do it this way.  Using
            //this.highObj == Bad references / lingering issues.
            this.highCb = highObj.highlightNext.bind(highObj);
            this.stopCb = highObj.onPlayStop.bind(highObj);

            var eM = this.getEventManager();
            if (eM && eM.onWord) {
                eM.onWord.subscribe(this.highCb);
                eM.onStatusChange.subscribe(this.stopCb);
            }
            this.highObj = highObj;
        } catch(e) {
            console.error('Failed to setup a highlighter (toSpeak)', toSpeak, e);
        }
        return this.highObj;
    };

    /**
     * Allow the user to specify a manager, but by default it is going to use the 
     * standard TTS.Manager.
     */
    TTS.Control.prototype.setManager = function(manager) {
        this._manager = manager;
    };

    TTS.Control.prototype.getManager = function() {
        if (!this._manager) {
            this._manager = window[this.cfg.DefaultManager] || window.TTS.Manager;
        }
        return this._manager;
    };

    /*
    *  Handle all the details of the different default runtimes we have to 
    * initialize and associate with the TTS.Manager.  Chrome has a word boundary
    * for a lot more elements than firefox, periods, question marks, # and = all
    * generate a seperate word.   This attempts to take that into account...
    */
    TTS.Control.prototype.BrowserHacks = function() {
        TTS.Highlighter.prototype.getWords = this.getWordBoundaryHack();
    };

    TTS.Control.prototype.getWordBoundaryHack = function() {
        var func = null;
        if (Util.Browser.isChrome() && !Util.Browser.isWindows()) {
            func = function(text) {
                if (!text) {
                    return;
                }

                var arr = text.replace(/<.*?\/>/g, '').replace(/\s{2,}/g, ' ').replace(/[-\/\^\*{}\-_`~()]/g, "").split(/\s/g);
                var fin = [];
                for (var i = 0; i < arr.length; ++i) {
                    var word = arr[i];
                    var punct = word.match(/[\.!\?\#,]+/g);
                    if (word && punct && punct.length) {
                        fin.push(word.replace(/[\.!\?\#,]+/g, ''));
                        fin.push(punct[0]);
                    } else if (word != '' && word != 'undefined') { //Note Chrome has a word for the [[sync blocks]]
                        fin.push(word);
                    }
                }
                return fin.filter(function(el) {
                    if (typeof el == 'string' && el != '' && !el.match(/\[/) && !el.match(/\]/)) {
                        return el;
                    }
                });
            };
        } else {
            //Firefox, the word boundary elements do not include .?,#= and other such elements
            //Firefox does _not_ have a word event for the [[sync blocks]]
            func = function(text) {
                if (!text) {
                    return;
                }
                var arr = text.replace(/<.*?\/>/g, '').replace(/\s{2,}/g, ' ').replace(/[\.,-\/!\^\*;:{}\-_`~()]/g, "").split(/\s/g);
                var fin = [];
                for (var i = 0; i < arr.length; ++i) {
                    var word = arr[i];
                    var punct = word.match(/[\#]+/g);
                    if (word && punct && punct.length) {
                        fin.push(word.replace(/[\#]+/g, ''));
                        fin.push(punct[0]);
                    } else if (word != '' && word != 'undefined') { //Note Chrome has a word for the [[sync blocks]]
                        fin.push(word);
                    }
                }
                return fin.filter(function(el) {
                    if (typeof el == 'string' && el != '' && !el.match(/\[/) && !el.match(/\]/)) {
                        return el;
                    }
                });
            };
        }
        return func;
    };


    TTS.Control.prototype.OSHacks = function() {
        if (Util.Browser.isLinux()) {
            this.speak = function(text, language) {
                TTS.Manager.play(text, language);
            };
        } else {
            this.speak = function(text, language) {
                setTimeout(function() {
                    TTS.Manager.play(text, language);
                }, this.cfg.PlayDelay || 0);
            };
        }
    };
    /** Placeholder:
     *
     * Attempt to speak the current text or information
     * This is re-assigned in the init block based on the OS hacks required.
     *If you want to override it you will need to override _after_ intialization
     */
    TTS.Control.prototype.speak = function(dom, parseType, language, selection) {
        //overriden in OSHacks
        console.warn("If this is in the message, then the TTS.Manager did not init correctly.");
    };

    /**
     * Subscribe to the various TTS events.
     */
    TTS.Control.prototype.eventSubscribe = function(cb) {
        var eventManager = this.getEventManager();
        eventManager.onStatusChange.subscribe(function() {
            TTS.Config.Debug && console.log("TTS Status Change.", TTS.Manager.getStatus());
        }.bind(this));

        //listen to status change events from now on.
        eventManager.onInitSuccess.subscribe(function(cb) {
            TTS.Config.Debug && console.log("Initializing TTS: Success.", TTS.Manager.getStatus());
            if (typeof cb == 'function') {
                cb();
            }
            this.initSuccess();
        }.bind(this, cb));

        eventManager.onInitFailure.subscribe(function() {
            console.error("Initializing TTS: Failed.", TTS.Manager.getStatus());
            if (typeof cb == 'function') {
                cb();
            }
        }.bind(this, cb));
    };


    TTS.Control.prototype.initSuccess = function() {
        //Initialize voice packs etc, shouldn't that be done by TTS.Manager?
        try {
            this.INIT_SUCCESS = true;
        } catch(e) {
            console.error("Failed to do post initialization processing.", e);
        }
    };

    TTS.Control.prototype.getDefaultSpeechTarget = function() {
        return this.speechTarget ? this.speechTarget : document.body;
    };

    TTS.Control.prototype.setDefaultSpeechTarget = function(el) {
        this.speechTarget = el;
    };


    /**
     *  dom - (optional) the dom fragment, text or selection information you want to play
     *  parseType - (optional) the parser will be determined by the dom type unless specified
     *  langauge  - (optional) defaults to the page default unless the dom is specified as other
     *  selection - Not sure why I would need this.
     *  highObj   - The container where you want to try and do highlights.
     */
    TTS.Control.prototype.play = function(speechTarget, parseType, language, selection) {
        var toSpeak = null;
        try {
            this.stop(); //Stop any playing that might be in progress
            this.removeHighlighter();

            toSpeak = this.setupPlay(speechTarget, parseType, language, selection);
            if (typeof speechTarget != 'string' && toSpeak && this.hasActualWords(toSpeak.text)) {
                this.setupHighlighter(toSpeak, toSpeak.text);
            }
            this.speak(toSpeak.text, toSpeak.language);
        } catch(e) {
            console.error("Failed to play (speechTarget, parseType, language, toSpeak)",
                speechTarget, parseType, language, selection, toSpeak, e
            );
        }
        return toSpeak;
    };


    /**
     *  Testing purposes.
     */
    TTS.Control.prototype.setupPlay = function(speechTarget, parseType, language) {
        var s = this.getSpeechTarget(speechTarget, parseType, language);
        if (!s) {
            TTS.Config.Debug && console.warn("Could not establish a speech target.", speechTarget, parseType, language);
            return;
        }
        s.language = s.language || TTS.Config.DefaultLanguage;
        return s;
    };


    TTS.Control.prototype.hasActualWords = function(text) {
        var test = (this.getWordBoundaryHack())(text); //Ensure there are actually words...
        if (test && test.length > 0) {
            return true;
        }
        return false;
    };

    /**
     *  Only return a function if there is actually something real to play.
     */
    TTS.Control.prototype.getPlayFunction = function(speechTarget, parseType, language) {
        if (this.isLanguageSupported(language)) {
            var s = this.setupPlay(speechTarget, parseType, language);
            if (s && s.text && this.hasActualWords(s.text)) {
                return function(parseContainer, language) {
                    TTS.getInstance().play(parseContainer, null, language);
                }.bind(this, s.parser, language);
            }
        }
        return false;
    };


    /**
     *  Attempts to provide a lookup for the type of text to speak without the user needing to
     *  know about all the crazy TTS.Parser.Container gotchas, the array parsing, the hacky text 
     *  lookups, etc.   Basically you should use play and not pass arguments and it should
     *  do the "right" thing.
     */
    TTS.Control.prototype.getSpeechTarget = function(speechTarget, parseType, language) {
        language = language || TTS.Config.DefaultLanguage;
        var altLang = language == 'ENU' ? 'ESN' : 'ENU';


        //Createa  TTS.Parser.Container, this will be used to get the text to speak
        var parser = null;

        //Check to see if the user passed in a direct TTS.Parser.Container they want to use
        if (speechTarget && speechTarget.$className == 'TTS.Parser.Container') {
            parser = speechTarget;
        }
        //Check to see if we have a specified parser type
        if (!parser && speechTarget && parseType) {
            parser = new TTS.Parser.Container(speechTarget, parseType, language, altLang);
        }

        //Text is a subset of html... might as well just parse it that way
        if (!parser && typeof speechTarget == 'string') {
            parser = new TTS.Parser.Container(speechTarget, TTS.Parser.Types.HTML, language, altLang);
            parseType = TTS.Parser.Types.HTML; //Debug info
        }

        //Check for a selection
        var docTarget = speechTarget && speechTarget.body ? speechTarget : document;
        var selection = this.getSelection(docTarget);
        if (!parser && selection && selection.commonAncestorContainer) {
            parser = new TTS.Parser.Container(docTarget, TTS.Parser.Types.Selection, language, altLang);
            parseType = TTS.Parser.Types.Selection; //Debug info
            speechTarget = selection;
        }

        //Test for what we assume is a dom object (if you pass an array we assume dom)
        if (!parser && typeof speechTarget == 'object' && !speechTarget.TTS) { //Ensure it is not 'window'
            if (!YAHOO.lang.isArray(speechTarget)) {
                speechTarget = [speechTarget];
            }
            parser = new TTS.Parser.Container(speechTarget, TTS.Parser.Types.DOM, language, altLang);
            parseType = TTS.Parser.Types.DOM; //Debug info
        }

        if (parser) {
            var textArr = parser.getPlayInfo(language);
            var info = {
                originalTarget: speechTarget,
                parser: parser,
                parseType: parseType,
                textInfo: textArr, //Map of text => actual dom containers
                text: parser.createPlayString(textArr, true),
                language: language
            };
            TTS.Config.Debug && console.log("TTS.Control.getSpeechTarget(speechTarget, parseType, lang, ret:",
                speechTarget, parseType, language, info
            );
            return info;
        }
        return null;
    };


    /**
     * Test to see if we have a range selection covering at least one element.  If true
     * this will return the range and most likely try and play that unless the user specified
     * something specific.
     */
    TTS.Control.prototype.getSelection = function(dom) {
        var rng = dom && dom.body ? rangy.getSelection(dom) : rangy.getSelection();
        rng = rng ? rng.getAllRanges() : null;
        if (!rng || !rng.length) {
            return false;
        }
        rng = rng[0];

        //Ends up being a click event.
        if ((rng.startContainer == rng.endContainer) && (rng.startOffset == rng.endOffset)) {
            return false;
        }

        //Ensure that even if they start with a drag operation we gain focus.
        return rng;
    };

    TTS.Control.prototype.isPlaying = function() {
        var state = TTS.Manager.getStatus();
        if (state == TTS.Status.NotSupported) {
            return false;
        }
        return state == TTS.Status.Playing || state == TTS.Status.Unknown;
    };

    TTS.Control.prototype.isAvailable = function() {
        return TTS.Manager.isAvailable();
    };

    TTS.Control.prototype.resume = function() { //Resume the audio
        TTS.Manager.resume();
    };

    TTS.Control.prototype.stop = function() //Stops the audio
    {
        TTS.Manager.stop();
    };

    TTS.Control.prototype.pause = function() { //Pause
        TTS.Manager.pause();
    };

    //Use the existing TTS.Manager Events but add in any subscribes you desire based on those events
    TTS.Control.prototype.getEventManager = function() {
        return TTS.Manager.Events;
    };

    TTS.Control.prototype.setVoice = function(val, langs) {
        TTS.Manager.setVoice(val);
    };
    TTS.Control.prototype.setVolume = function(val) {
        TTS.Manager.setVolume(val);
    };

    TTS.Control.prototype.setRate = function(val) {
        TTS.Manager.setRate(val);
    };

    TTS.Control.prototype.setPitch = function(val) {
        TTS.Manager.setPitch(val);
    };

})();