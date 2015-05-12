define(['backbone', 'models/transformer-model'], function(Backbone, Transformer){
  var Transformers = Backbone.Collection.extend({
    model: Transformer,
    url: '/api/transformers'
  });
  
  return Transformers;
});