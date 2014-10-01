
Text to Speech
============================================================================================================
These are the modules responsible for using the text to speech support built into the AIR secure browser.
This also supports certain flavors of Chrome and some sort of native Android hackery.

Note that speech only works in SecureBrowser, Chrome and Android.  You cannot really test in Firefox, even
nastier the HTML and speech format that is parsed looks very different browser to browser.

The most recent versions of the TTS code will be in mercurial at:
hg clone https://bugz.airast.org/kiln/Code/TDS-Student-Labs/Tools/TTS


Code Structure
============================================================================================================
TTSControl
 -> Only one instance of this class should be created and used in a page.
 -> This is the top level module now used by the test code.  And instance wraps the TTSManager API (hell) and
 then tries to do the correct thing.  See the class for ussage (typically TTSControlInstance.play)

 //Example
 var onlyOnePerPage = new TTS.Control(); 
     onlyOnePerPage.play("ph'nglui mglw'nafh Cthulhu R'lyeh wgah'nagl fhtagn"); //Play text
     onlyOnePerPage.play(document.getElementById("R'lyeh")); //Play text in this dom element
     onlyOnePerPage.play(); //Plays your selection if avail, else the document.body

TTSHighlighter 
 -> Class responsible for listening to the speech events and highlighting the currently spoken
word.  A new instance is created each time you hit play and it simply has a word queue and rangy selection
methods.  Created by TTSControl.

TTSManager: 
 -> Base level API and hacks, this is _not_ a place you want to be calling if you can help it.
 -> Parsers (TTSParser.HTML, TTSParser.DOM, TTSParser.Selection) 
 -> Browser (android, chrome, securebrowser) Browser specific event playing and plugin arg

VoicePack
 -> The loading and manipulation of the various available voice packs.


Chrome Extension (knowledge from Balaji)
============================================================================================================
The chrome TTS events are listed here: http://developer.chrome.com/extensions/tts.html
 
On Chrome you will need to enable the extension that is located in this directory. The extension source 
code is available in ChromeExtension in this dir. (the CVS code is checkout SecureBrowser/ChromeExtension)
 
Go to about:settings under the extensions tab and enable developer mode (to load custom extentions). Load 
the AIRSecureExtension folder as an “unpacked extension” and you are ready to roll.

 

TDS Message Tool
============================================================================================================
Here is a description of the TDS Messages Tool that you may find in the folder.  This does what exactly?
C:\WorkSpace\TDSCore\AppsCurrent\Tools\Messages - open Messages.sln in the Visual Studio 2010.
 
Set the main project to TDS.Shared.Messages.Console and just run it – it may also be run from the command 
line without the UI with the following arguments:
“-c <Client Name> -o <Output File Name> -l <comma separated languages> -k <comma separated contexts>”. Use “-h” to get more information.
   
Step 1: Verify it is pointing to the right data base:
     
Step 2: Select Client

Step 3: Depending on client requirements select either ESN or HAW as additional languages – it will extract translations for the selected languages.
      
Step 4: Specify contexts. I recommend leaving it at “Generate for all contexts”.  If running from as a command line tool however you will need to specify all relevant contexts.

Step 5: Specify the output path.

Step 6: If you are trying to debug something then you may choose to pretty print – for production purposes leave that unchecked.

Step 7: Click on “Get messages with these settings” and check to make sure that it did not report any error.
