Sections.LoginVerify = function()
{
    Sections.LoginVerify.superclass.constructor.call(this, 'sectionLoginVerify');

    this.Controls =
    {
        // buttons
        btnVerifyDeny: YUD.get('btnVerifyDeny'),
        btnVerifyApprove: YUD.get('btnVerifyApprove'),

        // labels
        verifyName: YUD.get('verifyName'),
        verifyGrade: YUD.get('verifyGrade'),
        verifyBirthday: YUD.get('verifyBirthday'),
        verifySchool: YUD.get('verifySchool'),
        verifySSID: YUD.get('verifySSID'),

        ddlGrades: YUD.get('verifyGradeSelector').getElementsByTagName('select')[0]
    };

    this.addClick(this.Controls.btnVerifyDeny, function()
    {
        this.request('back');
    });

    this.addClick(this.Controls.btnVerifyApprove, this.approve);
};

YAHOO.lang.extend(Sections.LoginVerify, Sections.Base);

// verify the login info received from the server
Sections.LoginVerify.prototype.load = function (loginInfo)
{
    var session = loginInfo.session, testee = loginInfo.testee;

    this.reset();

    // e.x., "March 27, 2007"
    /*var formatBirthday = function()
    {
        var months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
        var date = testee.birthday.getDate();
        var month = testee.birthday.getMonth();
        var year = testee.birthday.getFullYear();

        return months[month] + ' ' + date + ', ' + year;
    };*/

    // set fields
    this.Controls.verifyName.innerHTML = testee.name;
    this.Controls.verifyGrade.innerHTML = testee.grade;
    this.Controls.verifyBirthday.innerHTML = testee.birthday; // formatBirthday();
    this.Controls.verifySchool.innerHTML = testee.schoolName;
    this.Controls.verifySSID.innerHTML = testee.id;

    // show/hide
    if (testee.isReal)
    {
        YUD.setStyle('verifyGradeViewer', 'display', 'block');
        YUD.setStyle('verifyGradeSelector', 'display', 'none');
    }
    else
    {
        YUD.setStyle('verifyGradeViewer', 'display', 'none');
        YUD.setStyle('verifyGradeSelector', 'display', 'block');
    }

    // set grades from global variable
    this.setGrades(window.grades);
};

Sections.LoginVerify.prototype.reset = function()
{
    this.Controls.verifyName.innerHTML = '';
    this.Controls.verifyGrade.innerHTML = '';
    this.Controls.verifyBirthday.innerHTML = '';
    this.Controls.verifySchool.innerHTML = '';
    this.Controls.verifySSID.innerHTML = '';
    this.Controls.ddlGrades.selectedIndex = 0;
};

Sections.LoginVerify.prototype.setGrades = function(grades)
{
    var label = Messages.get('User.List.Identified.SelectGradeList');
    this.Controls.ddlGrades[0] = new Option(label, '');

    for (var i = 0; i < TDS.Config.grades.length; i++)
    {
        var grade = TDS.Config.grades[i];
        this.Controls.ddlGrades[i + 1] = new Option(grade, grade);
    }
};

Sections.LoginVerify.prototype.getGrade = function()
{
    return this.Controls.ddlGrades.value;
};

Sections.LoginVerify.prototype.approve = function()
{
    var grade = null;

    if (LoginShell.testee.isReal)
    {
        // get grade from student
        grade = LoginShell.testee.grade;
    }
    else
    {
        // get grade from dropdown
        grade = this.getGrade();

        if (grade == null || grade.length == 0)
        {
            var label = Messages.get('User.List.Identified.MustSelectGrade');
            label = label.replace('<-- ', ''); // remove any arrows from label
            TDS.Dialog.showWarning(label);
            return;
        }
    }

    this.request('next', grade);
};

