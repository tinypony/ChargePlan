define([ 'jquery', 'underscore', 'backbone' ], function($, _, Backbone) {
  var ProjectModel = Backbone.Model.extend({
    urlRoot : '/api/projects',
    defaults : function() {
      routes: []
    },

    initialize : function() {

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
    }
  });

  return ProjectModel;
});