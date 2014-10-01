var Equation = { };
// load equation editor
ContentManager.onItemEvent('available', function (page, item) {
    if (!item.isResponseType('EquationEditor')) return;

    var doc = page.getDoc();

    // get equation container
    var equationContainer = doc.getElementById('EquationEditor_' + item.position); // EquationEditor wrapper

    // get render xml string
    var eqXml = item.rendererSpec;

    var itemId = item.getID();

    // create the editor
    var equationEditor = new EquationEditor(doc, equationContainer, eqXml, page.id, itemId);
    //equationEditor.isValid();
    //alert(equationEditor.syntaxchecking);
    item.equationEditor = equationEditor;

    var htmlObj = equationEditor.getHtmlObj();
    item.htmlObj = htmlObj;

    // get components for keyboard navigation
    var eqWrapper = htmlObj.children[0];
    var eqEditor = htmlObj.children[1].children[0];

    equationEditor.frameClasses(doc.body.classList);
    Equation._editorContentLoaded(item, equationEditor);

    // BUG #12516: Mac OS X secure browser selection hack
    //if (Equation.requiresSelectionFix()) {
    //equationEditor.frameStyle();
    //}

    //focus and blur event
    eqEditor.blur = function () {
        setTimeout(function () {
            eqEditor.children[0].blur(); // remove focus from the suim iframe
        }, 0);
    };

    eqEditor.focus = function () {
        setTimeout(function () {
            var focusableElement = eqEditor.children[0]; // get suim iframe element
            equationEditor.suim.focusToSuim();
            //Util.Dom.focus(focusableElement);
            //return false;
        }, 0);
    };

    item.eqEditor = eqEditor;

    //add components
    item.addComponent(eqWrapper);   // add editor board
    item.addComponent(eqEditor);

    /*
    equationEditor.setResponse('<math xmlns="http://www.w3.org/1998/Math/MathML" '
    + ' display="block"><mstyle displaystyle="true"><mn>7</mn><mo>-'
    + '</mo><msup><mrow><mn>5</mn></mrow><mrow><mn>2</mn></mrow></msup></mstyle></math>');

    alert(equationEditor.getResponse());
    */


    // NOTE: Disabled tab key because this is used for navigation now
    // var tabKey = new YAHOO.util.KeyListener(textArea, { keys: [9] }, { fn: tabFunc }, 'keypress');
    // tabKey.enable();

    // check for read-only
    var readOnlyFunc = function (evt) {
        if (item.isReadOnly()) {
            YUE.stopEvent(evt);
        }
    };

    YUE.on(equationContainer, 'keypress', readOnlyFunc);
    YUE.on(equationContainer, 'mousedown', readOnlyFunc);
    
    // set response if there is a value and it is MathML
    if (item.value != null && item.value.indexOf('MathML') != -1)
    {
        item.setResponse(item.value);
    }
});

ContentManager.onItemEvent('show', function (page, item) {
    if (!item.isResponseType('EquationEditor')) return;
    var equationEditor = item.equationEditor;
    suim = equationEditor.suim;
    
    // if (editor && editor.contentLoaded) editor.show();
});

// pre-config for the editor iframe component
Equation._editorContentLoaded = function (item, editor) {
    var suimDoc = editor.frameDocument();

    // add content manager events into editor
    ContentManager.addMouseEvents(item, suimDoc);
    ContentManager.addKeyEvents(suimDoc);
    YUE.on(suimDoc, 'mousedown', function () {
        item.setActive();
        item.setActiveComponent(item.eqEditor);
        ContentManager.focus(item.eqEditor);
    });
    suimDoc.focus = function () {
        setTimeout(function () {
            ContentManager.focus(item.eqEditor); // remove focus from the suim iframe
        }, 0);
    }
    // add zoom
    var zoom = item.getPage().getZoom();
    zoom.addDocument(suimDoc);
    zoom.refresh();
};

/*
 * get currently actived tab
 */
Equation._getActiveTab = function (tabs)
{
	var activeTab;
	for(var i=0;i<tabs.childElementCount;i++)
	{
		if(tabs.children[i].style.display!="none") {
			activeTab = tabs.children[i];
			return activeTab;
		}
	}
	return tabs.children[0].children[0];
};

