define(['jquery', 'backbone', 'hbs!templates/simulation/charger-details'], 
    function($, Backbone, template) {
  
  var ChargerDetailsView = Backbone.View.extend({
    
    initialize: function(options) {
      this.model = options.model;
    },
    
    showModel: function(model) {
      this.model = model;
      this.render();
    },
    
    render: function() {
      this.$el.html(template({
        charger: this.model ? this.model.toJSON() : null 
      }));
      
      return this;
    }
  });
  
  return ChargerDetailsView;
});