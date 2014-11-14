﻿/*
This is the code for the review screen where they can go back into the test or complete.
*/

(function (TDS, Sections) {

    function TestReview() {
        TestReview.superclass.constructor.call(this, 'sectionTestReview');
        this.addClick('btnReviewTest', this.viewGroup);
        this.addClick('btnCompleteTest', this.score);
    };

    YAHOO.lang.extend(TestReview, Sections.Base);

    TestReview.prototype.load = function () {

        // show marked warning text
        this.setMarked(window.groups);

        // show unanswered warning text
        this.setUnanswered(window.groups);

        // fill select box
        this.setGroups(window.groups);
    };

    // show warning div if any question is marked for review
    TestReview.prototype.setMarked = function(groups) {
        var marked = groups.some(function (group) {
            return group.items.some(function(item) {
                return item.marked;
            });
        });
        if (marked) {
            $('#markedWarning').show();
        }
    };

    // show warning div if any question is unanswered
    TestReview.prototype.setUnanswered = function (groups) {
        var unanswered = groups.some(function (group) {
            return group.items.some(function(item) {
                return !item.answered;
            });
        });
        if (unanswered) {
            $('#unansweredWarning').show();
        }
    };

    TestReview.prototype.setGroups = function (groups) {
        var accProps = Accommodations.Manager.getCurrentProps();
        if (accProps && accProps.isReviewScreenLayoutListView()) {
            this.renderListView(groups);
        } else {
            this.renderDropDown(groups);
        }
    };

    TestReview.prototype.renderDropDown = function (groups) {

        var ddlNavigation = YUD.get('ddlNavigation');
        ddlNavigation.options.length = 0; // clear selectbox

        groups.forEach(function(group) {

            // create label
            var label = '';
            var firstItem = Util.Array.first(group.items);
            var firstPos = firstItem ? firstItem.position : 0;
            var lastItem = Util.Array.last(group.items);
            var lastPos = lastItem ? lastItem.position : 0;
            var marked = group.items.some(function(item) {
                return item.marked;
            });

            // check if the nav acc says to use tasks for labels
            var accProps = Accommodations.Manager.getDefaultProperties();
            if (accProps && accProps.getNavigationDropdown() == 'TDS_NavTk') {
                //if the task accommodation is set we would need to say something like "task".
                //the task number is simply the group number starting with 1.
                label = Messages.getAlt('TDSShellObjectsJS.Label.TaskLabel', 'Task ') + group.page;
            } else {
                label = firstPos;
                if (firstPos != lastPos) {
                    label += ' - ' + lastPos;
                }
            }

            if (marked) {
                label += ' (' + Messages.get('TDSShellObjectsJS.Label.Marked') + ')';
            }

            // add selectbox option
            ddlNavigation[ddlNavigation.length] = new Option(label, group.page);
        });

    };

    TestReview.prototype.renderListView = function (groups) {

        // remove hard coded drop down 
        var $container = $('#sectionTestReview div.choices');
        $('label', $container).remove(); // remove <label>
        $('select', $container).remove(); // remove <select>
        $('#btnReviewTest').remove(); // remove <button> "Review My Answers"

        // create <div class="pages"> 
        var pagesEl = document.createElement('div');
        pagesEl.className = 'pages';

        // create <h3>Questions:</h3>
        var headerEl = document.createElement('h3');
        $(headerEl).text(Messages.getAlt('TestShellScripts.Label.Questions', 'Questions:'));

        // create <ul> of items
        var listEl = document.createElement('ul');

        // create <li>
        groups.forEach(function (group) {
            group.items.forEach(function(item) {
                var el = document.createElement('li');
                var linkEl = document.createElement('a');
                linkEl.href = '#';
                $(linkEl).click(function (evt) {
                    YUE.stopEvent(evt);
                    TDS.redirectTestShell(group.page, item.position);
                });
                $(linkEl).text(item.position);
                var label = 'Question';
                if (item.marked) {
                    $(linkEl).addClass('marked');
                    label += ' marked for review';
                }
                if (!item.answered) {
                    $(linkEl).addClass('unanswered');
                    if (item.marked) {
                        label += ' and';
                    }
                    label += ' unanswered';
                }
                linkEl.setAttribute('title', label);
                el.appendChild(linkEl);
                listEl.appendChild(el);
            });
        });

        // add to review shell
        pagesEl.appendChild(headerEl);
        pagesEl.appendChild(listEl);
        $container.prepend(pagesEl);

    };

    TestReview.prototype.viewGroup = function(group) {
        var ddlNavigation = YUD.get('ddlNavigation');

        if (ddlNavigation.value == '') {
            //we need to show a warning e.g. "Please select a page first". however, depending
            //on the accommodation of the navigation drop down we may need to replace the "page" with something else e.g. "task".
            var label = Messages.get('TDSShellObjectsJS.Label.PageLabel').toLowerCase();
            // check if the nav acc says to use tasks for labels
            var defaultAccProps = Accommodations.Manager.getDefaultProperties();
            if (defaultAccProps && defaultAccProps.getNavigationDropdown() == 'TDS_NavTk') {
                //overwrite the label value.
                label = Messages.get('TDSShellObjectsJS.Label.TaskLabel').toLowerCase();
            }
            var pageFirstMessage = Messages.get('ReviewShell.Message.PageFirst', [label]);
            TDS.Dialog.showAlert(pageFirstMessage);

            return;
        }

        // get test shell url
        TDS.redirectTestShell(ddlNavigation.value);
    };

    TestReview.prototype.score = function() {

        // show confirm dialog
        var msgCannotComplete = Messages.getAlt('ReviewShell.Message.CannotCompleteTest', 'Cannot complete the test.');
        var msgSubmitTest = Messages.getAlt('ReviewShell.Message.SubmitTest', 'Are you sure you want to submit the test?');

        // get score accommodations
        var accProps = Accommodations.Manager.getCurrentProps();
        var hideTestScore = accProps.hideTestScore();
        var showItemScoreReportSummary = accProps.showItemScoreReportSummary();
        var showItemScoreReportResponses = accProps.showItemScoreReportResponses();

        // create function for submitting the test
        var submitTest = function() {
            TDS.Dialog.showPrompt(msgSubmitTest, function() {
                TDS.Student.API.scoreTest(hideTestScore, showItemScoreReportSummary, showItemScoreReportResponses).then(function(summary) {
                    if (summary) {
                        this.request('next', summary);
                    }
                }.bind(this));
            }.bind(this));
        }.bind(this);

        // check if the test can be completed
        var testInfo = TDS.Student.Storage.getTestInfo();
        if (testInfo && testInfo.validateCompleteness) {
            TDS.Student.API.canCompleteTest().then(function(canComplete) {
                if (canComplete) {
                    submitTest();
                } else {
                    TDS.Dialog.showAlert(msgCannotComplete);
                }
            }.bind(this));
        } else {
            submitTest();
        }
    };

    Sections.TestReview = TestReview;

})(window.TDS, window.Sections);