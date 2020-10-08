const canvasWidth = 5000;
const canvasHeight = 2500;
const scrollIncrementWidthToInitInCenter = 100;
const scrollIncrementHeightToInitInCenter = -100;

const nodeForceStrength = -30;
const nodeCollisionRadius = 10;
const linkForceStrength = 0.01;
const linkPreferredDistance = 50;
const dragEndAlphaTarget = 0.1;
const alphaMin = 0.05;
const dragStartAlphaTarget = 0.08;
const collisionWidthRatio = 1.25;

const offWhite = 'rgb(200,200,200)';

const fontSize = 40;
const fontFamily = 'courier new';
const fontSizeToCharacterWidthRatio = 0.6;

const columnHeight = 45;
const columnDefaultBackgroundColor = 'dodgerblue';
const columnHighlightBackgroundColor = 'crimson';
const columnDefaultTextColor = offWhite;
const columnDefaultOpacity = 1;
const columnFontWeight = 'normal';
const columnClass = 'column';

const linkDefaultColor = 'grey';
const linkHighlightColor = 'crimson';
const linkFill = 'none';
const linkDefaultWidth = '1';
const linkHighlightWidth = '5';

const labelPaddingHorizontal = 15;
const labelOffsetToReachCenter = 8;
const labelHighlightTextColor = offWhite;

const topLevelNodePaddingHorizontal = 10;
const topLevelNodePaddingVertical = 10;
const topLevelNodeDefaultBackgroundColor = offWhite;
const topLevelNodeDefaultTextColor = offWhite;
const topLevelNodeBackgroundHighlightColor = 'crimson';
const topLevelNodeHighlightOpacity = 0.6;
const topLevelNodeDefaultOpacity = 0.2;
const topLevelNodeDefaultFontWeight = 'bold';
const topLevelNodeClass = 'top-level-node';
const topLevelNodeCollapsedHeight = 60;
const topLevelNodeWidthBuffer = 100;
const topLevelNodeHeightBuffer = 80;
const topLevelNodeTitleHeight = 55;

const tableType = 'TABLE';
const viewType = 'VIEW';
const columnType = 'COLUMN';

const idDelimiter = '::';

let freezeHighlight = false;
let showColumns = true;
let showColumnsChanged = false;

let gridStartingWidths = { 0: 0 };
let gridStartingHeights = {};

let correctionX = 0;
let correctionY = 0;

const optimisationIterations = 100;
const optimisePaddingIncrement = 10;
let optimisedPadding = {};

let staticMode = true;

/**
 * Enforce a maximum number of logging messages that can be produced.
 * This is because the d3.js visualisation involves a very large number of iterations
 * through the code. In the case that a logging message is printing on every one of these 
 * iterations, the application slows and crashes due to the bottleneck of logging messages.
 * This threshold is the amount beyond which, logging will be ignored.
 */
const loggingCountThreshold = 50;

let NOT_FOUND = -1;

let uploadFilesInput = $('#upload-files-input');
let generateVisualisationButton = $('#generate-visualisation-button');
let demoButton = $('#demo-button');
let chooseFilesButton = $('#choose-files-button');
let fileListContainer = $('#file-list-container');
let fileNameContainers = $('.file-name-container');
let searchInput = $('#search-input');
let showColumnsToggleSwitch = $('#show-columns-toggle-switch');
let staticModeToggleSwitch = $('#static-mode-toggle-switch');

let tickImg = "<img class='tick' src='https://lesspestcontrol.com.au/wp-content/uploads/green-tick.png'>";
let crossImg = "<img class='cross' src='https://freesvg.org/img/milker_X_icon.png'>";

const propic_file_order = [
    'crm_join.sql',
    'supporting_views.sql',
    'appraisal_case.sql',
    'buyer_vendor_case.sql',
    'record_sale_nearby.sql',
    'leads_from_crm.sql',
    'leads_from_market.sql',
    'leads_with_score.sql',
    'agent_leads.sql'
]