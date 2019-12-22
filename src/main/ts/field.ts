let maxGold = 40;
let maxPirateText = 4*3+3;
function initField() {
    let field = $('#field');
    Loc.ALL.forEach(loc => {
        let cell = setCellLoc($("<img/>"),loc)
                        .attr("id","cell"+loc.index())
                        .addClass("cell");
         field.append(cell);
    });
    let svg = $("<svg/>")
                    .attr("id","svg")
                    .addClass("field");
    field.append(svg);

    for (let i=0; i<maxGold; i++) {
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

    for(let id of HeroId.ALL) {
        svg.append( heroEl(id)
                    .attr("id","hero"+id.index())
                    .hide()
                  );
    };

    for(let i=0;i<maxPirateText;i++){
        svg.append($("<text fill='black'/>")
                    .attr("id","piratetext"+i)
                    .hide()
        );
    }

    field.html(field.html()); // workaround to fix svg element
    Loc.ALL.forEach(loc => {
         let front = setCellLoc($("<div/>"),loc)
                        .attr("id","front"+loc.index())
                        .addClass("cell");
         front.click(loc, event => {fieldClick(event.data)});
         field.append(front);
    });
}

function hero(hero:Hero) {return $("#hero"+hero.id.index())}
function front(loc:Loc) {return $("#front"+loc.index())}
function cell(loc:Loc) {return $("#cell"+loc.index())}
function gold(index:number) {return $("#gold"+index)}
function goldText(index:number) {return $("#goldtext"+index)}

function refreshSelectableFieldCell() {
    $(".frontSelectable").removeClass("frontSelectable");
    for(let hero of Hero.heroes) {
        if(! hero.canGo()) continue;
            let f = front(hero.loc);
            if (  !( f.hasClass("frontSelected") || f.hasClass("frontSelectedWithGold") )) {
                f.addClass("frontSelectable");
            }
    }
}

function selectCell(loc:Loc,withGold:boolean) {
    cell(loc).addClass("fieldSelected");
    front(loc).addClass(withGold ? "frontSelectedWithGold":"frontSelected");
}
function unselectCell(loc:Loc) {
    cell(loc).removeClass("fieldSelected");
    front(loc).removeClass("frontSelected");
    front(loc).removeClass("frontSelectedWithGold");
}
function isSelected(loc:Loc) {
    return cell(loc).hasClass("fieldSelected");
}
function isSelectable(loc:Loc) {
    return front(loc).hasClass("frontSelectable");
}

function switchSelectedHero() {
    if (selHero == undefined) return;

    let loc = selHero.loc;
    let firstHero:Hero = undefined;
    let foundHero:Hero = undefined;
    let next = false;
    for(let h of Hero.heroes) {
        if (! loc.equals(h.loc)) continue;
        if (h.equals(selHero)) {
            next = true;
        } else if (next) {
            foundHero = h;
            break;
        } else if (firstHero == undefined) {
            firstHero = h;
        }
    }

    if (foundHero != undefined) {
        selectHero(foundHero);
    } else if (firstHero != undefined) {
        selectHero(firstHero);
    }
}

function fieldClick(loc:Loc) {
    if (isSelected(loc)) {
        let go={id:id, hero:selHero.id, loc:loc, withGold:withGold};
        send("go",go);
    } else if (isSelectable(loc)) {
        if (selHero == undefined ||
                ! selHero.loc.equals(loc) ) {
            let h = Hero.heroes.find( h => { return h.loc.equals(loc)});
            if (h != undefined) selectHero(h);
        } else {
            switchSelectedHero();
        }
    }
}
function hideHero(h:Hero) {
    hero(h).hide();
}


const heroDelta = [
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
const heroTextDelta = [-3,5];

const moneyEllipseBottomDelta = [52,57];
const moneyEllipseDelta = [-4,2];
const moneyTextDelta = [-4,5];
const moneyText10Delta = [-8,5];

function setHero(h:Hero, animate:boolean) {
    if (h.hidden) {
        hideHero(h);
        return;
    }

    let el = hero(h);
    let hd = heroDelta[h.count-1][h.index];
    let attr={cy:h.loc.row*LEN+hd[1],cx:h.loc.col*LEN+hd[0]};
    el = el.show();

    if (animate) el.animate(attr,500);
    else el.css(attr);
    let txt;
    if (h.count>1) {
        txt = $("#piratetext"+pirateTextIndex)
                .attr({x:attr.cx+heroTextDelta[0], y:attr.cy+heroTextDelta[1]})
                .empty().append(""+(h.index+1));
        if (animate) txt.show(1000);
        else txt.show();
        pirateTextIndex++;
    }
    return el;
}

const attrName = ["cx","cy","r","fill","stroke","stroke-width"];
const cssName = ["cx","cy"];

function showHeroOnTop(h:Hero) {
    /*let id = hero(h).attr("id");
    let lastId = $("#svg circle:last").attr("id");
    let sid = "#" + id;
    let slastId = "#" + lastId;

    if (id == lastId) return;
    for (let name of attrName) {
        let value = $(sid).attr(name);
        let valueLast = $(slastId).attr(name);

        $(sid).attr(name, valueLast);
        $(slastId).attr(name, value);
    }

    for (let name of cssName) {
        let value = $(sid).css(name);
        let valueLast = $(slastId).css(name);

        $(sid).css(name, valueLast);
        $(slastId).css(name, value);
    }

    $(sid).attr("id",lastId);
    $("#svg circle:last").attr("id",id);*/
}

function selectFieldHero(h:Hero) {
    hero(h)
        .attr({'stroke-width':'4',stroke:getHeroSelectedBorderColor(h.id)});
    showHeroOnTop(h);
}
function unselectFieldHero(h:Hero) {
    hero(h)
        .attr({'stroke-width':'2',stroke:'black'});
}


let goldIndex = 0;
let pirateTextIndex = 0;
function resetGold() {
    for (let i=0;i<goldIndex;i++) {
        gold(i).hide();
        goldText(i).hide();
    }
    goldIndex = 0;

    for(let i=0;i<pirateTextIndex;i++) {
        $("#piratetext"+i).hide();
    }
    pirateTextIndex = 0;
}
function showGold(loc:Loc,count:number,index:number,g:number) {
    if (g==0) return;
    let p0 = count==1 ? moneyEllipseBottomDelta : heroDelta[count-1][index];
    let x=loc.col*LEN + p0[0]+moneyEllipseDelta[0];
    let y=loc.row*LEN + p0[1]+moneyEllipseDelta[1];
    gold(goldIndex).attr({cx:x,cy:y}).show();

    let dp = g<10 ? moneyTextDelta : moneyText10Delta;
    x+= dp[0];
    y+= dp[1];

    goldText(goldIndex).attr({x:x,y:y})
        .empty().append(""+g).show();

    goldIndex++;
}
