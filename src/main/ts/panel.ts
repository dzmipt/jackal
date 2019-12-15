function initPanel() {
   let panel = $("#panel");
   let svg = $("<svg/>")
                    .attr("id","psvg")
                    .addClass("panel")
                    .css("left","0px");
   var frontEls = [];
   PirateAll.forEach(p => {
           let loc = new Loc(2*p.team, p.num);
           let pcell = setCellLoc($("<div/>"),loc)
                            .attr("id","pcell"+p.index())
                            .addClass("pcell");
           panel.append(pcell);

           let pirate = pirateEl(p.team)
                            .attr({cx:LEN*loc.col+35,cy:LEN*loc.row+35});
           svg.append(pirate);

           let front = setCellLoc($("<div/>"),loc)
                            .attr("id","pfront"+p.index())
                            .addClass("cell");
           front = addEventListeners(front,p);
           frontEls.push(front);
   });

   panel.append(svg);
   panel.html(panel.html()); // workaround to fix svg element

   frontEls.forEach(front=>{panel.append(front)});
}

function pcell(p:Pirate) {return $("#pcell"+p.index())}
function selectPCell(p:Pirate) {pcell(p).addClass("pselected")}
function unselectPCell(p:Pirate) {pcell(p).removeClass("pselected")}
//function isSelectedPCell(team,num) {pcell(team,num).hasClass("pselected")}

function panelOver(p:Pirate) {
    if(pirates[p.team][p.num].steps.length == 0) return;
    pcell(p).addClass("pover");
}
function panelLeave(p:Pirate) {
    pcell(p).removeClass("pover");
}
function panelClick(p:Pirate) {
    if (pirates[p.team][p.num].steps.length == 0) return;
    if (selPirate!=undefined && selPirate.equals(p)) unselectPirate();
    else selectPirate(p);
}

let selPirate:Pirate = undefined;
let withGold:boolean = false;

function getSelectedPirate() {
    if (selPirate == undefined) return undefined;
    return pirates[selPirate.team][selPirate.num];
}
function unselectFieldCells() {
    let p = getSelectedPirate();
    if (p == undefined) return;
    p.steps.forEach(step => {unselectCell(step)});
    refreshSelectableFieldCell();
}
function selectFieldCells() {
    let p = getSelectedPirate();
    if (p == undefined) return;
    let steps = withGold ? p.stepsWithGold : p.steps;
    steps.forEach(step => {selectCell(step,withGold)});
    refreshSelectableFieldCell();
}

function unselectPirate() {
    if (selPirate == undefined) return;
    unselectFieldPirate(selPirate);
    let p = getSelectedPirate();
    unselectPCell(selPirate);
    cell(p.loc).removeClass("fieldPirateSelected");
    unselectFieldCells();
    selPirate = undefined;
}
function selectPirate(p:Pirate) {
    unselectPirate();
    selPirate = p;
    selectFieldPirate(selPirate);
    let pirate = getSelectedPirate();
    selectPCell(p);
    cell(pirate.loc).addClass("fieldPirateSelected");
    selectFieldCells();
}

function selectWithGold() {
    if (withGold) return;
    if (selPirate == undefined) return;
    unselectFieldCells();
    withGold = true;
    selectFieldCells();
}

function unselectWithGold() {
    if (!withGold) return;
    unselectFieldCells();
    withGold = false;
    selectFieldCells();
}

