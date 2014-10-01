/**************/
/* PROPERTIES */
/**************/

// create a strongly typed wrapper for an accommodations data structure which assumes all accommodations in the data structure are selected
Accommodations.Properties = function(accommodations)
{
    this._accommodations = accommodations;
};

// get the first selected value for a type
Accommodations.Properties.prototype.getSelectedValue = function(typeName)
{
    if (this._accommodations == null) return null;
    var accType = this._accommodations.getType(typeName);

    if (accType != null)
    {
        var selectedValues = accType.getSelected();

        if (selectedValues.length > 0)
        {
            return selectedValues[0];
        }
    }

    return null;
};

// get the first selected value code for a type
Accommodations.Properties.prototype.getSelectedCode = function(type)
{
    var selectedValue = this.getSelectedValue(type);
    return (selectedValue) ? selectedValue.getCode() : null;
};

// check if a acc value code is selected
Accommodations.Properties.prototype.isSelected = function(type, code)
{
    if (this._accommodations == null) return false;
    var foundValue = this._accommodations.findCode(type, code, true);
    return (foundValue != null);
};

// check if a acc type exists and if it does not equal the selected types code
Accommodations.Properties.prototype.existsAndNotEquals = function(type, code)
{
    // get selected types code
    var selectedCode = this.getSelectedCode(type);

    // make sure there is something selected and if there is that it doesn't equal the valye passed in
    return (selectedCode != null && selectedCode != code);
};

/***
Below is a list of helper functions for getting known accommodations
***/

// get selected language code
Accommodations.Properties.prototype.getLanguage = function()
{
    var language = this.getSelectedCode('Language');
    return (language) ? language : 'ENU';
};

// get all languages
Accommodations.Properties.prototype.getLanguages = function()
{
    var accType = this._accommodations.getType('Language');
    return (accType != null) ? accType.getCodes() : ['ENU'];
};

Accommodations.Properties.prototype.hasBraille = function()
{
    // for legacy ITS reasons check braille accommodation
    if (this.isSelected('Braille', 'TDS_Braille1')) return true;
    
    // otherwise check if language has braille support
    var language = this.getLanguage();
    return Util.String.endsWith(language, '-Braille');
};

Accommodations.Properties.prototype.getBrailleType = function() {
    return this.getSelectedCode('Braille Type');
};

Accommodations.Properties.prototype.getCalculator = function() { return this.getSelectedCode('Calculator'); };
Accommodations.Properties.prototype.getColorChoice = function() { return this.getSelectedCode('Color Choices'); };
Accommodations.Properties.prototype.getFormula = function() { return this.getSelectedCode('Formula'); };
Accommodations.Properties.prototype.getFontSize = function() { return this.getSelectedCode('Font Size'); };
Accommodations.Properties.prototype.getFontType = function() { return this.getSelectedCode('Font Type'); };
Accommodations.Properties.prototype.hasGuideForRevision = function() { return this.isSelected('Guide to Revision', 'TDS_GfR1'); };
Accommodations.Properties.prototype.hasHighlighting = function() { return this.isSelected('Highlight', 'TDS_Highlight1'); };
Accommodations.Properties.prototype.hasMarkForReview = function() { return this.isSelected('Mark for Review', 'TDS_MfR1'); };
Accommodations.Properties.prototype.hasPrintItem = function() { return this.isSelected('Print on Request', 'TDS_PoD_Item'); };
Accommodations.Properties.prototype.hasPrintStimulus = function() { return this.isSelected('Print on Request', 'TDS_PoD_Stim'); };
Accommodations.Properties.prototype.getPrintSize = function() { return this.getSelectedCode('Print Size'); };
Accommodations.Properties.prototype.getPeriodicTable = function() { return this.getSelectedCode('Periodic Table'); };
Accommodations.Properties.prototype.hasStrikethrough = function() { return this.isSelected('Strikethrough', 'TDS_ST1'); };
Accommodations.Properties.prototype.hasTutorial = function () { return this.isSelected('Tutorial', 'TDS_T1'); };
Accommodations.Properties.prototype.hasASIVoiceGuidance = function() { return this.isSelected('Scaffolding Voice Guidance', 'TDS_ASIGuide1'); };

Accommodations.Properties.prototype.hasTTSInstruction = function() { return this.isSelected('TTS', 'TDS_TTS_Inst'); };
Accommodations.Properties.prototype.hasTTSItem = function() { return this.isSelected('TTS', 'TDS_TTS_Item'); };
Accommodations.Properties.prototype.hasTTSStimulus = function() { return this.isSelected('TTS', 'TDS_TTS_Stim'); };
Accommodations.Properties.prototype.hasTTS = function()
{
    return this.existsAndNotEquals('TTS', 'TDS_TTS0');
};
Accommodations.Properties.prototype.hasTTSPausing = function () {
    return this.isSelected('TTS Pausing', 'TDS_TTSPause1');
};

