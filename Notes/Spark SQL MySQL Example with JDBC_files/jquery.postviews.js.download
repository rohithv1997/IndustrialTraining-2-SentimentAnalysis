/*

	01. - Post Views

*/

/* jshint -W099 */
/* global jQuery:false, stData:false */

var pv = jQuery.noConflict();

pv(function(){

	'use strict';

/*==01.==========================================

 	P O S T   V I E W S
	Number of views

===============================================*/

/*

	1 - POST VIEWS

		1.1 - Ajax

*/

	/*-------------------------------------------
		1.1 - Ajax
	-------------------------------------------*/

	var
		st_postviews_ajax = function() {

			if ( stData[5] !== undefined ) {

				var
					$id = stData[4] ? stData[4] : 1;

					pv.ajax({
						type : 'GET',
						data : {
							id : $id,
						},
						dataType : 'html',
						url : stData[5] + '/stkit/components/postviews/functions/postviews-ajax.php',
						success	: function(data) {
							pv('.post-short-info .ico16-views').stop(true, false).animate({ 'opacity': 0 }, 500, function(){ pv(this).text(data).stop(true, false).animate({ 'opacity': 1 }, 500); });
						}
					});

			}

		};

		if ( pv('.post-short-info .ico16-views').length ) {
			setTimeout( st_postviews_ajax, 3000 ); }


});