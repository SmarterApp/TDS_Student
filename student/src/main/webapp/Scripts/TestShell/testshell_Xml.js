TestShell.Xml = {};

(function(TestShell) {

    function createRequest(id, timestamp, responses) {
        
        var xml = '<request action="update" eventID="' + id + '" timestamp="' + timestamp + '">';

        if (YAHOO.util.Lang.isArray(responses)) {
            xml += serializeResponses(responses);
        }

        xml += '</request>';
        return xml;
    };

    // serialize a collection of responses into XML to send to the server
    function serializeResponses(responses) {
        
        var xml = '<responseUpdates>';

        Util.Array.each(responses, function(response) {
            xml += serializeResponse(response);
        });

        xml += "</responseUpdates>";

        return xml;
    };

    // serialize a collection of responses into XML to send to the server
    function serializeResponse(response) {
        
        var xml = '<responseUpdate ';
        xml += 'id="' + response.id + '" ';
        xml += 'itsBank="' + response.itsBank + '" ';
        xml += 'itsItem="' + response.itsItem + '" ';
        xml += 'segmentID="' + response.group.segmentID + '" ';
        xml += 'page="' + response.pageNum + '" ';
        xml += 'position="' + response.position + '" ';
        xml += 'sequence="' + response.sequence + '" ';
        xml += 'dateCreated="' + response.dateCreated + '" ';
        xml += 'isSelected="' + response.isSelected + '" ';
        xml += 'isValid="' + response.isValid + '" ';
        xml += '>';

        var item = response.getItem();

        // add file path
        xml += '<filePath>' + (item.filePath || '') + '</filePath>';

        // add response value
        xml += '<value>';

        if (response.value != null) {
            if (typeof response.value == 'string') {
                // escape closing tag for CDATA
                var responseValue = response.value.replace(/]]>/g, ']]&gt;');
                xml += '<![CDATA[' + responseValue + ']]>';
            } else {
                // get serialized value and throw error
                var serialized = response.value;
                try {
                    serialized = JSON.stringify(serialized);
                } catch(ex) {
                    // serializing was only for debugging so ignore
                }
                throw new Error('Cannot generate response xml because of invalid value property type: ' + serialized);
            }
        }

        xml += '</value>';

        xml += '</responseUpdate>';

        return xml;
    };
    
    TestShell.Xml.createRequest = createRequest;

})(TestShell);


/*************************************************************************************************/

