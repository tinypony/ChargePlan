define(['jquery', 
        'backbone', 
        'views/misc/modal-dialog', 
        'hbs!templates/simulation/stop-details'], 
    function($, Backbone, ModalDialog, template) {
  
  var StopDetailsView = Backbone.View.extend({
    events: {
      'click .add-charger': 'addChargerToStop'
    },
    
    initialize: function(opts) {
      _.bindAll(this, ['addChargerToStop']);
      this.chargersToAdd = [];
      this.chargers = opts.chargers;
      this.stop = opts.stop;
      this.project = opts.project;
    },
    
    addChargerToStop: function() {
      var chargerId = this.$('select').val();
      var charger = this.chargers.get(chargerId);
      this.$('ul').append('<li>' + charger.get('manufacturer') + ' ' + charger.get('model') + ' ' + charger.get('power') + '</li>');
      this.chargersToAdd.push(chargerId);
    },
    
    render: function() {
      var self = this;
      
      this.$el.html(template({
        stop: this.stop,
        chargerTypes: this.chargers.toJSON()
      }));
      
      var dialog = new ModalDialog({
        title: this.stop.name,
        id: 'stop-info',
        buttonText: 'Save changes',
        showClose: true,
        clickHandlers: {
          'primary': function(ev) {
            _.each(self.chargersToAdd, function(chargId) {
              self.project.addCharger({
                chargerType: chargId,
                stop: self.stop.stopId
              })
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