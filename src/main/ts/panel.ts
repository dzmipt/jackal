function initPanel() {
    initTop();
    initRight();
    $("#goldIcon").click(goldIconClick);
    $("#rumIcon").click(rumIconClick);
}

function initTop() {
    let top = $("#top");
    for (let num=0;num<6;num++) {
        let coord = getCoordinate(new Loc(0,3+num));
        let divHero = $("<div/>")
                        .attr("id","divhero" + num)
                        .addClass("button")
                        .css(coord)
                        .append($("<img/>")
                                .attr("id","hero"+num)
                                .css("pointer-events","none")
                        ).append($("<div/>")
                                .attr("id","herolabel"+num)
                                .addClass("heroLabel")
                                .css("top","7px")
                        ).append($("<div/>")
                                .attr("id","heronotes"+num)
                                .addClass("heroLabel")
                        ).click(num, event =>{heroClick(event.data)})
                        .mousedown(event => {$(event.target).css("top","3px")})
                        .mouseup(event => {$(event.target).css("top","0px")});
        top.append(divHero);
        if (num>=3) {
            top.append(
                $("<img/>")
                    .attr("id","heroteam"+num)
                    .addClass("smallhero")
                    .css({top:coord.top+33,left:coord.left+35})
            );
        }
    }
}

function initRight() {
    let right = $("#right");
    for(let team=0;team<4;team++){
        right.append(
            $("<div/>")
                .attr("id","team"+team)
                .addClass("team teamUnselected")
                .append(
                        setCellLoc($("<img/>"), new Loc(0,0))
                            .attr("src","/img/team"+team+"cell.png")
                            .addClass("teamicon")
                )
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
        );
    }
    right.append($("<img id='cave' src='/img/cave.png'/>"));
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
    $("#team"+currentTeam).addClass("teamSelected");
    for(let team=0;team<4;team++) {
        setTeamIcon(team, view.gold[team], "teamgold", "teamgoldtext");
        setTeamIcon(team, view.rum[team], "teamrum", "teamrumtext");
    }

    resetTop(view.teamName);
}

function resetTop(teamName:string) {
    $("#topLabel").empty().append(teamName);
    for(let num=0;num<6;num++) {
        let h = $("#divhero"+num);
        let hero = Hero.get(new HeroId(currentTeam,num));
        if (hero.hidden && num>=3) {
            h.hide();
        } else {
            h.show();
            let src:string = hero.id.num<3 ? "team"+currentTeam : "hero"+hero.id.num;
            $("#hero"+num).attr("src","/img/" + src + "cell.png");

            if ( ( hero.canGo() && hero.id.team == currentTeam) || hero.rumReady) h.removeClass("disabled");
            else h.addClass("disabled");
         }

        if (num>=3) {
            let ht = $("#heroteam"+num);
            if (hero.hidden) {
                ht.hide();
            } else {
                ht.attr("src","/img/team"+hero.id.team+".png").show();
            }
        }
    }

    updateIcons();
}

function updateGoldIcon() {
    let enabled;
    if (selHero == undefined) {
        enabled = Hero.heroes.some(h => {return h.stepsWithGold.length>0});
    } else {
        enabled = selHero.stepsWithGold.length>0;
    }

    let el = $("#goldIcon");
    if (enabled) el.removeClass("disabled");
    else el.addClass("disabled");
}

function updateRumIcon() {
    let enabled:boolean;
     if (selHero == undefined) {
        enabled = Hero.heroes.some(h => {return h.rumReady});
     } else {
        enabled = selHero.rumReady;
     }

    let el = $("#rumIcon");
    if (enabled) el.removeClass("disabled");
    else el.addClass("disabled");
}

function updateIcons() {
    updateGoldIcon();
    updateRumIcon();
}

function goldIconClick() {
    if (withGold) {
        unselectWithGold();
    } else {
        selectWithGold();
    }
}

function rumIconClick() {
    if (selHero == undefined) return;
    if (!selHero.rumReady) return;
    let rum = {id:id, heroId:selHero.id};
    send("drink", rum);
}

function heroClick(num:number) {
    let hero = Hero.get(new HeroId(currentTeam, num));
    if ( selectableHeroes.indexOf(hero) == -1 ) return;
    selectHero(hero);
}

let selectableHeroes:Hero[] = [];
let selHero:Hero = undefined;
let withGold:boolean = false;


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
    $("#divhero"+selHero.id.num).removeClass("teamSelected");
    unselectFieldHero(selHero);
    front(selHero.loc).removeClass("fieldPirateSelected");
    unselectFieldCells();
    selHero = undefined;
    updateIcons();
}
function selectHero(h:Hero) {
    unselectHero();
    $("#divhero"+h.id.num).addClass("teamSelected");
    selHero = h;
    selectFieldHero(selHero);
    front(selHero.loc).addClass("fieldPirateSelected");
    selectFieldCells();
    updateIcons();
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

function selectHeroAtLoc(loc:Loc) {
    if (selHero == undefined ||
            ! selHero.loc.equals(loc) ) {
        let h = selectableHeroes.find( h => { return h.loc.equals(loc)});
        if (h != undefined) selectHero(h);
    } else {
        switchSelectedHero();
    }
}
function switchSelectedHero() {
    if (selHero == undefined) return;

    let heroes:Hero[] = selectableHeroes.filter(h => {return h.loc.equals(selHero.loc)} );
    let index = heroes.indexOf(selHero);
    selectHero( heroes[ (index+1)%heroes.length ] );
}

function selectNextPrevHero(delta:number) {
    if (selectableHeroes.length == 0) return;

    let index = 0;
    if (selHero != undefined) {
        index = selectableHeroes.indexOf(selHero);
        let count = selectableHeroes.length;
        index = (count+index+delta) % count;
    }
    selectHero(selectableHeroes[index]);
}

function selectNextHero() {
    selectNextPrevHero(1);
}

function selectPrevHero() {
    selectNextPrevHero(-1);
}

