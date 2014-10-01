Sections.TestVerify = function()
{
    Sections.TestVerify.superclass.constructor.call(this, 'sectionTestVerify');

    this.addClick('btnApproveAccommodations', this.approve);
    this.addClick('btnWrongAccommodations', this.deny);
};

YAHOO.lang.extend(Sections.TestVerify, Sections.Base);

/*
Sections.TestVerify.prototype.init = function()
{
    var ddlTestForms = YUD.get('ddlTestForms');

    YUE.on(ddlTestForms, 'change', function(evt)
    {
        LoginShell.formSelection = (ddlTestForms.value != '') ? ddlTestForms.value : null;
    });
};
*/

Sections.TestVerify.prototype.load = function(approval)
{
    var segmentsAccommodations = approval.segmentsAccommodations;

    // clear the segments container
    var segmentsContainer = YUD.get('summary-segments');
    segmentsContainer.innerHTML = '';

    // set student info
    YUD.get('lblVerifySessionID').innerHTML = LoginShell.session.id;

    // show form selector if proxy mode
    this.renderForms(LoginShell.testForms);

    // remove global accommodations css
    TDS.globalAccommodations.removeCSS(document.body);

    // add test accommodations css
    segmentsAccommodations[0].applyCSS(document.body);

    // render accommodations
    Util.Array.each(segmentsAccommodations, this.renderAccommodations, this);
};

Sections.TestVerify.prototype.renderForms = function(testForms)
{
    var verifyForm = YUD.get('verifyTestForm');
    if (verifyForm == null) return;

    var verifyFormSelector = verifyForm.getElementsByTagName('select')[0];
    if (verifyFormSelector == null) return;

    // clear existing forms
    verifyFormSelector.options.length = 0; 

    // check if any forms
    if (testForms == null || testForms.length == 0)
    {
        YUD.setStyle(verifyForm, 'display', 'none');
        return;
    }

    verifyFormSelector[0] = new Option('Select a form', '');

    // add test forms
    for (var i = 0; i < testForms.length; i++)
    {
        var testForm = testForms[i];
        verifyFormSelector[i + 1] = new Option(testForm.id, testForm.key);
    }
    
    YUD.setStyle(verifyForm, 'display', 'block');
};

Sections.TestVerify.prototype.renderAccommodations = function(segmentAccommodations)
{
    // skip accommodations that have nothing visible
    if (!segmentAccommodations.isAnyVisible()) return;
    
    var segmentsContainer = YUD.get('summary-segments');

    // create segment container
    var segmentContainer = HTML.DIV({ 'className': 'summary-segment' });
    segmentsContainer.appendChild(segmentContainer);

    // create segment header
    var segmentHeader = HTML.H3(null, segmentAccommodations.getLabel());
    segmentContainer.appendChild(segmentHeader);

    // render approved accommodations
    var accTypes = segmentAccommodations.getTypes();

    Util.Array.each(accTypes, function(accType)
    {
        // only show visible
        if (accType.isVisible())
        {
            this.renderAccType(segmentContainer, accType);
        }

    }, this);
};

Sections.TestVerify.prototype.renderAccType = function(segmentContainer, accType)
{
    var typeLabel = accType.getLabel() + ':';
    var valuesLabel = this.getTypeValuesLabel(accType);

    // create html for acc summary
    var spanTypeLabel = HTML.SPAN({ 'class': 'summary-segment-type-label' }, typeLabel);
    var spanValuesLabel = HTML.SPAN({ 'class': 'summary-segment-type-values' }, valuesLabel);

    var row = HTML.DIV({ 'className': 'summary-segment-type' }, spanTypeLabel, spanValuesLabel);

    segmentContainer.appendChild(row);
};

Sections.TestVerify.prototype.getTypeValuesLabel = function(accType)
{
    var valueNames = [];

    var accValues = accType.getValues();

    for (var i = 0; i < accValues.length; i++)
    {
        var accValue = accValues[i];
        valueNames.push(accValue.getLabel());
    }

    return valueNames.join(', ');
};

// Get the selected test form key.
// - NULL = wasn't possible for user to select form
// - '' = user did not choose anything
// - 'some value' = form selected
Sections.TestVerify.prototype.getSelectedTestForm = function()
{
    var ddlTestForms = YUD.get('ddlTestForms');
    return (ddlTestForms.options.length > 0) ? ddlTestForms.value : null;
};

Sections.TestVerify.prototype.approve = function()
{
    // get the selected form key
    var selectedFormKey = this.getSelectedTestForm();

    // validate selected form key if it exists
    if (YAHOO.lang.isString(selectedFormKey))
    {
        // check if a form was selected
        if (selectedFormKey == '')
        {
            var defaultError = 'Must select a test form';
            TDS.Dialog.showWarning(Messages.getAlt('LoginShell.Alert.MustSelectForm', defaultError));
            return;
        }

        // if the testee has allowed form keys then make sure one of them match what was selected
        var formKeys = LoginShell.testeeForms;
        
        if (YAHOO.lang.isArray(formKeys) && (formKeys.length > 0) && formKeys.indexOf(selectedFormKey) == -1)
        {
            var defaultError = 'The selected test form does not match what is allowed for the student';
            TDS.Dialog.showWarning(Messages.getAlt('LoginShell.Alert.FormSelectionInvalid', defaultError));
            return;
        }

        LoginShell.formSelection = selectedFormKey;
    }

    // apply accommodations and go to test instructions page
    this.applyAccommodations();
    this.request('next');
};

// this function is used for applying any accommodations
Sections.TestVerify.prototype.applyAccommodations = function() {
    var accProps = TDS.getAccommodationProperties();
    if (accProps && accProps.isPermissiveModeEnabled()) {
        Util.SecureBrowser.enablePermissiveMode(true);
    }
};

Sections.TestVerify.prototype.deny = function()
{
    // reset CSS on body
    LoginShell.resetCSS();
    
    // remove accommodations
    LoginShell.clearTestAccommodations();

    // go back
    var self = this;
    LoginShell.api.denyApproval(function()
    {
        self.request('back');
    });
};
