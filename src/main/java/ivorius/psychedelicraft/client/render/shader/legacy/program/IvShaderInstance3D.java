package ivorius.psychedelicraft.client.render.shader.legacy.program;

import org.apache.logging.log4j.Logger;

/**
 * Created by Sollace on 4 Jan 2023
 */
public abstract class IvShaderInstance3D extends IvShaderInstance2D {
    protected IvShaderInstance3D(Logger logger) {
        super(logger);
    }

    public boolean isShaderActive() {
        return false;
    }

    public void trySettingUpShader(String vertexShaderFile, String fragmentShaderFile) {

    }
}
