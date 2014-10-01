﻿TDS.Notes.ScratchPad = function(args){    
    args = args || {};
    this.dom = args.dom;
    this.type = TDS.Notes.Types.ScratchPad;
};
YAHOO.lang.extend(TDS.Notes.ScratchPad, TDS.Notes.Base);

TDS.Notes.ScratchPad.prototype.serialize = function(){
    return this.note.save();
};

TDS.Notes.ScratchPad.prototype.unserialize = function(response){
    if(!response){return;}
    if(typeof response == 'string' && response){
        response = JSON.parse(response);
    }
    if(response){
      this.note.load('list', response);
    }
    return true;
};

TDS.Notes.ScratchPad.prototype.create = function(){
    var SPF = ScratchPad.Factory.getInstance();

    var cId = 'note_container_' + this.dom.id;
    this.note = SPF.getOrCreateScratchPadById(cId);
    return this.note;
};
