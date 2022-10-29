/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package earthsubstrate;

import java.util.Set;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/**
 *
 * @author Christopher Brett
 */
public class UserInputSecurity {

    protected static JSONObject sanitizeAgainstCrossScripting(JSONObject dirtyJSONObject) {
        JSONObject cleanJSONObject = new JSONObject();
        try {
            Set<String> allNamesInThisObject = dirtyJSONObject.keySet();
            for (String key : allNamesInThisObject) {
                if(dirtyJSONObject.get(key).getClass().equals(String.class)){
                    Document document = new Cleaner(Safelist.simpleText()).clean(Jsoup.parse((String) dirtyJSONObject.get(key)));
                    document.outputSettings().escapeMode(EscapeMode.xhtml);
                    cleanJSONObject.put(key, document.body().html());
                }else{
                    cleanJSONObject.put(key, dirtyJSONObject.get(key));
                }
            }
            return cleanJSONObject;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return new JSONObject();
    }
}
