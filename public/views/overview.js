define([ 'jquery', 
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
         'hbs!templates/overview' ], 
         function( $, _, Backbone, Mapbox, Mocks,
             ApiConfig, bootstrap, sidebar, ConfigManager, 
             DetailsView, MapView, RouteList, template ) {

  var RoutesOverview = Backbone.View.extend({

    events : {
      'click .add-routes': 'showTable'
    },

    initialize : function(optimize) {
      _.bindAll(this, ['showTable']);
      
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
    
    showTable: function() {
      this.detailsView.showRoutesTable();
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

      this.listView.render()
    }

  });

  return RoutesOverview;
});