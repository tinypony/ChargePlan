define(['jquery', 
        'jquery-ui',
        'underscore', 
        'backbone', 
        'config-manager', 
        'views/simulation/stops-visual', 
        'hbs!templates/simulation'], 
    function($, JUI, _, Backbone, ConfigManager, RouteVisualizationView, template) {

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
      
      var availableDates = _.map(this.getInstance().stats, function(stat) {
    	  return stat.date;
      });
      
      var available = function(date) {
        dmy = date.getFullYear() + '-' + (date.getMonth()+1) + "-" + date.getDate();
        if ($.inArray(dmy, availableDates) != -1) {
          return [true, '','Available'];
        } else {
          return [false, '', 'unAvailable'];
        }
      }
      
      this.$('#route-date').datepicker({ beforeShowDay: available, dateFormat:'yy-m-d' });
      
      
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