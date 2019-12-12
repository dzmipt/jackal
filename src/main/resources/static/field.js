let maxGold = 40;
let maxPirateText = 4*3+3;
function initField() {
    let field = $('#field');
    for (row=0; row<13; row++) {
      for (col=0; col<13; col++) {
        let cell = setCellLoc($("<img/>"),row,col)
                        .attr("id","cell"+row+"_"+col)
                        .addClass("cell");
         field.append(cell);
      }
    }
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

    for(team=0;team<4;team++){
        for(num=0;num<3;num++){
            let pirate = pirateEl(team)
                        .attr("id","pirate"+team+"_"+num)
                        .hide();
             svg.append(pirate);
        }
    }

    for(i=0;i<maxPirateText;i++){
        svg.append($("<text fill='black'/>")
                    .attr("id","piratetext"+i)
                    .hide()
        );
    }

    field.html(field.html()); // workaround to fix svg element
    for (row=0; row<13; row++) {
      for (col=0; col<13; col++) {
         let front = setCellLoc($("<div/>"),row,col)
                        .attr("id","front"+row+"_"+col)
                        .addClass("cell");
         front = addEventListeners(front,"field",row,col);
         field.append(front);
      }
    }
}

function pirate(team,num) {return $("#pirate"+team+"_"+num)}
function front(row,col) {return $("#front"+row+"_"+col)}
function cell(row,col) {return $("#cell"+row+"_"+col)}
function gold(index) {return $("#gold"+index)}
function goldText(index) {return $("#goldtext"+index)}

function refreshSelectableFieldCell() {
    $(".frontSelectable").removeClass("frontSelectable");
    for(team=0;team<4;team++) {
        for(num=0;num<3;num++) {
            let p = pirates[team][num];
            if (p.steps.length == 0 && p.stepsWithGold.length == 0) continue;
            let loc = p.loc;
            let f = front(loc.row,loc.col);
            if (  !( f.hasClass("frontSelected") || f.hasClass("frontSelectedWithGold") )) {
                f.addClass("frontSelectable");
            }
        }
    }
}

function selectCell(row,col,withGold) {
    cell(row,col).addClass("fieldSelected");
    front(row,col).addClass(withGold ? "frontSelectedWithGold":"frontSelected");
}
function unselectCell(row,col) {
    cell(row,col).removeClass("fieldSelected");
    front(row,col).removeClass("frontSelected");
    front(row,col).removeClass("frontSelectedWithGold");
}
function isSelected(row,col) {
    return cell(row,col).hasClass("fieldSelected");
}
function isSelectable(row,col) {
    return front(row,col).hasClass("frontSelectable");
}
function fieldOver(row,col) {
    if(isSelected(row,col)) front(row,col).addClass("over");
}
function fieldLeave(row,col) {
    front(row,col).removeClass("over");
}
function fieldClick(row,col) {
    if (isSelected(row,col)) {
        go={id:id, pirate:selPirate, loc:{row:row,col:col},withGold:withGold};
        send("go",go);
    } else if (isSelectable(row,col)) {
        for(team=0;team<4;team++) {
            for(num=0;num<3;num++) {
                let loc = pirates[team][num].loc;
                if(loc.row == row && loc.col == col) {
                    selectPirate(team,num);
                    break;
                }
            }
        }
    }
}
function hidePirate(team,num) {
    pirate(team,num).hide();
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

function setPirate(team,num,row,col,animate,count,index) {
    let el = pirate(team,num);
    let p = pirateDelta[count-1][index];
    let attr={cy:row*LEN+p[1],cx:col*LEN+p[0]};
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
function showGold(row,col,count,index,g) {
    if (g==0) return;
    let p0 = count==1 ? moneyEllipseBottomDelta : pirateDelta[count-1][index];
    let x=col*LEN + p0[0]+moneyEllipseDelta[0];
    let y=row*LEN + p0[1]+moneyEllipseDelta[1];
    gold(goldIndex).attr({cx:x,cy:y}).show();

    let dp = g<10 ? moneyTextDelta : moneyText10Delta;
    x+= dp[0];
    y+= dp[1];

    goldText(goldIndex).attr({x:x,y:y})
        .empty().append(g).show();

    goldIndex++;
}
