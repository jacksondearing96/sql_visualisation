/* eslint-disable no-undef */

function error(message) {
  console.error(message);
  throw new Error(message);
}

const canvasWidth = 600;
const canvasHeight = 600;

const columnHeight = 20;

const fontSize = 15;
const fontSizeToCharacterWidthRatio = 0.6;

const labelPaddingHorizontal = 15;
const labelOffsetToReachCenter = 4;

const tablePaddingHorizontal = 10;
const tablePaddingVertical = 25;

var nodes = [
  { color: "red", group: "1", order: "0", tag: "column" },
  { color: "orange", group: "1", order: "1", tag: "column" },
  { color: "green", group: "1", order: "2", tag: "column" },
  { color: "grey", group: "1", tag: "table" },

  { color: "yellow", group: "3", order: "0", tag: "column" },
  { color: "grey", group: "3", tag: "table" },

  { color: "blue", group: "2", order: "0", tag: "column" },
  { color: "purple", group: "2", order: "1", tag: "column" },
  { color: "grey", group: "2", tag: "table" }
];

var links = [
  { source: "yellow", target: "red", tag: "" },
  { source: "green", target: "blue", tag: "" }
];

function countColumns(group) {
  let count = 0;
  for (let node of nodes) {
    if (node.group === group) ++count;
  }
  return count;
}

function calculateTextWidth(text) {
  let numberOfCharacters = text.length;
  let width = fontSize * fontSizeToCharacterWidthRatio * numberOfCharacters;
  return width + 2 * labelPaddingHorizontal;
}

function maxColumnWidthForGroup(group) {
  let maxWidth = 0;
  for (let node of nodes) {
    let nodeWidth = calculateTextWidth(node.color);
    if (node.group === group && node.tag === "column" && nodeWidth > maxWidth) {
      maxWidth = nodeWidth;
    }
  }
  return maxWidth;
}

function calculateNodeWidth(node) {
  let maxColumnWidth = maxColumnWidthForGroup(node.group);
  if (node.tag === "table") return maxColumnWidth + 2 * tablePaddingHorizontal;
  return maxColumnWidth;
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
  return y + parseInt(node.order, 10) * columnHeight + tablePaddingVertical;
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
  .attr("opacity", (d) => {
    if (d.tag === "table") return 0.2;
    return 1;
  })
  .call(d3.drag().on("start", dragStart).on("drag", drag).on("end", dragEnd));

// Add the arrowhead marker definition to the svg element
const arrowSize = 10;
const markerBoxWidth = arrowSize;
const markerBoxHeight = arrowSize;
const refX = markerBoxWidth / 2;
const refY = markerBoxHeight / 2;
const arrowPoints = [
  [0, 0],
  [0, arrowSize],
  [arrowSize, arrowSize / 2]
];

// Definition for arrow head.
svg
  .append("defs")
  .append("marker")
  .attr("id", "arrow")
  .attr("viewBox", [0, 0, markerBoxWidth, markerBoxHeight])
  .attr("refX", refX)
  .attr("refY", refY)
  .attr("markerWidth", markerBoxWidth)
  .attr("markerHeight", markerBoxHeight)
  .attr("orient", "auto-start-reverse")
  .append("path")
  .attr("d", d3.line()(arrowPoints))
  .attr("stroke", "grey")
  .attr("fill", "grey");

var linkSelection = svg
  .selectAll("line")
  .data(links)
  .enter()
  .append("line")
  .attr("stroke", "grey")
  .attr("fill", "none")
  .attr("marker-end", "url(#arrow)")
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
  .force("nodes", d3.forceManyBody().strength(-30))
  .force(
    "links",
    d3
      .forceLink(links)
      .id((d) => d.color)
      .strength(1)
  )
  .on("tick", ticked);

function ticked() {
  nodeSelection.attr("x", (d) => getNodeX(d)).attr("y", (d) => getNodeY(d));

  lables
    .attr("x", (d) => {
      return getNodeX(d) + labelPaddingHorizontal;
    })
    .attr(
      "y",
      (d) => getNodeY(d) + columnHeight / 2 + labelOffsetToReachCenter
    );

  linkSelection
    .attr("x1", (d) => {
      let columnX = getNodeX(d.source);

      return columnX + maxColumnWidthForGroup(d.source.group);
    })
    .attr("y1", (d) => {
      let columnY = getNodeY(d.source);
      return columnY + columnHeight / 2;
    })
    .attr("x2", (d) => {
      let columnX = getNodeX(d.target);
      return columnX;
    })
    .attr("y2", (d) => {
      let columnY = getNodeY(d.target);
      return columnY + columnHeight / 2;
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
