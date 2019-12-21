function initPanel() {
    initRight();
    $("#goldIcon").click(goldIconClick);
}

function initRight() {
    let right = $("#right");
    for(let team=0;team<4;team++){
        right.append(
            $("<div/>")
                .attr("id","team"+team)
                .addClass("team teamUnselected")
                .append(
                        setCellLoc($("<div/>"),new Loc(1,0))
                            .attr("id","teamgold"+team)
                            .addClass("teamicon")
                            .append($("<img/>")
                                     .attr("src","/img/gold.png")
                                     .addClass("cell")
                            )
                            .append($("<div/>")
                                     .attr("id","teamgoldtext"+team)
                                     .addClass("text goldtext")
                            )
                            .hide()
                )
                .append(
                        setCellLoc($("<div/>"),new Loc(2,0))
                            .attr("id","teamrum"+team)
                            .addClass("teamicon")
                            .append($("<img/>")
                                         .attr("src","/img/rumbottle.png")
                                         .addClass("cell")
                            )
                            .append($("<div/>")
                                     .attr("id","teamrumtext"+team)
                                     .addClass("text goldtext")
                            )
                            .hide()
                )
                .append(
                    $("<svg/>")
                        .attr("id","svgteam"+team)
                        .addClass("team")
                        .append( pirateEl(team)
                                    .attr({cx:35,cy:35})
                        )
                )
        );
    }
    right.html(right.html()); // workaround to fix svg element
}

function setTeamIcon(team:number,val:number,divId:string,textId:string) {
    if (val == 0) {
        $("#"+divId+team).hide();
    } else {
        $("#"+textId+team).empty().append(""+val);
        $("#"+divId+team).show();
    }
}

function resetTeam(view:any) {
    $("div.team").removeClass("teamSelected");
    $("div.team").addClass("teamUnselected");
    $("#team"+view.currentTeam).addClass("teamSelected");
    for(let team=0;team<4;team++) {
        setTeamIcon(team, view.gold[team], "teamgold", "teamgoldtext");
        setTeamIcon(team, view.rum[team], "teamrum", "teamrumtext");
    }

    //resetTop(<number>view.currentTeam, view,pirates);
}

let selPirate:Pirate = undefined;
let withGold:boolean = false;

function goldIconClick() {
    if (withGold) {
        unselectWithGold();
    } else {
        selectWithGold();
    }
}

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
    cell(p.loc).removeClass("fieldPirateSelected");
    unselectFieldCells();
    selPirate = undefined;
}
function selectPirate(p:Pirate) {
    unselectPirate();
    selPirate = p;
    selectFieldPirate(selPirate);
    let pirate = getSelectedPirate();
    cell(pirate.loc).addClass("fieldPirateSelected");
    selectFieldCells();
}

function selectWithGold() {
    $("#goldIcon").addClass("teamSelected");
    if (withGold) return;
    if (selPirate == undefined) return;
    unselectFieldCells();
    withGold = true;
    selectFieldCells();
}

function unselectWithGold() {
    $("#goldIcon").removeClass("teamSelected");
    if (!withGold) return;
    unselectFieldCells();
    withGold = false;
    selectFieldCells();
}

