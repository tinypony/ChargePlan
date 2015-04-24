define(['jquery', 
        'backbone', 
        'views/misc/modal-dialog', 
        'hbs!templates/simulation/stop-details'], 
    function($, Backbone, ModalDialog, template) {
  
  var StopDetailsView = Backbone.View.extend({
    events: {
      'change .available-chargers': 'addChargerToStop'
    },
    
    initialize: function(opts) {
      _.bindAll(this, ['addChargerToStop']);
      this.chargersToAdd;
      this.chargers = opts.chargers;
      this.stop = opts.stop;
      this.project = opts.project;
      this.routeId = opts.routeId;
    },
    
    addChargerToStop: function() {
      var chargerId = this.$('select').val();
      var charger = this.chargers.get(chargerId);
      this.chargersToAdd = chargerId;
    },
    
    render: function() {
      var self = this;
      
      this.$el.html(template({
        stop: this.stop,
        chargerTypes: this.chargers.toJSON()
      }));
      
      this.$('.available-chargers').val(this.stop.charger);
      
      var dialog = new ModalDialog({
        title: this.stop.name,
        id: 'stop-info',
        buttonText: 'Save changes',
        showClose: true,
        clickHandlers: {
          'primary': function(ev) {
            console.log({
              stop: self.stop.stopId,
              route: self.routeId,
              chargersToAdd: self.chargersToAdd,
              minChargingTime: self.$('.min-charging-time').val()
            });
            self.project.updateStop({
              stop: self.stop.stopId,
              route: self.routeId,
              chargersToAdd: self.chargersToAdd,
              minChargingTime: self.$('.min-charging-time').val()
            });
            dialog.close();
          }
        }
      });
      
      dialog.render().content(this.$el);
    }
  });
  
  return StopDetailsView;
});