package com.dianping.wizard.widget.concurrent;

import com.dianping.wizard.exception.WidgetException;
import com.dianping.wizard.widget.InvocationContext;
import com.dianping.wizard.widget.RenderingResult;
import com.dianping.wizard.widget.Widget;
import com.dianping.wizard.widget.WidgetRenderer;
import com.dianping.wizard.widget.interceptor.Interceptor;
import com.dianping.wizard.widget.interceptor.InterceptorConfig;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author ltebean
 */
public class ConcurrentRenderer implements WidgetRenderer{

    private Logger logger= Logger.getLogger(this.getClass());

    @Override
    public RenderingResult render(Widget widget, String mode, Map<String, Object> params) {
        if (widget == null) {
            throw new IllegalArgumentException("widget can not be null");
        }
        if (params == null) {
            params=new HashMap<String, Object>();
        }
        Map<String,Future<RenderingResult>> tasks = LayoutParser.parseAndExecute(widget,mode,params);
        Iterator<Interceptor> interceptors= InterceptorConfig.getInterceptors("concurrent");
        InvocationContext invocation=new InvocationContext(widget, mode,params, interceptors);
        invocation.getContext().put("tasks",tasks);
        RenderingResult result=new RenderingResult();
        try {
            String resultCode=invocation.invoke();
            if(resultCode==InvocationContext.SUCCESS){
                result.output=invocation.getOutput();
                result.script=invocation.getScript();
            }else if(resultCode==InvocationContext.NONE){
                return result;
            }else{
                throw new WidgetException("unknown result code-"+resultCode+" returned by widget:"+widget.name);
            }
        } catch (Exception e) {
            logger.error("rendering error",e);
        }
        return result;

    }
}
