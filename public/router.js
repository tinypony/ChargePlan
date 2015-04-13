define(['jquery', 
        'backbone', 
        'views/configure',
        'views/overview',
        'views/simulation'], 
        function($, Backbone, ConfigurationView, Overview, SimulationView){
  
  var EMNRouter = Backbone.Router.extend({
    routes: {
      'configuration': 'configuration',
      'overview': 'routesOverview',
      'simulation/:route': 'simulation'
    },
    
    init: function() {
      if(this.view) {
        this.view.remove();
      }
      
      $('body').append('<div class="view-content"></div>');
    },
    
    container: function() {
      return $('body > .view-content');
    },

    configuration: function() {
      this.init();
      this.view = new ConfigurationView({el: this.container()});
    },
    
    routesOverview: function() {
      this.init();
      this.view = new Overview({el: this.container()});
    },
    
    simulation: function(routeName) {
      this.init();
      this.view = new SimulationView({el: this.container(), route: routeName});
    }
    
  });
  
  var router = new EMNRouter();
  return router;
});