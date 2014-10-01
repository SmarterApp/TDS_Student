/*
Code used for showing comments.
*/

(function (CM, TS) {

    // listen for item comments
    CM.onItemEvent('comment', function (page, item) {
        TS.Comments.showItem(item);
    });

    // Close Comments
    CM.onPageEvent('beforeHide', function () {
        TestShell.Comments.hide();
    });

    // setup comments
    function load() {

        TS.Comments.init();

        // listen for global comments
        TS.UI.addClick('btnGlobalNotes', function() {
            TS.Comments.showGlobal();
        });
    }

    // listen for test shell init (needs to work on legacy and geo)
    TS.Events.subscribe('init', load);

})(ContentManager, TestShell);
