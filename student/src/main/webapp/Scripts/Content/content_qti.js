/*
This file is used for any general QTI utilities or parsers.
*/

ContentManager.QTI = {};

(function (QTI) {

    var validHtmlTags = [

        // Section 6.2.1. Text Elements
        'abbr', 'acronym', 'address', 'blockquote', 'br', 'cite', 'code', 'dfn', 'div', 'em',
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'kbd', 'p', 'pre', 'q', 'samp', 'span', 'strong', 'var',

        // Section 6.2.2. List Elements
        'dl', 'dlElement', 'dt', 'dd', 'ol', 'ul', 'li',

        // Section 6.2.3. Object Elements
        //'object', 'param',

        // Section 6.2.4. Presentation Elements
        'b', 'big', 'hr', 'i', 'small', 'sub', 'sup', 'tt',

        // Section 6.2.5. Table Elements
        'caption', 'col', 'colgroup', 'table', 'tableCell', ' tableCellScope', 'tbody', 'td', 'tfoot',
        'th', 'thead', 'tr',

        // Section 6.2.6. Image Element
        'img',

        // Section 6.2.7. Hypertext Element
        'a'
    ];

    function adopt(oldParent, newParent) {
        if (oldParent && newParent) {
            var orphans = YUD.getChildren(oldParent);
            if (orphans && orphans.length) {
                var orphanCount = orphans.length;
                for (var j = 0; j < orphanCount; j++) {
                    var orphan = orphans[j];
                    newParent.appendChild(oldParent.removeChild(orphan));
                }
            }
        }
    };

    function pruneNonHtml(doc) {

        // List that will contain all nodes from XML
        var allNodes = [];

        // Recursively traverse entire XML tree, building list of all nodes in allNodes[]
        var traverseTreeRecursive = function (parentNode) {
            if (parentNode) {
                allNodes.push(parentNode);
                var children = YUD.getChildren(parentNode);
                if (children) {
                    for (var i = 0; i < children.length; i++) {
                        traverseTreeRecursive(children[i]);
                    }
                }
            }
        };

        var itemNode = doc.documentElement;
        traverseTreeRecursive(itemNode);
        var nodeCount = allNodes.length;

        // Run through list of all nodes, deleting nodes that are not valid HTML
        // before deleting a node, promote any valid children to its parent
        for (var i = 0; i < nodeCount; i++) {
            var currentNode = allNodes.pop();
            if ((validHtmlTags.indexOf(currentNode.nodeName) < 0) &&
                (currentNode.parentNode != doc)) {
                var grandParentNode = currentNode.parentNode;
                adopt(currentNode, grandParentNode);
                grandParentNode.removeChild(currentNode);
            };
        }
    };

    // takes item qti and loads what it can into the stem element
    function loadStem(item) {

        // get stem container
        var stemEl = item.getStemElement();

        // get XML and convert <prompt> tags to <p>
        var xmlString = item.qti.xml;
        xmlString = xmlString.replace(/<prompt/g, '<p');
        xmlString = xmlString.replace(/<\/prompt/g, '<\/p');

        // Load XML and prune all non-valid HTML
        var xmlDoc = Util.Xml.parseFromString(xmlString);
        pruneNonHtml(xmlDoc);

        // Place HTML in stem container
        adopt(xmlDoc.documentElement, stemEl);
    };

    // expose public functions
    QTI.validHtmlTags = validHtmlTags;
    QTI.loadStem = loadStem;

})(ContentManager.QTI);


