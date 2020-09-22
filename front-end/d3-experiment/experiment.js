const canvasWidth = 600;
const canvasHeight = 600;

const columnWidth = 70;
const columnHeight = 20;

var nodes = [
  { color: "grey", group: "1", tag: "table" },
  { color: "red", group: "1", order: "0", tag: "column" },
  { color: "orange", group: "1", order: "1", tag: "column" },
  { color: "green", group: "1", order: "2", tag: "column" },

  { color: "grey", group: "3", tag: "table" },
  { color: "yellow", group: "3", order: "0", tag: "column" },

  { color: "grey", group: "2", tag: "table" },
  { color: "blue", group: "2", order: "0", tag: "column" },
  { color: "purple", group: "2", order: "1", tag: "column" }
];

var links = [
  { source: "yellow", target: "red", tag: "" },
  { source: "green", target: "blue", tag: "" }
];


var svg = d3
  .select("svg")
  .attr("width", canvasWidth)
  .attr("height", canvasHeight);

var nodeSelection = svg
  .selectAll("rect")
  .data(nodes)
  .enter()
  .append("g")
  .append("rect")
  .attr("width", (d) => calculateNodeWidth(d))
  .attr("height", (d) => calculateNodeHeight(d))
  .attr("fill", (d) => d.color)
  .attr("transform", 100)
  .call(d3.drag().on("start", dragStart).on("drag", drag).on("end", dragEnd));

var linkSelection = svg
  .selectAll("line")
  .data(links)
  .enter()
  .append("line")
  .attr("stroke", "black")
  .attr("stroke-width", 1);

var lables = svg
  .selectAll("g")
  .append("text")
  .attr("color", "black")
  .text((d) => d.color);

var simulation = d3.forceSimulation(nodes);

simulation
  .force("center", d3.forceCenter(canvasWidth / 2, canvasHeight / 2))
  .force("nodes", d3.forceManyBody().strength(30))
  .force("charge", (node) => -30)
  .force(
    "links",
    d3
      .forceLink(links)
      .id((d) => d.color)
      .distance(200)
      .strength(0.1)
  )
  .on("tick", ticked);

function dragStart(d) {
  simulation.alphaTarget(0.5).restart();
  d.fx = d.x;
  d.fy = d.y;
}

function drag(d) {
  d.fx = d3.event.x;
  d.fy = d3.event.y;
}

function dragEnd(d) {
  simulation.alphaTarget(0);
  d.fx = null;
  d.fy = null;
}
