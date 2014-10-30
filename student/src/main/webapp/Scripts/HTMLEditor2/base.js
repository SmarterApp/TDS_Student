(function (CKEDITOR) {

    // check if ckeditor lib is available
    if (CKEDITOR == null) return;

    // We need to turn off the automatic inline editor creation 
    CKEDITOR.disableAutoInline = true;

    // check if we are using inline mode
    var useInline = false;
    var mode = Util.QueryString.parse().ckeditor;
    if (mode) {
        // someone manually defined mode
        useInline = (mode == 'inline');
    } else if (Util.Browser.isIOS() || Util.Browser.isAndroid()) {
        // tablets need inline for selection to work properly (especially android)
        useInline = true;
    }

    // fix paths for icons
    CKEDITOR.on('instanceCreated', function (ev) {
        var clipPath = HTMLEditor.resolveBaseUrl('Scripts/HTMLEditor2/plugins/clipboard/icons/');
        var spellPath = HTMLEditor.resolveBaseUrl('Scripts/HTMLEditor2/plugins/spellchecker/icons/');
        CKEDITOR.skin.addIcon('cut', clipPath + 'cut.png', 0, true);
        CKEDITOR.skin.addIcon('copy', clipPath + 'copy.png', 0, true);
        CKEDITOR.skin.addIcon('paste', clipPath + 'paste.png', 0, true);
        CKEDITOR.skin.addIcon('spellchecker', spellPath + 'spellchecker.png', 0, true);
    });

    // fix paths
    function fixPaths() {

        // version of ckeditor (should match what is in scripts_shared.xml)
        var version = '4.3';

        var rootPath = HTMLEditor.resolveBaseUrl('');
        var basePath = rootPath + 'Scripts/Libraries/ckeditor/' + version + '/';
        var pluginsPath = basePath + 'plugins/';
        var customPath = rootPath + 'Scripts/HTMLEditor2/plugins/';

        // set base path for ckeditor, needed for js file combiner
        if (CKEDITOR.basePath != basePath) {
            CKEDITOR.basePath = basePath;
        }

        // set base path for plugins
        if (CKEDITOR.plugins.basePath != pluginsPath) {
            CKEDITOR.plugins.basePath = pluginsPath;
        }

        // manually fix plugin paths since they are registered early with the wrong path
        var pluginName;
        for (pluginName in CKEDITOR.plugins.registered) {
            var plugin = CKEDITOR.plugins.registered[pluginName];

            // check if the base path is not correct
            if (plugin.path.indexOf(pluginsPath) == -1) {
                plugin.path = CKEDITOR.plugins.getPath(pluginName);
            }
        }

        // manually fix icon paths since they are registered early with the wrong path
        var iconName;
        for (iconName in CKEDITOR.skin.icons) {
            var icon = CKEDITOR.skin.icons[iconName];
            var fileName = icon.path.split('/').pop();

            // check if the path is not our custom plugins (for clipboard fix above)
            if (icon.path.indexOf(customPath) == -1) {
                icon.path = pluginsPath + fileName;
            }
        }
    }

    function createConfig(responseType) {

        // create ckeditor config
        var config = {
            title: '', // removes title attribute
            customConfig: '', // disables downloading config.js
            readOnly: true, // always lock to protect against fixDom() being triggered
            startupFocus: false,
            baseFloatZIndex: 50, // allow showAlert dialogs to be higher than cke_dialog_background_cover and dialogs
            dialog_noConfirmCancel: true,
            tabSpaces: 0,  // TAB key inserts 4 &nbsp characters into editor at cursor
            extraPlugins: 'clipboard,spellchecker,indent,customindent',
            removePlugins: 'sharedSpaces,floatingspace,resize,tableresize,wordcount',
            disableNativeSpellChecker: true,
            disableNativeTableHandles: true,
            height: '' // removes height attribute
        };

        // The toolbar groups arrangement, optimized for two toolbar rows.
        if (responseType == 'PlainTextSpell') {
            config.toolbarGroups = [
                { name: 'spellchecker' }
            ];
        } else {
            config.toolbarGroups = [
                { name: 'basicstyles', groups: ['basicstyles', 'cleanup'] },
                { name: 'paragraph', groups: ['list', 'indent', 'blocks', 'align'] },
                { name: 'clipboard', groups: ['clipboard', 'undo'] },
                { name: 'spellchecker' },
                { name: 'insert' } // <-- special characters
            ];
        }

        // Remove some buttons, provided by the standard plugins, which we don't
        // need to have in the Standard(s) toolbar.
        if (responseType == 'HTMLEditorTable') {
            config.removeButtons = 'Strike,Subscript,Superscript';
        } else {
            // Remove Table button, unless we are responseType HTMLEditorTable
            config.removeButtons = 'Strike,Subscript,Superscript,Table';
        }

        if (useInline) {
            config.removePlugins += ',wysiwygarea'; // remove iframe
        } else {
            config.removePlugins += ',divarea'; // remove inline
        }

        // if using iframes then add styles
        if (!useInline) {
            var styles = [];
            styles.push(HTMLEditor.resolveBaseUrl('Shared/CSS/accommodations.css'));
            styles.push(HTMLEditor.resolveBaseUrl('Scripts/HTMLEditor2/frame.css'));
            config.contentsCss = styles;
        }

        return config;
    }

    // this function is called when the editor and dom is ready 
    function onInstanceReady(editor) {

        editor.isReady = true;

        // add TDS class
        editor.container.addClass('editor');

        // get the main content element
        if (useInline) {
            var editable = editor.editable();
            editor.contentDom = editable.$; // editable <div>
        } else {
            editor.contentDom = editor.document.$.body; // frame <body>
        }

        // remove title attribute
        var editorEl = editor.element.$;
        if (editorEl.removeAttribute) {
            editorEl.removeAttribute('title');
        }

        // unlock editor
        setTimeout(function() {
            editor.setReadOnly(false);
        }, 0);
    }

    function createEditor(containerEl, responseType, configOverride) {

        // clear editor div
        containerEl.innerHTML = '';

        // create editor and add it to the container element 
        fixPaths();
        var config = createConfig(responseType);
        if (configOverride) {
            for (var property in configOverride){
                config[property] = configOverride[property];
            }
        }
        var editor = CKEDITOR.appendTo(containerEl, config);
        editor.isReady = false;

        // wait for editor to be ready
        editor.on('instanceReady', onInstanceReady.bind(null, editor));

        return editor;
    };

    var HTMLEditor = {};

    HTMLEditor.create = createEditor;

    // users of HTMLEditor need to always override this
    HTMLEditor.resolveBaseUrl = function (url) {
        return url;
    }

    // users of HTMLEditor need to always override this
    HTMLEditor.getLanguage = function() {
        return 'ENU';
    }

    window.HTMLEditor = HTMLEditor;

})(window.CKEDITOR);

