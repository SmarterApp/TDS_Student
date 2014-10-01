// This module is used for setting up moogle

(function() {

    // load the moogle search ui
    function loadMoogle(passage, results) {
        var moogle = new TDS.Moogle('moogle' + passage.getID());
        var passageContainer = passage.getElement();
        moogle.load(passageContainer, results);
    }

    // listen for when the passage is available
    ContentManager.onPassageEvent('available', function (page, passage) {

        // check for specs
        if (passage.specs && passage.specs.length > 0) {

            // try and find moogle xml
            var results = Util.Array.find(passage.specs, function (spec) {
                return spec.nodeName == 'search';
            });

            // if moogle was found then begin loading search ui
            if (results) {
                loadMoogle(passage, results);
            }
        }
    });

})();