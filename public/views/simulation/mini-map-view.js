define(['jquery', 
        'underscore', 
        'backbone', 
        'mapbox',
        'api-config'], function($, _, Backbone, Mapbox, ApiConfig){
	'use strict';
	
	var MiniMapView = Backbone.View.extend({
		initialize: function(options){
			this.routeId = options.route;
		},
		
	    drawRoute: function() {
	        var self = this;
	        
	        $.get('/api/routes/'+this.routeId+'/stops').done(function(data) {
	            var stops;
	            var direction0 = data['0'];
	            var direction1 = data['1'];
	            
	            if(direction0) {
	              stops = direction0.stops;
	            } else {
	              stops = direction1.stops;
	            }
	            
	            var coordinates = _.map(stops, function(stop) {
	          	  return L.latLng( parseFloat(stop.y, 10), parseFloat(stop.x, 10) );
	            });
	            
	            //Create polyline
	            var polyline = L.polyline( coordinates, {color: 'steelblue', opacity: 1}).addTo(self.map);
	            self.map.fitBounds(polyline.getBounds());
	        });

	    },
	    
		render: function(){
			var self = this;
	        L.mapbox.accessToken = ApiConfig.tokens.mapbox;
	        this.map = L.mapbox.map('route-map-view', 'tinypony.l8cdckm5',  { zoomControl: false }).setView([ 59.914, 10.748 ], 12);
	        this.drawRoute();
		}
	});
	
	return MiniMapView;
});