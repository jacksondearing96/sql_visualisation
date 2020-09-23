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

const containerWindowWidthRatio = 0.9;
const containerWindowHeightRatio = 0.9;

const loggingCountThreshold = 50;

$(document).ready(() => {
  setContainerDimensions();
  setContainerScrollPosition();
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

function setContainerDimensions() {
  $("#container").css("width", $(window).width() * containerWindowWidthRatio);
  $("#container").css("height", $(window).height() * containerWindowHeightRatio);
}

function setContainerScrollPosition() {
  $("#container").scrollTop(canvasHeight / 2 - 100);
  $("#container").scrollLeft(canvasWidth / 2 - 100);
}

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

function getLabelX(node) {
  return getNodeX(node) + labelPaddingHorizontal;
}

function getLabelY(node) {
  return getNodeY(node) + columnHeight / 2 + labelOffsetToReachCenter;
}

function getLinkSourceX(link) {
  if (isTopLevelId(link.source.id)) {
    return (
      getNodeX(link.source) + calculateTextWidthWithPadding(link.source.name)
    );
  }
  return getNodeX(link.source) + calculateNodeWidth(link.source);
}

function getLinkSourceY(link) {
  if (isTopLevelId(link.source.id)) {
    return getNodeY(link.source) + calculateNodeHeight(link.source) / 2;
  }
  return getNodeY(link.source) + columnHeight / 2;
}

function getLinkTargetX(link) {
  return getNodeX(link.target);
}

function getLinkTargetY(link) {
  if (isTopLevelId(link.target.id)) {
    return getNodeY(link.target) + calculateNodeHeight(link.target) / 2;
  }
  return getNodeY(link.target) + columnHeight / 2;
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

function setGroupClasses(node) {
  return node.group + isTopLevelNode(node) ? " top-level-node" : " column";
}

function columnOfLabel(label) {
  if (!label.id.includes("label-")) error("Invalid label ID: " + label.id);
  return label.id.split("label-")[1];
}

function highlightColumns(columns) {
  $(columns).attr({
    fill: columnHighlightBackgroundColor,
    opacity: highlightOpacity
  });
}

function highlightLabels(labels) {
  $(labels).attr("fill", labelHighlightTextColor);
}

function highlightLinks(links) {
  $(links).attr({
    stroke: linkHighlightColor,
    fill: linkHighlightColor,
    "stroke-width": linkHighlightWidth
  });
}

function unHighlightColumns(columns) {
  columns
    .attr("fill", columnDefaultBackgroundColor)
    .filter((index, column) => isTopLevelId(column.id))
    .attr("opacity", topLevelNodeOpacity);
}

function unHighlightLabels(labels) {
  $(labels)
    .filter((index, label) => isTopLevelId(columnOfLabel(label)))
    .attr("fill", tableDefaultTextColor);
}

function unHighlightLinks(links) {
  $(links).attr({
    stroke: linkDefaultColor,
    fill: linkDefaultColor,
    "stroke-width": linkDefaultWidth
  });
}

function highlightIds(ids) {
  highlightColumns(
    $("rect").filter((index, column) => ids.includes(column.id))
  );

  highlightLabels(
    $(".label").filter((index, label) => ids.includes(columnOfLabel(label)))
  );

  highlightLinks($(".link").filter((index, link) => ids.includes(link.id)));
}

function unHighlightIds(ids) {
  unHighlightColumns(
    $("rect").filter((index, column) => ids.includes(column.id))
  );

  unHighlightLabels(
    $(".label").filter((index, label) => ids.includes(columnOfLabel(label)))
  );

  unHighlightLinks($(".link").filter((index, link) => ids.includes(link.id)));
}

function linkMouseOver(link) {
  highlightIds([
    link.id,
    ...getAllSourceSiblings(link.source.id),
    ...getAllTargetSiblings(link.target.id)
  ]);
}

function linkMouseOut(link) {
  unHighlightIds([
    link.id,
    ...getAllSourceSiblings(link.source.id),
    ...getAllTargetSiblings(link.target.id)
  ]);
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
  links.forEach((link) => {
    getNodeById(link.source).outgoing.push(link);
    getNodeById(link.target).incoming.push(link);
  });
}

allocateIncomingAndOutgoingLinks();

function getAllChildColumnIdsFromTopLevelId(id) {
  return nodes
    .filter(
      (node) => node.group === getNodeById(id).group && node.type === columnType
    )
    .map((node) => node.id);
}

function getAllSourceSiblings(id) {
  let column = getNodeById(id);

  let sourceColumnIds = [];
  for (let incomingLink of column.incoming) {
    sourceColumnIds.push(...getAllSourceSiblings(incomingLink.source.id));
  }
  if (isTopLevelId(id)) {
    let allColumnsIds = getAllChildColumnIdsFromTopLevelId(id);
    for (let columnId of allColumnsIds) {
      sourceColumnIds.push(...getAllSourceSiblings(columnId));
    }
  }

  let sourceSiblings = [];
  sourceSiblings.push(
    ...[id, ...sourceColumnIds, ...column.incoming.map((link) => link.id)]
  );
  return sourceSiblings;
}

function getAllTargetSiblings(id) {
  let column = getNodeById(id);

  let targetColumnIds = [];
  for (let outgoingLink of column.outgoing) {
    targetColumnIds.push(...getAllTargetSiblings(outgoingLink.target.id));
  }
  if (isTopLevelId(id)) {
    let allColumnsIds = getAllChildColumnIdsFromTopLevelId(id);
    for (let columnId of allColumnsIds) {
      targetColumnIds.push(...getAllTargetSiblings(columnId));
    }
  }

  let targetSiblings = [];
  targetSiblings.push(
    ...[id, ...targetColumnIds, ...column.outgoing.map((link) => link.id)]
  );
  return targetSiblings;
}

function getAllLineageSiblingIds(id) {
  let siblingIds = [
    id,
    ...getAllSourceSiblings(id),
    ...getAllTargetSiblings(id)
  ];

  siblingIds = siblingIds.filter(
    (id, index) => siblingIds.indexOf(id) === index
  );

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
  .attr("class", (d) => setGroupClasses(d))
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
  .on("mouseover", (d) => linkMouseOver(d))
  .on("mouseout", (d) => linkMouseOut(d));

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

  lables.attr("x", (d) => getLabelX(d)).attr("y", (d) => getLabelY(d));

  linkSelection
    .attr("x1", (d) => getLinkSourceX(d))
    .attr("y1", (d) => getLinkSourceY(d))
    .attr("x2", (d) => getLinkTargetX(d))
    .attr("y2", (d) => getLinkTargetY(d));
}
