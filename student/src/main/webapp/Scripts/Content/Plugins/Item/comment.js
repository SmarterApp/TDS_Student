/*
Item Tool: Comment
*/

(function(CM) {

    function match(page, entity, content) {
        if (entity instanceof ContentItem) {
            var accProps = page.getAccProps();
            return accProps.hasStudentComments();
        }
        return false;
    }

    function Plugin_IC(page, entity) {
        
    }

    CM.registerEntityPlugin('item.comment', Plugin_IC, match);

    Plugin_IC.prototype.toggle = function () {
        var item = this.entity;
        console.log('Item Tool: Comment', item);
        CM.fireEntityEvent('comment', item);
    }

    Plugin_IC.prototype.load = function () {

        // check if we are only showing menu
        var accProps = this.page.getAccProps();
        if (accProps.showItemToolsMenu()) {
            return;
        }

        // add tool
        var messageLabel = CM.getCommentLabel();
        this.entity.addToolElement({
            classname: 'commentItem',
            text: Messages.get(messageLabel),
            fn: this.toggle.bind(this)
        });

    }

    Plugin_IC.prototype.showMenu = function (menu, evt) {
        var messageLabel = CM.getCommentLabel();
        menu.addMenuItem('entity', {
            text: Messages.get(messageLabel),
            classname: 'comment',
            onclick: { fn: this.toggle.bind(this) }
        });
    }

})(window.ContentManager);