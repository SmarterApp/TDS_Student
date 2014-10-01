/** **************************************************************************
* @class DataTable
* @superclass DataDisplayElement
* @param sim
* @return DataTable instance
* Concrete Class to display data in the output panel.
*****************************************************************************
*/
Simulator.Display.DataTable = function (sim, panel) {

    Simulator.Display.DataDisplayElement.call(this, sim);

    //private variables - start here

    var source = 'DataTable';
    var iteratorSelectorMap = [];
    var rowFunctionMap = [];
    var headingMap = [];
    var headingNames = [];
    var autoCountMap = [];
    var scoreableMap = [];
    var numFiguresMap = [];
    var includeKeyInOutput = [];
    var columnTextLengthMap = [];
    var hasOutputKeys = false;

    var colWidth = -1;
    var whiteboardKey = null;
    var userAlerted = false;
    var deleteColumn = false;
    var numRows = undefined;
    var htmlTableRendered = false;
    var userAddRows = true;
    var numColumns = undefined;
    var header = true;
    var incrementTrialOnOutput = false;
    var clearRows = false;
    var footer = false;
    var scoreable = false;
    var headerVisible = true;
    var outputSource = 'evaluator';
    var deleteRowImage = '';
    var prevBorder = 'none';
    var simID = null;

    var instance = this;

    if (sim) {
        simID = sim.getSimID();
    }

    var HTMLPanel = panel.getHTMLElement();

    //private variables - end here

    //private functions - start here

    var keyboardInput = function () { return sim.getKeyboardInput(); };

    var util = function () { return sim.getUtils(); };

    var simMgr = function () { return sim.getSimulationManager(); };

    var eventMgr = function () { return sim.getEventManager(); };

    var dbg = function () { return sim.getDebug(); };

    var speechGrammarBldr = function () { return sim.getSpeechGrammarBldr(); };

    var whiteboard = function () { return sim.getWhiteboard(); };

    var scoringTable = function () { return sim.getScoringTable(); };

    var simDocument = function () { return sim.getSimDocument(); };

    var HTMLDataTable = simDocument().createElement('table');
    HTMLDataTable.setAttribute('class', 'dataTable');
    HTMLDataTable.rules = 'all';

    function updateHTMLTable(newHTMLTable) {
        HTMLDataTable = newHTMLTable;
    }

    this.setFocusable(true, true);

    function addRow(table, loadFromResponse) {
        var newCell = null;
        var hTable = simDocument().getElementById(table.getID());
        var nRows = hTable.rows.length;
        var lastCellID = table.createCellId(hTable.tBodies[0].rows.length - 2, numColumns - 1);
        var newRow = hTable.insertRow(nRows);
        for (var i = 0; i < numColumns; i++) {
            newCell = newRow.insertCell(i);
            newCell.id = table.createCellId(hTable.tBodies[0].rows.length - 1, i);
            newCell.innerHTML = ' ';
            // add keyboard support for table cells
            if (table.isFocusable()) keyboardInput().addFocusableElementItem(table, table.getID(), newCell.id);
        }
        if (table.getClearRows()) {
            var dCell = newCell = newRow.insertCell(numColumns);
            dCell.style.borderRight = '0px';
            dCell.style.borderTop = '0px';
            dCell.style.borderBottom = '0px';
            dCell.id = table.createCellId(hTable.tBodies[0].rows.length - 1, numColumns);
            var anchor = simDocument().createElement('a');
            anchor.href = '#';
            anchor.setAttribute('class', 'clearRow');
            var img = simDocument().createElement('img');
            img.src = table.getDeleteRowImage();
            anchor.appendChild(img);
            anchor.onclick = (function (e) {
                var theRow = simMgr().getTrialNum() - 1;
                if (loadFromResponse) {
                    theRow = nRows - 1;
                }
                var id = table.getNodeID();
                return function (e) {
                    instance.clearRow(e, id, theRow);
                };
            })();
            dCell.appendChild(anchor);
            // add keyboard support for clear row buttons
            if (table.isFocusable()) keyboardInput().addFocusableElementItem(table, table.getID(), dCell.id);
        }
        updateHTMLTable(hTable);
        table.setNumRows(hTable.tBodies[0].rows.length);
    }

    function resolveColumn(inputKey, outputKey) {
        for (var i = 0; i < numColumns; i++) {  // try all table columns
            if (headingMap[i]['inputKey']) { // we are expecting an inputKey for this column
                if (inputKey) { // we have an inputKey from the user
                    if (headingMap[i]['inputKey'].toLowerCase() == inputKey.toLowerCase()) {  // inputKeys match; see if this column also expects an outputKey
                        if (outputKeyMatch(outputKey, i)) return i;  // both keys match or no output key expected
                        else return -1; // inputKey matches but output key does not
                    }
                }
            } else if (outputKey) { // no input key expected for this column see if there is an output key match
                if (outputKeyMatch(outputKey, i)) return i;  // outputKey matches
            }
        }
        return -1;  // no match on output key either for this column
    }

    function outputKeyMatch(outputKey, colNum) {
        if (headingMap[colNum]['outputKey']) {  // This column expects an output key
            if (outputKey) {  // we have received an outputKey so do the comparison			
                if (headingMap[colNum]['outputKey'][0] == '+') {  // first check to see of a compound key matches
                    if (headingMap[colNum]['outputKey'].substring(1) == outputKey) return true;
                }
                else {
                    theOutputKeys = headingMap[colNum]['outputKey'].split(',');  // there can be multiple output keys specified for a single column
                    for (var j = 0; j < theOutputKeys.length; j++) {  // See if any of the column outputKeys match the outputKey parameter
                        anOutputKey = theOutputKeys[j].replace(/^\s+/, '');  // get rid of any leading blanks
                        anOutputKey = anOutputKey.replace(/\s+$/, '');  // get rid of any trailing blanks
                        if (anOutputKey.toLowerCase() == outputKey.toLowerCase()) return true;  // output keys match
                    } return false;  // outputKey from user did not match any output keys specified for this column
                }
            }
        } else {
            if (outputKey) return false;  // we have a key but no key expected by the column
            else return true; // no key expected and no key passed to the function so return true
        }
    }

    function parseTableInput(obj, inputs) {
        var inputArray = [];
        var elements = null;
        var parts = null;
        var cellWritten = false;
        for (var p in inputs) {
            inputArray = inputs[p];
            if (inputArray) {
                for (var i = 0; i < inputArray.length; i++) {
                    if (inputArray[i]) {
                        elements = inputArray[i].split(Simulator.Constants.PAIR_DELIMITTER);
                        for (var k = 0; k < elements.length; k++) {
                            parts = elements[k].split(Simulator.Constants.KEY_VALUE_DELIMITTER);
                            if (parts[1]) cellWritten = obj.setCell(parts[0], parts[1]);
                            else cellWritten = obj.setCell(p, parts[0]);
                        }
                    }
                }
            }
        }
        if (cellWritten && !hasOutputKeys) {
            debug(source, 'cellWritten = ' + cellWritten + ', hasOutputKeys = ' + hasOutputKeys + '. Sending "tableUpdated" event');
            eventMgr().postEvent(new Simulator.Event(obj, 'info', 'tableUpdated'));
        }
    }

    function parseOutput(obj, outputs) {
        var parts = [];
        var functionList = [];
        var functionDB = [];
        var iterations = [];
        if (outputs == '' || outputs == ';') {
            dbg().logError(source, 'Improper output parameter received in DataTable.parseOutput: ' + outputs + '');
            return;
        }
        iterations = outputs.split(Simulator.Constants.ITERATION_DELIMITTER);
        for (var t = 0; t < iterations.length; t++) {
            //functionList = iterations[t].split(',');
            functionList = iterations[t].split(Simulator.Constants.PAIR_DELIMITTER);
            for (var fl = 0; fl < functionList.length; fl++) {
                //var index = functionList[fl].indexOf(':');
                parts = functionList[fl].split(Simulator.Constants.KEY_VALUE_DELIMITTER);
                parts[0] = util().removeHeadingAndTrailingQuotes(parts[0], 'both');  // remove double and single quotes
                parts[1] = util().removeHeadingAndTrailingQuotes(parts[1], 'both');  // remove double quotes
                if (t == 0)
                    functionDB[parts[0]] = [];
                if (parts[0])
                    functionDB[parts[0]].push(parts[parts.length - 1]);  // Must make sure to get the last member of parts 
            }
        }
        var cellWritten = false;
        for (var k = 0; k < numColumns; k++) {
            var allData = null;
            var inputKey = headingMap[k]['inputKey'];
            var outputKeys = headingMap[k]['outputKey'];
            if (outputKeys) {
                //if(compoundOutputKey(outputKeys)) {
                //cellWritten = obj.setCell(inputKey, outputs, outputKeys.substring(1));  // compoundOutputKey indicates all keys considered as a unit
                //break;
                //}
                //else {
                var outputKeyList = outputKeys.split(',');
                for (var i = 0; i < outputKeyList.length; i++) {
                    var data = null;
                    if (typeof outputKeyList[i] == 'Array') outputKeyList[i] = outputKeyList[i][0];
                    outputKeyList[i] = outputKeyList[i].replace(/^\s*/, "");
                    outputKeyList[i] = outputKeyList[i].replace(/\s*$/, "");
                    outputKeyList[i] = outputKeyList[i].replace(/^"\s*/, "\*");
                    outputKeyList[i] = outputKeyList[i].replace(/\s*\"$/, "\"");
                    outputKeyList[i] = outputKeyList[i].replace(/\"/g, "");
                    if (outputKeyList[i] in functionDB) {
                        if (iteratorSelectorMap[k]) {
                            data = util().applyFilter(iteratorSelectorMap[k], functionDB[outputKeyList[i]]);
                            functionDB[outputKeyList[i]] = data;  // Override the output with the filtered results
                        } else {
                            if (includeKeyInOutput[k]) {
                                functionDB[outputKeyList[i]] = outputKeyList[i] + ' : ' + functionDB[outputKeyList[i]];
                            }
                        }
                        data = functionDB[outputKeyList[i]];
                        if (data instanceof Array) data = data.join(',');
                        if (allData) allData = allData + ', ' + data;
                        else allData = data;
                        cellWritten = obj.setCell(inputKey, allData, outputKeyList[i]);
                    }
                }
            }
        }
        if (cellWritten) {
            obj.saveScoreableInputs();
            //if (incrementTrialOnOutput) simMgr().nextTrialNum();
            eventMgr().postEvent(new Simulator.Event(obj, 'info', 'tableUpdated'));
        }
    }

    function compoundOutputKey(outputs) {
        return outputs[0] == '+' ? true : false;
    }

    function postTableDataToWhiteboard(obj) {
        var dataSeriesID = null;
        whiteboard().addCategory('itemTableData');
        var cellID = null;
        var cell = null;
        var buff = [];
        for (var j = 0; j < numRows; j++) {
            for (var i = 0; i < numColumns; i++) {
                cellID = obj.createCellId(j, i);
                cell = simDocument().getElementById(cellID);
                if (i == 0) buff.push(cell.innerHTML);
                else {
                    buff.push(', ');
                    buff.push(cell.innerHTML);
                }
            }
            dataSeriesID = 'series' + (j + 1);
            if (!whiteboardKey) whiteboardKey = whiteboard().addItem('itemTableData', dataSeriesID);
            whiteboard().setItem('itemTableData', dataSeriesID, buff.join(''), whiteboardKey);
        }
    }

    function rowIsEmpty(tbl, theRow) {
        var nCols = tbl.getNumColumns();
        if (theRow < tbl.getNumRows() && theRow >= 0) {
            for (var i = 0; i < nCols; i++) {
                var id = tbl.createCellId(theRow, i);
                var cell = simDocument().getElementById(id);
                if (cell.innerHTML != '') return false;
            }
        }
        return true;
    }

    var createTableBody = function (tbl) {
        var tBody = simDocument().createElement('tbody');
        HTMLDataTable.appendChild(tBody);
        var row;

        tbl.setFocusable(true, false);

        for (var j = 0; j < numRows; j++) {
            row = simDocument().createElement('tr');
            tBody.appendChild(row);
            for (var i = 0; i < numColumns; i++) {
                var cell = simDocument().createElement('td');
                cell.id = tbl.createCellId(j, i);
                row.appendChild(cell);
                cell.innerHTML = '';
                //if (tbl.isFocusable()) keyboardInput().addFocusableElementItem(tbl, tbl.getNodeID(), cell.id);
                // add keyboard support for clear row buttons
                if (tbl.isFocusable()) keyboardInput().addFocusableElementItem(tbl, tbl.getNodeID(), cell.id);
            }
            if (tbl.getClearRows()) {
                var dCell = newCell = row.insertCell(row.cells.length);
                dCell.id = tbl.createCellId(j, numColumns);
                dCell.style.borderRight = '0px';
                dCell.style.borderTop = '0px';
                dCell.style.borderBottom = '0px';
                var anchor = simDocument().createElement('a');
                anchor.href = '#';
                anchor.setAttribute('class', 'clearRow');
                var img = simDocument().createElement('img');
                img.src = tbl.getDeleteRowImage();
                anchor.appendChild(img);
                anchor.onclick = (function (e) {
                    var theRow = j;
                    var id = tbl.getNodeID();
                    return function (e) {
                        instance.clearRow(e, id, theRow);
                    };
                })();
                dCell.appendChild(anchor);
                //if (simMgr().getSpeechEnabled()) dCell.innerHTML = 'Clear Row ' + j;
                //if (tbl.isFocusable()) keyboardInput().addFocusableElementItem(tbl, tbl.getNodeID(), dCell.id);
                // add keyboard support for clear row buttons
                if (tbl.isFocusable()) keyboardInput().addFocusableElementItem(tbl, tbl.getNodeID(), dCell.id);
            }
        }
        //if (simMgr().getSpeechEnabled()) createRowDeletionSpeechCommand(tbl);
        return;
    };

    var createRowDeletionSpeechCommand = function (tbl) {
        speechGrammarBldr().createTableRowClearRule(tbl.getName(), '', tbl, 'Data Table');
    };

    //private functions - end here

    this.setEname(source);

    //public functions - start here

    this.getHTMLTable = function () {
        return HTMLDataTable;
    };

    this.getID = function () {
        return HTMLDataTable.id;
    };

    this.setID = function (id) {
        HTMLDataTable.setAttribute('id', this.getNodeID());
    };

    this.getUserAddRows = function () {
        return userAddRows;
    };

    this.setUserAddRows = function (newUserAddRows) {
        if (newUserAddRows == 'true') userAddRows = true;
        else userAddRows = false;
    };

    this.getNumRows = function () {
        return numRows;
    };

    this.setNumRows = function (newNumRows) {
        numRows = parseInt(newNumRows);
        if (numRows == undefined) {
            dbg().logWarning(source, 'Could not convert string representation of number of data table rows to an integer');
        }
        if (numRows == 0) deleteColumn = true;
        return this;
    };

    this.getNumColumns = function () {
        return numColumns;
    };

    this.setNumColumns = function (newNumColumns) {
        numColumns = parseInt(newNumColumns);
        if (!numColumns) {
            dbg().logWarning(source, 'Could not convert string representation of number of data table columns to an integer');
        }
        colWidth = 85 / numColumns;
        dColWidth = (100 - (colWidth * numColumns)) + '%';
        return this;
    };

    this.getHeader = function () {
        return header;
    };

    this.setHeader = function (header) {
        var indexStr = '';
        var maxLength = 0;
        var k = -1;
        var cell = null;
        var headingText = null;
        var isIE = util().isInternetExplorer();
        if (header != null) {
            //if (simMgr().getSpeechEnabled()) HTMLPanel.innerHTML += 'Move To Data Table\n';
            var tHead = HTMLDataTable.createTHead();
            var row = simDocument().createElement('tr');
            tHead.appendChild(row);
            for (var i = 0; i < header.childNodes.length; i++) {
                try {
                    var child = header.childNodes[i];
                    if (child.nodeName[0] != '#') {
                        k += 1;
                        if (k <= numColumns) {
                            cell = simDocument().createElement('th');
                            row.appendChild(cell);
                            indexStr = 'text';
                            if (isIE) headingText = child.attributes.getNamedItem(indexStr).value;
                            else headingText = child.attributes[indexStr].nodeValue;
                            cell.innerHTML = headingText;
                            headingNames[k] = headingText;
                            headingMap[k] = [];
                            indexStr = "contentType";
                            headingMap[k].push([indexStr]);
                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr) != undefined)
                                    headingMap[k][indexStr] = child.attributes.getNamedItem(indexStr).value;
                                else
                                    headingMap[k][indexStr] = "text";
                            }
                            else {
                                if (child.attributes[indexStr] != undefined)
                                    headingMap[k][indexStr] = child.attributes[indexStr].nodeValue;
                                else
                                    headingMap[k][indexStr] = "text";  // default value                            
                            }

                            indexStr = "inputKey";
                            headingMap[k].push([indexStr]);
                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr) != undefined)
                                    headingMap[k][indexStr] = child.attributes.getNamedItem(indexStr).value;
                                else
                                    headingMap[k][indexStr] = null;
                            }
                            else {
                                if (child.attributes[indexStr] != undefined)
                                    headingMap[k][indexStr] = child.attributes[indexStr].nodeValue;
                                else
                                    headingMap[k][indexStr] = null;  // default value                            
                            }

                            indexStr = "outputKey";
                            headingMap[k].push([indexStr]);
                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr) != undefined) {
                                    headingMap[k][indexStr] = child.attributes.getNamedItem(indexStr).value;
                                    headingMap[k][indexStr] = headingMap[k][indexStr].replace(/\s*\"\s*/g, "\"");
                                    hasOutputKeys = true;
                                }
                                else
                                    headingMap[k][indexStr] = null;
                            }
                            else {
                                if (child.attributes[indexStr] != undefined) {
                                    headingMap[k][indexStr] = child.attributes[indexStr].nodeValue;
                                    headingMap[k][indexStr] = headingMap[k][indexStr].replace(/\s*\"\s*/g, "\"");
                                    hasOutputKeys = true;
                                }
                                else headingMap[k][indexStr] = null;
                            }




                            indexStr = "includeKeyInOutput";
                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr))
                                    if (child.attributes.getNamedItem(indexStr).value == "yes") includeKeyInOutput[k] = true;
                                    else includeKeyInOutput[k] = false;
                            }
                            else {
                                if (child.attributes[indexStr])
                                    if (child.attributes[indexStr].nodeValue == "yes") includeKeyInOutput[k] = true;
                                    else includeKeyInOutput[k] = false;
                            }

                            indexStr = "autoCount";
                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr)) autoCountMap[k] = child.attributes.getNamedItem(indexStr).value;
                                else autoCountMap[k] = null;
                            }
                            else {
                                if (child.attributes[indexStr]) autoCountMap[k] = child.attributes[indexStr].nodeValue;
                                else autoCountMap[k] = null;
                            }

                            indexStr = "filter";
                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr)) iteratorSelectorMap[k] = child.attributes.getNamedItem(indexStr).value;
                                var len = headingText.length;
                                if (len > maxLength) maxLength = len;
                            }
                            else {
                                if (child.attributes[indexStr]) iteratorSelectorMap[k] = child.attributes[indexStr].nodeValue;
                                var len = headingText.length;
                                if (len > maxLength) maxLength = len;
                            }


                            indexStr = "scoreable";

                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr) != undefined) {
                                    scoreableMap[k] = true;
                                    scoringTable().addElement(headingNames[k], "output");
                                }
                                else scoreableMap[k] = false;
                            }
                            else {
                                if (child.attributes[indexStr] != undefined) {
                                    scoreableMap[k] = true;
                                    scoringTable().addElement(headingNames[k], "output");
                                }
                                else scoreableMap[k] = false;
                            }



                            indexStr = "numDecFigures";

                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr) != undefined) numFiguresMap[k] = child.attributes.getNamedItem(indexStr).value;
                            }
                            else {
                                if (child.attributes[indexStr] != undefined) numFiguresMap[k] = child.attributes[indexStr].nodeValue;
                            }


                            indexStr = "rowFunction";
                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr) != undefined) rowFunctionMap[k] = child.attributes.getNamedItem(indexStr).value;
                            }
                            else {
                                if (child.attributes[indexStr] != undefined) rowFunctionMap[k] = child.attributes[indexStr].nodeValue;
                            }

                            indexStr = "maxTextLength";
                            if (isIE) {
                                if (child.attributes.getNamedItem(indexStr)) columnTextLengthMap[k] = child.attributes.getNamedItem(indexStr).value;
                                else columnTextLengthMap[k] = -1;

                            }
                            else {
                                if (child.attributes[indexStr]) columnTextLengthMap[k] = child.attributes[indexStr].nodeValue;
                                else columnTextLengthMap[k] = -1;

                            }
                        }
                    }
                } catch (err) {
                    dbg().logError(source, 'Error occurred during construction of data table header column ' + k + ' for attribute ' + indexStr + ': ' + err.message);
                }
            }

        }
        return this;
    };

    this.getInputKey = function (columnNumber) {
        return headingMap[columnNumber]['inputKey'];
    };

    this.getOutputKey = function (columnNumber) {
        return headingMap[columnNumber]['outputKey'];
    };

    this.headingMapInverse = function (num) {
        for (var key in headingMap) {
            if (headingMap[key] == num) return key;
        }
        return null;
    };

    this.setIncrementTrialOnOutput = function (newIncrementTrialOnOutput) {
        if (newIncrementTrialOnOutput == 'true') incrementTrialOnOutput = true;
        else incrementTrialOnOutput = false;
    };

    this.getIncrementTrialOnOutput = function () {
        return incrementTrialOnOutput;
    };

    this.createCellId = function (row, col) {
        return this.getName() + simID + row + col;
    };

    this.getFooter = function () {
        return footer;
    };

    this.setFooter = function (footer) {
        var tFoot = HTMLDataTable.createTFoot();
        var row = simDocument().createElement('TR');
        tFoot.appendChild(row);
        for (var i = 0; i < numColumns; i++) {
            var cell = simDocument().createElement('TD');
            cell.style.width = colWidth;
            row.appendChild(cell);
            cell.innerHTML = ' ';
        }
        this.HTMLDataTable.style.display = 'block';
        return this;
    };

    this.getClearRows = function () {
        return clearRows;
    };

    this.setClearRows = function (newClearRows) {
        clearRows = newClearRows == 'true' || newClearRows == 'yes' ? true : false;
        return this;
    };

    this.getScoreable = function () {
        return scoreable;
    };

    this.setScoreable = function (newScoreable) {
        if (newScoreable == 'yes') scoreable = true;
        else scoreable = false;
        return this;
    };

    this.getHeaderVisible = function () {
        return headerVisible;
    };

    this.setHeaderVisible = function (newHeaderVisible) {
        headerVisible = newHeaderVisible;
        return this;
    };

    this.getOutputSource = function () {
        return outputSource;
    };

    this.setOutputSource = function (newOutputSource) {
        outputSource = newOutputSource;
        return this;
    };

    this.getDeleteRowImage = function () {
        return deleteRowImage;
    };

    this.setDeleteRowImage = function (newDeleteRowImage) {
        deleteRowImage = newDeleteRowImage;
        if (deleteRowImage) {
            this.setClearRows(true);
            deleteColumn = true;
        }
        return this;
    };

    this.evaluateRowFunction = function (row, column) {
        if (rowFunctionMap[column]) {
            var sum = 0;
            var cellID = this.createCellId(row, column);
            var cell = simDocument().getElementById(cellID);
            switch (rowFunctionMap[column]) {
                case 'mean':
                    for (var i = 0; i < numColumns; i++) if (i != column) sum += parseFloat(cell.inerHTML);
                    return (sum / numRows).toFixed(this.getFixedDigits());
                    break;
                case 'sum':
                    for (var i = 0; i < numColumns; i++) if (i != column) sum += parseFloat(cell.inerHTML);
                    return sum.toFixed(this.getFixedDigits());
                    break;
                case 'max':
                    var max = null;
                    for (var i = 0; i < numColumns; i++) max = Math.max(parseFloat(cell.inerHTML), max);
                    return max.toFixed(this.getFixedDigits());
                    break;
                case 'min':
                    var min = null;
                    for (var i = 0; i < numColumns; i++) min = Math(min, parseFloat(cell.inerHTML));
                    return min.toFixed(this.getFixedDigits());
                    break;
            }
        }
    };

    this.replace = function () {
        HTMLPanel.replaceChild(HTMLDataTable, HTMLDataTable);
        this.refreshTable();
    };

    this.append = function () {
        HTMLPanel.appendChild(HTMLDataTable);
    };

    this.setCell = function (inputKey, data, outputKey) {
        var cellWritten = false;
        var rowNum = simMgr().getTrialNum() - 1;  // rows start at 0 in the table
        var colNum = resolveColumn(inputKey, outputKey);
        if (rowNum == numRows) {
            if (userAddRows) addRow(this);
            else {
                if (!userAlerted) {
                    Simulator.showAlertWarning('You cannot add additional data table rows.');
                    userAlerted = true;
                }
                return;
            }
        }
        cellWritten = this.setCellWithRowColNum(rowNum, colNum, data);
        this.setAutoCountCells(rowNum);
        return cellWritten;
    };

    this.setCellWithRowColNum = function (rowNum, colNum, data) {
        var cellWritten = false;
        var testVal = null;
        if (rowNum < numRows && rowNum >= 0 && colNum != -1) {
            var id = this.createCellId(rowNum, colNum);
            cell = simDocument().getElementById(id);
            var contentType = headingMap[colNum]['contentType'];
            if (data === Simulator.Constants.NO_DATA_INDICATOR) cell.inerHTML = Simulator.Constants.NO_DATA_INDICATOR;
            else if (contentType == 'text') {
                if (!isNaN(data)) {   // string representation of a number
                    testVal = parseFloat(data);
                    var figures = parseInt(numFiguresMap[colNum]);
                    if (!figures) figures = 0;
                    if (rowFunctionMap[colNum]) testVal = this.evaluateRowFunction(rowNum, colNum);
                    data = testVal.toFixed(figures);
                }
                else if (columnTextLengthMap[colNum] != -1) {
                    var parts = data.split(Simulator.Constants.MULTIPLE_VALUE_DELIMITTER);
                    var txtLen = parseInt(columnTextLengthMap[colNum]);
                    for (var i = 0; i < parts.length; i++) {
                        if (parts[i].length > txtLen) parts[i] = parts[i].substr(0, txtLen - 4) + " ...";
                        if (i > 0) parts[i] = ', ' + parts[i];
                    }
                    if (parts.length > 1) data = parts.join('\n');
                    else data = parts[0];
                }
                cell.innerHTML = util().replaceAll(data, Simulator.Constants.MULTIPLE_VALUE_DELIMITTER, ',');
            } else if (contentType == 'image') {
                cell.inerHTML = '<img src="data:image/png;base64,"' + data + '" alt="x">';
            }
            cellWritten = true;
        } else cellWritten = false;
        return cellWritten;
    };

    this.setAutoCountCells = function (rowNum) {
        if (rowNum < numRows && rowNum >= 0) {
            for (var i = 0; i < this.getNumColumns(); i++) {
                if (autoCountMap[i] != null) {
                    var id = this.createCellId(rowNum, i);
                    cell = simDocument().getElementById(id);
                    cell.innerHTML = simMgr().getTrialNum();
                }
            }
        }
    };

    this.handleEvent = function (event) {
        var newEvent = undefined;
        var data = '';
        switch (event.type) {
            case 'inputReq':
                break;
            case 'input':
                data = event.data;
                parseTableInput(this, data);
                break;
            case 'command':
                switch (event.context) {
                    case 'resetTrials':
                        this.clearAllRows(this);
                        userAlerted = false;
                        break;
                    case 'startTrial':
                        if (!simMgr().trialLimitReached()) {
                            userAlerted = false;
                            data = whiteboard().getCategory('dataInput');
                            if (data != null) {
                                parseTableInput(this, data);
                                postTableDataToWhiteboard(this);
                            }
                        }
                        break;
                    case 'startAnimation':
                        userAlerted = false;
                        data = whiteboard().getCategory('dataInput');
                        if (data != null) {
                            parseTableInput(this, data);
                            postTableDataToWhiteboard(this);
                        }
                        break;
                    default:
                        dbg().logWarning(source, 'Unhandled command ' + event.context + ' received by DataTable');
                        break;
                }
                break;
            case 'info':
                switch (event.context) {
                    case 'inputAvailable':
                        if (!simMgr().trialLimitReached()) {
                            data = whiteboard().getCategory('dataInput');
                            if (data != null) {
                                parseTableInput(this, data);
                                postTableDataToWhiteboard(this);
                            }
                        }
                        break;
                    case Simulator.Constants.ANIMATION_FINISHED:
                    case Simulator.Constants.ANIMATION_THREAD_FINISHED:
                        if (this.getOutputSource()) {
                            if (this.getOutputSource() == 'evaluator') data = whiteboard().getItem('evaluationOutput', 'output');  // Allow these sources to be combined in the future
                            else if (this.getOutputSource() == 'animation') data = whiteboard().getItem('animationOutput', 'output');
                        } else {
                            data = whiteboard().getItem('evaluationOutput', 'output');
                            if (!data) data = whiteboard().getItem('animationOutput', 'output');
                        }
                        if (data != null) parseOutput(this, data);
                        break;
                    case 'outputAvailable':
                        if ((!simMgr().trialLimitReached()) && ((this.getWaitOn() == null) || (this.getWaitOn() == ''))) {  // if "waitOn" is set, wait until animation finishes to post data
                            data = whiteboard().getItem('evaluationOutput', 'output');
                            if (!data) data = whiteboard().getItem('evaluation', 'output');
                            if (!data) data = whiteboard().getItem('animationOutput', 'output');
                            if (data != null) parseOutput(this, data);
                            else eventMgr().postEvent(new Simulator.Event(this, 'info', 'tableUpdated'));
                        } else if ((this.getWaitOn() != null) && (this.getWaitOn() != '')) {
                            eventMgr().registerEvent(new Simulator.Event(this, 'info', this.getWaitOn()));
                        }
                        break;
                    case 'animationOutputAvailable':
                        data = whiteboard().getItem('animationOutput', 'output');
                        if (data != null) parseOutput(this, data);
                        else eventMgr().postEvent(new Simulator.Event(this, 'info', 'tableUpdated'));
                        break;
                }
                break;
            default:
                dbg().logWarning(source, 'DataTable ' + this.getName() + ': Unhandled event type received: ' + event.inspect());
                return;
        }
        if (newEvent != undefined && newEvent != null) newEvent.postEvent();
    };

    this.allAttributesLoaded = function () {
        createTableBody(this);
    };

    this.saveScoreableInputs = function () {
        var row = simMgr().getTrialNum() - 1;
        for (var i = 0; i < numColumns; i++) {
            var cellID = this.createCellId(row, i);
            var cell = simDocument().getElementById(cellID);
            if (scoreableMap[i]) scoringTable().setValue(headingNames[i], row, cell.innerHTML);
        }
    };

    this.getContents = function (indent, name) {
        var buff = [];
        var numCols = 0;
        var table = simDocument().getElementById(this.getNodeID());
        if (table) {   // the data table is optional
            var body = table.tBodies[0];
            var children = body.childNodes;
            if (!name) buff.push(indent + '<stateTable>\n');
            else buff.push('<' + name + '>\n');
            for (var i = 0; i < children.length; i++) {
                if (children[i].nodeName[0] != '#') {
                    var child = children[i];
                    if (child.nodeName.toLowerCase() == 'tr') {
                        buff.push(indent + '  <tr>\n');
                        var cols = child.childNodes;
                        if (this.getClearRows() == true) numCols = cols.length - 1;
                        else numCols = cols.length;
                        for (var j = 0; j < numCols; j++) {
                            if (cols[j].nodeName.toLowerCase() == 'th') {
                                buff.push(indent + '    <th>');
                                buff.push(cols[j].childNodes[0].textContent);
                                buff.push('</th>');
                            } else if (cols[j].nodeName.toLowerCase() == 'td') {
                                buff.push(indent + '    <td>');
                                buff.push(cols[j].textContent);
                                buff.push('</td>\n');
                            }
                        }
                        buff.push(indent + '  </tr>\n');
                    }
                }
            }
            if (!name) buff.push(indent + '</stateTable>\n');
            else buff.push('</' + name + '>\n');
            return buff.join('');
        } else return '';
    };

    this.loadFromResponse = function (node) {
        var responseRowNum = -1;
        var lastTableRowNum = this.getNumRows() - 1;  // Rows begin at 0
        var colNum = -1;
        var children = node.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeName[0] != '#') {
                if (child.nodeName.toLowerCase() == 'tr') {   // we found a row
                    responseRowNum++;
                    colNum = -1;
                    if (lastTableRowNum < responseRowNum) {  // Have we gotten to the end of the existing rows in the table?
                        addRow(this, true);
                        lastTableRowNum++;
                    }
                    var childChildren = child.childNodes;
                    for (var j = 0; j < childChildren.length; j++) {
                        var childChild = childChildren[j];
                        if (childChild.nodeName[0] != '#') {
                            if (childChild.nodeName.toLowerCase() == 'td') {   // we found a column
                                colNum++;
                                var cellID = this.createCellId(responseRowNum, colNum);
                                var cell = simDocument().getElementById(cellID);
                                if (cell) cell.innerHTML = childChild.textContent;
                                else dbg().logError(source, 'DataTable.loadFromResponse: Could not find cell with ID = ' + cellID);
                            }
                        }
                    }
                }
            }
        }
    };

    this.keyboardNavigateTo = function (elementID, itemID, index) {
        var item = simDocument().getElementById(itemID);
        if (item) item.setAttribute('class', 'simAreaFocus');
    };

    this.keyboardNavigateAwayFrom = function (elementID, itemID, index) {
        var item = simDocument().getElementById(itemID);
        if (item) item.removeAttribute('class');
    };

    this.recordKeyboardSelection = function (elementID, itemID, itemIndex) {
        var element = null;
        var item = null;
        if (itemID) {
            item = simDocument().getElementById(itemID);
            if (!item) {
                element = simDocument().getElementById(elementID);
                if (element) item = element.getElementsByClassName(itemID)[0];
            }
            if (item) {
                item.isSelected = true;
                // this.setSelectStateViaKeyboard(elementID, itemID);
                // this.onChange(elementID); //TODO: Verify this while testing
                // check if the item being selected is a "clear row" button
                if (this.getClearRows()) {
                    if ((itemIndex % (numColumns + 1)) == numColumns) {
                        var currentRow = Math.floor(itemIndex / (numColumns + 1));
                        this.clearRow(null, itemID, currentRow, false);
                    }
                }
            }
        }
    };

    this.receivedSpeechFocus = function () {
        debug(this.getName() + ' received speech focus');
        var node = simDocument().getElementById(this.getID());
        prevBorder = node.style.border;
        node.style.border = 'thin solid #ff0000';
    };

    this.removeSpeechFocus = function (value) {
        debug(this.getName() + ' lost speech focus');
        var node = simDocument().getElementById(this.getID());
        node.style.border = prevBorder;
    };

    this.speechActivated = function (value) {
        var parts = value.split('#');
        this.clearRow(null, this.getNodeID(), parts[1].trim(), false);
    };

    this.getSourceName = function () {
        return source;
    };

    this.inspect = function (embedded, force) {
        var buff = [];
        var sep = '\n\n';
        var rowSep = '\n\n';
        var colSep = '   ';
        var tHeader = HTMLDataTable.tHead;
        var tBody = HTMLDataTable.tBodies[0];
        buff.push('Inspecting '); buff.push(this.getName()); buff.push(sep);
        for (var i = 0; i < numColumns; i++) {
            buff.push(tHeader.rows[0].cells[i].innerHTML); buff.push(colSep);
        }
        buff.push(rowSep);
        for (var l = 0; l < numRows; l++) {
            var row = tBody.rows[l];
            for (var j = 0; j < numColumns; j++) {
                buff.push(row.cells[j].innerHTML); buff.push(colSep);
            }
            buff.push(rowSep);
        }
        buff.push(sep);
        for (var prop in this) {
            if (prop.substr(0, 3) == 'get') {
                buff.push(prop);
                buff.push(' = ');
                buff.push(eval('this.' + prop + '()'));
                buff.push(sep);
            }
        }
        if (!embedded) (force === null) ? debug(buff.join('')) : debugf(buff.join(''));
        return buff.join('');
    };

    this.refreshTable = function () {
        HTMLDataTable.style.display = 'none';
        HTMLDataTable.style.display = 'table';
    };

    this.clearRow = function (e, htmlTbl, theRow, batchDelete) {
        var evt = window.event || e;
        if (!simMgr().isPlaying() && !simMgr().isReadOnly()) {
            var tbl = this;
            var nRows = tbl.getNumRows();
            var nCols = tbl.getNumColumns();
            if (theRow < nRows && theRow >= 0) {
                if (!rowIsEmpty(tbl, theRow)) {
                    for (var i = 0; i < nCols; i++) {
                        var id = tbl.createCellId(theRow, i);
                        var cell = simDocument().getElementById(id);
                        cell.innerHTML = '';
                    }
                    scoringTable().clearRow(theRow);
                    //if(!batchDelete) simMgr().RedoTrialNum(theRow + 1);   // Rows start at 0; trials start at 1
                    if (!batchDelete) eventMgr().postEvent(new Simulator.Event(tbl, 'command', 'redoTrial', theRow + 1));
                }
            }
        }
        if (evt) {
            if (evt.preventDefault) {
                evt.preventDefault();
            } else {
                evt.returnValue = false;
            }
        }
    };

    this.clearAllRows = function (tbl) {
        if (!simMgr().isReadOnly()) {
            var htmlTbl = simDocument().getElementById(tbl.getNodeID());
            var nRows = tbl.getNumRows();
            for (var j = 0; j < nRows; j++) {
                this.clearRow(null, htmlTbl.id, j, true);
            }
            eventMgr().postEvent(new Simulator.Event(tbl, 'info', 'allTableRowsCleared'));
            //simMgr().ResetTrialNum();
        }
    };

    this.setAttributes = function (attr, node) {
        if (node) attr = util().getAttributes(node);
        Simulator.Display.DataTable.prototype.setAttributes.call(this, attr, node);
        for (var key in attr) {
            switch (key) {
                case 'rows':
                    this.setNumRows(attr[key]);
                    break;
                case 'columns':
                    this.setNumColumns(attr[key]);
                    break;
                case 'footer':
                    this.setFooter(attr[key]);
                    break;
                case 'clearRow':
                    this.setClearRows(attr[key]);
                    break;
                case 'headerVisible':
                    this.setHeaderVisible(attr[key]);
                    break;
                case 'name':
                    this.setID(this.getName());
                    break;
                case 'deleteRowImage':
                    this.setDeleteRowImage(attr[key]);
                    break;
                case 'contentType':
                    this.setContentType(attr[key]);
                    break;
                case 'incrementTrialOnOutput':
                    this.setIncrementTrialOnOutput(attr[key]);
                    break;
                case 'userAddRows':
                    this.setUserAddRows(attr[key]);
                    break;
                case 'rowFunction':
                    this.setRowFunction(attr[key]);
                    break;
                case 'outputSource':
                    this.setOutputSource(attr[key]);
                    break;
            }
        }
        var children = node.childNodes;
        for (var k = 0; k < children.length; k++) {
            if (children[k].nodeName == 'header') {
                this.setHeader(children[k]);
                break;
            }
        }
        this.allAttributesLoaded();
    };

    //public functions - end here

    this.render = function (panelName) {
        if (this.getVisible()) {
            if (!htmlTableRendered) {
                this.append();
                htmlTableRendered = true;
            }
            else this.append();
            this.refreshTable();
        } else {
            var HTMLTable = simDocument().getElementById(this.getName());
            HTMLTable.style.display = 'none';
            this.mapHTML2JS(HTMLTable);
        }
    };

    // Convenience function for the most frequently used Debug methods
    function debug(str1, str2, trace) {
        dbg().debug(source, str1, str2, trace);
    }

    function debugf(str1, str2, trace) {
        dbg().debugf(source, str1, str2, trace);
    }

};            // End of DataTable Class Specification

//Inherit methods and class variables
Simulator.Display.DataTable.prototype = new Simulator.Display.DataDisplayElement();
Simulator.Display.DataTable.parent = Simulator.Display.DataDisplayElement;
Simulator.Display.DataTable.prototype.constructor = Simulator.Display.DataTable;  // Reset the prototype to point to the current class






