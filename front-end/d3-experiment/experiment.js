/* eslint-disable no-undef */

function error(message) {
  console.error(message);
  throw new Error(message);
}

const canvasWidth = 600;
const canvasHeight = 600;

const columnWidth = 70;
const columnHeight = 20;

const fontSize = 15;
const fontSizeToCharacterWidthRatio = 0.6;

const labelPaddingHorizontal = 15;

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

function getNode(color) {
  for (let node of nodes) {
    if (color === node.color) return node;
  }
  error("Could not find node (color = " + color + ")");
}

function countColumns(group) {
  let count = 0;
  for (let node of nodes) {
    if (node.group === group) ++count;
  }
  return count;
}

function calculateNodeWidth(node) {
  if (node.tag === "table") return columnWidth + columnWidth / 3;
  return calculateTextWidth(node.color);
}

function calculateNodeHeight(node) {
  if (node.tag === "table")
    return columnHeight * (countColumns(node.group) + 1);
  return columnHeight;
}

function getParentTable(node) {
  for (let other of nodes) {
    if (other.tag === "table" && other.group === node.group) {
      return other;
    }
  }
  error("Could not find parent table.");
  return null;
}

function getNodeX(node) {
  if (node.tag === "table") return node.x;

  let x = getNodeX(getParentTable(node));
  return x + 10;
}

function getNodeY(node) {
  if (node.tag === "table") return node.y;

  let y = getNodeY(getParentTable(node));
  return y + (parseInt(node.order, 10) + 1) * columnHeight;
}

function calculateTextWidth(text) {
  let numberOfCharacters = text.length;
  let width = fontSize * fontSizeToCharacterWidthRatio * numberOfCharacters;
  return width + 2 * labelPaddingHorizontal;
}

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
  .attr("font-size", fontSize)
  .attr("font-family", "courier new")
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

function ticked() {
  nodeSelection.attr("x", (d) => getNodeX(d)).attr("y", (d) => getNodeY(d));

  const offsetToCenterText = 4;

  lables
    .attr("x", (d) => {
      return getNodeX(d) + labelPaddingHorizontal;
    })
    .attr("y", (d) => getNodeY(d) + columnHeight / 2 + offsetToCenterText);

  linkSelection
    .attr("x1", (d) => {
      let columnX = getNodeX(d.source);

      return columnX + columnWidth;
    })
    .attr("y1", (d) => {
      let columnY = getNodeY(d.source);
      return columnY + columnHeight / 2 + offsetToCenterText;
    })
    .attr("x2", (d) => {
      let columnX = getNodeX(d.target);
      return columnX;
    })
    .attr("y2", (d) => {
      let columnY = getNodeY(d.target);
      return columnY + columnHeight / 2 + offsetToCenterText;
    });
}

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
