define(['jquery', 
        'backbone', 
        'views/misc/modal-dialog', 
        'amcharts.serial',
        'hbs!templates/overview/stop-details',
        'hbs!templates/misc/loading'], 
    function($, Backbone, ModalDialog, amRef, template, loading) {
  
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
      
      if(this.isEndStop) {
          this.$('.charging-time-slider').slider({
    		min: 1,
    		max: 60,
    		step: 1,
    		value: minTime ? minTime/60 : 10,
    		slide: function(event, ui) {
    			self.$('.charging-time-label').text(ui.value + ' minutes');
    		},
      		stop: function(event, ui) {
                self.project.updateStop({
                  stop: self.stop.stopId,
                  route: self.routeId,
                  chargersToAdd: self.$('.available-chargers').val(),
                  minChargingTime: self.getChargingTime()
                }).done(function(){
              	  self.getConsumptionInfo();
                });
        	}
          });
          
          self.$('.charging-time-label').text((minTime ? minTime/60 : 10) + ' minutes');
          
      } else {
          this.$('.charging-time-slider').slider({
      		min: 1,
      		max: 60,
      		step: 1,
      		disabled: true,
      		value: minTime ? minTime : 10,
      		slide: function(event, ui) {
      			self.$('.charging-time-label').text(ui.value + ' seconds');
      		},
      		stop: function(event, ui) {
              self.project.updateStop({
                stop: self.stop.stopId,
                route: self.routeId,
                chargersToAdd: self.$('.available-chargers').val(),
                minChargingTime: self.getChargingTime()
              }).done(function(){
            	  self.getConsumptionInfo();
              });
      		}
          });
          
          self.$('.charging-time-label').text(minTime ? minTime : 10 + ' seconds');
      }
      
      if(this.stop.charger) {
        this.$('.available-chargers').val(this.stop.charger.type.id);
      }
      
      
      var dialog = new ModalDialog({
        title: this.stop.name,
        id: 'stop-info-map',
        buttonText: 'Save changes',
        showClose: true,
        
        clickHandlers: {
          'primary': function(ev) {
            self.project.updateStop({
              stop: self.stop.stopId,
              route: self.routeId,
              chargersToAdd: self.$('.available-chargers').val(),
              minChargingTime: self.getChargingTime()
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
    
    getChargingTime: function() {
    	if(this.isEndStop) {
    		return this.$('.charging-time-slider').slider('option', 'value') * 60
    	} else {
    		return this.$('.charging-time-slider').slider('option', 'value');
    	}
    },
    
    getConsumptionInfo: function() {
    	var self = this;
    	$.ajax({
            url: '/api/projects/'+this.project.get('id')+'/stop/consumption/'+this.stop.stopId,
            method: 'GET'
          }).done(function(data) {
        	  var transformed = self.transformData(data);
        	  var graphs = self.getGraphs(transformed);
        	  
        	  var chart = AmCharts.makeChart('stop-consumption-chart', {
        		    type: "serial",
        			theme: "light",
        			sequencedAnimation: true,
        			startEffect: '>',
        			startDuration: 0.3,
        		    legend: {
        		        horizontalGap: 10,
        		        maxColumns: 1,
        		        position: 'bottom',
        				useGraphSettings: true,
        				markerSize: 10
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
        		        "gridPosition": "start",
        		        "axisAlpha": 0,
        		        "gridAlpha": 0,
        		        "position": "left"
        		    },
        		    "export": {
        		    	"enabled": true,
        		        "libs": {
        		         	"path": "http://www.amcharts.com/lib/3/plugins/export/libs/"
        		      	}
        		     }
        		});
          });
    },
    
    transformData: function(data) {
    	return _.map(data, function(val, hour){
    		var retVal = {
    			hour: hour
    		};
    		
    		_.each(val, function(item){
    			retVal[item.routeId] = item.avgPower;
    		});
    		
    		return retVal;
    	});
    },
    
    getGraphs: function(transformedData) {
    	var routesFound = [];
    	_.each(transformedData, function(it){
    		var keys = _.keys(it);
    		keys = _.without(keys, 'hour');
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