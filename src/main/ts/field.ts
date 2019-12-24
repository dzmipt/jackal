let maxGold = 40;
let maxPirateText = 4*3+3;
function initField() {
    let cells = $('#cells');
    Loc.ALL.forEach(loc => {
        let cell = setCellLoc($("<img/>"),loc)
                        .attr("id","cell"+loc.index())
                        .addClass("cell");
         cells.append(cell);
    });
    let svg = $("#svg");

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
    let field = $("#field");
    field.html(field.html()); // workaround to fix svg element

    let heroes = $("#heroes");
    for(let id of HeroId.ALL) {
        heroes.append( heroEl(id)
                    .attr("id","hero"+id.index())
                    .hide()
                  );
    };

    let fronts = $("#fronts");
    Loc.ALL.forEach(loc => {
         let front = setCellLoc($("<div/>"),loc)
                        .attr("id","front"+loc.index())
                        .css("z-index","10")
                        .addClass("cell");
         front.click(loc, event => {fieldClick(event.data)});
         fronts.append(front);
    });
}

function getHeroImgSrc(id:HeroId, selected:boolean) {
    let src:string = id.num<3 ? "team"+id.team : "hero"+id.num;
    if (selected) src = src + "sel";
    return "/img/"+src+".png";
}
function heroEl(id:HeroId) {
    return $("<img/>")
                .attr("src",getHeroImgSrc(id, false))
                .css("position","absolute");
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

function heroZLevel(h:Hero, level:number) {
    hero(h).css("z-index",""+level);
}

function selectFieldHero(h:Hero) {
    hero(h).attr("src",getHeroImgSrc(h.id,true));
    heroZLevel(selHero, 2);
}
function unselectFieldHero(h:Hero) {
    hero(h).attr("src",getHeroImgSrc(h.id,false));
    heroZLevel(selHero, 1);
}

type Point = [number,number];

// [x,y]
const heroCenter:Point[][] = [
    [ [35,35] ], // 1
    [], // 2
    [], // 3
    [], // 4
    [ [63,11],
      [33,11],
      [16,31],
      [22,55],
      [58,55] ]  // 5
];

// [dx,dy]
const heroDelta:Point[][] = [
    [ [0,0] ], // 1
    [ [-1,0], [1,0] ], // 2
    [ [-1,0], [0,0], [1,0] ], // 3
    [ [-1,-1],[1,-1],[-1,1], [1,1] ], // 4
    [ [-1,-1],[1,-1],[0,0], [-1,1], [1,1] ], // 5
    [ [-1,-1],[0,-1],[1,-1], [-1,1], [0,1], [1,1] ], // 6
    [ [-1,-1],[0,-1],[1,-1], [0,0], [-1,1], [0,1], [1,1] ], // 7
    [ [-1,-1],[0,-1],[1,-1], [-1,0], [0,0], [-1,1], [0,1], [1,1] ], // 8
    [ [-1,-1],[0,-1],[1,-1], [-1,0], [0,0], [1,0], [-1,1], [0,1], [1,1] ], // 8
];

const heroSpace = [
    25, // 1
    5, // 2
    5, // 3
    5, // 4
    5  // 5
]

const moneyEllipseBottomDelta = [52,57];
const moneyEllipseDelta = [-4,2];
const moneyTextDelta = [-4,5];
const moneyText10Delta = [-8,5];

function add(x:[number,number], y:[number,number]):[number,number] {
    return [ x[0]+y[0], x[1]+y[1] ];
}

function addC(x:[number,number], c:number):[number,number] {
    return [ x[0]+c, x[1]+c ];
}

function mul(k:number, x:[number,number]):[number,number] {
    return [ k*x[0], k*x[1]];
}


function setHero(h:Hero, pos:number, count:number, animate:boolean) {
    if (h.hidden) {
        hideHero(h);
        return;
    }

    let el = hero(h);
    let space = heroSpace[h.count-1];
    let hd:Point = mul(space, heroDelta[count>9 ? 8 : count-1] [count>9 ? 8 : pos]);

    let hc:Point = add(hd, addC( heroCenter[h.count-1][h.index], -10) );


    let attr={top:h.loc.row*LEN+hc[1],left:h.loc.col*LEN+hc[0]};
    el = el.show();

    if (animate) el.animate(attr,500);
    else el.css(attr);
    return el;
}

let goldIndex = 0;
function resetGold() {
    for (let i=0;i<goldIndex;i++) {
        gold(i).hide();
        goldText(i).hide();
    }
    goldIndex = 0;
}

function showGold(loc:Loc,count:number,index:number,g:number) {
    if (g==0) return;
    let p0 = count==1 ? moneyEllipseBottomDelta : heroCenter[count-1][index];
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
