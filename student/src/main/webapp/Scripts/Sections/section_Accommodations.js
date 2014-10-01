Sections.Accommodations = function()
{
    Sections.Accommodations.superclass.constructor.call(this, 'sectionAccommodations');

    this.addClick('btnAccBack', function()
    {
        this.request('back');
    });

    this.addClick('btnAccSelect', this.submit);

    // current accommodations
    this._segmentsAccommodations = null;
    this._rendererCollection = null;
};

YAHOO.lang.extend(Sections.Accommodations, Sections.Base);

Sections.Accommodations.prototype.init = function()
{
    // DEBUG:
    var querystring = Util.QueryString.parse();
    if (querystring.showInvisibleAccs) Accommodations.Renderer.hideInvisible = false;
    if (querystring.showUnselectableAccs) Accommodations.Renderer.hideUnselectable = false;
};

Sections.Accommodations.prototype.load = function (segmentsAccommodations)
{
    this._segmentsAccommodations = segmentsAccommodations;
    this._rendererCollection = [];

    // get segments container
    var segmentsContainer = YUD.get('segments');

    // clear segments
    segmentsContainer.innerHTML = '';

    Util.Array.each(segmentsAccommodations, function(segmentAccommodations)
    {
        // skip accommodations that have nothing visible
        if (!segmentAccommodations.isAnyVisible()) return;

        // create segment container
        var segmentContainer = HTML.DIV({ id: 'segment-' + segmentAccommodations.getId(), 'className': 'segment' });
        segmentsContainer.appendChild(segmentContainer);

        // create segment header
        var segmentHeader = HTML.H3(null, segmentAccommodations.getLabel());
        segmentContainer.appendChild(segmentHeader);

        // create segment accommodations
        var renderer = new Accommodations.Renderer(segmentAccommodations, segmentContainer);
        renderer.bind();
        renderer.render();

        this._rendererCollection.push(renderer);

    }, this);
};

Sections.Accommodations.prototype.submit = function()
{
    var self = this;

    // update acc object with what the user selected
    /*Util.Array.each(this._renderers, function(renderer)
    {
        renderer.save();
    });*/

    var formValues = [];
    formValues.push('testKey=' + LoginShell.testSelection.key);
    formValues.push('testID=' + LoginShell.testSelection.id);
    formValues.push('subject=' + LoginShell.testSelection.subject);
    formValues.push('grade=' + LoginShell.testSelection.grade);

    Util.Array.each(this._segmentsAccommodations, function(segmentAccommodations)
    {
        var segmentPos = segmentAccommodations.getPosition();
        var codes = segmentAccommodations.getSelectedDelimited(true, ',');
        formValues.push('segment=' + segmentPos + '#' + codes);
    });

    Util.dir(formValues);

    // submit test for approval
    LoginShell.api.openTest(formValues.join('&'), function(oppInfo)
    {
        // once we open test move next
        if (oppInfo)
        {
            LoginShell.setOppInfo(oppInfo);
            self.request('next');
        }
    });
};
