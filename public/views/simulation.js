define([ 'jquery', 
         'jquery-ui', 
         'underscore', 
         'backbone', 
         'const',
         'moment',
         'config-manager', 
         'amcharts.serial', 
         'views/simulation/stops-visual', 
         'views/simulation/bus-details', 
         'views/simulation/charger-details', 
         'views/simulation/mini-map-view',
         'collections/chargers', 
         'collections/buses', 
         'hbs!templates/simulation',
         'hbs!templates/misc/loading',
         'bsselect'], 
    function($, JUI, _, Backbone, Const, moment, ConfigManager, amRef, RouteVisualizationView, BusDetailsView, ChargerDetailsView, MiniMap, Chargers, Buses, template, loading) {

  var SimulationView = Backbone.View.extend({

    events : {
      'change .bus-select' : 'onBusSelect',
      'change #route-date' : 'onParamChange'
    },

    onParamChange : function() {
      var opts = {
        date : this.$('#route-date').val(),
        busType : this.buses.get(this.$('.bus-select').val()),
        minWaitingTime : 10 * 60
      };

      if (!_.isUndefined(opts.date) && opts.date.length && !_.isUndefined(opts.busType)) {
        this.runSimulation(opts);
      }
    },

    initialize : function(options) {
      var self = this;
      this.routeName = options.route;
      this.route;

      _.bindAll(this, [ 'onParamChange' ]);

      this.$el.html(loading());
      var onReady = _.after(4, function() {
        self.render();
      });

      this.chargers = new Chargers();
      this.buses = new Buses();

      this.chargers.fetch().done(onReady);
      this.buses.fetch().done(onReady);

      $.get('/api/routes/' + options.route).done(function(instance) {
        self.route = instance;
        onReady();
      });

      ConfigManager.getProject().done(function(proj) {
        self.project = proj;

        self.listenTo(proj, 'sync', function() {
          self.onParamChange();
        });

        onReady();
      });
    },

    getInstance : function() {
      return this.route;
    },

    onBusSelect : function() {
      var selectedbusid = this.$('.bus-select').val();
      var busModel = this.buses.get(selectedbusid);
      this.busDetails.showModel(busModel);
      this.onParamChange();
    },

    onChargerSelect : function() {
      var selectedbusid = this.$('.charger-select').val();
      var chargerModel = this.chargers.get(selectedbusid);
      this.chargerDetails.showModel(chargerModel);
    },

    runSimulation : function(opts) {
      var self = this;
      this.$('.simulation-results').append(loading());
      
      $.ajax({
        url : '/api/projects/' + this.project.get('id') + '/simulate',
        data : JSON.stringify({
          routeId : this.route.routeId,
          date : opts.date,
          busType : opts.busType,
          minWaitingTime : opts.minWaitingTime
        }),
        method: 'POST',
        contentType : 'application/json'
      }).done(function(data){
    	var costResult = data.cost;
        var feasibilityResult = data.feasibility;
        self.showFeasibility(feasibilityResult);
        self.showCost(costResult);
      });
    },
    
    showFeasibility: function(data) {
    	this.$('.simulation-results .loading-container').remove();
    	
        if(data.survived) {
        	this.project.getRoute(this.route.routeId).state = Const.RouteState.SIMULATED_OK;
        } else {
        	this.project.getRoute(this.route.routeId).state = Const.RouteState.SIMULATED_FAIL;
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
            'valueLineEnabled' : false,
            'valueLineBalloonEnabled' : false,
            'categoryBalloonDateFormat' : 'HH:NN'
          },
          'categoryField' : 'timestamp',
          'categoryAxis' : {
            'axisAlpha' : 0,
            'gridAlpha' : 0,
            'maximum' : 110,
            'max' : 110,
            'minHorizontalGap' : 60,
            'title' : 'Time',
            'parseDates' : true,
            'minPeriod' : 'ss'
          }
        });
    },
    
    showCost: function(data) {
    	data = _.sortBy(data, function(entry){
    		return moment(entry.date, 'YYYY-M-D').unix();
    	});
    	
        var chart = amRef.makeChart('cost-chart', {
            'theme' : 'none',
            'type' : 'serial',
            'autoMargins' : false,
            'marginLeft' : 70,
            'marginRight' : 8,
            'marginTop' : 10,
            'marginBottom' : 70,
            'pathToImages' : 'http://www.amcharts.com/lib/3/images/',
            'dataProvider' : data,
            'valueAxes' : [ {
              'id' : 'v1',
              'axisAlpha' : 0,
              'inside' : false,
              'min' : 0,
              'minimum' : 0,
              'gridAlpha' : 0.1,
              'title' : 'Cost (NOK)'
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
              'valueField' : 'energyPrice'
            }, {
                'useNegativeColorIfDown' : false,
                'balloonText' : '[[category]]<br><b>value: [[value]]</b>',
                'bullet' : 'round',
                'bulletBorderAlpha' : 1,
                'bulletBorderColor' : '#FFFFFF',
                'hideBulletsCount' : 50,
                'lineThickness' : 2,
                'lineColor' : '#000',
                'valueField' : 'dieselPrice'
              } ],
            'chartCursor' : {
              'valueLineEnabled' : false,
              'valueLineBalloonEnabled' : false,
              'categoryBalloonDateFormat' : 'DD, MMM'
            },
            'categoryField' : 'date',
            'categoryAxis' : {
              'axisAlpha' : 0,
              'gridAlpha' : 0,
              'minHorizontalGap' : 60,
              'title' : 'Date',
              'parseDates' : true,
              'minPeriod' : 'DD'
            }
          });
    },
    
    getRouteStats: function(route) {
      var stats = {};
      var runningAverageStat = function(statName, statName2) {
        return function(memo, stat, n) {
          
          if(_.isUndefined(statName2)) {
            return (stat[statName] + n * memo) / (n+1);
          } else {
            return (stat[statName][statName2] + n * memo) / (n+1);
          }
        };
      };
     
      stats.totalDistance = Math.floor(_.reduce(route.stats, runningAverageStat('totalDistance'), 0)) / 1000;
      stats.departures = Math.round(_.reduce(route.stats, runningAverageStat('departures'), 0));
      stats.routeLength = Math.round(_.reduce(route.stats, runningAverageStat('length'), 0)) / 1000;
      
      stats.CO2 = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'CO2'), 0));
      stats.CO = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'CO'), 0));
      stats.NOx = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'NOx'), 0));
      
      return stats;
    },

    render : function() {
      var self = this;
      var routeInstance = this.getInstance();
      routeInstance.firstEnd = routeInstance.longName.split('/')[0];
      routeInstance.lastEnd = routeInstance.longName.split('/')[1];
      
      this.$el.html(template({
        route : routeInstance,
        stats : this.getRouteStats(routeInstance),
        chargers : this.chargers.toJSON(),
        buses : this.buses.toJSON()
      }));
      
      this.miniMap = new MiniMap({el: this.$('#route-map-view'), route: routeInstance.routeId});
      this.miniMap.render();

      var availableDates = _.map(this.getInstance().stats, function(stat) {
        return stat.date;
      });

      var available = function(date) {
        dmy = date.getFullYear() + '-' + (date.getMonth() + 1) + "-" + date.getDate();
        if ($.inArray(dmy, availableDates) != -1) {
          return [ true, '', 'Available' ];
        } else {
          return [ false, '', 'unAvailable' ];
        }
      }

      this.$('#route-date').datepicker({
        beforeShowDay : available,
        dateFormat : 'yy-mm-dd'
      });
      
      this.$('#route-date').datepicker('setDate', availableDates[0]);
      this.$('.selectpicker').selectpicker();

      this.routeVis = new RouteVisualizationView({
        el : this.$('.route-path-visualization'),
        route : this.getInstance(),
        project : this.project,
        chargers : this.chargers
      });

      this.busDetails = new BusDetailsView({
        el : this.$('.bus-details')
      });

      this.chargerDetails = new ChargerDetailsView({
        el : this.$('.charger-details')
      });

      this.busDetails.render();
      this.routeVis.render();
    }
  });

  return SimulationView;
});