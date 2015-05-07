define(['jquery', 
        'backbone', 
        'router',
        'hbs!templates/overtop-menu'], 
    function($, Backbone, router, template) {
  
  var MenuView = Backbone.View.extend({
    events: {
      'click .settings': 'openSettings'
    },
    
    openSettings: function() {
    	router.navigate('/configuration', true);
    },
    
    render: function() {
      this.$el.html(template());
    }
  });
  
  return MenuView;
});