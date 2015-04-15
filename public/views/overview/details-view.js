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
    
    show: function() {
      this.$el.show();
    },
    
    hide: function() {
      this.$el.hide();
    },
    
    render: function() {
      this.$el.html(template({}));
      this.hide();
    }
    
  });
  
  return EndStop;
});