﻿/*
This is used to process browser validations.
*/

// tds.securebrowser.validator.run()

TDS.SecureBrowser.Validators = (function() {

    var list = [];

    // add object with isSupported() and validate()
    function register(validator) {
        list.push(validator);
    }

    function getValidators() {
        return list;
    }

    function run() {
        var exemptions = [];

        // retrieve Secure Browser validation exemptions from appsettings
        var validatorExemptionsAppSetting = TDS.getAppSetting('sb.validatorExemptions');
        if (validatorExemptionsAppSetting && validatorExemptionsAppSetting != '') {
            exemptions = validatorExemptionsAppSetting.split(',');
        }

        // find validators that are supported on this browser/platform
        var validators = list.filter(function(validator) {
            return !Util.Array.contains(exemptions, validator.name) && validator.isSupported();
        });
        // run all the validators (returns: matches/rejects)
        return Util.Array.partition(validators, function (validator) {
            return validator.validate();
        });
    }

    function validate() {
        var partition = run();
        return partition.rejects.map(function(validator) {
            return validator.message;
        });
    }

    return {
        register: register,
        getValidators: getValidators, 
        validate: validate
    }

})();

