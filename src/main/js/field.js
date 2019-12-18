let maxGold = 40;
let maxPirateText = 4*3+3;
function initField() {
    let field = $('#field');
    LocAll.forEach(loc => {
        let cell = setCellLoc($("<img/>"),loc)
                        .attr("id","cell"+loc.index())
                        .addClass("cell");
         field.append(cell);
    });
    let svg = $("<svg/>")
                    .attr("id","svg")
                    .addClass("field");
    field.append(svg);

    for (i=0; i<maxGold; i++) {
        svg.append(
            goldEl()
                .attr("id","gold"+i)
                .hide()
        );
        svg.append(
            goldTextEl()
                .attr("id","goldtext"+i)
                .hide()
        );
    }

    PirateAll.forEach(pirate => {
            svg.append( pirateEl(pirate.team)
                        .attr("id","pirate"+pirate.index())
                        .hide()
                      );
    });

    for(i=0;i<maxPirateText;i++){
        svg.append($("<text fill='black'/>")
                    .attr("id","piratetext"+i)
                    .hide()
        );
    }

    field.html(field.html()); // workaround to fix svg element
    LocAll.forEach(loc => {
         let front = setCellLoc($("<div/>"),loc)
                        .attr("id","front"+loc.index())
                        .addClass("cell");
         front = addEventListeners(front,"field",loc);
         field.append(front);
    });
}

function pirate(p) {return $("#pirate"+p.index())}
function front(loc) {return $("#front"+loc.index())}
function cell(loc) {return $("#cell"+loc.index())}
function gold(index) {return $("#gold"+index)}
function goldText(index) {return $("#goldtext"+index)}

function refreshSelectableFieldCell() {
    $(".frontSelectable").removeClass("frontSelectable");
    PirateAll.forEach(pirate => {
            let p = pirates[pirate.team][pirate.num];
            if (p.steps.length == 0 && p.stepsWithGold.length == 0) return;
            let loc = p.loc;
            let f = front(loc);
            if (  !( f.hasClass("frontSelected") || f.hasClass("frontSelectedWithGold") )) {
                f.addClass("frontSelectable");
            }
    });
}

function selectCell(loc,withGold) {
    cell(loc).addClass("fieldSelected");
    front(loc).addClass(withGold ? "frontSelectedWithGold":"frontSelected");
}
function unselectCell(loc) {
    cell(loc).removeClass("fieldSelected");
    front(loc).removeClass("frontSelected");
    front(loc).removeClass("frontSelectedWithGold");
}
function isSelected(loc) {
    return cell(loc).hasClass("fieldSelected");
}
function isSelectable(loc) {
    return front(loc).hasClass("frontSelectable");
}
function fieldOver(loc) {
    if(isSelected(loc)) front(loc).addClass("over");
}
function fieldLeave(loc) {
    front(loc).removeClass("over");
}
function fieldClick(loc) {
    if (isSelected(loc)) {
        go={id:id, pirate:selPirate, loc:loc, withGold:withGold};
        send("go",go);
    } else if (isSelectable(loc)) {
        let p = PirateAll.find( p => {
               return pirates[p.team][p.num].loc.equals(loc);
        });
        if (p != undefined) selectPirate(p);
    }
}
function hidePirate(p) {
    pirate(p).hide();
}


const pirateDelta = [
    [ [35,35] ], // 1
    [], // 2
    [], // 3
    [], // 4
    [ [63,11],
      [33,11],
      [16,31],
      [22,55],
      [58,55] ]  // 5
]
const pirateTextDelta = [-3,5];

const moneyEllipseBottomDelta = [52,57];
const moneyEllipseDelta = [-4,2];
const moneyTextDelta = [-4,5];
const moneyText10Delta = [-8,5];

function setPirate(p,loc,animate,count,index) {
    let el = pirate(p);
    let pd = pirateDelta[count-1][index];
    let attr={cy:loc.row*LEN+pd[1],cx:loc.col*LEN+pd[0]};
    el = el.show();

    if (animate) el.animate(attr,500);
    else el.css(attr);
    let txt;
    if (count>1) {
        txt = $("#piratetext"+pirateTextIndex)
                .attr({x:attr.cx+pirateTextDelta[0], y:attr.cy+pirateTextDelta[1]})
                .empty().append(index+1);
        if (animate) txt.show(1000);
        else txt.show();
        pirateTextIndex++;
    }
    return el;
}


let goldIndex = 0;
let pirateTextIndex = 0;
function resetGold() {
    for (i=0;i<goldIndex;i++) {
        gold(i).hide();
        goldText(i).hide();
    }
    goldIndex = 0;

    for(i=0;i<pirateTextIndex;i++) {
        $("#piratetext"+i).hide();
    }
    pirateTextIndex = 0;
}
function showGold(loc,count,index,g) {
    if (g==0) return;
    let p0 = count==1 ? moneyEllipseBottomDelta : pirateDelta[count-1][index];
    let x=loc.col*LEN + p0[0]+moneyEllipseDelta[0];
    let y=loc.row*LEN + p0[1]+moneyEllipseDelta[1];
    gold(goldIndex).attr({cx:x,cy:y}).show();

    let dp = g<10 ? moneyTextDelta : moneyText10Delta;
    x+= dp[0];
    y+= dp[1];

    goldText(goldIndex).attr({x:x,y:y})
        .empty().append(g).show();

    goldIndex++;
}
