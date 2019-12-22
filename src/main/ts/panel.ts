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
                        .append( heroEl(new HeroId(team,0))
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

function resetPanels(view:any) {
    $("div.team").removeClass("teamSelected");
    $("div.team").addClass("teamUnselected");
    $("#team"+view.currentTeam).addClass("teamSelected");
    for(let team=0;team<4;team++) {
        setTeamIcon(team, view.gold[team], "teamgold", "teamgoldtext");
        setTeamIcon(team, view.rum[team], "teamrum", "teamrumtext");
    }

    resetTop(<number>view.currentTeam);
}

function resetTop(currentTeam:number) {
    for(let i=0;i<6;i++) {
        let h = $("#hero"+i).hide();
        if (Hero.get(new HeroId(currentTeam,i)).hidden) continue;
        h.attr("src","/img/team"+currentTeam+".png").show();
    }
}

let selHero:Hero = undefined;
let withGold:boolean = false;

function goldIconClick() {
    if (withGold) {
        unselectWithGold();
    } else {
        selectWithGold();
    }
}

function unselectFieldCells() {
    if (selHero == undefined) return;
    selHero.steps.forEach(step => {unselectCell(step)});
    refreshSelectableFieldCell();
}
function selectFieldCells() {
    if (selHero == undefined) return;
    let steps = withGold ? selHero.stepsWithGold : selHero.steps;
    steps.forEach(step => {selectCell(step,withGold)});
    refreshSelectableFieldCell();
}

function unselectHero() {
    if (selHero == undefined) return;
    $("#hero"+selHero.id.num).removeClass("teamSelected");
    unselectFieldHero(selHero);
    cell(selHero.loc).removeClass("fieldPirateSelected");
    unselectFieldCells();
    selHero = undefined;
}
function selectHero(h:Hero) {
    unselectHero();
    $("#hero"+h.id.num).addClass("teamSelected");
    selHero = h;
    selectFieldHero(selHero);
    cell(selHero.loc).addClass("fieldPirateSelected");
    selectFieldCells();
}

function selectWithGold() {
    if (withGold) return;
    if (selHero == undefined) return;
    unselectFieldCells();
    withGold = true;
    $("#goldIcon").addClass("teamSelected");
    selectFieldCells();
}

function unselectWithGold() {
    if (!withGold) return;
    unselectFieldCells();
    withGold = false;
    $("#goldIcon").removeClass("teamSelected");
    selectFieldCells();
}

