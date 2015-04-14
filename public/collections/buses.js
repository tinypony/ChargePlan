define(['backbone', 'models/bus-model'], function(Backbone, Bus){
  var Chargers = Backbone.Collection.extend({
    model: Bus,
    url: '/api/buses'
  });
  
  return Chargers;
});