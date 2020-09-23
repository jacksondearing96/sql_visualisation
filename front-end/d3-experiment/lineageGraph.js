/* eslint-disable no-undef */

const canvasWidth = 2000;
const canvasHeight = 1000;

const nodeForceStrength = -30;

const columnHeight = 20;
const columnDefaultBackgroundColor = "dodgerblue";
const columnHighlightBackgroundColor = "red";
const columnDefaultTextColor = "white";
const columnOpacity = 1;
const columnFontWeight = "normal";

const linkDefaultColor = "grey";
const linkHighlightColor = "red";
const linkDefaultWidth = "1";
const linkHighlightWidth = "5";
const linkPreferredDistance = 50;
const linkStrength = 0.1;

const fontSize = 15;
const fontSizeToCharacterWidthRatio = 0.6;

const labelPaddingHorizontal = 15;
const labelOffsetToReachCenter = 4;
const labelHighlightTextColor = "white";

// TODO: this should be calculated based on the table width.
const tablePaddingHorizontal = 10;
const tablePaddingVertical = 25;
const tableDefaultBackgroundColor = "blue";
const tableDefaultTextColor = "black";

const topLevelNodeOpacity = 0.2;
const topLevelNodeFontWeight = "bold";

const highlightOpacity = 1;

const tableType = "TABLE";
const viewType = "VIEW";
const columnType = "COLUMN";

const idDelimiter = "::";

const loggingCountThreshold = 50;

$(document).ready(() => {
  $("#container").css("width", $(window).width() * 0.9);
  $("#container").css("height", $(window).height() * 0.9);
});

function error(message) {
  console.error(message);
  throw new Error(message);
}

let logCount = 0;
function log(message) {
  if (logCount < loggingCountThreshold) console.log(message);
  if (logCount === loggingCountThreshold)
    console.error("Logging capacity exceeded!");
  ++logCount;
}

var nodes = [
  {
    id: "%(crm)s_task::",
    name: "%(crm)s_task",
    group: "%(crm)s_task",
    incoming: [],
    outgoing: [],
    type: "TABLE"
  },
  {
    id: "%(crm)s_task::accountid",
    name: "accountid",
    group: "%(crm)s_task",
    order: "0",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },
  {
    id: "%(crm)s_task::ownerid",
    name: "ownerid",
    group: "%(crm)s_task",
    order: "1",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },
  {
    id: "%(crm)s_task::status",
    name: "status",
    group: "%(crm)s_task",
    order: "2",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },
  {
    id: "%(crm)s_task::activitydate",
    name: "activitydate",
    group: "%(crm)s_task",
    order: "3",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },

  {
    id: "customer_insight::",
    name: "customer_insight",
    group: "customer_insight",
    incoming: [],
    outgoing: [],
    type: "TABLE"
  },
  {
    id: "customer_insight::acct_sf_id",
    name: "acct_sf_id",
    group: "customer_insight",
    order: "0",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },
  {
    id: "customer_insight::user_sf_id",
    name: "user_sf_id",
    group: "customer_insight",
    order: "1",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },

  {
    id: "note_count_by_agent::",
    name: "note_count_by_agent",
    group: "note_count_by_agent",
    incoming: [],
    outgoing: [],
    type: "VIEW"
  },
  {
    id: "note_count_by_agent::acct_sf_id",
    name: "acct_sf_id",
    group: "note_count_by_agent",
    order: "0",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },
  {
    id: "note_count_by_agent::user_sf_id",
    name: "user_sf_id",
    group: "note_count_by_agent",
    order: "1",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },
  {
    id: "note_count_by_agent::cnt",
    name: "cnt",
    group: "note_count_by_agent",
    order: "2",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  },
  {
    id: "note_count_by_agent::*",
    name: "*",
    group: "note_count_by_agent",
    order: "3",
    incoming: [],
    outgoing: [],
    type: "COLUMN"
  }
];

