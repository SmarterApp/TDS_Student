/**
 * Object that represents a word that is being referenced (prev, next or current word).
 * Allows us to retrieve this specific word within the DOM.
 */
TTS = window.TTS || {};

TTS.Highlighter = function(textArr, debugStr){

  this.wordQueue   = this.createIndex(textArr);
  this.wordPointer = -1;

  //Test Case variables and trackers.
  this.wordBoundaryCounter = 0; //Total number of words, used in test cases.
  //Purely for matching up the string you send to the speech engine, with what this module thinks
  //should be considered word boundaries.
  this.playStr = debugStr || 'Not specified';

  TTS.Config.Debug && console.log("Highlighter initialized with: ", textArr, this.wordQueue);
};

/**
 *  Used in the test case function to make sure you have something sane to read.
 */
TTS.Highlighter.prototype.getOnlyWords = function(){
  if(!this.wordQueue){return [];}

  var words = [];
  for(var i=0; i<this.wordQueue.length; ++i){
    words.push(this.wordQueue[i].text);
  }
  return words;

};

//Build up the range of words.
TTS.Highlighter.prototype.parseWords = function(textArr){
    var entry = null;
    for(var i=0; i<textArr.length; i++){
      entry = textArr[i];
      entry.words = this.getWords(this.cleanse(entry.text));
    }

};

//Detect and handle cross node 'words' that need to be highlighted.
TTS.Highlighter.prototype.mergeCrossBoundaryWords = function(textArr){
    var entry = null;
    for(var i=textArr.length-1; i > 0; --i){
      var entry = textArr[i];
      var prev  = textArr[i-1];

      if(prev && this.detectCrossElementWord(prev, entry)){
        var last  = prev.words.length-1;
        var merge = entry.words.shift();
        var start = prev.words[last];

        prev.words[last] += merge;
        if(entry.words.length == 0){ //For when there is only one word that might _also_ be a cross el
          prev.lastNode = entry.lastNode || entry.node;
        }else{ //Standard cross element matchup
          prev.lastNode = entry.node;
        }
        prev.endStr    = entry.endStr   || merge;
        prev.startStr  = start;
      }
    }

};

TTS.Highlighter.prototype.createRanges = function(textArr){
    var entry = null;
    var wordArr = []; //What we can actually trust to play on each word 'boundary'

    for(var i=0; i<textArr.length; ++i){
      entry = textArr[i];
      for(var j=0; entry.words && j < entry.words.length; ++j){
        var rng = null;
        if(j+1 == entry.words.length && entry.lastNode){
            rng = this.createCrossElRange(entry.node, entry.startStr, entry.lastNode, entry.endStr);
            if(rng){
              rng.crossEl = true;
            }
        }else{
            rng = this.createRange(entry.words[j], entry.node, entry);
        }

        wordArr.push({
          last: (j+1 == entry.words.length), //Are we a bounding element?
          text: entry.words[j], 
          node: entry.node, //Starting Element
          lastNode: entry.lastNode, //Possible ending element
          lastIndex: entry.lastIndex, //Possible ending index
          startStr: entry.startStr,
          rng: rng
        });
      }
    }
    console.log("Done setting up array");
    //Ensure that the common parent is reset somehow.
    return wordArr;
};


