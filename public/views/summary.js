define([ 'jquery', 
         'jquery-ui', 
         'underscore', 
         'backbone',
         'views/summary/simulated-route-list',
         'config-manager',
         'amcharts.serial', 
         'mocks',
         'hbs!templates/results',
         'hbs!templates/misc/loading',
         'bsselect'], 
    function($, JUI, _, Backbone, RouteList, ConfigManager, amRef, Mocks, template, loading) {

  var SummaryView = Backbone.View.extend({

    events : {
    	
    },

    onParamChange : function() {

    },

    initialize : function(options) {
    	var self = this;
    	ConfigManager.getProject().done(function(project){
    		self.render();
    	});
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
    		this.showCost(data.cost);
    	} else if(order === '3') {
    		
    	}
    },
    
    getActiveTab: function() {
    	return this.$('.nav-tabs li.active').attr('data-tab');
    },
    
    showEnergyUse: function(data) {
        
        var chart = amRef.makeChart('energy-chart', {
            'theme' : 'none',
            'type' : 'serial',
            'autoMargins' : false,
            'marginLeft' : 80,
            'marginRight' : 8,
            'marginTop' : 10,
            'marginBottom' : 70,
            'pathToImages' : 'http://www.amcharts.com/lib/3/images/',
            'dataProvider' : data,
            'dataDateFormat': 'HH:NN',
            'valueAxes' : [ {
              'id' : 'v1',
              'axisAlpha' : 0,
              'inside' : false,
              'min' : 0,
              'minimum' : 0,
              'gridAlpha' : 0.1,
              'title' : 'Energy demand (kW)'
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
              'valueField' : 'power'
            }],
            'chartCursor' : {
              'valueLineEnabled' : false,
              'valueLineBalloonEnabled' : false,
              'categoryBalloonDateFormat' : 'DD, MMM'
            },
            'categoryField' : 'time',
            'categoryAxis' : {
              'axisAlpha' : 0,
              'gridAlpha' : 0,
              'minHorizontalGap' : 60,
              'title' : 'Time',
              'parseDates' : false,
              'minPeriod' : 'hh',
              'dateFormats': [{period: 'hh', format: 'HH:NN'}]
            }
          });
    },
    
    showCost: function(data) {
    	console.log(data);
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
            'dataDateFormat': 'HH:NN',
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
              'valueField' : 'power'
            }],
            'chartCursor' : {
              'valueLineEnabled' : false,
              'valueLineBalloonEnabled' : false,
              'categoryBalloonDateFormat' : 'DD, MMM'
            },
            'categoryField' : 'time',
            'categoryAxis' : {
              'axisAlpha' : 0,
              'gridAlpha' : 0,
              'minHorizontalGap' : 60,
              'title' : 'Time',
              'parseDates' : false,
              'minPeriod' : 'hh',
              'dateFormats': [{period: 'hh', format: 'HH:NN'}]
            }
          });
    },

    render : function() {
      var self = this;
      this.$el.html(template());
      this.listView  = new RouteList({el: this.$('.side-list')});
      this.listView.render();
      this.showCost(Mocks.getEndStopData());
      
      this.$('.simulation-date').datepicker({
          dateFormat : 'yy-mm-dd'
      });
      
      this.$('li.tab[data-tab="1"] a').on('shown.bs.tab', function (e) {
    	  self.showCost(Mocks.getEndStopData());
      });
      
      this.$('li.tab[data-tab="2"] a').on('shown.bs.tab', function (e) {
    	  self.showEnergyUse(Mocks.getEndStopData());
      });
      
      this.$('li.tab[data-tab="1"]').addClass('active');
    }
  });

  return SummaryView;
});