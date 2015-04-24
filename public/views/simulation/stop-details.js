define(['jquery', 
        'backbone', 
        'views/misc/modal-dialog', 
        'hbs!templates/simulation/stop-details'], 
    function($, Backbone, ModalDialog, template) {
  
  var StopDetailsView = Backbone.View.extend({
    
    initialize: function(opts) {
      this.chargersToAdd;
      this.chargers = opts.chargers;
      this.stop = opts.stop;
      this.project = opts.project;
      this.routeId = opts.routeId;
      this.isEndStop = opts.isEndStop;
    },
    
    render: function() {
      var self = this;
      
      var minTime;
      
      if(this.stop.chargingTimes) {
        minTime = this.stop.chargingTimes[this.routeId];
      } else if(this.isEndStop) {
        minTime = 600;
      } else {
        minTime = 10;
      }
      this.$el.html(template({
        stop: this.stop,
        chargerTypes: this.chargers.toJSON(),
        minChargingTime: minTime,
        isEndStop: this.isEndStop
      }));
      
      if(this.stop.charger) {
        this.$('.available-chargers').val(this.stop.charger.type.id);
      }
      
      var dialog = new ModalDialog({
        title: this.stop.name,
        id: 'stop-info',
        buttonText: 'Save changes',
        showClose: true,
        clickHandlers: {
          'primary': function(ev) {
            self.project.updateStop({
              stop: self.stop.stopId,
              route: self.routeId,
              chargersToAdd: self.$('.available-chargers').val(),
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