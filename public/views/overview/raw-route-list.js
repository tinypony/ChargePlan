define([ 'jquery', 'underscore', 'backbone', 'config-manager', 'event-bus',
		'scroller', 'views/overview/raw-route-table',
		'views/overview/route-list/route-list-menu',
		'hbs!templates/overview/simple-route-list',
		'hbs!templates/overview/simple-route-list-item' ], function($, _,
		Backbone, ConfigManager, EventBus, scroller, RouteTable, RouteMenu,
		routeListTemplate, itemTemplate) {

	var RawRouteList = Backbone.View.extend({

		events : {
			'click .add-routes' : 'onClickAdd',
			'click .route-list-item' : 'showRouteMenu',
			'mouseover .route-list-item' : 'onMouseover',
			'mouseout .route-list-item' : 'onMouseout'
		},

		initialize : function(options) {
			var self = this;

			ConfigManager.getProject().done(
					function(project) {
						self.project = project;

						self.listenTo(EventBus, 'add:route', function(route) {
							self.project.addRoute(route);
							self.$('.route-list').append(itemTemplate({
								route : route
							}));
						});

						self.listenTo(EventBus, 'remove:route',
								function(route) {
									self.$(
											'.route-list-item[data-routeid="'
													+ route.routeId + '"]')
											.remove();
									self.project.removeRoute(route.routeId);
								});
					});
		},

		drawRoute : function(route) {
			EventBus.trigger('draw:route', route);
		},

		setRoutes : function(route) {
			var self = this;
			this.project.setRoutes(route);

			this.$('.route-list').empty();
			EventBus.trigger('clear:routes');

			_.each(this.project.get('routes'), function(route) {
				self.$('.route-list').append(itemTemplate({
					route : route
				}));
				self.drawRoute(route);
			});
		},

		onClickAdd : function() {
			var self = this;
			var tableView = new RouteTable();

			var modal = $('#myModal').modal();
			$('#myModal .modal-body').append(tableView.render().$el);

			$('#myModal .add-selected-routes').click(function() {
				self.setRoutes(tableView.getSelectedRoutes());
				modal.modal('hide');
			});

			$('#myModal').on('shown.bs.modal', function() {
				tableView.recalculateTable();
			});

			$('#myModal').on('hide.bs.modal', function() {
				$(this).off('click');
				$(this).off('hide.bs.modal');
				$(this).off('shown.bs.modal');
				tableView.remove();
			});
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

			ConfigManager.getProject().done(
					function(project) {
						var routes = project.get('routes');

						self.$el.html(routeListTemplate({
							routes : routes
						}));

						self.routeMenu = new RouteMenu({
							el : $('#second-level-route-menu')
						});

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

							self.$('.accordion-group').on('show.bs.collapse',
									toggleHandler);
							self.$('.accordion-group').on('hide.bs.collapse',
									toggleHandler);
						});
					});

			return this;
		}
	});

	return RawRouteList;
});
