define(['jquery', 
        'underscore',
        'backbone', 
        'views/configure',
        'views/overview',
        'views/simulation',
        'views/solutions',
        'views/summary',
        'hbs!templates/landing'], 
        function($, _, Backbone, ConfigurationView, Overview, SimulationView, SolutionsView, SummaryView, landing) {
  
  var EMNRouter = Backbone.Router.extend({
    routes: {
      'configuration': 'configuration',
      '': 'routesOverview',
      'simulation/:route': 'simulation',
      'solutions' : 'solutions',
      'summary': 'summary',
      
      'landing': 'land'
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
    },
    
    land: function() {
    	$('body').empty().append(landing());
    	_.defer(function(){

        	$('.feature').addClass('loaded');
        	$('.buttons').addClass('loaded');
    	});
    }
    
  });
  
  var router = new EMNRouter();
  return router;
});