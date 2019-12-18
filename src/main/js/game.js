const LEN = 70;
const pirateColor = ["white","yellow","red","black"];

class Loc {
    constructor(row,col) {this.row = row;this.col = col}
    equals(x) {return this.row == x.row && this.col == x.col}
    index() {return ""+this.row+"_"+this.col}
}
let LocAll = [];
for(row=0;row<13;row++) {
    for(col=0;col<13;col++) {
        LocAll.push(new Loc(row,col));
    }
}

class Pirate {
    constructor(team,num) {this.team = team; this.num = num}
    equals(x) {return this.team == x.team; this.num == num}
    index() {return ""+this.team+"_"+this.num}
}
let PirateAll = [];
for(team=0;team<4;team++){
    for(num=0;num<3;num++){
        PirateAll.push(new Pirate(team,num));
    }
}


function getCoordinate(loc) {
    return {left:loc.col*LEN,top:loc.row*LEN};
}
function setCellLoc(el,loc) {
    return el.css(getCoordinate(loc));
}
function addEventListeners(el,s,loc) {
         let data = {src:s,loc:loc};
         return el
                .on("mouseenter",data, (event) => {over(event.data); } )
                .on("mouseleave",data, (event) => {leave(event.data); } )
                .on("click",data, (event) => {click(event.data); } );

}
function pirateEl(team) {
    return $("<circle r='10' stroke-width='2' stroke='black'/>")
                     .attr("fill",pirateColor[team]);
}

function goldEl() {
    return $("<ellipse rx='15' ry='8' fill='GoldenRod' stroke='MediumBlue'/>");
}

function goldTextEl() {
     return $("<text fill='MediumBlue'/>");
}

var overData = undefined;
function leave(data) {
    if (overData != undefined) {
        if (overData.src=="field") fieldLeave(overData.loc);
        else if (overData.src=="panel") panelLeave(overData.loc);
    }

    if (data != overData) return;
    overData = undefined;
}
function over(data) {
    leave(overData);
    overData=data;

    if (data.src=="field") fieldOver(data.loc);
    else if (data.src=="panel") panelOver(data.loc);
}

function click(data) {
    if (data.src=="field") fieldClick(data.loc);
    else if (data.src=="panel") panelClick(data.loc);
}

/*function init(icons) {
    icons.forEach(data=>iconSrc[data.value]="/img/"+data.location);
    console.log("iconSrc: "+iconSrc);
}*/

let id="";
let pirates = undefined;

/*function setIcons(icons) {
    for(row=0;row<13;row++){
        for(col=0;col<13;col++){
            cell(row,col).attr("src", iconSrc[icons[row][col]]);
        }
    }
}*/

function setView(view) {
    id = view.id;
    unselectPirate();
    resetGold();
    let animate = view.animateShip == null;
    LocAll.forEach(loc => {
            cell(loc).attr("src", "/img/"+view.cells[loc.row][loc.col].icon+".png");
            let count = view.cells[loc.row][loc.col].count;
            for(i=0;i<count; i++) {
                showGold(loc,count,i,view.cells[loc.row][loc.col].gold[i]);
            }
    });
    let selPirate = undefined;
    let numToMove = 0;
    pirates = view.pirates;
    PirateAll.forEach(pirate => {
            let p = pirates[pirate.team][pirate.num];
            p.loc = new Loc(p.loc.row, p.loc.col);
            let loc = p.loc;
            for(i=0;i<p.steps.length;i++) p.steps[i] = new Loc(p.steps[i].row, p.steps[i].col);
            for(i=0;i<p.stepsWithGold.length;i++) p.stepsWithGold[i] = new Loc(p.stepsWithGold[i].row, p.stepsWithGold[i].col);
            setPirate(pirate,loc,animate,view.cells[loc.row][loc.col].count,p.index);
            if (p.steps.length > 0 || p.stepsWithGold.length > 0) {
                selPirate = pirate;
                numToMove++;
            }
    });
    refreshSelectableFieldCell();
    if (numToMove == 1) {
        selectPirate(selPirate);
    }
}

function initGame() {
    $(window).keydown(e=>{
        if (e.keyCode == 27) { //esc
            unselectPirate();
        }
        if (e.shiftKey) selectWithGold();
        else unselectWithGold();
     });
     $(window).keyup(e=>{
        unselectWithGold();
     });

    send("init",{});
}


$(function(){
    initField();
    initPanel();
    initGame();
});
