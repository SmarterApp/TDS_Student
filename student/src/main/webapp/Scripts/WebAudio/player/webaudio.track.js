﻿(function (window, webAudio) {
    "use strict";

    function Track(audioBuffer) {
        this.audioBuffer = audioBuffer;

        Object.defineProperties(this, {
            audioBuffer: { value: audioBuffer, writeable: false, enumerable: true, configurable: false },
            sampleRate: {
                enumerable: true, configurable: false,
                get: function () {
                    return this.audioBuffer.sampleRate;
                }
            }
        });
    };

    Track.fromPCM = function (channelData, sampleRate, context) {
        var numberOfChannels = channelData.length,
            audioBuffer = context.createBuffer(numberOfChannels, channelData[0].length, sampleRate),
            channel;

        for (channel = 0; channel < numberOfChannels; ++channel) {
            audioBuffer.getChannelData(channel).set(channelData[channel], 0);
        }

        return new Track(audioBuffer);
    };

    Track.prototype.toPCM = function () {
        var numberOfChannels = this.audioBuffer.numberOfChannels,
            channelData = [],
            currentChannel, channel;

        for (channel = 0; channel < numberOfChannels; ++channel) {
            currentChannel = this.audioBuffer.getChannelData(channel);
            channelData[channel] = new Float32Array(currentChannel.length);
            channelData[channel].set(currentChannel, 0);
        }

        return {
            channelData: channelData,
            sampleRate: this.audioBuffer.sampleRate
        };
    };

    Track.prototype.toAudioBuffer = function (context) {
        return this.audioBuffer;
    };

    Track.fromArrayBuffer = function (buffer, context, format, callback, errorCallback) {
        var success, failure;

        success = function (audioBuffer) {
            var track = new Track(audioBuffer);
            callback(track);
        };

        failure = function () {
            webAudio.encoding.decode(buffer, format, function (track) {
                callback(track);
            }, function (error) {
                if (errorCallback) {
                    errorCallback(error);
                }
            });
        };

        // first we see if the browser supports decoding the audio natively,
        // then we attempt to decode it with our own decoder
        context.decodeAudioData(buffer, success, failure);
    };

    Track.fromBlob = function (blob, context, callback) {
        var reader = new FileReader();

        reader.addEventListener('loadend', function () {
            // TODO: sniff format form blob mime type
            Track.fromArrayBuffer(reader.result, context, null, callback);
        });

        reader.readAsArrayBuffer(blob);
    };

    // exports
    webAudio.Track = Track;

})(window, webAudio);
