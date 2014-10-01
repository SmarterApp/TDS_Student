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

    // standalone means the sound check page is being used outside of the test (e.x., diagnostic.xhtml)
    /*
    if (!standalone)
    {
        hasAudio = true; // TestOpportunity.TestProperties.HasAudio.ToString().ToLower()
        hasRecorder = true; // TestOpportunity.TestProperties.HasRecorder.ToString().ToLower()
    }
    */

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

        // remove playing fail style (BUG #16088)
        YUD.removeClass('audioPlayer', 'playing_fail');

        YUD.setStyle('checkSound', 'display', '');
        YUD.setStyle('checkRecorder', 'display', 'none');
        YUD.setStyle('checkError', 'display', 'none');
    }

    function showRecorderCheck()
    {
        checkState = CheckState.Recorder;

        // BUG #16088: Record button remains disabled
        if (audioRecorder && YUD.hasClass(audioRecorder, 'recording_fail'))
        {
            audioRecorder.classNameEvent = 'recording_ready'; // BUG #16238: Another Mic icon displays
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

        // if there is also a recorder then show the check recorder UI
        if (hasRecorder)
        {
            showRecorderCheck();
        }
        // otherwise redirect to intructions
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
	
    //HACK FOR BUGG-102153
    //switch .ogg to .m4a if unsupported
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
    
    // BUG #12486: When using <a> tags you have to stop the event on 
    // Firefox 3.5 OSX 10.6 or the '#' in href makes it return to the 
    // sound check page. To do this use "javascript:void(0);" in the href.

    // add button listners
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

    // this gets called every time section is shown
    this.load = function ()
    {
        // set all audio sections as hidden
        YUD.setStyle('flashError', 'display', 'none');
        YUD.setStyle('javaError', 'display', 'none');
        YUD.setStyle('checkSound', 'display', 'none');
        YUD.setStyle('checkRecorder', 'display', 'none');
        
        // check if SB recorder is available
        if (hasRecorder && TDS.SecureBrowser.getRecorder() == null) {
            YUD.setStyle('soundCheckRecorderError', 'display', 'block');
            return;
        }
        
        if (hasAudio)
        {
            // show check sound UI
            showSoundCheck();
        }
        else if (hasRecorder)
        {
            // show check recorder UI
            showRecorderCheck();
        }
    };

};