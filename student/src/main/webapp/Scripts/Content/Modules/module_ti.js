/*************/
/* TI EVENTS */
/*************/

(function(CM) {

    // load table for a table item
    function processItem(page, item) {

        // load table xml
        var table = new TDS.TableItem(item.position);
        table.loadXml(document, item.value);
        item.table = table;

        // Add components to keyboard navigation using ctrl-TAB
        var componentArray = table.getComponentArray();
        for (var i = 0; i < componentArray.length; ++i) {
            item.addComponent(componentArray[i]);
        }

    };

    // listen for when the item content is available
    CM.onItemEvent('available', function (page, item) {
        if (item.isResponseType('TableInput')) {
            processItem(page, item);
        }
    });

    var responseGetter = function (item, response) {
        if (item.table) {
            response.value = item.table.getResponseXml();
            response.isValid = item.table.isValid();
            response.isSelected = response.isValid;
        }
    };

    var responseSetter = function (item, value) {
        if (item.table && typeof item.table.loadResponseXml == 'function') {
            item.table.loadResponseXml(value);
        }
    };

    // register response getter/setter for tableinput
    CM.registerResponseHandler('TableInput', responseGetter, responseSetter);

})(ContentManager);
