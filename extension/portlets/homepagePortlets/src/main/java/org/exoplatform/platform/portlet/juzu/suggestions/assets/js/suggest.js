function sortByContacts(a, b){
    return b.number - a.number;
}
function dynamicSort(property) {
    var sortOrder = 1;
    if(property[0] === "-") {
        sortOrder = -1;
        property = property.substr(1, property.length - 1);
    }
    return function (a,b) {
        var result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
        return result * sortOrder;
    }
}

Array.prototype.shuffle = function() {
    var len = this.length;
    var i = len;
    while (i--) {
        var p = parseInt(Math.random()*len);
        var t = this[i];
        this[i] = this[p];
        this[p] = t;
    }
};

$(function() {
    var member;
    var connect ;
    var connection;
    var private;
    var public;
    var spacemember;
    var request;
    var join;
    $(".var").each(function() {
        member = $(this).data("member");
        connect = $(this).data("connect");
        connection = $(this).data("connection");
        private = $(this).data("private");
        public=  $(this).data("public");
        spacemember=  $(this).data("spacemember");
        join=  $(this).data("join");
        request=  $(this).data("request");

    });



    $.getJSON("/rest/homepage/intranet/people/contacts/suggestions", function(items){

        if (items.length > 0){
            $("#content").show();
            $("#peopleSuggest").show();

        }

        items.sort(dynamicSort("suggestionName"));
        // sort my most contacts instead of random
        items.sort(sortByContacts);

        $.each(items, function(i, item){

            var link = "";
            if (i < 2)
            { link += "<li id='"+item.suggestionId+"'>";}
            else
            { link += "<li style='display:none;' id='"+item.suggestionId+"'>" }

            link += "<div class='peoplePicture' ><a href='#'><img src='"+item.avatar+"'></a></div>";
            link += "<div class='peopleInfo'>";
            link += "<div class='peopleName'><a href='"+item.profile+"' target='_parent'>"+item.suggestionName+"</a></div>";
            link += "<div style='display:none;' class='peopleAction' ><a class='connect' href='#' onclick='return false'>"+connect+"</a> | <a class='ignore' href='#' onclick='return false'><img src='/homepage-portlets/style/images/deny.png' alt='' title=''></a></div>";
            link +="<div class='peoplePosition'>"+item.title+"</div><div class='peopleConnection'>"+item.number+"&nbsp;"+connection+"</div>";
            link += "</div></li>";

            $("#suggestions").append(link);

            $("#"+item.suggestionId).mouseover(function(){
                $("#"+item.suggestionId+" .peopleName").addClass("actionAppears");;
                $("#"+item.suggestionId+" .peopleAction").show();
            });
            $("#"+item.suggestionId).mouseout(function(){

                $("#"+item.suggestionId+" .peopleAction").hide();
                $("#"+item.suggestionId+" .peopleName").removeClass("actionAppears");;
            });

            $("#"+item.suggestionId+" a.connect").live("click", function(){
                $.getJSON("/rest/homepage/intranet/people/contacts/connect/"+item.suggestionId, null);

                if($("#suggestions").children().length == 1) {
                    $("#peopleSuggest").fadeOut(500, function () {
                        $("#"+item.relationId).remove();
                        $("#peopleSuggest").hide();
                        if ($("#spaceSuggest").is(":hidden")){
                            $("#content").hide();
                        }


                    });
                }
                else {
                    $("#"+item.suggestionId).fadeOut(500, function () {
                        $("#"+item.suggestionId).remove();
                        $('#suggestions li:hidden:first').fadeIn(500, function() {});

                    });
                }
            });

            $("#"+item.suggestionId+" a.ignore").live("click", function(){
                //$.getJSON("/rest/homepage/intranet/people/contacts/ignore/"+item.suggestionId, null);
                if($("#suggestions").children().length == 1) {
                    $("#peopleSuggest").fadeOut(500, function () {
                        $("#"+item.relationId).remove();
                        $("#peopleSuggest").hide();
                        if ($("#spaceSuggest").is(":hidden")){
                            $("#content").hide();
                        }


                    });
                }
                else {
                    $("#"+item.suggestionId).fadeOut(500, function () {
                        $("#"+item.suggestionId).remove();
                        $('#suggestions li:hidden:first').fadeIn(500, function() {});

                    });
                }
            });






        });
    });





    $.getJSON("/rest/homepage/intranet/spaces/suggestions", function(items){

        if (items.length > 0){
            $("#content").show();
            $("#spaceSuggest").show();
        }

        items.shuffle();

        items.sort(dynamicSort("displayName"));
        // sort my most contacts instead of random
        items.sort(sortByContacts);

        $.each(items, function(i, item){

            var link = "";

            if (i < 2)
            { link += "<li id='"+item.spaceId+"'>";}
            else
            { link += "<li style='display:none;' id='"+item.spaceId+"'>" }

            link += "<div class='spacePicture' ><a href='#'><img src='"+item.avatarUrl+"'></a></div>";
            link += "<div class='spaceInfo'>";
            link += "<div class='spaceName'><a href='/portal/intranet/all-spaces' target='_parent'>"+item.displayName+"</a></div>";
            if(item.privacy=="Private")
            link += "<div class='spacePrivacy'><img src='/homepage-portlets/style/images/user_group.png'>"+private+"&nbsp;-&nbsp;"+item.members+"&nbsp;"+spacemember+"</div>";
            else
                link += "<div class='spacePrivacy'><img src='/homepage-portlets/style/images/user_group.png'>"+public+"&nbsp;-&nbsp;"+item.members+"&nbsp;"+spacemember+"</div>";
            if(item.registration == "open")
                link += "<div class='spaceAction' ><a class='connect' href='#' onclick='return false'>"+join+"</a>";
            else
                link += "<div class='spaceAction' ><a class='connect' href='#' onclick='return false'>"+request+"</a>";

            link += "<a class='ignore' href='#' onclick='return false'><img src='/homepage-portlets/style/images/deny.png' alt='' title=''></a></div>";
            link += "<div class='spaceCommon'>"+item.number+"&nbsp;"+member+"</div>";
            link += "</div></li>";

            $("#suggestionsspace").append(link);

            $("#"+item.spaceId).mouseover(function(){
                $("#"+item.spaceId+" .spacePrivacy").addClass("actionspaceAppears");;
                $("#"+item.spaceId+" .spaceAction").show();
            });
            $("#"+item.spaceId).mouseout(function(){

                $("#"+item.spaceId+" .spaceAction").hide();
                $("#"+item.spaceId+" .spacePrivacy").removeClass("actionspaceAppears");;
            });


            $("#"+item.spaceId+" a.connect").live("click", function(){
                if(item.registration == "open")
                    $.getJSON("/rest/homepage/intranet/spaces/join/"+item.spaceId, function(){
                        window.parent.location.href="/portal/g/:spaces:"+item.name;
                    });
                else
                    $.getJSON("/rest/homepage/intranet/spaces/request/"+item.spaceId, null);

                if($("#suggestionsspace").children().length == 1) {
                    $("#spaceSuggest").fadeOut(500, function () {
                        $("#"+item.spaceId).remove();
                        $("#spaceSuggest").hide();
                        if ($("#peopleSuggest").is(":hidden")){
                            $("#content").hide();
                        }


                    });
                }
                else {
                    $("#"+item.spaceId).fadeOut(500, function () {
                        $("#"+item.spaceId).remove();
                        $('#suggestionsspace li:hidden:first').fadeIn(500, function() {});

                    });
                }
            });

            $("#"+item.spaceId+" a.ignore").live("click", function(){
                //$.getJSON("/rest/homepage/intranet/people/contacts/ignore/"+item.suggestionId, null);
                if($("#suggestionsspace").children().length == 1) {
                    $("#spaceSuggest").fadeOut(500, function () {
                        $("#"+item.spaceId).remove();
                        $("#spaceSuggest").hide();
                        if ($("#peopleSuggest").is(":hidden")){
                            $("#content").hide();
                        }


                    });
                }
                else {
                    $("#"+item.spaceId).fadeOut(500, function () {
                        $("#"+item.spaceId).remove();
                        $('#suggestionsspace li:hidden:first').fadeIn(500, function() {});

                    });
                }
            });

        });
    });

});