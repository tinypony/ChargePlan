define(['jquery', 'underscore', 'backbone', 'hbs!templates/misc/modal'], 
    function($, _, Backbone, template) {
  var ModalDialog = Backbone.View.extend({
    
    events: {
      'click .modal-footer .btn-primary': 'onClick'
    },
    
    initialize: function(options) {
      _.bindAll(this, ['onClick']);
      this.config = options;
    },
    
    close: function() {
      this.$el.modal('hide');
    },
    
    content: function(el) {
      this.$('.modal-body').append(el);
      return this;
    },
    
    onClick: function(ev) {
      this.config.clickHandlers['primary'].call(ev);
    },
    
    render: function() {
      var self = this;
      
      console.log(this.config);
      this.setElement(template({
        config: this.config
      }));
      
      this.$el.on('hidden.bs.modal', function(){
        self.remove();
      });
      
      $('body').prepend(this.$el);
      
      this.$el.modal();
      return this;
    }
    
  });
  
  return ModalDialog;
});
