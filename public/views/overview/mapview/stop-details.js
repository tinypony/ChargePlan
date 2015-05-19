define(['jquery', 
        'backbone', 
        'moment',
        'views/misc/modal-dialog', 
        'amcharts.serial',
        'hbs!templates/overview/stop-details',
        'hbs!templates/overview/charging-time',
        'hbs!templates/misc/loading'], 
    function($, Backbone, moment, ModalDialog, amRef, template, times, loading) {
  
  var StopDetailsView = Backbone.View.extend({
    
    initialize: function(opts) {
      this.chargersToAdd;
      this.chargers = opts.chargers;
      this.stop = opts.stop;
      this.project = opts.project;
      this.routeId = opts.routeId;
      this.isEndStop = opts.isEndStop;
    },
    
    render: function() {
      var self = this;
      var minTime;
      
      if(this.stop.chargingTimes) {
        minTime = this.stop.chargingTimes[this.routeId];
      } else if(this.isEndStop) {
        minTime = 600;
      } else {
        minTime = 10;
      }
      
      this.$el.html(template({
        stop: this.stop,
        chargerTypes: this.chargers.toJSON(),
        isEndStop: this.isEndStop
      }));
      
      this.$('.available-chargers').selectpicker();
      
      if(this.stop.charger) {
        this.$('.available-chargers').val(this.stop.charger.type.id);
        this.$('.available-chargers').selectpicker('refresh');
      }
      
      var dialog = new ModalDialog({
        title: this.stop.name,
        id: 'stop-info-map',
        buttonText: 'Save changes',
        showClose: true,
        
        clickHandlers: {
          primary: function(ev) {
            self.project.updateStop({
              stop: self.stop.stopId,
              route: self.routeId,
              chargersToAdd: self.$('.available-chargers').val(),
              minChargingTime: self.getChargingTimes()
            });
            
            dialog.close();
          }
        },
        
        onShow: function() {
            self.$('.stop-consumption-chart').html(loading());
            self.getConsumptionInfo();
        }
      });
      
      dialog.render().content(this.$el);
    },
    
    getChargingTimes: function() {
    	chargingTimes = {};
    	
    	this.$('.charging-time-input').each(function(){
    		var $this = $(this);
    		var routeId = $this.attr('data-routeid');
    		var time = Number.parseInt($this.val(), 10);
    		chargingTimes[routeId] = time;
    	});
    	
    	return chargingTimes;
    },
    
    getConsumptionInfo: function() {
    	var self = this;
    	
    	$.ajax({
            url: '/api/projects/'+this.project.get('id')+'/stop/'+this.stop.stopId,
            method: 'GET'
          }).done(function(data) {
        	  var transformed = self.transformData(data.consumption);
        	  var peakDemand = self.getPeakDemand(transformed);
        	  var graphs = self.getGraphs(transformed);
        	  self.$('.peak-demand-label span').text(Math.ceil(peakDemand) + ' kW');
        	  
        	  var chart = AmCharts.makeChart('stop-consumption-chart', {
        		    type: "serial",
        			theme: "light",
        			sequencedAnimation: true,
        			startEffect: '>',
        			startDuration: 0.3,
        			dataDateFormat: 'HH',
        		    legend: {
        		        horizontalGap: 10,
        		        maxColumns: 1,
        		        position: 'bottom',
        				useGraphSettings: true,
        				markerSize: 10,
        				maxColumns: 3
        		    },
        		    "dataProvider": transformed,
        		    "valueAxes": [{
        		        "stackType": "regular",
        		        "axisAlpha": 0.3,
        		        "gridAlpha": 0,
        		        min: 0,
        		        title: 'Power demand (kW)'
        		    }],
        		    "graphs": graphs,
        		    "categoryField": "hour",
        		    "categoryAxis": {
        		        gridPosition: 'start',
        		        axisAlpha: 0,
        		        gridAlpha: 0,
        		        position: 'left'
        	
        		    },
        		    "export": {
        		    	"enabled": true,
        		        "libs": {
        		         	"path": "http://www.amcharts.com/lib/3/plugins/export/libs/"
        		      	}
        		     }
        		});
        	  
        	  var chargingTimes = _.map(data.chargingTimes, function(time, routeId){
        		  return {
        			  time: time,
        			  routeId: routeId
        		  };
        	  });
        	  
        	  self.$('.charging-time-list').append(times({chargingTimes: chargingTimes}));
          });
    },
    
    transformData: function(data) {

    	return _.map(data, function(val, hour){
    		var retVal = {
    			hour: moment().set('hour', hour).format('H:00'),
    			total: 0
    		};
    		
    		_.each(val, function(item){
    			retVal[item.routeId] = item.avgPower;
    			retVal.total += item.avgPower;
    		});
    		
    		return retVal;
    	});
    },
    
    getPeakDemand: function(transformedData) {
    	return _.max(transformedData, function(item){
    		return item.total;
    	}).total;
    },
    
    getGraphs: function(transformedData) {
    	var routesFound = [];
    	_.each(transformedData, function(it){
    		var keys = _.keys(it);
    		keys = _.without(keys, 'hour', 'total');
    		routesFound = routesFound.concat(keys);
    	});
    	routesFound = _.uniq(routesFound);
    	var graphs = _.map(routesFound, function(r){
    		return {
		        "balloonText": "<b>[[title]]</b><br><span style='font-size:14px'>[[category]]: <b>[[value]]</b></span>",
		        "fillAlphas": 0.8,
		        "labelText": "[[value]]",
		        "lineAlpha": 0.3,
		        "title": r,
		        "type": "column",
				"color": "#000000",
		        "valueField": r
		    };
    	});
    	
    	return graphs;
    }
  });
  
  return StopDetailsView;
});