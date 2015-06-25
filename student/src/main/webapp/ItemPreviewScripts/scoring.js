//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
var ItemScoringEngine = {};

ItemScoringEngine.DEFAULT_EXPLANATION = 'Score explanation not available.';

ItemScoringEngine.Events = null;
ItemScoringEngine.DevMode = false;

// call this to setup the scoring engine once the blackbox is ready
ItemScoringEngine.init = function() {
    this.Events = new Util.EventManager();
    this.DevMode = Util.QueryString.parse().dev == 'true';
    
};

ItemScoringEngine.isScorable = function(itemFormat) {
    return this._checkScorability(itemFormat) != 'NOTSCORED';
};

ItemScoringEngine._checkScorability = function(itemformat) {
    switch (itemformat) {
//        case "MC":
//        case "MS":
//            return 'LOCAL';
        default:
            return 'REMOTE';
    }   
};

ItemScoringEngine._localScore = function(itemFormat, response, rubric)
{
    if (itemFormat == 'MC' || itemFormat == 'MS') {
        var scorePoint = (response == rubric.data) ? 1 : 0; // MC scorer checks if response matches the rubric
        var score = new ItemScoringEngine.Score(scorePoint, 1, ItemScoringEngine.ScoringStatus.SCORED, null, null, null, null);
        score._explanationHTML = 'Your response earned ' + scorePoint + ' point of a possible maximum of 1.';
        setTimeout(function() {
            ItemScoringEngine.Events.fire('onItemScoreResponse', new ItemScoringEngine.ItemScoreResponse(score));
        }, 0);
    } 
};

// a helper function for sending a item score request to the scoring engine
// example: ItemScoringEngine.createItemScoreRequestXml('MC', '100', 'A', 'c:\\item.grx')
ItemScoringEngine.sendItemScoreRequest = function(itemFormat, itemID, studentResponse, rubric, contextToken) {

    if (!this.isScorable(itemFormat)) {
        return;
    }

    // Item is locally scored
    if (this._checkScorability(itemFormat) == 'LOCAL') {
        this._localScore(itemFormat, studentResponse, rubric);
        return;
    }

    // Item is remotely scored on item scoring server.
    var responseInfo = new ItemScoringEngine.ResponseInfo(itemFormat, itemID, studentResponse, rubric, contextToken);
    var itemScoreRequest = new ItemScoringEngine.ItemScoreRequest(responseInfo);

    var xmlDoc = itemScoreRequest.createXml();
    var xmlString = Util.Xml.serializeToString(xmlDoc);

    var callback = {
        success: ItemScoringEngine.receiveItemScoreResponse,
        failure: ItemScoringEngine.receiveItemScoreFailure,
        scope: ItemScoringEngine
    };

    YAHOO.util.Connect.initHeader('Content-Type', 'text/xml');
    YAHOO.util.Connect.asyncRequest('POST', window.ItemScoringServerUrl, callback, xmlString);

    TDS.Dialog.showProgress();
};

ItemScoringEngine.receiveItemScoreResponse = function(xhrObj) {
    TDS.Dialog.hideProgress();
    var itemScoreResponse = new ItemScoringEngine.ItemScoreResponse();
    itemScoreResponse.readXml(xhrObj.responseXML);
    ItemScoringEngine.Events.fire('onItemScoreResponse', itemScoreResponse);
};

ItemScoringEngine.receiveItemScoreFailure = function(xhrObj) {
    TDS.Dialog.hideProgress();
    var errorMessage = ItemScoringEngine.DEFAULT_EXPLANATION; // +' [' + xhrObj.statusText + ']';
    ItemPreview.showAlert('Score Failure', errorMessage);
};

/********************************************/

// Class that is used to carry info about the item and student response 
ItemScoringEngine.ResponseInfo = function(itemFormat, itemID, studentResponse, rubric, contextToken) {
    this._itemFormat = itemFormat;
    this._itemID = itemID;
    this._studentResponse = studentResponse;
    this._rubric = rubric;
    this._contextToken = contextToken;
};

