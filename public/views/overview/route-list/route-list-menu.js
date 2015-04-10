define(['jquery', 'underscore', 'backbone'], function($, _, Backbone) {
  var RouteMenu = Backbone.View.extend({
    initialize: function() {
    },
    
    showFor: function(routeName, elem) {
      var pos = elem.position();
      this.$el.css('top', pos.top + 40);
      this.$el.addClass('shown');
    },
    
    hide: function() {
      this.$el.removeClass('shown');
    }
  });
  
  return RouteMenu;
});