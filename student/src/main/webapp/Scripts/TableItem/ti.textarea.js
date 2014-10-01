TDS.TableItem = (typeof (TDS.TableItem) == "undefined") ? {} : TDS.TableItem;

// Logic for a textarea in a table item.
TDS.TableItem.TextArea = function() {

    // Instance variables
    var textAreaId;
    // validation type and message mapping
    var typeInputAllowedMessage = [];
    typeInputAllowedMessage['alphaOnly'] = 'letters only.';
    typeInputAllowedMessage['numericOnly'] = 'numbers only.';
    typeInputAllowedMessage['alphaNumericOnly'] = 'letters or numbers only.';
    typeInputAllowedMessage['textOnly'] = 'text only.';

    this.render = function(tableId, inputSpan, txtAreaIdNum, typeInputAllowed, countOfTextAreas, scrngTable, colNmbr, rowNmbr) {
        var inputTextArea = document.createElement('textarea');
        YUD.addClass(inputTextArea, 'ti ti-textarea');

        textAreaId = txtAreaIdNum;
        inputTextArea.id = tableId + '-inputcell-' + textAreaId;

        YUE.addListener(inputTextArea, 'keypress', function(event) {
            var validKey = TDS.TableItem.TextArea.onKeyPressHandler(tableId, textAreaId, event, typeInputAllowed, countOfTextAreas, inputTextArea);
            if (!validKey) {
                TDS.TableItem.showAlertWarning("Please enter " + typeInputAllowedMessage[typeInputAllowed]);
                inputTextArea.focus();
            }
        });

        // resize textarea
        YUE.addListener(inputTextArea, 'keyup', function() {
            inputTextArea.style.height = 'auto';
            inputTextArea.style.height = inputTextArea.scrollHeight / 16 + 'em';
        });

        YUE.addListener(inputTextArea, 'blur', function() {
            // polulate filled td's
            scrngTable.setValue(colNmbr, rowNmbr, inputTextArea.value);
            // update column header id name pair
            if (rowNmbr == 0) {
                TDS.TableItem.getColumnHeaderIdNamePair()[colNmbr] = inputTextArea.value;
            }
        });

        inputSpan.appendChild(inputTextArea);
        // populate td's not filled, except header row
        scrngTable.setValue(colNmbr, rowNmbr, inputTextArea.value);
    };

    TDS.TableItem.TextArea.onKeyPressHandler = function(tblId, id, event, typeInputAllowed, size, inputTxtArea) {
        // navigate from the current textarea to the next or previous textarea
        var nextFocEle = document.getElementById(tblId + '-inputcell-' + ((id + 1) % size));
        var previousFocEle = document.getElementById(tblId + '-inputcell-' + ((id - 1) % size));
        // next input cell: ctrl+Tab: 
        if (event.ctrlKey && event.keyCode == 9) {
            inputTxtArea.blur();
            nextFocEle.focus();
        }
        // previous input cell: shift+ctrl+Tab: 
        if (event.shiftKey && event.ctrlKey && event.keyCode == 9) {
            if (id > 0) {
                inputTxtArea.blur();
                previousFocEle.focus();
            } else {
                inputTxtArea.focus();
            }
        }

        // validate user input
        var char0, match;
        if (event.which == null) {
            char0 = String.fromCharCode(event.keyCode); // old IE
        } else if (event.which != 0 && event.charCode != 0) {
            char0 = String.fromCharCode(event.which); // All others
        } else {
            return true;
        }
        switch (typeInputAllowed) {
        case 'alphaOnly':
            match = char0.match(/[a-z\.]/i);
            return (match != null && match.length > 0);
        case 'numericOnly':
            match = (inputTxtArea.value.length == 0 && char0.match(/[0-9\+\-\.]/))
                || (inputTxtArea.value.length > 0 && ((inputTxtArea.value.indexOf('.') == -1 && char0.match(/[0-9\.]/))
                                                || (inputTxtArea.value.indexOf('.') != -1 && char0.match(/[0-9]/))));
            return (match != null && match.length > 0);
        case 'alphaNumericOnly':
            match = char0.match(/[a-z0-9\.\+\-]/i);
            return (match != null && match.length > 0);
        case 'textOnly':
            return true;
        default:
            return true; // allow all characters
        }
    };
};