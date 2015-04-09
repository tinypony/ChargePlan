define(['jquery', 'backbone', 'views/overtop-menu'], function($, Backbone, MenuView){
  var App = {
    init: function() {
      var menu = new MenuView({el: $('header')});
      menu.render();
      Backbone.history.start();
    } 
  };
  
  return App;
});