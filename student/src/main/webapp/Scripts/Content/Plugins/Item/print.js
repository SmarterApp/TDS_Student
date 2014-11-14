/*
Item Tool: Print 
NOTE: To test this in itempreview add querystring "printable=true"
*/

(function(CM) {

    function match(page, entity, content) {
        // check if item and it is printable
        if (entity instanceof ContentItem && content.printable) {
            // check if the print acc is enabled for items
            var accProps = page.getAccProps();
            return accProps.hasPrintItem();
        }
        return false;
    }

    function Plugin_Print(page, entity) {
        this.element = null;
    }

    CM.registerEntityPlugin('item.print', Plugin_Print, match);

    // send print request
    Plugin_Print.prototype.send = function() {

        var item = this.entity;
        console.log('Item Tool: Print', item);

        // TDS notification
        if (typeof (window.tdsItemPrint) == 'function') {
            window.tdsItemPrint(item.position);
        }
    };

    Plugin_Print.prototype.load = function() {

        // check if we are only showing menu
        var accProps = this.page.getAccProps();
        if (accProps.showItemToolsMenu()) {
            return;
        }

        // add tool
        var messageKey = 'TDSContentEventsJS.Label.PrintItem';
        this.element = this.entity.addToolElement({
            classname: 'printItem',
            text: Messages.get(messageKey),
            fn: this.send.bind(this)
        });
    };

    Plugin_Print.prototype.showMenu = function(menu, evt) {
        var messageKey = 'TDSContentEventsJS.Label.PrintItem';
        menu.addMenuItem('entity', {
            text: Messages.get(messageKey),
            classname: 'printItem',
            onclick: { fn: this.send.bind(this) }
        });
    };

    Plugin_Print.prototype.dispose = function() {
        if (this.element) {
            $(this.element).remove();
            this.element = null;
        }
    };

})(window.ContentManager);