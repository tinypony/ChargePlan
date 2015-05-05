define(['jquery', 
        'backbone', 
        'views/misc/modal-dialog', 
        'amcharts.serial',
        'hbs!templates/simulation/stop-details',
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
        minChargingTime: minTime,
        isEndStop: this.isEndStop
      }));
      
      if(this.stop.charger) {
        this.$('.available-chargers').val(this.stop.charger.type.id);
      }
      
      var dialog = new ModalDialog({
        title: this.stop.name,
        id: 'stop-info',
        buttonText: 'Save changes',
        showClose: true,
        
        clickHandlers: {
          'primary': function(ev) {
            self.project.updateStop({
              stop: self.stop.stopId,
              route: self.routeId,
              chargersToAdd: self.$('.available-chargers').val(),
              minChargingTime: self.$('.min-charging-time').val()
            });
            dialog.close();
          }
        }
      });
      
      dialog.render().content(this.$el);
      this.$('.stop-consumption-chart').html(loading());
      
      $.ajax({
        url: '/api/projects/'+this.project.get('id')+'/stop/consumption/'+this.stop.stopId,
        method: 'GET'
      }).done(function(data) {
    	  
    	  console.log(data);
    	  /*
        self.$('.stop-consumption-chart .loading-container').remove();
        var chart = amRef.makeChart('stop-consumption-chart', {
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
            'title' : 'Energy consumption (kW)'
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
            'valueField' : 'value'
          } ],
          'chartCursor' : {
            'valueLineEnabled' : true,
            'valueLineBalloonEnabled' : true
          },
          'categoryField' : 'key',
          'categoryAxis' : {
            'axisAlpha' : 0,
            'gridAlpha' : 0,
            'maximum' : 110,
            'max' : 110,
            'minHorizontalGap' : 60,
            'title' : 'Time',
            'parseDates' : true,
            'minPeriod' : 'mm'
          }
        });
        */
      });
    }
  });
  
  return StopDetailsView;
});