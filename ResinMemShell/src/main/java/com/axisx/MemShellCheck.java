package com.axisx;

import com.sun.org.apache.bcel.internal.Repository;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class MemShellCheck implements Filter {

    private static Integer count=0;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-type", "text/plain;charset=UTF-8");
        String id = req.getParameter("id");
        String action=req.getParameter("action");
        try {
            Object context=getContext(); //WebAppContext
            if(action.equals("scan")){
                /**
                 * Listener
                 */
                resp.getWriter().println("/**\n" + "* Listener\n" + "*/\n");
                List<Object> listeners=(List<Object>)getFV(context,"_listeners");
                if (listeners != null || listeners.size() != 0) {
                    List<ServletRequestListener> newListeners = new ArrayList<>();
                    for (Object o : listeners) {
                        Object l=getFV(o,"_object");
                        if (l instanceof ServletRequestListener) {
                            newListeners.add((ServletRequestListener) l);
                        }
                    }
                    String listenerTName=null;
                    for (ServletRequestListener listener : newListeners) {
                        listenerTName=listener.getClass().getName();
                        int idw=IDGenerate();
                        resp.getWriter().println(Template(idw,listener.hashCode(),null,null,listenerTName,listener.getClass().getClassLoader().getClass().getName(),listener.getClass()));
                        HashMap<String, Object> memShellInfo= CheckStruct.newMemShellInfo(null,null,listenerTName,listener.getClass().getClassLoader().getClass().getName(),listener.getClass(),"Listener");
                        CheckStruct.set(listener.hashCode(),memShellInfo);
                    }
                }


                /**
                 * Filter
                 */
                resp.getWriter().println("/**\n" + "* Filter\n" + "*/\n");

                try{
                    Object filterManager=getFV(context,"_filterManager");
                    HashMap filterInstance=(HashMap)getFV(filterManager,"_instances");
                    Object filterMapper=getFV(context,"_filterMapper");
                    ArrayList filterMappers=(ArrayList)getFV(filterMapper,"_filterMap");


                    Iterator<String> it = filterMappers.iterator();
                    int i=0;
                    while(it.hasNext()) {
                        Object lis=it.next();
                        String filterName1=String.valueOf(getFV(lis,"_filterName"));
                        String urlPattern=String.valueOf(getFV(lis,"_urlPattern"));
                        Object filter=filterInstance.get(filterName1);
                        if(filter!=null){
                            String filterClassName =filter.getClass().getName();
                            String filterClassLoaderName = filter.getClass().getClassLoader().getClass().getName();
                            int idw=IDGenerate();
                            resp.getWriter().println(Template(idw,filter.hashCode(),filterName1,urlPattern,filterClassName,filterClassLoaderName,filter.getClass()));
                            HashMap<String, Object> memShellInfo= CheckStruct.newMemShellInfo(filterName1,urlPattern,filterClassName,filterClassLoaderName,filter.getClass(),"Filter");
                            CheckStruct.set(filter.hashCode(),memShellInfo);
                            i++;
                        }

                    }
                }catch (Exception e){e.printStackTrace();}

                /**
                 * Servlet
                 */
                resp.getWriter().println("/**\n" + "* Servlet\n" + "*/\n");
                try{
                    Object servletManager=getFV(context,"_servletManager");
                    HashMap servlets=(HashMap)getFV(servletManager,"_servlets");
                    Object servletMapper=getFV(context,"_servletMapper");
                    HashMap servleturlPatterns=(HashMap)getFV(servletMapper,"_urlPatterns");
                    int servletId = 0;
                    Iterator<Map.Entry> entriesUrl = servleturlPatterns.entrySet().iterator();


                    while (entriesUrl.hasNext()) {
                        Map.Entry entry = (Map.Entry) entriesUrl.next();
                        String servletName1 = (String) entry.getKey();
                        HashSet urlset=(HashSet) entry.getValue();
                        String urlPattern=String.valueOf(urlset.iterator().next());
                        Object servletConfigImpl=(Object) servlets.get(servletName1);
                        String servletClassName=String.valueOf(getFV(servletConfigImpl,"_servletClassName"));
                        Class servletClass=(Class) getFV(servletConfigImpl,"_servletClass");
                        int idw=IDGenerate();
                        String servletClassLoaderName=String.valueOf(servletClass.newInstance().getClass().getClassLoader().getClass().getName());

                        resp.getWriter().println(Template(idw,servletClass.hashCode(),servletName1,urlPattern,servletClassName,servletClassLoaderName,servletClass));
                        HashMap<String, Object> memShellInfo= CheckStruct.newMemShellInfo(servletName1,urlPattern,servletClassName,servletClassLoaderName,servletClass,"Servlet");
                        CheckStruct.set(servletClass.hashCode(),memShellInfo);
                        servletId++;
                    }

                }catch (Exception e){e.printStackTrace();}
//                Field f12=ServletHandler.getClass().getDeclaredField("_servletMappings");
//                f12.setAccessible(true);
//                Object servletmapping = f12.get(ServletHandler);
//                Object[] servletmappings = new Object[Array.getLength(servletmapping)];
//
//                for(int var8 = 0; var8 < Array.getLength(servletmapping); ++var8) {
//                    Object fm = Array.get(servletmapping, var8);
//                    Field f3 = fm.getClass().getDeclaredField("_servletName");
//                    f3.setAccessible(true);
//                    String servletName1= (String) f3.get(fm);
//                    Field f4 = fm.getClass().getDeclaredField("_pathSpecs");
//                    f4.setAccessible(true);
//                    String[] urlpatterns= (String[]) f4.get(fm);
//
//                    Field f6 = ServletHandler.getClass().getDeclaredField("_servlets");
//                    f6.setAccessible(true);
//                    Object servlets=f6.get(ServletHandler);
//                    for(int var9 = 0; var9 < Array.getLength(servlets); ++var9) {
//                        Object servlet = Array.get(servlets, var9);
//                        Field f7= servlet.getClass().getSuperclass().getDeclaredField("_name");
//                        f7.setAccessible(true);
//                        String name= (String) f7.get(servlet);
//                        if (name.equals(servletName1)){
//                            Field f8= servlet.getClass().getSuperclass().getSuperclass().getDeclaredField("_class");;
//                            f8.setAccessible(true);
//                            Class servletClass= (Class) f8.get(servlet);
//                            int idw=IDGenerate();
//                            String servletClassName = servletClass.getClass().getName();
//                            String servletClassLoaderName = null;
//                            try {
//                                servletClassLoaderName = servletClass.getClass().getClassLoader().getClass().getName();
//                            } catch (Exception e) {}
//                            resp.getWriter().println(Template(idw,servletClass.hashCode(),servletName1,arrayToString(urlpatterns),servletClassName,servletClassLoaderName,servletClass));
//                            HashMap<String, Object> memShellInfo= CheckStruct.newMemShellInfo(servletName1,arrayToString(urlpatterns),servletClassName,servletClassLoaderName,servletClass,"Servlet");
//                            CheckStruct.set(servletClass.hashCode(),memShellInfo);
//                        }
//                    }
//                }
            }else if (action.equals("dump")){
                String className= (String) CheckStruct.get(Integer.valueOf(id),"class");
                byte[] classBytes = Repository.lookupClass(Class.forName(className)).getBytes();
                resp.addHeader("content-Type", "application/octet-stream");
                String filename = Class.forName(className).getSimpleName() + ".class";

                String agent = req.getHeader("User-Agent");
                if (agent.toLowerCase().indexOf("chrome") > 0) {
                    resp.addHeader("content-Disposition", "attachment;filename=" + new String(filename.getBytes("UTF-8"), "ISO8859-1"));
                } else {
                    resp.addHeader("content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
                }
                ServletOutputStream outDumper = resp.getOutputStream();
                outDumper.write(classBytes, 0, classBytes.length);
                outDumper.close();
            }

        } catch (Exception e) {e.printStackTrace();}
//        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    /**
     * Jetty
     */
    public static synchronized Object getContext() throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        ClassLoader classLoader=Thread.currentThread().getContextClassLoader();
        Class servletInvocation=classLoader.loadClass("com.caucho.server.dispatch.ServletInvocation");

        Object contextRequest = servletInvocation.getMethod("getContextRequest").invoke(null);
        Object webapp = contextRequest.getClass().getMethod("getWebApp").invoke(contextRequest);
        return webapp;
    }

    public String Template(Integer count,Integer ID,String Name,String Pattern,String Class,String ClassLoader,Class<?> aClass){
        if (Pattern!=null){
            return String.format("Count:%d\nID: %d\nName: %s\nPattern: %s\nClass: %s\nClassLoader: %s\nFile Path: %s\n",count,ID, Name,Pattern,Class,ClassLoader,classFileIsExists(aClass));
        }else {
            return String.format("Count:%d\nID: %d\nClass: %s\nClassLoader: %s\nFile Path: %s\n",count,ID,Class,ClassLoader,classFileIsExists(aClass));
        }

    }

    public Integer IDGenerate(){
        count=count+1;
        return count;
    }

    public Object getFV(Object var0, String var1) throws Exception {
            Field var2 = null;
            Class var3 = var0.getClass();

            while(var3 != Object.class) {
                try {
                    var2 = var3.getDeclaredField(var1);
                    break;
                } catch (NoSuchFieldException var5) {
                    var3 = var3.getSuperclass();
                }
            }

            if (var2 == null) {
                throw new NoSuchFieldException(var1);
            } else {
                var2.setAccessible(true);
                return var2.get(var0);
            }
        }

    public static String arrayToString(String[] str) {
        String res = "";
        for (String s : str) {
            res += String.format("%s,", s);
        }
        res = res.substring(0, res.length() - 1);
        return res;
    }

    public static String classFileIsExists(Class clazz) {
        if (clazz == null) {
            return "class is null";
        }
        String className = clazz.getName();
        String classNamePath = className.replace(".", "/") + ".class";
        URL is = clazz.getClassLoader().getResource(classNamePath);
        if (is == null) {
            return "There is no corresponding class file on disk, it may be MemShell";
        } else {
            return is.getPath();
        }
    }


    public static class CheckStruct {

        static HashMap<Integer, HashMap<String, Object>> memShells = new HashMap<>();

        public static boolean set(Integer id, HashMap<String, Object> info) throws Exception {
            if (memShells.containsKey(id)){
                System.out.println(String.format("%s already exists !!!", id));
                return false;
            }
            if (memShells.put(id, info) != null){
                return true;
            }
            else {
                return false;
            }
        }

        public static Object get(Integer id, String key) {
            try{
                return memShells.get(id).get(key);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        public static HashMap<String, Object> newMemShellInfo(Object name,Object pattern,Object classC,Object ClassLoader,Object filePath,Object type){
            HashMap<String, Object> memShellInfo = new HashMap<String, Object>();
            memShellInfo.put("name", name);
            memShellInfo.put("pattern", pattern);
            memShellInfo.put("class", classC);
            memShellInfo.put("classloader", ClassLoader);
            memShellInfo.put("filePath", filePath);
            memShellInfo.put("type", type);
            return memShellInfo;
        }

    }

}
