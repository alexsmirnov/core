
/** 
  * Global object container for RichFaces API.
  * All classes should be defined here.
  * @class
  * @name RichFaces
  * @static
  *
  * */
window.RichFaces = {};

(function(jQuery, richfaces) {
	
	// get DOM element by id or DOM element or jQuery object
	richfaces.getDomElement = function (source) {
		var type = typeof source;
		var element;
		if (type == "string") {
			// id
			element = document.getElementById(source);
		} else if (type == "object") {
			if (source.nodeType) {
				// DOM element
				element = source;
			} else 
			if (source instanceof jQuery) {
				// jQuery object
				element = source.get(0);
			}
		}
		return element;
	}
	
	// get RichFaces component object by component id or DOM element or jQuery object
	richfaces.$ = function (source) {
		var element = richfaces.getDomElement(source);

		if (element) {
			return (element["richfaces"] || {})["component"];
		}
	}
	
	// find component and call his method
	richfaces.invokeMethod = function(source, method) {
		var c = richfaces.$(source);
		if (c) {
			var f = c[method];
			if (typeof f == "function") {
				return f.apply(c, Array.prototype.slice.call(arguments, 2));
			}
		}
	}

	//dom cleaner
	richfaces.cleanDom = function(source) {
		var e = (typeof source == "string") ? document.getElementById(source) : jQuery('body').get(0);
		if (e) {
			var elements = e.getElementsByTagName("*");
			if (elements.length) {
				jQuery.cleanData(elements);
				jQuery.cleanData([e]);
				jQuery.each(elements, function(index) {
					richfaces.invokeMethod(this, "destroy");
				});
				richfaces.invokeMethod(e, "destroy");
			}
		}
	}
	
	//form.js
	richfaces.submitForm = function(form, parameters, target) {
		if (typeof form === "string") { form = jQuery(form) };
		var initialTarget = form.attr("target");
		var parameterInputs = new Array();
		try {
			form.attr("target", target);

			if (parameters) {
				for (var parameterName in parameters) {
					var parameterValue = parameters[parameterName];

					var input = jQuery("input[name='" + parameterName + "']", form);
					if (input.length == 0) {
						var newInput = jQuery("<input />").attr({type: 'hidden', name: parameterName, value: parameterValue});
						if (parameterName === 'javax.faces.portletbridge.STATE_ID' /* fix for fileUpload in portlets */) {
							input = newInput.prependTo(form);
						} else {
							input = newInput.appendTo(form);
						}
					} else {
						input.val(parameterValue);
					}

					input.each(function() {parameterInputs.push(this)});
				}
			}

			//TODO: inline onsubmit handler is not triggered - http://dev.jquery.com/ticket/4930
			form.trigger("submit");
		} finally {
			form.attr("target", initialTarget);
			jQuery(parameterInputs).remove();
		}
	};
	//

	//utils.js
	jQuery.fn.toXML = function () {
		var out = '';

		if (this.length > 0) {
			if (typeof XMLSerializer == 'function' ||
				typeof XMLSerializer == 'object') {

				var xs = new XMLSerializer();
				this.each(function() { out += xs.serializeToString(this); });
			} else if (this[0].xml !== undefined) {
				this.each(function() { out += this.xml; });
			} else {
				this.each( function() { out += this; } );
			}
		}

		return out;
	};

    //there is the same pattern in server-side code: 
	//org.ajax4jsf.javascript.ScriptUtils.escapeCSSMetachars(String)
	var CSS_METACHARS_PATTERN = /([#;&,.+*~':"!^$[\]()=>|\/])/g;

	/**
     * Escapes CSS meta-characters in string according to 
     *  <a href="http://api.jquery.com/category/selectors/">jQuery selectors</a> document.
     * 
     * @param s - string to escape meta-characters in
     * @return string with meta-characters escaped
	 */
	richfaces.escapeCSSMetachars = function(s) {
		//TODO nick - cache results
		
		return s.replace(CSS_METACHARS_PATTERN, "\\$1");
	};
	
	richfaces.log = (function(jQuery) {
		var LOG_LEVELS = {'debug': 1, 'info': 2, 'warn': 3, 'error': 4};
		var LOG_LEVEL_COLORS = {'debug': 'darkblue', 'info': 'blue', 'warn': 'gold', 'error': 'red'};
		var DEFAULT_LOG_LEVEL = LOG_LEVELS['info'];
		var currentLogLevel = DEFAULT_LOG_LEVEL;

		var consoleInitialized = false;

		var setLevel = function(level) {
			currentLogLevel = LOG_LEVELS[level] || DEFAULT_LOG_LEVEL;
			if (consoleInitialized) {
				getConsole().find("select.rich-log-element").val(currentLogLevel);
			}
			clear();
		};

		var setLevelFromSelect = function(iLevel) {
			currentLogLevel = iLevel || DEFAULT_LOG_LEVEL;
		};

		var clear = function() {
			if (window.console && useBrowserConsole) {
				window.console.clear();
			} else {
				var console = getConsole();
				console.children(".rich-log-contents").children().remove();
			}
		};

		var getConsole = function() {
			var console = jQuery('#richfaces\\.log');
			if (console.length != 0) {
				if (!consoleInitialized) {
					consoleInitialized = true;

					var clearBtn = console.children("button.rich-log-element");
					if (clearBtn.length == 0) {
						clearBtn = jQuery("<button type='button' class='rich-log-element'>Clear</button>").appendTo(console);
					}
					clearBtn.click(clear);

					var levelSelect = console.children("select.rich-log-element");
					if (levelSelect.length == 0) {
						levelSelect = jQuery("<select class='rich-log-element' />").appendTo(console);
					}

					if (levelSelect.children().length == 0) {
						for (var level in LOG_LEVELS) {
							jQuery("<option value='" + LOG_LEVELS[level]+ "'>" + level + "</option>").appendTo(levelSelect);
						}
					}

					levelSelect.val(currentLogLevel);
					levelSelect.change(function(event) { clear(); setLevelFromSelect(parseInt(jQuery(this).val(), 10)); return false;});

					var consoleEntries = console.children(".rich-log-contents");
					if (consoleEntries.length == 0) {
						consoleEntries = jQuery("<div class='rich-log-contents'></div>").appendTo(console);
					}
				}
			}

			return console;
		};

		var useBrowserConsole = false;

		var AM_PM = /(\s*(?:a|p)m)$/i;

		var getMessagePrefix = function(level) {
			var date = new Date();
			var formattedDate = date.toLocaleTimeString();

			var ms = (date.getMilliseconds() + 1000).toString().substr(1);
			if (AM_PM.test(formattedDate)) {
				formattedDate = formattedDate.replace(AM_PM, "." + ms + "$1");
			} else {
				formattedDate += "." + ms;
			}

			return level + '[' + formattedDate.replace() + ']: ';
		};

		var appendConsoleEntry = function(level, messagePrefix, messageText, console) {
			//TODO - cache jQuery("<element>")?
			var newEntry = jQuery(document.createElement("div")).appendTo(console.children(".rich-log-contents"));
			jQuery("<span style='color: " + LOG_LEVEL_COLORS[level] + "'></span>").appendTo(newEntry).text(messagePrefix);

			var entrySpan = jQuery(document.createElement("span")).appendTo(newEntry);
			if (typeof messageText != 'object' || !messageText.appendTo) {
				entrySpan.text(messageText);
			} else {
				messageText.appendTo(entrySpan);
			}
		};

		var appendBrowserConsoleEntry = function(level, text) {
			window.console[level]();
		};

		var appendMessage = function(level, message) {
			if (window.console && useBrowserConsole) {
				var text = getMessagePrefix(level) + message;
				appendBrowserConsoleEntry(level, text);
			} else {
				var console = getConsole();
				if (LOG_LEVELS[level] >= currentLogLevel && console.length != 0) {
					var messagePrefix = getMessagePrefix(level);
					appendConsoleEntry(level, messagePrefix, message, console);
				}
			}
		};

		var methods = {setLevel: setLevel, clear: clear};
		for (var logLevel in LOG_LEVELS) {
			var f = function(text) {
				appendMessage(arguments.callee.logLevel, text);
			};
			f.logLevel = logLevel;
			methods[logLevel] = f;
		}
		return methods;
	}(jQuery));

	/**
	 * Evaluates chained properties for the "base" object.
	 * For example, window.document.location is equivalent to
	 * "propertyNamesString" = "document.location" and "base" = window
	 * Evaluation is safe, so it stops on the first null or undefined object
	 *
	 * @param propertyNamesArray - array of strings that contains names of the properties to evaluate
	 * @param base - base object to evaluate properties on
	 * @return returns result of evaluation or empty string
	 */
	richfaces.getValue = function(propertyNamesArray, base) {
		var result = base;
		var c = 0;
		do {
			result = result[propertyNamesArray[c++]];
		} while (result && c != propertyNamesArray.length);

		return result;
	};

	var VARIABLE_NAME_PATTERN_STRING = "[_A-Z,a-z]\\w*";
	var VARIABLES_CHAIN = new RegExp("^\\s*"+VARIABLE_NAME_PATTERN_STRING+"(?:\\s*\\.\\s*"+VARIABLE_NAME_PATTERN_STRING+")*\\s*$");
	var DOT_SEPARATOR = /\s*\.\s*/;

	richfaces.evalMacro = function(macro, base) {
		var value = "";
		// variable evaluation
		if (VARIABLES_CHAIN.test(macro)) {
			// object's variable evaluation
			var propertyNamesArray = jQuery.trim(macro).split(DOT_SEPARATOR);
			value = richfaces.getValue(propertyNamesArray, base);
			if (!value) {
				value = richfaces.getValue(propertyNamesArray, window);
			}
		} else {
			//js string evaluation
			try {
				if (base.eval) {
					value = base.eval(macro);
				} else with (base) {
					value = eval(macro) ;
				}
			} catch (e) {
				richfaces.log.warn("Exception: " + e.message + "\n[" + macro + "]");
			}
		}

		if (typeof value == 'function') {
			value = value(base);
		}
		//TODO 0 and false are also treated as null values
		return value || "";
	};

	var ALPHA_NUMERIC_MULTI_CHAR_REGEXP = /^\w+$/;

	richfaces.interpolate = function (placeholders, context) {
		var contextVarsArray = new Array();
		for (var contextVar in context) {
			if (ALPHA_NUMERIC_MULTI_CHAR_REGEXP.test(contextVar)) {
				//guarantees that no escaping for the below RegExp is necessary
				contextVarsArray.push(contextVar);
			}
		}

		var regexp = new RegExp("\\{(" + contextVarsArray.join("|") + ")\\}", "g");
		return placeholders.replace(regexp, function(str, contextVar) {return context[contextVar];});
	};

	richfaces.clonePosition = function(element, baseElement, positioning, offset) {

	};
	//

	var pollTracker = {};
		richfaces.startPoll =  function(options) {
		var pollId = options.pollId;
		var interval = options.pollinterval;
		var ontimer = options.ontimer;
		richfaces.stopPoll(pollId);
	
		richfaces.setZeroRequestDelay(options);
	
		pollTracker[pollId] = window.setTimeout(function(){
			var pollElement = document.getElementById(pollId);
				try {
					ontimer.call(pollElement || window);
					richfaces.startPoll(options);
				} catch (e) {
					// TODO: handle exception
				}
		},interval);
	};

	richfaces.stopPoll =  function(id) {
		if(pollTracker[id]){
			window.clearTimeout(pollTracker[id]);
			delete pollTracker[id];
		}
	};
	
	var pushTracker = {};

	richfaces.startPush = function(options) {
		var clientId = options.clientId;
		var pushResourceUrl = options.pushResourceUrl;
		var pushId = options.pushId;
		var interval = options.interval;
		var ondataavailable = options.ondataavailable;
		richfaces.setZeroRequestDelay(options);
		
		richfaces.stopPush(pushId);

		pushTracker[pushId] = setTimeout(function() { // TODO: define this function in richfaces object to avoid definition every time when call startPush
			var ajaxOptions = {
				type: "HEAD",
				//TODO - encodeURIComponent; URL sessionId handling check
				//TODO - add pushUri supports
				url: pushResourceUrl + "?id=" + pushId,
				dataType: "text",
				complete: function(xhr) {
					var isPushActive = !!pushTracker[pushId];

					//TODO may someone wish to stop push from dataavailable handler?
					delete pushTracker[pushId];

					if (xhr.status == 200 && xhr.getResponseHeader("Ajax-Push-Status") == "READY") {
						var pushElement = document.getElementById(clientId);
						try {
							ondataavailable.call(pushElement || window);
						} catch (e) {
							// TODO: handle exception
						}
					}

					if (isPushActive) {
						richfaces.startPush(options);
					}
				}
			};

			if (options.timeout) {
				ajaxOptions.timeout = options.timeout;
			}

			jQuery.ajax(ajaxOptions);
		}, interval);
	};

	richfaces.stopPush = function(id) {
		if (pushTracker[id]){
			window.clearTimeout(pushTracker[id]);
			delete pushTracker[id];
		}
	};

	var jsfEventsAdapterEventNames = {
		event: {
			'begin': ['begin'],
			'complete': ['beforedomupdate'],
			'success': ['success', 'complete']
		},
		error: ['error', 'complete']
	};

	richfaces.createJSFEventsAdapter = function(handlers) {
		//hash of handlers
		//supported are:
		// - begin
		// - beforedomupdate
		// - success
		// - error
		// - complete
		handlers = handlers || {};

		return function(eventData) {
			var source = eventData.source;
			//that's request status, not status control data
			var status = eventData.status;
			var type = eventData.type;

			var typeHandlers = jsfEventsAdapterEventNames[type];
			var handlerNames = (typeHandlers || {})[status] || typeHandlers;

			if (handlerNames) {
				for (var i = 0; i < handlerNames.length; i++) {
					var eventType = handlerNames[i];
					var handler = handlers[eventType];
					if (handler) {
						var event = {};
						jQuery.extend(event, eventData);
						event.type = eventType;
						if (type != 'error') {
							delete event.status;
						}

						handler.call(source, event);
					}
				}
			}
		};
	};

	var setGlobalStatusNameVariable = function(statusName) {
		//TODO: parallel requests
		if (statusName) {
			richfaces['statusName'] = statusName;
		} else {
			delete richfaces['statusName'];
		}
	}

	richfaces.setZeroRequestDelay = function(options) {
		if (typeof options.requestDelay == "undefined") {
			options.requestDelay = 0;
		}
	};
	
	var getGlobalStatusNameVariable = function() {
		return richfaces.statusName;
	}

	var chain = function() {
		var functions = arguments;
		if (functions.length == 1) {
			return functions[0];
		} else {
			return function() {
				var callResult;
				for (var i = 0; i < functions.length; i++) {
					var f = functions[i];
					callResult = f.apply(this, arguments);
				}

				return callResult;
			};
		}
	};

	/**
	 * curry (g, a) (b) -> g(a, b)
	 */
	var curry = function(g, a) {
		var _g = g;
		var _a = a;

		return function(b) {
			_g(_a, b);
		};
	};

	var createEventHandler = function(handlerCode) {
		if (handlerCode) {
			return new Function("event", "data", handlerCode);
		}

		return null;
	};

	//TODO take events just from .java code using EL-expression
	var AJAX_EVENTS = (function() {
		var serverEventHandler = function(clientHandler, event) {
			var xml = jQuery("partial-response > extension#org\\.richfaces\\.extension", event.responseXML);

			var serverHandler = createEventHandler(xml.children(event.type).text());
			xml.end();

			var dataString = xml.children("data").text();
			xml.end();

			var data = null;
			if (dataString) {
				try {
					data = window["eval"]("(" + dataString + ")");
				} catch (e) {
					richfaces.log.warn("Error evaluating custom response data: " + e.message);
				}
			}

			if (serverHandler) {
				serverHandler.call(window, event, data);
			} else if (clientHandler) {
				clientHandler.call(window, event, data);
			}
		};

		return {
			'begin': null,
			'complete': serverEventHandler,
			'beforedomupdate': serverEventHandler
		}
	}());

	richfaces.ajax = function(source, event, options) {
		var sourceId = (typeof source == 'object' && source.id) ? source.id : source;
		var sourceElt = (typeof source == 'object') ? source : document.getElementById(sourceId);
		
		options = options || {};

		parameters = options.parameters || {}; // TODO: change "parameters" to "richfaces.ajax.params"
		parameters.execute = "@component";
		parameters.render = "@component";

		if (!parameters["org.richfaces.ajax.component"]) {
			parameters["org.richfaces.ajax.component"] = sourceId;
		}

		var eventHandlers;

		for (var eventName in AJAX_EVENTS) {
			var handler = createEventHandler(options[eventName]);

			var serverHandler = AJAX_EVENTS[eventName];
			if (serverHandler) {
				handler = curry(serverHandler, handler);
			}

			if (handler) {
				eventHandlers = eventHandlers || {};
				eventHandlers[eventName] = handler;
			}
		}

		if (options.status) {
			var namedStatusEventHandler = function() { setGlobalStatusNameVariable(options.status); };

			//TODO add support for options.submit
			eventHandlers = eventHandlers || {};
			if (eventHandlers.begin) {
				eventHandlers.begin = chain(namedStatusEventHandler, eventHandlers.begin);
			} else {
				eventHandlers.begin = namedStatusEventHandler;
			}
		}

		if (!jQuery(sourceElt).is(":input:not(:submit, :button, :image, :reset)")) {
			parameters[sourceId] = sourceId;
		}

		if (eventHandlers) {
			var eventsAdapter = richfaces.createJSFEventsAdapter(eventHandlers);
			parameters['onevent'] = eventsAdapter;
			parameters['onerror'] = eventsAdapter;
		}
		
		if (richfaces.queue) {
			parameters.queueId = options.queueId;
		}

		jsf.ajax.request(source, event, parameters);
	};

	var RICHFACES_AJAX_STATUS = "richfaces:ajaxStatus";

	var getStatusDataAttributeName = function(statusName) {
		return statusName ? (RICHFACES_AJAX_STATUS + "@" + statusName) : RICHFACES_AJAX_STATUS;
	};

	var statusAjaxEventHandler = function(data, methodName) {
		if (methodName) {
			//global status name
			var statusName = getGlobalStatusNameVariable();
			var source = data.source;

			var statusApplied = false;
			var statusDataAttribute = getStatusDataAttributeName(statusName);

			var statusContainers;
			if (statusName) {
				statusContainers = [jQuery(document)];
			} else {
				statusContainers = [jQuery(source).parents('form'), jQuery(document)];
			}

			for (var containerIdx = 0; containerIdx < statusContainers.length && !statusApplied;
				containerIdx++) {

				var statusContainer = statusContainers[containerIdx];
				var statuses = statusContainer.data(statusDataAttribute);
				if (statuses) {
					for (var statusId in statuses) {
						var status = statuses[statusId];
						var result = status[methodName].apply(status, arguments);
						if (result) {
							statusApplied = true;
						} else {
							delete statuses[statusId];
						}
					}

					if (!statusApplied) {
						statusContainer.removeData(statusDataAttribute);
					}
				}
			}
		}
	};

	var initializeStatuses = function() {
		var thisFunction = arguments.callee;
		if (!thisFunction.initialized) {
			thisFunction.initialized = true;

			var jsfEventsListener = richfaces.createJSFEventsAdapter({
				begin: function(event) { statusAjaxEventHandler(event, 'start'); },
				error: function(event) { statusAjaxEventHandler(event, 'error'); },
				success: function(event) { statusAjaxEventHandler(event, 'success'); },
				complete: function() { setGlobalStatusNameVariable(null); }
			});

			jsf.ajax.addOnEvent(jsfEventsListener);
			//TODO blocks default alert error handler
			jsf.ajax.addOnError(jsfEventsListener);
		}
	};

	richfaces.status = function(statusId, options) {
		this.statusId = statusId;
		this.options = options || {};
		this.register();
	};

	jQuery.extend(richfaces.status.prototype, (function() {
		//TODO - support for parallel requests

		var getElement = function() {
			var elt = document.getElementById(this.statusId);
			return elt ? jQuery(elt) : null;
		};

		var showHide = function(selector) {
			var element = getElement.call(this);
			if (element) {
				var statusElts = element.children();
				statusElts.each(function() {
					var t = jQuery(this);
					t.css('display', t.is(selector) ? '': 'none');
				});

				return true;
			}

			return false;
		};

		return {
			register: function() {
				initializeStatuses();

				var statusName = this.options.statusName;
				var dataStatusAttribute = getStatusDataAttributeName(statusName);

				var container;
				if (statusName) {
					container = jQuery(document);
				} else {
					container = getElement.call(this).parents('form');
					if (container.length == 0) {
						container = jQuery(document);
					};
				}

				var statuses = container.data(dataStatusAttribute);
				if (!statuses) {
					statuses = {};
					container.data(dataStatusAttribute, statuses);
				}

				statuses[this.statusId] = this;
			},

			start: function() {
				if (this.options.onstart) {
					this.options.onstart.apply(this, arguments);
				}

				return showHide.call(this, '.rich-status-start');
			},

			stop: function() {
				if (this.options.onstop) {
					this.options.onstop.apply(this, arguments);
				}
			},

			success: function() {
				if (this.options.onsuccess) {
					this.options.onsuccess.apply(this, arguments);
				}
				this.stop();

				return showHide.call(this, '.rich-status-stop');
			},

			error: function() {
				if (this.options.onerror) {
					this.options.onerror.apply(this, arguments);
				}
				this.stop();

				return showHide.call(this, ':not(.rich-status-error) + .rich-status-stop, .rich-status-error');
			}
		};
	}()));
	
	var ajaxOnComplete = function (data) {
		var type = data.type;
		var responseXML = data.responseXML;

		if (data.type == 'event' && data.status == 'complete' && responseXML) {
			var partialResponse = jQuery(responseXML).children("partial-response");
			if (partialResponse && partialResponse.length) {
				var elements = partialResponse.children('changes').children('update, delete');
				jQuery.each(elements, function () {
					richfaces.cleanDom(jQuery(this).attr('id'));
				});
			}
		}
	};
	// move this code to somewhere
	if (typeof jsf != 'undefined') {
		jsf.ajax.addOnEvent(ajaxOnComplete);
	}
	if (window.addEventListener) {
		window.addEventListener("unload", richfaces.cleanDom, false);
	} else {
		window.attachEvent("onunload", richfaces.cleanDom);
	}
}(jQuery, RichFaces));

