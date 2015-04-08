define(['amcharts.serial','backbone'], function(amRef, Backbone){
	
	var ChargingStationDetailsView = Backbone.View.extend({
		initialize: function(options) {
			this.data = options.data;
		},
		
		render: function() {
			var chart = amRef.makeChart('power-chart', {
		        'theme' : 'none',
		        'type' : 'serial',
		        'autoMargins' : false,
		        'marginLeft' : 70,
		        'marginRight' : 8,
		        'marginTop' : 50,
		        'marginBottom' : 60,
		        'pathToImages' : 'http://www.amcharts.com/lib/3/images/',
		        'dataProvider' : this.data,
		        'valueAxes' : [ {
		          'id' : 'v1',
		          'axisAlpha' : 0,
		          'inside' : false,
		          'min' : 0,
		          'minimum' : 0,
		          'max' : 600,
		          'maximum' : 600,
		          'gridAlpha' : 0.1,
		          'title' : 'Power use (kW)'
		        } ],
		        'graphs' : [ {
		          'useNegativeColorIfDown' : false,
		          'balloonText' : '[[category]]<br><b>value: [[value]]</b>',
		          'bullet' : 'round',
		          'bulletBorderAlpha' : 1,
		          'bulletBorderColor' : '#FFFFFF',
		          'hideBulletsCount' : 50,
		          'lineThickness' : 1,
		          'lineColor' : '#0088cc',
		          'valueField' : 'power'
		        } ],
		        'chartCursor' : {
		          'valueLineEnabled' : true,
		          'valueLineBalloonEnabled' : true
		        },
		        'categoryField' : 'time',
		        'categoryAxis' : {
		          'parseDates' : false,
		          'axisAlpha' : 0,
		          'gridAlpha' : 0,
		          'title' : 'Time'
		        }
		      });
		      
		      this.$('.high-consumption').text(Math.round(_.max(this.data, function(datapoint){
		        return datapoint.power;
		      }).power) + ' kW');
		}
	});
	
	return ChargingStationDetailsView;
	
});