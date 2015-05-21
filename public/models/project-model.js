define([ 'jquery', 
         'underscore', 
         'backbone' ], 
		function($, _, Backbone) {
	
  var ProjectModel = Backbone.Model.extend({
    urlRoot : '/api/projects',
    defaults : function() {
      routes: []
    },

    initialize : function() {

    },
    
    notifyChangedStates: function() {
    	this.trigger('state:changed');
    },
    
    addRoute: function(route) {
      var self = this;
      
      return $.ajax({
    	  url: '/api/projects/'+this.get('id')+'/routes',
    	  method : 'POST',
    	  data: JSON.stringify(route),
    	  contentType: 'application/json'
      }).done(function() {
    	  self.fetch();
      });
    },
    
    setRouteState: function(routeId, state) {
    	this.getRoute(routeId).state = state;
    	this.notifyChangedStates();
    },
    
    setRouteStates: function(array) {
    	var self = this;
    	
    	_.each(array, function(item) {
    		if(!item) {
    			return;
    		}
    		self.getRoute(item.routeId).state = item.state;
    	});
    	
    	this.notifyChangedStates();
    },
    
    removeRoute: function(route) {
      var routes = this.get('routes');
      var routeToRemove = _.findWhere(routes, {routeId: route});
      var self = this;
      
      routes = _.reject(routes, function(r) {
        return r.routeId === route;
      });
      
      $.ajax('/api/projects/'+this.id+'/routes/remove',{
    	  method: 'POST',
          data: JSON.stringify(routeToRemove),
          contentType: 'application/json',
    	}).done(function(){
    	     self.set('routes', routes);
    	     self.trigger('sync');
    	});

    },

    setRoutes : function(routeBags) {
      this.set('routes', routeBags);
      this.save();
    },
    
    getRoute: function(routeId) {
    	var routes = this.get('routes');
    	var r = _.findWhere(routes,{routeId: routeId});
    	return r;
    },
    
    addCharger: function(opts) {
      var self = this;
      if(_.isUndefined(opts.chargerType) || _.isUndefined(opts.stop)) {
        throw new Error("Charger type and stop must be defined");
      }
      
      $.ajax('/api/projects/'+this.id+'/add/charger', {
        method: 'PUT',
        data: JSON.stringify(opts),
        contentType: "application/json",
      }).done(function(){
        self.fetch();
      });
    },
    
    updateStop: function(opts) {
      var self = this;
      console.log(opts);
      return $.ajax('/api/projects/'+this.id+'/update/stop', {
        method: 'PUT',
        data: JSON.stringify(opts),
        contentType: 'application/json'
      }).done(function(){
        self.fetch();
      });
    }
  });

  return ProjectModel;
});