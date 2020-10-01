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
    $(labels).attr('fill', labelHighlightTextColor);
}

function highlightLinks(links) {
    $(links).attr({
        stroke: linkHighlightColor,
        fill: linkHighlightColor,
        'stroke-width': linkHighlightWidth
    });
}

function unHighlightColumns(columns) {
    columns.attr('fill', columnDefaultBackgroundColor);
}

function unHighlightTopLevelNodes(nodes) {
    nodes
        .attr('fill', topLevelNodeDefaultBackgroundColor)
        .attr('opacity', topLevelNodeDefaultOpacity);
}

function unHighlightLabels(labels) {
    $(labels)
        .filter((index, label) => isTopLevelId(columnOfLabel(label)))
        .attr('fill', topLevelNodeDefaultTextColor);
}

function unHighlightLinks(links) {
    $(links).attr({
        stroke: linkDefaultColor,
        fill: linkDefaultColor,
        'stroke-width': linkDefaultWidth
    });
}

function highlightIds(ids) {
    highlightColumns(
        $('rect').filter((index, column) => !isTopLevelId(column.id) && ids.includes(column.id))
    );

    highlightTopLevelNodes(
        $('rect').filter((index, node) => isTopLevelId(node.id) && ids.includes(node.id))
    );

    highlightLabels(
        $('.label').filter((index, label) => ids.includes(columnOfLabel(label)))
    );

    highlightLinks($('.link').filter((index, link) => ids.includes(link.id)));
}

function unHighlightIds(ids) {
    unHighlightColumns(
        $('rect').filter((index, column) => !isTopLevelId(column.id) && ids.includes(column.id))
    );

    unHighlightTopLevelNodes(
        $('rect').filter((index, node) => isTopLevelId(node.id) && ids.includes(node.id))
    );

    unHighlightLabels(
        $('.label').filter((index, label) => ids.includes(columnOfLabel(label)))
    );

    unHighlightLinks($('.link').filter((index, link) => ids.includes(link.id)));
}

function columnMouseOver(id) {
    highlightIds(getAllLineageSiblingIds(id));
}

function columnMouseOut(id) {
    unHighlightIds(getAllLineageSiblingIds(id));
}

function labelMouseOver() {
    let columnId = $(this.parentElement).find('rect').attr('id');
    columnMouseOver(columnId);
}

function labelMouseOut() {
    let columnId = $(this.parentElement).find('rect').attr('id');
    columnMouseOut(columnId);
}

function linkMouseOver(link) {
    highlightIds([
        link.id,
        ...getAllSourceSiblings(link.source.id),
        ...getAllTargetSiblings(link.target.id)
    ]);
}

/////////////////////////////////////////////////////////////////////////////////
// TODO: Use the dedicated function for this.
function linkMouseOut(link) {
    unHighlightIds([
        link.id,
        ...getAllSourceSiblings(link.source.id),
        ...getAllTargetSiblings(link.target.id)
    ]);
}