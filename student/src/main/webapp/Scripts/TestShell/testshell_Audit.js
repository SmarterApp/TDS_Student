/*
This class is used to collect a audit history of the test.

FIREBUG EXAMPLES:
View Audit - console.dir(TestShell.Audit.get(TestShell.PageManager.getPages()[1].id))
View Latency - console.dir(TestShell.Audit.Latency.create(TestShell.PageManager.getCurrent()))
*/

TestShell.Audit = 
{
    _records: [], // all records in the order they arived
    _recordGroups: new Util.Structs.Map(), // records groups by id
    _toolsUsed: []
};

TestShell.Audit.Record = function(id, event)
{
    this.id = id;
    this.event = event;
    this.date = new Date();
};

// get an existing audit
TestShell.Audit.get = function(id)
{
    var records = this._recordGroups.get(id);
    return (records) ? records.slice(0) : null;
};

TestShell.Audit.getList = function() 
{
    return this._records;
};

// record an audit event
TestShell.Audit.add = function(id, event)
{
    var record = new TestShell.Audit.Record(id, event);
    this._records.push(record);

    var records = this._recordGroups.get(id);

    if (records == null)
    {
        records = [];
        this._recordGroups.set(id, records);
    }

    records.push(record);
};

// add a tool (acccode) for a page
TestShell.Audit.addToolUsage = function(page, accType, accCode)
{
    // look for existing tool
    var toolUsageToAdd = Util.Array.find(this._toolsUsed, function(toolUsage) 
    {
        return (toolUsage.page == page && 
                toolUsage.type == accType && 
                toolUsage.code == accCode);
    });

    if (toolUsageToAdd)
    {
        toolUsageToAdd.count++;
    }
    else
    {
        toolUsageToAdd =
        {
            page: page,
            type: accType,
            code: accCode,
            count: 1
        };

        this._toolsUsed.push(toolUsageToAdd);
    }
};

TestShell.Audit.getToolsUsed = function() { return this._toolsUsed; };

/****************************************************************************************/

// used for creating latency data from the audit
TestShell.Audit.Latency = function()
{
    this.itemPage = 0;
    this.numItems = 0;
    this.requestTime = 0;
    this.loadAttempts = 0;
    this.loadDate = null;
    this.loadTime = 0;
    this.createDate = null;
    this.visitTime = 0;
    this.visitCount = 0;
    this.visitDate = null;
};

TestShell.Audit.Latency.prototype.toJson = function()
{
    var json = [];

    var properties = 0;

    var addProperty = function(name, value, converter)
    {
        if (properties > 0) json.push(',');
        json.push('"');
        json.push(name);
        json.push('":');

        if (YAHOO.lang.isObject(value) && YAHOO.lang.isFunction(converter))
        {
            value = converter(value);
        }

        if (YAHOO.lang.isNull(value))
        {
            json.push('null');
        }
        else if (YAHOO.lang.isNumber(value))
        {
            json.push(value);
        }
        else
        {
            json.push('"');
            json.push(value);
            json.push('"');
        }

        properties++;
    };

    var dateToString = function(d)
    {
        return '\/Date(' + d.getTime() + ')\/';
    };

    json.push('{');

    addProperty('itemPage', this.itemPage);
    addProperty('numItems', this.numItems);
    addProperty('requestTime', this.requestTime);
    addProperty('loadAttempts', this.loadAttempts);
    addProperty('loadDate', this.loadDate, dateToString);
    addProperty('loadTime', this.loadTime);
    addProperty('createDate', this.createDate, dateToString);
    addProperty('visitCount', this.visitCount);
    addProperty('visitDate', this.visitDate, dateToString);
    addProperty('visitTime', this.visitTime);

    json.push('}');

    return json.join('');
};

// find latest record for an event
TestShell.Audit.findEvent = function(id, event, minDate)
{
    var records = TestShell.Audit.get(id);
    if (records == null) return null;

    records = records.reverse(); // reverse array

    return Util.Array.find(records, function(record)
    {
        return (record.event == event && (minDate == null || record.date >= minDate));
    });
};

// return collection of records for an event
TestShell.Audit.filterEvents = function(id, event)
{
    var records = TestShell.Audit.get(id);
    if (records == null) return [];
    
    return Util.Array.filter(records, function(record) { return record.event == event; });
};

// get the total time difference in between two events
TestShell.Audit.aggregateEvents = function(id, event1, event2, fixEnd)
{
    var records = TestShell.Audit.get(id);
    if (records == null) return -1;

    var total = 0;
    var startRecord = null;
    var endRecord = null;

    Util.Array.each(records, function(record)
    {
        // find start record
        if (event1 == record.event)
        {
            startRecord = record;
            endRecord = null;
        }

        if (event2 == record.event)
        {
            endRecord = record;
        }

        if (startRecord != null && endRecord != null)
        {
            // get timestamp diff
            total += (endRecord.date - startRecord.date);

            // reset
            startRecord = null;
            endRecord = null;
        }
    });

    // if fix end is true and the start record has no end record it will use set the end date to right now 
    if (fixEnd && startRecord != null && endRecord == null)
    {
        // get timestamp diff for start date that never ended
        var now = new Date();
        total += (now - startRecord.date);
    }

    return total;
};

