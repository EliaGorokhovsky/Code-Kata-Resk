let screen;
let renderer;

/**
 * Gets elements that have been loaded
 */
window.onload = () => {
	screen = document.getElementById("screen");
	renderer = screen.getContext("2d");
	renderer.strokeStyle = "#000";

	function resizeProcedure() {
		if (window.innerWidth / 25 < window.innerHeight / 25) {
			screen.width = window.innerWidth;
			screen.height = window.innerWidth;
		} else {
			screen.height = window.innerHeight;
			screen.width = window.innerHeight;
		}
	}

	resizeProcedure();
	window.onresize = resizeProcedure;
};

/**
 * Repeatedly draws the game
 */
window.setInterval(() => {

	//TODO: draw game

}, 1000);
