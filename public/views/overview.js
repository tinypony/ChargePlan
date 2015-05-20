define([ 'jquery', 
         'underscore', 
         'backbone', 
         'mapbox', 
         'mocks', 
         'api-config', 
         'bootstrap', 
         'sidebar', 
         'config-manager', 
         'event-bus',
         'views/overview/details-view', 
         'views/overview/map-view', 
         'views/overview/raw-route-list', 
         'views/simulation', 
         'hbs!templates/overview' ], 
         
         function( $, _, Backbone, Mapbox, Mocks,
             ApiConfig, bootstrap, sidebar, ConfigManager, EventBus,
             DetailsView, MapView, RouteList, SimulationView, template ) {

  var RoutesOverview = Backbone.View.extend({
	  
	initialize : function(optimize) {   
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
    
    render : function() {
      var self = this;
      this.$el.html(template());
      this.detailsView = new DetailsView({el: this.$('.retractable-container')});
     
      this.listView = new RouteList({
        el : this.$('.side-list'),
        project : this.data.project
      });
      
      this.renderSubviews();
    },
    
    createMap: function() {
    	this.mapView = new MapView();
    	this.mapView.setData(this.data);
    	this.mapView.render();
    	this.$('.details-view').prepend(this.mapView.$el);
    	this.mapView.displayMap();
    },
    
    destroyMap: function() {
    	this.mapView.remove();
    },

    renderSubviews : function() {
      var self = this;
      this.createMap();
      this.detailsView.render();
      if(!this.data.project.get('routes').length) {
    	  this.detailsView.show();
      }

      this.listView.render();
      this.registerListeners();
    },
    
    registerListeners: function() {
      var self = this;
      
      this.listenTo(EventBus, 'route:add', function(route){
    	  self.data.project.addRoute(route);
    	  self.listView.addRoute(route);
    	  self.mapView.drawRoute(route);
      });
      
      this.listenTo(EventBus, 'route:remove', function(routeId){
    	  self.data.project.removeRoute(routeId);
    	  self.listView.removeRoute(routeId);
    	  self.mapView.clearRoute(routeId);
      });
      
      this.listenTo(EventBus, 'route:select', function(routeId) {
        self.destroyMap();
        
        if(self.routeDetails) {
          self.routeDetails.remove();
        }
        
        self.routeDetails = new SimulationView({
          route: routeId
        });
        
        self.$('#route-details').append(self.routeDetails.$el);
      });
      
      this.listenTo(EventBus, 'simulation:all', function() {
    	  if(self.listView.isSelected()) {
    		  EventBus.trigger('route:unselect');
    	  }

		  self.listView.render();
      }); 
      
      this.listenTo(EventBus, 'route:unselect', function() {
    	self.listView.onUnselect();
        self.routeDetails.remove();
        self.createMap();
      });
    }

  });

  return RoutesOverview;
});