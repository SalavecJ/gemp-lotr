package com.gempukku.lotro.bots.rl.v2.learning;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.util.JsonSerializable;

import java.util.Arrays;
import java.util.List;

public class SavedVector implements JsonSerializable {
    public final String className;
    public final double[] state;
    public final double[] chosen;
    public final List<double[]> notChosen;
    public double reward;

    public SavedVector(String className, double[] state, double[] chosen, List<double[]> notChosen) {
        this.className = className;
        this.state = state;
        this.chosen = chosen;
        this.notChosen = notChosen;
    }
    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("className", className);
        obj.put("state", state);
        obj.put("chosen", chosen);
        obj.put("reward", reward);
        obj.put("notChosen", notChosen.toArray());
        return obj;
    }

    public static SavedVector fromJson(String json) {
        JSONObject obj = JSON.parseObject(json);
        String className = obj.getString("className");
        double[] state = obj.getObject("state", double[].class);
        double[] chosen = obj.getObject("chosen", double[].class);
        List<double[]> notChosen = Arrays.asList(obj.getObject("notChosen", double[][].class));
        double reward = obj.getDoubleValue("reward");

        SavedVector tbr = new SavedVector(className, state, chosen, notChosen);
        tbr.reward = reward;
        return tbr;
    }
}
