define(['underscore','backbone'], function(_, Backbone){
	var ProjectModel = Backbone.Model.extend({
		urlRoot: '/api/projects',
		defaults: function() {
			routes: []
		},
		
		initialize: function() {
		},
		
		addRoute: function(routeBag) {
			var routes = this.get('routes');
			routes.push(routeBag);
			this.set('routes', routes);
			this.save();
		},
		
		removeRoute: function(routeName) {
			var routes = this.get('routes');
			routes = _.without(routes, _.findWhere(routes, {name: routeName}));
			this.set('routes', routes);
			this.save();
		}
	});
	
	return ProjectModel;
});