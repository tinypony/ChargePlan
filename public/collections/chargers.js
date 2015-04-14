define(['backbone', 'models/charger-model'], function(Backbone, Charger){
  var Chargers = Backbone.Collection.extend({
    model: Charger,
    url: '/api/chargers'
  });
  
  return Chargers;
});