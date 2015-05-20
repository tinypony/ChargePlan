define([ 'jquery', 
         'underscore', 
         'backbone', 
         'config-manager', 
         'event-bus', 
         'scroller', 
         'views/overview/raw-route-table', 
         'views/overview/route-list/route-list-menu', 
         'hbs!templates/overview/simple-route-list', 
         'hbs!templates/overview/simple-route-list-item' ], function($, _,
    Backbone, ConfigManager, EventBus, scroller, RouteTable, RouteMenu, routeListTemplate, itemTemplate) {

  var RawRouteList = Backbone.View.extend({

    events : {
      'click .route-list-item' : 'onClickRoute',
      'click .route-list-item .remove': 'onClickRemove',
      'mouseover .route-list-item' : 'onMouseover',
      'mouseout .route-list-item' : 'onMouseout',
      'click .back' : 'onBack'
    },

    initialize : function(options) {
      _.bindAll(this, ['addRoute']);
      var self = this;
    },

    drawRoute : function(route) {
      EventBus.trigger('route:draw', route);
    },

    setRoutes : function(routes) {
      var self = this;
      this.project.setRoutes(routes);

      this.$('.route-list').empty();
      EventBus.trigger('clear:routes');

      _.each(this.project.get('routes'), function(route) {
        self.$('.route-list').append(itemTemplate({
          route : route
        }));
        self.drawRoute(route);
      });
    },
    
    onBack: function() {
        EventBus.trigger('route:unselect');
    },
    
    onUnselect: function() {
    	this.selected = false;
    	 this.$('.cube').removeClass('back');
         this.$('.route-list-item').removeClass('active');
    },
    
    addRoute: function(route) {
      var item = itemTemplate({route:this.getRenderableRoute(route)});
      this.$('.no-message').remove();
      this.$('.route-list').append(item);
    },
    
    removeRoute: function(route) {
      this.$('.route-list-item[data-routeid="'+route+'"]').remove();
    },
    
    onClickRoute: function(ev) {
      var $targ = $(ev.currentTarget);
      var routeId = $targ.attr('data-routeid');
      
      if($targ.hasClass('active')) {
        this.onBack();
      } else {
        $targ.addClass('active').siblings().removeClass('active');
        EventBus.trigger('route:select', routeId);
        this.selected=true;
        this.$('.cube').addClass('back');
      }
    },
    
    isSelected: function() {
    	return this.selected;
    },
    
    onClickRemove: function(ev) {
      var $targ = $(ev.currentTarget);
      var routeId = $targ.attr('data-routeid');
      EventBus.trigger('route:remove', routeId);
      ev.stopPropagation();
    },

    onMouseout : function(ev) {
      $target = $(ev.currentTarget);
      var routeId = $target.attr('data-routeid');
      EventBus.trigger('route:highlight', routeId, false);
    },

    onMouseover : function(ev) {
      $target = $(ev.currentTarget);
      var routeName = $target.attr('data-routeid');
      EventBus.trigger('route:highlight', routeName, true);
    },

    showRouteMenu : function(ev) {
      $target = $(ev.currentTarget);
      var routeName = $target.attr('data-routename');

      if ($target.hasClass('active')) {
        $target.removeClass('active');
        this.routeMenu.hide();
      } else {
        $target.addClass('active');
        this.routeMenu.showFor(routeName, $target);
      }
    },
    
    getRenderableRoute: function(r) {
		var longName = r.longName.split('/');
    	r.firstStop = longName[0];
    	r.lastStop = longName[1];
    	return r;
    },
    
    updateStates: function() {
    	var self = this;
    	this.$('.route-list-item').each(function(){
    		var $listItem = $(this);
    		var routeId = $listItem.attr('data-routeid');
    		var route = self.project.getRoute(routeId);
    		$('.route-name', $listItem).attr('data-state', route.state);
    	});
    },

    render : function() {
      var self = this;

      ConfigManager.getProject().done(function(project) {
        self.project = project;
        var routes = project.get('routes');
        routes = _.map(routes, self.getRenderableRoute);
        
        self.$el.html(routeListTemplate({
          routes: routes
        }));
        
        self.listenTo(project, 'state:changed', self.updateStates);

        _.defer(function() {
          self.$('.nano').nanoScroller({
            flash : true
          });
        });
      });

      return this;
    }
  });

  return RawRouteList;
});
