const LEN = 70;
const pirateColor = ["white","yellow","red","black"];
const selPirateBorderColor = ["blue","blue","blue","cornsilk"];

class Loc {
    row:number;
    col:number;
    constructor(row,col) {this.row = row;this.col = col}
    equals(x:Loc) {return this.row == x.row && this.col == x.col}
    index() {return ""+this.row+"_"+this.col}
}
let LocAll = [];
for(let row=0;row<13;row++) {
    for(let col=0;col<13;col++) {
        LocAll.push(new Loc(row,col));
    }
}

class Pirate {
    team:number;
    num:number;
    constructor(team,num) {this.team = team; this.num = num}
    equals(x:Pirate) {return this.team == x.team && this.num == x.num}
    index() {return ""+this.team+"_"+this.num}
}
let PirateAll = [];
for(let team=0;team<4;team++){
    for(let num=0;num<3;num++){
        PirateAll.push(new Pirate(team,num));
    }
}


function getCoordinate(loc:Loc) {
    return {left:loc.col*LEN,top:loc.row*LEN};
}
function setCellLoc(el:JQuery,loc:Loc) {
    return el.css(getCoordinate(loc));
}

type OverData = Loc | Pirate;

function addEventListeners(el:JQuery,data:OverData) {
         return el
                .on("mouseenter",data, (event) => {evtOver(event.data); } )
                .on("mouseleave",data, (event) => {evtLeave(event.data); } )
                .on("click",data, (event) => {evtClick(event.data); } );

}
function pirateEl(team:number) {
    return $("<circle r='10' stroke-width='2' stroke='black'/>")
                     .attr("fill",pirateColor[team]);
}

function goldEl() {
    return $("<ellipse rx='15' ry='8' fill='GoldenRod' stroke='MediumBlue'/>");
}

function goldTextEl() {
     return $("<text fill='MediumBlue'/>");
}

var overData:OverData = undefined;
function evtLeave(data:OverData) {
    if (overData != undefined) {
        if(overData instanceof Loc) fieldLeave(overData);
//        else panelLeave(overData);
    }

    if (data != overData) return;
    overData = undefined;
}
function evtOver(data:OverData) {
    evtLeave(overData);
    overData=data;

    if(overData instanceof Loc) fieldOver(overData);
//    else panelOver(overData);

}

function evtClick(data:OverData) {
    if(overData instanceof Loc) fieldClick(overData);
//    else panelClick(overData);
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
let pirates:any = undefined;

function setView(view:any) {
    id = view.id;
    resetTeam(view);
    unselectPirate();
    resetGold();
    let animate = view.animateShip == null;
    LocAll.forEach(loc => {
            cell(loc).attr("src", "/img/"+view.cells[loc.row][loc.col].icon+".png");
            let count = view.cells[loc.row][loc.col].count;
            for(let i=0;i<count; i++) {
                showGold(loc,count,i,view.cells[loc.row][loc.col].gold[i]);
            }
    });
    let selPirate:Pirate = undefined;
    let numToMove = 0;
    pirates = view.pirates;

    PirateAll.forEach(pirate => {
        let p = pirates[pirate.team][pirate.num];
        if (p.steps.length > 0 || p.stepsWithGold.length > 0) {
            showPirateOnTop(pirate);
        }
    });

    PirateAll.forEach(pirate => {
            let p = pirates[pirate.team][pirate.num];
            p.loc = new Loc(p.loc.row, p.loc.col);
            let loc = p.loc;
            for(let i=0;i<p.steps.length;i++) p.steps[i] = new Loc(p.steps[i].row, p.steps[i].col);
            for(let i=0;i<p.stepsWithGold.length;i++) p.stepsWithGold[i] = new Loc(p.stepsWithGold[i].row, p.stepsWithGold[i].col);
            setPirate(pirate,loc,animate,view.cells[loc.row][loc.col].count,p.index);
            if (p.dead) hidePirate(pirate);
            if (p.steps.length > 0 || p.stepsWithGold.length > 0) {
                selPirate = pirate;
                numToMove++;
            }
    });
    refreshSelectableFieldCell();
    if (numToMove == 1) {
        selectPirate(selPirate);
    }

    animateRum(view.animateRum);
}

function initGame() {
    $(window).keydown(e=>{
        if (e.keyCode == 27) { //esc
            unselectPirate();
        } else if (e.keyCode == 32) { //space
            switchSelectedPirate();
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
