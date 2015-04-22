define([ 'jquery', 'underscore', 'backbone' ], function($, _, Backbone) {
  var ProjectModel = Backbone.Model.extend({
    urlRoot : '/api/projects',
    defaults : function() {
      routes: []
    },

    initialize : function() {

    },
    
    addRoute: function(route) {
      this.get('routes').push(route);
      this.save();
    },
    
    removeRoute: function(route) {
      var routes = this.get('routes');
      routes = _.without(routes, function(r){
        return r.routeId === route.routeId;
      });
      
      this.set('routes', routes);
      this.save();
    },

    setRoutes : function(routeBags) {
      this.set('routes', routeBags);
      this.save();
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
      
      $.ajax('/api/projects/'+this.id+'/update/stop', {
        method: 'PUT',
        data: JSON.stringify(opts),
        contentType: "application/json",
      }).done(function(){
        self.fetch();
      });
    }
  });

  return ProjectModel;
});