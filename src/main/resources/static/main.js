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
 * Repeatedly draws the game every second
 */
window.setInterval(async () => {

	const players = await JSON.parse(await (await fetch(`${window.location.href.split("?")[0]}/api/teams/order`)).text());
	const ownedTerritories = {};
	for (i in players) {
		const player = players[i];
		ownedTerritories[player] = await JSON.parse(await (await fetch(`${window.location.href.split("?")[0]}/api/teams/territories?teamColor=${player}`)).text());
	}

	const nodes = [];

	for (let i = 0; i < 25 * 25; i++) {
		nodes.push({ id: i, color: "white", numberOfTroops: 0, connections: await JSON.parse(await (await fetch(`${window.location.href.split("?")[0]}/api/board/adjacencies?id=${i}`)).text()) });
		let owner = Object.keys(ownedTerritories).find(color => ownedTerritories[color].includes(i));
		if (owner) {
			nodes[i].color = owner.toLowerCase();
			nodes[i].numberOfTroops = (await JSON.parse(await (await fetch(`${window.location.href.split("?")[0]}/api/board/troops?id=${i}`)).text())).amount;
		}
	}

	const nodeRadius = screen.width / 27 / 2 * 0.75;
	const getLocationOf = id => {
		let row = Math.floor(id / 25);
		let col = id % 25;
		return [(col + 1) * screen.width / 27, (row + 1) * screen.height / 27];
	};

	for (i in nodes) {
		const node = nodes[i];
		for (j in node.connections) {
			const connection = node.connections[j];
			renderer.beginPath();
			renderer.moveTo(getLocationOf(node.id)[0], getLocationOf(node.id)[1]);
			renderer.lineTo(getLocationOf(connection)[0], getLocationOf(connection)[1]);
			renderer.closePath();
			renderer.stroke();
		}
	}

	for (i in nodes) {
		const node = nodes[i];
		renderer.fillStyle = node.color;
		renderer.beginPath();
		renderer.arc(getLocationOf(node.id)[0], getLocationOf(node.id)[1], nodeRadius, 0, 2 * Math.PI);
		renderer.closePath();
		renderer.fill();
		renderer.stroke();
		renderer.fillStyle = "black";
		renderer.fillText(`${node.numberOfTroops}`, getLocationOf(node.id)[0] - nodeRadius / 4, getLocationOf(node.id)[1] + nodeRadius / 4, 2 * nodeRadius)
	}

}, 5000);
