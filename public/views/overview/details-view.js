define([ 'jquery', 
         'underscore', 
         'backbone', 
         'views/overview/raw-route-table',
         'hbs!templates/overview/details' ], 
         function($, _, Backbone, RoutesTableView, template) {
  var EndStop = Backbone.View.extend({
    
    events: {
      'click .close-ear' : 'hide'
    },
    
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
    
    show: function() {
      this.$el.show();
      this.hidden = false;
      this.contentView.recalculateTable();
    },
    
    hide: function() {
      this.$el.hide();
      this.hidden = true;
    },
    
    render: function() {
      this.$el.html(template({}));
      this.contentView = new RoutesTableView({el: this.$('.details-container')});
      this.contentView.render();
      this.hide();
    }
    
  });
  
  return EndStop;
});