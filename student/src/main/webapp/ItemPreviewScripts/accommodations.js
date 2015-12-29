//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
var accDialog = null;

// get the accommodations to be used for the dialog
function getAccommodationsJson()
{
    // create custom accommodations
    var accsJson = { types: [] };

    accsJson.types.push(
    {
        name: 'Language', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'ENU', name: 'English', selected: true },
            { code: 'ESN', name: 'Spanish' },
            { code: 'ENU-Braille', name: 'Braille' }
        ]
    });

    accsJson.types.push(
    {
        name: 'TTX Business Rules', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_TTX_0', selected: true },
            { code: 'TDS_TTX_A202', name: 'A202' },
            { code: 'TDS_TTX_A203', name: 'A203' },
            { code: 'TDS_TTX_A204', name: 'A204' },
            { code: 'TDS_TTX_A205', name: 'A205' },
	    { code: 'TDS_TTX_A206', name: 'A206' },
            { code: 'TDS_TTX_M101', name: 'M101' },
            { code: 'TDS_TTX_M102', name: 'M102' },
            { code: 'TDS_TTX_HI101', name: 'HI101' },
            { code: 'TDS_TTX_HI102', name: 'HI102' },
            { code: 'ITS_BRF_HI105', name: 'HI105' },
            { code: 'ITS_TTX_BRF101', name: 'BRF101' },
            { code: 'ITS_TTX_BRF102', name: 'BRF102' },
            { code: 'ITS_TTX_BRF103', name: 'BRF103' },
            { code: 'ITS_TTX_BRF104', name: 'BRF104' },
            { code: 'TDS_TTX_U101', name: 'U101' },
            { code: 'TDS_TTX_U102', name: 'U102' }
        ]
    });

    accsJson.types.push(
    {
        name: 'Color Choices', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_CC0', name: 'None', selected: true },
            { code: 'TDS_CCYellow', name: 'Yellow' },
            { code: 'TDS_CCYellow2', name: 'Yellow Light' },
            { code: 'TDS_CCBlue', name: 'Blue' },
            { code: 'TDS_CCBlue2', name: 'Blue Light' },
            { code: 'TDS_CCMagenta', name: 'Magneta' },
            { code: 'TDS_CCMagenta2', name: 'Magneta Light' },
            { code: 'TDS_CCGray', name: 'Gray' },
            { code: 'TDS_CCGray2', name: 'Gray Light' },
            { code: 'TDS_CCGreen', name: 'Green' },
            { code: 'TDS_CCGreen2', name: 'Green Light' },
            { code: 'TDS_CCInvert', name: 'Invert' },
            { code: 'TDS_CCYellowB', name: 'Yellow on Blue' },
            { code: 'TDS_CCWhiteN', name: 'White on Navy' }
        ]
    });

    accsJson.types.push(
    {
        name: 'American Sign Language', allowChange: true, isSelectable: true, isVisible: true, values:
        [
			{ code: 'TDS_ASL0', name: 'False', selected: true },
			{ code: 'TDS_ASL1', name: 'True' }
        ]
    });
	
    accsJson.types.push(
    {
        name: 'Item Numbers', allowChange: true, isSelectable: true, isVisible: true, values:
        [
			{ code: 'TDS_ItmNum1', name: 'True', selected: true },
			{ code: 'TDS_ItmNum0', name: 'False' }
        ]
    });

    accsJson.types.push(
    {
        name: 'Print Size', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_PS_L0', name: 'None', selected: true },
            { code: 'TDS_PS_L1', name: 'Level 1' },
            { code: 'TDS_PS_L2', name: 'Level 2' },
            { code: 'TDS_PS_L3', name: 'Level 3' },
            { code: 'TDS_PS_L4', name: 'Level 4' }
        ]
    });

    accsJson.types.push(
    {
        name: 'Font Type', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_FT_San-Serif', name: 'Arial', selected: true },
            { code: 'TDS_FT_Serif', name: 'Times New Roman' },
            { code: 'TDS_FT_Verdana', name: 'Verdana' }
        ]
    });

    accsJson.types.push(
    {
        name: 'Font Size', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_F_S12', name: '12 Point', selected: true },
            { code: 'TDS_F_S14', name: '14 Point' }
        ]
    });

    accsJson.types.push(
    {
        name: 'TTS', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_TTS0', name: 'Disabled', allowCombine: false },
            { code: 'TDS_TTS_Item', name: 'Item', allowCombine: true, selected: true },
            { code: 'TDS_TTS_Stim', name: 'Stimulus', allowCombine: true, selected: true }
        ]
    });
	
    accsJson.types.push(
    {
        name: 'TTS Pausing', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_TTSPause0', name: 'False' },
            { code: 'TDS_TTSPause1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'TTS Tracking', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_TTSTracking0', name: 'False' },
            { code: 'TDS_TTSTracking1', name: 'True', selected: true }
        ]
    });


    accsJson.types.push(
    {
        name: 'Masking', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_Masking0', name: 'False' },
            { code: 'TDS_Masking1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Highlight', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_Highlight0', name: 'False' },
            { code: 'TDS_Highlight1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Expandable Passages', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_ExpandablePassages0', name: 'False' },
            { code: 'TDS_ExpandablePassages1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Item Tools Menu', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_ITM0', name: 'False' },
            { code: 'TDS_ITM1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Item Response Reset', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_ItemReset0', name: 'False', selected: true },
            { code: 'TDS_ItemReset1', name: 'True' }
        ]
    });

    accsJson.types.push(
    {
        name: 'Print on Request', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_PoD0', name: 'None', allowCombine: false },
            { code: 'TDS_PoD_Item', name: 'Item', allowCombine: true, selected: true },
            { code: 'TDS_PoD_Stim', name: 'Stimulus', allowCombine: true, selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Guide for Revision', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_GfR0', name: 'False' },
            { code: 'TDS_GfR1', name: 'True', selected: true }
        ]
    });
    
    accsJson.types.push(
    {
        name: 'Mark for Review', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_MfR0', name: 'False' },
            { code: 'TDS_MfR1', name: 'True', selected: true }
        ]
    });
    
    accsJson.types.push(
    {
        name: 'Student Comments', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_SC0', name: 'False' },
            { code: 'TDS_SC1', name: 'True', selected: true }
        ]
        });

    accsJson.types.push(
    {
        name: 'Tutorial', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_T0', name: 'False' },
            { code: 'TDS_T1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Strikethrough', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_ST0', name: 'False' },
            { code: 'TDS_ST1', name: 'True', selected: true }
        ]
    });
  
    accsJson.types.push(
    {
        name: 'Line Reader', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_LR0', name: 'False' },
            { code: 'TDS_LR1', name: 'True', selected: true }
        ]
    });
  
    accsJson.types.push(
    {
        name: 'Feedback', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'LPN_FB0', name: 'False' },
            { code: 'LPN_FB1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Word List', allowChange: false, isSelectable: false, isVisible: false, values:
        [
            { code: 'TDS_WL_DICT', name: 'Dictionary', selected: true },
            { code: 'TDS_WL_ESNGlossary', name: 'Dictionary', selected: true },
            { code: 'TDS_WL_THES', name: 'Thesaurus', selected: false}
        ]
    });

    // MEASUREMENT TOOLS:
    accsJson.types.push(
    {
        name: 'Compass', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_Compass0', name: 'False' },
            { code: 'TDS_Compass1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Straight Line', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_StraightLine0', name: 'False' },
            { code: 'TDS_StraightLine1', name: 'True', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Ruler', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_Ruler0', name: 'False', selected: true },
            { code: 'TDS_Ruler1', name: 'True' }
        ]
    });

    accsJson.types.push(
    {
        name: 'Protractor', allowChange: true, isSelectable: true, isVisible: true, values:
        [
            { code: 'TDS_Protractor0', name: 'False', selected: true },
            { code: 'TDS_Protractor1', name: 'True' }
        ]
    });
    
    accsJson.types.push(
    {
        name: 'Scaffolding Voice Guidance',
        allowChange: true,
        isSelectable: true,
        isVisible: true,
        values:
        [
            { code: 'TDS_ASIGuide0', name: 'True', selected: true },
            { code: 'TDS_ASIGuide1', name: 'False' }
        ]
    });

    accsJson.types.push(
    {
        name: 'Audio Playback Controls',
        allowChange: true,
        isSelectable: true,
        isVisible: true,
        values:
        [
            { code: 'TDS_APC_PS', name: 'Play and Stop', selected: false },
            { code: 'TDS_APC_PSP', name: 'Play Stop and Pause', selected: true }
        ]
    });

    accsJson.types.push(
    {
        name: 'Streamlined Mode',
        allowChange: true,
        isSelectable: true,
        isVisible: true,
        values:
        [
            { code: 'TDS_SLM0', name: 'False', selected: true },
            { code: 'TDS_SLM1', name: 'True', selected: false }
        ]
    });

    accsJson.types.push(
    {
        name: 'Paginate Item Groups',
        allowChange: true,
        isSelectable: true,
        isVisible: true,
        values:
        [
            { code: 'TDS_PIG0', name: 'False', selected: true },
            { code: 'TDS_PIG1', name: 'True', selected: false }
        ]
    });

    accsJson.types.push(
    {
        name: 'HTMLEditor Button Groups',
        allowChange: true,
        isSelectable: true,
        isVisible: true,
        values:
        [
            { code: 'TDS_HEBG_None', name: 'All button enabled', selected: true },
            { code: 'TDS_HEBG_BasicStyles', name: 'Disable Basic Styles', allowCombine: true },
            { code: 'TDS_HEBG_Cleanup', name: 'Disable Cleanup', allowCombine: true },
            { code: 'TDS_HEBG_List', name: 'Disable List', allowCombine: true },
            { code: 'TDS_HEBG_Indent', name: 'Disable Indent', allowCombine: true },
            { code: 'TDS_HEBG_Clipboard', name: 'Disable Clipboard', allowCombine: true },
            { code: 'TDS_HEBG_Undo', name: 'Disable Undo/Redo', allowCombine: true },
            { code: 'TDS_HEBG_SpellChecker', name: 'Disable Spell Check', allowCombine: true },
            { code: 'TDS_HEBG_Tables', name: 'Disable Insert Table', allowCombine: true },
            { code: 'TDS_HEBG_SpecialChar', name: 'Disable Special Characters', allowCombine: true }
        ]
    });
    
    return accsJson;
};