var links = [
  {
    source: "%(crm)s_task::ownerid",
    target: "customer_insight::acct_sf_id",
    id: "link0"
  },
  {
    source: "customer_insight::acct_sf_id",
    target: "note_count_by_agent::acct_sf_id",
    id: "link1"
  },
  {
    source: "customer_insight::user_sf_id",
    target: "note_count_by_agent::user_sf_id",
    id: "link2"
  },
  {
    source: "customer_insight::",
    target: "note_count_by_agent::*",
    id: "link3"
  },
  {
    source: "%(crm)s_task::status",
    target: "customer_insight::",
    id: "link4"
  }
];

function getNodeById(id) {
  for (let node of nodes) {
    if (node.id === id) return node;
  }
  error("Couldn't find node with id: " + id);
}

function isTopLevelNode(node) {
  return node.type === tableType || node.type === viewType;
}

function isTopLevelId(id) {
  return id.endsWith(idDelimiter);
}

function countColumnsInGroup(group) {
  return nodes.filter((node) => node.group === group).length;
}

function calculateTextWidth(text) {
  let numberOfCharacters = text.length;
  let width = fontSize * fontSizeToCharacterWidthRatio * numberOfCharacters;
  return width;
}

function calculateTextWidthWithPadding(text) {
  return calculateTextWidth(text) + 2 * labelPaddingHorizontal;
}

Array.prototype.max = function () {
  return Math.max.apply(null, this);
};

function maxColumnWidthForGroup(group) {
  return nodes
    .filter((node) => node.group === group)
    .filter((node) => node.type === columnType)
    .map((node) => calculateTextWidthWithPadding(node.name))
    .max();
}

function topLevelWidthForGroup(group) {
  return calculateTextWidthWithPadding(
    nodes.filter((node) => isTopLevelNode(node) && node.group === group)[0].name
  );
}

function calculateNodeWidth(node) {
  let maxColumnWidth = maxColumnWidthForGroup(node.group);
  let topLevelWidth = topLevelWidthForGroup(node.group);

  if (isTopLevelNode(node)) {
    return Math.max(
      maxColumnWidth + 2 * tablePaddingHorizontal,
      calculateTextWidthWithPadding(node.name)
    );
  }
  return Math.max(maxColumnWidth, topLevelWidth - 2 * tablePaddingHorizontal);
}

function calculateNodeHeight(node) {
  return isTopLevelNode(node)
    ? columnHeight * countColumnsInGroup(node.group) + tablePaddingVertical
    : columnHeight;
}

function getParentTable(node) {
  let parent = nodes.filter((n) => isTopLevelNode(n) && n.group === node.group);
  if (parent.length !== 1) error("Could not find parent table.");
  return parent[0];
}

function getNodeX(node) {
  if (isTopLevelNode(node)) return node.x;
  return getNodeX(getParentTable(node)) + tablePaddingHorizontal;
}

function getNodeY(node) {
  if (isTopLevelNode(node)) return node.y;
  let parentY = getNodeY(getParentTable(node));
  return (
    parentY + parseInt(node.order, 10) * columnHeight + tablePaddingVertical
  );
}

function determineNodeColor(node) {
  return isTopLevelNode(node)
    ? tableDefaultBackgroundColor
    : columnDefaultBackgroundColor;
}

function determineNodeOpacity(node) {
  return isTopLevelNode(node) ? topLevelNodeOpacity : columnOpacity;
}

function determineTextColor(node) {
  return isTopLevelNode(node) ? tableDefaultTextColor : columnDefaultTextColor;
}

function setGroupClass(node) {
  return node.group + isTopLevelNode(node) ? " top-level-node" : " column";
}

