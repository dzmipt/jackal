let maxGold = 117;
function initField() {
    let cells = $('#cells');
    Loc.ALL.forEach(loc => {
        let cell = setCellLoc($("<img/>"),loc)
                        .attr({id:"cell"+loc.index(), src:"/img/sea.png"})
                        .addClass("cell");
         cells.append(cell);
    });

    for(let team=0;team<4;team++) {
        cells.append(
            $("<div/>")
                .attr("id","ship"+team)
                .addClass("cell")
                .append(
                    $("<img/>")
                        .attr({src:"/img/ship.png", id:"shipimg"+team})
                        .addClass("cell")
                ).append($("<svg class='field'/>")
                            .append(goldEl()
                                        .attr({id:"gold"+(-team-1),cx:48,cy:59})
                                        .hide()
                            )
                            .append(goldTextEl()
                                        .attr({id:"goldtext"+(-team-1),cx:44,cy:64})
                                        .hide()
                            )
                ).append(
                    $("<img/>")
                        .attr("src","/img/team"+team+".png")
                        .addClass("smallhero")
                        .css({left:58, top:58})
                )
        );
    }

    cells.append(
        $("<img/>")
            .attr({src:"/img/bear.png", id:"bear"})
            .addClass("cell")
            .hide()
    );
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
                    .css("opacity",0.0)
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
function cell(loc:Loc) {
    for(let team=0;team<4;team++){
        if(loc.equals(ships[team])) {
            return $("#shipimg"+team);
        }
    }
    return $("#cell"+loc.index())
}
function ship(team:number) {return $("#ship"+team)}
function gold(index:number) {return $("#gold"+index)}
function goldText(index:number) {return $("#goldtext"+index)}

let ships:Loc[] = undefined;

function setShips(locs:Loc[]) {
    ships = locs;
    for(let team=0;team<4;team++){
        ship(team).animate(getCoordinate(locs[team]),500);
    }
}

function setBear(loc:Loc) {
    if (loc == null) return;
    $("#bear")
        .show()
        .animate(getCoordinate(loc),500);
}

function refreshSelectableFieldCell() {
    $(".frontSelectable").removeClass("frontSelectable");
    for(let hero of Hero.heroes) {
        if(! (hero.hasAction()) )  continue;
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
    $(".fieldSelected").removeClass("fieldSelected");
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
        let go={id:id, heroId:selHero.id, loc:loc, withGold:withGold};
        send("go",go);
    } else if (isSelectable(loc)) {
        selectHeroAtLoc(loc);
    }
}

function heroZLevel(h:Hero, level:number) {
    hero(h).css("z-index",""+level);
}

function selectFieldHero(h:Hero) {
    hero(h).attr("src",getHeroImgSrc(h.id,true));
    heroZLevel(selHero, 3);
}
function unselectFieldHero(h:Hero) {
    hero(h).attr("src",getHeroImgSrc(h.id,false));
    heroZLevel(selHero, 2);
}

type Point = [number,number];

// [x,y]
const heroCenter:Point[][] = [
    [ [25,25] ], // 1
    [ [1,46],[40,0] ], // 2
    [ [47,48], [14,23] ,[47,0] ], // 3
    [ [47,49], [7,34], [39,12], [10,0] ], // 4
    [ [53,1], [23,1], [6,21], [12,45], [48,45] ]  // 5
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
    22, // 1
    5, // 2
    5, // 3
    5, // 4
    5  // 5
]

const moneyEllipseBottomDelta = [42,47];
const moneyEllipseDelta = [6,12];
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


function getTargetAttr(hindex:number, hcount:number, hloc:Loc, pos:number, count:number) {
    let space = heroSpace[hcount-1];
    let hd:Point = mul(space, heroDelta[count>9 ? 8 : count-1] [count>9 ? 8 : pos]);

    let hc:Point = add(hd, heroCenter[hcount-1][hindex] );


    return {top:hloc.row*LEN+hc[1],left:hloc.col*LEN+hc[0]};
}

function setHero(h:Hero, pos:number, count:number) {
    let attr = getTargetAttr(h.index,h.count,h.loc, pos, count);
    if (h.inCave) attr.left += 2;
    attr['opacity'] =  h.hidden || h.dead ? 0.0 : 1.0;

    let el = hero(h);
    if (h.viaLoc != null) {
        let attrVia = getTargetAttr(0,1,h.viaLoc,0,1);
        attrVia['opacity'] = attr['opacity'];
        el.animate(attrVia, 500);
    }
    el.animate(attr,500);
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

function resetShipGold(gold:number[]) {
    for(let team=0;team<4;team++) {
        showGoldByIndex(-team-1, new Loc(0,0), 1,0, gold[team]);
    }
}

function showGoldByIndex(goldIdx:number,loc:Loc,count:number,index:number,g:number) {
    if (g==0) return;
    let p0 = count==1 ? moneyEllipseBottomDelta : heroCenter[count-1][index];
    let x=loc.col*LEN + p0[0]+moneyEllipseDelta[0];
    let y=loc.row*LEN + p0[1]+moneyEllipseDelta[1];
    gold(goldIdx).attr({cx:x,cy:y}).show();

    let dp = g<10 ? moneyTextDelta : moneyText10Delta;
    x+= dp[0];
    y+= dp[1];

    goldText(goldIdx).attr({x:x,y:y})
        .empty().append(""+g).show();
}

function showGold(loc:Loc,count:number,index:number,g:number) {
    if (g==0) return;
    showGoldByIndex(goldIndex,loc,count,index,g);
    goldIndex++;
}
