TestShell.Response = function(group)
{
    this.group = group;
    this.id = null;
    this.itsBank = 0;
    this.itsItem = 0;
    this.position = 0;
    this.sequence = 0;
    this.dateCreated = null;
    this.mark = false;
    this.isSelected = false;
    this.isRequired = false;
    this.isValid = false;
    this.prefetched = false;

    this.value = null;
    this.comment = '';

    // get the content item
    this.getItem = function()
    {
        var page = group.getContentPage();
        if (page != null) return page.getItem(this.position);
        return null;
    };
};

// Reset the response data structure.
// NOTE: this should get called only after this has been done on the server.
TestShell.Response.prototype.reset = function()
{
    this.sequence = 0;
    this.value = null;
    this.isSelected = false;
    this.isValid = false;
    this.mark = false;
	this.comment = '';
};

// manually set the students response
TestShell.Response.prototype.setValue = function(data)
{
    this.value = data;
    this.isValid = true;
    this.isSelected = true;
};

// gets the last response saved (NULL means never responded)
TestShell.Response.prototype.getLastValue = function()
{
    // last response for this item attempted to be sent to the server
    if (this.value != null) return this.value;

    // original response for this item when it was first loaded
    var item = this.getItem();
    if (item) return item.value; 
    
    return null;
};

// checks if this resposne has been answered
TestShell.Response.prototype.isAnswered = function()
{
    return (this.isSelected && this.isValid);
};

// checks if this item has a response to save
TestShell.Response.prototype.isDirty = function()
{
    // make sure we have an item
    var item = this.getItem();
    if (!item) return false;

    // HACK: MC
    if (item.format.toLowerCase() == 'mc') return false;

    // HACK: audio recorder
    if (item.recorder) {
        var recorderObj = TDS.Audio.Widget.getRecorder(item.recorder);
        if (recorderObj) {
            return recorderObj.dirty;
        }
    }

    // get the widgets response handler
    var itemResponse = item.getResponse();
    if (!itemResponse) return false;
	
	// make sure the item widget response is ready 
    if (itemResponse.isReady === false) {
        return false;
    }

    // get responses
    var currentResponse = itemResponse.value; // current response for this item that the student has made
    var previousResponse = this.getLastValue(); // last response

    // HACK: grid
    if (item.responseType.toLowerCase() == 'grid')
    {
        // make sure grid has been loaded
        if (!item.grid || !item.grid.isLoaded()) return false;

        // if the grid is empty and there is no original response then this is the first time the student is responding to this
        if (previousResponse == null && !item.grid.isValid()) return false;

        // check if the response has changed
        return item.grid.hasChanged(previousResponse);
    }

    // check if isselected has changed
    if (itemResponse.isSelected != this.isSelected) return true;

    // if the response has never been saved the handler has empty string (empty textarea) and the items original value is null, then nothing has changed
    if (previousResponse == null && (currentResponse == null || currentResponse.length == 0)) return false;

    // check if the current response is different than the previous response
    return (currentResponse != previousResponse);
};

// attempt to undo a response made (NOTE: only recorded audio is supported, this should go in ContentManager at some point)
TestShell.Response.prototype.undo = function()
{
    var item = this.getItem();
    if (!item || !item.recorder) return false;

    var previousResponse = this.getLastValue(); // last response

    // if there is a previous response then unload current one and load the previous one
    if (previousResponse != null)
    {
        Util.log('recorder: undo - loadBase64Audio');
        TDS.Audio.Recorder.loadBase64Audio(item.recorder.id, previousResponse);
        // item.recorder.unloadAudioClip();
        // item.recorder.loadBase64Audio(previousResponse);
    }

    return true;
};

TestShell.Response.prototype.toString = function()
{
    return (this.position != null) ? this.position.toString() : '';
};

TestShell.Response.Status = function() {
    this.id = null;
    this.status = '';
    this.reason = '';
};

function getResponseIDs(responses)
{
    return Util.Array.reduce(responses, '', function(text, response)
    {
        return text + ((text.length == 0) ? '' : ', ') + response.position;
    });
}
