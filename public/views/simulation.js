define(['jquery', 
        'underscore', 
        'backbone', 
        'config-manager', 
        'views/simulation/stops-visual', 
        'hbs!templates/simulation'], 
    function($, _, Backbone, ConfigManager, RouteVisualizationView, template) {

  var SimulationView = Backbone.View.extend({
    initialize: function(options) {
      var self = this;
      this.routeName = options.route;
      this.routeInstances = [];
      
      var onReady = _.after(2, function(){
        self.render();
      });
      
      $.get('/api/routes/'+options.route).done(function(instances){
        self.routeInstances = instances;
        onReady();
      });
      
      ConfigManager.getProject().done(function(proj){
        self.project = proj;
        onReady();
      });
    },
    
    getInstance: function() {
      return this.routeInstances[0];
    },
    
    render: function() {
      var self = this;
      
      this.$el.html(template({
        route: this.getInstance()
      }));
      
      this.routeVis = new RouteVisualizationView({
        el: this.$('.route-path-visualization'), 
        route: this.getInstance(),
        project: this.project
      });
      
      this.routeVis.render();
      
    }
  });
  
  return SimulationView;
});