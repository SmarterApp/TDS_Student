﻿/*
This is AIR's API for Mindgrub's SB mobile API.
Since they implemented exactly what we designed it pretty much
a one for one call. So this is the API everyone should look at when
building other audio API's. 
*/

(function(SB) {
    
    function Recorder() {
        Recorder.superclass.constructor.call(this);
    }

    YAHOO.lang.extend(Recorder, TDS.SecureBrowser.Base.Recorder);

    Recorder.prototype.initialize = function(eventListener) {
        AIRMobile.recorder.initialize(eventListener);
    };

    Recorder.prototype.getCapabilities = function() {
        return AIRMobile.recorder.getCapabilities(event);
    };

    Recorder.prototype.getDefaultOptions = function() {

        // TODO: Use the first thing returned when calling getCapabilities()

        return {
            captureDevice: 0,
            sampleRate: 48000,
            channelCount: 1,
            sampleSize: 16,
            encodingFormat: 'opus'
        };
    };

    Recorder.prototype.getStatus = function() {
        return AIRMobile.recorder.getStatus();
    };

    Recorder.prototype.startCapture = function(options, eventListener) {
        options = YAHOO.lang.augmentObject(this.getDefaultOptions(), (options || {}));
        AIRMobile.recorder.startCapture(options, eventListener);
    };

    Recorder.prototype.stopCapture = function() {
        AIRMobile.recorder.stopCapture();
    };

    Recorder.prototype.play = function(audioData, eventListener) {
        AIRMobile.recorder.play(audioData, eventListener);
    };

    Recorder.prototype.stopPlay = function() {
        AIRMobile.recorder.stopPlay();
    };

    Recorder.prototype.pausePlay = function() {
        AIRMobile.recorder.pausePlay();
    };

    Recorder.prototype.resumePlay = function() {
        AIRMobile.recorder.resumePlay();
    };

    Recorder.prototype.retrieveAudioFileList = function(callback) {
        
        if (!callback) {
            throw new Error('This recorder does not support sync api call.');
        }

        // we remove the DOM event to be consistent with Firefox
        AIRMobile.recorder.retrieveAudioFileList(function(event) {
            callback(event.files);
        });
    };

    Recorder.prototype.retrieveAudioFile = function(fileIdentifier, callback) {

        if (!callback) {
            throw new Error('This recorder does not support sync api call.');
        }

        // we remove the DOM event to be consistent with Firefox
        AIRMobile.recorder.retrieveAudioFile(function(event) {
            callback(event.audioInfo);
        });
    };

    Recorder.prototype.clearAudioFileCache = function(callback) {
        AIRMobile.recorder.clearAudioFileCache(callback);
    };

    // public api
    SB.Mobile.Recorder = Recorder;

})(TDS.SecureBrowser);
