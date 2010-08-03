/**
 *
 */
package org.richfaces.resource;

import javax.faces.context.FacesContext;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public interface ResourceCodec {
    
    public String encodeResource(FacesContext context, String resourceName, Object resourceData, String resourceVersion);

    public String decodeResourceName(FacesContext context, String requestPath);

    public Object decodeResourceData(FacesContext context, String requestPath);

    public String decodeResourceVersion(FacesContext context, String requestPath);

    public String getResourceKey(FacesContext context, String requestPath);
}
