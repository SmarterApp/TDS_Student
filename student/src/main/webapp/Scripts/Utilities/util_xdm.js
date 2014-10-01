if (typeof Util != 'object') Util = {};

/*
Cross-domain messaging API.
- Requires jQuery
- Supports promises
- Return objects from handlers
- Serializes error messages

Inspiration:
- http://engineering.wingify.com/posts/jquery-promises-with-postmessage/
- https://github.com/wingify/please.js

Example:

	XDM.init(window); // <-- window to listen on (call this on parent and frame)
			
	// register handler in frame
	XDM.addListener('TDS:setResponse', function(itemKey, value) {
		return { key: + new Date() };
	});

	// post to frame in parent
	XDM(frame).post('TDS:setResponse', 100, 'A')
		.then(function(obj) {
			console.log('setResponse data: ', obj.key);
		}, function(ex) {
			console.log('setResponse error: ', ex);
		});

*/

(function (Util) {

	var defaults = {
		targetWindow: window,
		targetOrigin: '*',
		sourceOrigin: false
	};

	var id = 0;
	var requests = {};
	var responses = {};
	var listeners = {};

	var XDM = function (targetWindow, targetOrigin) {
		return $.extend(XDM.bind(), {
			targetWindow: targetWindow,
			targetOrigin: targetOrigin,
			post: postRequest
		});
	};

	XDM.init = function (thisWindow) {
	    if (thisWindow.addEventListener) {
	        thisWindow.addEventListener('message', messageHandler, true);
	    }
		return XDM;
	};

	// register a listener
	XDM.addListener = function(name, callback) {
		listeners[name] = callback;
	};

	XDM.removeListener = function (name) {
		delete listeners[name];
	};

	// send a request
	function postRequest(requestName) {
		var req = new Request(requestName);
		req.targetWindow = this.targetWindow || defaults.targetWindow;
		req.targetOrigin = this.targetOrigin || defaults.targetOrigin;
		req.data = [].slice.call(arguments, 1);
		req.send();
		return req;
	}

	// recieve a request/response
	function messageHandler(evt) {

	    // console.log('MESSAGE RECIEVED', evt);

		try {
		    var data = JSON.parse(evt.data);
		} catch (ex) {
			console.log('XDM: error parsing json data');
			return;
		}

		if (data.type === 'request') {
		    console.log('MESSAGE REQUEST: ', data);
		    var response = new Response(data);
			responses[response.id] = response.data;
			response.targetWindow = evt.source;
			response.targetOrigin = evt.origin === 'null' ? defaults.targetOrigin : evt.origin;
			response.send();
		}
		else if (data.type === 'response') {
		    console.log('MESSAGE RESPONSE: ', data);
			if (data.success) {
			    requests[data.id].resolve(data.data);
			} else {
				requests[data.id].reject(new XDM.Error(data.data));
			}

			delete requests[data.id];
		}
	}

	function Request(name) {
		this.init.apply(this, [].slice.call(arguments));
	}

	function Response(req) {
		this.init(req);
	}

	Request.prototype.init = function(name) {

		$.extend(this, $.Deferred());

		this.id = ++id;
		this.name = name;
		this.data = [].slice.call(arguments);
		this.type = 'request';

		requests[id] = this;
	};

	Request.prototype.send = function() {
		this.targetWindow = this.targetWindow || defaults.targetWindow;
		this.targetOrigin = this.targetOrigin || defaults.targetOrigin;
		this.targetWindow.postMessage(JSON.stringify(this), this.targetOrigin);
	};

	Request.prototype.toJSON = function() {
		return {
			id: this.id,
			name: this.name,
			type: this.type,
			data: this.data
		};
	};

	Request.create = function (obj) {
		return $.extend(new Request(), obj);
	};

	Response.prototype.init = function(req) {
		this.id = req.id;
		this.name = req.name;
		this.type = 'response';
		try {
		    var response = Request.create(req);
		    var listener = listeners[req.name];
            if (listener) {
                this.data = listener.apply(this, response.data);
                this.success = true;
            } else {
                throw new Error('Could not find the listener \'' + req.name + '\'');
            }
		} catch (error) {
			this.data = new XDM.Error(error);
			this.success = false;
		}
	};

	Response.prototype.send = function() {
		this.targetWindow = this.targetWindow || defaults.targetWindow;
		this.targetOrigin = this.targetOrigin || defaults.targetOrigin;
		this.targetWindow.postMessage(JSON.stringify(this), this.targetOrigin);
		if (!this.success) {
			throw this.data.error;
		}
	};

	Response.prototype.toJSON = function() {
		return {
			id: this.id,
			name: this.name,
			type: this.type,
			data: this.data,
			success: this.success
		};
	};

	XDM.Error = function (error) {
		this.error = error;
		$.extend(this, error);
		this.name = error.name;
		this.message = error.message;

		// IE
		this.number = error.number;
		this.description = error.description;

		// Firefox
		this.fileName = error.fileName;
		this.lineNumber = error.lineNumber;

		// Chrome / Firefox / latest IE
		this.stack = error.stack;

		this.toString = function () {
		    return this.message;
		};

	};

	Util.XDM = XDM;

})(Util);
