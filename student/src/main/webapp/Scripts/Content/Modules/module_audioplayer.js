/*
This code takes the <a> audio links and makes them widgets.
*/

(function (Audio) {

    var Player = Audio.Player;
    var Recorder = Audio.Recorder;
    var Widget = Audio.Widget;

    // subscribe to when the iframes item(s) loads (we need look for audio player links)
    function onItemAudioAvailable(page, item) {

        var itemEl = item.getElement();
        if (itemEl == null) {
            return;
        }

        YUD.batch(itemEl.getElementsByTagName('a'), function (linkEl) {

            /*
            if visible is true and autoplay is no, use "sound_explicit"
            if visible is true and autoplay is yes, use "sound_repeat"
            if visible is false and autoplay is yes, use "sound_cue"
            */

            // skip any links that aren't audio
            if (!YUD.hasClass(linkEl, 'sound_explicit') &&
                !YUD.hasClass(linkEl, 'sound_repeat') &&
                !YUD.hasClass(linkEl, 'sound_cue')) {
                return;
            }

            // HACK: There is only styles for 'sound_repeat' and if we do try and target 'sound_explicit' 
            // with CSS we might affect sound icons in options which use 'sound_explicit'
            if (YUD.hasClass(linkEl, 'sound_explicit')) {
                YUD.addClass(linkEl, 'sound_repeat');
            }

            // BUG: #22685: After clicking on audio button, hitting ENTER to select an answer option play/stop audio
            ContentManager.preventFocus(linkEl);

            // create audio widget
            var audioID;
            var accProps = page.getAccommodationProperties();
            if (accProps && accProps.isAudioRewindEnabled()) {
                audioID = Widget.createPlayerRewind(linkEl);
            } else {
                audioID = Widget.createPlayer(linkEl);
            }

            // check if widget was created
            if (audioID) {

                // track widget id created for this item (not really used)
                item.audioLinks.push(audioID);

                // if autoplay is set true then add into queue
                // BUG 119152: Don't autoplay in accessibility shell
                if (!ContentManager.isAccessibilityEnabled()) {
                    var autoplay = YUD.getAttribute(linkEl, 'autoplay');
                    autoplay = (autoplay) ? (autoplay.toLowerCase() == 'true') : false;
                    if (autoplay) {
                        page.autoPlayQueue.append(audioID);
                    }
                }

            } else {
                YUD.addClass(linkEl, 'disabled');
            }
        });
    }

    // this is called when the context menu is requested
    function onItemAudioContextMenuPlayer(page, item, menu) {

        // check if we are recording or playing back a recording
        if (Recorder.isCapturing() || Recorder.isPlaying()) {
            return;
        }

        // add play menu item based on link
        var addPlayerLink = function (level, title, linkEl) {

            if (linkEl == null) {
                return;
            }

            var isLinkPlaying = YUD.hasClass(linkEl, 'playing_start');

            // if audio is playing but it is not this link then don't add any menus
            if (Player.isPlaying() && !isLinkPlaying) {
                return;
            }

            // meun text
            var menuLabel = (isLinkPlaying ? Messages.get('TDSAudioJS.Label.Stop') : Messages.get('TDSAudioJS.Label.Play')) + ' ' + title;

            // menu function
            var menuFunc = function () {
                if (isLinkPlaying) {
                    Player.stop(linkEl.id);
                } else {
                    Player.play(linkEl.id);
                }
            };

            // menu css
            var menuClass = isLinkPlaying ? 'stopquestion' : 'playquestion';

            // menu object
            var menuItem = {
                text: menuLabel,
                onclick: { fn: menuFunc },
                classname: menuClass
            };

            // assign menu item to context menu
            menu.addMenuItem(level, menuItem);
        };

        // add play menu item based on element
        var addPlayerElement = function (title, el) {

            if (el == null) {
                return;
            }

            var linkEls = el.getElementsByTagName('a');

            // count of audio widgets
            var audioCount = 0;

            // loop through each link for this item
            YUD.batch(linkEls, function (linkEl) {
                // make sure this is audio link
                if (!YUD.hasClass(linkEl, 'sound_explicit') && !YUD.hasClass(linkEl, 'sound_repeat')) {
                    return;
                }

                audioCount++;

                addPlayerLink('entity', title, linkEl);
            });
        };

        // AUDIO: Stem/Illustration
        if (item.getActiveComponent() == item.getStemElement() ||
            item.getActiveComponent() == item.getIllustrationElement()) {
            // NOTE: "TDSAudioJS.Label.AddMenuQuestion" text is "Question"
            addPlayerElement(Messages.get('TDSAudioJS.Label.AddMenuQuestion'), item.getStemElement());
            addPlayerElement(Messages.get('TDSAudioJS.Label.AddMenuQuestion'), item.getIllustrationElement());
        }

        // AUDIO: MC Option
        if (item.MC) {
            // check if there is a focused MC option
            var optionGroup = item.MC;
            var focusedOption = optionGroup.getFocusedOption();

            if (focusedOption) {
                // NOTE: "TDSAudioJS.Label.AddMenuOption" text is "Option"
                addPlayerLink('component', Messages.get('TDSAudioJS.Label.AddMenuOption') + ' ' + focusedOption.key, focusedOption.getSoundLink());
            }
        }
    }

    // apply event handlers for audio (NOTE: use passage/item instead of entity due to ordering issues) 
    ContentManager.onPassageEvent('available', onItemAudioAvailable);
    ContentManager.onItemEvent('available', onItemAudioAvailable);
    ContentManager.onItemEvent('menushow', onItemAudioContextMenuPlayer);

    // listen for when page is hidden
    ContentManager.onPageEvent('hide', function (page) {

        // if we are playing audio stop it
        if (Player.isPlaying()) {
            Player.stopAll();
        }

    });

})(TDS.Audio);
