define(['jquery', 
        'underscore', 
        'backbone', 
        'config-manager', 
        'event-bus',
        'scroller',
        'views/overview/raw-route-table',
        'hbs!templates/overview/simple-route-list',
        'hbs!templates/overview/simple-route-list-item'], 
		function($, _, Backbone, ConfigManager, EventBus, scroller, RouteTable, routeListTemplate, itemTemplate) {
	
	var RawRouteList = Backbone.View.extend({
	    
	    events: {
	      'mouseover .accordion-toggle': 'onMouseover',
	      'mouseout .accordion-toggle': 'onMouseout',
	      'click .add-routes': 'onClickAdd'
	    },
	    
	    initialize: function(options) {
	      var self = this;
	      
	      ConfigManager.getProject().done(function(project){
	        self.project = project;
	        
	        self.listenTo(EventBus, 'add:routes', function(routeBags){
	          self.project.addRoutes(routeBags);
	          
	          _.each(routeBags, function(routeBag){
	            self.$('.route-list').append(itemTemplate({route:routeBag}));
	          });
	        });
	       
	        self.listenTo(EventBus, 'add:route', function(routeBag) {
	          self.project.addRoute(routeBag);        
	          self.$('.route-list').append(itemTemplate({route:routeBag}));
	        });
	        
	        self.listenTo(EventBus, 'remove:route', function(routeBag) {        
	          self.$('.route-list-item[data-routename="'+routeBag.name+'"]').remove();
	          self.project.removeRoute(routeBag.name);
	        });
	      });
	    },
	    
	    onClickAdd: function() {
	      var tableView = new RouteTable();
	      tableView.render();
	      var modal = $('#myModal').modal();
	      $('#myModal .modal-body').append(tableView.$el);
	      $('#myModal .add-selected-routes').click(function(){
	        tableView.addRoutesToProject();
	        modal.modal('hide');
	      });
	      $('#myModal').on('hide.bs.modal', function(){
	        tableView.remove();
	      });
	    },
	    
	    onMouseout: function(ev) {
	      $target = $(ev.currentTarget);
	      var routeName = $target.attr('data-routename');
	      this.trigger('route:highlight', routeName, false);
	    },

	    onMouseover: function(ev) {
	      $target = $(ev.currentTarget);
	      var routeName = $target.attr('data-routename');
	      this.trigger('route:highlight', routeName, true);
	    },
	    
	    render: function() {
	      var self = this;
	      
	      ConfigManager.getProject().done(function(project) {
	        
	        self.$el.html(routeListTemplate({
	          routes: project.get('routes')
	        }));
	        
	        _.defer(function() {
	          self.$('.nano').nanoScroller({flash: true});
	          
	          var toggleHandler = function(ev) {
	            var $tar = $(ev.currentTarget);
	            $tar.toggleClass('toggle-on');
	            ev.stopPropagation();
	          };
	            
	          self.$('.accordion-group').on('show.bs.collapse', toggleHandler);
	          self.$('.accordion-group').on('hide.bs.collapse', toggleHandler);
	        });
	      });
	      
	      
	      return this;
	    }
	  });
	
	return RawRouteList
});
