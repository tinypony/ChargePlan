define([ 'jquery', 'underscore', 'backbone', 'config-manager', 'event-bus', 'scroller', 'views/overview/raw-route-table', 'views/overview/route-list/route-list-menu', 'hbs!templates/overview/simple-route-list', 'hbs!templates/overview/simple-route-list-item' ], function($, _,
    Backbone, ConfigManager, EventBus, scroller, RouteTable, RouteMenu, routeListTemplate, itemTemplate) {

  var RawRouteList = Backbone.View.extend({

    events : {
      'click .route-list-item' : 'onClickRoute',
      'click .route-list-item .remove': 'onClickRemove',
      'mouseover .route-list-item' : 'onMouseover',
      'mouseout .route-list-item' : 'onMouseout'
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
    
    addRoute: function(route) {
      this.project.addRoute(route);
      var item = itemTemplate({route:route});
      this.$('.route-list').append(item);
    },
    
    removeRoute: function(route) {
      this.project.removeRoute(route);
      this.$('.route-list-item[data-routeid="'+route+'"]').remove();
    },

//    onClickAdd : function() {
//      var self = this;
//      var tableView = new RouteTable();
//
//      var modal = $('#myModal').modal();
//      $('#myModal .modal-body').append(tableView.render().$el);
//
//      $('#myModal .add-selected-routes').click(function() {
//        self.setRoutes(tableView.getSelectedRoutes());
//        modal.modal('hide');
//      });
//
//      $('#myModal').on('shown.bs.modal', function() {
//        tableView.recalculateTable();
//      });
//
//      $('#myModal').on('hide.bs.modal', function() {
//        $(this).off('click');
//        $(this).off('hide.bs.modal');
//        $(this).off('shown.bs.modal');
//        tableView.remove();
//      });
//    },
    
    onClickRoute: function(ev) {
      var $targ = $(ev.currentTarget);
      var routeId = $targ.attr('data-routeid');
      
      if($targ.hasClass('active')) {
        $targ.removeClass('active');
        EventBus.trigger('route:unselect', routeId);
      } else {
        $targ.addClass('active').siblings().removeClass('active');
        EventBus.trigger('route:select', routeId);
      }
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

    render : function() {
      var self = this;

      ConfigManager.getProject().done(function(project) {
        var routes = project.get('routes');
        self.project= project;
        self.$el.html(routeListTemplate({
          routes : routes
        }));
        
        self.listenTo(EventBus, 'route:add', self.addRoute);
        self.listenTo(EventBus, 'route:remove', self.removeRoute);

        _.each(routes, function(routeBag) {
          self.drawRoute(routeBag);
        });

        _.defer(function() {
          self.$('.nano').nanoScroller({
            flash : true
          });

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

  return RawRouteList;
});