//Hacks to remove bad matches or elements that are removed when sent to the actual speech engine.
TTS.Highlighter.prototype.cleanse = function(text){
  if(typeof text != 'string' || !text.length > 1){
    return '';
  }
  text = text.replace(/"/g, " ");
  text = text.replace(/\u201C/g, " "); //left quote
 
  //for emphasis reasons i am putting in a semi-colon.
  text = text.replace(/\u201D/g, ""); //right quote
  return text;
};

//Need to clean this crap up a bit.
TTS.Highlighter.prototype.createIndex = function(textArr){
    if(!textArr || textArr.length < 1){return [];}

    //First parse out what are probably the word boundaries for this browser type.
    this.parseWords(textArr);

    //Next merge 'words' that are actually cross node strings, for example:
    //<span>hel</span><span class="someClass">lo</span>
    this.mergeCrossBoundaryWords(textArr);

    //Create all the ranges that will be used to select these elements when the are 'played'
    return this.createRanges(textArr);
};

/**
 *  Last index of a string (for cross boundary lookup only)
 */
TTS.Highlighter.prototype.lastIndexOf = function(match, inStr){
  var valid = -1;
  if(inStr != null && match!= null){
    valid = inStr.indexOf(match);
    if(valid >= 0){
      while(((valid + match.length) < inStr.length) && inStr.indexOf(match, valid+match.length) > 0){
        valid = inStr.indexOf(match, valid+match.length);  
      }
    }
  }
  return valid;
};

//Attempt to create a proper selection range.
TTS.Highlighter.prototype.createCrossElRange = function(nodeStart, strStart, nodeEnd, strEnd){
		var nodeValue = nodeStart.nodeValue;
		if (nodeValue == null) {
			nodeValue = nodeStart.innerHTML;
		}

    //TODO: Need to fix this search
    var index = this.lastIndexOf(strStart, nodeValue);
    if(index == -1){
      return;
    }
    var rng = rangy.createRange();
    try{
        rng.setStart(nodeStart, index);
        rng.setEnd(nodeEnd, strEnd.length);
    }catch(e){
      TTS.Config.Debug && console.error(
          "Cannot createCrossElRange with this information.",
          "nodeStart, strStart, nodeEnd, strEnd, e",
          nodeStart, strStart, nodeEnd, strEnd, e
      );
      rng.selectNode(nodeStart);
    }
    return rng;
};

//All sorts of freaky hacks and text tests.
//Find the index of a string, starting at info.processedIndex,
TTS.Highlighter.prototype.createRange = function(str, node, entry){
  //
	var nodeValue = node.nodeValue;
	if (nodeValue == null) {
		  nodeValue = node.innerHTML;
      if(nodeValue == '' || nodeValue == null){
        return;
      }
  }

  var rng = rangy.createRange();
  try{
    var startIndex = !isNaN(entry.positionIndex) ? entry.positionIndex : 0;
    var index = nodeValue.indexOf(str, startIndex);
    if(index != -1){
      if(node.nodeType != 3 && YUD.hasClass(node, 'TTS')){
        rng.selectNode(node);
      }else{
        entry.positionIndex = index + (str.length);
        rng.setStart(node, index);
        rng.setEnd(node, index + str.length);
      }
    }else{
        rng.selectNode(node);
    } //move the pointer forward (if not found then select entire node)
  }catch(e){
    TTS.Config.Debug && console.error("Cannot create a range given this information.", str, node, entry, e);
    rng.selectNode(node);
  }
  return rng;
};

 //Detect a word like "Some<span>thing</span> which is "something"
TTS.Highlighter.prototype.detectCrossElementWord = function(a, b){
   if(!a || !b || a.forceSpace || b.forceSpace){
     return false;
   }
   var txtA = a && a.text ? a.text : '';
   var txtB = b && b.text ? b.text : '';
   if(txtA.match(/[\.!\?\#\s,]$/)){
     return false;
   }
   if(txtB.match(/^[\.!\?\#\s,]/)){
     return false;
   }
   return true;
};

/**
 * Remove the selections.
 */
TTS.Highlighter.prototype.clear = function() {
    var sel = rangy.getSelection();
    sel.removeAllRanges();
    this.wordPointer = -1;
}


/**
 * Returns array of plain text words to be read that may be recognized by the highlighter.
 *
 * Overriden / provided by the TTS.Control class.   Each browser word boundary counts differently.
 */
TTS.Highlighter.prototype.getWords = function(html) {
  console.error("This is overriden on a per browser basis.  You should not see this message.");
}

TTS.Highlighter.prototype.highlight = function(info){
    if(info && info.rng){
      var sel = rangy.getSelection();
          sel.setSingleRange(info.rng);
    }

};

TTS.Highlighter.prototype.highlightPrev = function(){
    if(this.wordPointer <= 0){
      return false;
    }
    var word = this.wordQueue[--this.wordPointer];
    if(this.highlight(word)){
      return word;
    }
    return false;
};


TTS.Highlighter.prototype.highlightNext = function(){
    this.wordBoundaryCounter++; //Total number of words, used in test cases.
    if(this.wordPointer >= this.wordQueue.length){
      return false;
    }
    var word = this.wordQueue[++this.wordPointer];
    if(this.highlight(word)){
      return word;
    }
    return false;
};

/**
 *  Look for stop events and remove the highlights.
 */
TTS.Highlighter.prototype.onPlayStop = function(evt){
  if(evt == 'Stopped'){
    this.stop();
  }
};

/**
 * Stop playing and reset to original state.
 */
TTS.Highlighter.prototype.stop = function() {
	this.clear();
};

