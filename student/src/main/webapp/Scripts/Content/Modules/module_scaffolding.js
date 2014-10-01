/*
This module is used for loading Alt Scaffolding items (QTI).
*/

(function (ContentManager) {

    // wait for available event and check if a scaffolding item
    ContentManager.onItemEvent('available', function(page, item) {

        // check if scaffolding
        if (!item.isResponseType('Scaffolding') &&
            !item.isResponseType('Scaffolding Vertical')) {
            return;
        }

        var pageDoc = page.getDoc();
        var container = pageDoc.getElementById('ScaffoldingContainer_' + item.position);
        
        // check voice guide
        var accProps = page.getAccommodationProperties();
        var voiceGuidance = accProps.hasASIVoiceGuidance();
        
        // If Voice Guidance is OFF then the scaffolding items continue to work the same with the exception that there is no audio playback on the items (no-auto queuing of audio and no speaker buttons anywhere).  The teacher is going to use their own voice (like they would on the paper/pencil assessment) to instruct the student on what to do.
        // I believe that when this is turned off then the item will need to be presented in it's entirety (stem with all options) immediately because there is no audio to wait for for presentation OR we just cycle through like we did before, but without playing the audio (whichever is easier).
        
        // remove exitAudio from stem 
        var stem = item.getStemElement();
        if (stem) {
            var exitAudioCue = processExitAudioLink(stem, voiceGuidance);
            var exitAudioPlayer = new AsiItem.AudioInterface();
            var exitAudioPlayerId = 'asi-' + item.position + 'exitAudio';
            exitAudioPlayer.add(exitAudioCue, exitAudioPlayerId);
        }

        var onTerminate = function (type, args, instance) {

            var questionSpan = container;

            while (questionSpan) {
                if ((/div/i.test(questionSpan.tagName)) && (YUD.hasClass(questionSpan, 'theQuestions'))) {
                    break;
                } else {
                    questionSpan = questionSpan.parentNode;
                }
            }

            // As a bit of a hack, we need to put a class on a specific part of the 
            // container in order to show when the question is done.  The reason we put it
            // on the 'questions' part of the div has to do with the MC baggage which we've 
            // inherited.
            if (questionSpan) {
                var completeDiv = document.createElement('div');
                YUD.addClass(completeDiv, 'asi-complete-span');
                questionSpan.parentNode.insertBefore(completeDiv, questionSpan);

                // The big 'complete' button should advance to the next item
                var nextFunction = ContentManager.setItemCompleted;
                if (nextFunction && item) {
                    YUE.addListener(completeDiv, 'click', function() { nextFunction(item); });
                }
            }
            exitAudioPlayer.start();
        };

        ContentManager.log('ASI: Processing item. ');
        var asi = new AsiItem.Parse(item);
        asi.createAsiChoices();
            
        // get the container and create ASI]
        var html = new AsiItem.Html(asi, container);
        item.scaffolding = new AsiItem.Interaction(html, asi, voiceGuidance);
        item.scaffolding.isReadOnly = item.isReadOnly;
        item.scaffolding.onTerminate(onTerminate);
            
        // build html before restoring response.            
        // Bug 117669 - restore the response after starting the interaction
        item.scaffolding.startInteraction(item.value);                    
    });
    
    // We clean up display on hide, so there is nothing that needs to be done on 'show' - 
    // showing the page is taken care of by shell plumbing.
    ContentManager.onItemEvent('hide', function (page, item) {
        if (item.scaffolding) {
            // quiet the item and normalize the state variables.
            item.scaffolding.hidePage();
        }
    });
})(ContentManager);

function processExitAudioLink(parentSpan, hasVoiceGuide) {
    var anchors = parentSpan.getElementsByTagName('a');
    for (var i = 0; i < anchors.length; i++) {
        var anchor = anchors[i];
        var source = Util.Audio.getPlayableSource(anchor);
        var anchorExitAudioAttr = YUD.getAttribute(anchor, 'data-its-exitaudio');
        if (anchorExitAudioAttr == 'true' && source != null) {
            anchor.parentNode.removeChild(anchor);
            return hasVoiceGuide ? source.url : null;
        }
    }
    return null;
};

// register response getter and setter for Edit questions
(function () {
    // response handler for EditTask questions (choice not here yet)
    var getter = function (item, response) {
        var value = '';
        if (item && item.scaffolding) {            
            value = item.scaffolding.getResponse();
        }

        response.value = value;
        response.isAvailable = true;

        var validResponse = (value && (value.length > 0) && (item.scaffolding) && (item.scaffolding.complete)) ? true : false;
        
        var str = 'ASI: getter for item ' + item.position + '. valid =' + validResponse + ' ';
        if (response.value)
            str = str + value;

        ContentManager.log(str);
        response.isSelected = validResponse;
        response.isValid = validResponse;
    };

    // response handler for ASI questions
    var setter = function (item, value) {
        if (item && item.scaffolding) {
            item.scaffolding.setResponse(value);
        }
    };

    ContentManager.registerResponseHandler('Scaffolding', getter, setter);
    ContentManager.registerResponseHandler('Scaffolding Vertical', getter, setter);
})();