define(['jquery', 'underscore', 'backbone', 'hbs!templates/report'], 
		function($, _, Backbone, template) {
	
	var ReportView = Backbone.View.extend({
		initialize: function() {
			
		},
		
		render: function() {
			this.$el.html(template());
		}
	});
	
	return ReportView;
});