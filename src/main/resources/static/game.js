/*const iconSEA = "SEA";
const iconSHIP = "SHIP";
const iconCLOSED = "CLOSED";
*/
//let iconSrc = {};
let LEN = 70;

const pirateColor = ["white","yellow","red","black"];

function getCoordinate(row,col) {
    return {left:col*LEN,top:row*LEN};
}
function setCellLoc(el,row,col) {
    return el.css(getCoordinate(row,col));
}
function addEventListeners(el,s,r,c) {
         let data = {src:s,row:r,col:c};
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
        if (overData.src=="field") fieldLeave(overData.row,overData.col);
        else if (overData.src=="panel") panelLeave(overData.row,overData.col);
    }

    if (data != overData) return;
    overData = undefined;
}
function over(data) {
    leave(overData);
    overData=data;

    if (data.src=="field") fieldOver(data.row,data.col);
    else if (data.src=="panel") panelOver(data.row,data.col);
}

function click(data) {
    if (data.src=="field") fieldClick(data.row,data.col);
    else if (data.src=="panel") panelClick(data.row,data.col);
}

function init(icons) {
    icons.forEach(data=>iconSrc[data.value]="/img/"+data.location);
    console.log("iconSrc: "+iconSrc);
}

let id="";
let pirates = undefined;


function setIcons(icons) {
    for(row=0;row<13;row++){
        for(col=0;col<13;col++){
            cell(row,col).attr("src", iconSrc[icons[row][col]]);
        }
    }
}

function setView(view) {
    id = view.id;
    unselectPirate();
    resetGold();
    let animate = view.animateShip == null;
    for(row=0;row<13;row++){
        for(col=0;col<13;col++){
            cell(row,col).attr("src", "/img/"+view.cells[row][col].icon+".png");
            let count = view.cells[row][col].count;
            for(i=0;i<count; i++) {
                showGold(row,col,count,i,view.cells[row][col].gold[i]);
            }
        }
    }
    let selPirate = undefined;
    let numToMove = 0;
    pirates = view.pirates;
    for(team=0;team<4;team++) {
        for(num=0;num<3;num++) {
            let p = pirates[team][num];
            let loc = p.loc;
            setPirate(team,num,loc.row,loc.col,animate,view.cells[loc.row][loc.col].count,p.index);
            if (p.steps.length > 0 || p.stepsWithGold.length > 0) {
                selPirate = {team:team,num:num};
                numToMove++;
            }
        }
    }
    refreshSelectableFieldCell();
    if (numToMove == 1) {
        selectPirate(selPirate.team, selPirate.num);
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