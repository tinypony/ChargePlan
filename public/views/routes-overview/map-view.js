define([ 'jquery', 
         'underscore', 
         'backbone', 
         'mapbox',
         'api-config',
         'chroma',
         'hbs!templates/routes-overview/endstop-popup'], 
         function($, _, Backbone, Mapbox, ApiConfig, chroma, endstopPopupTemplate) {
  var MapView = Backbone.View.extend({
    initialize: function() {

    },
    
    getRouteStyle: function(rank, isHighlighted) {
      if(isHighlighted) {
        return {color: 'steelblue', opacity: 1};
      } else {
        return {color: 'lightgrey', opacity: 0.5};
      }
    },
    
    highlightRoute: function(route, isHighlighted) {
      var polyline, style, routeName;
      
      if(_.isString(route)) {
        polyline = this.drawnRoutes[route];
        routeName = route;
      } else {
        polyline = route;
        routeName = route.userData.route.name;
      }
      
      var routeObj = _.find(this.routes, function(r){
        return r.name === routeName;
      });
      
      style = this.getRouteStyle(null, isHighlighted);
      polyline.setStyle(style);
      polyline.bringToFront(); 
    },
    
    unhighlightAllRoutes: function() {
      var self = this;
      _.each(_.keys(this.drawnRoutes), function(routeName){
        self.highlightRoute(routeName, false);
      })
    },
    
    resetMap: function(){
      var self = this;
      _.each(this.endStops, function(stopMarker){
        self.map.removeLayer(stopMarker);
      });
      
      _.each(this.drawnRoutes, function(routeLayer){
        self.map.removeLayer(routeLayer);
      });
      

      this.endStops = {};
      this.drawnRoutes = {};
    },
    
    displayData: function(data) {
      this.resetMap();
      this.routes = data.routes;
      this.stops = data.stops;
      
      var self = this;
      
   //   this.scale = chroma.scale(['darkorange', 'green'])
    //  .domain([rankDomain.min, rankDomain.max], 3);
      
      var createMarker = function(stop) {
        var routesWithEndStop = _.filter(data.routes, function(route){
          return _.first(route.waypoints).stopdId === stop.stopId || _.last(route.waypoints).stopId === stop.stopId;
        });
        
        var marker = L.marker([stop.y, stop.x]);
        marker.bindPopup(endstopPopupTemplate({
          stopname: stop.name,
          routes: routesWithEndStop
        }));
        
        marker.addTo(self.map);
        self.endStops[stop.stopId] = marker;
        
        marker.on('click', function(e) {
          self.map.panTo(e.latlng);
          self.trigger('show:endstop', stop.id);
        });
        
        marker.on('mouseover', function(e) {
          _.each(routesWithEndStop, function(routeFound) {
            self.highlightRoute(routeFound.name, true);
          });          
          
          e.target.openPopup();
        });
        
        marker.on('mouseout', function(e) {
          self.unhighlightAllRoutes();
          e.target.closePopup();
        });
      };
      
      var drawRoute = function( route, fitToMap ) {        
        var coordinates = _.map(route.waypoints, function(waypoint){
          var stop = data.stops[waypoint.stopId];
          return L.latLng(parseFloat(stop.y, 10), parseFloat(stop.x, 10));
        });
        
        //Create polyline
        var polyline = L.polyline( coordinates, self.getRouteStyle(route, false)).addTo(self.map);
        polyline.userData = {
            route: route
        };
        
        polyline.on('mouseover', function(e){
          self.highlightRoute(e.target, true);
        });
        
        polyline.on('mouseout', function(e){
          self.highlightRoute(e.target, false);
        });
        
        self.drawnRoutes[route.name] = polyline;
      }
      
      _.each(data.routes, function(route) {
        var first = _.first(route.waypoints);
        var last = _.last(route.waypoints);
        
        drawRoute(route);
        
        if(_.isUndefined(self.endStops[first.stopId])) {
          var firstStop = _.find(data.stops, function(stop){
            return stop.stopId === first.stopId;
          });
          createMarker(firstStop);
        }
        
        if(_.isUndefined(self.endStops[last.stopId])) {
          var lastStop = _.find(data.stops, function(stop){
            return stop.stopId === last.stopId;
          });
          createMarker(lastStop);
        }
      });
    },
    
    render: function() {
      L.mapbox.accessToken = ApiConfig.tokens.mapbox;
      this.map = L.mapbox.map('map', 'tinypony.l8cdckm5').setView([ 59.914, 10.748 ], 12);
    }
  });
  
  return MapView;
});