function highlightIds(ids) {
  let columns = $("rect");
  for (let column of columns) {
    if (ids.includes(column.id)) {
      $(column).attr({
        fill: columnHighlightBackgroundColor,
        opacity: highlightOpacity
      });
    }
  }

  let labels = $(".label");
  for (let label of labels) {
    if (!label.id.includes("label-")) error("Invalid label ID: " + label.id);
    let associatedColumnId = label.id.split("label-")[1];
    if (ids.includes(associatedColumnId)) {
      $(label).attr("fill", labelHighlightTextColor);
    }
  }

  let links = $(".link");
  for (let link of links) {
    if (ids.includes(link.id)) {
      $(link).attr({
        stroke: linkHighlightColor,
        fill: linkHighlightColor,
        "stroke-width": linkHighlightWidth
      });
      $(".arrow").attr("stroke-width", linkDefaultWidth);
    }
  }
}

function unHighlightIds(ids) {
  let columns = $("rect");
  for (let column of columns) {
    if (ids.includes(column.id)) {
      $(column).attr("fill", columnDefaultBackgroundColor);
      if (isTopLevelId(column.id)) {
        $(column).attr("opacity", topLevelNodeOpacity);
      }
    }
  }

  let labels = $(".label");
  for (let label of labels) {
    if (!label.id.includes("label-")) error("Invalid label ID: " + label.id);
    let associatedColumnId = label.id.split("label-")[1];
    if (ids.includes(associatedColumnId) && isTopLevelId(associatedColumnId)) {
      $(label).attr("fill", tableDefaultTextColor);
    }
  }

  let links = $(".link");
  for (let link of links) {
    if (ids.includes(link.id)) {
      $(link).attr("stroke", linkDefaultColor);
      $(link).attr("fill", linkDefaultColor);
      $(link).attr("stroke-width", linkDefaultWidth);
    }
  }
}

function linkMouseOver(id) {
  highlightIds(getAllLineageSiblingIds(id));
}

function linkMouseOut(id) {
  unHighlightIds(getAllLineageSiblingIds(id));
}

function columnMouseOver(id) {
  highlightIds(getAllLineageSiblingIds(id));
}

function columnMouseOut(id) {
  unHighlightIds(getAllLineageSiblingIds(id));
}

function labelMouseOver() {
  let columnId = $(this.parentElement).find("rect").attr("id");
  columnMouseOver(columnId);
}

function labelMouseOut() {
  let columnId = $(this.parentElement).find("rect").attr("id");
  columnMouseOut(columnId);
}

function allocateIncomingAndOutgoingLinks() {
  for (let link of links) {
    getNodeById(link.source).outgoing.push(link);
    getNodeById(link.target).incoming.push(link);
  }
}

allocateIncomingAndOutgoingLinks();

function getAllChildColumnIdsFromTopLevelId(id) {
  let topLevelNode = getNodeById(id);
  let group = topLevelNode.group;
  let columnIds = [];
  for (let node of nodes) {
    if (node.group === group && node.type === columnType) {
      columnIds.push(node.id);
    }
  }
  return columnIds;
}

function getAllSourceSiblings(id) {
  let column = getNodeById(id);

  let sourceColumnIds = [];
  for (let incomingLink of column.incoming) {
    sourceColumnIds.push(...getAllSourceSiblings(incomingLink.source.id));
  }
  if (isTopLevelNode(getNodeById(id))) {
    let allColumnsIds = getAllChildColumnIdsFromTopLevelId(id);
    for (let columnId of allColumnsIds) {
      sourceColumnIds.push(...getAllSourceSiblings(columnId));
    }
  }

  let sourceSiblings = [];
  sourceSiblings.push(id);
  sourceSiblings.push(...sourceColumnIds);
  sourceSiblings.push(...column.incoming.map((link) => link.id));
  return sourceSiblings;
}

function getAllTargetSiblings(id) {
  let column = getNodeById(id);

  let targetColumnIds = [];
  for (let outgoingLink of column.outgoing) {
    targetColumnIds.push(...getAllTargetSiblings(outgoingLink.target.id));
  }
  if (isTopLevelNode(getNodeById(id))) {
    let allColumnsIds = getAllChildColumnIdsFromTopLevelId(id);
    for (let columnId of allColumnsIds) {
      targetColumnIds.push(...getAllTargetSiblings(columnId));
    }
  }

  let targetSiblings = [];
  targetSiblings.push(id);
  targetSiblings.push(...targetColumnIds);
  targetSiblings.push(...column.outgoing.map((link) => link.id));
  return targetSiblings;
}

