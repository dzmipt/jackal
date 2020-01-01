function click(id:string) {
    window.location.href = "/game.html?id="+id;
}
function newClick() {
    $("#newgame").show();
}
function sendNew() {
    if (!validInput()) return;
    let names = [];
    for(let team=0;team<4;team++) {
        names.push($("#jackalname"+team).val());
    }

    let friends:number[] = [];
    let colIds:string[] = [];
    for (let team=0; team<4; team++) {
        let id = $("#line"+team).parent().attr("id");
        let idx = colIds.indexOf(id);
        if (idx == -1) {
            idx = colIds.push(id) - 1;
        }
        friends.push(idx);
    }
    send("new",{names:names, friends:friends});
}
function setList(list:any) {
    let listDiv = $("#list");

    let games:any[] = list.games;
    for(let game of games) {
        let gameDiv = $("<div/>")
                    .addClass("tile highlightable")
                    .click(game.id, (event) => { click(event.data)})
                    .append(
                        "" + game.from + " - " + game.to + " - " + (1+game.last) + (game.last>0 ? " turn" : " turns")
                    );
        for(let team=0;team<4;team++) {
            gameDiv.append($("<img/>").attr("src","/img/team"+team+".png").addClass("teamImg"));
            gameDiv.append(game.teamNames[team]);
        }

        listDiv.append(gameDiv);
    }
}

function startNew(res:any) {
    click(res.id);
}

function validInput() {
    let res = true;
    for(let team=0;team<4;team++) {
        let value:string = <string>$("#jackalname"+team).val();
        res = res && value.trim().length>0;
    }
    return res;
}

function changeInput() {
    let button = $("#sendnew");
    if (validInput() ) {
        button.addClass("highlightable").removeClass("disabled");
    } else {
        button.removeClass("highlightable").addClass("disabled");
    }
}

$(function(){
    addSubscription("list",setList);
    addSubscription("new",startNew);
    $( "#col0, #col1, #col2, #col3" ).sortable({
      connectWith: ".column"
    }).disableSelection();

    $("input").change(changeInput);
    send("list",{});

    $("#new").click(newClick);

    $("#sendnew").click(sendNew);
});