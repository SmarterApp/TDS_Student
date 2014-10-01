TDS.TableItem = (typeof (TDS.TableItem) == "undefined") ? {} : TDS.TableItem;

/** **************************************************************************
* @class Table
* @superclass none
* @param none
* @return Table instance
* Creates a new Table class.
*****************************************************************************
*/
TDS.TableItem.ResponseTable = function (table) {
    // variables
    var columns = [];
    var columnNames = [];

    TDS.TableItem.ResponseTable.scoringTable = function () { return table.getScoringTable(); };
    TDS.TableItem.ResponseTable.clearTable = function () {
        for (var p in columns) {
            if (columns.hasOwnProperty(p)) {
                var col = columns[p];
                for (var j = 0; j < col.length; j++) col[j] = '';
            }
        }
    };

    this.setValue = function (colName, rowNum, value) {
        var theValue=value;
        //if (value instanceof Array) {
        //    theValue = value.join(', ');
        //} else {
        //    theValue = value;
        //}
        if (rowNum == null || rowNum < 0) {
            rowNum = 0; // starting at 0
        }
        if (rowNum > -1) {
            if (!columns[colName]) {
                columns[colName] = [];
            }
            columns[colName][rowNum] = theValue;
        }
    };

    this.getContents = function () {
        var buff = [];
        var colNames = [];
        var maxLen = 0;
        var len;
        var i = 0;
        var aColName;
        var idNamePair = TDS.TableItem.getColumnHeaderIdNamePair();
        buff.push('<responseTable>');
        buff.push('    <tr>');

        for (var p in columns) {
            buff.push('       <th id = "' + p + '">' + idNamePair[p] + '</th>');
            len = columns[p].length;
            maxLen = Math.max(len, maxLen);
            colNames[i++] = p;
        }

        buff.push('    </tr>');
        var size = colNames.length;
        var value;

        for (var row = 1; row < maxLen; row++) {
            buff.push('    <tr>');
            for (var col = 0; col < size; col++) {
                aColName = colNames[col];
                value = columns[aColName][row];
                if (value == undefined || value == null) {
                    value = '';
                }
                buff.push('       <td>' + value + '</td>');
            }
            buff.push('    </tr>');
        }
        buff.push('</responseTable>');
         
        // excluding col
        var colCount = TDS.TableItem.getTotalColCount();
        var rowCount = TDS.TableItem.getTotalRowCount();
        var cidstr;
        var exclColIds = TDS.TableItem.getExcludedColIds();
        for (var m = 0; m < exclColIds.length; m++) {
            cidstr = "id = " + "\"" + exclColIds[m] + "\"";
            for (var j = 0; j < buff.length; j++) {
                var arrayElem = buff[j];
                if (arrayElem.indexOf(cidstr) > -1) {
                    for (var rc = rowCount; rc > -1; rc--) {
                        //buff.splice(j + ((colCount + 2) * rc), 1);
                        buff.splice(j + ((colCount + 2) * rc), 1);
                    }
                }
            }
            colCount--;
        }

        // excluding row, as a possible future feature
        //var trstr = "<tr>";
        //var firstIndexOftr = 0;
        //for (var rnum in TDS.TableItem.getExcludedRowNms()) {
        //    for (var k = 0; k < buff0.length; k++) {
        //        if (buff[k].indexOf(trstr) > -1) {
        //            firstIndexOftr = k;
        //        }
        //    }
        //    buff0.splice(firstIndexOftr + rnum * TDS.TableItem.getColumnHeaderIdNamePair().length, TDS.TableItem.getColumnHeaderIdNamePair().length + 2);
        //}

        return buff.join('\n');
    };

    TDS.TableItem.ResponseTable.restoreResponseTable = function (node) {
        var child;
        var attr;
        var text;
        var colNum;
        var rowNum = -2; // We want to skip the numbering of the header row so we set rowNum to -2 (rosw start at 0
        this.clearTable();
        var children = node.childNodes;
        for (var q = 0; q < children.length; q++) {
            if (children[q].nodeName == 'tr') {
                rowNum++;
                colNum = -1;
                child = children[q];
                for (var m = 0; m < child.childNodes.length; m++) {
                    if (child.childNodes[m].nodeName == 'td') { // we want to skip the header row
                        colNum++;
                        text = child.childNodes[m].textContent;
                        scoringTable().setValue(columnNames[colNum], rowNum, text);
                    } else if (child.childNodes[m].nodeName == 'th') {
                        attr = child.childNodes[m].attributes;
                        for (var j = 0; j < attr.length; j++) {
                            if (attr[j].nodeValue[0] != '#') {
                                columnNames.push(attr[j].nodeValue);
                            }
                        }
                    }
                }
            }
        }
    };
};


