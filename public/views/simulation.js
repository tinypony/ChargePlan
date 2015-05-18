define([ 'jquery', 
         'jquery-ui', 
         'underscore', 
         'backbone', 
         'const',
         'moment',
         'config-manager', 
         'amcharts.serial', 
         'views/simulation/bus-details', 
         'views/simulation/mini-map-view',
         'collections/chargers', 
         'collections/buses', 
         'hbs!templates/simulation',
         'hbs!templates/misc/loading',
         'bsselect'], 
    function($, JUI, _, Backbone, Const, moment, ConfigManager, amRef, BusDetailsView, MiniMap, Chargers, Buses, template, loading) {

  var SimulationView = Backbone.View.extend({

    events : {
      'change .bus-select' : 'onBusSelect',
      'change #route-date' : 'onParamChange',
      'click li.tab': 'onTabChange'
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

    onBusSelect : function(ev, isSilent) {
      var selectedbusid = this.$('.bus-select').val();
      var busModel = this.buses.get(selectedbusid);
      this.busDetails.showModel(busModel);
      
      if(!isSilent) {
    	  this.onParamChange();
      }
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
      }).done(function(data) {
    	console.log(data);
        self.data = data;
    	self.$('.simulation-results .loading-container').remove();
        self.showData(data);
      })
    },
    
    onTabChange: function() {
    	if(this.data) this.showData(this.data);
    },
    
    showData: function(data, dataTab) {
    	var order;
    	if(!dataTab) {
    		order = this.getActiveTab();
    	} else {
    		order = dataTab;
    	}
    	
    	if(order === '1') {
    		this.showFeasibility(data.feasibility);
    		
    	} else if(order === '2') {
    		var cost = _.sortBy(data.cost, function(item){
    			return moment(item.date, 'YYYY-M-D').unix();
    		});
    		this.showCost(cost);
    	} else if(order === '3') {
    		var cost = _.sortBy(data.cost, function(item){
    			return moment(item.date, 'YYYY-M-D').unix();
    		});
    		
    		var emis = _.map(cost, function(entr){
    			return {
    				date: entr.date,
    				CO: entr.emissions.CO,
    				CO2: entr.emissions.CO2,
    				NOx: entr.emissions.NOx,
    				total: entr.emissions.CO + entr.emissions.CO2 + entr.emissions.NOx
    			};
    		});
    		this.showEmissions(emis);
    	}
    },
    
    getActiveTab: function() {
    	return this.$('.nav-tabs li.active').attr('data-tab');
    },
    
    showFeasibility: function(data) {
    	if(!data) {
    		return;
    	}
        if(data.survived) {
        	this.project.getRoute(this.route.routeId).state = Const.RouteState.SIMULATED_OK;
        } else {
        	this.project.getRoute(this.route.routeId).state = Const.RouteState.SIMULATED_FAIL;
        }
        
        var chart = amRef.makeChart('simulation-result-chart', {
          'theme' : 'none',
          'type' : 'serial',
          'autoMargins' : false,
          'marginLeft' : 60,
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
            'maximum' : 100,
            'gridAlpha' : 0.1,
            'title' : 'Battery level (%)'
          } ],
          'graphs' : [ {
            'useNegativeColorIfDown' : false,
            'balloonText' : 'Time: [[category]]<br><b>Battery: [[value]]</b>',
            'bullet' : 'round',
            'bulletBorderAlpha' : 1,
            'bulletBorderColor' : '#FFFFFF',
            'hideBulletsCount' : 50,
            'lineThickness' : 2,
            'lineColor' : data.survived ? '#0088cc' : '#ef1a2d',
            'valueField' : 'charge',
            fillAlphas: 0.5
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
    	
    	var days = data.length;
    	var totalElectricity = _.reduce(data, function(memo, item){
    		return memo + item.energyPrice;
    	}, 0);
    	
    	var totalDiesel = _.reduce(data, function(memo, item){
    		return memo + item.dieselPrice;
    	}, 0);
    	
    	this.$('.route-summary').removeClass('hidden');
    	this.$('label.summary-value.days').text(days);
    	this.$('label.summary-value.diesel').text( Math.floor(totalDiesel) + ' NOK');
    	this.$('label.summary-value.electricity').text(Math.floor(totalElectricity) + ' NOK');
    	
        var chart = amRef.makeChart('cost-chart', {
            'theme' : 'none',
            'type' : 'serial',
            'autoMargins' : false,
            'marginLeft' : 80,
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
            'legend': {
            	data: [{title: 'Electricity price', color: '#0088cc'}, {title: 'Diesel equivalent price', color: "#777"}]
            },
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
                'lineColor' : '#777',
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
    
    showEmissions: function(data) {
    	
    	var totalCO2 = _.reduce(data, function(memo, item){
    		return memo + item.CO2;
    	}, 0);
    	
    	var totalCO = _.reduce(data, function(memo, item){
    		return memo + item.CO;
    	}, 0);
    	
    	var totalNOx = _.reduce(data, function(memo, item){
    		return memo + item.NOx;
    	}, 0);
    	
    	this.$('.route-summary').removeClass('hidden');
    	this.$('label.summary-value.co2').text(Math.floor(totalCO2) + ' kg');
    	this.$('label.summary-value.co').text( Math.floor(totalCO) + ' kg');
    	this.$('label.summary-value.nox').text(Math.floor(totalNOx) + ' kg');
    	
    	var chart = amRef.makeChart('emissions-chart', {
		    type: 'serial',
			theme: 'light',
			sequencedAnimation: true,
		    dataProvider: data,
		    valueAxes: [{
		        stackType: 'regular',
		        axisAlpha: 0.3,
		        gridAlpha: 0,
		        min: 0,
		        gridAlpha : 0.1,
		        title: 'Emissions (Kg)'
		    }],
		    graphs: [{
              useNegativeColorIfDown : false,
              balloonText : '[[category]]<br><b>Total emissions cut: [[value]]</b>',
              bullet : 'round',
              bulletBorderAlpha : 1,
              bulletBorderColor : '#FFFFFF',
              hideBulletsCount : 50,
              lineThickness : 2,
              lineColor : '#0088cc',
		      valueField: 'total'
		    }],
		    chartCursor: {
              'valueLineEnabled' : false,
              'valueLineBalloonEnabled' : false,
              'categoryBalloonDateFormat' : 'DD MMMM'
            },
		    categoryField: 'date',
		    categoryAxis: {
		        gridPosition: 'start',
		        axisAlpha: 0,
		        gridAlpha: 0,
		        position: 'left',
		        minPeriod: 'DD',
		        title: 'Date',
		        parseDates: true
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
      
      this.busDetails = new BusDetailsView({
    	  el: this.$('.bus-details')
	  });
	
	  this.busDetails.render();

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
      
      if(routeInstance.latestSimulation) {
    	  this.$('.selectpicker').selectpicker('val', routeInstance.latestSimulation.type.id);
    	  this.onBusSelect(null, true);
    	  this.data = routeInstance.latestSimulation;
    	  if(this.data.simulationDate) {
    		  this.$('#route-date').datepicker('setDate', new Date(this.data.simulationDate));
  		  }
    	  this.showData(this.data, '1');
      }
      
      this.$('li.tab[data-tab="1"] a').on('shown.bs.tab', function (e) {
    	  self.showData(self.data, '1');
      });
      
      this.$('li.tab[data-tab="2"] a').on('shown.bs.tab', function (e) {
    	  self.showData(self.data, '2');
      });
      
      this.$('li.tab[data-tab="3"] a').on('shown.bs.tab', function (e) {
    	  self.showData(self.data, '3');
      });
      
      this.$('li.tab[data-tab="1"]').addClass('active');
    }
  });

  return SimulationView;
});