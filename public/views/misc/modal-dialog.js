define(['jquery', 
        'underscore', 
        'backbone', 
        'hbs!templates/misc/modal'], 
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
      
      this.setElement(template({
        config: this.config
      }));
      
      if(!_.isString(this.config.title)) {
    	  this.$('.modal-title').empty().append(this.config.title);
      }
      
      this.$el.on('hidden.bs.modal', function(){
        self.remove();
      });
      
      this.$el.on('shown.bs.modal', function(){
    	 if(self.config.onShow && _.isFunction(self.config.onShow)) {
    		 self.config.onShow.call();
    	 } 
      });
      
      $('body').prepend(this.$el);
      
      this.$el.modal();
      return this;
    }
    
  });
  
  return ModalDialog;
});
