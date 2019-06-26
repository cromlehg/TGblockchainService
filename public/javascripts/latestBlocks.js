var latestBlocks = (function() {
	var init = function(params) {
		var url = params.url;
		var $container = params.$container;
		var tick = function() {
			$.get(params.url).done(function(data) {
				$container.html(data);
				setTimeout(tick, 5000);
			})
		};
		tick();
	};
	return {init: init};
})();