Accommodations.Properties.prototype.hasCalculator = function() 
{
    return this.existsAndNotEquals('Calculator', 'TDS_Calc0');
};

Accommodations.Properties.prototype.hasColorChoice = function()
{
    return this.existsAndNotEquals('Color Choices', 'TDS_CC0');
};

Accommodations.Properties.prototype.hasFormula = function()
{
    return this.existsAndNotEquals('Formula', 'TDS_F0');
};

Accommodations.Properties.prototype.hasPeriodicTable = function()
{
    return this.existsAndNotEquals('Periodic Table', 'TDS_PT0');
};

Accommodations.Properties.prototype.hasStudentComments = function()
{
    return this.existsAndNotEquals('Student Comments', 'TDS_SC0');
};

Accommodations.Properties.prototype.getStudentComments = function()
{
    return this.getSelectedCode('Student Comments');
};

// get the type of navigation drop down that should be down on the test shell
// 'TDS_Nav0' = No Dropdown
// 'TDS_NavQu' = Show Question Numbers
// 'TDS_NavPg' = Show Page Numbers
// 'TDS_NavTk' = Show Task Numbers
Accommodations.Properties.prototype.getNavigationDropdown = function()
{
    return this.getSelectedCode('Navigation Dropdown');
};

Accommodations.Properties.prototype.getTTXBusinessRules = function()
{
    return this.getSelectedCode('TTX Business Rules');
};

// when answering a question do we show visual feedback (rationale)
Accommodations.Properties.prototype.showFeedback = function() //does show feedback
{
    // NOTE: this acc type/value might be changed later
    return this.isSelected('Feedback', 'LPN_FB1');
};

// do we show and enable the expandable passages link
Accommodations.Properties.prototype.showExpandablePassages = function()
{
    return this.isSelected('Expandable Passages', 'TDS_ExpandablePassages1');
};

// In the sound check page, do we show the volume, pitch, and/or rate controls
Accommodations.Properties.prototype.hasAudioControl = function () {   
    return this.existsAndNotEquals('TTS Audio Adjustments', 'TDS_TTSAA0');
};

// In the sound check page, do we show the voice pack control 
Accommodations.Properties.prototype.showVoicePackControl = function () {
    return this.isSelected('TTS Audio Adjustments', 'TDS_TTSAA_SelectVP');
};

// In the sound check page, do we show the volume control 
Accommodations.Properties.prototype.showVolumeControl = function () {
    return this.isSelected('TTS Audio Adjustments', 'TDS_TTSAA_Volume');
};

// In the sound check page, do we show the pitch control
Accommodations.Properties.prototype.showPitchControl = function () {
    return this.isSelected('TTS Audio Adjustments', 'TDS_TTSAA_Pitch');
};

// In the sound check page, do we show the rate control
Accommodations.Properties.prototype.showRateControl = function () {
    return this.isSelected('TTS Audio Adjustments', 'TDS_TTSAA_Rate');
};

// if true then you can only pause if the page has all questions answered
Accommodations.Properties.prototype.blockPausing = function () {
    return this.isSelected('Block Pausing', 'TDS_BP1');
};

// if true then you can only pause if the page has all questions answered
Accommodations.Properties.prototype.hasResponseReset = function () {
    return this.isSelected('Item Response Reset', 'TDS_ItemReset1');
};

// check if we should load the classic test shell
Accommodations.Properties.prototype.isTestShellClassic = function () {
    return this.isSelected('Test Shell', 'TDS_TS_Classic');
};

// check if we should load the modern test shell
Accommodations.Properties.prototype.isTestShellModern = function () {
    return this.isSelected('Test Shell', 'TDS_TS_Modern');
};

// check if we should load the modern test shell
Accommodations.Properties.prototype.isTestShellAccessibility = function () {
    return this.isSelected('Test Shell', 'TDS_TS_Accessibility');
};

// if this is true you are required to visit a page before it is considered completed
Accommodations.Properties.prototype.requirePageVisit = function () {
    return this.isSelected('Require Page Visit', 'TDS_RPV1');
};

Accommodations.Properties.prototype.hasASL = function () {
    return this.isSelected('American Sign Language', 'TDS_ASL1');
};

// If this is true then hide the final test score from the student on the results page.
Accommodations.Properties.prototype.hideTestScore = function () {
    return this.isSelected('Suppress Score', 'TDS_SS1');
};

