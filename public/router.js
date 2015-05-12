define(['jquery', 
        'backbone', 
        'views/configure',
        'views/overview',
        'views/simulation',
        'views/solutions',
        'views/summary'], 
        function($, Backbone, ConfigurationView, Overview, SimulationView, SolutionsView, SummaryView) {
  
  var EMNRouter = Backbone.Router.extend({
    routes: {
      'configuration': 'configuration',
      '': 'routesOverview',
      'simulation/:route': 'simulation',
      'solutions' : 'solutions',
      'summary': 'summary'
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
    },
    
    solutions: function() {
      this.init();
      this.view = new SolutionsView({el: this.container()});
    },
    
    summary: function() {
    	this.init();
    	this.view = new SummaryView({el: this.container()});
    }
    
  });
  
  var router = new EMNRouter();
  return router;
});