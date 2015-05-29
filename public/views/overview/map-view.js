define([ 'jquery', 
         'underscore', 
         'backbone', 
         'mapbox',
         'event-bus',
         'api-config',
         'chroma',
         'const',
         'views/overview/mapview/stop-details',
         'collections/chargers',
         'hbs!templates/overview/endstop-popup'], 
         function($, _, Backbone, Mapbox, EventBus, ApiConfig, chroma, 
        		 Constants, StopDetails, ChargersCollection, endstopPopupTemplate) {
	
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
  
  var MapView = Backbone.View.extend({
	  
    initialize: function() {
      this.drawnRoutes = {};
      this.drawnStops = {};
      
      this.listenTo(EventBus, 'route:draw', this.drawRoute);
      this.listenTo(EventBus, 'route:highlight', this.highlightRoute);
      this.listenTo(EventBus, 'clear:routes', this.resetMap);
    },
    
    setData: function(data) {
      var self = this;
      this.data = {};
      this.data.stops = data.stops;
      this.data.routes = data.routes;
      this.data.project = data.project;
      
//      this.listenTo(this.data.project, 'sync', function(){
//    	  self.resetMap();
//    	  self.displayRoutes(self.data.project.get('routes'));
//      });
    },
    
    getRouteWaypoints: function(routeBag) {
      var self = this;
      
      return _.map(route.waypoints, function(waypoint){
        var stop =  self.data.stops[waypoint.stopId];
        return L.latLng(parseFloat(stop.y, 10), parseFloat(stop.x, 10));
      });
    },
    
    drawRoute: function(route) {
      var routeId = route.routeId;
      var self = this;
      
      if(this.drawnRoutes[routeId]) {
    	return;
      }
      
      $.get('/api/routes/'+routeId+'/stops').done(function(data) {
          if(self.drawnRoutes[routeId]) {
        	return;
          }
          
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
          var polyline = L.polyline( coordinates, self.getRouteStyle(route, false)).addTo(self.map);
          polyline.userData = {
              route: route
          };
          
          self.drawnRoutes[routeId] = polyline;
          if(direction0) {
        	  self.drawBusStop(_.first(direction0.stops), true, routeId);
        	  self.drawBusStop(_.last(direction0.stops), true, routeId);
          }
          
          if(direction1) {
        	  self.drawBusStop(_.first(direction1.stops), true, routeId);
        	  self.drawBusStop(_.last(direction1.stops), true, routeId);
          }
      });

    },
    
    clearRoute: function(route) {
    	var self = this;
    	var pl = this.drawnRoutes[route];
    	if(!pl) {
    		return;
    	}
    	

        this.map.removeLayer(pl);
        this.drawnRoutes = _.omit(this.drawnRoutes, route);
        
        _.each(this.drawnStops, function(stop) {
        	stop.userData.routes = _.without(stop.userData.routes, route);
        	if(stop.userData.routes.length === 0) {
        		self.clearStop(stop.userData.stop.stopId);
        	}
        });
    },
    
    clearStop: function(stopId) {
    	var marker = this.drawnStops[stopId];
    	this.map.removeLayer(marker);
    	this.drawnStops = _.omit(this.drawnStops, stopId);
    },
    
    drawBusStop: function(stop, isEndstop, routeId) {
      var self = this;
      
      if(this.drawnStops[stop.stopId]) {
    	  this.drawnStops[stop.stopId].userData.routes.push(routeId);
    	  return;
      }
    	  
      var marker = L.marker([stop.y, stop.x]);
      var elStop = _.findWhere(this.data.project.get('stops'), {stopId: stop.stopId});
      
      if(elStop && elStop.charger) {
          marker.setIcon(L.icon(endStopChargerIcon));  
      } else {
    	  marker.setIcon(L.icon(endStopIcon));  
      }
//      marker.bindPopup(endstopPopupTemplate({
//        stopname: stop.name+"("+stop.stopId+")",
//        routes: _.uniq(routesWithEndStop, false, function(route){
//          return route.name;
//        })
//      }));
      
      marker.addTo(self.map);
      self.drawnStops[stop.stopId] = marker;
      
      marker.on('click', function(e) {
        self.map.panTo(e.latlng);
        
        self.onStopClick(stop);
      });
//      
//      marker.on('mouseover', function(e) {
//        _.each(routesWithEndStop, function(routeFound) {
//          self.highlightRoute(routeFound.name, true);
//        });          
//        
//        e.target.openPopup();
//      });
//      
//      marker.on('mouseout', function(e) {
//        self.unhighlightAllRoutes();
//        e.target.closePopup();
//      });
      marker.userData = {
    	stop: stop,
    	routes: [routeId]
      };
    },
    
    onStopClick : function(scheduleStop) {
        var self = this;
        var electrifiedStop = _.findWhere(this.data.project.get('stops'), {'stopId': scheduleStop.stopId});
        
        if(!electrifiedStop) {
          electrifiedStop = scheduleStop;
          electrifiedStop.charger = null;
        }
        
        var chargers = new ChargersCollection();
        chargers.fetch().done(function(collection){
            var stopDetails = new StopDetails({
                stop: electrifiedStop,
                chargers: chargers,
                project: self.data.project
              });
              
              stopDetails.render();
              self.listenTo(stopDetails, 'close', function(){
                stopDetails.remove();
                self.stopListening(stopDetails);
              });
        });
    },
    
    getRouteStyle: function(route, isHighlighted) {
      var style = {
          color: 'grey',
          opacity: 0.5
      }
      
      if(route.state === Constants.RouteState.UNSIMULATED) {
        style.color = 'grey';
      } else if(route.state === Constants.RouteState.SIMULATED_OK) {
        style.color = 'green';
      } else if(route.state === Constants.RouteState.SIMULATED_FAIL) {
        style.color = 'red';
      } else if(route.state === Constants.RouteState.RESIMULATE ) {
        style.color = 'orange';
      }
      
      if(isHighlighted) {
        style.opacity = 1;
      } else {
        style.opacity = 0.5;
      }
      
      return style;
    },
    
    pulsateRoute: function(route) {
    	
    },
    
    highlightRoute: function(route, isHighlighted) {
      var polyline = this.getPolyline(route);
      
      if(_.isUndefined(polyline)) {
        return;
      }
      
      style = this.getRouteStyle(polyline.userData.route, isHighlighted);
      polyline.setStyle(style);
      polyline.bringToFront(); 
    },
    
    getPolyline: function(route) {
      var polyline, style, routeName;
      
      if(_.isString(route)) {
        polyline = this.drawnRoutes[route];
        routeName = route;
      } else {
        polyline = route;
        routeName = route.userData.name;
      }
      
      return polyline;
    },
    
    unhighlightAllRoutes: function() {
      var self = this;
      _.each(_.keys(this.drawnRoutes), function(routeName){
        self.highlightRoute(routeName, false);
      })
    },
    
    resetMap: function(){
      var self = this;
      _.each(this.drawnStops, function(stopMarker){
        self.map.removeLayer(stopMarker);
      });
      
      _.each(this.drawnRoutes, function(routeLayer, routeId){
    	self.clearRoute(routeId);
      });
      

      this.drawnStops = {};
      this.drawnRoutes = {};
    },
    
    render: function() {
    	this.$el.addClass('map-container-boom');
    	this.$el.append('<div id="map" class="map-view"></div>');
    },
    
    displayMap: function() {
    	var self = this;
        L.mapbox.accessToken = ApiConfig.tokens.mapbox;
        this.map = L.mapbox.map('map', 'tinypony.l8cdckm5',  { zoomControl: false }).setView([ 59.914, 10.748 ], 12);
        new L.Control.Zoom({ position: 'topright' }).addTo(this.map);
        
        this.draw();
    },
    
    getRoutes: function() {
    	console.log(this.data.routes);
    },
    
    draw: function() {
    	this.displayRoutes(this.data.project.get('routes'));
    },
    
    displayRoutes: function(routes) {
    	var self = this;
    	_.each(routes, function(route) {
        	self.drawRoute(route);
        });
    }
  });
  
  return MapView;
});
