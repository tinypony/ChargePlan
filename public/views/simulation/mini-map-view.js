define(['jquery', 
        'underscore', 
        'backbone', 
        'config-manager',
        'mapbox',
        'api-config',
        'views/simulation/stop-details',
        'collections/chargers',
        'collections/transformers'], function($, _, Backbone, ConfigManager, Mapbox, ApiConfig, StopDetails, Chargers, Transformers) {
	'use strict';
	
	var endStopIcon = {
	        iconUrl: "/assets/img/map_pointer_1.png",
	        iconSize: [32, 51],
	        iconAnchor: [16, 51],
	        popupAnchor: [0, -51],
	        className: 'dot'
		};
		
		var endStopChargerIcon = {
		        iconUrl: "/assets/img/map_pointer_2.png",
		        iconSize: [32, 51],
		        iconAnchor: [16, 51],
		        popupAnchor: [0, -51],
		        className: 'dot'	
		};
		
		var stopIcon = {
		        iconUrl: "/assets/img/reddot.png",
		        iconSize: [20, 20],
		        iconAnchor: [15, 15],
		        popupAnchor: [0, -20],
		        className: 'dot'	
		};
		
	var MiniMapView = Backbone.View.extend({
		initialize: function(options){
			this.routeId = options.route;
		},
		
	    drawRoute: function() {
	        var self = this;
	        this.drawnStops = {};
	        
	        $.get('/api/routes/'+this.routeId+'/stops').done(function(data) {
	            var stops;
	            var direction0 = data['0'];
	            var direction1 = data['1'];


	            if(direction0) {
		            var coordinates = _.map(direction0.stops, function(stop) {
		          	  return L.latLng( parseFloat(stop.y, 10), parseFloat(stop.x, 10) );
		            });
		            
		            //Create polyline
		            var polyline = L.polyline( coordinates, {color: 'steelblue', opacity: 0.6}).addTo(self.map);
		            self.map.fitBounds(polyline.getBounds());
		            
		            _.each(direction0.stops, function(stop, arr, idx){
		            	self.drawBusStop(stop, idx===0 || idx === arr.length-1);
		            });
	            }
	            
	            if(direction1) {
		            var coordinates = _.map(direction1.stops, function(stop) {
		          	  return L.latLng( parseFloat(stop.y, 10), parseFloat(stop.x, 10) );
		            });
		            
		            //Create polyline
		            var polyline = L.polyline( coordinates, {color: 'steelblue', opacity: 0.6}).addTo(self.map);
		            self.map.fitBounds(polyline.getBounds());
		            
		            _.each(direction1.stops, function(stop, idx, arr){
		            	self.drawBusStop(stop, idx===0 || idx === arr.length-1 );
		            });
	            }
	            
	        });

	    },
	    
	    drawBusStop: function(stop, isLastStop) {
	        var self = this;
	        var marker = L.marker([stop.y, stop.x]);
	        var elStop = _.findWhere(this.project.get('stops'), {stopId: stop.stopId});
	        
	        if(isLastStop) {
		        if(elStop && elStop.charger) {
		            marker.setIcon(L.icon(endStopChargerIcon));  
		        } else {
		      	  marker.setIcon(L.icon(endStopIcon));  
		        }
	        } else {
	        	marker.setIcon(L.icon(stopIcon));  
	        	marker.setOpacity(0);
	        }
	        
	        marker.addTo(self.map);
	        
	        marker.on('click', function(e) {
	          self.map.panTo(e.latlng);
	          self.onStopClick(stop, isLastStop);
	        });
//	        
//	        marker.on('mouseover', function(e) {
//	          _.each(routesWithEndStop, function(routeFound) {
//	            self.highlightRoute(routeFound.name, true);
//	          });          
//	          
//	          e.target.openPopup();
//	        });
//	        
//	        marker.on('mouseout', function(e) {
//	          self.unhighlightAllRoutes();
//	          e.target.closePopup();
//	        });
	        marker.userData = {
	        	stop: stop,
	        	isLast: isLastStop
	        };

	        this.drawnStops[stop.stopId] = marker;
	    },
	    
	    drawTransformers: function() {
	    	var self = this;
	    	this.transformers.each(function(trans){
	        	var marker = L.marker([trans.get('lat'), trans.get('lon')]);
	        	marker.addTo(self.map);
	        });
	    },
	    
	    onStopClick : function(stop, isEndStop) {
	        var self = this;
	        var electrifiedStop = _.findWhere(this.project.get('stops'), {stopId: stop.stopId});
	        
	        if(!electrifiedStop) {
	          electrifiedStop = stop;
	          electrifiedStop.chargers = [];
	        }
	        
	        var chargers = new Chargers();
	        
	        
	        var ready = function(){
		        var stopDetails = new StopDetails({
		          stop: electrifiedStop,
		          chargers: chargers,
		          project: self.project,
		          routeId: self.routeId,
		          isEndStop: isEndStop
		        });
		        
		        stopDetails.render();
		        
		        self.listenTo(stopDetails, 'close', function(){
		          stopDetails.remove();
		          self.stopListening(stopDetails);
		        });
	        };
	        
	        chargers.fetch().done(ready);

	      },
	    
		render: function() {
			var self = this;
	        L.mapbox.accessToken = ApiConfig.tokens.mapbox;
	        this.map = L.mapbox.map('route-map-view', 'tinypony.l8cdckm5',  { zoomControl: false }).setView([ 59.914, 10.748 ], 12);
	        
	        this.map.on('zoomend', function(){
	        	if(self.map.getZoom() >= 15) {
	        		_.each(self.drawnStops, function(marker, stopId){
	        			if(!marker.userData.isLast) {
	        				marker.setOpacity(1);
	        			}
	        		});
	        	} else if(self.map.getZoom() <= 14) {
	        		_.each(self.drawnStops, function(marker, stopId){
	        			if(!marker.userData.isLast) {
	        				marker.setOpacity(0);
	        			}
	        		});
	        	}
	        });
	        
	        ConfigManager.getProject().done(function(proj) {
	        	self.project = proj;
	        	self.transformers = new Transformers();
		        self.transformers.fetch().done(function(){
		        	self.drawRoute();
		        //	self.drawTransformers();
		        });
	        });
		}
	});
	
	return MiniMapView;
});