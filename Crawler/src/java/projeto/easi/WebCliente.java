package projeto.easi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Matheus
 */
public class WebCliente {
    
    private static String domain = "", upperDomain = "", page = "";
    private static Map<String, Object> params = new HashMap<String, Object>();
    private static Map<String, Object> headers = new HashMap<String, Object>();
    
    public static enum HttpMethod {
        
        POST,
        GET;
    }
    
    private static String encodeURIComponent(String component) {
        try {
            return URLEncoder.encode(component, "UTF-8");
        } catch (Exception e) {
            // Nothing
        }
        
        return component;
    }

    //transformar os parametros passados para uma url: chave=valor&
    private static String mapToURLMap(Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            StringBuilder str = new StringBuilder();
            
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                str.append(entry.getKey());
                str.append("=");
                str.append(encodeURIComponent(entry.getValue().toString()));
                str.append("&");
            }
            
            str.deleteCharAt(str.length() - 1);
            return str.toString().replaceAll("\\s", "+");
        }
        
        return null;
    }
    
    public static String getPageContent(String urlPagina, HttpMethod method) {
        try {
            String paramsStr = mapToURLMap(params);
            //se houver parametro a ser passado pelo metodo get
            if (method.equals(HttpMethod.GET) && paramsStr != null) {
                urlPagina = urlPagina + "?" + paramsStr;
            }
            
            URL url = new URL(urlPagina);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                conn.addRequestProperty(entry.getKey(), entry.getValue().toString());
            }
            
            if (method.equals(HttpMethod.POST)) {
                conn.getOutputStream().write(paramsStr.getBytes());
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                Map<String, List<String>> responseHeaders = conn.getHeaderFields();
                if (responseHeaders.containsKey("Set-Cookie")) {
                    String setCookies = responseHeaders.get("Set-Cookie").get(0);
                    String[] parts = setCookies.split(";\\s+");
                    for (int i = 0; i < parts.length; i++) {
                        System.out.println("[PAGE CONTENT] SET-COOKIE <" + i + ">: " + parts[i]);
                    }
                    headers.put("Cookie", parts[0]);
                }
                if (responseHeaders.containsKey("Cookie")) {
                    String setCookies = responseHeaders.get("Cookie").get(0);
                    String[] parts = setCookies.split(";\\s+");
                    for (int i = 0; i < parts.length; i++) {
                        System.out.println("[PAGE CONTENT] COOKIE <" + i + ">: " + parts[i]);
                    }
                    headers.put("Cookie", parts[0]);
                }
                
                //imprimir no servidor todos os 
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    System.out.println("--------------HEADER-------------------" +entry.getKey() +" : "+ entry.getValue().toString());
                }
                
                StringBuilder str = new StringBuilder();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                
                while ((line = br.readLine()) != null) {
                    str.append(line);
                    str.append("\n");
                }
                
                str.deleteCharAt(str.length() - 1);
                
                return str.toString();
            } else {
                throw new RuntimeException("HTTP ERROR: " + responseCode);
            }
        } catch (Exception ex) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream writer = new PrintStream(out);
            ex.printStackTrace(writer);
            return out.toString();
        }
    }
    
    public static String prependLinks(String html) {
        domain = page;
        int domainEnd = domain.indexOf("/", domain.indexOf("://") + 3);
        if (domainEnd < 0) {
            upperDomain = domain;
        } else {
            upperDomain = domain.substring(0, domainEnd);
        }
        if (upperDomain.endsWith("/")) {
            upperDomain = upperDomain.substring(0, upperDomain.length() - 1);
        }
        if (!domain.endsWith("/")) {
            domain = domain + "/";
        }
        
        StringBuffer codigo = new StringBuffer();
        String addr;
        Matcher m = Pattern.compile("(?i)(src=|href=|url\\(\\s*)(['\\\"]?)([^'\\\"\\)]+)(['\\\"\\)]?)").matcher(html);
        
        while (m.find()) {
            addr = m.group(3);
            if (addr.startsWith("http") || addr.startsWith("#")) {
                m.appendReplacement(codigo, "$1$2$3$4");
            } else if (addr.startsWith("/")) {
                m.appendReplacement(codigo, "$1$2" + upperDomain + "$3$4");
            } else {
                m.appendReplacement(codigo, "$1$2" + domain + "$3$4");
            }
        }
        m.appendTail(codigo);
        
        return codigo.toString();
    }
    
    public static String injectCodeAtHead(String html, String code) {
        int location = html.lastIndexOf("</head>");
        html = html.substring(0, location) + code + html.substring(location);
        
        return html;
    }
    
    public static String converteLink(String html, String page) {
        StringBuffer codigo = new StringBuffer();
        String addr;
        Matcher m = Pattern.compile("(?i)(<a.*?href=|<form.*?action=)(['\"]?)([^'#\"]+)(['\"]?)").matcher(html);
        
        while (m.find()) {
            addr = m.group(3);
            if (addr.startsWith("http") || addr.startsWith("#")) {
                m.appendReplacement(codigo, "$1$2" + page + "$3$4");
            } else if (addr.startsWith("/")) {
                m.appendReplacement(codigo, "$1$2" + "http://localhost:8084/Crawler/index.jsp?url=" + upperDomain + "$3$4");
            }
        }
        m.appendTail(codigo);
        
        return codigo.toString();
    }
    
    public static String loadPage(String url) {
        page = url;

//      params.put("q", "HADOUKEN");
//      params.put("t", "post");

        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Language", "pt-br,pt;q=0.8,en-us;q=0.5,en;q=0.3");
//      headers.put("Cookie", "_redmine_session=BAh7CDoPc2Vzc2lvbl9pZCIlMmYzYmJjYzgxODc3MzI1M2MyZGEyNjY0Y2RhNDkxMjU6DHVzZXJfaWRpDToQX2NzcmZfdG9rZW4iMWZQZ3dzTDRnZzJCQUc5UDdUYS94S0lkcXdrZlpKSTZWc3JqNmNtL3RVZnc9--e7f5d354a3ed5199c3e04d3b971eec1e50b84d1e");

        String html = getPageContent(page, HttpMethod.GET);
        html = prependLinks(html);
        html = injectCodeAtHead(html, "<script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/capt.js\"></script>");
        html = converteLink(html, "http://localhost:8084/Crawler/index.jsp?url=");        
        
        return html;
    }
}