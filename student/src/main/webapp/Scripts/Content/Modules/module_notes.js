/**
 *  All note code for blackbox is handled under the TDS.Notes namespace with
 *  the typical needs and usage being a simple. TDS.Notes.open() or .close()
 *
 *  Detailed readme information about notes is in Notes/main.js (TDS.Notes)
 *
 *  The configuration of notes can be modified by defining TDS.Confg.NotesConfig 
 *
 *  The notes modules do not themselves save, but rather they have events that will
 *  provide information and the test shell is listening.  These are under:
 *  TDS.Notes.Events[Save|Load] which are standard YUI event systems.
 *
 *  If you want to provide a different note type for a particular item type, you can define
 *  item.NotesType = TDS.Notes.Types[TextArea|DropDown|ScratchPad]
 */
(function(){

    //Handle and manage any configuration that is being set.
    ContentManager.onItemEvent('comment', function(page, item){
      try{
        var notes = TDS.Notes.open(item); //For some reason the first render in blackbox bails...
        //notes.load(); //Only global notes currently saves to the DB :(
      }catch(e){
        console.error("Could not open a notes dialog for item?", item, e);
      }
    });
})();
