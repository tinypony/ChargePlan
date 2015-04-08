define(['jquery', 
        'underscore', 
        'backbone', 
        'config-manager', 
        'event-bus', 
        'hbs!templates/overview/details/project-details'], 
        
		function($, _, Backbone, ConfigManager, EventBus, template) {
	
	var ProjectDetailsView = Backbone.View.extend({
		initialize: function() {
			var self = this;
			
			this.listenTo(EventBus, 'add:route', function(routeBag) {
				self.model.addRoute(routeBag);				
				self.$('.active-routes').append('<div class="route-entry" data-routename="'+routeBag.name+'">'+routeBag.name+'</div>');
			});
			
			this.listenTo(EventBus, 'remove:route', function(routeBag) {				
				self.$('.route-entry[data-routename="'+routeBag.name+'"]').remove();
				self.model.removeRoute(routeBag.name);
			});
		},
		
		render: function() {
			var self = this;
			ConfigManager.getProject().done(function(project){
				self.model = project;
				
				self.$el.html(template({
					routes: project.get('routes')
				}));
				
				
			});
		}
	});
	
	return ProjectDetailsView;
	
});