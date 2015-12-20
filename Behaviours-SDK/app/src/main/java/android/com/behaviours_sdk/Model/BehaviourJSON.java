package android.com.behaviours_sdk.Model;

/**
 * Created by Mohammed on 12/16/2015.


import javax.annotation.Generated;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
 */
public class BehaviourJSON {

    private String ver;
    private String name;
    private String path;
    private Parameters parameters;

    /**
     * @return
     * The ver
     */
    public String getVer() {
        return ver;
    }

    /**
     * @param ver
     * The ver
     */
    public void setVer(String ver) {
        this.ver = ver;
    }

    /**
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     * The path
     */
    public String getPath() {
        return path;
    }

    /**
     *
     * @param path
     * The path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     * @return
     * The parameters
     */
    public Parameters getParameters() {

        return parameters;
    }

    /**
     *
     * @param parameters
     * The parameters
     */
    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

/*    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
*/
}
