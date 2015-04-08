define(['jquery', 
        'underscore', 
        'backbone', 
        'event-bus',
        'scroller',
        'hbs!templates/overview/route-list'], 
		function($, _, Backbone, EventBus, scroller, routeListTemplate) {
	
	var RawRouteList = Backbone.View.extend({
	    
	    events: {
	      'mouseover .accordion-toggle': 'onMouseover',
	      'mouseout .accordion-toggle': 'onMouseout',
	      'change .route-select-cb': 'onCheckbox'
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
	    	 
	    	 route.avg.distance = Math.floor(_.reduce(route.stats, runningAverageStat('totalDistance'), 0));
	    	 route.avg.departures = Math.round(_.reduce(route.stats, runningAverageStat('departures'), 0));
	    	 route.avg.CO2 = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'CO2'), 0));
	    	 route.avg.CO = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'CO'), 0));
	    	 route.avg.NOx = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'NOx'), 0));
	    	 
	    	 if(bin[route.name]) {
	    		 bin[route.name].instances.push(route);
	    	 } else {
	    		 bin[route.name] = {name: route.name, instances: [route]};
	    	 }
	    	 
	    	 if(_.findWhere(options.project.routes, {name: route.name})) {
	    		 bin[route.name].imported = true;
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
	    
	    onCheckbox: function(ev) {
	    	ev.stopPropagation();
	    	var $this = $(ev.target);
	    	var routeName = $this.attr('data-routename');
	    	
	    	if($this.is(':checked')) {
	    		EventBus.trigger('add:route', this.data[routeName]);
	    	} else {
	    		EventBus.trigger('remove:route', this.data[routeName]);
	    	}
	    },
	    
	    render: function() {
	      this.$el.html(routeListTemplate({
	        routes: _.values(this.data)     
	      }));
	      
	      var self = this;

	      
	      this.$('#root-accordion').collapse();
	      _.each(_.keys(this.data), function(route) {
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
	
	return RawRouteList
});
