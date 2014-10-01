ContentManager.onItemEvent('available', function (page, item) {
    if (!item.isResponseType('simulator')) return;

    var pageDoc = page.getDoc();
    var simContainer = pageDoc.getElementById('SimContainer_' + item.position);

    var simXml = item.rendererSpec;

    var createSimComponent = function (simulator, simContainer) {
        var simComponent =
        {
            id: 'SIM_' + item.position,
            focus: function () { Util.Dom.focus(simContainer); },
            blur: function () { Util.Dom.blur(simContainer); }
        };

        //        // subscribe to when sim frame is focused and set as active component
        //        YUE.onFocus(simDoc, function () {
        //            item.setActiveComponent(simComponent);
        //        });

        item.addComponent(simComponent); // simDoc.body

        YUE.on(simContainer, 'click', function () {
            item.setActiveComponent(simComponent);
        });
    };

    // begins loading the simulators resources (images and flash)
    var simImageFiles = SimulationLoader.parseImages(simXml);

    // add resources to loader
    for (var i = 0; i < simImageFiles.length; i++) {
        var loaderImage = new ResourceLoader.Image(simImageFiles[i]);
        page.addResourceLoader(loaderImage);
    }

    var simSWFFiles = SimulationLoader.parseFlash(simXml);

    for (var i = 0; i < simSWFFiles.length; i++) {
        var loaderBinary = new ResourceLoader.Binary(simSWFFiles[i]);
        page.addResourceLoader(loaderBinary);
    }

    var responseXml;

    // if this is ran within TDS then ask TDS for the response..
    if (typeof (window.TestShell) == 'object' && typeof (window.TestShell.PageManager) == 'object') {
        var response = window.TestShell.PageManager.getResponse(item.position);
        responseXml = response.getLastValue();
    }
    // otherwise use whatever the item has..
    else {
        responseXml = item.value;
    }

    // load sim xml
    var simulator = new Simulator.Simulator(simContainer, ContentManager.isAccessibilityEnabled());
    Simulator.Animation.FlashAnimationInterface.MapInstance(simulator);
    simulator.setAnimationShellPath(ContentManager.resolveBaseUrl('Scripts/Simulator2/Renderer/SWF/SimulationShell.swf'));
    simulator.setAnimationExternalScriptsPath(ContentManager.resolveBaseUrl('Scripts/Libraries'));
    simulator.loadXml(simXml, responseXml);
    item.simulator = simulator;

    // add TDS specific features to sim doc
    var simWin = simulator.getWin();
    var simDoc = simulator.getDoc();

    // add fixes to frame
    //ContentManager.fixItemFrame(item, simWin, simDoc);

    // create sim component
    createSimComponent(simulator);

    // add highlight style
    //Util.Style.installStyles(simDoc, '.highlight { background-color: #F6FF00 !important; } ');

    // call this to force zoom update
    var simZoomUpdate = function () {
        var zoomInfo = page.getZoom();
        zoomInfo._setDocumentLevel(simDoc, zoomInfo.currentLevel, true);
    };

    var zoomImage = function (img) {
        // we need to add 'Image' for it to be zoomable
        YUD.addClass(img, 'Image');
        simZoomUpdate();
    };

    // Check all images.
    YUD.batch(simDoc.getElementsByTagName('img'), function (img) {
        // Check if image has loaded already.
        // (If it is cached or fast enough this will be true.)
        if (Util.Dom.isImgLoaded(img)) {
            zoomImage(img);
        }
        else {
            // Wait for image to load.
            // (We have to wait otherwise the width/height will be the
            // dimensions of the inline DOM element and not the image.)
            YUE.on(img, 'load', function () {
                zoomImage(img);
            });
        }
    });

    // check for when image gets added (poster, simulatation result)
    simulator.subscribe('info', 'imageEmbedded', function (evt) {
        // console.info('imageEmbedded');
        if (evt.data) YUD.addClass(evt.data, 'Image');
        simZoomUpdate();
    });

    // check for when flash gets added
    simulator.subscribe('info', 'animationEmbedded', function (evt) {
        // console.info('animationEmbedded');
        simZoomUpdate();

        VideoManager.SWF.stopRightClick(evt.data);
    });

    // check for state changes
    simulator.subscribe('info', 'simulatorStateChange', function (evt) {
        // console.info('simulatorStateChange: ' + evt.data);

        if (YAHOO.lang.isString(evt.data)) {
            var eventType = evt.data.toUpperCase();

            // check if sim is ready
            if (eventType == "READY") {
                // if we are in read only mode then set sim as locked
                if (ContentManager.isReadOnly()) {
                    simulator.setReadOnlyState(true);
                }

                // remove loading screen
                YUD.removeClass(simContainer, 'loading');
            }
            // check if sim has an error
            else if (eventType == "ERROR") {
                // add error screen
                YUD.addClass(simContainer, 'failed');
            }
        }
    });

    // subscribe to sim window onerror
    if (typeof TDS.Diagnostics == 'object') {
        TDS.Diagnostics.addErrorHandler(simWin);
    }

});

// add "Image" class to all sim images so zooming will work
/*
ContentManager.onItemEvent('beforeZoom', function(page, item)
{
    if (item.simulator == null) return;

    var simDoc = item.simulator.getDoc();
    var simImages = simDoc.getElementsByTagName('img');
    
    YUD.batch(simImages, function(simImage)
    {
        YUD.addClass(simImage, 'Image');
    });
});
*/

ContentManager.onItemEvent('zoom', function(page, item)
{
    if (item.simulator == null) return;

    // get current zoom factor
    var zoomInfo = page.getZoom();
    var zoomFactor = (zoomInfo == null) ? 1 : zoomInfo.levels[zoomInfo.currentLevel].factor;

    item.simulator.zoom(zoomFactor);
});

ContentManager.onItemEvent('show', function (page, item) {
    if (!item.simulator) return;

    item.simulator.setVisibilityAndAttachEvents(true);

});

ContentManager.onItemEvent('hide', function (page, item) {
    if (!item.simulator) return;

    item.simulator.setVisibilityAndAttachEvents(false);
});

ContentManager.onItemEvent('focus', function (page, item) {
    // notify a simulation item when it is on focus
    if (item.simulator) {
        item.simulator.focus();
    }
});

ContentManager.onItemEvent('blur', function (page, item) {
    // notify a simulation item after it loses focus
    if (item.simulator) {
        item.simulator.blur();
    }
});

// register response getter/setter for java grid
(function()
{
    var getter = function(item, response)
    {
        if (item.simulator == null) return;

        response.value = item.simulator.getResponseXml();
        response.isValid = item.simulator.isValid();
        response.isSelected = response.isValid;
    };

    var setter = function(item, value)
    {
        if (item.simulator == null) return;
        item.simulator.loadResponseXml(value);
    };

    ContentManager.registerResponseHandler('simulator', getter, setter);
})();
