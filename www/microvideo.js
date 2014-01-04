var exec = require('cordova/exec');

module.exports = {

	record: function(successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'MicroVideo', 'record', []);
	}

};
