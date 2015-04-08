define(['backbone'], function(Backbone){
	var ConfigModel = Backbone.Model.extend({
		url: '/api/configuration'
	});
	
	return ConfigModel;
});