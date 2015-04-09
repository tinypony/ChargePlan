define(['jquery', 
        'backbone',  
        'hbs!templates/overtop-menu'], 
    function($, Backbone, template) {
  
  var MenuView = Backbone.View.extend({
    events: {
      'click .add-routes': 'openRoutes'
    },
    
    openRoutes: function() {

    },
    
    render: function() {
      this.$el.html(template());
    }
  });
  
  return MenuView;
});