define([ 'jquery', 'underscore', 'backbone', 'config-manager', 'event-bus', 'scroller', 'views/overview/raw-route-table', 'views/overview/route-list/route-list-menu', 'hbs!templates/overview/simple-route-list', 'hbs!templates/overview/simple-route-list-item' ], function($, _,
    Backbone, ConfigManager, EventBus, scroller, RouteTable, RouteMenu, routeListTemplate, itemTemplate) {

  var RawRouteList = Backbone.View.extend({

    events : {
      'click .route-list-item' : 'onClickRoute',
      'mouseover .route-list-item' : 'onMouseover',
      'mouseout .route-list-item' : 'onMouseout'
    },

    initialize : function(options) {
      var self = this;
    },

    drawRoute : function(route) {
      EventBus.trigger('draw:route', route);
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
        this.trigger('route:unselect', routeId);
      } else {
        $targ.addClass('active').siblings().removeClass('active');
        this.trigger('route:select', routeId);
      }
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

        self.$el.html(routeListTemplate({
          routes : routes
        }));

        // self.routeMenu = new RouteMenu({
        // el : $('#second-level-route-menu')
        // });

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
