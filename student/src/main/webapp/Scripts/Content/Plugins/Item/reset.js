/*
Item Tool: Reset
*/

(function (CM) {

    function match(page, entity, content) {
        if (entity instanceof ContentItem) {
            var accProps = page.getAccProps();
            return accProps.hasResponseReset();
        }
        return false;
    }

    function Plugin_GTR(page, entity) {

    }

    CM.registerEntityPlugin('item.reset', Plugin_GTR, match);

    // send reset request
    Plugin_GTR.prototype.send = function () {

        var item = this.entity;
        console.log('Item Tool: Reset', item);

        // TDS notification
        if (typeof (window.tdsRemoveResponse) == 'function') {
            window.tdsRemoveResponse(item.position);
        }
    }

    Plugin_GTR.prototype.load = function () {

        // check if should just use the menu
        var accProps = this.page.getAccProps();
        if (accProps.showItemToolsMenu()) {
            return;
        }

        // add tool
        var messageKey = 'TDSContentEventsJS.Label.ResetItem';
        this.entity.addToolElement({
            classname: 'removeResponse',
            text: Messages.get(messageKey),
            fn: this.send.bind(this)
        });

    }

    Plugin_GTR.prototype.showMenu = function (menu, evt) {
        var messageKey = 'TDSContentEventsJS.Label.ResetItem';
        menu.addMenuItem('entity', {
            text: Messages.get(messageKey),
            classname: 'resetItem',
            onclick: { fn: this.send.bind(this) }
        });
    }

})(window.ContentManager);