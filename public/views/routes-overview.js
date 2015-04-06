define([ 'jquery', 
         'underscore', 
         'backbone', 
         'mapbox', 
         'mocks',
         'scroller',
         'api-config', 
         'bootstrap',
         'sidebar',
         'views/routes-overview/endstop-details',
         'views/routes-overview/map-view',
         'hbs!templates/routes-overview',
         'hbs!templates/routes-overview/route-list'], 
         function($, _, Backbone, Mapbox, Mocks, scroller,
             ApiConfig, bootstrap, sidebar, EndStopDetails, MapView, template, routeListTemplate) {

  
  var RouteList = Backbone.View.extend({
    
    events: {
      'mouseover .accordion-toggle': 'onMouseover',
      'mouseout .accordion-toggle': 'onMouseout'
    },
    
    initialize: function(options){
      var bin = {};
      /* map to 
       * {
       * 	string: {
       * 		name: string
       * 		routes: [{
       * 			route,
       * 			route
       * 		}]
       * 	},
       * 	...
       */ 
      _.each(options.data, function(route){
    	 route.avg = {};
    	 
    	 var runningAverageStat = function(statName, statName2) {
    		 return function(memo, stat, n) {
    			 if(_.isUndefined(statName2)) {
    				 return (stat[statName] + n * memo) / (n+1);
    			 } else {
    				 return (stat[statName][statName2] + n * memo) / (n+1);
    			 }
    		 };
    	 };
    	 
    	 route.avg.distance = _.reduce(route.stats, runningAverageStat('totalDistance'), 0);
    	 route.avg.departures = Math.round(_.reduce(route.stats, runningAverageStat('departures'), 0));
    	 route.avg.CO2 = _.reduce(route.stats, runningAverageStat('emissions', 'CO2'), 0);
    	 route.avg.CO = _.reduce(route.stats, runningAverageStat('emissions', 'CO'), 0);
    	 route.avg.NOx = _.reduce(route.stats, runningAverageStat('emissions', 'NOx'), 0);
    	 
    	 if(bin[route.name]) {
    		 bin[route.name].routes.push(route);
    	 } else {
    		 bin[route.name] = {name: route.name, routes: [route]};
    	 }
      });
      
      this.data = bin;
    },
    
    onMouseout: function(ev) {
      $target = $(ev.currentTarget);
      var routeName = $target.attr('data-routename');
      this.trigger('route:highlight', routeName, false);
    },

    onMouseover: function(ev) {
      $target = $(ev.currentTarget);
      var routeName = $target.attr('data-routename');
      this.trigger('route:highlight', routeName, true);
    },
    
    render: function() {
      this.$el.html(routeListTemplate({
        routes: _.values(this.data)     
      }));
      
      var self = this;

      
      this.$('#root-accordion').collapse();
      _.each(_.keys(this.data), function(route){
    	  console.log('#accordion-'+route);
    	  self.$('#accordion-'+route).collapse();
      });
      
      _.defer(function() {
        self.$('.nano').nanoScroller({flash: true});
        
        var toggleHandler = function(ev) {
	        var $tar = $(ev.currentTarget);
	        $tar.toggleClass('toggle-on');
	        ev.stopPropagation();
        };
          
        self.$('.accordion-group').on('show.bs.collapse', toggleHandler);
        self.$('.accordion-group').on('hide.bs.collapse', toggleHandler);
      });
      
      return this;
    }
  });
  
  var RoutesOverview = Backbone.View.extend({
    defaultDate: '2015-2-18',
    
    initialize : function(optimize) {
      _.bindAll(this, ['displayData']);
      this.date = this.defaultDate;
      this.retrieveData(true);
    },
    
    retrieveData: function( isFirst) {
      var self = this;
      this.data = {};
      
      var onReady = _.after(2, function(){
        if(isFirst) {
          self.render();
        } else {
          self.displayData();
        }
      });
      
      $.get('/api/routes').done(function(data){
        self.data.routes = data;
        onReady();
      });
      
      $.get('/api/stops').done(function(data){
        self.data.stops = data;
        onReady();
      });
    },
    
    getDate: function() {
      return this.date;
    },

    
    getStat: function(route, date) {
      var routeObj;
      var routes = this.routeData.routes;
      
      if(_.isString(route)) {
        routeObj = _.find(routes, function(item){
          return item.name === route;
        });
      } else {
        routeObj = route;
      }
      
      return routeObj.dayStats;
    },
    
    displayData: function() {
      var self = this;
      
      if(this.listView){
        this.stopListening(this.listView);
        this.listView.remove();
      }
     
      this.mapView.displayData({routes: this.data.routes, stops: this.data.stops});
      
      this.listView = new RouteList({data: this.data.routes});     
      this.$('.side-list').append(this.listView.render().$el);
      
      this.listenTo(this.listView, 'route:highlight', function(routeName, isHighlighted){
        self.mapView.highlightRoute(routeName, isHighlighted);
      });
      
    },

    render : function() {
      var self = this;      
      this.$el.html(template());
      
      $('#side-list-container').slideReveal({
    	  trigger: $("#buses-button"),
    	  push: false,
    	  width: 320,
    	  show: function(panel, trigger) {
    		  panel.addClass('open')
    	  },
    	  hide: function(panel, trigger) {
    		  panel.removeClass('open');
    	  }
      }).removeClass('init');
      
      this.mapView = new MapView({el: this.$('#map')});
      
      this.mapView.render();
      this.listenTo(this.mapView, 'show:endstop', function(stopId){
        self.endStopDetails.setData(Mocks.getEndStopData());
        self.endStopDetails.show();
      });

      this.endStopDetails = new EndStopDetails({el: this.$('.endstop-details')});
      this.endStopDetails.render();
      
      this.displayData(this.data);
    }

  });

  return RoutesOverview;
});