const LEN = 70;

class Loc {
    row:number;
    col:number;
    constructor(row,col) {this.row = row; this.col = col}
    equals(x:Loc) {return this.row == x.row && this.col == x.col}
    index() {return ""+this.row+"_"+this.col}

    static ALL:Loc[];
}
Loc.ALL = [];
for(let row=0;row<13;row++) {
    for(let col=0;col<13;col++) {
        Loc.ALL.push(new Loc(row,col));
    }
}

class HeroId {
    team:number;
    num:number;
    constructor(team,num) {this.team = team; this.num = num}
    equals(x:HeroId) {return this.team == x.team && this.num == x.num}
    index() {return ""+(this.num<3?this.team:-1)+"_"+this.num}
    static ALL:HeroId[];
    static BenGunn:HeroId = new HeroId(-1,3);
    static Friday:HeroId = new HeroId(-1,4);
    static Missioner:HeroId = new HeroId(-1,5);
}
HeroId.ALL = [];
for(let team=0;team<4;team++){
    for(let num=0;num<3;num++){
        HeroId.ALL.push(new HeroId(team,num));
    }
}
HeroId.ALL.push(HeroId.BenGunn); // Ben Gunn
HeroId.ALL.push(HeroId.Friday); // Friday
HeroId.ALL.push(HeroId.Missioner); // Missioner

class Hero {
    id:HeroId;
    inCave:boolean;
    hidden:boolean;
    dead:boolean;
    loc:Loc;
    viaLoc:Loc;
    steps:Loc[];
    stepsWithGold:Loc[];
    index:number;
    count:number;
    rumReady:boolean;
    constructor(id:HeroId, inCave:boolean, hidden:boolean, dead:boolean, loc:Loc, viaLoc:Loc, steps:Loc[], stepsWithGold: Loc[],
                index:number, count:number, rumReady:boolean) {
        this.id = id;
        this.inCave = inCave;
        this.hidden = hidden;
        this.dead = dead;
        this.loc = loc;
        this.viaLoc = viaLoc;
        this.steps = steps;
        this.stepsWithGold = stepsWithGold;
        this.index = index;
        this.count = count;
        this.rumReady = rumReady;
    }
    hasAction():boolean { return this.steps.length>0 || this.stepsWithGold.length>0 || this.rumReady}
    equals(h:Hero) {return this.id.equals(h.id)}
    static heroes:Hero[];
    static get(id:HeroId):Hero {
        let index;
        if(id.num == 3) index = 12;
        else if(id.num == 4) index = 13;
        else if(id.num == 5) index = 14;
        else index = id.team * 3 + id.num;
        return Hero.heroes[index];
    }
}


function getCoordinate(loc:Loc) {
    return {left:loc.col*LEN,top:loc.row*LEN};
}
function setCellLoc(el:JQuery,loc:Loc) {
    return el.css(getCoordinate(loc));
}

function goldEl() {
    return $("<ellipse rx='15' ry='8' fill='GoldenRod' stroke='MediumBlue'/>");
}

function goldTextEl() {
     return $("<text fill='MediumBlue'/>");
}

function animateRum(animateRum:any) {
    if (animateRum == null) return;
    $("#rumanimate").remove();
    $("#field").append(
            setCellLoc($("<img/>"),animateRum.from)
                           .attr("id","rumanimate")
                           .attr("src","/img/rumbottle.png")
                           .addClass("cell")
    );

    let loc = getCoordinate(animateRum.to);
    $("#rumanimate").animate({opacity:0.0,top:loc.top,left:loc.left},1000);

    setTimeout(()=>{$("#rumanimate").remove()}, 1500);
}

let id:string="";
let currentTeam:number = undefined;

function setCells(cells:any) {
    Loc.ALL.forEach(loc => {
            cell(loc).attr("src", "/img/"+cells[loc.row][loc.col].icon+".png");
            let count = cells[loc.row][loc.col].count;
            for(let i=0;i<count; i++) {
                showGold(loc,count,i,cells[loc.row][loc.col].gold[i]);
            }
    });
}
function getLoc(obj:any):Loc {
    return new Loc(obj.row,obj.col);
}
function getLocs(objs:any):Loc[] {
    let l:Loc[] = [];
    if (objs == null) return l;
    for(let obj of <any[]>objs) {
        l.push(getLoc(obj));
    }
    return l;
}

