define(['jquery', 
        'backbone', 
        'views/configure',
        'views/endstop-viz',
        'views/routes-viz',
        'views/route-select',
        'views/overview'], 
        function($, Backbone, ConfigurationView,
            EndStopView, RouteStatsView, RouteSelectionView, Overview){
  
  var EMNRouter = Backbone.Router.extend({
    routes: {
      'configuration': 'configuration',
      'routesviz': 'routesVisualization',
      'routestats': 'routeStats',
      'select': 'routeSelect',
      'overview': 'routesOverview'
    },
    
    init: function() {
      if(this.view) {
        this.view.remove();
      }
      
      $('body').append('<div class="view-content"></div>');
    },

    configuration: function() {
      this.init();
      this.view = new ConfigurationView({el: $('body > .view-content')});
   //   this.view.render();
    },
    
    routesVisualization: function() {
      this.init();
      this.view = new EndStopView({el: $('body > .view-content')});
     // this.view.render();
    },
    
    routeStats: function() {
      this.init();
      this.view = new RouteStatsView({el: $('body > .view-content')});
    },
    
    routeSelect: function() {
      this.init();
      this.view = new RouteSelectionView({el: $('body > .view-content')});
      this.view.render();
    },
    
    routesOverview: function() {
      this.init();
      this.view = new Overview({el: $('body > .view-content')});
    }
    
  });
  
  var router = new EMNRouter();
  return router;
});