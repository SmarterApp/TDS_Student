/*
TDS ContentManager module. Does not work standalone.
*/

(function (CM) {

    // load table for a table item
    function processItem(page, item) {

        // check for table
        var itemEl = item.getElement();
        var tableEls = $('table.tableItem', itemEl).first();
        if (tableEls.length == 0) return; // no table found
        var tableEl = tableEls[0];

        // move table into response area
        var tableContainer = document.getElementById('TableContainer_' + item.position);
        if (tableContainer) {
            tableContainer.appendChild(tableEl);
        }

        // render table inputs
        var useTextArea = item.isResponseType('TableInputExt');
        var ti = new TDS.TableInput(tableEl, useTextArea);
        ti.render();

        // load existing value
        if (item.value) {
            ti.setResponse(item.value);
            ti.validateInputs();
        }

        // keep table widget
        item.table = ti;

        // get all the input elements and add them as components
        var inputEls = ti.getInputs();
        for (var i = 0; i < inputEls.length; ++i) {
            item.addComponent(inputEls[i]);
        }

        var readOnlyFunc = function (evt) {
            if (item.isReadOnly()) {
                YUE.stopEvent(evt);
            }
        };

        YUE.on(tableContainer, 'keypress', readOnlyFunc);
        YUE.on(tableContainer, 'mousedown', readOnlyFunc);
    };

    // listen for when the item content is available
    CM.onItemEvent('available', function (page, item) {
        if (item.isResponseType('TableInput') ||
            item.isResponseType('TableInputExt')) {
            processItem(page, item);
        }
    });

    var responseGetter = function (item, response) {
        if (item.table) {
            response.value = item.table.getResponse();
            response.isValid = item.table.isResponseValid();
            response.isSelected = response.isValid;
        }
    };

    var responseSetter = function (item, value) {
        if (item.table) {
            item.table.setResponse(value);
        }
    };

    // register response getter/setter for tableinput
    CM.registerResponseHandler('TableInput', responseGetter, responseSetter);
    CM.registerResponseHandler('TableInputExt', responseGetter, responseSetter);

})(ContentManager);