function setHeroes(view:any) {
    $("#herolabel3").empty().append("Ben Gunn");
    $("#herolabel4").empty().append("Friday");
    $("#herolabel5").empty().append("Missioner");

    let vheroes:any = view.heroes;
    Hero.heroes = [];
    for(let i in HeroId.ALL) {
        let vh:any = vheroes[i];
        let id:HeroId = HeroId.ALL[i];
        let inCave:boolean = vh.inCave;
        let hidden:boolean = vh.hidden;
        let dead:boolean = vh.dead;
        let index:number = vh.index;
        let loc = inCave ? new Loc(12,13) : getLoc(vh.loc);
        let count:number = inCave ? 1 : view.cells[loc.row][loc.col].count;
        Hero.heroes.push(new Hero(id, inCave, hidden, dead, loc, vh.viaLoc,
                                    getLocs(vh.steps), getLocs(vh.stepsWithGold),
                                    index, count,
                                    vh.rumReady));
        if (id.team == currentTeam || id.num>=3) {
            let notes:string[] = vh.notes;
            let noteText:string[] = [];
            for(let note of notes) {
                if (note == "pirate") {
                    $("#herolabel"+id.num).empty().append("Pirate");
                }
                if (note == "drunk") noteText.push("Drunk");
                if (note == "trapped") noteText.push("Trapped");
            }
            if (dead) {
                noteText.push("Dead");
            }
            if (inCave) {
                noteText.push("In Cave");
            }
            if (count>1) {
                noteText.push(""+ (index+1) + " of " + count);
            }
            $("#heronotes"+id.num)
                            .empty()
                            .append(noteText.join("<br/>"));
        }
    }
}

function resetFieldHeroes() {
    unselectHero();

    selectableHeroes = [];
    for(let hero of Hero.heroes) {
        let zlevel = 1;
        if (hero.hasAction()) {
            zlevel = 2;
            selectableHeroes.push(hero);
        }
        heroZLevel(hero, zlevel);
        let count=0;
        let pos=-1;
        for(let h of Hero.heroes) {
            if(h.equals(hero)) pos = count;
            if (h.index == hero.index && h.loc.equals(hero.loc)) count++;
        }
        setHero(hero, pos, count);

    }

    refreshSelectableFieldCell();
    if (selectableHeroes.length == 1) {
        selectHero(selectableHeroes[0]);
    }
}

function countHeroesActive() {
    let count = 0;
    for(let hero of Hero.heroes) {
        if (hero.hasAction()) count++;
    }
    return count;
}

function setView(view:any) {
    id = view.id;
    if (currentTeam != view.currentTeam) unselectWithGold();
    currentTeam = view.currentTeam;
    HeroId.BenGunn.team = view.benGunnTeam;
    HeroId.Friday.team = view.fridayTeam;
    HeroId.Missioner.team = view.missionerTeam;
    setShips(getLocs(view.ship));
    setBear(view.bear);
    resetGold();
    resetShipGold(view.gold);
    setCells(view.cells);
    setHeroes(view);
    resetPanels(view);
    resetFieldHeroes();
    animateRum(view.animateRum);
    if (view.teeHee) {
        $("#topLabel").addClass("teeHee");
    } else {
        $("#topLabel").removeClass("teeHee");
    }
    if (countHeroesActive() == 0) {
        view.adv = "NoMove";
    }
    if (view.adv != null) {
        let adv = $("#adv");
        let text = "";
        if (view.adv == "Earthquake") text = "Earthquake";
        if (view.adv == "NoMove") text = "No moves. Next...";
        adv.empty().append(text).show();
    }
}

function hideAdv() {
    $("#adv").fadeOut();
    if (countHeroesActive() == 0) {
        send("nextTeam",{id:id});
    }
}

function initGame() {
    $(window).keydown(e=>{
        if (e.keyCode == 27) { // esc
            unselectHero();
        } else if (e.keyCode == 32) { // space
            switchSelectedHero();
        } else if (e.keyCode == 37) { // left arrow
            selectPrevHero();
        } else if (e.keyCode == 39) { // right arrow
            selectNextHero();
        }
        if (e.shiftKey) selectWithGold();
        else unselectWithGold();

        e.preventDefault();
     });
     $(window).keyup(e=>{
        unselectWithGold();
     });

    $("#prevTurn").click(prevTurn);
    $("#nextTurn").click(nextTurn);
    $("#adv").click(hideAdv);
    addSubscription("view", setView);
    let id:string = new URLSearchParams(document.location.search.substring(1)).get("id");
    send("init",{id:id});
}

function prevTurn() {
    $("#adv").hide();
    send("prevTurn", {id:id});
}

function nextTurn() {
    $("#adv").hide();
    send("nextTurn", {id:id});
}

$(function(){
    initField();
    initPanel();
    initGame();
});
