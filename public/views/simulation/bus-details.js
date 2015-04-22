define(['jquery', 'backbone', 'hbs!templates/simulation/bus-details'], 
    function($, Backbone, template) {
  
  var BusDetailsView = Backbone.View.extend({
    
    initialize: function(options) {
      this.model = options.model;
    },
    
    showModel: function(model) {
      this.model = model;
      this.render();
    },
    
    render: function() {
      
      this.$el.html(template({
        bus: this.model ? this.model.toJSON() : null 
      }));
      
      return this;
    }
  });
  
  return BusDetailsView;
});