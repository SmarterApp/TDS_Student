//Created to fulfill ticket 88056

(function (TS){
    var Player = TDS.Audio.Player;
    var Slideshow = window.slide; 

    function attemptPlay(){
        subscribeEndAudio();
        Util.log('unmuting...');
        if(Util.SecureBrowser.unmute()){
            TestShell.muted = false;
        }
    }
    
    function engageAutomute(){
        subscribePlayAudio();
        doMute();
        Util.log('Automute engaged');
    }

    function doMute()
    {
        unsubscribeEndAudio();
        Util.log('muting...');
        if(Util.SecureBrowser.mute()){
            TestShell.muted = true;
        }
    }
    
    function subscribePlayAudio(){
        Player.onPlay.subscribe(attemptPlay);
        Slideshow.onPlay.subscribe(attemptPlay);
    }

    function subscribeEndAudio(){
        Player.onIdle.subscribe(doMute);
        Player.onFail.subscribe(doMute);
        Slideshow.onIdle.subscribe(doMute);
    }
    
    function unsubscribeEndAudio(){
        Player.onIdle.unsubscribe(doMute);
        Player.onFail.unsubscribe(doMute);
        Slideshow.onIdle.unsubscribe(doMute);
    }

    function automute_init(){
        var accProps = TDS.getAccommodationProperties();
        if(accProps && accProps.isAutoMute()) {
            engageAutomute();
        }
    }
    TS.Events.subscribe('init', automute_init);
    
    //DEBUG
    //TS.automute_start = engageAutomute;
})(TestShell);