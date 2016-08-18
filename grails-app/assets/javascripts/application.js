// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
// Jquery has to be the first, because others like bootstrap depend on it
//= require_tree bower_components/jquery
//= require_tree bower_components
//= require postload/PostLoader
//= require_self

if (typeof jQuery !== 'undefined') {
    (function($) {
        $('#spinner').ajaxStart(function() {
            $(this).fadeIn();
        }).ajaxStop(function() {
            $(this).fadeOut();
        });
    })(jQuery);
}

function fireWindowEvent(eventName){
    var event = document.createEvent('Event');
    event.initEvent(eventName, true, true);
    window.dispatchEvent(event);
}

$( document ).ready( function(){

    $('ul.nav li.dropdown').hover(
        function() { $(this).children('.dropdown-menu').stop(true, true).delay(100).fadeIn(); },
        function() { $(this).children('.dropdown-menu').stop(true, true).delay(100).fadeOut(); }
    );
    $('li.dropdown-submenu').hover(
        function() { $(this).children('ul').stop(true, true).delay(100).fadeIn(); },
        function() { $(this).children('ul').stop(true, true).delay(100).fadeOut(); }
    );

});
