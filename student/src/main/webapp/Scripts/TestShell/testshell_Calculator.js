TestShell.Calculator = {};

TestShell.Calculator._calcMapping = {};
TestShell.Calculator._calcMapping['Basic'] = 'TDS_CalcBasic';
TestShell.Calculator._calcMapping['StandardMem'] = 'TDS_CalcStdMem';
TestShell.Calculator._calcMapping['Standard'] = 'TDS_CalcStd';
TestShell.Calculator._calcMapping['Scientific'] = 'TDS_CalcSci';
TestShell.Calculator._calcMapping['ScientificInv'] = 'TDS_CalcSciInv';
TestShell.Calculator._calcMapping['Graphing'] = 'TDS_CalcGraphing';
TestShell.Calculator._calcMapping['GraphingInv'] = 'TDS_CalcGraphingInv';
TestShell.Calculator._calcMapping['Matrices'] = 'TDS_CalcMatrices';
TestShell.Calculator._calcMapping['Regression'] = 'TDS_CalcRegress';

// get a calc acc code using a calc type name
TestShell.Calculator.getCode = function(calcType) {
	return TestShell.Calculator._calcMapping[calcType];
};

TestShell.Calculator.toggle = function() {
	var contentPage = ContentManager.getCurrentPage();
	if (contentPage == null)
		return;

	// check if we have the calculator tool
	var accProps = contentPage.getAccommodationProperties();
	if (accProps == null || !accProps.hasCalculator())
		return;

	// take the calc accs and them to calc types
	var calcModes = accProps.getCalculator();

	for ( var calcName in TestShell.Calculator._calcMapping) {
		var calcCode = TestShell.Calculator._calcMapping[calcName];
		calcModes = calcModes.replace(calcCode, calcName);
	}

	// create calc url
	var id = 'tool-calculator-' + calcModes.replace(/&/g, "-");
	var calcUrl = "../Scripts/Calculator/TDSCalculator.xhtml?mode="
			+ calcModes.replace("&amp;", ",");
	calcUrl = calcUrl.replace(/&/g, ",");

	var panel = TDS.ToolManager.get(id);

	// check if calc panel exists
	if (panel == null) {
		panel = TestShell.Calculator.create(id, calcUrl);
	}

	TDS.ToolManager.toggle(panel);
};

// a helper function for safely getting the current calc instance
TestShell.Calculator.getInstance = function(id) {
	var frameCalc = document.getElementById('frame-' + id);

	// make sure frame exists
	if (!frameCalc || !frameCalc.contentWindow)
		return null;

	// get calc window
	var winCalc = frameCalc.contentWindow;

	// make sure calc functions exists
	if (!YAHOO.lang.isFunction(winCalc.getWorkingCalcInstance))
		return null;

	// get calc instance
	var calcInstance = winCalc.getWorkingCalcInstance();
	if (calcInstance != null
			&& YAHOO.lang.isFunction(calcInstance.setInitKeyboardElement))
		return calcInstance;
	return null;
};

// sets the panel style for the current calc instance
TestShell.Calculator.setStyle = function(panel, calcInstance) {
	// clear all CSS
	for ( var calcName in TestShell.Calculator._calcMapping) {
		var calcCode = TestShell.Calculator._calcMapping[calcName];
		YUD.removeClass(panel.element, calcCode);
	}

	// add css for calc instance
	var currentCalcCode = TestShell.Calculator
			.getCode(calcInstance.config.name);

	if (currentCalcCode) {
		YUD.addClass(panel.element, currentCalcCode);
	}

	// update dimensions
	panel.refresh();
};

// create a calc dialog panel
TestShell.Calculator.create = function(id, calcUrl) {
	var headerText = window.Messages.getAlt('TestShell.Label.Calculator',
			'Calculator');

	// create calc panel
	var panel = TDS.ToolManager.createPanel(id, 'calculator', headerText, null);

	// load calculator into panel
	TDS.ToolManager.loadUrl(panel, calcUrl, function() {
		// add the client_<clientStylePath> to the body of the frame.
		YUD.addClass(panel.getFrame().contentDocument.body, 'client_'
				+ TDS.clientStylePath);

		// subscribe to calculator mode change
		var calcInstance = TestShell.Calculator.getInstance(id);

		if (calcInstance && calcInstance.parent) {
			// give javascript on the page time to init so there is a calc
			// instance ready
			setTimeout(function() {
				TestShell.Calculator.setStyle(panel, calcInstance);
			}, 0);

			// listen for when someone changes the current calc instance
			calcInstance.parent.CalcModeChange.subscribe(function(ev, arr) {
				var oldCalc = arr[0], newCalc = arr[1];
				TestShell.Calculator.setStyle(panel, newCalc);

				// make sure we are not out of bounds
				var panelX = panel.cfg.getProperty('x');
				var panelY = panel.cfg.getProperty('y');
				panel.moveTo(panelX, panelY);
			});
		}
	});

	// When the calculator is shown, we need to make sure that focus is on the
	// right element
	// NOTE: this event might not get fired the first time the calc shows it
	// seems..
	panel.showEvent.subscribe(function() {
		var calcInstance = TestShell.Calculator.getInstance(id);

		if (calcInstance) {
			// clear remaining focus visual indication (blue border)
			calcInstance.clearFocus(calcInstance.config.keyboardRegionDivs);

			// focus on panel. I have to put this into a timeout since the
			// overlayMgr
			// is going to be assinging focus to the panel on this event (look
			// at tds_toolManager.js)
			setTimeout(function() {
				calcInstance.setInitKeyboardElement();
			}, 1);
		}
	});

	return panel;
};
