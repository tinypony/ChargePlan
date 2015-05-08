define([ 'jquery', 
         'backbone', 
         'collections/chargers', 
         'collections/buses',
		'config-manager',
		'hbs!templates/menu/sim-parameters', 
		'bsslider' ], 
		function($, Backbone, Chargers, Buses, ConfigManager, simParamsTemplate) {

	var SimParams = Backbone.View.extend({
		initialize : function() {
			this.buses = new Buses();
			this.chargers = new Chargers();
		},
		
		getParams: function() {
			return {
				date: this.$('.simulation-date').val(),
				charger: this.$('.charger-select').val(),
				busType: this.buses.get(this.$('.bus-select').val()),
				minChargingTime: this.$('.charging-time').slider('value')
			};
			
		},

		render : function() {

			var self = this;
			var onReady = _.after(3, function() {
				self.$el.html(simParamsTemplate({
					buses : self.buses.toJSON(),
					chargers : self.chargers.toJSON()
				}));

				self.$('.selectpicker').selectpicker();

				var available = function(date) {
					dmy = date.getFullYear() + '-' + (date.getMonth() + 1)
							+ "-" + date.getDate();
					if ($.inArray(dmy, self.availableDates) != -1) {
						return [ true, '', 'Available' ];
					} else {
						return [ false, '', 'unAvailable' ];
					}
				};
				
				self.$('.simulation-date').datepicker({
			        beforeShowDay : available,
			        dateFormat : 'yy-mm-dd'
			      });
				
				self.$('.charging-time').slider({
					min: 1,
					max: 1000,
					step: 20,
					value: 600,
					slide: function(event, ui) {
						self.$('.time-label').text(ui.value + ' seconds');
					}
				});
			});

			this.buses.fetch().done(onReady);
			this.chargers.fetch().done(onReady);
			ConfigManager.getProject().done(
					function(project) {
						$.get('/api/projects/' + project.get('id') + '/dates')
								.done(function(dates) {
									self.availableDates = dates;
									onReady();
								});
					});

			return this;
		}
	});

	return SimParams;
});