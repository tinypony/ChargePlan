define(['jquery', 
        'jquery-ui',
        'underscore', 
        'backbone', 
        'config-manager', 
        'amcharts.serial', 
        'views/simulation/stops-visual', 
        'views/simulation/bus-details', 
        'views/simulation/charger-details',
        'collections/chargers',
        'collections/buses',
        'hbs!templates/simulation'], 
    function($, JUI, _, Backbone, ConfigManager, amRef, RouteVisualizationView, 
        BusDetailsView, ChargerDetailsView, Chargers, Buses, template) {

  var SimulationView = Backbone.View.extend({
    
    events: {
      'change .bus-select': 'onBusSelect',
      'change #route-date' : 'onParamChange'
    },
    
    onParamChange: function() {      
      var opts = {
        date: this.$('#route-date').val(),
        busType: this.buses.get( this.$('.bus-select').val()),
        minWaitingTime: 12 * 60
      };
      
      if(!_.isUndefined(opts.date) && !_.isUndefined(opts.busType)) {
        this.runSimulation(opts);
      }
    },
    
    initialize: function(options) {
      var self = this;
      this.routeName = options.route;
      this.route;
      
      _.bindAll(this, ['onParamChange']);
      
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
        self.listenTo(proj, 'sync', self.onParamChange());
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
      this.onParamChange();
    },
    
    onChargerSelect: function() {
      var selectedbusid = this.$('.charger-select').val();
      var chargerModel = this.chargers.get(selectedbusid);
      this.chargerDetails.showModel(chargerModel);
    },
    
    runSimulation: function(opts) {
      var self = this;
      
      $.ajax({
        url: '/api/projects/'+this.project.get('id')+'/simulate',
        data: JSON.stringify({
          routeId: this.route.routeId,
          date: opts.date,
          busType: opts.busType,
          minWaitingTime: opts.minWaitingTime
        }),
        method: 'POST',
        contentType: 'application/json'
      }).done(function(data) {
        if(data.survived) {
          //self.$('.runbutton .glyphicon').addClass('glyphicon-ok').removeClass('glyphicon-remove');
        } else {
          //self.$('.runbutton .glyphicon').addClass('glyphicon-remove').removeClass('glyphicon-ok');
        }
        
        var chart = amRef.makeChart('simulation-result-chart', {
          'theme' : 'none',
          'type' : 'serial',
          'autoMargins' : false,
          'marginLeft' : 70,
          'marginRight' : 8,
          'marginTop' : 10,
          'marginBottom' : 70,
          'pathToImages' : 'http://www.amcharts.com/lib/3/images/',
          'dataProvider' : data.batteryHistory,
          'valueAxes' : [ {
            'id' : 'v1',
            'axisAlpha' : 0,
            'inside' : false,
            'min' : 0,
            'minimum' : 0,
            'max' : 100,
            'maximum' : 110,
            'gridAlpha' : 0.1,
            'title' : 'Battery level (%)'
          } ],
          'graphs' : [ {
            'useNegativeColorIfDown' : false,
            'balloonText' : '[[category]]<br><b>value: [[value]]</b>',
            'bullet' : 'round',
            'bulletBorderAlpha' : 1,
            'bulletBorderColor' : '#FFFFFF',
            'hideBulletsCount' : 50,
            'lineThickness' : 2,
            'lineColor' : '#0088cc',
            'valueField' : 'charge'
          } ],
          'chartCursor' : {
            'valueLineEnabled' : true,
            'valueLineBalloonEnabled' : true
          },
          'categoryField' : 'timestamp',
          'categoryAxis' : {
            'axisAlpha' : 0,
            'gridAlpha' : 0,
            'maximum' : 110,
            'max' : 110,
            'minHorizontalGap' : 60,
            'title' : 'Time',
            'parseDates': true,
            'dataDateFormat': 'HHmm',
            'minPeriod': 'hh'
          }
        });
      });
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