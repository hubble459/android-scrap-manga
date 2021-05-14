package nl.hubble.scraper.type;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import nl.hubble.scraper.model.Manga;

public class MangaFox extends Query {
    private final String[] accepts = new String[]{"mangapark", "fanfox"};

    public MangaFox(Context context) {
        super(context);
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
//        Document doc = Jsoup.parse(url, timeout);
        return super.parse(url, timeout);
    }


    @Override
    protected void getDocument(URL url) throws IOException {
        doc = Jsoup.connect(url.toExternalForm().replace("http://", "https://"))
                .cookie("isAdult", "1")
                .header("host", "fanfox.net")
                .header("referer", "http://fanfox.net/")
                .header("accept", "text/html")
                .timeout(timeout)
                .followRedirects(true)
                .get();
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
//        return super.images(url, timeout);
        Log.e("OWO", "images: EVALLING");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        Bindings obj = (Bindings) engine.eval("function(p,a,c,k,e,d){e=function(c){return(c<a?\"\":e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)d[e(c)]=k[c]||e(c);k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1;};while(c--)if(k[c])p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c]);return p;}('k e(){2 f=\"//8.b.7/c/3/4/6.0/g\";2 1=[\"/n.h?5=m&9=a\",\"/l.h?5=j&9=a\"];o(2 i=0;i<1.u;i++){s(i==0){1[i]=\"//8.b.7/c/3/4/6.0/g\"+1[i];p}1[i]=f+1[i]}q 1}2 d;d=e();r=t;',31,31,'|pvalue|var|manga|36721|token|001|me|zjcdn|ttl|1621008000|mangafox|store||dm5imagefun|pix|compressed|jpg||595acd47e6cb27bd5501f3950ec12b779cdcdbad|function|m20201203_111915_617|414cbac3ede9509c9a28c2300bd1e5c23d596dc7|m20201203_111915_616|for|continue|return|currentimageid|if|19268319|length'.split('|'),0,{})");
        String s = (String) obj.get("d");
        Log.e("OWO", "images: " + s);
        return new ArrayList<>();
    }

    @Override
    public boolean accepts(URL url) {
        String hostname = url.getHost();
        for (String accept : accepts) {
            if (hostname.contains(accept)) {
                return true;
            }
        }
        return false;
    }
}
