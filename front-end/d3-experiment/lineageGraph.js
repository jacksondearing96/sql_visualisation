/* eslint-disable no-undef */

const canvasWidth = 5000;
const canvasHeight = 2500;
const scrollIncrementWidthToInitInCenter = -500;
const scrollIncrementHeightToInitInCenter = -300;

const nodeForceStrength = -30;
const dragEndAlphaTarget = 0;
const dragStartAlphaTarget = 0.5;

const offWhite = "rgb(200,200,200)";

const fontSize = 15;
const fontFamily = 'Cutive Mono';
const fontSizeToCharacterWidthRatio = 0.6;

const columnHeight = 20;
const columnDefaultBackgroundColor = "dodgerblue";
const columnHighlightBackgroundColor = "crimson";
const columnDefaultTextColor = offWhite;
const columnDefaultOpacity = 1;
const columnFontWeight = "normal";

const linkDefaultColor = "grey";
const linkHighlightColor = "crimson";
const linkFill = "none";
const linkDefaultWidth = "1";
const linkHighlightWidth = "5";
const linkPreferredDistance = 50;
const linkStrength = 0.1;

const labelPaddingHorizontal = 15;
const labelOffsetToReachCenter = 4;
const labelHighlightTextColor = offWhite;

const topLevelNodePaddingHorizontal = 10;
const topLevelNodePaddingVertical = 25;
const topLevelNodeDefaultBackgroundColor = offWhite;
const topLevelNodeDefaultTextColor = offWhite;
const topLevelNodeBackgroundHighlightColor = "crimson";
const topLevelNodeHighlightOpacity = 0.6;
const topLevelNodeDefaultOpacity = 0.2;
const topLevelNodeDefaultFontWeight = "bold";

const tableType = "TABLE";
const viewType = "VIEW";
const columnType = "COLUMN";

const idDelimiter = "::";

const containerWindowWidthRatio = 0.95;
const containerWindowHeightRatio = 0.85;

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
  $("#container").scrollTop(canvasHeight / 2 + scrollIncrementHeightToInitInCenter);
  $("#container").scrollLeft(canvasWidth / 2 + scrollIncrementWidthToInitInCenter);
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
      maxColumnWidth + 2 * topLevelNodePaddingHorizontal,
      calculateTextWidthWithPadding(node.name)
    );
  }
  return Math.max(maxColumnWidth, topLevelWidth - 2 * topLevelNodePaddingHorizontal);
}

function calculateNodeHeight(node) {
  return isTopLevelNode(node)
    ? columnHeight * countColumnsInGroup(node.group) + topLevelNodePaddingVertical
    : columnHeight;
}

function getParentTable(node) {
  let parent = nodes.filter((n) => isTopLevelNode(n) && n.group === node.group);
  if (parent.length !== 1) error("Could not find parent table.");
  return parent[0];
}

function getNodeX(node) {
  if (isTopLevelNode(node)) return node.x;
  return getNodeX(getParentTable(node)) + topLevelNodePaddingHorizontal;
}

function getNodeY(node) {
  if (isTopLevelNode(node)) return node.y;
  let parentY = getNodeY(getParentTable(node));
  return (
    parentY + parseInt(node.order, 10) * columnHeight + topLevelNodePaddingVertical
  );
}

function getLabelX(node) {
  return getNodeX(node) + labelPaddingHorizontal;
}

function getLabelY(node) {
  if (isTopLevelNode(node)) return getNodeY(node) + columnHeight / 2 + labelOffsetToReachCenter + 5;
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
    ? topLevelNodeDefaultBackgroundColor
    : columnDefaultBackgroundColor;
}

function determineNodeOpacity(node) {
  return isTopLevelNode(node) ? topLevelNodeDefaultOpacity : columnDefaultOpacity;
}

function determineTextColor(node) {
  return isTopLevelNode(node) ? topLevelNodeDefaultTextColor : columnDefaultTextColor;
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
  });
}

function highlightTopLevelNodes(nodes) {
  $(nodes).attr({
    fill: topLevelNodeBackgroundHighlightColor,
    opacity: topLevelNodeHighlightOpacity
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
  columns.attr("fill", columnDefaultBackgroundColor);
}

function unHighlightTopLevelNodes(nodes) {
  nodes
    .attr("fill", topLevelNodeDefaultBackgroundColor)
    .attr("opacity", topLevelNodeDefaultOpacity);
}

function unHighlightLabels(labels) {
  $(labels)
    .filter((index, label) => isTopLevelId(columnOfLabel(label)))
    .attr("fill", topLevelNodeDefaultTextColor);
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
    $("rect").filter((index, column) => !isTopLevelId(column.id) && ids.includes(column.id))
  );

  highlightTopLevelNodes(
    $("rect").filter((index, node) => isTopLevelId(node.id) && ids.includes(node.id))
  );

  highlightLabels(
    $(".label").filter((index, label) => ids.includes(columnOfLabel(label)))
  );

  highlightLinks($(".link").filter((index, link) => ids.includes(link.id)));
}

function unHighlightIds(ids) {
  unHighlightColumns(
    $("rect").filter((index, column) => !isTopLevelId(column.id) && ids.includes(column.id))
  );

  unHighlightTopLevelNodes(
    $("rect").filter((index, node) => isTopLevelId(node.id) && ids.includes(node.id))
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
    (siblingId, index) => siblingIds.indexOf(siblingId) === index
  );

  return siblingIds;
}

function dragStart(d) {
  simulation.alphaTarget(dragStartAlphaTarget).restart();
  d.fx = d.x;
  d.fy = d.y;
}

function drag(d) {
  d.fx = d3.event.x;
  d.fy = d3.event.y;
}

function dragEnd(d) {
  simulation.alphaTarget(dragEndAlphaTarget);
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
  .attr("fill", linkFill)
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
  .attr("font-family", fontFamily)
  .attr("font-weight", (d) =>
    isTopLevelNode(d) ? topLevelNodeDefaultFontWeight : columnFontWeight
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
