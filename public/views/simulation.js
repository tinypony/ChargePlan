define(['jquery', 
        'jquery-ui',
        'underscore', 
        'backbone', 
        'config-manager', 
        'views/simulation/stops-visual', 
        'views/simulation/bus-details', 
        'views/simulation/charger-details',
        'collections/chargers',
        'collections/buses',
        'hbs!templates/simulation'], 
    function($, JUI, _, Backbone, ConfigManager, RouteVisualizationView, 
        BusDetailsView, ChargerDetailsView, Chargers, Buses, template) {

  var SimulationView = Backbone.View.extend({
    
    events: {
      'change .bus-select': 'onBusSelect',
      'change .charger-select': 'onChargerSelect'
    },
    
    initialize: function(options) {
      var self = this;
      this.routeName = options.route;
      this.route;
      
      var onReady = _.after(4, function(){
        self.render();
      });
      
      this.chargers = new Chargers();
      this.buses = new Buses();
      
      this.chargers.fetch().done(onReady);
      this.buses.fetch().done(onReady);
      
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
    
    onBusSelect: function() {
      var selectedbusid = this.$('.bus-select').val();
      var busModel = this.buses.get(selectedbusid);
      this.busDetails.showModel(busModel);
    },
    
    onChargerSelect: function() {
      var selectedbusid = this.$('.charger-select').val();
      var chargerModel = this.chargers.get(selectedbusid);
      this.chargerDetails.showModel(chargerModel);
    },
    
    
    
    render: function() {
      var self = this;
      
      this.$el.html(template({
        route: this.getInstance(),
        chargers: this.chargers.toJSON(),
        buses: this.buses.toJSON()
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
      
      this.busDetails = new BusDetailsView({
        el: this.$('.bus-details')
      });
      
      this.chargerDetails = new ChargerDetailsView({
        el: this.$('.charger-details')
      });
      
      this.busDetails.render();
      this.routeVis.render();
    }
  });
  
  return SimulationView;
});