// load a delimited string into accommodations

function loadAccommodationsDelimitted(accommodations, delimited)
{
    // create hash table of types and codes 
    var accHash = {};

    var codes = delimited.split(';');
    
    // go through each code and lookup type to add to hash table
    for (var i = 0; i < codes.length; i++)
    {
        var code = codes[i];
        var accType = findAccommodationType(accommodations, code);
        
        if (accType != null)
        {
            var typeName = accType.getName();
            
            if (accHash[typeName] == null)
            {
                accHash[typeName] = [];
            }

            accHash[typeName].push(code);
        }
    }
    
    // go through each hash table group and select codes
    for (var typeName in accHash)
    {
        accommodations.selectCodes(typeName, accHash[typeName]);
    }
}

// return the accommodation type for a code
function findAccommodationType(accommodations, code)
{
    var accTypes = accommodations.getTypes();

    for (var i = 0; i < accTypes.length; i++)
    {
        var accType = accTypes[i];
        var accValues = accType.getValues();

        for (var j = 0; j < accValues.length; j++)
        {
            var accValue = accValues[j];
            
            if (accValue.getCode() == code)
            {
                return accType;
            }
        }
    }

    return null;
}

function createAccommodations()
{
    // add default accommodations
    var accommodationsJson = getAccommodationsJson();
    var accommodations = Accommodations.Manager.getCurrent();
    accommodations.importJson(accommodationsJson);
    // accommodations.selectDefaults();

    // create accommodations dialog
    var accContainer = BlackboxDoc.getElementById('accDialog');
    
    if (accContainer == null) {
    
        // create dialog container
        accContainer = Util.Dom.createElementFromHtml(' \
        <div id="accDialog" style="display: none;"> \
	        <div class="hd">Choose Accommodations:</div> \
	        <div class="bd"> \
	            <form id="accForm" name="accForm" method="post" action=""></form> \
	        </div> \
        </div>');

        document.body.appendChild(accContainer);
    }

    accDialog = new Accommodations.Dialog(accommodations, accContainer);
    
    // try and reload accommodations from cookie
    var accsCookie = YAHOO.util.Cookie.get("accommodations"); 
    
    if (accsCookie != null)
    {
        loadAccommodationsDelimitted(accommodations, accsCookie);
    }
    
    // fired when someone updates accommodations
    accDialog.onSave.subscribe(function(accs)
    {
        // save in cookie
        var accsDelimited = accs.getSelectedDelimited();
        YAHOO.util.Cookie.set('accommodations', accsDelimited);

        // reload
        TDS.Dialog.showProgress();
        window.location.reload();
    });
    
    // set custom accommodations
    if (typeof window.accCustom == 'object')
    {
        for(var i = 0, ii = window.accCustom.length; i < ii; i++)
        {
            var acc = window.accCustom[i];
            accommodations.selectCodes(acc.type, acc.codes);
        }
    }
}

function showAccommodations()
{
    accDialog.show();
}