Equation._keyDown = function(ev, symbolPanel) {
	// check if left/up/right/down
	if (!(ev.keyCode >= 37 && ev.keyCode <= 40)) return;

	// check if focused entity
    
    // setup shortcuts
    YUE.onFocus(symbolPanel, function(evt)
    {
        var target = YUE.getTarget(evt);

        if (target.nodeName == 'A')
        {
            symbolPanel.focused = target;
        }
    });
	
	// get current row (div)
	var divRow;
	if(symbolPanel.focused) divRow = symbolPanel.focused.parentNode.parentNode.parentNode;
	else divRow = symbolPanel;
	
	// up/down
	if (ev.keyCode == 38 || ev.keyCode == 40) {
		// get all the rows
		var divRows = [];
		divRows.push(symbolPanel.children[0]);  //add menu
		
		var activeTab = Equation._getActiveTab(symbolPanel.children[1]);
		YUD.batch(activeTab.getElementsByTagName('div'), function(div) {
			divRows.push(div);
		});

		//divRows.push(symbolPanel.children[2].children[0].children[0]);

		// create iterator and select row
		var rows = Util.Iterator(divRows);
		rows.jumpTo(divRow);

		// up
		if (ev.keyCode == 38) {
			rows.prev();
		}
			// down
		else if (ev.keyCode == 40) {
			rows.next();
		}

		divRow = rows.current();
	}

	var links = divRow.getElementsByTagName('a');

	// change dom container into array
	links = Util.Array.map(links, function(link) { return link; });

	// create iterator and select column
	var columns = Util.Iterator(links);
	columns.jumpTo(symbolPanel.focused);

	// left
	if (ev.keyCode == 37) {
		columns.prev();
	}
		// right
	else if (ev.keyCode == 39) {
		columns.next();
	}

	// if there is no column selected then reset to first one
	if (!columns.valid()) columns.reset();

	// focus on element
	var currentColumn = columns.current();
	Util.Dom.focus(currentColumn);
};
/********************************************************************/

// check if an items active component is the special characters component
Equation._isActiveComponent = function(element, item)
{
    //if (!item.editor) return; // no editor
    //if (!item.editor.specialCharacters) return; // no special characters

    var currentComponent = item.getActiveComponent();
    return (currentComponent && Util.Dom.isElement(currentComponent) && YUD.hasClass(currentComponent, 'eqWrapper'));
};

ContentManager.onItemEvent('keyevent', function(page, item, evt)
{
    if (!item.equationEditor) return;
    if (evt.type != 'keydown') return;
    if (Util.Event.hasModifier(evt)) return; // no modifiers
	
	if (Equation._isActiveComponent(item.htmlObj.children[0], item)) {
		//EquationEditor._keyDown(evt, page.getDoc().getElementById('allCats').parentNode);
		Equation._keyDown(evt, item.getActiveComponent());
	}
});

// Does this browser require the Mac OS X secure browser selection hack
Equation.requiresSelectionFix = function () {
    // check if mac and SB is less than 4.0
    return (Util.Browser.isMac() && Util.Browser.getSecureVersion() < 4.0);
};


// RESPONSE HANDLER: EQUATION EDITOR
(function()
{
    var getResponse = function(item, response)
    {
        // get the response
        response.value = item.equationEditor.getResponse();

        // check if the response is empty (NOTE: only tested for Firefox)
        var emptyMathML = '<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mstyle displaystyle="true"/></math>';
        var isEmpty = (response.value == emptyMathML);
        /*
        var syntaxchecking = item.equationEditor.syntaxchecking;
        
        if(syntaxchecking == "on") {
        
            // ************* more detail of syntax checking will be added here
            // check if we are currently in the middle of editing a formula
            var isEditing = (response.value.indexOf('editbox') != -1 || response.value.indexOf('div') != -1);

            // if the mathml is empty or we are in the middle of editing then do not consider the response valid
            response.isValid = (!isEmpty && !isEditing);
        }
        */
        //else {
            
        response.isValid = (!isEmpty);
        //}
        
        response.isSelected = response.isValid;
        /*if (response.value)
        {
            // escape closing tag for CDATA (bug #15742)
            response.value = response.value.replace(/]]>/g, ']]&gt;');
        }*/
    };

    var setResponse = function(item, value)
    {
        // get textarea
        var pageDoc = item.getPage().getDoc();
        //var equationContainer = pageDoc.getElementById('EquationEditor_' + item.position); // EquationEditor wrapper
        //showMathML();
        //equationContainer.value = value;
        item.equationEditor.setResponse(value);
    };

    ContentManager.registerResponseHandler('EquationEditor', getResponse, setResponse);

})();

