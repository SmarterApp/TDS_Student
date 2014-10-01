TDS.TableItem = (typeof(TDS.TableItem) == "undefined") ? { } : TDS.TableItem;

TDS.TableItem = function(item_position) {

    // variables
    var scoringTable = new TDS.TableItem.ResponseTable(this);
    var textAreaIdNum = -1;
    // colId and colName mapping
    var columnHeaderIdNamePair = new Array();
    var totalColCount = 0;
    var totalRowCount = 0;
    var colNum = 0;
    var rowNum = 0;
    var limitOnNumberOfTdFilled='AnswerAll';
    var maxNumberOfTdToBeFilled = 10; // ToDo: to receive from its
    var minNumberOfTdToBeFilled = 6; // ToDo: to receive from its
    var allEditableSpanElements;
    var excludedColIds = [];
    var excludedRowNms = [];
    var componentArray = [];
    
    // accessors
    TDS.TableItem.getTableId = function() {
        return 'ti-'+item_position;
    };
    this.getScoringTable = function() {
        return scoringTable;
    };
    TDS.TableItem.getColumnHeaderIdNamePair = function() {
        return columnHeaderIdNamePair;
    };
    TDS.TableItem.getTotalColCount = function () {
        return totalColCount;
    };
    TDS.TableItem.getTotalRowCount = function() {
        return totalRowCount;
    };
    TDS.TableItem.getExcludedColIds = function() {
        return excludedColIds;
    };
    TDS.TableItem.getExcludedRowNms = function() {
        return excludedRowNms;
    };
    this.getComponentArray = function() {
        return componentArray;
    };
    
    // This is the main function to call for external users to
    // render the item and response xml into the table item.
    this.loadXml = function(itemXml, responseXml) {
        // load item xml
        TDS.TableItem.loadTableItemTextAreasFromXmlText(itemXml);
        // load response xml
        if (responseXml != null && responseXml.length > 0) {
            TDS.TableItem.loadResponseXml(responseXml);
        }
    };

    // start the table item with a xml string
    TDS.TableItem.loadTableItemTextAreasFromXmlText = function(xmlText) {
        var xmlDoc = TDS.TableItem.loadXmlDocFromString(xmlText);
        TDS.TableItem.loadTableItemTextAreasFromXmlDom(xmlDoc);
    };

    // Parse the xml in xmlStr
    TDS.TableItem.loadXmlDocFromString = function(xmlStr) {
        var xmlDoc;
        if (window.DOMParser && navigator.userAgent.toLowerCase().indexOf('msie') == -1) {
            var domParser = new DOMParser();
            xmlDoc = domParser.parseFromString(xmlStr, 'application/xml');
        } else // Internet Explorer
        {
            xmlDoc = new ActiveXObject('Microsoft.XMLDOM');
            xmlDoc.async = 'false';
            xmlDoc.loadXML(xmlStr);
        }
        return xmlDoc;
    };


    // start the table item with an xml dom
    TDS.TableItem.loadTableItemTextAreasFromXmlDom = function() {
        // get main table elements
        var stem = document.getElementById('Stem_' + item_position);
        var tableElement = stem.getElementsByClassName('tableItem')[0];

        var theadElement = tableElement.getElementsByTagName('thead')[0];
        var tbodyElement = tableElement.getElementsByTagName('tbody')[0];

        // build column header dictionary
        // get all th inside thead
        var allIdentifiableColumns = YUD.getElementsBy(TDS.TableItem.isThIdentifiable, 'th', theadElement);
        totalColCount = allIdentifiableColumns.length;
        var identifiableColumn;
        var identifiableColumnId;
        var identifiableColumnNm;
        for (var i = 0; i < totalColCount; i++) {
            identifiableColumn = allIdentifiableColumns[i];
            identifiableColumnId = YUD.getAttribute(identifiableColumn, 'data-its-identifier');
            identifiableColumnNm = TDS.TableItem.getInnermostText(identifiableColumn);
            identifiableColumnNm = identifiableColumnNm ==  '&nbsp;' ? '' : identifiableColumnNm;
            // add to the dictionary
            columnHeaderIdNamePair[identifiableColumnId] = identifiableColumnNm;
        }

        // get all tr inside tbody
        var allTrElements = tbodyElement.getElementsByTagName('tr');
        totalRowCount = allTrElements.length;

        // build colId list for excluded columns
        var allExcludableColumnSpans = YUD.getElementsBy(TDS.TableItem.isSpanExcludable, 'span', theadElement);
        var excludableColSpan;
        var excludedColId;
        var colIncluded;
        for (var m = 0; m < allExcludableColumnSpans.length; m++) {
            excludableColSpan = allExcludableColumnSpans[m];
            colIncluded = YUD.getAttribute(excludableColSpan, 'data-its-includedInResponse');
            if (colIncluded=='false') {
                excludedColId = YUD.getAttribute(excludableColSpan.parentNode, 'data-its-identifier');
                excludedColIds.push(excludedColId);
            }
        }

        // build rowNm list for excluded rows - for future feature of excluding rows from response
        //var allExludableRowSpans = YUD.getElementsBy(TDS.TableItem.isSpanExcludable, 'span', tbodyElement);
        //var excludableRowSpan;
        //var excludedRowNm;
        //var rowIncluded;
        //for (var n = 0; n < allExludableRowSpans.length; n++) {
        //    excludableRowSpan = allExludableRowSpans[n];
        //    rowIncluded = YUD.getAttribute(excludableRowSpan, 'data-its-includedInResponse');
        //    if(rowIncluded=='false') {
        //        excludedRowNm = n;
        //        excludedRowNms.push(excludedRowNm);
        //    }
        //}
        
        // get all editable span elements
        allEditableSpanElements = YUD.getElementsBy(TDS.TableItem.isSpanEditable, 'span', tableElement);
        
        // traverse all th elements inside thead
        var allTheadThElements = theadElement.getElementsByTagName('th');
        var thElement;
        var j;
        for (j = 0; j < allTheadThElements.length; j++) {
            thElement = allTheadThElements[j];
            YUD.addClass(thElement, 'ti');
            
            // assign colNum and rowNum for all th's
            colNum = 'col' + j % allIdentifiableColumns.length;
            rowNum = Math.floor(j / allIdentifiableColumns.length);
            var thSpanElements = thElement.getElementsByTagName('span');
            if (thSpanElements.length > 0
                && thSpanElements[0].hasAttribute('data-its-input')
                && YUD.getAttribute(thSpanElements[0], 'data-its-input') == "true") {
                // add class to the parent th
                YUD.addClass(thElement, 'ti_textbox');
                // get editable span
                var thInputSpanElement = thSpanElements[0];
                var thTypeInputAllowed = YUD.getAttribute(thInputSpanElement, 'data-its-validationRule');
                textAreaIdNum++;
                TDS.TableItem.createInputTextArea(TDS.TableItem.getTableId(), thInputSpanElement, textAreaIdNum, thTypeInputAllowed, allEditableSpanElements.length, scoringTable, colNum, rowNum);
            } else {
                // populate pre-filled td's
                scoringTable.setValue(colNum, rowNum, '');
            }
        }

        // traverse all td elements inside tbody
        var allTbodyTdElements = tbodyElement.getElementsByTagName('td');
        var tdElement;
        for (var k = 0; k < allTbodyTdElements.length; k++) {
            tdElement = allTbodyTdElements[k];
            YUD.addClass(tdElement, 'ti');
            // assign colNum and rowNum for all td's
            colNum = 'col' + (k + j) % allIdentifiableColumns.length;
            rowNum = Math.floor((k + j) / allIdentifiableColumns.length);
            var tdSpanElements = tdElement.getElementsByTagName('span');
            if (tdSpanElements.length > 0
                && tdSpanElements[0].hasAttribute('data-its-input')
                && YUD.getAttribute(tdSpanElements[0], 'data-its-input') == "true") {
                // add class to the parent td
                YUD.addClass(tdElement, 'ti_textbox');
                // get editable span
                var tdInputSpanElement = tdSpanElements[0];
                var tdTypeInputAllowed = YUD.getAttribute(tdInputSpanElement, 'data-its-validationRule');
                textAreaIdNum++;
                TDS.TableItem.createInputTextArea(TDS.TableItem.getTableId(), tdInputSpanElement, textAreaIdNum, tdTypeInputAllowed, allEditableSpanElements.length, scoringTable, colNum, rowNum);
            } else {
                // populate pre-filled td's
                scoringTable.setValue(colNum, rowNum, '');
            }
        }
        
        // get tablecontainer div and push teable item into it
        var tableContainer = document.getElementById('TableContainer_' + item_position);
        tableContainer.appendChild(tableElement);
    };

    TDS.TableItem.isThIdentifiable = function(elem1) {
        var hasDataItsId = elem1.hasAttribute('data-its-identifier');
        return hasDataItsId;
    };

    TDS.TableItem.isSpanExcludable = function(elem12) {
        return elem12.hasAttribute('data-its-includedInResponse');
    };

    TDS.TableItem.isSpanEditable = function(elem3) {
        return elem3.hasAttribute('data-its-input')
            && YUD.getAttribute(elem3, 'data-its-input') == "true";
    };

    TDS.TableItem.getInnermostText = function (node) {
        if (node.getElementsByTagName('span').length > 0) {
            return node.getElementsByTagName('span')[0].innerHTML;
        } else {
            return node.getElementsByTagName('p')[0].innerHTML;
        }
    };

    TDS.TableItem.createInputTextArea = function(tblId, span, textIdNum, typeInputAllowed, numberOfTextAreas, scrTbl, cNmbr, rNmbr) {
        var theElement;
        theElement = new TDS.TableItem.TextArea();
        theElement.render(tblId, span, textIdNum, typeInputAllowed, numberOfTextAreas, scrTbl, cNmbr, rNmbr);
        // build array for textarea navigation 
        componentArray.push(theElement);
    };

    TDS.TableItem.showAlertWarning = function(msg) {
        if (TDS.Dialog) {
            TDS.Dialog.showWarning(msg, function() {});
        } else {
            alert(msg);
        }
    };

    // get the response table
    this.getResponseXml = function() {
        return TDS.TableItem.saveInput();
    };

    TDS.TableItem.saveInput = function() {
        var inputStr = '<responseSpec>\n';
        var responseStr = scoringTable.getContents('  ');
        inputStr += responseStr + '\n\n';
        inputStr += '\n</responseSpec>';
        return inputStr;
    };

    // loads the response information
    TDS.TableItem.loadResponseXml = function(xmlText) {
        var xmlDoc = TDS.TableItem.loadXmlDocFromString(xmlText);
        TDS.TableItem.restoreTable(xmlDoc);
    };

    TDS.TableItem.restoreTable = function(xmlDoc) {
        var responseSpec = xmlDoc.getElementsByTagName('responseSpec')[0];
        scoringTable.restoreResponseTable(responseSpec.getElementsByTagName('responseTable')[0]);
    };

    // check if the response is considered valid
    this.isValid = function () {
        var tableItemTextAreaElements = YUD.getElementsByClassName('ti-textarea');
        var numberOfTextAreaFilled = 0;
        for (var i = 0; i < tableItemTextAreaElements.length; i++) {
            if (tableItemTextAreaElements[i].value != '') {
                numberOfTextAreaFilled++;
            }
        }
        switch (limitOnNumberOfTdFilled) {
        case 'AnswerAll':
            return numberOfTextAreaFilled == tableItemTextAreaElements.length;
        case 'AnswerAtMost':
            return numberOfTextAreaFilled <= maxNumberOfTdToBeFilled;
        case 'AnswerAtLeast':
            return numberOfTextAreaFilled >= minNumberOfTdToBeFilled;
        default:
            return true;
        }
    };
};
 