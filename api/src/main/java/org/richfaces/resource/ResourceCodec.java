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
    
    public String encodeResourceRequestPath(FacesContext context, String libraryName, String resourceName, 
        Object resourceData, String resourceVersion);
    
    public String encodeJSFMapping(FacesContext context, String resourcePath);
    
    public String decodeResourceName(FacesContext context, String requestPath);

    public String decodeLibraryName(FacesContext context, String requestPath);

    public Object decodeResourceData(FacesContext context, String requestPath);

    public String decodeResourceVersion(FacesContext context, String requestPath);

    public String getResourceKey(FacesContext context, String requestPath);
}
