package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.game.Player;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;

public class PlayerInfoRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final Gson JsonConvert = new Gson();

    public PlayerInfoRequestHandler(Map<Type, Object> context) {
        super(context);

    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            String participantId = getQueryParameterSafely(queryDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            responseWriter.writeJsonResponse(JSON.toJSONString(resourceOwner.GetUserInfo()));

        } else {
            throw new HttpProcessingException(404);
        }
    }




}
