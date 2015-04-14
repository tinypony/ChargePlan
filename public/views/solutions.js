define(['jquery', 
        'underscore', 
        'backbone', 
        'collections/chargers', 
        'collections/buses',
        'hbs!templates/solutions'], 
    function($, _, Backbone, Chargers, Buses, template) {
  
  var SolutionView = Backbone.View.extend({
    
    events: {
      'click .save-bus': 'saveBus',
      'click .save-charger': 'saveCharger'
    },
    
    initialize: function() {
      
      var self = this;
      
      this.loadData().done(function(obj) {
        self.chargers = obj.chargers;
        self.buses = obj.buses;
        self.listenTo(self.buses, 'sync', self.render);
        self.listenTo(self.chargers, 'sync', self.render);
        self.render();
      });
    },
    
    saveBus: function(ev) {
      ev.preventDefault();
      var form = this.$('form#new-bus-form');
      this.buses.create({
        manufacturer: $('input[name="manufacturer"]', form).val(),
        model: $('input[name="model"]', form).val(),
        capacity: $('input[name="capacity"]', form).val()
      });
    },
    
    saveCharger: function(ev) {
      ev.preventDefault();
      
      var form = this.$('form#new-charger-form');
      this.chargers.create({
        manufacturer: $('input[name="manufacturer"]', form).val(),
        model: $('input[name="model"]', form).val(),
        power: $('input[name="power"]', form).val()
      });
    },
    
    loadData: function() {
      var promise = $.Deferred();
      
      var buses = new Buses();
      var chargers = new Chargers();
      
      var onReady = _.after(2, function(){
        promise.resolve({
          buses: buses,
          chargers: chargers
        });
      });
      
      buses.fetch().done(onReady);
      chargers.fetch().done(onReady);
      
      return promise;
    },
    
    render: function() {
      this.$el.html(template({
        chargers: this.chargers.toJSON(),
        buses: this.buses.toJSON()
      }));
    }
  });
  
  return SolutionView;
});