// Item format
ItemScoringEngine.ResponseInfo.prototype.getItemFormat = function() { return this._itemFormat; };

// Unique ID for the item (likely the ITS id)
ItemScoringEngine.ResponseInfo.prototype.getItemIdentifier = function() { return this._itemID; };

// Student response
ItemScoringEngine.ResponseInfo.prototype.getStudentResponse = function() { return this._studentResponse; };

// Rubric information for the scorer
ItemScoringEngine.ResponseInfo.prototype.getRubric = function() { return this._rubric; };

// Placeholder to associate add'l info related to this student response (such as testeeid, position etc)
ItemScoringEngine.ResponseInfo.prototype.getContextToken = function() { return this._contextToken; };

/********************************************/

ItemScoringEngine.ScoringStatus =
{
    NOTSCORED: 0,
    SCORED: 1,
    WAITINGFORMACHINESCORE: 2,
    NOSCORINGENGINE: 3,
    SCORINGERROR: 4
};

// Enum to represent the status of the scoring operation
ItemScoringEngine.Score = function(scorePoint /*int*/, maxScore/*int*/, status /*ScoringStatus*/, dimension /*string*/, scoreRationale /*xmlnode*/, childScores /*Score[]*/, contextToken /*string*/) {
    this._scorePoint = scorePoint;
    this._status = status;
    this._dimension = dimension;
    this._scoreRationale = scoreRationale;
    this._childScores = childScores;
    this._contextToken = contextToken;
    this._maxScore = maxScore;
    this._explanationHTML = "<b>Error scoring your response.</b>";

    if (this._scoreRationale != null && status == ItemScoringEngine.ScoringStatus.SCORED) {
        
        var htmlBuilder = [];

        // helper function for adding to the html with C# style formatter
        var html = function () {
            var str = Util.String.format.apply(this, arguments);
            htmlBuilder.push(str);
        };
       
        if (this._maxScore > -1 && this._scorePoint > -1) {
            html('<b>Your response earned <font color="blue">{0}</font> point(s) of a possible {1}</b>', this._scorePoint, this._maxScore);
        } else if (this._scorePoint > -1) {
            html('<b>Your response earned <font color="blue">{0}</font> point(s)</b>', this._scorePoint);
        } else {
            html('<b>Your response could not be scored</b>');
        }
        
        // table
        html('<table border="1">');
        var propositionsNode = Util.Dom.queryTag('Propositions', scoreRationale);
        if (propositionsNode != null) {
            // columns
            html('<thead><tr>');
            html('<th><b>Description</b></th>');
            html('<th><b>Your answer</b></th>');
            html('</tr></thead>');

            // rows
            html('<tbody>');
            Util.Dom.queryTagsBatch('Proposition', propositionsNode, function (propNode) {
                html('<tr>');
                html('<td>{0}</td>', propNode.getAttribute('description'));
                html('<td align="center" class="{0}">{0}</td>', propNode.getAttribute('state'));
                html('</tr>');
            });
            html('</tbody>');
        }
        html('</table>');
        
        var bindingsNode = Util.Dom.queryTag('Bindings', scoreRationale);
        if (bindingsNode != null && bindingsNode.childNodes.length > 0) {
            // table
            html('<br/><table border="1"><tbody>');
            
            // columns
            html('<thead><tr>');
            html('<th align="left"><b>Binding Name</b></th>');
            html('<th align="left"><b>Binding Value</b></th>');
            html('</tr></thead>');

            // rows
            html('<tbody>');
            Util.Dom.queryTagsBatch('Binding', bindingsNode, function (bindNode) {
                html('<tr>');
                html('<td align="left">{0}</td>', bindNode.getAttribute('name'));
                html('<td align="left">{0}</td>', bindNode.getAttribute('value'));
                html('</tr>');
            });
            html('<tbody>');
            
            html('</table>');
        }
        
        this._explanationHTML = htmlBuilder.join('');
    }
};

