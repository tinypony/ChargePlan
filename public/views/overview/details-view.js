define([ 'jquery', 
         'underscore', 
         'backbone', 
         'views/overview/raw-route-table',
         'hbs!templates/overview/details' ], 
         function($, _, Backbone, RoutesTableView, template) {
  var EndStop = Backbone.View.extend({
    
    
    initialize: function(options){
      this.hidden = true;
    },
    
    showProjectDetails: function() {
    	this.contentView = new ProjectDetailsView({el: this.$('.details-container')});
    	this.contentView.render();
    	this.show();
    },
    
    showRoutesTable: function() {
      this.contentView = new RoutesTableView({el: this.$('.details-container')});
      this.contentView.render();
      this.show();
    },
    
    isHidden: function() {
      return this.hidden;
    },
    
    toggle: function() {
    	if(this.isHidden()) {
    		this.show();
    	} else {
    		this.hide();
    	}
    },
    
    show: function() {
      this.$el.removeClass('off').addClass('on');
      this.contentView.recalculateTable();
      this.hidden = false;
    },
    
    hide: function() {
      this.$el.addClass('off').removeClass('on');
      this.hidden = true;
    },
    
    render: function() {
      this.$el.html(template({}));
      this.contentView = new RoutesTableView({el: this.$('.details-container')});
      this.contentView.render();
      this.listenTo(this.contentView, 'open-toggle', this.toggle);
      this.hide();
    }
    
  });
  
  return EndStop;
});