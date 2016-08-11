/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * This class contains all global js functionality of osm, that is not necessary for above-the-fold content.
 * This script is loaded after window.load event.
 *
 * This script Calls custom Event PostLoadedScriptArrived. An event handler is registered in _common/_postloadInitializedJS.gsp
 * which publishes PostLoaded functionality under namespace POSTLOADED by instantiate an instance of class PostLoaded.
 *
 * @param dataFromGsp This contains all init data and is filled in _common/_postloadInitializedJS.gsp
 * @constructor
 */
//= require spin/spin.min.js
//= require_self
function PostLoaded(dataFromGsp){

    this.getSmallSpinner = function(color, relativePositionTop, relativePositionLeft){
        return this.getSpinner(color, 5, 1.5, 4, relativePositionTop, relativePositionLeft)
    };
    this.getLargeSpinner = function(color, relativePositionTop, relativePositionLeft){
        return this.getSpinner(color, 20, 10, 30, relativePositionTop, relativePositionLeft)
    };

    /**
     * Appends a spinner with color color to parent html element.
     * See http://fgnass.github.io/spin.js/ for options.
     *
     * @param parentElement
     *          Html element the spinner will be added to.
     * @param color
     *          Color of the spinner
     * @param lineLength
     *          Radial length of single lines in spinner.
     * @param lineWidth
     *          Width of single spinner lines.
     * @param innerRadius
     *          Inner radius of spinner.
     */
    this.getSpinner = function(color, lineLength, lineWidth, innerRadius, relativePositionTop, relativePositionLeft){
        var opts = {
            lines: 18,              // The number of lines to draw
            length: lineLength,     // The length of each line
            width: lineWidth,       // The line thickness
            radius: innerRadius,    // The radius of the inner circle
            corners: 1,             // Corner roundness (0..1)
            rotate: 0,              // The rotation offset
            direction: 1,           // 1: clockwise, -1: counterclockwise
            color: color,           // #rgb or #rrggbb or array of colors
            opacity: 0.10,          // opacity
            speed: 1,               // Rounds per second
            trail: 60,              // Afterglow percentage
            shadow: false,          // Whether to render a shadow
            hwaccel: false,         // Whether to use hardware acceleration
            className: 'spinner',   // The CSS class to assign to the spinner
            zIndex: 2e9,            // The z-index (defaults to 2000000000)
            top: relativePositionTop,               // Top position relative to parent in px
            left: relativePositionLeft               // Left position relative to parent in px
        };
        return new Spinner(opts).spin();
    }

}

$('.dropdown-toggle').dropdown();
fireWindowEvent('PostLoadedScriptArrived');