// Score point for this dimension
ItemScoringEngine.Score.prototype.getScorePoint = function() {
    return this._scorePoint;
};

// Status of this score
ItemScoringEngine.Score.prototype.getStatus = function() {
    return this._status;
};

// Dimension that this score is for
ItemScoringEngine.Score.prototype.getScoringDimension = function() {
    return this._dimension;
};

// Rationale for this score for this dimension
ItemScoringEngine.Score.prototype.getScoringRationale = function() {
    return this._rationale;
};

// Any children (sub-dimensional) scores associated with this compound score
ItemScoringEngine.Score.prototype.getSubScores = function() {
    return this._childScores;
};

// Placeholder to associate add'l info related to this student response (such as testeeid, position etc)
ItemScoringEngine.Score.prototype.getContextToken = function() {
    return this._contextToken;
};

// HTML score explanation returned from the server 
ItemScoringEngine.Score.prototype.getExplanationHTML = function() {
    return this._explanationHTML;
};

/********************************************/

// A class used for transporting a scoring request.
ItemScoringEngine.ItemScoreRequest = function(responseInfo /*ResponseInfo*/) {
    this._responseInfo = responseInfo;
};

ItemScoringEngine.ItemScoreRequest.prototype.getResponseInfo = function() {
    return this._responseInfo;
};

ItemScoringEngine.ItemScoreRequest.prototype.createXml = function() {
    var responseInfo = this.getResponseInfo();

    // <ItemScoreRequest>
    var xmlDoc = Util.Xml.parseFromString('<ItemScoreRequest></ItemScoreRequest>');
    var itemScoreRequestNode = xmlDoc.documentElement;

    // <ResponseInfo>
    var responseInfoNode = xmlDoc.createElement('ResponseInfo');
    responseInfoNode.setAttribute('itemIdentifier', responseInfo.getItemIdentifier());
    responseInfoNode.setAttribute('itemFormat', responseInfo.getItemFormat());
    itemScoreRequestNode.appendChild(responseInfoNode);

    // <StudentResponse>
    var studentResponseNode = xmlDoc.createElement('StudentResponse');
    var studentResponseData = xmlDoc.createCDATASection(responseInfo.getStudentResponse());
    studentResponseNode.appendChild(studentResponseData);
    responseInfoNode.appendChild(studentResponseNode);

    // <Rubric>
    var rubric = responseInfo.getRubric();
    var rubricNode = xmlDoc.createElement('Rubric');

    if (rubric.type == 'Text') {
        rubricNode.setAttribute('type', 'Data');
        rubricNode.setAttribute('cancache', 'false');
        rubricNode.appendChild(xmlDoc.createCDATASection(rubric.data));
    } else {
        rubricNode.setAttribute('type', 'Uri');
        rubricNode.setAttribute('cancache', 'false');
        rubricNode.appendChild(xmlDoc.createTextNode(rubric.data));
    }

    responseInfoNode.appendChild(rubricNode);

    // <ContextToken>
    if (responseInfo.getContextToken() != null) {
        var contextTokenNode = xmlDoc.createElement('ContextToken');
        var contextTokenData = xmlDoc.createCDATASection(responseInfo.getContextToken());
        contextTokenNode.appendChild(contextTokenData);
        responseInfoNode.appendChild(contextTokenNode);
    }

    // Ask for all bindings
    if (ItemScoringEngine.DevMode) {    
        var requestedBindings = xmlDoc.createElement("OutgoingBindings");
        responseInfoNode.appendChild(requestedBindings);

        var requestedBinding = xmlDoc.createElement("Binding");
        requestedBinding.setAttribute('name', '*');
        requestedBinding.setAttribute('type', '');
        requestedBinding.setAttribute('value', '');
        requestedBindings.appendChild(requestedBinding);
    }
    
    return xmlDoc;
};

