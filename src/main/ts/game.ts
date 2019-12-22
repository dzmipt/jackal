const LEN = 70;
const pirateColor = ["white","yellow","red","black"];
const selPirateBorderColor = ["blue","blue","blue","cornsilk"];

const heroesColor = ["ForestGreen","SaddleBrown","DarkBlue"];
const selHeroesBorderColor = ["cornsilk","cornsilk","cornsilk"];

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
    index() {return ""+this.team+"_"+this.num}
    static ALL:HeroId[];
}
HeroId.ALL = [];
for(let team=0;team<4;team++){
    for(let num=0;num<3;num++){
        HeroId.ALL.push(new HeroId(team,num));
    }
}
HeroId.ALL.push(new HeroId(-1,3)); // Ben Gunn
HeroId.ALL.push(new HeroId(-1,4)); // Friday
HeroId.ALL.push(new HeroId(-1,5)); // Missioner

class Hero {
    id:HeroId;
    hidden:boolean;
    loc:Loc;
    steps:Loc[];
    stepsWithGold:Loc[];
    index:number;
    count:number;
    rumReady:boolean;
    constructor(id:HeroId, hidden:boolean, loc:Loc, steps:Loc[], stepsWithGold: Loc[],
                index:number, count:number, rumReady:boolean) {
        this.id = id;
        this.hidden = hidden;
        this.loc = loc;
        this.steps = steps;
        this.stepsWithGold = stepsWithGold;
        this.index = index;
        this.count = count;
        this.rumReady = rumReady;
    }
    canGo():boolean { return this.steps.length>0 || this.stepsWithGold.length>0}
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

function getHeroSelectedBorderColor(id:HeroId) {
    return id.num<3 ? selPirateBorderColor[id.team] : selHeroesBorderColor[id.num];
}

function heroEl(id:HeroId) {
    let color = id.num<3 ? pirateColor[id.team] : heroesColor[id.num]
    return $("<circle r='10' stroke-width='2' stroke='black'/>")
                     .attr("fill",color);
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
    let vheroes:any = view.heroes;
    Hero.heroes = [];
    for(let i in HeroId.ALL) {
        let vh:any = vheroes[i];
        let id:HeroId = HeroId.ALL[i];
        let loc = getLoc(vh.loc);
        Hero.heroes.push(new Hero(id, vh.hidden, loc,
                                    getLocs(vh.steps), getLocs(vh.stepsWithGold),
                                    vh.index, view.cells[loc.row][loc.col].count,
                                    vh.rumReady));
    }
}

function resetFieldHeroes(animate:boolean) {
    unselectHero();

    let lastHero:Hero = undefined;
    let numToMove = 0;

    for(let hero of Hero.heroes) {
        if (hero.canGo()) {
            showHeroOnTop(hero);
            lastHero = hero;
            numToMove++;
        }
        setHero(hero, animate);

    }

    refreshSelectableFieldCell();
    if (numToMove == 1) {
        selectHero(lastHero);
    }
}

function setView(view:any) {
    id = view.id;
    resetGold();
    setCells(view.cells);
    setHeroes(view);
    resetPanels(view);
    let animate = view.animateShip == null;
    resetFieldHeroes(animate);
    animateRum(view.animateRum);
}

function initGame() {
    $(window).keydown(e=>{
        if (e.keyCode == 27) { //esc
            unselectHero();
        } else if (e.keyCode == 32) { //space
            switchSelectedHero();
        }
        if (e.shiftKey) selectWithGold();
        else unselectWithGold();
     });
     $(window).keyup(e=>{
        unselectWithGold();
     });

    $("#prevTurn").click(prevTurn);
    $("#nextTurn").click(nextTurn);

    send("init",{});
}

function prevTurn() {
    send("prevTurn", {id:id});
}

function nextTurn() {
    send("nextTurn", {id:id});
}

$(function(){
    initField();
    initPanel();
    initGame();
});
