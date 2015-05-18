define([ 'jquery', 
         'backbone', 
         'collections/chargers', 
         'collections/buses',
		'config-manager',
		'hbs!templates/menu/sim-parameters', 
		'bsslider' ], 
		function($, Backbone, Chargers, Buses, ConfigManager, simParamsTemplate) {

	var SimParams = Backbone.View.extend({
		initialize : function(options) {
			this.buses = new Buses();
			this.chargers = new Chargers();
			this.simParams = options.simParams;
		},
		
		setParams: function(params) {
			if(!params) {
				return;
			}
			
			if(params.date) {
				this.$('.simulation-date').val(params.date);
			}
			
			if(params.charger) {
				this.$('select.charger-select').val(params.charger);
			}
			
			if(params.busType) {
				this.$('select.bus-select').val(params.busType.id);
			}
			
			if(params.minChargingTime) {
				this.$('.charging-time').slider('value', params.minChargingTime / 60);
				this.$('.time-label').text(params.minChargingTime/60 + ' minutes');
			}
			
			this.$('.selectpicker').selectpicker('refresh');
		},
		
		getParams: function() {
			return {
				date: this.$('.simulation-date').val(),
				charger: this.$('.charger-select').val(),
				busType: this.buses.get(this.$('.bus-select').val()),
				minChargingTime: this.$('.charging-time').slider('value') * 60
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
					max: 60,
					step: 1,
					value: 10,
					slide: function(event, ui) {
						self.$('.time-label').text(ui.value + ' minutes');
					}
				});
				
				self.setParams(self.simParams);
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