// create latency object for a page group
TestShell.Audit.Latency.create = function(group)
{
    /*
    NOTES:
    createDate = The date-time that the iframe is created. This will be used to compute the load time.
    loadDate = Set this time as a result of the OnLoad callback function. Set exactly once for each iframe.
    requestTime = 'page-requested' - 'page-show'
    loadTime = 'content-loaded' - 'content-requested'
    visitTime += 'page-hide' - 'page-show'
    visitCount = count('page-show')
    loadAttempts = count('content-requested')
    */

    var latency = new TestShell.Audit.Latency();
    latency.itemPage = group.pageNum;
    latency.numItems = group.responses.length;

    // timestamps
    var createRecord = TestShell.Audit.findEvent(group.id, 'content-requested');
    if (createRecord == null) return null; // this is NULL if content was never preloaded
    latency.createDate = createRecord.date;

    var loadRecord = TestShell.Audit.findEvent(group.id, 'content-loaded', createRecord.date);
    if (loadRecord != null) latency.loadDate = loadRecord.date;

    var visitRecord = TestShell.Audit.findEvent(group.id, 'page-show');
    if (visitRecord != null) latency.visitDate = visitRecord.date;

    // durations
    latency.requestTime = TestShell.Audit.aggregateEvents(group.id, 'page-requested', 'page-show');
    latency.loadTime = TestShell.Audit.aggregateEvents(group.id, 'content-requested', 'content-loaded');
    latency.visitTime = TestShell.Audit.aggregateEvents(group.id, 'page-show', 'page-hide', true);

    // counts
    latency.loadAttempts = TestShell.Audit.filterEvents(group.id, 'content-requested').length;
    latency.visitCount = TestShell.Audit.filterEvents(group.id, 'page-show').length;

    return latency;
};

TestShell.Audit.Latency.createCollection = function()
{
    var latencies = [];

    Util.Array.each(TestShell.PageManager.getGroups(), function(group)
    {
        var latency = null;

        // get latency for group
        try { latency = TestShell.Audit.Latency.create(group); }
        catch(ex)
        {
            // NOTE: missing latencies due to errors will be missing in report
            Util.log('Latency error: ' + ex);
        }

        if (latency != null) latencies.push(latency);
    });

    return latencies;
};

// get json for latencies
TestShell.Audit.Latency.getJson = function()
{
    var latencyCount = 0;
    var latencies = TestShell.Audit.Latency.createCollection();

    var json = [];
    json.push('[');

    Y.Array.each(latencies, function(latency)
    {
        if (latencyCount > 0) json.push(',');
        json.push(latency.toJson());
        latencyCount++;
    });

    json.push(']');
    return json.join('');
};

// get json for the test shell audit
TestShell.Audit.getJson = function()
{
    var json = [];
    json.push('{');
    
    // latencies
    json.push('"latencies":');
    json.push(TestShell.Audit.Latency.getJson());
    json.push(',');
    
    // tools uses
    json.push('"toolsUsed":');
    json.push(YAHOO.Lang.JSON.stringify(this._toolsUsed));

    json.push('}');

    return json.join('');
};

/****************************************************************************************/
// The event subscriptions below are used to audit the different components of the test shell

TestShell.ContentLoader._xhrManager.Events.subscribe('onSent', function(request)
{
    TestShell.Audit.add(request.getId(), 'content-requested');
});

TestShell.ContentLoader._xhrManager.Events.subscribe('onSuccess', function(request)
{
    TestShell.Audit.add(request.getId(), 'content-received');
});

TestShell.ContentLoader._xhrManager.Events.subscribe('onFailure', function(request)
{
    TestShell.Audit.add(request.getId(), 'content-failed');
});

ContentManager.onPageEvent('init', function(contentPage)
{
    TestShell.Audit.add(contentPage.id, 'content-init');
});

ContentManager.onPageEvent('rendering', function(contentPage)
{
    TestShell.Audit.add(contentPage.id, 'content-rendering');
});

ContentManager.onPageEvent('rendered', function(contentPage)
{
    TestShell.Audit.add(contentPage.id, 'content-rendered');
});

ContentManager.onPageEvent('available', function(contentPage)
{
    TestShell.Audit.add(contentPage.id, 'content-available');
});

ContentManager.onPageEvent('loaded', function(contentPage)
{
    TestShell.Audit.add(contentPage.id, 'content-loaded');
});

TestShell.PageManager.Events.subscribe('onShow', function(page)
{
    TestShell.Audit.add(page.id, 'page-show');
});

TestShell.PageManager.Events.subscribe('onHide', function(page)
{
    TestShell.Audit.add(page.id, 'page-hide');
});

/****************************************************************************************/
// Record accommodation tool usage

// TTS
TTS.Manager.Events.onStatusChange.subscribe(function(currentStatus)
{
    // we are only interested in playing event
    if (currentStatus != TTS.Status.Playing) return;

    // get content manager page
    var currentPage = ContentManager.getCurrentPage();
    if (currentPage == null) return;

    // get test shell page
    var page = TestShell.PageManager.get(currentPage.id);
    if (page == null) return;

    // get current entity (item or passage)
    var currentEntity = currentPage.getActiveEntity();
        
    if (currentEntity instanceof ContentPassage)
    {
        TestShell.Audit.addToolUsage(page.pageNum, 'TTS', 'TDS_TTS_Stim');
    }
    else if (currentEntity instanceof ContentItem)
    {
        TestShell.Audit.addToolUsage(page.pageNum, 'TTS', 'TDS_TTS_Item');
    }
});