// If this is true then we show the item score report summary on the test results page.
Accommodations.Properties.prototype.showItemScoreReportSummary = function () {
    return this.isSelected('Item Score Report', 'TDS_ISR_Summary');
};

Accommodations.Properties.prototype.showItemScoreReportResponses = function () {
    return this.isSelected('Item Score Report', 'TDS_ISR_SumViewResp');
};

Accommodations.Properties.prototype.isPermissiveModeEnabled = function () {
    return this.isSelected('Permissive Mode', 'TDS_PM1');
};

// check if fullscreen button is showing on test shell
Accommodations.Properties.prototype.hasFullScreenEnabled = function () {
    return this.isSelected('Fullscreen', 'TDS_FS_On') ||
           this.isSelected('Fullscreen', 'TDS_FS_Pass');
};

// check if password is required to exit from fullscreen
Accommodations.Properties.prototype.hasFullScreenPassword = function () {
    return this.isSelected('Fullscreen', 'TDS_FS_Pass');
};

Accommodations.Properties.prototype.hasSoundPlayCheck = function ()
{
    return this.isSelected('Hardware Checks', 'TDS_HWPlayback');
};

Accommodations.Properties.prototype.hasRecorderCheck = function()
{
    return this.isSelected('Hardware Checks', 'TDS_HWRecording');
};

Accommodations.Properties.prototype.hasSoundCheck = function()
{
    return this.existsAndNotEquals('Hardware Checks', 'TDS_HW0');
};

Accommodations.Properties.prototype.isAutoMute = function ()
{
    return this.isSelected('Mute System Volume', 'TDS_Mute1');
};

// check if masking tool is showing on test shell
Accommodations.Properties.prototype.hasMaskingEnabled = function () {
    return this.isSelected('Masking', 'TDS_Masking1');
};

// check if audio rewind is set
Accommodations.Properties.prototype.isAudioRewindEnabled = function () {
    return this.isSelected('Audio Playback Controls', 'TDS_APC_PSP');
};

// check if paginate item groups is enabled
Accommodations.Properties.prototype.isPaginatedItemGroupsEnabled = function () {
    return this.isSelected('Paginate Item Groups', 'TDS_PIG1');
};

// check if reverse contrast color scheme is selected.
Accommodations.Properties.prototype.hasReverseContrast = function () {
    return this.isSelected('Color Choices', 'TDS_CCInvert');
};

/* 
What kind of test progress indicator to show?
•	TDS_TPI_None – Hide progress indicator.
•	TDS_TPI_Responses – Shows “1 out of 20”. This is the current way of doing it and we default to this if acc is missing.
•	TDS_TPI_ResponsesFix – Shows “1 out of 20” and performs test length adjust when test is completed. 
•	TDS_TPI_Percent – Show “10% completed” and performs test length adjust when test is completed. 
*/
Accommodations.Properties.prototype.getTestProgressIndicator = function () {
    return this.getSelectedCode('Test Progress Indictator') || 'TDS_TPI_Responses';
};

// check if system volume control is enabled
Accommodations.Properties.prototype.hasSystemVolumeControl = function () {
    return this.isSelected('System Volume Control', 'TDS_SVC1');
};

Accommodations.Properties.prototype.isDictionaryEnabled = function () {
    return this.existsAndNotEquals('Dictionary', 'TDS_Dict0');
};

Accommodations.Properties.prototype.getDictionary = function () {
    return this.getSelectedCode('Dictionary');
};

Accommodations.Properties.prototype.getDictionaryOptions = function () {
    var accType = this._accommodations.getType('Dictionary Options');
    return (accType != null) ? accType.getCodes() : [];
};

Accommodations.Properties.prototype.isThesaurusEnabled = function () {
    return this.existsAndNotEquals('Thesaurus', 'TDS_TH0');
};

Accommodations.Properties.prototype.getThesaurus = function () {
    return this.getSelectedCode('Thesaurus');
};

Accommodations.Properties.prototype.getThesaurusOptions = function () {
    var accType = this._accommodations.getType('Thesaurus Options');
    return (accType != null) ? accType.getCodes() : [];
};

Accommodations.Properties.prototype.isTTSTrackingEnabled = function () {
    return this.existsAndNotEquals('TTS Tracking', 'TDS_TTSTracking0');    
};

Accommodations.Properties.prototype.isSecurityBreachDetectionEnabled = function () {    
    return this.isSelected('Enhanced Security Options', 'TDS_ESO_DetectBrowserBreach') && !this.isPermissiveModeEnabled();
};
