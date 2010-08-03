/**
 * @author Pavel Yaschenko
 */


(function ($, richfaces, params) {
	
	richfaces.blankFunction = function (){}; //TODO: add it to global library
	
    /**
     * @class Base class for all components.
     * All RichFaces components should use this class as base or another RichFaces class which based on it.
	 *
		<pre><code>
		//Inheritance example:
		(function ($, richfaces, params) {
		
			// Constructor definition
			richfaces.MyComponent = function(componentId, [options]) {
				// call constructor of parent class
				$super.constructor.call(this, componentId, [options]);
			};
			
			// define private method
			var myPrivateMethod = function () {
			}
			
			// create container for protected method's links
			// in this example we link private method as protected
			var $p ={myProtectedMethod: myPrivateMethod};
			
			// Extend component class and add protected methods from parent class to our container
			$p = richfaces.BaseComponent.extend(richfaces.BaseComponent, richfaces.MyComponent, $p);
			
			// define super class link
			var $super = richfaces.MyComponent.$super;
		
			// Add new properties and methods
			$.extend(richfaces.MyComponent.prototype, (function (params) {
				return {
		 				name:"MyComponent", 
						f:function (){alert("hello");
					}
				};
			})(params));
		})(jQuery, RichFaces);
		</code></pre>
	 *
     * @memberOf RichFaces
     * @name BaseComponent
     *
     * @constructor
     * @param {String} componentId - component id
     * */
	richfaces.BaseComponent = function(componentId) {
		this.id = componentId;
	};
	
	/**
     * <protected> Attach component object to DOM element by component id, DOM element or jQuery object and returns the element
     *
     * @function
     * @name RichFaces.BaseComponent#attachToDom
     * @param {string|DOMElement|jQuery} source - component id, DOM element or DOM elements wrapped by jQuery
     *
     * @return {DOMElement}
     * */
	var attachToDom = function(source) {
		source = source || this.id;
		var element = richfaces.getDomElement(source);
		if (element) {
			element["richfaces"] = element["richfaces"] || {};
			element.richfaces.component = this;
		}
		return element;
	}
	
	var $p = {attachToDom:attachToDom};
	
	/**
     * Method extends child class prototype with parent prototype 
     * and return the object with parent's protected methods
     *
     * @function
     * @name RichFaces.BaseComponent.extend
     *
     * @return {object}
     * */
	richfaces.BaseComponent.extend = function (parent, child, h) {
		var F = richfaces.blankFunction;
		F.prototype = parent.prototype;
	    child.prototype = new F();
	    child.prototype.constructor = child;
	    child.$super = parent.prototype;
	    if (child.$super == richfaces.BaseComponent.prototype) {
	    	var r = jQuery.extend({}, $p, h||{});
	    }
	    
	    // create wrapper with protected methods and variables
	    child.extend = function (_parent, _child, _h) {
	    		var _r = jQuery.extend({}, r||h||{}, _h||{});
				return richfaces.BaseComponent.extend(_parent, _child, _r);
		}
	    return r||h;
	};
	
	$.extend(richfaces.BaseComponent.prototype, (function (params) {
		return {
			/**
             * Component name.
             *
             * @name RichFaces.BaseComponent#name
             * @type String
             * */
			name: "BaseComponent",
			
			/**
             * Method for converting object to string
             *
             * @function
             * @name RichFaces.BaseComponent#toString
             *
             * @return {String}
             * */
			toString: function() {
				var result = [];
				if (this.constructor.$super) {
					result[result.length] = this.constructor.$super.toString();
				}
				result[result.length] = this.name;
				return result.join(', ');
			},
			
			/**
             * Method returns element's id for event handlers binding.
             * Event API calls this method when binding by component object as selector was used.
             *
             * @function
             * @name RichFaces.BaseComponent#getEventElement
             *
             * @return {String}
             * */			
			getEventElement: function() {
				return this.id; 
			},
			
			/**
             * Destroy method. Will be called before remove component from the page 
             *
             * @function
             * @name RichFaces.BaseComponent#destroy
             *
             * */	
			destroy: function() {
			}
		};
	})(params));
     
})(jQuery, window.RichFaces || (window.RichFaces={}));