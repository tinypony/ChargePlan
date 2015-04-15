define(['jquery', 
        'jquery-ui',
        'underscore', 
        'backbone', 
        'config-manager', 
        'views/simulation/stops-visual', 
        'collections/chargers',
        'hbs!templates/simulation'], 
    function($, JUI, _, Backbone, ConfigManager, RouteVisualizationView, Chargers, template) {

  var SimulationView = Backbone.View.extend({
    initialize: function(options) {
      var self = this;
      this.routeName = options.route;
      this.route;
      
      var onReady = _.after(3, function(){
        self.render();
      });
      
      this.chargers = new Chargers();
      this.chargers.fetch().done(onReady);
      
      $.get('/api/routes/'+options.route).done(function(instance){
        self.route = instance;
        onReady();
      });
      
      ConfigManager.getProject().done(function(proj){
        self.project = proj;
        onReady();
      });
    },
    
    getInstance: function() {
      return this.route;
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
        project: this.project,
        chargers: this.chargers
      });
      
      this.routeVis.render();
    }
  });
  
  return SimulationView;
});