define(['jquery', 
        'underscore', 
        'backbone', 
        'config-manager',
        'event-bus',
        'scroller',
        'datatables',
        'hbs!templates/route-table'], 
		function($, _, Backbone, ConfigManager, EventBus, scroller, dt, routeTableTemplate) {
	
	var RawRouteTable = Backbone.View.extend({
	    
	    events: {
	      'mouseover .accordion-toggle': 'onMouseover',
	      'mouseout .accordion-toggle': 'onMouseout',
	      'change .route-select-cb': 'onCheckbox'
	    },
	    
	    initialize: function(options) {
	      var self = this;
	      _.bindAll(this, ['onCheckbox']);
	      this.selectedRoutes = [];
	      _.bindAll(this, ['processData']);
	      
	      ConfigManager.getProject().done(function(project) {
	        self.project = project;
	        self.selectedRoutes = project.get('routes');
	        
	        $.get('/api/routes').done(function(data){
	          self.processData(data);
	          self.render();
	        });
	      })
	    },
	    
	    processData: function(data) {
//	      var bin = {};
	      var self = this;
//	       _.each(data, function(route){
//	         route.avg = {};
//	         
//	         var runningAverageStat = function(statName, statName2) {
//	           return function(memo, stat, n) {
//	             
//	             if(_.isUndefined(statName2)) {
//	               return (stat[statName] + n * memo) / (n+1);
//	             } else {
//	               return (stat[statName][statName2] + n * memo) / (n+1);
//	             }
//	           };
//	         };
//	         
//	         route.avg.distance = Math.floor(_.reduce(route.stats, runningAverageStat('totalDistance'), 0));
//	         route.avg.departures = Math.round(_.reduce(route.stats, runningAverageStat('departures'), 0));
//	         route.avg.CO2 = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'CO2'), 0));
//	         route.avg.CO = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'CO'), 0));
//	         route.avg.NOx = Math.floor(_.reduce(route.stats, runningAverageStat('emissions', 'NOx'), 0));
//	         
//	         if(bin[route.name]) {
//	           bin[route.name].instances.push(route);
//	         } else {
//	           bin[route.name] = {name: route.name, instances: [route]};
//	         }
//	         
//	         if(_.findWhere(self.project.get('routes'), {name: route.name})) {
//	           bin[route.name].imported = true;
//	         }
//	        });
//	        
//	        _.each(_.values(bin), function(routeBag){
//	          routeBag.avg = {};
//	          
//	          var runningAverageStat = function(statName, statName2) {
//	             return function(memo, instance, n) {
//	               
//	               if(_.isUndefined(statName2)) {
//	                 return (instance.avg[statName] + n * memo) / (n+1);
//	               } else {
//	                 return (instance.avg[statName][statName2] + n * memo) / (n+1);
//	               }
//	             };
//	           };
//	           
//	           routeBag.avg.distance = Math.floor(_.reduce(routeBag.instances, runningAverageStat('distance'), 0));
//	           routeBag.avg.departures = Math.round(_.reduce(routeBag.instances, runningAverageStat('departures'), 0));
//	           routeBag.avg.CO2 = Math.floor(_.reduce(routeBag.instances, runningAverageStat('CO2'), 0));
//	           routeBag.avg.CO = Math.floor(_.reduce(routeBag.instances, runningAverageStat('CO'), 0));
//	           routeBag.avg.NOx = Math.floor(_.reduce(routeBag.instances, runningAverageStat('NOx'), 0));
//	           bin[routeBag.name] = routeBag;
//	        });
//	        
//	        this.data = bin;
	    	_.each(data, function(route) {
		         if(_.findWhere(self.project.get('routes'), {routeId: route.routeId})) {
		           route.imported = true;
		         }
	    	});
	    	this.data = data
	    },
	    
	    getSelectedRoutes: function() {
	      return this.selectedRoutes;
	    },
	    
	    onCheckbox: function(ev) {
	    	ev.stopPropagation();
	    	var $this = $(ev.target);
	    	var routeId = $this.attr('name');
	    	
	    	if($this.is(':checked')) {
	    	  EventBus.trigger('route:add', _.findWhere(this.data, {routeId: routeId}));
	    	} else {
	    	  EventBus.trigger('route:remove', _.findWhere(this.data, {routeId: routeId}))
	    	}
	    },
	    
	    addRoutesToProject: function() {
	      EventBus.trigger('add:routes', this.selectedRoutes);
	    },
	    
	    recalculateTable: function() {
	    	this.table.fnAdjustColumnSizing();
	    },
	    
	    
	    render: function() {
	      
	      this.$el.html(routeTableTemplate({
	        routes: _.values(this.data)     
	      }));
	      
	      this.table = this.$('#all-routes-table').dataTable({
	        paging: false,
	        scrollY: 338,
	        info: false,
	        order: [[ 1, 'asc' ]],
	        columnDefs: [
	          { 'width': '30px', 'targets': 0 },
	          { 'orderable': false, 'targets': 0 }
	        ]
	      });
	      
	      return this;
	    }
	  });
	
	return RawRouteTable;
});
