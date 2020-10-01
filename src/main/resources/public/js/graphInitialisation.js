var nodes = []
var links = []
var simulation, nodeSelection, linkSelection, lables, svg;

function generateVisualisation(graph) {
    initialiseContainer();
    initialiseGraphData(graph);
    generateForceDirectedSimulation();
}

function initialiseContainer() {
    $('#visualisation-container').show(600, initialiseScrollPosition);
}

function initialiseScrollPosition() {
    $('#visualisation-container').scrollTop(canvasHeight / 2 + scrollIncrementHeightToInitInCenter);
    $('#visualisation-container').scrollLeft(canvasWidth / 2 + scrollIncrementWidthToInitInCenter);
}

function initialiseGraphData(graph) {
    nodes = graph.nodes;
    links = graph.links;
    allocateIncomingAndOutgoingLinks();
}

function allocateIncomingAndOutgoingLinks() {
    links.forEach((link) => {
        getNodeById(link.source).outgoing.push(link);
        getNodeById(link.target).incoming.push(link);
    });
}

function generateForceDirectedSimulation() {

    svg = d3
        .select('svg')
        .attr('width', canvasWidth)
        .attr('height', canvasHeight);

    nodeSelection = svg
        .selectAll('rect')
        .data(nodes)
        .enter()
        .append('g')
        .append('rect')
        .attr('width', (d) => calculateNodeWidth(d))
        .attr('height', (d) => calculateNodeHeight(d))
        .attr('fill', (d) => determineNodeColor(d))
        .attr('opacity', (d) => determineNodeOpacity(d))
        .attr('class', (d) => setGroupClasses(d))
        .attr('id', (d) => d.id)
        .call(d3.drag().on('start', dragStart).on('drag', drag).on('end', dragEnd))
        .on('mouseover', (d) => columnMouseOver(d.id))
        .on('mouseout', (d) => columnMouseOut(d.id));

    linkSelection = svg
        .selectAll('line')
        .data(links)
        .enter()
        .append('line')
        .attr('stroke', linkDefaultColor)
        .attr('fill', linkFill)
        .attr('stroke-width', linkDefaultWidth)
        .attr('id', (d) => d.id)
        .attr('class', 'link')
        .on('mouseover', (d) => linkMouseOver(d))
        .on('mouseout', (d) => linkMouseOut(d));

    lables = svg
        .selectAll('g')
        .append('text')
        .attr('fill', (d) => determineTextColor(d))
        .attr('font-size', fontSize)
        .attr('font-family', fontFamily)
        .attr('font-weight', (d) =>
            isTopLevelNode(d) ? topLevelNodeDefaultFontWeight : columnFontWeight
        )
        .attr('class', 'label')
        .attr('id', (d) => 'label-' + d.id)
        .text((d) => d.name)
        .on('mouseover', labelMouseOver)
        .on('mouseout', labelMouseOut);

    simulation = d3.forceSimulation(nodes);

    simulation
        .force('center', d3.forceCenter(canvasWidth / 2, canvasHeight / 2))
        .force('nodes', d3.forceManyBody().strength(nodeForceStrength))
        .force(
            'links',
            d3
            .forceLink(links)
            .id((d) => d.id)
            .distance(linkPreferredDistance)
            .strength(linkStrength)
        )
        .on('tick', ticked);
}