(function(TestShell) {

    // add helper functions
    var getNode = Util.Dom.queryTag;
    var getNodes = Util.Dom.queryTags;
    var batchNodes = Util.Dom.queryTagsBatch;
    var selectNode = Util.Dom.querySelector;
    var selectNodes = Util.Dom.querySelectorAll;
    var getAttribInt = Util.Xml.getAttributeInt;
    var getAttribBool = Util.Xml.getAttributeBool;
    var getAttrib = Util.Xml.getAttribute;
    var getText = Util.Xml.getNodeText;
    var getCData = Util.Xml.getCData;

    function validResults(xmlDoc) {
        if (xmlDoc == null) return false;
        var nodeResults = getNode('results', xmlDoc);
        if (nodeResults == null) return false;
        return true;
    };

    function parseResults(xmlDoc) {
        
        if (xmlDoc == null) return null;
        var resultsNode = getNode('results', xmlDoc);
        if (resultsNode == null) return null;

        var results = {
            eventID: getAttribInt(resultsNode, 'eventID'),
            machineID: getAttrib(resultsNode, 'machineID'),
            timestamps: readTimestamps(resultsNode),
            notification: readNotification(resultsNode),
            summary: parseSummary(resultsNode),
            updates: readResponseUpdates(resultsNode),
            groups: readGroups(resultsNode)
        };

        return results;
    };

    function readTimestamps(resultsNode) {
        
        var node = getNode('timestamps', resultsNode);
        if (node == null) return null;

        var timestamps = {
            clientSent: getAttribInt(node, 'sent'),
            serverReceived: getAttribInt(node, 'received'),
            serverCompleted: getAttribInt(node, 'completed'),
            clientReceived: new Date().getTime()
        };

        return timestamps;
    };

    function readNotification(resultsNode)
    {
        var node = getNode('notification', resultsNode);
        if (node == null) return null;

        var notification = {
            type: node.getAttribute('type'),
            message: node.getAttribute('message')
        };

        return notification;
    };

    function parseSummary(resultsNode)
    {
        var node = getNode('summary', resultsNode);
        if (node == null) return null;

        var summary = {
            testLength: getAttribInt(node, 'testLength'),
            testLengthMet: getAttribBool(node, 'testLengthMet'),
            testFinished: getAttribBool(node, 'testFinished'),
            prefetched: getAttribBool(node, 'prefetched')
            // groups: getAttribInt(node, 'groups'),
            // groupsCompleted: getAttribInt(node, 'groupsCompleted'),
            // responsesTotal: getAttribInt(node, 'responsesTotal'),
            // responsesValid: getAttribInt(node, 'responsesValid'),
            // responsesVisible: getAttribInt(node, 'responsesVisible'),
            // firstGroup: getAttribInt(node, 'firstGroup'),
            // lastGroup: getAttribInt(node, 'lastGroup')
        };

        return summary;
    };

    // reads the status of the response updates
    function readResponseUpdates(resultsNode) {
        
        var responses = [];
        var updatesNode = getNode('updates', resultsNode);

        // get all the <responseupdate> nodes
        batchNodes('response', updatesNode, function(updateNode) {
            var responseStatus = parseResponseStatus(updateNode);
            responses.push(responseStatus);
        });

        return responses;
    };

    function parseResponseStatus(updateNode) {
        var responseStatus = new TestShell.Response.Status();
        responseStatus.position = getAttribInt(updateNode, 'position');
        responseStatus.status = getAttrib(updateNode, 'status');
        responseStatus.reason = getAttrib(updateNode, 'reason');
        return responseStatus;
    };

    // function for parsing response nodes into response objects
    function parseResponse(responseNode, group) {
        
        var response = new TestShell.Response(group);
        response.id = getAttrib(responseNode, 'id');
        response.dateCreated = getAttrib(responseNode, 'created');
        response.format = getAttrib(responseNode, 'format');

        response.itsBank = getAttribInt(responseNode, 'bank');
        response.itsItem = getAttribInt(responseNode, 'item');
        response.pageNum = getAttribInt(responseNode, 'page');
        response.position = getAttribInt(responseNode, 'position');
        response.sequence = getAttribInt(responseNode, 'sequence');

        response.mark = getAttribBool(responseNode, 'mark');
        response.isSelected = getAttribBool(responseNode, 'isSelected');
        response.isRequired = getAttribBool(responseNode, 'isRequired');
        response.isValid = getAttribBool(responseNode, 'isValid');
        response.prefetched = getAttribBool(responseNode, 'prefetched');

        return response;
    };

    // function for parsing group nodes into group objects
    function parseGroup(resultsNode, groupNode) {
        
        var group = new TestShell.PageGroup();
        group.id = getAttrib(groupNode, 'id');
        group.pageNum = getAttribInt(groupNode, 'page');
        group.numRequired = getAttribInt(groupNode, 'numRequired');

        group.segment = getAttribInt(groupNode, 'segment');
        group.segmentID = getAttrib(groupNode, 'segmentID');

        // get all the <response> nodes for a <group>
        batchNodes('response', groupNode, function(node) {
            var response = parseResponse(node, group);
            group.responses.push(response);
        });

        // get content
        /*
        var contentsNode = getNode('contents', resultsNode);
        var nodeContent = selectNode('content[groupID = "' + group.id + '"]', contentsNode);

        if (nodeContent) {
            var contentParser = new ContentManager.Xml();
            group.content = contentParser.parseContent(nodeContent);
        }
        */

        return group;
    };
    
    function readGroups(resultsNode) {
        
        var groupsNode = getNode('groups', resultsNode);
        var groups = [];

        // get all the <group> nodes
        batchNodes('group', groupsNode, function(node) {
            var group = parseGroup(resultsNode, node);
            groups.push(group);
        });

        return groups;
    };

    TestShell.Xml.validResults = validResults;
    TestShell.Xml.parseResults = parseResults;

})(TestShell);
