var nodes = []
var links = []
var simulation, nodeSelection, linkSelection, labels, svg;

function generateVisualisation(graph) {
    initialiseContainer();
    initialiseGraphData(graph);
    generateForceDirectedSimulation();
    allocateInitialPositions();
    activateToggleColumnsButton();
}

function initialiseContainer() {
    $('#visualisation-container').show(600, initialiseScrollPosition);
}

function initialiseScrollPosition() {
    // $('#visualisation-container').scrollTop(scrollIncrementHeightToInitInCenter);
    // $('#visualisation-container').scrollLeft(scrollIncrementWidthToInitInCenter);
}

function initialiseGraphData(graph) {
    nodes = graph.nodes;
    links = graph.links;
    allocateIncomingAndOutgoingLinks();
}

function allocateIncomingAndOutgoingLinks() {
    links.forEach(link => {
        getNodeById(link.source).outgoing.push(link);
        getNodeById(link.target).incoming.push(link);
    });
}

function generateSimplifiedGraph() {
    let simplifiedNodes = [];
    let simplifiedLinks = [];
    $.extend(simplifiedNodes, nodes);
    $.extend(simplifiedLinks, links);

    simplifiedNodes = simplifiedNodes.filter(isTopLevelNode);

    let uniqueLinkIdentifiers = {};
    let uniqueLinks = [];
    simplifiedLinks = simplifiedLinks.filter(l => {

        let link = {};
        $.extend(link, l);

        let sourceParent = {};
        let targetParent = {};
        $.extend(sourceParent, getParentTable(link.source));
        $.extend(targetParent, getParentTable(link.target));

        link.source = sourceParent;
        link.target = targetParent;

        let uniqueLinkIdentifier = link.source.id + "#" + link.target.id;
        if (uniqueLinkIdentifier in uniqueLinkIdentifiers) return false;
        uniqueLinkIdentifiers[uniqueLinkIdentifier] = "exists";
        uniqueLinks.push(link);
        return true;
    });

    simplifiedLinks = uniqueLinks;

    return { nodes: simplifiedNodes, links: simplifiedLinks };
}

function generateForceDirectedSimulation() {

    svg = d3
        .select('svg')
        .attr('width', canvasWidth)
        .attr('height', canvasHeight)
        .call(d3.zoom().on("zoom", function() {
            svg.attr("transform", d3.event.transform)
        }))
        .append("g");

    linkSelection = svg
        .selectAll('line')
        .data(links)
        .enter()
        .append('line')
        .attr('stroke', linkDefaultColor)
        .attr('fill', linkFill)
        .attr('stroke-width', linkDefaultWidth)
        .attr('id', link => link.id)
        .attr('class', 'link')
        .on('mouseover', linkMouseOver)
        .on('mouseout', linkMouseOut);

    nodeSelection = svg
        .selectAll('rect')
        .data(nodes)
        .enter()
        .append('g')
        .attr('class', setNodeClass)
        .append('rect')
        .attr('class', node => node.group)
        .attr('width', calculateNodeWidth)
        .attr('height', calculateNodeHeight)
        .attr('fill', determineNodeColor)
        .attr('opacity', determineNodeOpacity)
        .attr('class', setNodeClass)
        .attr('id', node => node.id)
        .call(
            d3.drag()
            .on('start', dragStart)
            .on('drag', drag)
            .on('end', dragEnd))
        .on('mouseover', node => columnMouseOver(node.id))
        .on('mouseout', node => columnMouseOut(node.id));

    labels = svg
        .selectAll('g')
        .append('text')
        .attr('fill', determineTextColor)
        .attr('font-size', fontSize)
        .attr('font-family', fontFamily)
        .attr('font-weight', label =>
            isTopLevelNode(label) ? topLevelNodeDefaultFontWeight : columnFontWeight
        )
        .attr('class', 'label')
        .attr('id', label => 'label-' + label.id)
        .text(label => label.name)
        .on('mouseover', labelMouseOver)
        .on('mouseout', labelMouseOut)
        .call(
            d3.drag()
            .on('start', dragStart)
            .on('drag', drag)
            .on('end', dragEnd));

    simulation = d3.forceSimulation(nodes);

    simulation
    // .force('center', d3.forceCenter(canvasWidth / 2, canvasHeight / 2))
    // .force('collision', d3.forceCollide(d => calculateNodeWidth(d) * collisionWidthRatio / 2))
    // .force('charge', d3.forceManyBody().strength(30))
        .force(
            'links',
            d3
            .forceLink(links)
            .id(link => link.id)
            .distance(linkPreferredDistance)
            .strength(linkForceStrength)
        )
        .on('tick', ticked)
        .alphaMin(alphaMin);
}