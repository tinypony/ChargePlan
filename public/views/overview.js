define(['jquery', 
        'underscore', 
        'backbone', 
        'mapbox', 
        'mocks', 
        'api-config',
    		'bootstrap', 
    		'sidebar', 
    		'config-manager',
    		'views/overview/details-view', 
    		'views/overview/map-view',
    		'views/overview/raw-route-list', 
    		'hbs!templates/overview' ], function(
		$, _, Backbone, Mapbox, Mocks, ApiConfig, bootstrap, sidebar,
		ConfigManager, DetailsView, MapView, RouteList, template) {

	var RoutesOverview = Backbone.View.extend({

		initialize : function(optimize) {
			_.bindAll(this, [ 'displayData' ]);
			this.date = this.defaultDate;
			this.retrieveData(true);
		},

		retrieveData : function(isFirst) {
			var self = this;
			this.data = {};

			var onReady = _.after(3, function() {
				if (isFirst) {
					self.render();
				} else {
					self.displayData();
				}
			});

			$.get('/api/routes').done(function(data) {
				self.data.routes = data;
				onReady();
			});

			$.get('/api/stops').done(function(data) {
				self.data.stops = data;
				onReady();
			});

			ConfigManager.getProject().done(function(project) {
				self.data.project = project;
				onReady();
			});

		},

		displayData : function() {

		},

		render : function() {
			var self = this;
			this.$el.html(template());

			$('#side-list-container').slideReveal({
				trigger : $("#buses-button"),
				push : false,
				width : 320,
				show : function(panel, trigger) {
					panel.addClass('open')
				},
				hide : function(panel, trigger) {
					panel.removeClass('open');
				}
			}).removeClass('init');

			this.mapView = new MapView({
				el : this.$('#map')
			});
			
//			this.detailsView = new DetailsView({
//				el : this.$('.endstop-details')
//			});
			
			this.listView = new RouteList({
				//data : this.data.routes,
				project : this.data.project
			});
			this.renderSubviews();
		},

		renderSubviews : function() {

			var self = this;
			this.mapView.render();
		//	this.detailsView.render();
			//this.detailsView.showProjectDetails();
			this.mapView.displayData({
				routes : this.data.routes,
				stops : this.data.stops
			});

			this.$('.side-list').append(this.listView.render().$el);
		}

	});

	return RoutesOverview;
});