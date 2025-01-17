var GempLotrCommunication = Class.extend({
    url:null,
    failure:null,

    init:function (url, failure) {
        this.url = serverDomain + url;
        this.failure = failure;
    },

    errorCheck:function (errorMap) {
        var that = this;
        return function (xhr, status, request) {
            var errorStatus = "" + xhr.status;
            if (errorMap != null && errorMap[errorStatus] != null)
                errorMap[errorStatus](xhr, status, request);
            else if (""+xhr.status != "200")
                that.failure(xhr, status, request);
        };
    },

    logout:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/logout",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap)
        });
    },

    getDelivery:function (callback) {
        $.ajax({
            type:"GET",
            url:this.url + "/delivery",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:callback,
            error:null,
            dataType:"xml"
        });
    },

    deliveryCheck:function (callback) {
        var that = this;
        return function (xml, status, request) {
            var delivery = request.getResponseHeader("Delivery-Service-Package");
            if (delivery == "true" && window.deliveryService != null)
                that.getDelivery(window.deliveryService);
            callback(xml);
        };
    },

    getGameHistory:function (start, count, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/gameHistory",
            cache:false,
            data:{
                start:start,
                count:count,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getStats:function (startDay, length, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/stats",
            cache:false,
            data:{
                startDay:startDay,
                length:length,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getPlayerStats:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/playerStats",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getLiveTournaments:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getHistoryTournaments:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament/history",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getTournament:function (tournamentId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament/" + tournamentId,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getLeagues:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/league",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getLeague:function (type, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/league/" + type,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    joinLeague:function (code, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/league/" + code,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getReplay:function (replayId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/replay/" + replayId,
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    startGameSession:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{ participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    updateGameState:function (channelNumber, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getGameCardModifiers:function (cardId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/game/" + getUrlParam("gameId") + "/cardInfo",
            cache:false,
            data:{
                cardId:cardId,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    gameDecisionMade:function (decisionId, response, channelNumber, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId"),
                decisionId:decisionId,
                decisionValue:response },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    concede:function (errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId") + "/concede",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    cancel:function (errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId") + "/cancel",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getDeck:function (deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    shareDeck:function (deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/share",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getLibraryDeck:function (deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/library",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getDecks:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/list",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getLibraryDecks:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/libraryList",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getCollectionTypes:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getMerchant:function (filter, ownedMin, start, count, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/merchant",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                filter:filter,
                ownedMin:ownedMin,
                start:start,
                count:count},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    buyItem:function (blueprintId, price, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/buy",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId,
                price:price},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    sellItem:function (blueprintId, price, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/sell",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId,
                price:price},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    tradeInFoil:function (blueprintId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/tradeFoil",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getCollection:function (collectionType, filter, start, count, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                filter:filter,
                start:start,
                count:count},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    importCollection:function (decklist, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection/import/",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                decklist:decklist},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    openPack:function (collectionType, pack, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                pack:pack},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    openSelectionPack:function (collectionType, pack, selection, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                pack:pack,
                selection:selection},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    saveDeck:function (deckName, targetFormat, notes, contents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck",
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName,
                targetFormat:targetFormat,
                notes:notes,
                deckContents:contents},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    renameDeck:function (oldDeckName, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/rename",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                oldDeckName:oldDeckName,
                deckName:deckName},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    deleteDeck:function (deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/delete",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getDeckStats:function (contents, targetFormat, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/stats",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                targetFormat:targetFormat,
                deckContents:contents},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getFormats:function (includeEvents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/formats",
            cache:true,
            data:{ 
                includeEvents:includeEvents
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    startChat:function (room, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/chat/" + room,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    updateChat:function (room, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/chat/" + room,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    sendChatMessage:function (room, messages, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/chat/" + room,
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId"),
                message:messages},
            traditional:true,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getHall:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    updateHall:function (callback, channelNumber, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/update",
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    joinQueue:function (queueId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/queue/" + queueId,
            cache:false,
            data:{
                deckName:deckName,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    leaveQueue:function (queueId, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/queue/" + queueId + "/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    dropFromTournament:function(tournamentId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/tournament/" + tournamentId + "/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    joinTable:function (tableId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/" + tableId,
            cache:false,
            data:{
                deckName:deckName,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    leaveTable:function (tableId, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/"+tableId+"/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    createTable:function (format, deckName, timer, desc, isPrivate, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall",
            cache:false,
            data:{
                format:format,
                deckName:deckName,
                timer:timer,
                desc:desc,
                isPrivate:isPrivate,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getFormat:function (formatCode, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall/format/" + formatCode,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getFormatRules:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall/formats/html",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    addTesterFlag:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/addTesterFlag",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    removeTesterFlag:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/removeTesterFlag",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    getRecentReplays:function (format, count, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/getRecentReplays",
            cache:false,
            data:{
                format:format,
                count:count
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    setShutdownMode:function (shutdown, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/shutdown",
            cache:false,
            data:{
                shutdown:shutdown
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    clearServerCache:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/clearCache",
            cache:false,
            data:{},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    reloadCardDefinitions:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/reloadCards",
            cache:false,
            data:{},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    getMOTD:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/admin/getMOTD",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    setMOTD:function (motd, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/setMOTD",
            cache:false,
            data:{
                motd:motd
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    addItems:function (collectionType, product, players, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addItems",
            cache:false,
            data:{
                collectionType:collectionType,
                product:product,
                players:players
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    permabanUser:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banUser",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    tempbanUser:function (login, duration, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banUserTemp",
            cache:false,
            data:{
                login:login,
                duration:duration
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    unbanUser:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/unBanUser",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    susUserSearch:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/findMultipleAccounts",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    banMultiple:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banMultiple",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    previewSealedLeague:function (format, start, serieDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/previewSealedLeague",
            cache:false,
            data:{
                format:format,
                start:start,
                serieDuration:serieDuration,
                maxMatches:maxMatches,
                name:name,
                cost:cost
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    
    addSealedLeague:function (format, start, serieDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addSealedLeague",
            cache:false,
            data:{
                format:format,
                start:start,
                serieDuration:serieDuration,
                maxMatches:maxMatches,
                name:name,
                cost:cost
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    previewSoloDraftLeague:function (format, start, serieDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/previewSoloDraftLeague",
            cache:false,
            data:{
                format:format,
                start:start,
                serieDuration:serieDuration,
                maxMatches:maxMatches,
                name:name,
                cost:cost
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    
    addSoloDraftLeague:function (format, start, serieDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addSoloDraftLeague",
            cache:false,
            data:{
                format:format,
                start:start,
                serieDuration:serieDuration,
                maxMatches:maxMatches,
                name:name,
                cost:cost
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    previewConstructedLeague:function (start, collectionType, prizeMultiplier, name, cost, formats, serieDurations, maxMatches, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/previewConstructedLeague",
            cache:false,
            data:{
                start:start,
                collectionType:collectionType,
                prizeMultiplier:prizeMultiplier,
                name:name,
                cost:cost,
                format:formats,
                serieDuration:serieDurations,
                maxMatches:maxMatches
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    
    addConstructedLeague:function (start, collectionType, prizeMultiplier, name, cost, formats, serieDurations, maxMatches, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addConstructedLeague",
            cache:false,
            data:{
                start:start,
                collectionType:collectionType,
                prizeMultiplier:prizeMultiplier,
                name:name,
                cost:cost,
                format:formats,
                serieDuration:serieDurations,
                maxMatches:maxMatches
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    
    
    //NEVER EVER EVER use this for actual authentication
    // This is strictly to simplify things like auto-hiding
    // of the admin panel.  If you actually need functionality
    // gated behind authorization, it goes on the server
    // and not in here.
    
    getPlayerInfo:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/player",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    getStatus:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    login:function (login, password, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/login",
            cache:false,
            async:false,
            data:{
                login:login,
                password:password,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    register:function (login, password, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/register",
            cache:false,
            data:{
                login:login,
                password:password,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getRegistrationForm:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:"/gemp-lotr/includes/registrationForm.html",
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getDraft:function (leagueType, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/soloDraft/"+leagueType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    makeDraftPick:function (leagueType, choiceId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/soloDraft/"+leagueType,
            cache:false,
            data:{
                choiceId:choiceId,
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
});
