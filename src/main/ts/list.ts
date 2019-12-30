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
        names.push($("#name"+team).val());
    }
    send("new",names);
}
function setList(list:any) {
    let listDiv = $("#list");

    let games:any[] = list.games;
    for(let game of games) {
        listDiv.append(
                $("<div/>")
                    .addClass("tile highlightable")
                    .click(game.id, (event) => { click(event.data)})
                    .append(
                        "" + game.from + " - " + game.to + " - " + (1+game.last) + (game.last>0 ? " turn" : " turns")
                    )
        );
    }
}

function startNew(res:any) {
    click(res.id);
}

function validInput() {
    let res = true;
    for(let team=0;team<4;team++) {
        let value:string = <string>$("#name"+team).val();
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

    $("input").change(changeInput);
    send("list",{});

    $("#new").click(newClick);

    $("#sendnew").click(sendNew);
});