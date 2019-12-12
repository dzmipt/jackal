function initPanel() {
   let panel = $("#panel");
   let svg = $("<svg/>")
                    .attr("id","psvg")
                    .addClass("panel")
                    .css("left","0px");
   var frontEls = [];
   for(team=0;team<4;team++){
       for(num=0;num<3;num++){
           row = 2*team;
           col = num;
           let pcell = setCellLoc($("<div/>"),row,col)
                            .attr("id","pcell"+team+"_"+num)
                            .addClass("pcell");
           panel.append(pcell);

           let pirate = pirateEl(team)
                            .attr({cx:LEN*col+35,cy:LEN*row+35});
           svg.append(pirate);

           let front = setCellLoc($("<div/>"),row,col)
                            .attr("id","pfront"+team+"_"+num)
                            .addClass("cell");
           front = addEventListeners(front,"panel",team,num);
           frontEls.push(front);
       }
   }

   panel.append(svg);
   panel.html(panel.html()); // workaround to fix svg element

   frontEls.forEach(front=>{panel.append(front)});
}

function pcell(team,num) {return $("#pcell"+team+"_"+num)}
function selectPCell(team,num) {pcell(team,num).addClass("pselected")}
function unselectPCell(team,num) {pcell(team,num).removeClass("pselected")}
//function isSelectedPCell(team,num) {pcell(team,num).hasClass("pselected")}

function panelOver(team,num) {
    if(pirates[team][num].steps.length == 0) return;
    pcell(team,num).addClass("pover");
}
function panelLeave(team,num) {
    pcell(team,num).removeClass("pover");
}
function panelClick(team,num) {
    if (pirates[team][num].steps.length == 0) return;
    if (selPirate!=undefined && selPirate.team == team && selPirate.num == num) unselectPirate();
    else selectPirate(team,num);
}

let selPirate = undefined;
let withGold = false;

function getSelectedPirate() {
    if (selPirate == undefined) return undefined;
    return pirates[selPirate.team][selPirate.num];
}
function unselectFieldCells() {
    let p = getSelectedPirate();
    if (p == undefined) return;
    p.steps.forEach(step => {unselectCell(step.row,step.col)});
    refreshSelectableFieldCell();
}
function selectFieldCells() {
    let p = getSelectedPirate();
    if (p == undefined) return;
    let steps = withGold ? p.stepsWithGold : p.steps;
    steps.forEach(step => {selectCell(step.row,step.col,withGold)});
    refreshSelectableFieldCell();
}

function unselectPirate() {
    if (selPirate == undefined) return;
    let p = getSelectedPirate();
    let team = selPirate.team;
    let num = selPirate.num;
    unselectPCell(team,num);
    cell(p.loc.row,p.loc.col).removeClass("fieldPirateSelected");
    unselectFieldCells();
    selPirate = undefined;
}
function selectPirate(team,num) {
    unselectPirate();
    selPirate = {team:team,num:num};
    let p = getSelectedPirate();
    selectPCell(team,num);
    cell(p.loc.row,p.loc.col).addClass("fieldPirateSelected");
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

