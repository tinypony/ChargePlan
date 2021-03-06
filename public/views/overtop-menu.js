define(['jquery', 
        'backbone', 
        'router',
        'const',
        'event-bus',
        'config-manager',
        'views/misc/modal-dialog',
        'views/misc/sim-parameters',
        'hbs!templates/menu/sim-parameters',
        'hbs!templates/overtop-menu'], 
    function($, Backbone, router, Constants, EventBus, ConfigManager, Dialog, SimParameters, simTemplate, template) {
  
  var MenuView = Backbone.View.extend({
    events: {
      'click .settings': 'openSettings',
      'click .project-main' : 'goMain',
      'click .all-routes-simulation': 'openSimulationDialog'
    },
    
    intialize: function() {
    	this.simulationParams = {};
    },
    
    goMain: function() {
    	router.navigate('/', true);
    },
    
    openSettings: function() {
    	router.navigate('/configuration', true);
    },
    
    openSimulationDialog: function() {
    	var self = this;
    	
    	ConfigManager.getProject().done(function(proj) {
    		
            var simParams = new SimParameters({ simParams: self.simulationParams});
            
            var dialog = new Dialog({
              title: $('<h5 class="subsection-header">Simulation parameters</h5>'),
              id: 'simulation-parameters',
              buttonText: 'Simulate',
              showClose: true,
              
              clickHandlers: {
                primary: function(ev) {
                  if(!simParams.isValid()) {
                	  return;
                  }
                  self.simulationParams = simParams.getParams();
                  $(this.target).addClass('active');
                  $.ajax({
                	 url: '/api/projects/'+proj.get('id')+'/chargers',
                	 method: 'POST',
                	 contentType: 'application/json',
                	 data: JSON.stringify({
                		 charger: self.simulationParams.charger,
                		 minChargingTime: self.simulationParams.minChargingTime
                	 })
                  }).done(function(){
                	  $.ajax({
                		 url: '/api/projects/'+proj.get('id')+'/simulate/all',
                     	 method: 'POST',
                     	 contentType: 'application/json',
                     	 data: JSON.stringify(self.simulationParams)
                	  }).done(function(data){
                		  
                		  var stateUpdate = _.map(data, function(routeSimResult) {
                			 if(routeSimResult.feasibility) {
	                			 return {
	                				 routeId: routeSimResult.routeId,
	                				 state: routeSimResult.feasibility.survived ? Constants.RouteState.SIMULATED_OK : Constants.RouteState.SIMULATED_FAIL
	                			 };
	                		  } else {
	                			  return null;
	                		  }
                		  });
                		  
                          EventBus.trigger('simulation:all');
                          proj.setRouteStates(stateUpdate);
                		  proj.fetch();
                          dialog.close();
                	  });
                	  
                  });
                }
              }
            });
            
            dialog.render().content(simParams.render().$el);
            
    	});

    },
    
    render: function() {
      this.$el.html(template());
    }
  });
  
  return MenuView;
});