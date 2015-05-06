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

    events : {
      'click .add-routes': 'toggleTable'
    },

    initialize : function(optimize) {
      _.bindAll(this, ['toggleTable']);
      
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
    
    toggleTable: function() {
      if(this.detailsView.isHidden()) {
        this.detailsView.show();
        this.$('.add-routes').removeClass('show').addClass('back');
      } else {
        this.detailsView.hide();
        this.$('.add-routes').removeClass('back').addClass('show');
      }
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
    	this.mapView.remove({empty: true});
    },

    renderSubviews : function() {
      var self = this;
      this.createMap();
      this.detailsView.render();

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
      
      this.listenTo(EventBus, 'route:unselect', function(routeId) {
        self.routeDetails.remove();
        self.createMap();
      });
    }

  });

  return RoutesOverview;
});