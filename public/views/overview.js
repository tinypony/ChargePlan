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

      this.mapView = new MapView({
        el : this.$('#map')
      });

      this.mapView.setData(this.data);
      this.detailsView = new DetailsView({el: this.$('.retractable-container')});
      this.listView = new RouteList({
        el : this.$('.side-list'),
        project : this.data.project
      });
      this.renderSubviews();
    },

    renderSubviews : function() {
      var self = this;
      this.mapView.render();
      this.detailsView.render();

      this.mapView.displayData({
        routes : this.data.routes,
        stops : this.data.stops
      });

      this.listView.render();
      
      this.registerListeners();
    },
    
    registerListeners: function() {
      var self = this;
      
      this.listenTo(EventBus, 'route:select', function(routeId) {
        self.$('#map').addClass('hidden');
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
        self.$('#map').removeClass('hidden');
      });
    }

  });

  return RoutesOverview;
});