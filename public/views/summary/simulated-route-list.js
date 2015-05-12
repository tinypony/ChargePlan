define([ 'jquery', 
         'underscore', 
         'backbone', 
         'config-manager', 
         'event-bus', 
         'scroller',
         'const',
         'views/overview/raw-route-table', 
         'views/overview/route-list/route-list-menu', 
         'hbs!templates/summary/simple-route-list', 
         'hbs!templates/summary/simple-route-list-item' ], function($, _,
    Backbone, ConfigManager, EventBus, scroller, Constants, RouteTable, RouteMenu, routeListTemplate, itemTemplate) {

  var RawRouteList = Backbone.View.extend({

    events : {
    	
    },

    initialize : function(options) {
    },
    
    getRenderableRoute: function(r) {
		var longName = r.longName.split('/');
    	r.firstStop = longName[0];
    	r.lastStop = longName[1];
    	r.display = r.state === Constants.RouteState.SIMULATED_OK; 
    	return r;
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