/********************************************/

// A class used for transporting a scoring response.
ItemScoringEngine.ItemScoreResponse = function(score /*Score*/) {
    this._score = score;
};

ItemScoringEngine.ItemScoreResponse.prototype.getScore = function() { return this._score; };
ItemScoringEngine.ItemScoreResponse.prototype.getContextToken = function() { return this._contextToken; };

ItemScoringEngine.ItemScoreResponse.prototype.readXml = function(xmlDoc) {
    // <ScoreInfo>
    var itemScoreResponseNode = Util.Dom.queryTag('ItemScoreResponse', xmlDoc);
    var scoreNode = Util.Dom.queryTag('Score', itemScoreResponseNode);
    var scoreInfoNode = Util.Dom.queryTag('ScoreInfo', scoreNode);

    var scorePoint = Util.Xml.getAttributeInt(scoreInfoNode, 'scorePoint');
    var maxScore = Util.Xml.getAttributeInt(scoreInfoNode, 'maxScore');
    var dimension = scoreInfoNode.getAttribute('scoreDimension');

    var status = ItemScoringEngine.ScoringStatus.SCORINGERROR;
    var statusName = scoreInfoNode.getAttribute('scoreStatus');

    switch (statusName.toUpperCase()) {
        case "NOTSCORED":
            status = ItemScoringEngine.ScoringStatus.NOTSCORED;
            break;
        case "SCORED":
            status = ItemScoringEngine.ScoringStatus.SCORED;
            break;
        case "WAITINGFORMACHINESCORE":
            status = ItemScoringEngine.ScoringStatus.WAITINGFORMACHINESCORE;
            break;
        case "NOSCORINGENGINE":
            status = ItemScoringEngine.ScoringStatus.NOSCORINGENGINE;
            break;
        case "SCORINGERROR":
            status = ItemScoringEngine.ScoringStatus.SCORINGERROR;
            break;
    }

    // <ScoreRationale>
    var rationale = Util.Dom.queryTag('ScoreRationale', scoreInfoNode);

    // <SubScoreList>
    var childScores = [];

    // <ContextToken>
    var contextToken = Util.Xml.getCData(scoreNode, 'ContextToken');

    this._score = new ItemScoringEngine.Score(scorePoint, maxScore, status, dimension, rationale, childScores, contextToken);
};

/********************************************/
// this is item preview usage of item scoring code:

ItemPreview.initItemScoringEngine = function() {
    ItemScoringEngine.init();

    ItemScoringEngine.Events.subscribe('onItemScoreResponse', function(itemScoreResponse) {
        var score = itemScoreResponse.getScore();
        ItemPreview.showAlert('Score Result', score.getExplanationHTML());
    });
};

// score the current item
ItemPreview.score = function() {
    // get current page
    var currentPage = ContentManager.getCurrentPage();
    if (currentPage == null) {
        return;
    }

    var activeEntity = currentPage.getActiveEntity();

    // check if passage
    if (activeEntity instanceof BlackboxWin.ContentPassage) {
        ItemPreview.showAlert('Score Status', 'Cannot score a passage.');
        return;
    }

    // get response
    var response = activeEntity.getResponse();

    if (response == null) {
        ItemPreview.showAlert('Score Status', 'No response handler for this item type.');
        return;
    }

    if (ItemScoringEngine.isScorable(activeEntity.format)) {
        if (activeEntity.rubric == null) {
            ItemPreview.showAlert('Score Status', 'This item is not currently scored automatically.');
            return;
        }

        ItemScoringEngine.sendItemScoreRequest(activeEntity.format, activeEntity.itemKey, response.value, activeEntity.rubric, null);
    } else {
        ItemPreview.showAlert('Score Status', 'This item is not currently scored automatically.');
    }
};