/*
Any patches or hacks for CKEditor.
*/

(function(CKEDITOR) {

    // BUG 103684 Handle special character CKE dialog same as YUI dialogs
    // http://ckeditor.com/forums/CKEditor-3.x/dialogDefinition.onShow-adding-event-handler
    CKEDITOR.on('dialogDefinition', function (e) {
        var dialogName = e.data.name;
        var dialog = e.data.definition.dialog;
        dialog.on('show', function () {
            YUD.addClass(document.body, 'showingDialog');
        });
        dialog.on('hide', function () {
            YUD.removeClass(document.body, 'showingDialog');
        });
    });

    // BUG 116874 recommended solution for preventing drag&drop into editor
    CKEDITOR.on('instanceCreated', function (ev) {

        ev.editor.on('contentDom', function () {

            // check if iframe and get element
            var element = (document != ev.editor.document) ?
                ev.editor.document : ev.editor.element;

            // stop drag
            element.on('dragstart', function (ev) {
                ev.data.preventDefault(true);
            });
            element.on('drop', function (ev) {
                ev.data.preventDefault(true);
            });

            // stop contextmenu
            element.on('contextmenu', function (ev) {
                ev.data.preventDefault(true);
            });
        });

        // for debugging setting response...
        /*ev.editor.on('dataReady', function () {
            var editable = ev.editor.editable();
            console.log('ckeditor dataReady: ', editable.$.innerHTML);
        });*/

    });

    // BUG 116874 ariaWidget is fired when menu iframes are created.  
    // Add ondragstart='return false;' to iframe body to disable drag&drop of menu items
    // Justin tried to have this integrated into CKEditor
    // https://github.com/ckeditor/ckeditor-dev/pull/52
    CKEDITOR.on('ariaWidget', function (e) {
        var menuIframe = e.data;
        if (menuIframe.hasClass && !menuIframe.hasClass('dragDisabled')) {
            var doc = menuIframe.getFrameDocument();
            var menuBody = doc.getBody();
            if (doc && menuBody) {
                menuBody.setAttribute('ondragstart', 'return false;');
                menuIframe.addClass('dragDisabled');
            }
        }
    });
    
    /* 
    BUG: On Firefox 3.6 the mode is undefined until 'dataReady' is fired. 
    But we can still get the selection. Otherwise we will return null and cause problems.
    You can find original code in \ckeditor\core\selection.js.
    */
    if (Util.Browser.getFirefoxVersion() < 10) {
        CKEDITOR.editor.prototype.getSelection = function (forceRealSelection) {

            // Check if there exists a locked or fake selection.
            if ((this._.savedSelection || this._.fakeSelection) && !forceRealSelection)
                return this._.savedSelection || this._.fakeSelection;

            // Editable element might be absent or editor might not be in a wysiwyg mode.
            var editable = this.editable();
            if (editable && (this.mode === undefined || this.mode == 'wysiwyg')) {
                var sel = new CKEDITOR.dom.selection(editable);
                // BUG: fixDom() will get triggered unless dom selection is locked (editable.js line 1085)
                if (this.mode === undefined) {
                    sel.isLocked = 1;
                }
                return sel;
            }

            return null;
        };
    }

})(CKEDITOR);

// Check if user deleted everything
(function (CKEDITOR) {

    var charDiff = 10;

    function instanceCreated(editor) {

        // the last text that was changed by this editor instance
        var lastChange = '';

        // check for when text is changed
        editor.on('change', function (evt) {

            // get editor text
            var editable = editor.editable();
            var text = editable.getText();

            // remove all whitespaces
            if (text) {
                text = text.replace(/([\s\t\r\n]*)/gm, '');
            }

            // check if anyting has changed
            if (text != lastChange && text.length == 0 && lastChange.length > charDiff) {
                console.warn('EDITOR DELETE WARNING: ' + lastChange);
            }

            // save text for the next check
            lastChange = text;

        });
        
    }

    CKEDITOR.on('instanceCreated', function (ev) {
        instanceCreated(ev.editor);
    });
    
})(CKEDITOR);