function getAllLineageSiblingIds(id) {
  let siblingIds = [];
  siblingIds.push(id);
  siblingIds.push(...getAllSourceSiblings(id));
  siblingIds.push(...getAllTargetSiblings(id));

  // Filter out duplicate elements.
  siblingIds = siblingIds.filter(
    (id, index) => siblingIds.indexOf(id) === index
  );

  highlightIds(siblingIds);
  return siblingIds;
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
  .attr("fill", (d) => determineNodeColor(d))
  .attr("opacity", (d) => determineNodeOpacity(d))
  .attr("class", (d) => setGroupClass(d))
  // note: can bubble up this ID to the 'g' element if req. Put here for conveinence now.
  .attr("id", (d) => d.id)
  .call(d3.drag().on("start", dragStart).on("drag", drag).on("end", dragEnd))
  .on("mouseover", (d) => columnMouseOver(d.id))
  .on("mouseout", (d) => columnMouseOut(d.id));

var linkSelection = svg
  .selectAll("line")
  .data(links)
  .enter()
  .append("line")
  .attr("stroke", linkDefaultColor)
  .attr("fill", "none")
  .attr("stroke-width", linkDefaultWidth)
  .attr("id", (d) => d.id)
  .attr("class", "link")
  .on("mouseover", (d) => linkMouseOver(d.source.id))
  .on("mouseout", (d) => linkMouseOut(d.source.id));

var lables = svg
  .selectAll("g")
  .append("text")
  .attr("fill", (d) => determineTextColor(d))
  .attr("font-size", fontSize)
  .attr("font-family", "courier new")
  .attr("font-weight", (d) =>
    isTopLevelNode(d) ? topLevelNodeFontWeight : columnFontWeight
  )
  .attr("class", "label")
  .attr("id", (d) => "label-" + d.id)
  .text((d) => d.name)
  .on("mouseover", labelMouseOver)
  .on("mouseout", labelMouseOut);

var simulation = d3.forceSimulation(nodes);

simulation
  .force("center", d3.forceCenter(canvasWidth / 2, canvasHeight / 2))
  .force("nodes", d3.forceManyBody().strength(nodeForceStrength))
  .force(
    "links",
    d3
      .forceLink(links)
      .id((d) => d.id)
      .distance(linkPreferredDistance)
      .strength(linkStrength)
  )
  .on("tick", ticked);

function ticked() {
  nodeSelection.attr("x", (d) => getNodeX(d)).attr("y", (d) => getNodeY(d));

  lables
    .attr("x", (d) => getNodeX(d) + labelPaddingHorizontal)
    .attr(
      "y",
      (d) => getNodeY(d) + columnHeight / 2 + labelOffsetToReachCenter
    );

  linkSelection
    .attr("x1", (d) => {
      if (isTopLevelId(d.source.id)) {
        return (
          getNodeX(d.source) + calculateTextWidthWithPadding(d.source.name)
        );
      }
      let columnX = getNodeX(d.source);
      return columnX + calculateNodeWidth(d.source);
    })
    .attr("y1", (d) => {
      if (isTopLevelId(d.source.id)) {
        return getNodeY(d.source) + calculateNodeHeight(d.source) / 2;
      }
      let columnY = getNodeY(d.source);
      return columnY + columnHeight / 2;
    })
    .attr("x2", (d) => {
      let columnX = getNodeX(d.target);
      return columnX;
    })
    .attr("y2", (d) => {
      if (isTopLevelId(d.target.id)) {
        return getNodeY(d.target) + calculateNodeHeight(d.target) / 2;
      }
      let columnY = getNodeY(d.target);
      return columnY + columnHeight / 2;
    });
}

$("#container").scrollTop(canvasHeight / 2 - 200);
$("#container").scrollLeft(canvasWidth / 2 - 100);
