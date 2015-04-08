define(['underscore', 'backbone'], function(_, Backbone){
	 var EventBus = _.extend({}, Backbone.Events);
	 return EventBus;
});