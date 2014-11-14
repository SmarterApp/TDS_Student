﻿/*
In previous version of ContentManager it was a singleton. All the page came through
that object. Here we are simulating the old functions for legacy users. 
NOTE: This file needs to be last so everyone can listen for 'createPages' event.
*/
(function (CM) {

    var pages = CM.createPages('contents');

    CM.createPage = function (content) {
        return pages.create(content);
    }

    CM.getPage = function (id) {
        return pages.get(id);
    }

    CM.getPages = function () {
        return pages.list();
    }

    CM.getCurrentPage = function() {
        return pages.getCurrent();
    }

    CM.getCurrentEntity = function() {
        var page = CM.getCurrentPage();
        return page ? page.getActiveEntity() : null;
    }

    CM.removePage = function(page) {
        return pages.remove(page);
    }
    
    /////////////////////////////////////////////////

    // Enable/disable caret mode (call with no arguments to toggle mode)
    CM.enableCaretMode = function (enable) {
        var enabled = Mozilla.enableCaretMode(enable);
        if (enabled) {
            var page = pages.getCurrent();
            if (page) {
                var entity = page.getActiveEntity();
                if (entity) {
                    entity.resetCaretPosition();
                }
            }
        }
    };

    // this resets the active component for an item
    CM.resetActiveComponent = function () {
        var page = pages.getCurrent();
        if (page) {
            var entity = page.getActiveEntity();
            if (entity instanceof ContentItem) {
                entity.resetComponent();
            }
        }
    };

    /////////////////////////////////////////////////

    // Send a request asking if we can go to the next page. 
    // Anyone listening to this event can return false which means do not leave.
    // If verify is true then anyone listening should not take any action. 
    CM.requestNextPage = function (verify) {
        var page = CM.getCurrentPage();
        if (page) {
            return CM.fire('requestNextPage', page, verify);
        } else {
            return true;
        }
    };

    // Send a request asking if we can go to the previous page. 
    // Anyone listening to this event can return false which means do not leave.
    // If verify is true then anyone listening should not take any action. 
    CM.requestPreviousPage = function (verify) {
        var page = CM.getCurrentPage();
        if (page) {
            return CM.fire('requestPreviousPage', page, verify);
        } else {
            return true;
        }
    };

    // Call this function to set an item as being completed.
    // NOTE: This is required by scaffolding. 
    CM.setItemCompleted = function (item) {
        if (!item) return;
        // fire completed event for this item
        CM.fireEntityEvent('completed', item);
        // fire completed event for this page if it only has one item
        var page = item.getPage();
        var items = page.getItems();
        if (items.length == 1) {
            // BUG: What about greater than one item? In the future we should track 'completed'. 
            CM.firePageEvent('completed', page);
        }
    };

})(ContentManager);

(function (CP) {

    CP.prototype.addResourceLoader = function (resource) {
        this._renderer.addResourceLoader(resource);
    }

})(ContentPage);
